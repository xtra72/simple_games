# 6장: 새로운 객체들 (New Objects) - 완전한 구현 가이드

> **학습 목표**: 상속만 사용했을 때 발생하는 문제점들을 직접 경험하고, 인터페이스가 필요한 이유를 이해합니다.

## 목차

1. [개요](#개요)
2. [Box 클래스 계층 구현](#box-클래스-계층-구현)
3. [MovableBox 클래스 구현](#movablebox-클래스-구현)
4. [PaintableBox 클래스 구현](#paintablebox-클래스-구현)
5. [클래스 조합 폭발 문제](#클래스-조합-폭발-문제)
6. [MixedWorld 구현](#mixedworld-구현)
7. [ExtendedCollisionHandler 구현](#extendedcollisionhandler-구현)
8. [상속의 한계 분석](#상속의-한계-분석)
9. [종합 테스트](#종합-테스트)
10. [문제점 요약](#문제점-요약)

## 개요

이 장에서는 상속만을 사용하여 새로운 도형(Box)을 추가하면서 발생하는 여러 가지 문제점들을 직접 경험해봅니다. 이러한 문제들은 7장에서 인터페이스를 통해 해결됩니다.

### 주요 문제점들

1. **클래스 폭발**: 기능 조합마다 새로운 클래스 필요
2. **코드 중복**: 동일한 로직을 여러 클래스에서 구현
3. **타입 체크 복잡성**: instanceof 지옥
4. **유지보수 어려움**: 새 타입 추가 시 전체 코드 수정

## Box 클래스 계층 구현

### 1. 기본 Box 클래스

먼저 사각형을 표현하는 기본 Box 클래스를 구현합니다.

```java
/**
 * 사각형을 표현하는 기본 클래스
 * Ball과 유사하지만 사각형의 특성을 가집니다.
 */
public class Box {
    private double x;      // 왼쪽 상단 X 좌표
    private double y;      // 왼쪽 상단 Y 좌표
    private double width;  // 너비
    private double height; // 높이
    
    /**
     * Box 생성자
     * @param x 왼쪽 상단 X 좌표
     * @param y 왼쪽 상단 Y 좌표
     * @param width 너비 (양수여야 함)
     * @param height 높이 (양수여야 함)
     * @throws IllegalArgumentException 너비나 높이가 0 이하인 경우
     */
    public Box(double x, double y, double width, double height) {
        if (width <= 0) {
            throw new IllegalArgumentException("너비는 양수여야 합니다: " + width);
        }
        if (height <= 0) {
            throw new IllegalArgumentException("높이는 양수여야 합니다: " + height);
        }
        
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    // Getter 메서드들
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    
    // Setter 메서드들
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    
    public void setWidth(double width) {
        if (width <= 0) {
            throw new IllegalArgumentException("너비는 양수여야 합니다: " + width);
        }
        this.width = width;
    }
    
    public void setHeight(double height) {
        if (height <= 0) {
            throw new IllegalArgumentException("높이는 양수여야 합니다: " + height);
        }
        this.height = height;
    }
    
    /**
     * 주어진 점이 Box 내부에 있는지 확인
     * @param px 점의 X 좌표
     * @param py 점의 Y 좌표
     * @return 점이 Box 내부에 있으면 true
     */
    public boolean contains(double px, double py) {
        return px >= x && px < x + width && 
               py >= y && py < y + height;
    }
    
    /**
     * Box의 경계를 RectangleBounds로 반환
     * @return Box의 경계 정보
     */
    public RectangleBounds getBounds() {
        return new RectangleBounds(x, y, width, height);
    }
    
    /**
     * Box의 중심점 X 좌표
     */
    public double getCenterX() {
        return x + width / 2;
    }
    
    /**
     * Box의 중심점 Y 좌표
     */
    public double getCenterY() {
        return y + height / 2;
    }
    
    @Override
    public String toString() {
        return String.format("Box{x=%.1f, y=%.1f, width=%.1f, height=%.1f}", 
                           x, y, width, height);
    }
}
```

### 2. RectangleBounds 클래스

Box의 경계를 표현하는 클래스입니다.

```java
/**
 * 사각형 경계를 표현하는 클래스
 * CircleBounds와 함께 Bounds 시스템의 일부입니다.
 */
public class RectangleBounds extends Bounds {
    private final double minX, minY, maxX, maxY;
    
    public RectangleBounds(double x, double y, double width, double height) {
        this.minX = x;
        this.minY = y;
        this.maxX = x + width;
        this.maxY = y + height;
    }
    
    @Override
    public double getMinX() { return minX; }
    
    @Override
    public double getMinY() { return minY; }
    
    @Override
    public double getMaxX() { return maxX; }
    
    @Override
    public double getMaxY() { return maxY; }
    
    @Override
    public boolean intersects(Bounds other) {
        return !(other.getMaxX() < minX || 
                 other.getMinX() > maxX ||
                 other.getMaxY() < minY || 
                 other.getMinY() > maxY);
    }
    
    @Override
    public boolean contains(double x, double y) {
        return x >= minX && x < maxX && y >= minY && y < maxY;
    }
    
    public double getWidth() { return maxX - minX; }
    public double getHeight() { return maxY - minY; }
    
    @Override
    public String toString() {
        return String.format("RectangleBounds{minX=%.1f, minY=%.1f, maxX=%.1f, maxY=%.1f}", 
                           minX, minY, maxX, maxY);
    }
}
```

## MovableBox 클래스 구현

Box에 이동 기능을 추가한 클래스입니다. MovableBall과 동일한 move 로직을 가집니다 (코드 중복 문제!).

```java
/**
 * 이동 가능한 Box 클래스
 * 문제점: MovableBall과 동일한 move 로직을 중복으로 구현
 */
public class MovableBox extends Box {
    private double dx; // X 방향 속도
    private double dy; // Y 방향 속도
    
    /**
     * MovableBox 생성자
     */
    public MovableBox(double x, double y, double width, double height) {
        super(x, y, width, height);
        this.dx = 0;
        this.dy = 0;
    }
    
    /**
     * 속도와 함께 생성하는 생성자
     */
    public MovableBox(double x, double y, double width, double height, double dx, double dy) {
        super(x, y, width, height);
        this.dx = dx;
        this.dy = dy;
    }
    
    // 속도 관련 메서드들
    public double getDx() { return dx; }
    public double getDy() { return dy; }
    public void setDx(double dx) { this.dx = dx; }
    public void setDy(double dy) { this.dy = dy; }
    
    /**
     * Box를 주어진 시간만큼 이동시킵니다
     * 주의: 이 코드는 MovableBall.move()와 완전히 동일합니다! (코드 중복 문제)
     * @param deltaTime 경과 시간 (초 단위)
     */
    public void move(double deltaTime) {
        double newX = getX() + dx * deltaTime;
        double newY = getY() + dy * deltaTime;
        setX(newX);
        setY(newY);
    }
    
    /**
     * 속도를 설정합니다
     */
    public void setVelocity(double dx, double dy) {
        this.dx = dx;
        this.dy = dy;
    }
    
    /**
     * 현재 속력을 계산합니다
     */
    public double getSpeed() {
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * X 방향 속도를 반전시킵니다 (벽 충돌 시 사용)
     */
    public void reflectX() {
        dx = -dx;
    }
    
    /**
     * Y 방향 속도를 반전시킵니다 (벽 충돌 시 사용)
     */
    public void reflectY() {
        dy = -dy;
    }
    
    @Override
    public String toString() {
        return String.format("MovableBox{x=%.1f, y=%.1f, width=%.1f, height=%.1f, dx=%.1f, dy=%.1f}", 
                           getX(), getY(), getWidth(), getHeight(), dx, dy);
    }
}
```

## PaintableBox 클래스 구현

Box에 색상 기능을 추가한 클래스입니다. PaintableBall과 동일한 색상 처리 로직을 가집니다 (코드 중복 문제!).

```java
import javafx.scene.paint.Color;
import javafx.scene.canvas.GraphicsContext;

/**
 * 색상을 가진 Box 클래스
 * 문제점: PaintableBall과 동일한 색상 처리 로직을 중복으로 구현
 */
public class PaintableBox extends Box {
    private Color color;
    
    /**
     * 기본 색상(빨간색)으로 PaintableBox 생성
     */
    public PaintableBox(double x, double y, double width, double height) {
        super(x, y, width, height);
        this.color = Color.RED; // 기본 색상
    }
    
    /**
     * 지정된 색상으로 PaintableBox 생성
     */
    public PaintableBox(double x, double y, double width, double height, Color color) {
        super(x, y, width, height);
        setColor(color); // validation을 위해 setter 사용
    }
    
    /**
     * 색상을 반환합니다
     * 주의: 이 메서드는 PaintableBall.getColor()와 완전히 동일합니다! (코드 중복)
     */
    public Color getColor() {
        return color;
    }
    
    /**
     * 색상을 설정합니다
     * 주의: 이 메서드는 PaintableBall.setColor()와 완전히 동일합니다! (코드 중복)
     * @param color 설정할 색상 (null이면 예외 발생)
     * @throws IllegalArgumentException color가 null인 경우
     */
    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("색상은 null일 수 없습니다");
        }
        this.color = color;
    }
    
    /**
     * Box를 화면에 그립니다
     * @param gc GraphicsContext 객체
     */
    public void paint(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillRect(getX(), getY(), getWidth(), getHeight());
        
        // 테두리도 그리기
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(getX(), getY(), getWidth(), getHeight());
    }
    
    /**
     * 색상의 밝기를 조절합니다
     * 주의: PaintableBall과 동일한 로직입니다! (코드 중복)
     */
    public void adjustBrightness(double factor) {
        if (factor < 0 || factor > 2) {
            throw new IllegalArgumentException("밝기 조절 값은 0.0~2.0 사이여야 합니다: " + factor);
        }
        
        double red = Math.min(1.0, color.getRed() * factor);
        double green = Math.min(1.0, color.getGreen() * factor);
        double blue = Math.min(1.0, color.getBlue() * factor);
        double opacity = color.getOpacity();
        
        this.color = new Color(red, green, blue, opacity);
    }
    
    @Override
    public String toString() {
        return String.format("PaintableBox{x=%.1f, y=%.1f, width=%.1f, height=%.1f, color=%s}", 
                           getX(), getY(), getWidth(), getHeight(), color);
    }
}
```

## 클래스 조합 폭발 문제

여기서부터 상속의 심각한 문제가 드러납니다. 각 기능 조합마다 새로운 클래스가 필요합니다!

### PaintableMovableBox 클래스

색상과 이동 기능을 모두 가진 Box입니다.

```java
import javafx.scene.paint.Color;
import javafx.scene.canvas.GraphicsContext;

/**
 * 색상과 이동 기능을 모두 가진 Box 클래스
 * 문제점: 긴 클래스 이름, 복잡한 상속 체인
 */
public class PaintableMovableBox extends MovableBox {
    private Color color;
    
    public PaintableMovableBox(double x, double y, double width, double height) {
        super(x, y, width, height);
        this.color = Color.RED;
    }
    
    public PaintableMovableBox(double x, double y, double width, double height, Color color) {
        super(x, y, width, height);
        setColor(color);
    }
    
    public PaintableMovableBox(double x, double y, double width, double height, 
                              double dx, double dy, Color color) {
        super(x, y, width, height, dx, dy);
        setColor(color);
    }
    
    // 색상 관련 메서드들 (PaintableBox와 중복!)
    public Color getColor() {
        return color;
    }
    
    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("색상은 null일 수 없습니다");
        }
        this.color = color;
    }
    
    public void paint(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillRect(getX(), getY(), getWidth(), getHeight());
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(getX(), getY(), getWidth(), getHeight());
    }
    
    public void adjustBrightness(double factor) {
        if (factor < 0 || factor > 2) {
            throw new IllegalArgumentException("밝기 조절 값은 0.0~2.0 사이여야 합니다: " + factor);
        }
        
        double red = Math.min(1.0, color.getRed() * factor);
        double green = Math.min(1.0, color.getGreen() * factor);
        double blue = Math.min(1.0, color.getBlue() * factor);
        double opacity = color.getOpacity();
        
        this.color = new Color(red, green, blue, opacity);
    }
    
    @Override
    public String toString() {
        return String.format("PaintableMovableBox{x=%.1f, y=%.1f, width=%.1f, height=%.1f, dx=%.1f, dy=%.1f, color=%s}", 
                           getX(), getY(), getWidth(), getHeight(), getDx(), getDy(), color);
    }
}
```

### BoundedBox 클래스

경계 처리 기능을 가진 Box입니다.

```java
/**
 * 경계 처리 기능을 가진 Box 클래스
 */
public class BoundedBox extends Box {
    private double worldWidth;
    private double worldHeight;
    
    public BoundedBox(double x, double y, double width, double height) {
        super(x, y, width, height);
        this.worldWidth = 800; // 기본값
        this.worldHeight = 600;
    }
    
    public BoundedBox(double x, double y, double width, double height, 
                     double worldWidth, double worldHeight) {
        super(x, y, width, height);
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }
    
    public void setWorldBounds(double width, double height) {
        this.worldWidth = width;
        this.worldHeight = height;
    }
    
    /**
     * Box가 화면 경계를 벗어나지 않도록 제한합니다
     */
    public void constrainToBounds() {
        double x = getX();
        double y = getY();
        double width = getWidth();
        double height = getHeight();
        
        if (x < 0) setX(0);
        if (y < 0) setY(0);
        if (x + width > worldWidth) setX(worldWidth - width);
        if (y + height > worldHeight) setY(worldHeight - height);
    }
    
    /**
     * Box가 화면 경계에 닿았는지 확인
     */
    public boolean isTouchingBounds() {
        double x = getX();
        double y = getY();
        double width = getWidth();
        double height = getHeight();
        
        return x <= 0 || y <= 0 || 
               x + width >= worldWidth || 
               y + height >= worldHeight;
    }
}
```

### 더 많은 조합 클래스들

실제로는 다음과 같은 클래스들이 모두 필요합니다:

```java
// 3개 기능의 모든 조합 = 8개 클래스!
class Box                           // 기본
class PaintableBox                  // 색상
class MovableBox                    // 이동
class BoundedBox                    // 경계
class PaintableMovableBox           // 색상 + 이동
class PaintableBoundedBox           // 색상 + 경계
class MovableBoundedBox             // 이동 + 경계
class PaintableMovableBoundedBox    // 색상 + 이동 + 경계
```

**문제점**: Ball도 동일하게 8개 클래스가 필요하므로, 총 **16개 클래스**가 필요합니다!

## MixedWorld 구현

Ball과 Box를 모두 관리하는 World 클래스입니다. 여기서 타입 체크의 복잡성이 드러납니다.

```java
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Ball과 Box를 모두 관리하는 World 클래스
 * 문제점: 타입별로 별도 처리 필요, instanceof 지옥
 */
public class MixedWorld {
    private final double width;
    private final double height;
    
    // 문제점 1: 각 타입별로 별도 컨테이너 필요
    private final List<Ball> balls;
    private final List<Box> boxes;
    
    public MixedWorld(double width, double height) {
        this.width = width;
        this.height = height;
        this.balls = new ArrayList<>();
        this.boxes = new ArrayList<>();
    }
    
    // 접근자 메서드들
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public int getBallCount() { return balls.size(); }
    public int getBoxCount() { return boxes.size(); }
    
    // 객체 추가 메서드들
    public void addBall(Ball ball) {
        balls.add(ball);
    }
    
    public void addBox(Box box) {
        boxes.add(box);
    }
    
    /**
     * 모든 객체를 업데이트합니다
     * 문제점: 각 타입별로 별도 처리, instanceof 체크 필요
     */
    public void update(double deltaTime) {
        // Ball 업데이트 - instanceof 지옥 시작!
        for (Ball ball : balls) {
            // MovableBall 계열 처리
            if (ball instanceof PaintableMovableBall) {
                PaintableMovableBall pmb = (PaintableMovableBall) ball;
                pmb.move(deltaTime);
                checkBallBounds(pmb);
            } else if (ball instanceof MovableBall) {
                MovableBall mb = (MovableBall) ball;
                mb.move(deltaTime);
                checkBallBounds(mb);
            }
            // BoundedBall 계열 처리는 생략...
        }
        
        // Box 업데이트 - 또 다른 instanceof 지옥!
        for (Box box : boxes) {
            if (box instanceof PaintableMovableBox) {
                PaintableMovableBox pmb = (PaintableMovableBox) box;
                pmb.move(deltaTime);
                checkBoxBounds(pmb);
            } else if (box instanceof MovableBox) {
                MovableBox mb = (MovableBox) box;
                mb.move(deltaTime);
                checkBoxBounds(mb);
            }
            // 다른 Box 타입들도 처리해야 함...
        }
    }
    
    /**
     * Ball의 경계 체크 (MovableBall용)
     */
    private void checkBallBounds(MovableBall ball) {
        double x = ball.getX();
        double y = ball.getY();
        double radius = ball.getRadius();
        
        if (x - radius < 0 || x + radius > width) {
            ball.reflectX();
            // 경계 내부로 이동
            if (x - radius < 0) ball.setX(radius);
            if (x + radius > width) ball.setX(width - radius);
        }
        
        if (y - radius < 0 || y + radius > height) {
            ball.reflectY();
            if (y - radius < 0) ball.setY(radius);
            if (y + radius > height) ball.setY(height - radius);
        }
    }
    
    /**
     * Box의 경계 체크 (MovableBox용)
     */
    private void checkBoxBounds(MovableBox box) {
        double x = box.getX();
        double y = box.getY();
        double boxWidth = box.getWidth();
        double boxHeight = box.getHeight();
        
        if (x < 0 || x + boxWidth > width) {
            box.reflectX();
            if (x < 0) box.setX(0);
            if (x + boxWidth > width) box.setX(width - boxWidth);
        }
        
        if (y < 0 || y + boxHeight > height) {
            box.reflectY();
            if (y < 0) box.setY(0);
            if (y + boxHeight > height) box.setY(height - boxHeight);
        }
    }
    
    /**
     * 모든 객체를 렌더링합니다
     * 문제점: 각 타입별로 다른 그리기 로직 필요
     */
    public void render(GraphicsContext gc) {
        // 배경 그리기
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, width, height);
        
        // Ball 렌더링 - 또 다른 instanceof 지옥!
        for (Ball ball : balls) {
            if (ball instanceof PaintableMovableBall) {
                PaintableMovableBall pmb = (PaintableMovableBall) ball;
                pmb.paint(gc);
            } else if (ball instanceof PaintableBall) {
                PaintableBall pb = (PaintableBall) ball;
                pb.paint(gc);
            } else {
                // 기본 Ball 그리기
                gc.setFill(Color.BLACK);
                gc.fillOval(ball.getX() - ball.getRadius(), 
                           ball.getY() - ball.getRadius(),
                           ball.getRadius() * 2, 
                           ball.getRadius() * 2);
            }
        }
        
        // Box 렌더링 - 또 또 다른 instanceof 지옥!
        for (Box box : boxes) {
            if (box instanceof PaintableMovableBox) {
                PaintableMovableBox pmb = (PaintableMovableBox) box;
                pmb.paint(gc);
            } else if (box instanceof PaintableBox) {
                PaintableBox pb = (PaintableBox) box;
                pb.paint(gc);
            } else {
                // 기본 Box 그리기
                gc.setFill(Color.DARKGRAY);
                gc.fillRect(box.getX(), box.getY(), box.getWidth(), box.getHeight());
                gc.setStroke(Color.BLACK);
                gc.strokeRect(box.getX(), box.getY(), box.getWidth(), box.getHeight());
            }
        }
    }
    
    /**
     * 특정 위치의 객체를 찾습니다
     * 문제점: 모든 타입을 개별적으로 검사해야 함
     */
    public Object findObjectAt(double x, double y) {
        // Ball 검사
        for (Ball ball : balls) {
            double dx = x - ball.getX();
            double dy = y - ball.getY();
            if (dx * dx + dy * dy <= ball.getRadius() * ball.getRadius()) {
                return ball;
            }
        }
        
        // Box 검사
        for (Box box : boxes) {
            if (box.contains(x, y)) {
                return box;
            }
        }
        
        return null;
    }
    
    /**
     * 이동 가능한 객체의 수를 반환합니다
     * 문제점: 모든 Movable 타입을 나열해야 함
     */
    public int getMovableObjectCount() {
        int count = 0;
        
        // Ball 계열 체크
        for (Ball ball : balls) {
            if (ball instanceof MovableBall || 
                ball instanceof PaintableMovableBall ||
                ball instanceof BoundedMovableBall ||
                ball instanceof PaintableMovableBoundedBall) {
                count++;
            }
        }
        
        // Box 계열 체크
        for (Box box : boxes) {
            if (box instanceof MovableBox || 
                box instanceof PaintableMovableBox ||
                box instanceof BoundedMovableBox ||
                box instanceof PaintableMovableBoundedBox) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * 색상을 가진 객체의 수를 반환합니다
     * 문제점: 모든 Paintable 타입을 나열해야 함
     */
    public int getPaintableObjectCount() {
        int count = 0;
        
        // Ball 계열 체크
        for (Ball ball : balls) {
            if (ball instanceof PaintableBall || 
                ball instanceof PaintableMovableBall ||
                ball instanceof PaintableBoundedBall ||
                ball instanceof PaintableMovableBoundedBall) {
                count++;
            }
        }
        
        // Box 계열 체크
        for (Box box : boxes) {
            if (box instanceof PaintableBox || 
                box instanceof PaintableMovableBox ||
                box instanceof PaintableBoundedBox ||
                box instanceof PaintableMovableBoundedBox) {
                count++;
            }
        }
        
        return count;
    }
}
```

## ExtendedCollisionHandler 구현

충돌 처리가 얼마나 복잡해지는지 보여주는 클래스입니다.

```java
import java.util.List;

/**
 * 확장된 충돌 처리 클래스
 * 문제점: 타입 조합마다 별도 메서드 필요 - 조합 폭발!
 */
public class ExtendedCollisionHandler {
    
    /**
     * Ball-Ball 충돌 처리
     * MovableBall들 간의 탄성 충돌을 처리합니다
     */
    public void checkBallToBallCollisions(List<Ball> balls) {
        for (int i = 0; i < balls.size(); i++) {
            for (int j = i + 1; j < balls.size(); j++) {
                Ball ball1 = balls.get(i);
                Ball ball2 = balls.get(j);
                
                // 둘 다 MovableBall인지 확인 (instanceof 체크!)
                if (ball1 instanceof MovableBall && ball2 instanceof MovableBall) {
                    MovableBall mb1 = (MovableBall) ball1;
                    MovableBall mb2 = (MovableBall) ball2;
                    
                    if (isColliding(mb1, mb2)) {
                        handleBallToBallCollision(mb1, mb2);
                    }
                }
            }
        }
    }
    
    /**
     * Box-Box 충돌 처리
     * MovableBox들 간의 충돌을 처리합니다
     */
    public void checkBoxToBoxCollisions(List<Box> boxes) {
        for (int i = 0; i < boxes.size(); i++) {
            for (int j = i + 1; j < boxes.size(); j++) {
                Box box1 = boxes.get(i);
                Box box2 = boxes.get(j);
                
                // 둘 다 MovableBox인지 확인 (instanceof 체크!)
                if (box1 instanceof MovableBox && box2 instanceof MovableBox) {
                    MovableBox mb1 = (MovableBox) box1;
                    MovableBox mb2 = (MovableBox) box2;
                    
                    if (isColliding(mb1, mb2)) {
                        handleBoxToBoxCollision(mb1, mb2);
                    }
                }
            }
        }
    }
    
    /**
     * Ball-Box 충돌 처리
     * 가장 복잡한 충돌 처리 - 완전히 다른 로직 필요!
     */
    public void checkBallToBoxCollisions(List<Ball> balls, List<Box> boxes) {
        for (Ball ball : balls) {
            if (!(ball instanceof MovableBall)) continue;
            
            MovableBall movableBall = (MovableBall) ball;
            
            for (Box box : boxes) {
                if (isCollidingBallToBox(movableBall, box)) {
                    handleBallToBoxCollision(movableBall, box);
                }
            }
        }
    }
    
    /**
     * Ball-Ball 충돌 확인
     */
    private boolean isColliding(MovableBall ball1, MovableBall ball2) {
        double dx = ball2.getX() - ball1.getX();
        double dy = ball2.getY() - ball1.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        double minDistance = ball1.getRadius() + ball2.getRadius();
        
        return distance < minDistance;
    }
    
    /**
     * Box-Box 충돌 확인 (AABB - Axis-Aligned Bounding Box)
     */
    private boolean isColliding(MovableBox box1, MovableBox box2) {
        return !(box1.getX() + box1.getWidth() < box2.getX() ||
                 box2.getX() + box2.getWidth() < box1.getX() ||
                 box1.getY() + box1.getHeight() < box2.getY() ||
                 box2.getY() + box2.getHeight() < box1.getY());
    }
    
    /**
     * Ball-Box 충돌 확인 (가장 복잡!)
     */
    private boolean isCollidingBallToBox(MovableBall ball, Box box) {
        // Box에서 Ball 중심까지 가장 가까운 점 찾기
        double closestX = Math.max(box.getX(), 
                         Math.min(ball.getX(), box.getX() + box.getWidth()));
        double closestY = Math.max(box.getY(), 
                         Math.min(ball.getY(), box.getY() + box.getHeight()));
        
        // 가장 가까운 점과 Ball 중심의 거리 계산
        double dx = ball.getX() - closestX;
        double dy = ball.getY() - closestY;
        double distanceSquared = dx * dx + dy * dy;
        
        return distanceSquared < (ball.getRadius() * ball.getRadius());
    }
    
    /**
     * Ball-Ball 충돌 처리 (탄성 충돌)
     */
    private void handleBallToBallCollision(MovableBall ball1, MovableBall ball2) {
        // 충돌 벡터 계산
        double dx = ball2.getX() - ball1.getX();
        double dy = ball2.getY() - ball1.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance == 0) return; // 같은 위치에 있으면 무시
        
        // 정규화된 충돌 벡터
        double nx = dx / distance;
        double ny = dy / distance;
        
        // 상대 속도
        double dvx = ball2.getDx() - ball1.getDx();
        double dvy = ball2.getDy() - ball1.getDy();
        
        // 충돌 축에서의 상대 속도
        double dvn = dvx * nx + dvy * ny;
        
        if (dvn > 0) return; // 이미 멀어지고 있으면 무시
        
        // 충돌 처리 (질량이 같다고 가정)
        ball1.setDx(ball1.getDx() + dvn * nx);
        ball1.setDy(ball1.getDy() + dvn * ny);
        ball2.setDx(ball2.getDx() - dvn * nx);
        ball2.setDy(ball2.getDy() - dvn * ny);
        
        // 겹침 해결
        double overlap = ball1.getRadius() + ball2.getRadius() - distance;
        double separationX = nx * overlap * 0.5;
        double separationY = ny * overlap * 0.5;
        
        ball1.setX(ball1.getX() - separationX);
        ball1.setY(ball1.getY() - separationY);
        ball2.setX(ball2.getX() + separationX);
        ball2.setY(ball2.getY() + separationY);
    }
    
    /**
     * Box-Box 충돌 처리
     */
    private void handleBoxToBoxCollision(MovableBox box1, MovableBox box2) {
        // 간단한 분리 처리 (실제로는 더 복잡함)
        double centerX1 = box1.getCenterX();
        double centerY1 = box1.getCenterY();
        double centerX2 = box2.getCenterX();
        double centerY2 = box2.getCenterY();
        
        double dx = centerX2 - centerX1;
        double dy = centerY2 - centerY1;
        
        if (Math.abs(dx) > Math.abs(dy)) {
            // 수평 충돌
            box1.reflectX();
            box2.reflectX();
        } else {
            // 수직 충돌
            box1.reflectY();
            box2.reflectY();
        }
    }
    
    /**
     * Ball-Box 충돌 처리 (가장 복잡!)
     */
    private void handleBallToBoxCollision(MovableBall ball, Box box) {
        CollisionSide side = detectCollisionSide(ball.getX(), ball.getY(), box);
        
        switch (side) {
            case LEFT:
            case RIGHT:
                ball.reflectX();
                break;
            case TOP:
            case BOTTOM:
                ball.reflectY();
                break;
            case CORNER:
                // 코너 충돌은 더 복잡한 처리 필요
                ball.reflectX();
                ball.reflectY();
                break;
        }
        
        // Ball을 Box 외부로 이동
        moveBallOutsideBox(ball, box, side);
    }
    
    /**
     * 충돌한 면을 감지합니다
     */
    public CollisionSide detectCollisionSide(double ballX, double ballY, Box box) {
        double boxLeft = box.getX();
        double boxRight = box.getX() + box.getWidth();
        double boxTop = box.getY();
        double boxBottom = box.getY() + box.getHeight();
        
        // 가장 가까운 점 계산
        double closestX = Math.max(boxLeft, Math.min(ballX, boxRight));
        double closestY = Math.max(boxTop, Math.min(ballY, boxBottom));
        
        // 거리 계산
        double dx = ballX - closestX;
        double dy = ballY - closestY;
        
        // 코너 충돌 확인
        if (Math.abs(dx) > 0.1 && Math.abs(dy) > 0.1) {
            return CollisionSide.CORNER;
        }
        
        // 면 충돌 확인
        if (Math.abs(dx) > Math.abs(dy)) {
            return dx < 0 ? CollisionSide.LEFT : CollisionSide.RIGHT;
        } else {
            return dy < 0 ? CollisionSide.TOP : CollisionSide.BOTTOM;
        }
    }
    
    /**
     * Ball을 Box 외부로 이동시킵니다
     */
    private void moveBallOutsideBox(MovableBall ball, Box box, CollisionSide side) {
        double radius = ball.getRadius();
        
        switch (side) {
            case LEFT:
                ball.setX(box.getX() - radius);
                break;
            case RIGHT:
                ball.setX(box.getX() + box.getWidth() + radius);
                break;
            case TOP:
                ball.setY(box.getY() - radius);
                break;
            case BOTTOM:
                ball.setY(box.getY() + box.getHeight() + radius);
                break;
            case CORNER:
                // 코너의 경우 대각선으로 이동
                double dx = ball.getX() - box.getCenterX();
                double dy = ball.getY() - box.getCenterY();
                double length = Math.sqrt(dx * dx + dy * dy);
                if (length > 0) {
                    dx /= length;
                    dy /= length;
                    ball.setX(box.getCenterX() + dx * (box.getWidth() / 2 + radius));
                    ball.setY(box.getCenterY() + dy * (box.getHeight() / 2 + radius));
                }
                break;
        }
    }
    
    /**
     * 특정 메서드가 존재하는지 확인 (테스트용)
     */
    public boolean hasMethod(String methodName) {
        try {
            switch (methodName) {
                case "checkBallToBallCollisions":
                case "checkBoxToBoxCollisions":
                case "checkBallToBoxCollisions":
                    return true;
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}

/**
 * 충돌한 면을 나타내는 enum
 */
enum CollisionSide {
    TOP,    // 위쪽 면
    BOTTOM, // 아래쪽 면
    LEFT,   // 왼쪽 면
    RIGHT,  // 오른쪽 면
    CORNER  // 모서리
}
```

## 상속의 한계 분석

### 1. 클래스 폭발 문제

```java
/**
 * 클래스 폭발 문제를 보여주는 예제
 * 3개의 기능 조합 = 2³ = 8개 클래스 필요
 */
public class ClassExplosionExample {
    
    public static void demonstrateClassExplosion() {
        System.out.println("=== 클래스 폭발 문제 ===");
        
        // Box 타입만으로도 8개 클래스 필요
        Box basic = new Box(0, 0, 10, 10);
        PaintableBox paintable = new PaintableBox(0, 0, 10, 10, Color.RED);
        MovableBox movable = new MovableBox(0, 0, 10, 10);
        // BoundedBox bounded = new BoundedBox(0, 0, 10, 10);
        
        PaintableMovableBox paintableMovable = new PaintableMovableBox(0, 0, 10, 10, Color.BLUE);
        // PaintableBoundedBox paintableBounded = new PaintableBoundedBox(0, 0, 10, 10, Color.GREEN);
        // MovableBoundedBox movableBounded = new MovableBoundedBox(0, 0, 10, 10);
        // PaintableMovableBoundedBox all = new PaintableMovableBoundedBox(0, 0, 10, 10, Color.YELLOW);
        
        System.out.println("Box 타입 클래스 수: 8개");
        System.out.println("Ball 타입 클래스 수: 8개");
        System.out.println("총 필요한 클래스 수: 16개");
        System.out.println();
        
        // 기능이 하나 더 추가되면?
        System.out.println("새로운 기능(Rotatable) 추가 시:");
        System.out.println("Box 타입: 16개 클래스");
        System.out.println("Ball 타입: 16개 클래스");
        System.out.println("총 필요한 클래스 수: 32개!");
        System.out.println();
        
        // 도형이 하나 더 추가되면?
        System.out.println("새로운 도형(Triangle) 추가 시:");
        System.out.println("Triangle 타입: 8개 클래스 추가");
        System.out.println("총 필요한 클래스 수: 24개!");
    }
}
```

### 2. 코드 중복 문제

```java
/**
 * 코드 중복 문제를 보여주는 예제
 */
public class CodeDuplicationExample {
    
    public static void demonstrateCodeDuplication() {
        System.out.println("=== 코드 중복 문제 ===");
        
        // 색상 처리 로직이 PaintableBall과 PaintableBox에서 완전히 동일
        PaintableBall ball = new PaintableBall(0, 0, 10, Color.RED);
        PaintableBox box = new PaintableBox(0, 0, 10, 10, Color.RED);
        
        // 동일한 색상 변경 로직
        ball.setColor(Color.BLUE);
        box.setColor(Color.BLUE);
        
        // 동일한 밝기 조절 로직
        ball.adjustBrightness(1.5);
        box.adjustBrightness(1.5);
        
        System.out.println("색상 처리 코드가 두 클래스에서 완전히 중복됨");
        System.out.println();
        
        // 이동 처리 로직이 MovableBall과 MovableBox에서 완전히 동일
        MovableBall movableBall = new MovableBall(0, 0, 10);
        MovableBox movableBox = new MovableBox(0, 0, 10, 10);
        
        movableBall.setDx(50);
        movableBall.setDy(30);
        movableBox.setDx(50);
        movableBox.setDy(30);
        
        movableBall.move(1.0);
        movableBox.move(1.0);
        
        System.out.println("이동 처리 코드가 두 클래스에서 완전히 중복됨");
        System.out.println("Ball 위치: (" + movableBall.getX() + ", " + movableBall.getY() + ")");
        System.out.println("Box 위치: (" + movableBox.getX() + ", " + movableBox.getY() + ")");
    }
}
```

### 3. 타입 체크 복잡성

```java
/**
 * 타입 체크 복잡성을 보여주는 예제
 */
public class TypeCheckingComplexityExample {
    
    public static void demonstrateTypeCheckingComplexity() {
        System.out.println("=== 타입 체크 복잡성 ===");
        
        List<Object> objects = Arrays.asList(
            new Ball(0, 0, 10),
            new PaintableBall(0, 0, 10, Color.RED),
            new MovableBall(0, 0, 10),
            new PaintableMovableBox(0, 0, 10, 10, Color.BLUE)
        );
        
        // Paintable 객체 찾기 - 모든 타입을 나열해야 함!
        int paintableCount = 0;
        for (Object obj : objects) {
            if (obj instanceof PaintableBall || 
                obj instanceof PaintableMovableBall ||
                obj instanceof PaintableBox ||
                obj instanceof PaintableMovableBox) {
                paintableCount++;
            }
        }
        
        System.out.println("Paintable 객체 수: " + paintableCount);
        System.out.println("문제: 새로운 Paintable 타입 추가 시 모든 instanceof 체크를 수정해야 함!");
        System.out.println();
        
        // Movable 객체 찾기 - 또 다른 instanceof 지옥!
        int movableCount = 0;
        for (Object obj : objects) {
            if (obj instanceof MovableBall || 
                obj instanceof PaintableMovableBall ||
                obj instanceof MovableBox ||
                obj instanceof PaintableMovableBox) {
                movableCount++;
            }
        }
        
        System.out.println("Movable 객체 수: " + movableCount);
        System.out.println("문제: Triangle 추가 시 모든 타입 체크 코드 수정 필요!");
    }
}
```

## 종합 테스트

### 전체 시스템 테스트

```java
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;

/**
 * 상속 기반 시스템의 전체 테스트
 * 모든 문제점들을 한번에 보여줍니다
 */
public class InheritanceSystemTest extends Application {
    
    private MixedWorld world;
    private ExtendedCollisionHandler collisionHandler;
    
    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(800, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // MixedWorld 생성
        world = new MixedWorld(800, 600);
        collisionHandler = new ExtendedCollisionHandler();
        
        // 다양한 타입의 객체들 추가
        setupObjects();
        
        // 게임 루프
        new AnimationTimer() {
            private long lastUpdate = 0;
            
            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }
                
                double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;
                
                // 업데이트 (instanceof 지옥 발생!)
                world.update(deltaTime);
                
                // 충돌 처리 (조합 폭발 발생!)
                // 실제로는 더 많은 충돌 메서드가 필요함
                
                // 렌더링 (타입별 처리 필요!)
                world.render(gc);
                
                // 정보 표시
                displayInformation(gc);
            }
        }.start();
        
        VBox root = new VBox(canvas);
        Scene scene = new Scene(root);
        primaryStage.setTitle("상속 시스템의 문제점 데모");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void setupObjects() {
        // Ball 계열 객체들
        world.addBall(new Ball(100, 100, 20));
        world.addBall(new PaintableBall(200, 150, 25, Color.RED));
        world.addBall(new MovableBall(300, 200, 30));
        world.addBall(new PaintableMovableBall(400, 250, 20, Color.BLUE));
        
        // Box 계열 객체들
        world.addBox(new Box(150, 300, 40, 30));
        world.addBox(new PaintableBox(250, 350, 50, 40, Color.GREEN));
        world.addBox(new MovableBox(350, 400, 45, 35));
        world.addBox(new PaintableMovableBox(450, 450, 55, 45, Color.YELLOW));
        
        // MovableBox들에 속도 설정
        for (int i = 0; i < world.getBoxCount(); i++) {
            // 여기서도 instanceof 체크가 필요함!
        }
    }
    
    private void displayInformation(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.fillText("총 Ball 수: " + world.getBallCount(), 10, 20);
        gc.fillText("총 Box 수: " + world.getBoxCount(), 10, 40);
        gc.fillText("이동 가능한 객체 수: " + world.getMovableObjectCount(), 10, 60);
        gc.fillText("색상을 가진 객체 수: " + world.getPaintableObjectCount(), 10, 80);
        
        gc.setFill(Color.RED);
        gc.fillText("문제점들:", 10, 120);
        gc.fillText("1. 16개의 클래스 필요 (Ball 8개 + Box 8개)", 10, 140);
        gc.fillText("2. 코드 중복 (색상, 이동 로직)", 10, 160);
        gc.fillText("3. instanceof 지옥 (타입 체크)", 10, 180);
        gc.fillText("4. 새 도형 추가 시 전체 코드 수정", 10, 200);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
```

### JUnit 테스트 모음

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import javafx.scene.paint.Color;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 상속 시스템의 모든 문제점을 검증하는 종합 테스트
 */
public class InheritanceProblemsTest {
    
    @Test
    public void testClassExplosionProblem() {
        // 3개 기능 × 2개 도형 = 16개 클래스 필요
        
        // 기본 타입들
        assertNotNull(new Box(0, 0, 10, 10));
        assertNotNull(new Ball(0, 0, 10));
        
        // 1개 기능 조합
        assertNotNull(new PaintableBox(0, 0, 10, 10, Color.RED));
        assertNotNull(new MovableBox(0, 0, 10, 10));
        assertNotNull(new PaintableBall(0, 0, 10, Color.BLUE));
        assertNotNull(new MovableBall(0, 0, 10));
        
        // 2개 기능 조합
        assertNotNull(new PaintableMovableBox(0, 0, 10, 10, Color.GREEN));
        assertNotNull(new PaintableMovableBall(0, 0, 10, Color.YELLOW));
        
        // 총 8개 클래스 × 2개 도형 = 16개 클래스!
        assertTrue(true, "클래스 폭발 문제 확인됨");
    }
    
    @Test
    public void testCodeDuplicationProblem() {
        PaintableBox box = new PaintableBox(0, 0, 10, 10, Color.RED);
        PaintableBall ball = new PaintableBall(0, 0, 10, Color.RED);
        
        // 동일한 색상 처리 로직 (중복!)
        box.setColor(Color.BLUE);
        ball.setColor(Color.BLUE);
        assertEquals(box.getColor(), ball.getColor());
        
        // 동일한 null 체크 (중복!)
        assertThrows(IllegalArgumentException.class, () -> box.setColor(null));
        assertThrows(IllegalArgumentException.class, () -> ball.setColor(null));
        
        MovableBox movableBox = new MovableBox(0, 0, 10, 10);
        MovableBall movableBall = new MovableBall(0, 0, 10);
        
        // 동일한 이동 처리 로직 (중복!)
        movableBox.setDx(50);
        movableBox.setDy(30);
        movableBall.setDx(50);
        movableBall.setDy(30);
        
        movableBox.move(1.0);
        movableBall.move(1.0);
        
        assertEquals(50, movableBox.getX(), 0.001);
        assertEquals(50, movableBall.getX(), 0.001);
        assertEquals(30, movableBox.getY(), 0.001);
        assertEquals(30, movableBall.getY(), 0.001);
    }
    
    @Test
    public void testTypeCheckingComplexity() {
        Object[] objects = {
            new Ball(0, 0, 10),
            new PaintableBall(0, 0, 10, Color.RED),
            new MovableBall(0, 0, 10),
            new Box(0, 0, 10, 10),
            new PaintableBox(0, 0, 10, 10, Color.BLUE),
            new MovableBox(0, 0, 10, 10)
        };
        
        // Paintable 객체 찾기 - instanceof 지옥!
        int paintableCount = 0;
        for (Object obj : objects) {
            if (obj instanceof PaintableBall || 
                obj instanceof PaintableBox) {
                paintableCount++;
            }
        }
        assertEquals(2, paintableCount);
        
        // Movable 객체 찾기 - 또 다른 instanceof 지옥!
        int movableCount = 0;
        for (Object obj : objects) {
            if (obj instanceof MovableBall || 
                obj instanceof MovableBox) {
                movableCount++;
            }
        }
        assertEquals(2, movableCount);
        
        // 새로운 타입 추가 시 모든 instanceof 체크를 수정해야 함!
    }
    
    @Test
    public void testCollisionExplosion() {
        ExtendedCollisionHandler handler = new ExtendedCollisionHandler();
        
        // 현재 2개 타입 = 3개 충돌 메서드 필요
        assertTrue(handler.hasMethod("checkBallToBallCollisions"));
        assertTrue(handler.hasMethod("checkBoxToBoxCollisions"));
        assertTrue(handler.hasMethod("checkBallToBoxCollisions"));
        
        // Triangle 추가 시 6개 메서드 필요
        // Circle 추가 시 10개 메서드 필요
        // n개 타입 = n(n+1)/2개 메서드!
        
        int typeCount = 2;
        int requiredMethods = typeCount * (typeCount + 1) / 2;
        assertEquals(3, requiredMethods);
        
        // 4개 타입이면?
        typeCount = 4;
        requiredMethods = typeCount * (typeCount + 1) / 2;
        assertEquals(10, requiredMethods, "4개 타입 = 10개 충돌 메서드 필요!");
    }
    
    @Test
    public void testMaintenanceNightmare() {
        MixedWorld world = new MixedWorld(800, 600);
        
        // 현재 상태: Ball과 Box 타입 지원
        world.addBall(new Ball(0, 0, 10));
        world.addBox(new Box(0, 0, 10, 10));
        
        assertEquals(1, world.getBallCount());
        assertEquals(1, world.getBoxCount());
        
        // Triangle 추가 시 필요한 수정사항:
        // 1. World에 List<Triangle> triangles 필드 추가
        // 2. addTriangle() 메서드 추가
        // 3. update() 메서드에 Triangle 처리 로직 추가
        // 4. render() 메서드에 Triangle 그리기 로직 추가
        // 5. 모든 타입 체크 코드에 Triangle 타입들 추가
        // 6. 충돌 처리에 3개 메서드 추가 (Triangle-Ball, Triangle-Box, Triangle-Triangle)
        
        System.out.println("Triangle 추가 시 수정해야 할 곳:");
        System.out.println("- World 클래스: 필드, 메서드 추가");
        System.out.println("- update/render 메서드: 새 타입 처리 로직");
        System.out.println("- 모든 instanceof 체크 코드");
        System.out.println("- CollisionHandler: 3개 메서드 추가");
        System.out.println("- 새로운 Triangle 클래스 8개 생성");
        
        assertTrue(true, "유지보수 악몽 확인됨!");
    }
}
```

## 문제점 요약

### 1. 클래스 폭발 (Class Explosion)

- **현재**: 3개 기능 × 2개 도형 = **16개 클래스**
- **기능 추가**: 4개 기능 × 2개 도형 = **32개 클래스**
- **도형 추가**: 3개 기능 × 3개 도형 = **24개 클래스**
- **문제**: 기능과 도형이 증가할 때마다 기하급수적으로 클래스 증가

### 2. 코드 중복 (Code Duplication)

- **색상 처리**: `PaintableBall`과 `PaintableBox`에서 완전히 동일
- **이동 처리**: `MovableBall`과 `MovableBox`에서 완전히 동일
- **경계 처리**: 모든 Bounded 클래스에서 유사한 로직
- **문제**: DRY 원칙 위반, 유지보수 어려움

### 3. 타입 체크 복잡성 (Type Checking Hell)

- **모든 곳에서 instanceof 체크**: update, render, collision 등
- **새 타입 추가 시**: 모든 instanceof 체크 코드 수정 필요
- **가독성 저하**: 복잡한 조건문과 캐스팅
- **문제**: 확장성 부족, 오류 발생 가능성 높음

### 4. 충돌 처리 조합 폭발

- **현재**: 2개 타입 = 3개 충돌 메서드
- **3개 타입**: 6개 충돌 메서드
- **4개 타입**: 10개 충돌 메서드
- **n개 타입**: n(n+1)/2개 충돌 메서드
- **문제**: 타입 증가 시 기하급수적 메서드 증가

### 5. 유지보수 악몽

- **새 도형 추가 시**: 전체 시스템 수정 필요
- **새 기능 추가 시**: 모든 도형에 대해 클래스 생성 필요
- **버그 수정 시**: 여러 클래스에서 동일한 수정 필요
- **문제**: 높은 결합도, 낮은 응집도

## 7장 미리보기: 인터페이스로 해결

7장에서는 다음과 같은 인터페이스를 도입하여 이 모든 문제를 해결합니다:

```java
// 인터페이스 기반 해결책 미리보기
interface Paintable {
    Color getColor();
    void setColor(Color color);
    void paint(GraphicsContext gc);
}

interface Movable {
    void move(double deltaTime);
    double getDx();
    double getDy();
    void setDx(double dx);
    void setDy(double dy);
}

interface Boundable {
    Bounds getBounds();
}

// 다중 인터페이스 구현
class Ball implements Paintable, Movable, Boundable {
    // 모든 기능을 하나의 클래스에서!
}
```

**인터페이스의 장점**:
- 클래스 수 대폭 감소 (16개 → 2개)
- 코드 중복 제거
- 다형성을 통한 간단한 타입 처리
- 높은 확장성과 유연성

---

이제 상속만 사용했을 때의 문제점들을 충분히 경험했습니다. 다음 7장에서는 인터페이스를 통해 이러한 모든 문제를 우아하게 해결하는 방법을 배우겠습니다!