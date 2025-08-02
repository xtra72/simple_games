# 7장: SimpleWorld 완전한 구현 가이드

## 목차
1. [인터페이스 설계와 구현](#1-인터페이스-설계와-구현)
2. [Ball 클래스 - 다중 인터페이스 구현](#2-ball-클래스---다중-인터페이스-구현)
3. [Box 클래스와 MovableBox](#3-box-클래스와-movablebox)
4. [Bounds 시스템 구현](#4-bounds-시스템-구현)
5. [SimpleWorld - 통합 관리자](#5-simpleworld---통합-관리자)
6. [실습 프로젝트들](#6-실습-프로젝트들)
7. [테스트 코드와 검증](#7-테스트-코드와-검증)

---

## 1. 인터페이스 설계와 구현

### 1.1 CollisionAction 열거형

먼저 충돌 행동을 정의하는 열거형을 만듭니다.

```java
/**
 * 충돌 시 취할 행동을 정의하는 열거형
 * 각 객체는 충돌 시 이 액션 중 하나를 취합니다.
 */
public enum CollisionAction {
    BOUNCE,    // 반사 - 속도 방향이 바뀜
    DESTROY,   // 파괴 - 객체가 제거됨
    STOP,      // 정지 - 속도가 0이 됨
    PASS,      // 통과 - 아무 일도 일어나지 않음
    CUSTOM     // 사용자 정의 - 특별한 처리
}
```

**구현 포인트:**
- 열거형은 고정된 상수들의 집합입니다
- 각 상수는 특정한 의미를 가집니다
- switch 문에서 사용하기 적합합니다

### 1.2 Paintable 인터페이스

화면에 그릴 수 있는 객체들을 위한 인터페이스입니다.

```java
import javafx.scene.canvas.GraphicsContext;

/**
 * 화면에 그릴 수 있는 객체를 나타내는 인터페이스
 * 모든 시각적 객체는 이 인터페이스를 구현해야 합니다.
 */
public interface Paintable {
    /**
     * 객체를 화면에 그립니다
     * @param gc 그래픽스 컨텍스트
     */
    void paint(GraphicsContext gc);
}
```

**인터페이스 설계 원칙:**
- 단일 책임: 오직 그리기 기능만 정의
- 간단함: 하나의 메서드만 포함
- 명확함: 메서드명이 목적을 명확히 표현

### 1.3 Movable 인터페이스

움직일 수 있는 객체들을 위한 인터페이스입니다.

```java
/**
 * 움직일 수 있는 객체를 나타내는 인터페이스
 * 위치와 속도를 가지고 시간에 따라 이동할 수 있습니다.
 */
public interface Movable {
    /**
     * 주어진 시간만큼 객체를 이동시킵니다
     * @param deltaTime 이동할 시간 (초 단위)
     */
    void move(double deltaTime);
    
    /**
     * X 방향 속도를 반환합니다
     * @return X 방향 속도 (픽셀/초)
     */
    double getDx();
    
    /**
     * Y 방향 속도를 반환합니다
     * @return Y 방향 속도 (픽셀/초)
     */
    double getDy();
    
    /**
     * X 방향 속도를 설정합니다
     * @param dx X 방향 속도 (픽셀/초)
     */
    void setDx(double dx);
    
    /**
     * Y 방향 속도를 설정합니다
     * @param dy Y 방향 속도 (픽셀/초)
     */
    void setDy(double dy);
}
```

**인터페이스 설계 이유:**
- 움직임에 필요한 모든 기능을 정의
- getter/setter로 속도 제어 가능
- 시간 기반 이동으로 부드러운 애니메이션

### 1.4 Boundable 인터페이스

경계를 가진 객체들을 위한 인터페이스입니다.

```java
/**
 * 경계를 가진 객체를 나타내는 인터페이스
 * 충돌 검사의 기반이 됩니다.
 */
public interface Boundable {
    /**
     * 객체의 경계를 반환합니다
     * @return 객체의 경계 정보
     */
    Bounds getBounds();
    
    /**
     * 다른 객체와 충돌하는지 확인합니다
     * @param other 충돌을 확인할 다른 객체
     * @return 충돌하면 true, 아니면 false
     */
    boolean isColliding(Boundable other);
}
```

### 1.5 Collidable 인터페이스

충돌 처리가 가능한 객체들을 위한 인터페이스입니다.

```java
/**
 * 충돌 처리가 가능한 객체를 나타내는 인터페이스
 * Boundable을 확장하여 충돌 감지뿐만 아니라 처리도 담당합니다.
 */
public interface Collidable extends Boundable {
    /**
     * 다른 객체와의 충돌을 처리합니다
     * @param other 충돌한 다른 객체
     */
    void handleCollision(Collidable other);
    
    /**
     * 충돌 시 취할 행동을 반환합니다
     * @return 충돌 액션
     */
    CollisionAction getCollisionAction();
    
    /**
     * 충돌 시 취할 행동을 설정합니다
     * @param action 새로운 충돌 액션
     */
    void setCollisionAction(CollisionAction action);
    
    /**
     * 객체가 제거되어야 하는지 확인합니다
     * @return 제거되어야 하면 true, 아니면 false
     */
    boolean isDestroyed();
}
```

**인터페이스 상속의 이점:**
- Collidable은 자동으로 Boundable의 모든 메서드를 포함
- 충돌 감지와 처리를 논리적으로 분리
- 확장성: 새로운 충돌 유형 추가 가능

---

## 2. Ball 클래스 - 다중 인터페이스 구현

Ball 클래스는 모든 인터페이스를 구현하는 핵심 클래스입니다.

```java
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 원형 공을 나타내는 클래스
 * 그리기, 움직임, 충돌을 모두 지원하는 완전한 게임 객체입니다.
 */
public class Ball implements Paintable, Movable, Collidable {
    // 위치와 크기
    private double x;
    private double y;
    private double radius;
    
    // 속도
    private double dx;
    private double dy;
    
    // 외형
    private Color color;
    
    // 충돌 처리
    private CollisionAction collisionAction;
    private boolean destroyed;
    
    // === 생성자들 ===
    
    /**
     * 기본 생성자 - 원점에 빨간 공 생성
     */
    public Ball() {
        this(0, 0, 10, Color.RED);
    }
    
    /**
     * 위치와 크기만 지정하는 생성자
     * @param x 중심 X 좌표
     * @param y 중심 Y 좌표  
     * @param radius 반지름
     */
    public Ball(double x, double y, double radius) {
        this(x, y, radius, Color.RED);
    }
    
    /**
     * 모든 속성을 지정하는 생성자
     * @param x 중심 X 좌표
     * @param y 중심 Y 좌표
     * @param radius 반지름
     * @param color 색상
     * @throws IllegalArgumentException 반지름이 0 이하이거나 색상이 null인 경우
     */
    public Ball(double x, double y, double radius, Color color) {
        if (radius <= 0) {
            throw new IllegalArgumentException("반지름은 0보다 커야 합니다: " + radius);
        }
        if (color == null) {
            throw new IllegalArgumentException("색상은 null일 수 없습니다");
        }
        
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.color = color;
        this.dx = 0;
        this.dy = 0;
        this.collisionAction = CollisionAction.BOUNCE;
        this.destroyed = false;
    }
    
    // === Paintable 인터페이스 구현 ===
    
    @Override
    public void paint(GraphicsContext gc) {
        // 채우기 색상 설정
        gc.setFill(color);
        // 원 그리기 (중심 좌표에서 반지름만큼 뺀 좌표부터 그림)
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        
        // 테두리 그리기 (선택사항)
        gc.setStroke(color.darker());
        gc.setLineWidth(1);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
    }
    
    // === Movable 인터페이스 구현 ===
    
    @Override
    public void move(double deltaTime) {
        x += dx * deltaTime;
        y += dy * deltaTime;
    }
    
    @Override
    public double getDx() {
        return dx;
    }
    
    @Override
    public double getDy() {
        return dy;
    }
    
    @Override
    public void setDx(double dx) {
        this.dx = dx;
    }
    
    @Override
    public void setDy(double dy) {
        this.dy = dy;
    }
    
    // === Boundable 인터페이스 구현 ===
    
    @Override
    public Bounds getBounds() {
        return new CircleBounds(x, y, radius);
    }
    
    @Override
    public boolean isColliding(Boundable other) {
        return getBounds().intersects(other.getBounds());
    }
    
    // === Collidable 인터페이스 구현 ===
    
    @Override
    public void handleCollision(Collidable other) {
        switch (collisionAction) {
            case BOUNCE:
                handleBounce(other);
                break;
            case DESTROY:
                destroyed = true;
                break;
            case STOP:
                dx = 0;
                dy = 0;
                break;
            case PASS:
                // 아무것도 하지 않음
                break;
            case CUSTOM:
                handleCustomCollision(other);
                break;
        }
    }
    
    /**
     * 반사 처리를 위한 헬퍼 메서드
     * @param other 충돌한 다른 객체
     */
    private void handleBounce(Collidable other) {
        Bounds myBounds = getBounds();
        Bounds otherBounds = other.getBounds();
        
        if (myBounds instanceof CircleBounds && otherBounds instanceof CircleBounds) {
            // 원-원 충돌
            CircleBounds myCircle = (CircleBounds) myBounds;
            CircleBounds otherCircle = (CircleBounds) otherBounds;
            
            // 충돌 벡터 계산
            double dx = myCircle.getCenterX() - otherCircle.getCenterX();
            double dy = myCircle.getCenterY() - otherCircle.getCenterY();
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            if (distance > 0) {
                // 정규화된 충돌 벡터
                dx /= distance;
                dy /= distance;
                
                // 속도 반사 (간단한 버전)
                this.dx = Math.abs(this.dx) * (dx > 0 ? 1 : -1);
                this.dy = Math.abs(this.dy) * (dy > 0 ? 1 : -1);
            }
        } else if (myBounds instanceof CircleBounds && otherBounds instanceof RectangleBounds) {
            // 원-사각형 충돌
            bounceOffRectangle((RectangleBounds) otherBounds);
        }
    }
    
    /**
     * 사각형과의 반사 처리
     * @param rect 사각형 경계
     */
    private void bounceOffRectangle(RectangleBounds rect) {
        // 간단한 버전: 공의 중심 위치에 따라 반사 방향 결정
        double centerX = rect.getX() + rect.getWidth() / 2;
        double centerY = rect.getY() + rect.getHeight() / 2;
        
        double deltaX = x - centerX;
        double deltaY = y - centerY;
        
        // 가로가 더 긴지 세로가 더 긴지 판단
        if (Math.abs(deltaX) / rect.getWidth() > Math.abs(deltaY) / rect.getHeight()) {
            // 좌우 충돌
            dx = -dx;
        } else {
            // 상하 충돌
            dy = -dy;
        }
    }
    
    /**
     * 사용자 정의 충돌 처리 (하위 클래스에서 오버라이드 가능)
     * @param other 충돌한 다른 객체
     */
    protected void handleCustomCollision(Collidable other) {
        // 기본적으로는 반사와 동일
        handleBounce(other);
    }
    
    @Override
    public CollisionAction getCollisionAction() {
        return collisionAction;
    }
    
    @Override
    public void setCollisionAction(CollisionAction action) {
        if (action == null) {
            throw new IllegalArgumentException("충돌 액션은 null일 수 없습니다");
        }
        this.collisionAction = action;
    }
    
    @Override
    public boolean isDestroyed() {
        return destroyed;
    }
    
    // === 기본 접근자 메서드들 ===
    
    public double getX() { return x; }
    public double getY() { return y; }
    public double getRadius() { return radius; }
    public Color getColor() { return color; }
    
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    
    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("색상은 null일 수 없습니다");
        }
        this.color = color;
    }
    
    /**
     * 주어진 점이 공 안에 있는지 확인
     * @param px X 좌표
     * @param py Y 좌표
     * @return 점이 공 안에 있으면 true
     */
    public boolean contains(double px, double py) {
        double distanceSquared = (px - x) * (px - x) + (py - y) * (py - y);
        return distanceSquared <= radius * radius;
    }
    
    /**
     * 공의 면적을 계산
     * @return 면적 (π × r²)
     */
    public double getArea() {
        return Math.PI * radius * radius;
    }
}
```

**구현 포인트 설명:**

1. **다중 인터페이스 구현**: `implements Paintable, Movable, Collidable`
2. **방어적 프로그래밍**: 생성자에서 매개변수 유효성 검사
3. **충돌 처리**: switch 문을 사용한 액션별 처리
4. **물리적 정확성**: 원-원, 원-사각형 충돌 계산
5. **확장성**: `handleCustomCollision` 메서드로 하위 클래스 확장 지원

---

## 3. Box 클래스와 MovableBox

### 3.1 Box 클래스

사각형 객체를 나타내는 클래스입니다.

```java
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 사각형 박스를 나타내는 클래스
 * 경계나 장애물로 주로 사용됩니다.
 */
public class Box implements Paintable, Collidable {
    // 위치와 크기
    private double x;
    private double y;
    private double width;
    private double height;
    
    // 외형
    private Color color;
    
    // 충돌 처리
    private CollisionAction collisionAction;
    private boolean destroyed;
    
    // === 생성자들 ===
    
    /**
     * 기본 생성자
     */
    public Box() {
        this(0, 0, 50, 50, Color.GRAY);
    }
    
    /**
     * 위치와 크기를 지정하는 생성자
     * @param x 좌상단 X 좌표
     * @param y 좌상단 Y 좌표
     * @param width 너비
     * @param height 높이
     */
    public Box(double x, double y, double width, double height) {
        this(x, y, width, height, Color.GRAY);
    }
    
    /**
     * 모든 속성을 지정하는 생성자
     * @param x 좌상단 X 좌표
     * @param y 좌상단 Y 좌표
     * @param width 너비
     * @param height 높이
     * @param color 색상
     */
    public Box(double x, double y, double width, double height, Color color) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("너비와 높이는 0보다 커야 합니다");
        }
        if (color == null) {
            throw new IllegalArgumentException("색상은 null일 수 없습니다");
        }
        
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
        this.collisionAction = CollisionAction.BOUNCE;
        this.destroyed = false;
    }
    
    // === Paintable 인터페이스 구현 ===
    
    @Override
    public void paint(GraphicsContext gc) {
        // 채우기
        gc.setFill(color);
        gc.fillRect(x, y, width, height);
        
        // 테두리
        gc.setStroke(color.darker());
        gc.setLineWidth(2);
        gc.strokeRect(x, y, width, height);
    }
    
    // === Boundable 인터페이스 구현 ===
    
    @Override
    public Bounds getBounds() {
        return new RectangleBounds(x, y, width, height);
    }
    
    @Override
    public boolean isColliding(Boundable other) {
        return getBounds().intersects(other.getBounds());
    }
    
    // === Collidable 인터페이스 구현 ===
    
    @Override
    public void handleCollision(Collidable other) {
        switch (collisionAction) {
            case BOUNCE:
                // Box는 보통 움직이지 않으므로 다른 객체가 반사되도록 함
                // 실제 반사는 충돌한 객체에서 처리
                break;
            case DESTROY:
                destroyed = true;
                break;
            case STOP:
                // Box는 기본적으로 움직이지 않음
                break;
            case PASS:
                // 아무것도 하지 않음
                break;
            case CUSTOM:
                handleCustomCollision(other);
                break;
        }
    }
    
    /**
     * 사용자 정의 충돌 처리
     * @param other 충돌한 다른 객체
     */
    protected void handleCustomCollision(Collidable other) {
        // 기본적으로 아무것도 하지 않음
    }
    
    @Override
    public CollisionAction getCollisionAction() {
        return collisionAction;
    }
    
    @Override
    public void setCollisionAction(CollisionAction action) {
        if (action == null) {
            throw new IllegalArgumentException("충돌 액션은 null일 수 없습니다");
        }
        this.collisionAction = action;
    }
    
    @Override
    public boolean isDestroyed() {
        return destroyed;
    }
    
    // === 접근자 메서드들 ===
    
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public Color getColor() { return color; }
    
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    
    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("색상은 null일 수 없습니다");
        }
        this.color = color;
    }
    
    /**
     * 주어진 점이 박스 안에 있는지 확인
     * @param px X 좌표
     * @param py Y 좌표
     * @return 점이 박스 안에 있으면 true
     */
    public boolean contains(double px, double py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }
    
    /**
     * 박스의 면적을 계산
     * @return 면적 (너비 × 높이)
     */
    public double getArea() {
        return width * height;
    }
}
```

### 3.2 MovableBox 클래스

Box를 상속받아 움직임 기능을 추가한 클래스입니다.

```java
import javafx.scene.paint.Color;

/**
 * 움직일 수 있는 사각형 박스 클래스
 * Box를 상속받아 Movable 인터페이스를 추가로 구현합니다.
 */
public class MovableBox extends Box implements Movable {
    // 속도
    private double dx;
    private double dy;
    
    // === 생성자들 ===
    
    /**
     * 기본 생성자
     */
    public MovableBox() {
        super();
        this.dx = 0;
        this.dy = 0;
    }
    
    /**
     * 위치와 크기를 지정하는 생성자
     * @param x 좌상단 X 좌표
     * @param y 좌상단 Y 좌표
     * @param width 너비
     * @param height 높이
     */
    public MovableBox(double x, double y, double width, double height) {
        super(x, y, width, height);
        this.dx = 0;
        this.dy = 0;
    }
    
    /**
     * 모든 속성을 지정하는 생성자
     * @param x 좌상단 X 좌표
     * @param y 좌상단 Y 좌표
     * @param width 너비
     * @param height 높이
     * @param color 색상
     */
    public MovableBox(double x, double y, double width, double height, Color color) {
        super(x, y, width, height, color);
        this.dx = 0;
        this.dy = 0;
    }
    
    // === Movable 인터페이스 구현 ===
    
    @Override
    public void move(double deltaTime) {
        setX(getX() + dx * deltaTime);
        setY(getY() + dy * deltaTime);
    }
    
    @Override
    public double getDx() {
        return dx;
    }
    
    @Override
    public double getDy() {
        return dy;
    }
    
    @Override
    public void setDx(double dx) {
        this.dx = dx;
    }
    
    @Override
    public void setDy(double dy) {
        this.dy = dy;
    }
    
    // === 충돌 처리 오버라이드 ===
    
    @Override
    public void handleCollision(Collidable other) {
        switch (getCollisionAction()) {
            case BOUNCE:
                handleBounce(other);
                break;
            case STOP:
                dx = 0;
                dy = 0;
                break;
            default:
                super.handleCollision(other);
                break;
        }
    }
    
    /**
     * MovableBox의 반사 처리
     * @param other 충돌한 다른 객체
     */
    private void handleBounce(Collidable other) {
        Bounds myBounds = getBounds();
        Bounds otherBounds = other.getBounds();
        
        // 간단한 반사 처리
        if (otherBounds instanceof RectangleBounds) {
            RectangleBounds otherRect = (RectangleBounds) otherBounds;
            RectangleBounds myRect = (RectangleBounds) myBounds;
            
            // 충돌 방향 판단
            double overlapX = Math.min(myRect.getX() + myRect.getWidth(), 
                                     otherRect.getX() + otherRect.getWidth()) - 
                             Math.max(myRect.getX(), otherRect.getX());
            double overlapY = Math.min(myRect.getY() + myRect.getHeight(), 
                                     otherRect.getY() + otherRect.getHeight()) - 
                             Math.max(myRect.getY(), otherRect.getY());
            
            if (overlapX < overlapY) {
                // 좌우 충돌
                dx = -dx;
            } else {
                // 상하 충돌
                dy = -dy;
            }
        } else {
            // 원과의 충돌 - 간단한 처리
            dx = -dx;
            dy = -dy;
        }
    }
}
```

**상속과 인터페이스의 조합:**
- Box의 모든 기능을 상속받음
- Movable 인터페이스를 추가로 구현
- 필요한 메서드만 오버라이드하여 특화

---

## 4. Bounds 시스템 구현

충돌 검사를 위한 경계 시스템을 구현합니다.

### 4.1 Bounds 추상 클래스

```java
/**
 * 모든 경계 타입의 기본 클래스
 */
public abstract class Bounds {
    /**
     * 다른 경계와 교차하는지 확인
     * @param other 다른 경계
     * @return 교차하면 true
     */
    public abstract boolean intersects(Bounds other);
    
    /**
     * 주어진 점이 경계 안에 있는지 확인
     * @param x X 좌표
     * @param y Y 좌표
     * @return 점이 경계 안에 있으면 true
     */
    public abstract boolean contains(double x, double y);
}
```

### 4.2 CircleBounds 클래스

```java
/**
 * 원형 경계를 나타내는 클래스
 */
public class CircleBounds extends Bounds {
    private double centerX;
    private double centerY;
    private double radius;
    
    public CircleBounds(double centerX, double centerY, double radius) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
    }
    
    @Override
    public boolean intersects(Bounds other) {
        if (other instanceof CircleBounds) {
            return intersectsCircle((CircleBounds) other);
        } else if (other instanceof RectangleBounds) {
            return intersectsRectangle((RectangleBounds) other);
        }
        return false;
    }
    
    /**
     * 다른 원과의 교차 검사
     */
    private boolean intersectsCircle(CircleBounds other) {
        double dx = centerX - other.centerX;
        double dy = centerY - other.centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance <= (radius + other.radius);
    }
    
    /**
     * 사각형과의 교차 검사
     */
    private boolean intersectsRectangle(RectangleBounds rect) {
        // 원의 중심에서 사각형의 가장 가까운 점까지의 거리 계산
        double closestX = Math.max(rect.getX(), 
                         Math.min(centerX, rect.getX() + rect.getWidth()));
        double closestY = Math.max(rect.getY(), 
                         Math.min(centerY, rect.getY() + rect.getHeight()));
        
        double dx = centerX - closestX;
        double dy = centerY - closestY;
        
        return (dx * dx + dy * dy) <= (radius * radius);
    }
    
    @Override
    public boolean contains(double x, double y) {
        double dx = x - centerX;
        double dy = y - centerY;
        return (dx * dx + dy * dy) <= (radius * radius);
    }
    
    // Getter 메서드들
    public double getCenterX() { return centerX; }
    public double getCenterY() { return centerY; }
    public double getRadius() { return radius; }
}
```

### 4.3 RectangleBounds 클래스

```java
/**
 * 사각형 경계를 나타내는 클래스
 */
public class RectangleBounds extends Bounds {
    private double x;
    private double y;
    private double width;
    private double height;
    
    public RectangleBounds(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    @Override
    public boolean intersects(Bounds other) {
        if (other instanceof RectangleBounds) {
            return intersectsRectangle((RectangleBounds) other);
        } else if (other instanceof CircleBounds) {
            return ((CircleBounds) other).intersects(this);
        }
        return false;
    }
    
    /**
     * 다른 사각형과의 교차 검사
     */
    private boolean intersectsRectangle(RectangleBounds other) {
        return !(x + width < other.x || 
                other.x + other.width < x ||
                y + height < other.y || 
                other.y + other.height < y);
    }
    
    @Override
    public boolean contains(double px, double py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }
    
    // Getter 메서드들
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
}
```

**충돌 검사 알고리즘:**
- 원-원: 중심 거리 ≤ 반지름 합
- 원-사각형: 원 중심에서 사각형 가장 가까운 점까지 거리 ≤ 반지름
- 사각형-사각형: AABB(Axis-Aligned Bounding Box) 교차 검사

---

## 5. SimpleWorld - 통합 관리자

모든 객체를 관리하는 중앙 클래스입니다.

```java
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 게임 세계를 관리하는 클래스
 * 모든 객체의 업데이트, 렌더링, 충돌 검사를 담당합니다.
 */
public class SimpleWorld {
    private double width;
    private double height;
    private List<Object> gameObjects;
    private List<Box> boundaries;
    private Color backgroundColor;
    
    /**
     * SimpleWorld 생성자
     * @param width 세계의 너비
     * @param height 세계의 높이
     */
    public SimpleWorld(double width, double height) {
        this.width = width;
        this.height = height;
        this.gameObjects = new ArrayList<>();
        this.boundaries = new ArrayList<>();
        this.backgroundColor = Color.LIGHTBLUE;
        
        createBoundaries();
    }
    
    /**
     * 경계 박스들을 생성합니다
     * 화면 가장자리에 보이지 않는 벽을 만들어 객체들이 화면 밖으로 나가지 않게 합니다.
     */
    public void createBoundaries() {
        double thickness = 20; // 경계 두께
        
        // 상단 경계
        Box topBoundary = new Box(-thickness, -thickness, width + 2 * thickness, thickness);
        topBoundary.setCollisionAction(CollisionAction.BOUNCE);
        boundaries.add(topBoundary);
        
        // 하단 경계
        Box bottomBoundary = new Box(-thickness, height, width + 2 * thickness, thickness);
        bottomBoundary.setCollisionAction(CollisionAction.BOUNCE);
        boundaries.add(bottomBoundary);
        
        // 좌측 경계
        Box leftBoundary = new Box(-thickness, 0, thickness, height);
        leftBoundary.setCollisionAction(CollisionAction.BOUNCE);
        boundaries.add(leftBoundary);
        
        // 우측 경계
        Box rightBoundary = new Box(width, 0, thickness, height);
        rightBoundary.setCollisionAction(CollisionAction.BOUNCE);
        boundaries.add(rightBoundary);
    }
    
    /**
     * 객체를 세계에 추가
     * @param obj 추가할 객체
     */
    public void addObject(Object obj) {
        if (obj != null) {
            gameObjects.add(obj);
        }
    }
    
    /**
     * 객체를 세계에서 제거
     * @param obj 제거할 객체
     */
    public void removeObject(Object obj) {
        gameObjects.remove(obj);
    }
    
    /**
     * 세계를 업데이트합니다
     * @param deltaTime 경과 시간 (초)
     */
    public void update(double deltaTime) {
        // 1단계: 움직일 수 있는 객체들 이동
        updateMovableObjects(deltaTime);
        
        // 2단계: 경계와의 충돌 검사
        checkBoundaryCollisions();
        
        // 3단계: 객체 간 충돌 검사
        checkObjectCollisions();
        
        // 4단계: 제거된 객체들 정리
        removeDestroyedObjects();
    }
    
    /**
     * 움직일 수 있는 객체들을 이동시킵니다
     */
    private void updateMovableObjects(double deltaTime) {
        for (Object obj : gameObjects) {
            if (obj instanceof Movable) {
                ((Movable) obj).move(deltaTime);
            }
        }
    }
    
    /**
     * 경계와의 충돌을 검사합니다
     */
    private void checkBoundaryCollisions() {
        for (Object obj : gameObjects) {
            if (obj instanceof Collidable) {
                Collidable collidable = (Collidable) obj;
                
                for (Box boundary : boundaries) {
                    if (collidable.isColliding(boundary)) {
                        collidable.handleCollision(boundary);
                        // 경계는 보통 영향받지 않지만, 필요시 처리
                    }
                }
            }
        }
    }
    
    /**
     * 객체 간 충돌을 검사합니다
     */
    private void checkObjectCollisions() {
        List<Collidable> collidables = new ArrayList<>();
        
        // Collidable 객체들만 추출
        for (Object obj : gameObjects) {
            if (obj instanceof Collidable) {
                collidables.add((Collidable) obj);
            }
        }
        
        // 모든 쌍에 대해 충돌 검사 (중복 방지)
        for (int i = 0; i < collidables.size(); i++) {
            for (int j = i + 1; j < collidables.size(); j++) {
                Collidable obj1 = collidables.get(i);
                Collidable obj2 = collidables.get(j);
                
                if (obj1.isColliding(obj2)) {
                    obj1.handleCollision(obj2);
                    obj2.handleCollision(obj1);
                }
            }
        }
    }
    
    /**
     * 제거된 객체들을 정리합니다
     */
    private void removeDestroyedObjects() {
        Iterator<Object> iterator = gameObjects.iterator();
        while (iterator.hasNext()) {
            Object obj = iterator.next();
            if (obj instanceof Collidable) {
                Collidable collidable = (Collidable) obj;
                if (collidable.isDestroyed()) {
                    iterator.remove();
                }
            }
        }
    }
    
    /**
     * 세계를 렌더링합니다
     * @param gc 그래픽스 컨텍스트
     */
    public void render(GraphicsContext gc) {
        // 배경 그리기
        gc.setFill(backgroundColor);
        gc.fillRect(0, 0, width, height);
        
        // 모든 Paintable 객체들 그리기
        for (Object obj : gameObjects) {
            if (obj instanceof Paintable) {
                ((Paintable) obj).paint(gc);
            }
        }
        
        // 디버그 모드에서는 경계도 그리기 (선택사항)
        if (isDebugMode()) {
            drawBoundaries(gc);
        }
    }
    
    /**
     * 디버그용 경계 그리기
     */
    private void drawBoundaries(GraphicsContext gc) {
        gc.setStroke(Color.RED);
        gc.setLineWidth(2);
        for (Box boundary : boundaries) {
            gc.strokeRect(boundary.getX(), boundary.getY(), 
                         boundary.getWidth(), boundary.getHeight());
        }
    }
    
    // === 유틸리티 메서드들 ===
    
    /**
     * 주어진 위치에 있는 객체를 찾습니다
     * @param x X 좌표
     * @param y Y 좌표
     * @return 해당 위치의 객체 (없으면 null)
     */
    public Object findObjectAt(double x, double y) {
        // 역순으로 검사 (위에 그려진 객체부터)
        for (int i = gameObjects.size() - 1; i >= 0; i--) {
            Object obj = gameObjects.get(i);
            
            if (obj instanceof Ball) {
                Ball ball = (Ball) obj;
                if (ball.contains(x, y)) {
                    return ball;
                }
            } else if (obj instanceof Box) {
                Box box = (Box) obj;
                if (box.contains(x, y)) {
                    return box;
                }
            }
        }
        return null;
    }
    
    /**
     * 특정 타입의 객체들을 반환합니다
     * @param type 찾을 객체의 클래스
     * @param <T> 객체 타입
     * @return 해당 타입의 객체 리스트
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getObjectsOfType(Class<T> type) {
        List<T> result = new ArrayList<>();
        for (Object obj : gameObjects) {
            if (type.isInstance(obj)) {
                result.add((T) obj);
            }
        }
        return result;
    }
    
    // === Getter/Setter 메서드들 ===
    
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public int getObjectCount() { return gameObjects.size(); }
    public List<Object> getObjects() { return new ArrayList<>(gameObjects); }
    public List<Box> getBoundaries() { return new ArrayList<>(boundaries); }
    
    public Color getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(Color color) { this.backgroundColor = color; }
    
    private boolean debugMode = false;
    public boolean isDebugMode() { return debugMode; }
    public void setDebugMode(boolean debug) { this.debugMode = debug; }
}
```

**SimpleWorld의 핵심 기능:**

1. **객체 관리**: 단일 리스트로 모든 타입 관리
2. **충돌 시스템**: 경계 충돌과 객체간 충돌 분리
3. **다형성 활용**: instanceof로 기능별 처리
4. **성능 최적화**: 제거된 객체 지연 삭제
5. **디버깅 지원**: 경계 시각화 기능

---

## 6. 실습 프로젝트들

### 6.1 Triangle 클래스 구현

```java
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 삼각형을 나타내는 클래스
 * 모든 주요 인터페이스를 구현합니다.
 */
public class Triangle implements Paintable, Movable, Collidable {
    private double x, y;        // 중심 좌표
    private double size;        // 삼각형 크기
    private double dx, dy;      // 속도
    private Color color;
    private CollisionAction collisionAction;
    private boolean destroyed;
    
    // 삼각형의 세 꼭짓점 (상대 좌표)
    private static final double[] TRIANGLE_X = {0, -0.5, 0.5};
    private static final double[] TRIANGLE_Y = {-0.6, 0.4, 0.4};
    
    public Triangle(double x, double y, double size, Color color) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.color = color;
        this.dx = 0;
        this.dy = 0;
        this.collisionAction = CollisionAction.BOUNCE;
        this.destroyed = false;
    }
    
    @Override
    public void paint(GraphicsContext gc) {
        gc.setFill(color);
        
        // 삼각형 꼭짓점 계산
        double[] xPoints = new double[3];
        double[] yPoints = new double[3];
        
        for (int i = 0; i < 3; i++) {
            xPoints[i] = x + TRIANGLE_X[i] * size;
            yPoints[i] = y + TRIANGLE_Y[i] * size;
        }
        
        gc.fillPolygon(xPoints, yPoints, 3);
        
        // 테두리
        gc.setStroke(color.darker());
        gc.setLineWidth(1);
        gc.strokePolygon(xPoints, yPoints, 3);
    }
    
    @Override
    public void move(double deltaTime) {
        x += dx * deltaTime;
        y += dy * deltaTime;
    }
    
    @Override
    public Bounds getBounds() {
        // 삼각형을 포함하는 사각형으로 근사
        double halfSize = size * 0.6;
        return new RectangleBounds(x - halfSize, y - halfSize, 
                                 halfSize * 2, halfSize * 2);
    }
    
    @Override
    public boolean isColliding(Boundable other) {
        return getBounds().intersects(other.getBounds());
    }
    
    @Override
    public void handleCollision(Collidable other) {
        switch (collisionAction) {
            case BOUNCE:
                // 간단한 반사
                if (other.getBounds() instanceof RectangleBounds) {
                    RectangleBounds rect = (RectangleBounds) other.getBounds();
                    double centerX = rect.getX() + rect.getWidth() / 2;
                    double centerY = rect.getY() + rect.getHeight() / 2;
                    
                    if (Math.abs(x - centerX) > Math.abs(y - centerY)) {
                        dx = -dx;
                    } else {
                        dy = -dy;
                    }
                }
                break;
            case DESTROY:
                destroyed = true;
                break;
            case STOP:
                dx = 0;
                dy = 0;
                break;
            case PASS:
                break;
            case CUSTOM:
                handleCustomCollision(other);
                break;
        }
    }
    
    protected void handleCustomCollision(Collidable other) {
        // 기본적으로 반사
        dx = -dx * 0.8; // 약간의 에너지 손실
        dy = -dy * 0.8;
    }
    
    // Getter/Setter 메서드들
    public double getX() { return x; }
    public double getY() { return y; }
    public double getDx() { return dx; }
    public double getDy() { return dy; }
    public void setDx(double dx) { this.dx = dx; }
    public void setDy(double dy) { this.dy = dy; }
    public CollisionAction getCollisionAction() { return collisionAction; }
    public void setCollisionAction(CollisionAction action) { this.collisionAction = action; }
    public boolean isDestroyed() { return destroyed; }
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }
}
```

### 6.2 ExplodingBall 클래스

```java
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 충돌 시 폭발하여 작은 공들을 생성하는 특수한 공
 */
public class ExplodingBall extends Ball {
    private boolean hasExploded;
    private int miniballCount;
    private static final Random random = new Random();
    
    public ExplodingBall(double x, double y, double radius, Color color) {
        super(x, y, radius, color);
        this.hasExploded = false;
        this.miniballCount = 3 + random.nextInt(3); // 3-5개
        setCollisionAction(CollisionAction.CUSTOM);
    }
    
    @Override
    protected void handleCustomCollision(Collidable other) {
        if (!hasExploded) {
            explode();
        }
    }
    
    /**
     * 폭발하여 작은 공들을 생성합니다
     * @return 생성된 작은 공들의 리스트
     */
    public List<Ball> explode() {
        hasExploded = true;
        List<Ball> miniBalls = new ArrayList<>();
        
        double miniRadius = getRadius() / 3.0; // 원래 크기의 1/3
        double baseSpeed = 50 + random.nextDouble() * 100; // 50-150 속도
        
        for (int i = 0; i < miniballCount; i++) {
            // 랜덤한 방향
            double angle = (2 * Math.PI * i) / miniballCount + 
                          random.nextDouble() * Math.PI / 4; // 약간의 랜덤 추가
            
            double speed = baseSpeed + random.nextDouble() * 50;
            double dx = speed * Math.cos(angle);
            double dy = speed * Math.sin(angle);
            
            // 작은 공 생성
            Ball miniBall = new Ball(getX(), getY(), miniRadius, 
                                   generateRandomColor());
            miniBall.setDx(dx);
            miniBall.setDy(dy);
            miniBall.setCollisionAction(CollisionAction.BOUNCE);
            
            miniBalls.add(miniBall);
        }
        
        // 원본 공 제거 표시
        return miniBalls;
    }
    
    /**
     * 랜덤한 색상 생성
     */
    private Color generateRandomColor() {
        return Color.color(random.nextDouble(), 
                          random.nextDouble(), 
                          random.nextDouble());
    }
    
    public boolean hasExploded() {
        return hasExploded;
    }
    
    public int getMiniballCount() {
        return miniballCount;
    }
    
    public void setMiniballCount(int count) {
        if (count > 0 && count <= 10) {
            this.miniballCount = count;
        }
    }
}
```

### 6.3 SpecialZone 클래스

```java
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 특별한 효과를 주는 구역
 */
public class SpecialZone extends Box {
    private ZoneType zoneType;
    
    public SpecialZone(double x, double y, double width, double height, ZoneType type) {
        super(x, y, width, height, getZoneColor(type));
        this.zoneType = type;
        setCollisionAction(CollisionAction.PASS); // 통과 가능
    }
    
    /**
     * 구역 타입에 따른 색상 반환
     */
    private static Color getZoneColor(ZoneType type) {
        switch (type) {
            case SPEED_UP: return Color.RED.deriveColor(0, 1, 1, 0.3);
            case SLOW_DOWN: return Color.BLUE.deriveColor(0, 1, 1, 0.3);
            case GRAVITY: return Color.PURPLE.deriveColor(0, 1, 1, 0.3);
            case ANTI_GRAVITY: return Color.YELLOW.deriveColor(0, 1, 1, 0.3);
            case TELEPORT: return Color.GREEN.deriveColor(0, 1, 1, 0.3);
            default: return Color.GRAY.deriveColor(0, 1, 1, 0.3);
        }
    }
    
    /**
     * 움직이는 객체에 효과를 적용합니다
     */
    public void applyEffect(Movable movable) {
        switch (zoneType) {
            case SPEED_UP:
                movable.setDx(movable.getDx() * 2.0);
                movable.setDy(movable.getDy() * 2.0);
                break;
            case SLOW_DOWN:
                movable.setDx(movable.getDx() * 0.5);
                movable.setDy(movable.getDy() * 0.5);
                break;
            case GRAVITY:
                movable.setDy(movable.getDy() + 10);
                break;
            case ANTI_GRAVITY:
                movable.setDy(movable.getDy() - 10);
                break;
            case TELEPORT:
                // 간단한 텔레포트: 반대편으로 이동
                if (movable instanceof Ball) {
                    Ball ball = (Ball) movable;
                    ball.setX(getX() + getWidth() - (ball.getX() - getX()));
                }
                break;
        }
    }
    
    @Override
    public void paint(GraphicsContext gc) {
        super.paint(gc);
        
        // 효과 아이콘 그리기
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font(16));
        
        String symbol = getZoneSymbol();
        double textX = getX() + getWidth() / 2 - 5;
        double textY = getY() + getHeight() / 2 + 5;
        gc.fillText(symbol, textX, textY);
    }
    
    /**
     * 구역 타입에 따른 심볼 반환
     */
    private String getZoneSymbol() {
        switch (zoneType) {
            case SPEED_UP: return "↑↑";
            case SLOW_DOWN: return "↓↓";
            case GRAVITY: return "⇓";
            case ANTI_GRAVITY: return "⇑";
            case TELEPORT: return "⟲";
            default: return "?";
        }
    }
    
    public ZoneType getZoneType() {
        return zoneType;
    }
}

/**
 * 특수 구역 타입을 정의하는 열거형
 */
enum ZoneType {
    SPEED_UP,      // 속도 증가
    SLOW_DOWN,     // 속도 감소
    GRAVITY,       // 중력 (아래로 끌어당김)
    ANTI_GRAVITY,  // 반중력 (위로 밀어냄)
    TELEPORT       // 순간이동
}
```

---

## 7. 테스트 코드와 검증

### 7.1 JUnit 테스트 구조

```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * SimpleWorld 종합 테스트
 */
public class SimpleWorldTest {
    
    private SimpleWorld world;
    
    @BeforeEach
    void setUp() {
        world = new SimpleWorld(800, 600);
    }
    
    @Test
    @DisplayName("인터페이스 구현 확인")
    void testInterfaceImplementation() {
        Ball ball = new Ball(100, 100, 20);
        
        // 모든 인터페이스 구현 확인
        assertTrue(ball instanceof Paintable);
        assertTrue(ball instanceof Movable);
        assertTrue(ball instanceof Collidable);
        assertTrue(ball instanceof Boundable);
    }
    
    @Test
    @DisplayName("다형성을 통한 통합 처리")
    void testPolymorphicHandling() {
        // 다양한 타입의 객체들
        Ball ball = new Ball(100, 100, 20);
        Box box = new Box(200, 200, 50, 40);
        Triangle triangle = new Triangle(300, 300, 30, Color.GREEN);
        
        world.addObject(ball);
        world.addObject(box);
        world.addObject(triangle);
        
        assertEquals(3, world.getObjectCount());
        
        // 모든 Movable 객체에 속도 설정
        for (Object obj : world.getObjects()) {
            if (obj instanceof Movable) {
                Movable movable = (Movable) obj;
                movable.setDx(50);
                movable.setDy(30);
            }
        }
        
        // 업데이트 후 이동 확인
        world.update(1.0);
        
        assertEquals(150, ball.getX(), 0.001);
        assertEquals(130, ball.getY(), 0.001);
    }
    
    @Test
    @DisplayName("충돌 시스템 테스트")
    void testCollisionSystem() {
        Ball ball1 = new Ball(100, 100, 20);
        Ball ball2 = new Ball(130, 100, 15);
        
        ball1.setDx(50);
        ball1.setCollisionAction(CollisionAction.BOUNCE);
        ball2.setCollisionAction(CollisionAction.BOUNCE);
        
        world.addObject(ball1);
        world.addObject(ball2);
        
        // 충돌 확인
        assertTrue(ball1.isColliding(ball2));
        
        double oldDx = ball1.getDx();
        world.update(0.1);
        
        // 충돌 후 속도 변화 확인
        assertNotEquals(oldDx, ball1.getDx());
    }
    
    @Test
    @DisplayName("ExplodingBall 폭발 테스트")
    void testExplodingBall() {
        ExplodingBall explodingBall = new ExplodingBall(100, 100, 25, Color.ORANGE);
        Box trigger = new Box(120, 120, 20, 20);
        
        assertFalse(explodingBall.hasExploded());
        
        List<Ball> miniBalls = explodingBall.explode();
        
        assertTrue(explodingBall.hasExploded());
        assertFalse(miniBalls.isEmpty());
        assertEquals(explodingBall.getMiniballCount(), miniBalls.size());
        
        // 모든 작은 공이 움직이는지 확인
        for (Ball miniBall : miniBalls) {
            assertTrue(Math.abs(miniBall.getDx()) > 0 || Math.abs(miniBall.getDy()) > 0);
        }
    }
}
```

### 7.2 통합 테스트 시나리오

```java
@Test
@DisplayName("완전한 게임 시나리오 테스트")
void testCompleteGameScenario() {
    // 1. 초기 설정
    Ball player = new Ball(50, 300, 15, Color.BLUE);
    player.setDx(100);
    player.setDy(50);
    
    ExplodingBall explosive = new ExplodingBall(400, 300, 20, Color.RED);
    
    Box obstacle = new Box(200, 280, 40, 40, Color.GRAY);
    obstacle.setCollisionAction(CollisionAction.BOUNCE);
    
    SpecialZone speedZone = new SpecialZone(300, 250, 100, 100, ZoneType.SPEED_UP);
    
    world.addObject(player);
    world.addObject(explosive);
    world.addObject(obstacle);
    world.addObject(speedZone);
    
    // 2. 시뮬레이션 실행
    for (int frame = 0; frame < 100; frame++) {
        world.update(0.016); // 60 FPS
        
        // 특수 구역 효과 적용
        for (Object obj : world.getObjects()) {
            if (obj instanceof Movable && speedZone.isColliding((Boundable) obj)) {
                speedZone.applyEffect((Movable) obj);
            }
        }
        
        // 폭발 처리
        List<ExplodingBall> explodingBalls = world.getObjectsOfType(ExplodingBall.class);
        for (ExplodingBall eb : explodingBalls) {
            if (eb.hasExploded()) {
                List<Ball> miniBalls = eb.explode();
                for (Ball mb : miniBalls) {
                    world.addObject(mb);
                }
                world.removeObject(eb);
            }
        }
    }
    
    // 3. 결과 검증
    assertTrue(world.getObjectCount() > 4); // 폭발로 인한 객체 증가
    
    List<Ball> balls = world.getObjectsOfType(Ball.class);
    assertFalse(balls.isEmpty());
    
    // 모든 공이 여전히 화면 안에 있는지 확인 (경계 충돌 작동)
    for (Ball ball : balls) {
        assertTrue(ball.getX() >= 0 && ball.getX() <= world.getWidth());
        assertTrue(ball.getY() >= 0 && ball.getY() <= world.getHeight());
    }
}
```

---

## 구현 체크리스트

### 필수 구현 항목
- [ ] CollisionAction 열거형
- [ ] Paintable 인터페이스
- [ ] Movable 인터페이스  
- [ ] Boundable 인터페이스
- [ ] Collidable 인터페이스
- [ ] Bounds 추상 클래스
- [ ] CircleBounds 클래스
- [ ] RectangleBounds 클래스
- [ ] Ball 클래스 (모든 인터페이스 구현)
- [ ] Box 클래스
- [ ] MovableBox 클래스
- [ ] SimpleWorld 클래스

### 확장 구현 항목
- [ ] Triangle 클래스
- [ ] ExplodingBall 클래스
- [ ] SpecialZone 클래스
- [ ] ZoneType 열거형

### 테스트 항목
- [ ] 인터페이스 구현 테스트
- [ ] 다형성 활용 테스트
- [ ] 충돌 시스템 테스트
- [ ] 특수 기능 테스트
- [ ] 통합 시나리오 테스트

---

## 학습 포인트 정리

### 6장 문제점들이 어떻게 해결되었는가?

1. **클래스 폭발 문제** → **인터페이스로 해결**
   - 기능 조합마다 새 클래스 필요 → 하나의 클래스가 여러 인터페이스 구현

2. **코드 중복 문제** → **인터페이스 계약으로 해결**
   - 비슷한 메서드들이 여러 클래스에 중복 → 인터페이스가 일관된 계약 제공

3. **유지보수 어려움** → **다형성으로 해결**
   - 새 타입 추가 시 기존 코드 수정 → instanceof로 기능별 처리

4. **확장성 부족** → **런타임 행동 변경으로 해결**
   - 컴파일 타임에 행동 고정 → CollisionAction으로 런타임 변경

### 인터페이스의 핵심 가치

1. **계약(Contract)**: 클래스가 구현해야 할 메서드 정의
2. **다형성(Polymorphism)**: 같은 인터페이스로 다른 구현체 처리
3. **유연성(Flexibility)**: 런타임에 행동 변경 가능
4. **확장성(Extensibility)**: 새로운 구현체 추가 용이
5. **분리(Separation)**: 인터페이스와 구현의 분리

이제 7장의 모든 핵심 개념과 구현이 완료되었습니다. 학습자들은 이 가이드를 통해 인터페이스의 강력함을 체험하고, 6장의 상속 문제들이 어떻게 우아하게 해결되는지 이해할 수 있습니다.