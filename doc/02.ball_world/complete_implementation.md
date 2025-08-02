# 2장: Ball World 완전한 구현 가이드

## 목차
1. [학습 목표와 핵심 개념](#1-학습-목표와-핵심-개념)
2. [Ball 클래스 완전 구현](#2-ball-클래스-완전-구현)
3. [PaintableBall 클래스 - 상속의 첫걸음](#3-paintableball-클래스---상속의-첫걸음)
4. [BallWorld 클래스 - 세계 관리](#4-ballworld-클래스---세계-관리)
5. [테스트 코드와 검증](#5-테스트-코드와-검증)
6. [일반적인 실수와 해결법](#6-일반적인-실수와-해결법)

---

## 1. 학습 목표와 핵심 개념

### 1.1 학습 목표
- **객체 지향 기본**: 클래스와 객체의 차이점 이해
- **생성자 오버로딩**: 다양한 방식으로 객체 생성
- **상속 기초**: extends 키워드와 super 사용법
- **캡슐화**: private 필드와 public 메서드
- **JavaFX 기초**: Canvas와 GraphicsContext 사용

### 1.2 핵심 개념 요약

**상속(Inheritance)이란?**
```java
// 부모 클래스의 모든 것을 물려받는 것
public class Child extends Parent {
    // Parent의 모든 public/protected 멤버를 자동으로 가짐
    // 새로운 기능 추가 가능
    // 기존 기능 수정(오버라이드) 가능
}
```

**캡슐화의 원칙:**
- 데이터는 private으로 숨김
- 접근은 public 메서드를 통해서만
- 유효성 검사는 setter에서 수행

---

## 2. Ball 클래스 완전 구현

### 2.1 Ball 클래스 - 기본 버전

```java
/**
 * 2차원 공간의 원형 공을 나타내는 클래스
 * 위치, 크기, 기본적인 기하학적 연산을 제공합니다.
 */
public class Ball {
    // === 필드 (데이터 캡슐화) ===
    private double x;          // 중심의 X 좌표
    private double y;          // 중심의 Y 좌표  
    private double radius;     // 반지름 (항상 양수)
    
    // === 생성자들 (Constructor Overloading) ===
    
    /**
     * 기본 생성자 - 원점에 반지름 10인 공 생성
     * 이것은 편의를 위한 생성자입니다.
     */
    public Ball() {
        this(0.0, 0.0, 10.0);  // 다른 생성자 호출
    }
    
    /**
     * 매개변수가 있는 생성자 - 모든 속성 지정
     * @param x 중심의 X 좌표
     * @param y 중심의 Y 좌표
     * @param radius 반지름
     * @throws IllegalArgumentException 반지름이 0 이하인 경우
     */
    public Ball(double x, double y, double radius) {
        // 방어적 프로그래밍: 잘못된 입력값 검사
        if (radius <= 0) {
            throw new IllegalArgumentException(
                "반지름은 0보다 커야 합니다. 입력값: " + radius);
        }
        
        this.x = x;
        this.y = y;
        this.radius = radius;
    }
    
    // === 접근자 메서드들 (Getters) ===
    
    /**
     * X 좌표를 반환합니다
     * @return 중심의 X 좌표
     */
    public double getX() {
        return x;
    }
    
    /**
     * Y 좌표를 반환합니다
     * @return 중심의 Y 좌표
     */
    public double getY() {
        return y;
    }
    
    /**
     * 반지름을 반환합니다
     * @return 공의 반지름
     */
    public double getRadius() {
        return radius;
    }
    
    // === 수정자 메서드들 (Setters) ===
    
    /**
     * X 좌표를 설정합니다
     * @param x 새로운 X 좌표
     */
    public void setX(double x) {
        this.x = x;
    }
    
    /**
     * Y 좌표를 설정합니다
     * @param y 새로운 Y 좌표
     */
    public void setY(double y) {
        this.y = y;
    }
    
    // 반지름은 생성 후 변경 불가 (불변 속성)
    // setRadius() 메서드를 제공하지 않음
    
    // === 기하학적 연산 메서드들 ===
    
    /**
     * 공의 면적을 계산합니다
     * @return 면적 (π × r²)
     */
    public double getArea() {
        return Math.PI * radius * radius;
    }
    
    /**
     * 공의 둘레를 계산합니다
     * @return 둘레 (2π × r)
     */
    public double getCircumference() {
        return 2 * Math.PI * radius;
    }
    
    /**
     * 주어진 점이 공 안에 있는지 확인합니다
     * @param px 확인할 점의 X 좌표
     * @param py 확인할 점의 Y 좌표
     * @return 점이 공 내부에 있으면 true, 아니면 false
     */
    public boolean contains(double px, double py) {
        // 점과 중심 사이의 거리 계산
        // 제곱근 계산을 피하기 위해 거리의 제곱을 비교
        double distanceSquared = (px - x) * (px - x) + (py - y) * (py - y);
        double radiusSquared = radius * radius;
        
        return distanceSquared <= radiusSquared;
    }
    
    /**
     * 두 공 사이의 중심간 거리를 계산합니다
     * @param other 다른 공
     * @return 중심간 거리
     */
    public double distanceTo(Ball other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * 다른 공과 겹치는지 확인합니다
     * @param other 다른 공
     * @return 겹치면 true, 아니면 false
     */
    public boolean overlaps(Ball other) {
        double distance = distanceTo(other);
        return distance < (this.radius + other.radius);
    }
    
    // === Object 클래스 메서드 오버라이드 ===
    
    /**
     * 공의 문자열 표현을 반환합니다
     * @return 공의 정보를 담은 문자열
     */
    @Override
    public String toString() {
        return String.format("Ball[center=(%.2f, %.2f), radius=%.2f]", 
                           x, y, radius);
    }
    
    /**
     * 두 공이 같은지 비교합니다
     * @param obj 비교할 객체
     * @return 위치와 크기가 같으면 true
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Ball ball = (Ball) obj;
        return Double.compare(ball.x, x) == 0 &&
               Double.compare(ball.y, y) == 0 &&
               Double.compare(ball.radius, radius) == 0;
    }
    
    /**
     * 해시 코드를 계산합니다
     * @return 해시 코드
     */
    @Override
    public int hashCode() {
        return Objects.hash(x, y, radius);
    }
}
```

### 2.2 구현 세부사항 설명

#### 생성자 체이닝 (Constructor Chaining)
```java
public Ball() {
    this(0.0, 0.0, 10.0);  // ← this() 호출로 다른 생성자 사용
}
```
- `this()`는 같은 클래스의 다른 생성자를 호출
- 코드 중복을 방지하고 유지보수성 향상
- 반드시 생성자의 첫 번째 문장이어야 함

#### 방어적 프로그래밍 (Defensive Programming)
```java
if (radius <= 0) {
    throw new IllegalArgumentException("반지름은 0보다 커야 합니다");
}
```
- 잘못된 입력값에 대한 사전 검증
- 예외를 통해 오류 상황을 명확히 전달
- 객체의 불변 조건(invariant) 보장

#### 성능 최적화 기법
```java
// 제곱근 계산 회피
double distanceSquared = (px - x) * (px - x) + (py - y) * (py - y);
return distanceSquared <= radiusSquared;  // √d ≤ r → d² ≤ r²
```
- `Math.sqrt()`는 비용이 큰 연산
- 수학적 등가성을 이용한 최적화
- 게임에서 자주 사용되는 테크닉

---

## 3. PaintableBall 클래스 - 상속의 첫걸음

### 3.1 PaintableBall 완전 구현

```java
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 화면에 그릴 수 있는 공 클래스
 * Ball 클래스를 상속받아 색상 정보와 그리기 기능을 추가합니다.
 */
public class PaintableBall extends Ball {
    // === 추가 필드 ===
    private Color color;    // 공의 색상
    
    // === 생성자들 ===
    
    /**
     * 3개 매개변수 생성자 - 기본 색상(빨간색) 사용
     * @param x 중심의 X 좌표
     * @param y 중심의 Y 좌표
     * @param radius 반지름
     */
    public PaintableBall(double x, double y, double radius) {
        this(x, y, radius, Color.RED);  // 기본 색상 사용
    }
    
    /**
     * 4개 매개변수 생성자 - 모든 속성 지정
     * @param x 중심의 X 좌표
     * @param y 중심의 Y 좌표
     * @param radius 반지름
     * @param color 색상
     * @throws IllegalArgumentException 색상이 null인 경우
     */
    public PaintableBall(double x, double y, double radius, Color color) {
        // 부모 클래스 생성자 호출 (반드시 첫 번째 문장)
        super(x, y, radius);
        
        // 색상 유효성 검사
        if (color == null) {
            throw new IllegalArgumentException("색상은 null일 수 없습니다");
        }
        
        this.color = color;
    }
    
    // === 색상 관련 메서드들 ===
    
    /**
     * 공의 색상을 반환합니다
     * @return 현재 색상
     */
    public Color getColor() {
        return color;
    }
    
    /**
     * 공의 색상을 변경합니다
     * @param color 새로운 색상
     * @throws IllegalArgumentException 색상이 null인 경우
     */
    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("색상은 null일 수 없습니다");
        }
        this.color = color;
    }
    
    // === 그리기 메서드 ===
    
    /**
     * 공을 화면에 그립니다
     * @param gc 그래픽스 컨텍스트
     * @throws IllegalArgumentException gc가 null인 경우
     */
    public void paint(GraphicsContext gc) {
        if (gc == null) {
            throw new IllegalArgumentException("GraphicsContext는 null일 수 없습니다");
        }
        
        // 채우기 색상 설정
        gc.setFill(color);
        
        // 원 그리기: fillOval(좌상단X, 좌상단Y, 너비, 높이)
        // 중심 좌표에서 반지름만큼 뺀 위치부터 그림
        double topLeftX = getX() - getRadius();
        double topLeftY = getY() - getRadius();
        double diameter = getRadius() * 2;
        
        gc.fillOval(topLeftX, topLeftY, diameter, diameter);
        
        // 테두리 그리기 (선택사항)
        drawBorder(gc);
    }
    
    /**
     * 공의 테두리를 그립니다
     * @param gc 그래픽스 컨텍스트
     */
    private void drawBorder(GraphicsContext gc) {
        // 테두리 색상: 원래 색상보다 어둡게
        gc.setStroke(color.darker());
        gc.setLineWidth(1.0);
        
        double topLeftX = getX() - getRadius();
        double topLeftY = getY() - getRadius();
        double diameter = getRadius() * 2;
        
        gc.strokeOval(topLeftX, topLeftY, diameter, diameter);
    }
    
    /**
     * 반투명하게 그리기
     * @param gc 그래픽스 컨텍스트
     * @param opacity 불투명도 (0.0 ~ 1.0)
     */
    public void paintWithOpacity(GraphicsContext gc, double opacity) {
        if (opacity < 0.0 || opacity > 1.0) {
            throw new IllegalArgumentException("불투명도는 0.0과 1.0 사이여야 합니다: " + opacity);
        }
        
        // 현재 불투명도 저장
        double oldOpacity = gc.getGlobalAlpha();
        
        // 새로운 불투명도 설정
        gc.setGlobalAlpha(opacity);
        
        // 그리기
        paint(gc);
        
        // 불투명도 복원
        gc.setGlobalAlpha(oldOpacity);
    }
    
    // === 색상 변경 유틸리티 메서드들 ===
    
    /**
     * 색상을 더 밝게 만듭니다
     */
    public void brighten() {
        color = color.brighter();
    }
    
    /**
     * 색상을 더 어둡게 만듭니다
     */
    public void darken() {
        color = color.darker();
    }
    
    /**
     * 색상의 채도를 조정합니다
     * @param saturation 새로운 채도 (0.0 ~ 1.0)
     */
    public void setSaturation(double saturation) {
        if (saturation < 0.0 || saturation > 1.0) {
            throw new IllegalArgumentException("채도는 0.0과 1.0 사이여야 합니다");
        }
        color = color.deriveColor(0, saturation, 1.0, 1.0);
    }
    
    // === Object 메서드 오버라이드 ===
    
    @Override
    public String toString() {
        return String.format("PaintableBall[center=(%.2f, %.2f), radius=%.2f, color=%s]", 
                           getX(), getY(), getRadius(), color);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (!(obj instanceof PaintableBall)) return false;
        
        PaintableBall other = (PaintableBall) obj;
        return Objects.equals(color, other.color);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), color);
    }
}
```

### 3.2 상속 관계 이해하기

#### super 키워드의 3가지 사용법
```java
// 1. 부모 생성자 호출
super(x, y, radius);

// 2. 부모 메서드 호출  
@Override
public String toString() {
    return super.toString() + ", color=" + color;
}

// 3. 부모 필드 접근 (protected 필드만 가능)
// this.x 대신 super.x (같은 의미, 하지만 private이므로 불가능)
```

#### 메서드 오버라이드 vs 오버로드
```java
// 오버라이드: 부모의 메서드를 재정의
@Override
public String toString() { ... }  // Ball의 toString() 재정의

// 오버로드: 같은 이름, 다른 매개변수
public void paint(GraphicsContext gc) { ... }
public void paintWithOpacity(GraphicsContext gc, double opacity) { ... }
```

---

## 4. BallWorld 클래스 - 세계 관리

### 4.1 BallWorld 완전 구현

```java
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * 여러 개의 공을 관리하는 세계 클래스
 * 공들의 생성, 관리, 렌더링을 담당합니다.
 */
public class BallWorld {
    // === 필드 ===
    private double width;                    // 세계의 너비
    private double height;                   // 세계의 높이
    private List<PaintableBall> balls;       // 공들의 리스트
    private Color backgroundColor;           // 배경 색상
    private Random random;                   // 랜덤 생성기
    
    // === 생성자 ===
    
    /**
     * BallWorld 생성자
     * @param width 세계의 너비
     * @param height 세계의 높이
     */
    public BallWorld(double width, double height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("너비와 높이는 0보다 커야 합니다");
        }
        
        this.width = width;
        this.height = height;
        this.balls = new ArrayList<>();
        this.backgroundColor = Color.LIGHTBLUE;
        this.random = new Random();
    }
    
    // === 공 관리 메서드들 ===
    
    /**
     * 새로운 공을 추가합니다
     * @param ball 추가할 공
     */
    public void addBall(PaintableBall ball) {
        if (ball == null) {
            throw new IllegalArgumentException("공은 null일 수 없습니다");
        }
        balls.add(ball);
    }
    
    /**
     * 공을 제거합니다
     * @param ball 제거할 공
     * @return 성공적으로 제거되었으면 true
     */
    public boolean removeBall(PaintableBall ball) {
        return balls.remove(ball);
    }
    
    /**
     * 모든 공을 제거합니다
     */
    public void clearBalls() {
        balls.clear();
    }
    
    /**
     * 랜덤한 위치에 랜덤한 공을 생성합니다
     * @return 생성된 공
     */
    public PaintableBall createRandomBall() {
        // 공이 경계 안에 완전히 들어가도록 위치 제한
        double maxRadius = 30;
        double minRadius = 10;
        
        double radius = minRadius + random.nextDouble() * (maxRadius - minRadius);
        double x = radius + random.nextDouble() * (width - 2 * radius);
        double y = radius + random.nextDouble() * (height - 2 * radius);
        
        // 랜덤 색상 생성
        Color randomColor = Color.color(
            random.nextDouble(),  // Red
            random.nextDouble(),  // Green
            random.nextDouble()   // Blue
        );
        
        PaintableBall ball = new PaintableBall(x, y, radius, randomColor);
        addBall(ball);
        return ball;
    }
    
    /**
     * 지정된 개수만큼 랜덤한 공들을 생성합니다
     * @param count 생성할 공의 개수
     */
    public void createRandomBalls(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("개수는 0 이상이어야 합니다");
        }
        
        for (int i = 0; i < count; i++) {
            createRandomBall();
        }
    }
    
    /**
     * 주어진 위치에서 가장 가까운 공을 찾습니다
     * @param x X 좌표
     * @param y Y 좌표
     * @return 가장 가까운 공 (없으면 null)
     */
    public PaintableBall findNearestBall(double x, double y) {
        if (balls.isEmpty()) {
            return null;
        }
        
        PaintableBall nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (PaintableBall ball : balls) {
            double distance = Math.sqrt(
                Math.pow(ball.getX() - x, 2) + Math.pow(ball.getY() - y, 2)
            );
            
            if (distance < minDistance) {
                minDistance = distance;
                nearest = ball;
            }
        }
        
        return nearest;
    }
    
    /**
     * 주어진 위치에 있는 공을 찾습니다
     * @param x X 좌표
     * @param y Y 좌표
     * @return 해당 위치의 공 (없으면 null)
     */
    public PaintableBall findBallAt(double x, double y) {
        // 뒤에서부터 검사 (위에 그려진 공 우선)
        for (int i = balls.size() - 1; i >= 0; i--) {
            PaintableBall ball = balls.get(i);
            if (ball.contains(x, y)) {
                return ball;
            }
        }
        return null;
    }
    
    // === 충돌 검사 메서드들 ===
    
    /**
     * 겹치는 공들의 쌍을 찾습니다
     * @return 겹치는 공들의 쌍 리스트
     */
    public List<BallPair> findOverlappingBalls() {
        List<BallPair> overlapping = new ArrayList<>();
        
        // 모든 쌍을 검사 (중복 방지를 위해 i < j)
        for (int i = 0; i < balls.size(); i++) {
            for (int j = i + 1; j < balls.size(); j++) {
                PaintableBall ball1 = balls.get(i);
                PaintableBall ball2 = balls.get(j);
                
                if (ball1.overlaps(ball2)) {
                    overlapping.add(new BallPair(ball1, ball2));
                }
            }
        }
        
        return overlapping;
    }
    
    /**
     * 겹치는 공들을 분리합니다
     */
    public void separateOverlappingBalls() {
        List<BallPair> overlapping = findOverlappingBalls();
        
        for (BallPair pair : overlapping) {
            separateBalls(pair.getBall1(), pair.getBall2());
        }
    }
    
    /**
     * 두 공을 분리합니다
     * @param ball1 첫 번째 공
     * @param ball2 두 번째 공
     */
    private void separateBalls(PaintableBall ball1, PaintableBall ball2) {
        double dx = ball2.getX() - ball1.getX();
        double dy = ball2.getY() - ball1.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance == 0) {
            // 완전히 겹친 경우 임의 방향으로 이동
            dx = 1;
            dy = 0;
            distance = 1;
        }
        
        // 정규화된 방향 벡터
        double unitX = dx / distance;
        double unitY = dy / distance;
        
        // 필요한 분리 거리
        double requiredDistance = ball1.getRadius() + ball2.getRadius();
        double separationDistance = (requiredDistance - distance) / 2;
        
        // 각 공을 반대 방향으로 이동
        ball1.setX(ball1.getX() - unitX * separationDistance);
        ball1.setY(ball1.getY() - unitY * separationDistance);
        ball2.setX(ball2.getX() + unitX * separationDistance);
        ball2.setY(ball2.getY() + unitY * separationDistance);
    }
    
    // === 렌더링 메서드들 ===
    
    /**
     * 세계를 렌더링합니다
     * @param gc 그래픽스 컨텍스트
     */
    public void render(GraphicsContext gc) {
        if (gc == null) {
            throw new IllegalArgumentException("GraphicsContext는 null일 수 없습니다");
        }
        
        // 배경 그리기
        drawBackground(gc);
        
        // 모든 공 그리기
        for (PaintableBall ball : balls) {
            ball.paint(gc);
        }
        
        // 정보 표시
        drawInfo(gc);
    }
    
    /**
     * 배경을 그립니다
     */
    private void drawBackground(GraphicsContext gc) {
        gc.setFill(backgroundColor);
        gc.fillRect(0, 0, width, height);
    }
    
    /**
     * 정보를 화면에 표시합니다
     */
    private void drawInfo(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.setFont(javafx.scene.text.Font.font(14));
        
        String info = String.format("공의 개수: %d", balls.size());
        gc.fillText(info, 10, 20);
        
        // 겹치는 공의 개수 표시
        int overlappingCount = findOverlappingBalls().size();
        if (overlappingCount > 0) {
            gc.setFill(Color.RED);
            String warning = String.format("겹치는 쌍: %d", overlappingCount);
            gc.fillText(warning, 10, 40);
        }
    }
    
    // === 유틸리티 메서드들 ===
    
    /**
     * 경계를 벗어난 공들을 경계 안으로 이동시킵니다
     */
    public void keepBallsInBounds() {
        for (PaintableBall ball : balls) {
            double radius = ball.getRadius();
            double x = ball.getX();
            double y = ball.getY();
            
            // X 좌표 제한
            if (x - radius < 0) {
                ball.setX(radius);
            } else if (x + radius > width) {
                ball.setX(width - radius);
            }
            
            // Y 좌표 제한
            if (y - radius < 0) {
                ball.setY(radius);
            } else if (y + radius > height) {
                ball.setY(height - radius);
            }
        }
    }
    
    /**
     * 모든 공의 색상을 변경합니다
     * @param color 새로운 색상
     */
    public void changeAllBallColors(Color color) {
        for (PaintableBall ball : balls) {
            ball.setColor(color);
        }
    }
    
    /**
     * 모든 공의 색상을 랜덤하게 변경합니다
     */
    public void randomizeAllBallColors() {
        for (PaintableBall ball : balls) {
            Color randomColor = Color.color(
                random.nextDouble(),
                random.nextDouble(),
                random.nextDouble()
            );
            ball.setColor(randomColor);
        }
    }
    
    // === Getter/Setter 메서드들 ===
    
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public int getBallCount() { return balls.size(); }
    public List<PaintableBall> getBalls() { return new ArrayList<>(balls); }
    public Color getBackgroundColor() { return backgroundColor; }
    
    public void setBackgroundColor(Color color) {
        if (color != null) {
            this.backgroundColor = color;
        }
    }
}

// === 보조 클래스 ===

/**
 * 두 공의 쌍을 나타내는 클래스
 */
class BallPair {
    private final PaintableBall ball1;
    private final PaintableBall ball2;
    
    public BallPair(PaintableBall ball1, PaintableBall ball2) {
        this.ball1 = ball1;
        this.ball2 = ball2;
    }
    
    public PaintableBall getBall1() { return ball1; }
    public PaintableBall getBall2() { return ball2; }
    
    @Override
    public String toString() {
        return String.format("BallPair[%s, %s]", ball1, ball2);
    }
}
```

---

## 5. 테스트 코드와 검증

### 5.1 Ball 클래스 테스트

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Ball 클래스의 모든 기능을 테스트합니다
 */
public class BallTest {
    
    @Test
    @DisplayName("기본 생성자 테스트")
    void testDefaultConstructor() {
        Ball ball = new Ball();
        
        assertEquals(0.0, ball.getX(), 0.001, "기본 X 좌표는 0이어야 합니다");
        assertEquals(0.0, ball.getY(), 0.001, "기본 Y 좌표는 0이어야 합니다");
        assertEquals(10.0, ball.getRadius(), 0.001, "기본 반지름은 10이어야 합니다");
    }
    
    @Test
    @DisplayName("매개변수 생성자 테스트")
    void testParameterConstructor() {
        Ball ball = new Ball(100, 200, 25);
        
        assertEquals(100.0, ball.getX(), 0.001);
        assertEquals(200.0, ball.getY(), 0.001);
        assertEquals(25.0, ball.getRadius(), 0.001);
    }
    
    @Test
    @DisplayName("잘못된 반지름으로 생성 시 예외 발생")
    void testInvalidRadius() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Ball(0, 0, 0);  // 반지름 0
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Ball(0, 0, -5);  // 음수 반지름
        });
    }
    
    @Test
    @DisplayName("위치 설정 테스트")
    void testSetPosition() {
        Ball ball = new Ball();
        
        ball.setX(50);
        ball.setY(75);
        
        assertEquals(50.0, ball.getX(), 0.001);
        assertEquals(75.0, ball.getY(), 0.001);
    }
    
    @Test
    @DisplayName("면적 계산 테스트")
    void testArea() {
        Ball ball = new Ball(0, 0, 10);
        double expectedArea = Math.PI * 10 * 10;
        
        assertEquals(expectedArea, ball.getArea(), 0.001);
    }
    
    @Test
    @DisplayName("둘레 계산 테스트")
    void testCircumference() {
        Ball ball = new Ball(0, 0, 5);
        double expectedCircumference = 2 * Math.PI * 5;
        
        assertEquals(expectedCircumference, ball.getCircumference(), 0.001);
    }
    
    @Test
    @DisplayName("점 포함 여부 테스트")
    void testContains() {
        Ball ball = new Ball(50, 50, 20);
        
        // 중심점
        assertTrue(ball.contains(50, 50), "중심점은 포함되어야 합니다");
        
        // 경계 안의 점
        assertTrue(ball.contains(55, 55), "경계 안의 점은 포함되어야 합니다");
        
        // 경계선상의 점
        assertTrue(ball.contains(70, 50), "경계선상의 점은 포함되어야 합니다");
        
        // 경계 밖의 점
        assertFalse(ball.contains(80, 50), "경계 밖의 점은 포함되지 않아야 합니다");
    }
    
    @Test
    @DisplayName("두 공 사이의 거리 계산 테스트")
    void testDistanceTo() {
        Ball ball1 = new Ball(0, 0, 10);
        Ball ball2 = new Ball(3, 4, 5);  // 3-4-5 직각삼각형
        
        assertEquals(5.0, ball1.distanceTo(ball2), 0.001);
    }
    
    @Test
    @DisplayName("두 공 겹침 여부 테스트")
    void testOverlaps() {
        Ball ball1 = new Ball(0, 0, 10);
        Ball ball2 = new Ball(15, 0, 10);  // 거리 15, 반지름 합 20
        Ball ball3 = new Ball(25, 0, 10);  // 거리 25, 반지름 합 20
        
        assertTrue(ball1.overlaps(ball2), "겹치는 공은 true를 반환해야 합니다");
        assertFalse(ball1.overlaps(ball3), "겹치지 않는 공은 false를 반환해야 합니다");
    }
    
    @Test
    @DisplayName("equals 메서드 테스트")
    void testEquals() {
        Ball ball1 = new Ball(10, 20, 15);
        Ball ball2 = new Ball(10, 20, 15);
        Ball ball3 = new Ball(10, 20, 16);
        
        assertEquals(ball1, ball2, "같은 속성의 공은 같아야 합니다");
        assertNotEquals(ball1, ball3, "다른 속성의 공은 달라야 합니다");
    }
    
    @Test
    @DisplayName("toString 메서드 테스트")
    void testToString() {
        Ball ball = new Ball(10.5, 20.7, 15.3);
        String result = ball.toString();
        
        assertTrue(result.contains("10.50"), "X 좌표가 포함되어야 합니다");
        assertTrue(result.contains("20.70"), "Y 좌표가 포함되어야 합니다");
        assertTrue(result.contains("15.30"), "반지름이 포함되어야 합니다");
    }
}
```

### 5.2 PaintableBall 클래스 테스트

```java
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * PaintableBall 클래스의 모든 기능을 테스트합니다
 */
public class PaintableBallTest {
    
    @Test
    @DisplayName("3개 매개변수 생성자 테스트 - 기본 색상")
    void testThreeParameterConstructor() {
        PaintableBall ball = new PaintableBall(10, 20, 15);
        
        assertEquals(10.0, ball.getX(), 0.001);
        assertEquals(20.0, ball.getY(), 0.001);
        assertEquals(15.0, ball.getRadius(), 0.001);
        assertEquals(Color.RED, ball.getColor(), "기본 색상은 빨간색이어야 합니다");
    }
    
    @Test
    @DisplayName("4개 매개변수 생성자 테스트")
    void testFourParameterConstructor() {
        PaintableBall ball = new PaintableBall(30, 40, 25, Color.BLUE);
        
        assertEquals(30.0, ball.getX(), 0.001);
        assertEquals(40.0, ball.getY(), 0.001);
        assertEquals(25.0, ball.getRadius(), 0.001);
        assertEquals(Color.BLUE, ball.getColor());
    }
    
    @Test
    @DisplayName("null 색상으로 생성 시 예외 발생")
    void testNullColor() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PaintableBall(0, 0, 10, null);
        });
    }
    
    @Test
    @DisplayName("색상 변경 테스트")
    void testSetColor() {
        PaintableBall ball = new PaintableBall(0, 0, 10);
        
        ball.setColor(Color.GREEN);
        assertEquals(Color.GREEN, ball.getColor());
        
        // null 색상 설정 시 예외
        assertThrows(IllegalArgumentException.class, () -> {
            ball.setColor(null);
        });
    }
    
    @Test
    @DisplayName("색상 조정 메서드 테스트")
    void testColorAdjustment() {
        PaintableBall ball = new PaintableBall(0, 0, 10, Color.BLUE);
        Color originalColor = ball.getColor();
        
        // 밝게 하기
        ball.brighten();
        assertNotEquals(originalColor, ball.getColor(), "밝게 한 후 색상이 변해야 합니다");
        
        // 어둡게 하기
        ball.setColor(Color.YELLOW);
        Color yellowColor = ball.getColor();
        ball.darken();
        assertNotEquals(yellowColor, ball.getColor(), "어둡게 한 후 색상이 변해야 합니다");
    }
    
    @Test
    @DisplayName("채도 조정 테스트")
    void testSetSaturation() {
        PaintableBall ball = new PaintableBall(0, 0, 10, Color.RED);
        
        // 정상 범위
        assertDoesNotThrow(() -> ball.setSaturation(0.5));
        assertDoesNotThrow(() -> ball.setSaturation(0.0));
        assertDoesNotThrow(() -> ball.setSaturation(1.0));
        
        // 범위 벗어남
        assertThrows(IllegalArgumentException.class, () -> ball.setSaturation(-0.1));
        assertThrows(IllegalArgumentException.class, () -> ball.setSaturation(1.1));
    }
    
    @Test
    @DisplayName("상속 관계 테스트")
    void testInheritance() {
        PaintableBall ball = new PaintableBall(10, 20, 15);
        
        // Ball의 모든 메서드 사용 가능
        assertTrue(ball instanceof Ball, "PaintableBall은 Ball의 하위 타입이어야 합니다");
        assertEquals(10.0, ball.getX(), 0.001);
        assertTrue(ball.contains(10, 20), "부모 클래스의 contains 메서드 작동해야 합니다");
        
        // 추가된 기능
        assertEquals(Color.RED, ball.getColor(), "추가된 색상 기능이 작동해야 합니다");
    }
    
    @Test
    @DisplayName("toString 오버라이드 테스트")
    void testToStringOverride() {
        PaintableBall ball = new PaintableBall(5, 10, 8, Color.GREEN);
        String result = ball.toString();
        
        assertTrue(result.contains("PaintableBall"), "클래스명이 포함되어야 합니다");
        assertTrue(result.contains("5.00"), "X 좌표가 포함되어야 합니다");
        assertTrue(result.contains("10.00"), "Y 좌표가 포함되어야 합니다");
        assertTrue(result.contains("8.00"), "반지름이 포함되어야 합니다");
        assertTrue(result.contains("0x008000ff"), "색상 정보가 포함되어야 합니다");
    }
    
    @Test
    @DisplayName("equals 메서드 오버라이드 테스트")
    void testEqualsOverride() {
        PaintableBall ball1 = new PaintableBall(10, 20, 15, Color.BLUE);
        PaintableBall ball2 = new PaintableBall(10, 20, 15, Color.BLUE);
        PaintableBall ball3 = new PaintableBall(10, 20, 15, Color.RED);
        Ball ball4 = new Ball(10, 20, 15);  // 색상 없는 Ball
        
        assertEquals(ball1, ball2, "같은 속성의 PaintableBall은 같아야 합니다");
        assertNotEquals(ball1, ball3, "색상이 다른 PaintableBall은 달라야 합니다");
        assertNotEquals(ball1, ball4, "PaintableBall과 Ball은 달라야 합니다");
    }
}
```

### 5.3 BallWorld 클래스 테스트

```java
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * BallWorld 클래스의 모든 기능을 테스트합니다
 */
public class BallWorldTest {
    
    private BallWorld world;
    
    @BeforeEach
    void setUp() {
        world = new BallWorld(800, 600);
    }
    
    @Test
    @DisplayName("BallWorld 생성 테스트")
    void testBallWorldCreation() {
        assertEquals(800, world.getWidth(), 0.001);
        assertEquals(600, world.getHeight(), 0.001);
        assertEquals(0, world.getBallCount(), "초기 공 개수는 0이어야 합니다");
    }
    
    @Test
    @DisplayName("잘못된 크기로 생성 시 예외 발생")
    void testInvalidSize() {
        assertThrows(IllegalArgumentException.class, () -> {
            new BallWorld(0, 100);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new BallWorld(100, -50);
        });
    }
    
    @Test
    @DisplayName("공 추가 및 제거 테스트")
    void testAddRemoveBall() {
        PaintableBall ball = new PaintableBall(100, 100, 20);
        
        // 추가
        world.addBall(ball);
        assertEquals(1, world.getBallCount());
        assertTrue(world.getBalls().contains(ball));
        
        // 제거
        assertTrue(world.removeBall(ball));
        assertEquals(0, world.getBallCount());
        assertFalse(world.getBalls().contains(ball));
        
        // 없는 공 제거
        assertFalse(world.removeBall(ball));
    }
    
    @Test
    @DisplayName("null 공 추가 시 예외 발생")
    void testAddNullBall() {
        assertThrows(IllegalArgumentException.class, () -> {
            world.addBall(null);
        });
    }
    
    @Test
    @DisplayName("랜덤 공 생성 테스트")
    void testCreateRandomBall() {
        PaintableBall ball = world.createRandomBall();
        
        assertNotNull(ball, "랜덤 공이 생성되어야 합니다");
        assertEquals(1, world.getBallCount(), "월드에 공이 추가되어야 합니다");
        
        // 경계 안에 있는지 확인
        assertTrue(ball.getX() >= ball.getRadius(), "공이 좌측 경계 안에 있어야 합니다");
        assertTrue(ball.getX() <= world.getWidth() - ball.getRadius(), "공이 우측 경계 안에 있어야 합니다");
        assertTrue(ball.getY() >= ball.getRadius(), "공이 상단 경계 안에 있어야 합니다");
        assertTrue(ball.getY() <= world.getHeight() - ball.getRadius(), "공이 하단 경계 안에 있어야 합니다");
    }
    
    @Test
    @DisplayName("여러 랜덤 공 생성 테스트")
    void testCreateRandomBalls() {
        world.createRandomBalls(5);
        assertEquals(5, world.getBallCount());
        
        // 음수 개수
        assertThrows(IllegalArgumentException.class, () -> {
            world.createRandomBalls(-1);
        });
    }
    
    @Test
    @DisplayName("위치로 공 찾기 테스트")
    void testFindBallAt() {
        PaintableBall ball = new PaintableBall(100, 100, 20);
        world.addBall(ball);
        
        // 공 중심
        assertEquals(ball, world.findBallAt(100, 100));
        
        // 공 안의 점
        assertEquals(ball, world.findBallAt(110, 110));
        
        // 공 밖의 점
        assertNull(world.findBallAt(200, 200));
    }
    
    @Test
    @DisplayName("가장 가까운 공 찾기 테스트")
    void testFindNearestBall() {
        PaintableBall ball1 = new PaintableBall(100, 100, 20);
        PaintableBall ball2 = new PaintableBall(200, 200, 20);
        world.addBall(ball1);
        world.addBall(ball2);
        
        // (120, 120)에서 가장 가까운 공은 ball1
        assertEquals(ball1, world.findNearestBall(120, 120));
        
        // (180, 180)에서 가장 가까운 공은 ball2  
        assertEquals(ball2, world.findNearestBall(180, 180));
        
        // 공이 없는 경우
        world.clearBalls();
        assertNull(world.findNearestBall(100, 100));
    }
    
    @Test
    @DisplayName("겹치는 공 찾기 테스트")
    void testFindOverlappingBalls() {
        PaintableBall ball1 = new PaintableBall(100, 100, 20);
        PaintableBall ball2 = new PaintableBall(130, 100, 20);  // 겹침
        PaintableBall ball3 = new PaintableBall(200, 100, 20);  // 겹치지 않음
        
        world.addBall(ball1);
        world.addBall(ball2);
        world.addBall(ball3);
        
        List<BallPair> overlapping = world.findOverlappingBalls();
        assertEquals(1, overlapping.size(), "겹치는 쌍이 1개여야 합니다");
        
        BallPair pair = overlapping.get(0);
        assertTrue((pair.getBall1() == ball1 && pair.getBall2() == ball2) ||
                  (pair.getBall1() == ball2 && pair.getBall2() == ball1),
                  "올바른 공 쌍이어야 합니다");
    }
    
    @Test
    @DisplayName("경계 안에 공 유지 테스트")
    void testKeepBallsInBounds() {
        // 경계를 벗어난 공 생성
        PaintableBall ball = new PaintableBall(-10, -10, 20);
        world.addBall(ball);
        
        world.keepBallsInBounds();
        
        // 경계 안으로 이동했는지 확인
        assertTrue(ball.getX() >= ball.getRadius());
        assertTrue(ball.getY() >= ball.getRadius());
    }
    
    @Test
    @DisplayName("모든 공 색상 변경 테스트")
    void testChangeAllBallColors() {
        world.createRandomBalls(3);
        world.changeAllBallColors(Color.PURPLE);
        
        for (PaintableBall ball : world.getBalls()) {
            assertEquals(Color.PURPLE, ball.getColor());
        }
    }
    
    @Test
    @DisplayName("모든 공 색상 랜덤화 테스트")
    void testRandomizeAllBallColors() {
        world.createRandomBalls(5);
        List<Color> originalColors = new ArrayList<>();
        
        for (PaintableBall ball : world.getBalls()) {
            originalColors.add(ball.getColor());
        }
        
        world.randomizeAllBallColors();
        
        // 최소 하나는 색상이 변경되었을 것으로 예상
        boolean colorChanged = false;
        List<PaintableBall> balls = world.getBalls();
        for (int i = 0; i < balls.size(); i++) {
            if (!balls.get(i).getColor().equals(originalColors.get(i))) {
                colorChanged = true;
                break;
            }
        }
        
        assertTrue(colorChanged, "최소 하나의 공은 색상이 변경되어야 합니다");
    }
    
    @Test
    @DisplayName("모든 공 제거 테스트")
    void testClearBalls() {
        world.createRandomBalls(10);
        assertEquals(10, world.getBallCount());
        
        world.clearBalls();
        assertEquals(0, world.getBallCount());
        assertTrue(world.getBalls().isEmpty());
    }
}
```

---

## 6. 일반적인 실수와 해결법

### 6.1 생성자 관련 실수

#### ❌ 잘못된 예제
```java
public Ball() {
    // super() 호출 없음 - 컴파일 에러는 없지만 부모 생성자가 자동 호출됨
    x = 0;
    y = 0;
    radius = 10;
}

public PaintableBall(double x, double y, double radius, Color color) {
    this.x = x;  // ❌ 부모 생성자 호출 전에 필드 접근
    super(x, y, radius);  // ❌ super()는 반드시 첫 번째 문장이어야 함
    this.color = color;
}
```

#### ✅ 올바른 예제
```java
public Ball() {
    this(0.0, 0.0, 10.0);  // ✅ 다른 생성자 호출
}

public PaintableBall(double x, double y, double radius, Color color) {
    super(x, y, radius);  // ✅ 첫 번째 문장에서 부모 생성자 호출
    if (color == null) {
        throw new IllegalArgumentException("색상은 null일 수 없습니다");
    }
    this.color = color;
}
```

### 6.2 접근 제한자 실수

#### ❌ 잘못된 예제
```java
public class Ball {
    public double x, y, radius;  // ❌ 직접 접근 가능, 캡슐화 위반
}

// 사용 코드
Ball ball = new Ball();
ball.radius = -10;  // ❌ 잘못된 값 설정 가능
```

#### ✅ 올바른 예제
```java
public class Ball {
    private double x, y, radius;  // ✅ 캡슐화
    
    public void setRadius(double radius) {
        if (radius <= 0) {
            throw new IllegalArgumentException("반지름은 0보다 커야 합니다");
        }
        this.radius = radius;
    }
}
```

### 6.3 상속과 오버라이드 실수

#### ❌ 잘못된 예제
```java
public class PaintableBall extends Ball {
    @Override
    public String toString(int format) {  // ❌ 다른 매개변수 = 오버로드
        return "PaintableBall";
    }
    
    @Override
    public boolean equals(PaintableBall other) {  // ❌ 다른 매개변수 타입
        return this.color.equals(other.color);
    }
}
```

#### ✅ 올바른 예제
```java
public class PaintableBall extends Ball {
    @Override
    public String toString() {  // ✅ 정확한 시그니처
        return super.toString() + ", color=" + color;
    }
    
    @Override
    public boolean equals(Object obj) {  // ✅ Object 타입 매개변수
        if (!super.equals(obj)) return false;
        if (!(obj instanceof PaintableBall)) return false;
        PaintableBall other = (PaintableBall) obj;
        return Objects.equals(this.color, other.color);
    }
}
```

### 6.4 JavaFX 그리기 실수

#### ❌ 잘못된 예제
```java
public void paint(GraphicsContext gc) {
    // ❌ 중심 좌표를 그대로 사용
    gc.fillOval(x, y, radius * 2, radius * 2);
}
```

#### ✅ 올바른 예제
```java
public void paint(GraphicsContext gc) {
    // ✅ 좌상단 좌표 계산
    double topLeftX = x - radius;
    double topLeftY = y - radius;
    double diameter = radius * 2;
    gc.fillOval(topLeftX, topLeftY, diameter, diameter);
}
```

### 6.5 컬렉션 사용 실수

#### ❌ 잘못된 예제
```java
public List<PaintableBall> getBalls() {
    return balls;  // ❌ 내부 리스트 직접 반환 - 캡슐화 위반
}

// 사용 코드
List<PaintableBall> balls = world.getBalls();
balls.clear();  // ❌ 외부에서 내부 데이터 조작 가능
```

#### ✅ 올바른 예제
```java
public List<PaintableBall> getBalls() {
    return new ArrayList<>(balls);  // ✅ 복사본 반환
}
```

### 6.6 실수 방지 체크리스트

- [ ] **생성자**: `super()` 또는 `this()` 호출이 첫 번째 문장인가?
- [ ] **접근 제한자**: 필드는 `private`, 메서드는 필요한 만큼만 `public`인가?
- [ ] **유효성 검사**: 생성자와 setter에서 잘못된 값을 검사하는가?
- [ ] **null 검사**: 참조 타입 매개변수에 대해 null 검사를 하는가?
- [ ] **오버라이드**: `@Override` 어노테이션을 사용하고 시그니처가 정확한가?
- [ ] **캡슐화**: 내부 데이터의 복사본을 반환하는가?
- [ ] **JavaFX**: 좌표 계산이 정확한가? (중심 → 좌상단)

---

## 학습 포인트 정리

### 2장에서 배운 핵심 개념들

1. **객체 지향 프로그래밍의 기초**
   - 클래스와 객체의 관계
   - 캡슐화의 중요성
   - 생성자의 역할과 오버로딩

2. **상속의 기본 개념**
   - `extends` 키워드 사용법
   - `super` 키워드의 세 가지 용도
   - 메서드 오버라이드의 규칙

3. **방어적 프로그래밍**
   - 매개변수 유효성 검사
   - 예외를 통한 오류 처리
   - 불변 조건(invariant) 유지

4. **JavaFX 그래픽스 기초**
   - Canvas와 GraphicsContext 사용법
   - 좌표계 이해 (중심 좌표 vs 좌상단 좌표)
   - 색상과 그리기 옵션

이제 3장에서는 이 기초 위에 움직임을 추가하여 더 역동적인 세계를 만들어보겠습니다!