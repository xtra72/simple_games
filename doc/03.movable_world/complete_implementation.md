# 3장: Movable World 완전한 구현 가이드

## 목차
1. [학습 목표와 핵심 개념](#1-학습-목표와-핵심-개념)
2. [MovableBall 클래스 - 움직임의 구현](#2-movableball-클래스---움직임의-구현)
3. [물리 엔진 기초](#3-물리-엔진-기초)
4. [MovableWorld 클래스 - 시간 기반 시뮬레이션](#4-movableworld-클래스---시간-기반-시뮬레이션)
5. [애니메이션 루프와 게임 루프](#5-애니메이션-루프와-게임-루프)
6. [테스트 코드와 검증](#6-테스트-코드와-검증)
7. [일반적인 실수와 해결법](#7-일반적인-실수와-해결법)

---

## 1. 학습 목표와 핵심 개념

### 1.1 학습 목표
- **시간 기반 움직임**: deltaTime을 이용한 프레임 독립적 애니메이션
- **벡터와 속도**: 2D 벡터를 이용한 움직임 표현
- **물리 시뮬레이션**: 기본적인 물리 법칙 적용
- **게임 루프**: 업데이트-렌더링 패턴 이해
- **성능 최적화**: 부드러운 애니메이션을 위한 기법

### 1.2 핵심 개념 요약

**시간 기반 애니메이션이란?**
```java
// 프레임 기반 (나쁜 예)
x += 5;  // 프레임마다 5픽셀 이동 - FPS에 따라 속도 변함

// 시간 기반 (좋은 예)  
x += velocity * deltaTime;  // 초당 velocity 픽셀 이동 - FPS 무관
```

**속도 벡터(Velocity Vector)**
- `dx`: X 방향 속도 (픽셀/초)
- `dy`: Y 방향 속도 (픽셀/초)
- 벡터의 크기 = 속력, 방향 = 이동 방향

---

## 2. MovableBall 클래스 - 움직임의 구현

### 2.1 MovableBall 완전 구현

```java
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 움직일 수 있는 공 클래스
 * PaintableBall을 상속받아 속도와 움직임 기능을 추가합니다.
 */
public class MovableBall extends PaintableBall {
    // === 움직임 관련 필드 ===
    private double dx;          // X 방향 속도 (픽셀/초)
    private double dy;          // Y 방향 속도 (픽셀/초)
    private double maxSpeed;    // 최대 속력 제한
    
    // === 물리 속성 ===
    private double mass;        // 질량 (나중에 충돌에서 사용)
    private double friction;    // 마찰 계수 (0.0 ~ 1.0)
    private boolean active;     // 활성 상태 (비활성 시 움직이지 않음)
    
    // === 생성자들 ===
    
    /**
     * 기본 생성자 - 정지 상태의 공 생성
     */
    public MovableBall(double x, double y, double radius) {
        this(x, y, radius, Color.RED, 0, 0);
    }
    
    /**
     * 색상 지정 생성자
     */
    public MovableBall(double x, double y, double radius, Color color) {
        this(x, y, radius, color, 0, 0);
    }
    
    /**
     * 속도까지 지정하는 생성자
     */
    public MovableBall(double x, double y, double radius, double dx, double dy) {
        this(x, y, radius, Color.RED, dx, dy);
    }
    
    /**
     * 모든 속성을 지정하는 완전한 생성자
     * @param x 초기 X 좌표
     * @param y 초기 Y 좌표
     * @param radius 반지름
     * @param color 색상
     * @param dx X 방향 초기 속도
     * @param dy Y 방향 초기 속도
     */
    public MovableBall(double x, double y, double radius, Color color, double dx, double dy) {
        super(x, y, radius, color);
        
        this.dx = dx;
        this.dy = dy;
        this.maxSpeed = 500.0;  // 기본 최대 속력: 500 픽셀/초
        this.mass = Math.PI * radius * radius;  // 면적에 비례하는 질량
        this.friction = 0.99;   // 기본 마찰 계수 (거의 마찰 없음)
        this.active = true;
    }
    
    // === 움직임 메서드들 ===
    
    /**
     * 주어진 시간만큼 공을 이동시킹니다
     * @param deltaTime 경과 시간 (초 단위)
     */
    public void move(double deltaTime) {
        if (!active || deltaTime <= 0) {
            return;
        }
        
        // 마찰 적용
        applyFriction(deltaTime);
        
        // 속력 제한 적용
        limitSpeed();
        
        // 위치 업데이트
        double newX = getX() + dx * deltaTime;
        double newY = getY() + dy * deltaTime;
        
        setX(newX);
        setY(newY);
    }
    
    /**
     * 마찰을 적용합니다
     * @param deltaTime 경과 시간
     */
    private void applyFriction(double deltaTime) {
        double frictionFactor = Math.pow(friction, deltaTime);
        dx *= frictionFactor;
        dy *= frictionFactor;
        
        // 매우 작은 속도는 0으로 처리 (성능 최적화)
        if (Math.abs(dx) < 0.1) dx = 0;
        if (Math.abs(dy) < 0.1) dy = 0;
    }
    
    /**
     * 속력을 최대값으로 제한합니다
     */
    private void limitSpeed() {
        double currentSpeed = getSpeed();
        if (currentSpeed > maxSpeed) {
            double ratio = maxSpeed / currentSpeed;
            dx *= ratio;
            dy *= ratio;
        }
    }
    
    // === 속도 관련 메서드들 ===
    
    /**
     * X 방향 속도를 반환합니다
     * @return X 방향 속도 (픽셀/초)
     */
    public double getDx() {
        return dx;
    }
    
    /**
     * Y 방향 속도를 반환합니다
     * @return Y 방향 속도 (픽셀/초)
     */
    public double getDy() {
        return dy;
    }
    
    /**
     * X 방향 속도를 설정합니다
     * @param dx 새로운 X 방향 속도
     */
    public void setDx(double dx) {
        this.dx = dx;
    }
    
    /**
     * Y 방향 속도를 설정합니다
     * @param dy 새로운 Y 방향 속도
     */
    public void setDy(double dy) {
        this.dy = dy;
    }
    
    /**
     * 속도 벡터를 한 번에 설정합니다
     * @param dx X 방향 속도
     * @param dy Y 방향 속도
     */
    public void setVelocity(double dx, double dy) {
        this.dx = dx;
        this.dy = dy;
    }
    
    /**
     * 현재 속력(속도의 크기)을 반환합니다
     * @return 속력 (픽셀/초)
     */
    public double getSpeed() {
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * 현재 이동 방향을 라디안 단위로 반환합니다
     * @return 방향 (라디안, 0 = 오른쪽, π/2 = 아래쪽)
     */
    public double getDirection() {
        return Math.atan2(dy, dx);
    }
    
    /**
     * 속력과 방향을 이용해 속도를 설정합니다
     * @param speed 속력 (픽셀/초)
     * @param direction 방향 (라디안)
     */
    public void setSpeedAndDirection(double speed, double direction) {
        this.dx = speed * Math.cos(direction);
        this.dy = speed * Math.sin(direction);
    }
    
    // === 힘과 가속도 메서드들 ===
    
    /**
     * 힘을 가해 속도를 변경합니다
     * @param forceX X 방향 힘
     * @param forceY Y 방향 힘
     * @param deltaTime 적용 시간
     */
    public void applyForce(double forceX, double forceY, double deltaTime) {
        if (mass <= 0) {
            throw new IllegalStateException("질량이 0 이하입니다");
        }
        
        // F = ma → a = F/m
        double accelerationX = forceX / mass;
        double accelerationY = forceY / mass;
        
        // v = v₀ + at
        dx += accelerationX * deltaTime;
        dy += accelerationY * deltaTime;
    }
    
    /**
     * 중력을 적용합니다
     * @param gravity 중력 가속도 (픽셀/초²)
     * @param deltaTime 적용 시간
     */
    public void applyGravity(double gravity, double deltaTime) {
        dy += gravity * deltaTime;
    }
    
    /**
     * 충격량을 적용합니다 (즉시 속도 변화)
     * @param impulseX X 방향 충격량
     * @param impulseY Y 방향 충격량
     */
    public void applyImpulse(double impulseX, double impulseY) {
        if (mass <= 0) {
            throw new IllegalStateException("질량이 0 이하입니다");
        }
        
        // J = mΔv → Δv = J/m
        dx += impulseX / mass;
        dy += impulseY / mass;
    }
    
    // === 유틸리티 메서드들 ===
    
    /**
     * 공을 정지시킵니다
     */
    public void stop() {
        dx = 0;
        dy = 0;
    }
    
    /**
     * 지정한 속도로 가속합니다
     * @param targetDx 목표 X 속도
     * @param targetDy 목표 Y 속도
     * @param acceleration 가속도 (픽셀/초²)
     * @param deltaTime 경과 시간
     */
    public void accelerateTowards(double targetDx, double targetDy, double acceleration, double deltaTime) {
        double diffX = targetDx - dx;
        double diffY = targetDy - dy;
        double distance = Math.sqrt(diffX * diffX + diffY * diffY);
        
        if (distance > 0) {
            double maxChange = acceleration * deltaTime;
            if (distance <= maxChange) {
                // 목표에 도달
                dx = targetDx;
                dy = targetDy;
            } else {
                // 목표 방향으로 가속
                double ratio = maxChange / distance;
                dx += diffX * ratio;
                dy += diffY * ratio;
            }
        }
    }
    
    /**
     * 특정 지점을 향해 이동합니다
     * @param targetX 목표 X 좌표
     * @param targetY 목표 Y 좌표
     * @param speed 이동 속력
     */
    public void moveTowards(double targetX, double targetY, double speed) {
        double diffX = targetX - getX();
        double diffY = targetY - getY();
        double distance = Math.sqrt(diffX * diffX + diffY * diffY);
        
        if (distance > 0) {
            dx = (diffX / distance) * speed;
            dy = (diffY / distance) * speed;
        }
    }
    
    /**
     * 속도를 반사시킵니다 (벽과의 충돌 등에 사용)
     * @param normalX 반사면의 법선 벡터 X
     * @param normalY 반사면의 법선 벡터 Y
     * @param restitution 반발 계수 (0 = 완전 비탄성, 1 = 완전 탄성)
     */
    public void reflect(double normalX, double normalY, double restitution) {
        // 법선 벡터 정규화
        double length = Math.sqrt(normalX * normalX + normalY * normalY);
        if (length == 0) return;
        
        normalX /= length;
        normalY /= length;
        
        // 속도를 법선과 접선 성분으로 분리
        double dotProduct = dx * normalX + dy * normalY;
        
        // 법선 성분만 반사 (접선 성분은 유지)
        dx -= (1 + restitution) * dotProduct * normalX;
        dy -= (1 + restitution) * dotProduct * normalY;
    }
    
    // === 상태 확인 메서드들 ===
    
    /**
     * 공이 움직이고 있는지 확인합니다
     * @return 움직이고 있으면 true
     */
    public boolean isMoving() {
        return getSpeed() > 0.1;  // 임계값 이상의 속도
    }
    
    /**
     * 공이 활성 상태인지 확인합니다
     * @return 활성이면 true
     */
    public boolean isActive() {
        return active;
    }
    
    /**
     * 공의 활성 상태를 설정합니다
     * @param active 활성 상태
     */
    public void setActive(boolean active) {
        this.active = active;
    }
    
    // === 물리 속성 접근자들 ===
    
    public double getMass() { return mass; }
    public double getFriction() { return friction; }
    public double getMaxSpeed() { return maxSpeed; }
    
    public void setMass(double mass) {
        if (mass <= 0) {
            throw new IllegalArgumentException("질량은 0보다 커야 합니다");
        }
        this.mass = mass;
    }
    
    public void setFriction(double friction) {
        if (friction < 0 || friction > 1) {
            throw new IllegalArgumentException("마찰 계수는 0과 1 사이여야 합니다");
        }
        this.friction = friction;
    }
    
    public void setMaxSpeed(double maxSpeed) {
        if (maxSpeed < 0) {
            throw new IllegalArgumentException("최대 속력은 0 이상이어야 합니다");
        }
        this.maxSpeed = maxSpeed;
    }
    
    // === 렌더링 관련 메서드들 ===
    
    /**
     * 공을 그리면서 속도 벡터도 표시합니다
     * @param gc 그래픽스 컨텍스트
     * @param showVelocity 속도 벡터를 표시할지 여부
     */
    public void paint(GraphicsContext gc, boolean showVelocity) {
        // 기본 공 그리기
        super.paint(gc);
        
        if (showVelocity && isMoving()) {
            drawVelocityVector(gc);
        }
    }
    
    /**
     * 속도 벡터를 화살표로 그립니다
     * @param gc 그래픽스 컨텍스트
     */
    private void drawVelocityVector(GraphicsContext gc) {
        // 속도 벡터를 화면에 맞게 스케일링
        double scale = 0.1;  // 속도를 10배 줄여서 표시
        double arrowX = dx * scale;
        double arrowY = dy * scale;
        
        // 화살표 그리기
        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(2);
        
        double startX = getX();
        double startY = getY();
        double endX = startX + arrowX;
        double endY = startY + arrowY;
        
        // 화살표 몸체
        gc.strokeLine(startX, startY, endX, endY);
        
        // 화살표 머리
        if (getSpeed() > 0) {
            double arrowLength = 8;
            double arrowAngle = Math.PI / 6;  // 30도
            
            double angle = Math.atan2(arrowY, arrowX);
            
            double arrowX1 = endX - arrowLength * Math.cos(angle - arrowAngle);
            double arrowY1 = endY - arrowLength * Math.sin(angle - arrowAngle);
            double arrowX2 = endX - arrowLength * Math.cos(angle + arrowAngle);
            double arrowY2 = endY - arrowLength * Math.sin(angle + arrowAngle);
            
            gc.strokeLine(endX, endY, arrowX1, arrowY1);
            gc.strokeLine(endX, endY, arrowX2, arrowY2);
        }
    }
    
    // === Object 메서드 오버라이드 ===
    
    @Override
    public String toString() {
        return String.format("MovableBall[center=(%.2f, %.2f), radius=%.2f, velocity=(%.2f, %.2f), speed=%.2f]",
                           getX(), getY(), getRadius(), dx, dy, getSpeed());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (!(obj instanceof MovableBall)) return false;
        
        MovableBall other = (MovableBall) obj;
        return Double.compare(other.dx, dx) == 0 &&
               Double.compare(other.dy, dy) == 0 &&
               Double.compare(other.mass, mass) == 0;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), dx, dy, mass);
    }
}
```

---

## 3. 물리 엔진 기초

### 3.1 물리 법칙들

#### 뉴턴의 운동 법칙
```java
// 1법칙: 관성의 법칙
// 힘이 0이면 속도는 일정하게 유지됨
if (force == 0) {
    // velocity remains constant
    position += velocity * deltaTime;
}

// 2법칙: F = ma
// 힘 = 질량 × 가속도
double acceleration = force / mass;
velocity += acceleration * deltaTime;

// 3법칙: 작용-반작용 법칙
// 충돌 시 두 객체에 반대 방향의 동일한 크기 힘 적용
ball1.applyImpulse(impulse, 0);
ball2.applyImpulse(-impulse, 0);
```

### 3.2 벡터 연산 유틸리티

```java
/**
 * 2D 벡터 연산을 위한 유틸리티 클래스
 */
public class Vector2D {
    public double x, y;
    
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    // 벡터의 크기 (길이)
    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }
    
    // 정규화 (단위 벡터로 변환)
    public Vector2D normalize() {
        double mag = magnitude();
        if (mag == 0) return new Vector2D(0, 0);
        return new Vector2D(x / mag, y / mag);
    }
    
    // 벡터 덧셈
    public Vector2D add(Vector2D other) {
        return new Vector2D(x + other.x, y + other.y);
    }
    
    // 벡터 뺄셈
    public Vector2D subtract(Vector2D other) {
        return new Vector2D(x - other.x, y - other.y);
    }
    
    // 스칼라 곱
    public Vector2D multiply(double scalar) {
        return new Vector2D(x * scalar, y * scalar);
    }
    
    // 내적 (dot product)
    public double dot(Vector2D other) {
        return x * other.x + y * other.y;
    }
    
    // 거리 계산
    public double distanceTo(Vector2D other) {
        return subtract(other).magnitude();
    }
    
    // 각도 계산 (라디안)
    public double angle() {
        return Math.atan2(y, x);
    }
    
    // 회전
    public Vector2D rotate(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vector2D(
            x * cos - y * sin,
            x * sin + y * cos
        );
    }
}
```

---

## 4. MovableWorld 클래스 - 시간 기반 시뮬레이션

### 4.1 MovableWorld 완전 구현

```java
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * 움직이는 공들을 관리하는 세계 클래스
 * 시간 기반 시뮬레이션과 물리 법칙을 적용합니다.
 */
public class MovableWorld {
    // === 기본 속성 ===
    private double width;
    private double height;
    private List<MovableBall> balls;
    private Color backgroundColor;
    
    // === 물리 설정 ===
    private double gravity;           // 중력 가속도 (픽셀/초²)
    private double airResistance;     // 공기 저항 계수
    private boolean gravityEnabled;   // 중력 적용 여부
    private boolean showVectors;      // 속도 벡터 표시 여부
    
    // === 경계 설정 ===
    private BoundaryBehavior boundaryBehavior;
    private double restitution;       // 경계 반발 계수
    
    // === 통계 정보 ===
    private double totalKineticEnergy;
    private int activeballCount;
    private Random random;
    
    // === 생성자 ===
    
    public MovableWorld(double width, double height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("너비와 높이는 0보다 커야 합니다");
        }
        
        this.width = width;
        this.height = height;
        this.balls = new ArrayList<>();
        this.backgroundColor = Color.LIGHTGRAY;
        
        // 물리 설정 초기화
        this.gravity = 98.0;  // 중력: 약 1G (98 픽셀/초²)
        this.airResistance = 0.02;
        this.gravityEnabled = false;
        this.showVectors = false;
        
        // 경계 설정
        this.boundaryBehavior = BoundaryBehavior.BOUNCE;
        this.restitution = 0.8;  // 80% 에너지 보존
        
        this.random = new Random();
    }
    
    // === 공 관리 메서드들 ===
    
    public void addBall(MovableBall ball) {
        if (ball != null) {
            balls.add(ball);
        }
    }
    
    public boolean removeBall(MovableBall ball) {
        return balls.remove(ball);
    }
    
    public void clearBalls() {
        balls.clear();
    }
    
    /**
     * 랜덤한 움직이는 공을 생성합니다
     */
    public MovableBall createRandomMovableBall() {
        // 크기와 위치
        double radius = 10 + random.nextDouble() * 20;  // 10-30 픽셀
        double x = radius + random.nextDouble() * (width - 2 * radius);
        double y = radius + random.nextDouble() * (height - 2 * radius);
        
        // 초기 속도
        double maxSpeed = 200;
        double dx = (random.nextDouble() - 0.5) * 2 * maxSpeed;  // -200 ~ +200
        double dy = (random.nextDouble() - 0.5) * 2 * maxSpeed;
        
        // 랜덤 색상
        Color color = Color.color(random.nextDouble(), random.nextDouble(), random.nextDouble());
        
        MovableBall ball = new MovableBall(x, y, radius, color, dx, dy);
        addBall(ball);
        return ball;
    }
    
    /**
     * 여러 개의 랜덤 공을 생성합니다
     */
    public void createRandomMovableBalls(int count) {
        for (int i = 0; i < count; i++) {
            createRandomMovableBall();
        }
    }
    
    // === 시뮬레이션 업데이트 ===
    
    /**
     * 세계를 업데이트합니다 (게임 루프의 핵심)
     * @param deltaTime 경과 시간 (초 단위)
     */
    public void update(double deltaTime) {
        if (deltaTime <= 0) return;
        
        // 1단계: 물리 법칙 적용
        applyPhysics(deltaTime);
        
        // 2단계: 공들 이동
        moveAllBalls(deltaTime);
        
        // 3단계: 경계 충돌 처리
        handleBoundaryCollisions();
        
        // 4단계: 공 간 충돌 처리 (간단한 버전)
        handleBallCollisions();
        
        // 5단계: 통계 업데이트
        updateStatistics();
    }
    
    /**
     * 모든 공에 물리 법칙을 적용합니다
     */
    private void applyPhysics(double deltaTime) {
        for (MovableBall ball : balls) {
            if (!ball.isActive()) continue;
            
            // 중력 적용
            if (gravityEnabled) {
                ball.applyGravity(gravity, deltaTime);
            }
            
            // 공기 저항 적용
            if (airResistance > 0) {
                applyAirResistance(ball, deltaTime);
            }
        }
    }
    
    /**
     * 공기 저항을 적용합니다
     */
    private void applyAirResistance(MovableBall ball, double deltaTime) {
        double speed = ball.getSpeed();
        if (speed > 0) {
            // 공기 저항은 속도의 제곱에 비례
            double resistance = airResistance * speed * speed;
            double angle = ball.getDirection();
            
            // 속도 반대 방향으로 저항력 적용
            double forceX = -resistance * Math.cos(angle);
            double forceY = -resistance * Math.sin(angle);
            
            ball.applyForce(forceX, forceY, deltaTime);
        }
    }
    
    /**
     * 모든 공을 이동시킵니다
     */
    private void moveAllBalls(double deltaTime) {
        for (MovableBall ball : balls) {
            ball.move(deltaTime);
        }
    }
    
    /**
     * 경계와의 충돌을 처리합니다
     */
    private void handleBoundaryCollisions() {
        for (MovableBall ball : balls) {
            handleBallBoundaryCollision(ball);
        }
    }
    
    /**
     * 개별 공의 경계 충돌을 처리합니다
     */
    private void handleBallBoundaryCollision(MovableBall ball) {
        double x = ball.getX();
        double y = ball.getY();
        double radius = ball.getRadius();
        
        boolean collided = false;
        
        // 좌우 경계
        if (x - radius <= 0) {
            handleLeftBoundaryCollision(ball);
            collided = true;
        } else if (x + radius >= width) {
            handleRightBoundaryCollision(ball);
            collided = true;
        }
        
        // 상하 경계
        if (y - radius <= 0) {
            handleTopBoundaryCollision(ball);
            collided = true;
        } else if (y + radius >= height) {
            handleBottomBoundaryCollision(ball);
            collided = true;
        }
        
        // 충돌 후 처리
        if (collided) {
            onBoundaryCollision(ball);
        }
    }
    
    private void handleLeftBoundaryCollision(MovableBall ball) {
        ball.setX(ball.getRadius());
        switch (boundaryBehavior) {
            case BOUNCE:
                ball.reflect(1, 0, restitution);  // 오른쪽 방향 법선
                break;
            case WRAP:
                ball.setX(width - ball.getRadius());
                break;
            case STOP:
                ball.setDx(0);
                break;
        }
    }
    
    private void handleRightBoundaryCollision(MovableBall ball) {
        ball.setX(width - ball.getRadius());
        switch (boundaryBehavior) {
            case BOUNCE:
                ball.reflect(-1, 0, restitution);  // 왼쪽 방향 법선
                break;
            case WRAP:
                ball.setX(ball.getRadius());
                break;
            case STOP:
                ball.setDx(0);
                break;
        }
    }
    
    private void handleTopBoundaryCollision(MovableBall ball) {
        ball.setY(ball.getRadius());
        switch (boundaryBehavior) {
            case BOUNCE:
                ball.reflect(0, 1, restitution);  // 아래쪽 방향 법선
                break;
            case WRAP:
                ball.setY(height - ball.getRadius());
                break;
            case STOP:
                ball.setDy(0);
                break;
        }
    }
    
    private void handleBottomBoundaryCollision(MovableBall ball) {
        ball.setY(height - ball.getRadius());
        switch (boundaryBehavior) {
            case BOUNCE:
                ball.reflect(0, -1, restitution);  // 위쪽 방향 법선
                break;
            case WRAP:
                ball.setY(ball.getRadius());
                break;
            case STOP:
                ball.setDy(0);
                break;
        }
    }
    
    /**
     * 경계 충돌 시 호출되는 콜백 메서드
     */
    protected void onBoundaryCollision(MovableBall ball) {
        // 하위 클래스에서 오버라이드 가능
        // 예: 충돌 소리 재생, 파티클 효과 등
    }
    
    /**
     * 공 간 충돌을 처리합니다 (간단한 버전)
     */
    private void handleBallCollisions() {
        for (int i = 0; i < balls.size(); i++) {
            for (int j = i + 1; j < balls.size(); j++) {
                MovableBall ball1 = balls.get(i);
                MovableBall ball2 = balls.get(j);
                
                if (ball1.overlaps(ball2)) {
                    handleBallCollision(ball1, ball2);
                }
            }
        }
    }
    
    /**
     * 두 공 사이의 충돌을 처리합니다
     */
    private void handleBallCollision(MovableBall ball1, MovableBall ball2) {
        // 충돌 벡터 계산
        double dx = ball2.getX() - ball1.getX();
        double dy = ball2.getY() - ball1.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance == 0) return;  // 완전히 겹친 경우 제외
        
        // 정규화된 충돌 벡터
        double normalX = dx / distance;
        double normalY = dy / distance;
        
        // 공들을 분리
        double overlap = (ball1.getRadius() + ball2.getRadius()) - distance;
        if (overlap > 0) {
            double separationX = normalX * overlap / 2;
            double separationY = normalY * overlap / 2;
            
            ball1.setX(ball1.getX() - separationX);
            ball1.setY(ball1.getY() - separationY);
            ball2.setX(ball2.getX() + separationX);
            ball2.setY(ball2.getY() + separationY);
        }
        
        // 탄성 충돌 (간단한 버전)
        // 상대 속도 계산
        double relativeVelX = ball2.getDx() - ball1.getDx();
        double relativeVelY = ball2.getDy() - ball1.getDy();
        
        // 법선 방향 상대 속도
        double normalVelocity = relativeVelX * normalX + relativeVelY * normalY;
        
        // 이미 분리되고 있는 경우 충돌 처리 안 함
        if (normalVelocity > 0) return;
        
        // 반발 계수 적용
        double impulse = -(1 + restitution) * normalVelocity;
        impulse /= (1 / ball1.getMass() + 1 / ball2.getMass());
        
        // 충격량 적용
        ball1.applyImpulse(-impulse * normalX, -impulse * normalY);
        ball2.applyImpulse(impulse * normalX, impulse * normalY);
    }
    
    /**
     * 통계 정보를 업데이트합니다
     */
    private void updateStatistics() {
        totalKineticEnergy = 0;
        activeballCount = 0;
        
        for (MovableBall ball : balls) {
            if (ball.isActive()) {
                activeballCount++;
                
                // 운동 에너지 = (1/2) * m * v²
                double speed = ball.getSpeed();
                totalKineticEnergy += 0.5 * ball.getMass() * speed * speed;
            }
        }
    }
    
    // === 렌더링 ===
    
    /**
     * 세계를 렌더링합니다
     */
    public void render(GraphicsContext gc) {
        // 배경 그리기
        gc.setFill(backgroundColor);
        gc.fillRect(0, 0, width, height);
        
        // 모든 공 그리기
        for (MovableBall ball : balls) {
            ball.paint(gc, showVectors);
        }
        
        // 정보 표시
        drawInfo(gc);
    }
    
    /**
     * 화면에 정보를 표시합니다
     */
    private void drawInfo(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.setFont(javafx.scene.text.Font.font(12));
        
        int line = 1;
        gc.fillText(String.format("공 개수: %d (활성: %d)", balls.size(), activeballCount), 10, line * 15);
        line++;
        
        gc.fillText(String.format("운동 에너지: %.1f", totalKineticEnergy), 10, line * 15);
        line++;
        
        if (gravityEnabled) {
            gc.fillText(String.format("중력: %.1f", gravity), 10, line * 15);
            line++;
        }
        
        gc.fillText(String.format("경계: %s", boundaryBehavior), 10, line * 15);
    }
    
    // === 유틸리티 메서드들 ===
    
    /**
     * 모든 공을 정지시킵니다
     */
    public void stopAllBalls() {
        for (MovableBall ball : balls) {
            ball.stop();
        }
    }
    
    /**
     * 모든 공에 랜덤한 속도를 부여합니다
     */
    public void randomizeAllBallVelocities() {
        double maxSpeed = 300;
        for (MovableBall ball : balls) {
            double dx = (random.nextDouble() - 0.5) * 2 * maxSpeed;
            double dy = (random.nextDouble() - 0.5) * 2 * maxSpeed;
            ball.setVelocity(dx, dy);
        }
    }
    
    /**
     * 특정 지점에서 폭발 효과를 생성합니다
     */
    public void explodeAt(double x, double y, double force) {
        for (MovableBall ball : balls) {
            double dx = ball.getX() - x;
            double dy = ball.getY() - y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            if (distance > 0) {
                double explosionForce = force / (distance * distance);  // 거리 제곱에 반비례
                double forceX = (dx / distance) * explosionForce;
                double forceY = (dy / distance) * explosionForce;
                
                ball.applyImpulse(forceX, forceY);
            }
        }
    }
    
    // === Getter/Setter 메서드들 ===
    
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public int getBallCount() { return balls.size(); }
    public List<MovableBall> getBalls() { return new ArrayList<>(balls); }
    
    public double getGravity() { return gravity; }
    public void setGravity(double gravity) { this.gravity = gravity; }
    
    public boolean isGravityEnabled() { return gravityEnabled; }
    public void setGravityEnabled(boolean enabled) { this.gravityEnabled = enabled; }
    
    public boolean isShowVectors() { return showVectors; }
    public void setShowVectors(boolean show) { this.showVectors = show; }
    
    public BoundaryBehavior getBoundaryBehavior() { return boundaryBehavior; }
    public void setBoundaryBehavior(BoundaryBehavior behavior) { this.boundaryBehavior = behavior; }
    
    public double getRestitution() { return restitution; }
    public void setRestitution(double restitution) {
        this.restitution = Math.max(0, Math.min(1, restitution));  // 0-1 범위로 제한
    }
    
    public double getTotalKineticEnergy() { return totalKineticEnergy; }
    public int getActiveBallCount() { return activeballCount; }
}

/**
 * 경계 충돌 시 행동을 정의하는 열거형
 */
enum BoundaryBehavior {
    BOUNCE,    // 반사
    WRAP,      // 화면 반대편으로 이동
    STOP       // 정지
}
```

---

## 5. 애니메이션 루프와 게임 루프

### 5.1 JavaFX AnimationTimer 활용

```java
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * MovableWorld를 위한 JavaFX 애플리케이션
 */
public class MovableWorldApp extends Application {
    private static final double CANVAS_WIDTH = 800;
    private static final double CANVAS_HEIGHT = 600;
    
    private MovableWorld world;
    private Canvas canvas;
    private GraphicsContext gc;
    private AnimationTimer gameLoop;
    
    // FPS 계산용
    private long lastFrameTime = 0;
    private double fps = 0;
    
    @Override
    public void start(Stage primaryStage) {
        // UI 설정
        setupUI(primaryStage);
        
        // 세계 초기화
        initializeWorld();
        
        // 게임 루프 시작
        startGameLoop();
        
        primaryStage.show();
    }
    
    private void setupUI(Stage primaryStage) {
        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        
        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        
        Scene scene = new Scene(root);
        
        // 키보드 이벤트 처리
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case G:
                    world.setGravityEnabled(!world.isGravityEnabled());
                    break;
                case V:
                    world.setShowVectors(!world.isShowVectors());
                    break;
                case R:
                    world.randomizeAllBallVelocities();
                    break;
                case S:
                    world.stopAllBalls();
                    break;
                case SPACE:
                    world.createRandomMovableBall();
                    break;
                case DIGIT1:
                    world.setBoundaryBehavior(BoundaryBehavior.BOUNCE);
                    break;
                case DIGIT2:
                    world.setBoundaryBehavior(BoundaryBehavior.WRAP);
                    break;
                case DIGIT3:
                    world.setBoundaryBehavior(BoundaryBehavior.STOP);
                    break;
            }
        });
        
        // 마우스 클릭으로 폭발 효과
        scene.setOnMouseClicked(event -> {
            world.explodeAt(event.getX(), event.getY(), 10000);
        });
        
        primaryStage.setTitle("Movable World - G:중력, V:벡터, R:랜덤속도, S:정지, Space:공추가");
        primaryStage.setScene(scene);
        canvas.requestFocus();
    }
    
    private void initializeWorld() {
        world = new MovableWorld(CANVAS_WIDTH, CANVAS_HEIGHT);
        world.setGravityEnabled(true);
        world.setBoundaryBehavior(BoundaryBehavior.BOUNCE);
        world.setRestitution(0.8);
        
        // 초기 공들 생성
        world.createRandomMovableBalls(5);
    }
    
    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long currentTime) {
                // deltaTime 계산 (나노초 → 초)
                double deltaTime = 0;
                if (lastFrameTime != 0) {
                    deltaTime = (currentTime - lastFrameTime) / 1_000_000_000.0;
                    
                    // 최대 deltaTime 제한 (게임이 너무 오래 정지되었을 때 대비)
                    deltaTime = Math.min(deltaTime, 1.0 / 30.0);  // 최대 30FPS 상당
                }
                lastFrameTime = currentTime;
                
                // FPS 계산
                if (deltaTime > 0) {
                    fps = 1.0 / deltaTime;
                }
                
                // 게임 로직 업데이트
                world.update(deltaTime);
                
                // 렌더링
                world.render(gc);
                
                // FPS 표시
                drawFPS(gc);
            }
        };
        
        gameLoop.start();
    }
    
    private void drawFPS(GraphicsContext gc) {
        gc.setFill(Color.RED);
        gc.setFont(javafx.scene.text.Font.font(12));
        gc.fillText(String.format("FPS: %.1f", fps), CANVAS_WIDTH - 80, 15);
    }
    
    @Override
    public void stop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
```

### 5.2 게임 루프 패턴 설명

#### 표준 게임 루프 구조
```java
public void gameLoop(double deltaTime) {
    // 1. 입력 처리 (Input)
    processInput();
    
    // 2. 게임 상태 업데이트 (Update)
    updateGameState(deltaTime);
    
    // 3. 물리 시뮬레이션 (Physics)
    updatePhysics(deltaTime);
    
    // 4. 렌더링 (Render)
    render();
    
    // 5. 정리 작업 (Cleanup)
    cleanup();
}
```

#### deltaTime의 중요성
```java
// 나쁜 예: 프레임 종속적
position += 5;  // 60FPS에서는 초당 300픽셀, 30FPS에서는 초당 150픽셀

// 좋은 예: 시간 기반
position += velocity * deltaTime;  // 항상 초당 velocity 픽셀
```

---

## 6. 테스트 코드와 검증

### 6.1 MovableBall 테스트

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class MovableBallTest {
    private MovableBall ball;
    
    @BeforeEach
    void setUp() {
        ball = new MovableBall(100, 100, 20, Color.RED, 50, 30);
    }
    
    @Test
    @DisplayName("기본 이동 테스트")
    void testBasicMovement() {
        double originalX = ball.getX();
        double originalY = ball.getY();
        
        ball.move(1.0);  // 1초 이동
        
        assertEquals(originalX + 50, ball.getX(), 0.001);
        assertEquals(originalY + 30, ball.getY(), 0.001);
    }
    
    @Test
    @DisplayName("시간 기반 이동 테스트")
    void testTimeBasedMovement() {
        double originalX = ball.getX();
        
        // 0.5초 이동
        ball.move(0.5);
        assertEquals(originalX + 25, ball.getX(), 0.001);
        
        // 추가로 0.5초 이동
        ball.move(0.5);
        assertEquals(originalX + 50, ball.getX(), 0.001);
    }
    
    @Test
    @DisplayName("속도 설정 테스트")
    void testVelocitySetting() {
        ball.setVelocity(100, 200);
        
        assertEquals(100, ball.getDx(), 0.001);
        assertEquals(200, ball.getDy(), 0.001);
    }
    
    @Test
    @DisplayName("속력과 방향 계산 테스트")
    void testSpeedAndDirection() {
        ball.setVelocity(30, 40);  // 3-4-5 직각삼각형
        
        assertEquals(50, ball.getSpeed(), 0.001);
        assertEquals(Math.atan2(40, 30), ball.getDirection(), 0.001);
    }
    
    @Test
    @DisplayName("힘 적용 테스트")
    void testForceApplication() {
        ball.setVelocity(0, 0);  // 정지 상태
        ball.setMass(10);
        
        ball.applyForce(100, 0, 1.0);  // 1초간 100N의 힘
        
        // F = ma → a = F/m = 100/10 = 10
        // v = v₀ + at = 0 + 10*1 = 10
        assertEquals(10, ball.getDx(), 0.001);
        assertEquals(0, ball.getDy(), 0.001);
    }
    
    @Test
    @DisplayName("중력 적용 테스트")
    void testGravity() {
        ball.setVelocity(0, 0);
        
        ball.applyGravity(98, 1.0);  // 1초간 중력 적용
        
        assertEquals(0, ball.getDx(), 0.001);
        assertEquals(98, ball.getDy(), 0.001);
    }
    
    @Test
    @DisplayName("충격량 적용 테스트")
    void testImpulse() {
        ball.setVelocity(10, 20);
        ball.setMass(5);
        
        ball.applyImpulse(25, 0);  // J = 25
        
        // Δv = J/m = 25/5 = 5
        assertEquals(15, ball.getDx(), 0.001);  // 10 + 5
        assertEquals(20, ball.getDy(), 0.001);  // 변화 없음
    }
    
    @Test
    @DisplayName("반사 테스트")
    void testReflection() {
        ball.setVelocity(10, 5);
        
        // 수직 벽에 반사 (법선 벡터: (-1, 0))
        ball.reflect(-1, 0, 1.0);  // 완전 탄성
        
        assertEquals(-10, ball.getDx(), 0.001);  // X 속도 반전
        assertEquals(5, ball.getDy(), 0.001);    // Y 속도 유지
    }
    
    @Test
    @DisplayName("최대 속력 제한 테스트")
    void testMaxSpeedLimit() {
        ball.setMaxSpeed(100);
        ball.setVelocity(200, 0);  // 최대 속력 초과
        
        ball.move(0.001);  // 작은 시간으로 업데이트
        
        assertTrue(ball.getSpeed() <= 100.1);  // 약간의 오차 허용
    }
    
    @Test
    @DisplayName("마찰 적용 테스트")
    void testFriction() {
        ball.setFriction(0.9);  // 90% 속도 유지
        ball.setVelocity(100, 0);
        
        ball.move(1.0);  // 1초 이동
        
        assertTrue(ball.getSpeed() < 100);  // 속력 감소
    }
    
    @Test
    @DisplayName("특정 지점으로 이동 테스트")
    void testMoveTowards() {
        ball.setX(0);
        ball.setY(0);
        
        ball.moveTowards(30, 40, 50);  // (30, 40)으로 속력 50으로 이동
        
        assertEquals(30, ball.getDx(), 0.001);  // 3:4:5 비율
        assertEquals(40, ball.getDy(), 0.001);
        assertEquals(50, ball.getSpeed(), 0.001);
    }
}
```

### 6.2 MovableWorld 테스트

```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MovableWorldTest {
    private MovableWorld world;
    
    @BeforeEach
    void setUp() {
        world = new MovableWorld(800, 600);
    }
    
    @Test
    @DisplayName("세계 생성 테스트")
    void testWorldCreation() {
        assertEquals(800, world.getWidth(), 0.001);
        assertEquals(600, world.getHeight(), 0.001);
        assertEquals(0, world.getBallCount());
        assertFalse(world.isGravityEnabled());
    }
    
    @Test
    @DisplayName("공 추가 및 업데이트 테스트")
    void testBallAddAndUpdate() {
        MovableBall ball = new MovableBall(100, 100, 20, 50, 30);
        world.addBall(ball);
        
        assertEquals(1, world.getBallCount());
        
        // 1초 업데이트
        world.update(1.0);
        
        assertEquals(150, ball.getX(), 0.001);
        assertEquals(130, ball.getY(), 0.001);
    }
    
    @Test
    @DisplayName("중력 적용 테스트")
    void testGravity() {
        MovableBall ball = new MovableBall(100, 100, 20, 0, 0);
        world.addBall(ball);
        world.setGravityEnabled(true);
        world.setGravity(98);
        
        world.update(1.0);
        
        assertEquals(0, ball.getDx(), 0.001);
        assertEquals(98, ball.getDy(), 0.001);
    }
    
    @Test
    @DisplayName("경계 반사 테스트")
    void testBoundaryBounce() {
        MovableBall ball = new MovableBall(10, 300, 15, -50, 0);
        world.addBall(ball);
        world.setBoundaryBehavior(BoundaryBehavior.BOUNCE);
        
        world.update(1.0);  // 1초 후 좌측 벽과 충돌
        
        assertTrue(ball.getDx() > 0);  // X 속도가 양수로 변경됨
        assertEquals(15, ball.getX(), 0.001);  // 경계에 맞춰 위치 조정
    }
    
    @Test
    @DisplayName("경계 랩어라운드 테스트")
    void testBoundaryWrap() {
        MovableBall ball = new MovableBall(10, 300, 15, -50, 0);
        world.addBall(ball);
        world.setBoundaryBehavior(BoundaryBehavior.WRAP);
        
        world.update(1.0);  // 1초 후 좌측 벽 통과
        
        assertEquals(world.getWidth() - 15, ball.getX(), 0.001);  // 우측으로 이동
    }
    
    @Test
    @DisplayName("공 간 충돌 테스트")
    void testBallCollision() {
        MovableBall ball1 = new MovableBall(100, 100, 20, 50, 0);
        MovableBall ball2 = new MovableBall(130, 100, 20, -50, 0);  // 마주 보며 접근
        
        world.addBall(ball1);
        world.addBall(ball2);
        
        // 충돌하도록 위치 조정
        ball1.setX(110);
        ball2.setX(120);
        
        world.update(0.1);
        
        // 충돌 후 속도 변화 확인
        assertTrue(ball1.getDx() < 50);   // 속도 감소
        assertTrue(ball2.getDx() > -50);  // 속도 증가 (음수에서 덜 음수로)
    }
    
    @Test
    @DisplayName("폭발 효과 테스트")
    void testExplosion() {
        MovableBall ball = new MovableBall(100, 100, 20, 0, 0);
        world.addBall(ball);
        
        world.explodeAt(50, 100, 1000);  // 왼쪽에서 폭발
        
        assertTrue(ball.getDx() > 0);  // 오른쪽으로 밀려남
    }
    
    @Test
    @DisplayName("통계 업데이트 테스트")
    void testStatistics() {
        MovableBall ball1 = new MovableBall(100, 100, 20, 30, 40);  // 속력 50
        MovableBall ball2 = new MovableBall(200, 200, 15, 0, 0);    // 정지
        
        world.addBall(ball1);
        world.addBall(ball2);
        
        world.update(0.1);
        
        assertEquals(2, world.getBallCount());
        assertTrue(world.getTotalKineticEnergy() > 0);
    }
    
    @Test
    @DisplayName("모든 공 정지 테스트")
    void testStopAllBalls() {
        world.createRandomMovableBalls(5);
        world.stopAllBalls();
        
        for (MovableBall ball : world.getBalls()) {
            assertEquals(0, ball.getDx(), 0.001);
            assertEquals(0, ball.getDy(), 0.001);
        }
    }
}
```

---

## 7. 일반적인 실수와 해결법

### 7.1 시간 기반 애니메이션 실수

#### ❌ 잘못된 예제
```java
// 프레임 기반 이동 - FPS에 따라 속도 변함
public void update() {
    x += dx;  // 프레임마다 dx만큼 이동 - 나쁨!
    y += dy;
}
```

#### ✅ 올바른 예제
```java
// 시간 기반 이동 - FPS 무관한 일정한 속도
public void update(double deltaTime) {
    x += dx * deltaTime;  // 초당 dx 픽셀 이동 - 좋음!
    y += dy * deltaTime;
}
```

### 7.2 물리 법칙 적용 실수

#### ❌ 잘못된 힘 적용
```java
// 매 프레임마다 동일한 힘 적용 - 가속도가 프레임에 의존
public void applyForce(double force) {
    double acceleration = force / mass;
    velocity += acceleration;  // deltaTime 없음!
}
```

#### ✅ 올바른 힘 적용
```java
// 시간을 고려한 힘 적용
public void applyForce(double force, double deltaTime) {
    double acceleration = force / mass;
    velocity += acceleration * deltaTime;  // 시간 고려!
}
```

### 7.3 경계 충돌 처리 실수

#### ❌ 잘못된 경계 처리
```java
// 경계를 관통한 후 되돌리기 - 떨림 현상 발생
if (x < 0) {
    x = 0;  // 위치만 수정, 속도는 그대로
}
```

#### ✅ 올바른 경계 처리
```java
// 경계에 정확히 맞추고 속도도 반사
if (x - radius < 0) {
    x = radius;        // 경계에 맞춤
    dx = -dx * restitution;  // 속도 반사
}
```

### 7.4 deltaTime 처리 실수

#### ❌ 잘못된 deltaTime 사용
```java
// deltaTime을 검증하지 않음
public void update(double deltaTime) {
    x += dx * deltaTime;  // deltaTime이 음수이거나 매우 클 수 있음!
}
```

#### ✅ 올바른 deltaTime 사용
```java
// deltaTime 검증 및 제한
public void update(double deltaTime) {
    if (deltaTime <= 0) return;
    
    // 최대 deltaTime 제한 (긴 정지 후 대비)
    deltaTime = Math.min(deltaTime, 1.0 / 30.0);
    
    x += dx * deltaTime;
}
```

### 7.5 부동소수점 정밀도 실수

#### ❌ 정확한 비교
```java
// 부동소수점 직접 비교 - 부정확할 수 있음
if (velocity == 0) {
    // 이 조건은 거의 만족되지 않을 수 있음
}
```

#### ✅ 임계값을 이용한 비교
```java
// 임계값을 이용한 비교
private static final double EPSILON = 0.001;

if (Math.abs(velocity) < EPSILON) {
    velocity = 0;  // 매우 작은 속도는 0으로 처리
}
```

### 7.6 성능 관련 실수

#### ❌ 불필요한 계산
```java
// 매 프레임마다 제곱근 계산
public void update() {
    double speed = Math.sqrt(dx * dx + dy * dy);  // 비싼 연산!
    if (speed > maxSpeed) {
        // 정규화...
    }
}
```

#### ✅ 최적화된 계산
```java
// 제곱근 계산 회피
public void update() {
    double speedSquared = dx * dx + dy * dy;
    double maxSpeedSquared = maxSpeed * maxSpeed;
    
    if (speedSquared > maxSpeedSquared) {
        double speed = Math.sqrt(speedSquared);  // 필요할 때만 계산
        double ratio = maxSpeed / speed;
        dx *= ratio;
        dy *= ratio;
    }
}
```

### 7.7 실수 방지 체크리스트

- [ ] **시간 기반**: 모든 이동이 deltaTime을 사용하는가?
- [ ] **deltaTime 검증**: 0 이하 또는 너무 큰 값을 체크하는가?
- [ ] **물리 법칙**: 힘과 가속도 적용이 올바른가?
- [ ] **경계 처리**: 위치와 속도를 모두 올바르게 처리하는가?
- [ ] **부동소수점**: 정확한 비교 대신 임계값을 사용하는가?
- [ ] **성능**: 불필요한 계산을 피하고 있는가?
- [ ] **범위 검사**: 속도나 힘이 합리적인 범위에 있는가?

---

## 학습 포인트 정리

### 3장에서 배운 핵심 개념들

1. **시간 기반 애니메이션**
   - deltaTime의 중요성과 사용법
   - 프레임 독립적인 움직임 구현
   - FPS에 관계없는 일관된 속도

2. **기본 물리 시뮬레이션**
   - 뉴턴의 운동 법칙 적용
   - 힘, 가속도, 속도, 위치의 관계
   - 충격량과 운동량 보존

3. **벡터 수학**
   - 2D 벡터의 기본 연산
   - 속도 벡터와 방향 계산
   - 정규화와 반사 계산

4. **게임 루프 패턴**
   - 입력-업데이트-렌더링 사이클
   - AnimationTimer 활용법
   - 성능 최적화 기법

5. **경계 처리**
   - 다양한 경계 행동 (반사, 랩어라운드, 정지)
   - 정확한 충돌 검사와 응답
   - 반발 계수와 에너지 손실

이제 4장에서는 이 움직이는 공들이 경계와 상호작용하는 방법을 더 자세히 배워보겠습니다!