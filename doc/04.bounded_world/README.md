# 4장: 경계가 있는 월드 (Bounded World)

## 학습 목표

이 장을 완료하면 다음을 할 수 있습니다:
- 경계 충돌 감지를 구현할 수 있습니다
- 물리적으로 정확한 반사를 구현할 수 있습니다
- 공 간의 충돌을 처리할 수 있습니다
- 탄성 및 비탄성 충돌을 시뮬레이션할 수 있습니다
- 상속을 통해 BoundedBall을 구현할 수 있습니다

## 핵심 개념

### 4.1 BoundedBall - 경계 처리 추가

**BoundedBall 클래스 설계**

MovableBall을 상속받아 경계 충돌 처리를 추가합니다:

**추가 필드:**
- `minX`, `minY`: 최소 경계 (공의 중심 기준)
- `maxX`, `maxY`: 최대 경계 (공의 중심 기준)

**메서드:**
- 생성자: 기본 경계를 800×600으로 설정
- `setBounds(double minX, double minY, double maxX, double maxY)`:
  - 경계 설정 시 반지름 고려
  - 공의 중심이 이동할 수 있는 범위 계산
- `move(double deltaTime)` 오버라이드:
  - 다음 위치 계산
  - 경계 충돌 검사
  - 충돌 시 속도 반전
  - 위치 보정 (경계 안쪽으로)

**구현 힌트:**
```java
// 경계 설정 시 공의 중심이 이동 가능한 범위
this.minX = minX + getRadius();
this.maxX = maxX - getRadius();

// 충돌 검사와 처리
if (nextX <= minX || nextX >= maxX) {
    // 1. 속도 반전
    // 2. 위치 보정
}
```

### 4.2 개선된 충돌 감지와 반사

**CollisionDetector 클래스 설계**

벽과의 충돌을 감지하고 처리하는 유틸리티 클래스입니다:

**내부 클래스 - WallCollision:**
- `Wall` enum: LEFT, RIGHT, TOP, BOTTOM, NONE
- `wall`: 충돌한 벽
- `penetration`: 벽 침투 깊이

**정적 메서드:**
- `checkWallCollision(BoundedBall ball, double minX, double minY, double maxX, double maxY)`:
  - 각 벽과의 충돌 검사
  - 충돌한 벽과 침투 깊이 반환
  - 충돌이 없으면 NONE 반환

- `resolveWallCollision(BoundedBall ball, WallCollision collision, double restitution)`:
  - 충돌한 벽에 따라 속도 반전
  - 반발 계수(restitution) 적용
  - LEFT/RIGHT: x 속도 반전
  - TOP/BOTTOM: y 속도 반전

**반발 계수(Coefficient of Restitution):**
- 0.0: 완전 비탄성 충돌 (끝적이는 효과)
- 1.0: 완전 탄성 충돌 (에너지 손실 없음)
- 0.8: 일반적인 공의 반발

**구현 힌트:**
```java
// 침투 깊이 계산
// 왼쪽 벽: minX - (ballX - radius)
// 오른쪽 벽: (ballX + radius) - maxX

// 속도 반전 시 반발 계수 적용
ball.setDx(-ball.getDx() * restitution);
```

### 4.3 공 간의 충돌

**BallCollision 클래스 설계**

두 공 사이의 충돌을 감지하고 처리하는 클래스입니다:

**정적 메서드:**
- `areColliding(Ball ball1, Ball ball2)`:
  - 두 공의 중심 거리 계산
  - 거리 < 두 반지름의 합이면 충돌
  
- `resolveElasticCollision(MovableBall ball1, MovableBall ball2)`:
  - 탄성 충돌 처리 (운동량 보존)
  - 충돌 방향 계산
  - 상대 속도 계산
  - 충격량 계산 및 속도 업데이트
  - 겹침 해결

- `separateBalls(Ball ball1, Ball ball2)` (private):
  - 겹친 공을 분리
  - 각 공을 겹침의 절반만큼 밀어냄

**물리 원리:**
1. **운동량 보존**: m₁v₁ + m₂v₂ = m₁v₁' + m₂v₂'
2. **충격량**: I = Δp = mΔv
3. **탄성 충돌**: 에너지 보존

**구현 힌트:**
```java
// 충돌 감지
거리 = √((x₂-x₁)² + (y₂-y₁)²)
충돌 조건: 거리 < r₁ + r₂

// 충돌 방향 벡터 (정규화)
n = (ball2 - ball1) / distance

// 충격량 계산
impulse = 2 * 상대속도 / (질량합)

// 멀어지고 있는지 확인
if (상대속도 · 충돌방향 <= 0) return;
```

### 4.4 BoundedWorld - 충돌 처리 통합

**BoundedWorld 클래스 설계**

World를 상속받아 충돌 처리 기능을 통합한 클래스입니다:

**추가 필드:**
- `restitution`: 반발 계수 (기본값 0.8)

**메서드:**
- `update(double deltaTime)`: 매 프레임 업데이트
  1. 모든 공 이동
  2. 벽과의 충돌 검사 및 처리
  3. 공 간의 충돌 검사 및 처리

- `setRestitution(double restitution)`: 반발 계수 설정
- `getRestitution()`: 반발 계수 반환

**구현 순서:**
1. **이동 단계**: 모든 MovableBall의 move() 호출
2. **벽 충돌 단계**: 
   - BoundedBall인지 확인
   - CollisionDetector.checkWallCollision() 호출
   - 충돌 시 resolveWallCollision() 호출
3. **공 충돌 단계**:
   - 이중 루프로 모든 쌍 검사 (i < j)
   - areColliding() 호출
   - 두 공이 모두 MovableBall이면 resolveElasticCollision()

**최적화 힌트:**
```java
// 이중 충돌 방지
for (int i = 0; i < balls.size(); i++) {
    for (int j = i + 1; j < balls.size(); j++) {
        // 각 쌍을 한 번만 검사
    }
}
```

## 실습 과제

### Lab 4-1: BoundedBall 구현
`BoundedBall` 클래스를 구현하고 테스트하세요:
- MovableBall을 상속
- 경계 충돌 감지
- 반사 구현
- 위치 보정

**테스트 코드:**
```java
@Test
public void testWallBounce() {
    BoundedBall ball = new BoundedBall(50, 300, 20);
    ball.setBounds(0, 0, 800, 600);
    ball.setDx(-100); // 왼쪽으로 이동
    
    // 충돌 전
    assertTrue(ball.getDx() < 0);
    
    // 충분히 이동시켜 충돌 발생
    for (int i = 0; i < 10; i++) {
        ball.move(0.1);
    }
    
    // 충돌 후 방향 반전
    assertTrue(ball.getDx() > 0);
}
```

### Lab 4-2: 반발 계수 구현
다양한 반발 계수로 실험:
- 완전 탄성 충돌 (restitution = 1.0)
- 비탄성 충돌 (restitution < 1.0)
- 에너지 손실 시각화

### Lab 4-3: 충돌 시뮬레이션
여러 공의 충돌 시뮬레이션:
- 10개의 공을 랜덤 위치에 생성
- 다양한 크기와 속도
- 충돌 효과 시각화 (색상 변화, 사운드 등)

### Lab 4-4: 고급 충돌 처리
특수한 충돌 상황 처리:
- 코너 충돌
- 동시 다중 충돌
- 고속 충돌 (터널링 방지)

**AdvancedCollisionApp 구현 가이드**

고급 충돌 시뮬레이션 앱을 만드세요:

**필요한 기능:**
1. BoundedWorld 사용
2. UI 컨트롤:
   - 중력 활성화 체크박스
   - 반발 계수 슬라이더 (0.0 ~ 1.0)
3. 다양한 크기의 공 생성
4. AnimationTimer로 게임 루프 구현

**추가 메서드:**
- `createBalls()`: 랜덤한 크기와 속도의 공 생성
- `applyGravity()`: 모든 공에 중력 적용 (dy 증가)
- `render()`: Canvas에 그리기

**중력 구현 힌트:**
```java
// 중력 가속도 (예: 500 pixels/s²)
private static final double GRAVITY = 500;

// 모든 공에 적용
for (Ball ball : world.getBalls()) {
    if (ball instanceof MovableBall) {
        MovableBall movable = (MovableBall) ball;
        movable.setDy(movable.getDy() + GRAVITY * deltaTime);
    }
}
```

## JUnit 테스트 예제

```java
public class CollisionTest {
    
    @Test
    public void testBallCollisionDetection() {
        Ball ball1 = new Ball(100, 100, 30);
        Ball ball2 = new Ball(150, 100, 30);
        
        assertTrue(BallCollision.areColliding(ball1, ball2));
        
        ball2.setX(200);
        assertFalse(BallCollision.areColliding(ball1, ball2));
    }
    
    @Test
    public void testElasticCollision() {
        MovableBall ball1 = new MovableBall(100, 100, 20);
        ball1.setDx(100);
        ball1.setDy(0);
        
        MovableBall ball2 = new MovableBall(140, 100, 20);
        ball2.setDx(-100);
        ball2.setDy(0);
        
        // 충돌 전 총 운동량
        double totalMomentumBefore = 
            ball1.getRadius() * ball1.getDx() + 
            ball2.getRadius() * ball2.getDx();
        
        BallCollision.resolveElasticCollision(ball1, ball2);
        
        // 충돌 후 총 운동량
        double totalMomentumAfter = 
            ball1.getRadius() * ball1.getDx() + 
            ball2.getRadius() * ball2.getDx();
        
        // 운동량 보존 확인
        assertEquals(totalMomentumBefore, totalMomentumAfter, 0.001);
    }
    
    @Test
    public void testWallCollisionDetection() {
        BoundedBall ball = new BoundedBall(50, 300, 20);
        
        WallCollision collision = CollisionDetector.checkWallCollision(
            ball, 0, 0, 800, 600
        );
        
        assertEquals(WallCollision.Wall.NONE, collision.wall);
        
        ball.setX(15); // 왼쪽 벽에 닿음
        collision = CollisionDetector.checkWallCollision(
            ball, 0, 0, 800, 600
        );
        
        assertEquals(WallCollision.Wall.LEFT, collision.wall);
        assertEquals(5, collision.penetration, 0.001);
    }
}
```

## 자가 평가 문제

1. **반발 계수(coefficient of restitution)란?**
   - 충돌 후 상대 속도와 충돌 전 상대 속도의 비율
   - 0: 완전 비탄성, 1: 완전 탄성

2. **운동량 보존 법칙이란?**
   - 외력이 없을 때 총 운동량은 보존됨
   - m1v1 + m2v2 = m1v1' + m2v2'

3. **터널링(tunneling) 문제란?**
   - 빠른 속도로 움직이는 물체가 벽을 통과하는 현상
   - 연속 충돌 감지로 해결

4. **충돌 응답의 두 단계는?**
   - 충돌 감지 (Detection)
   - 충돌 해결 (Resolution)

## 자주 하는 실수와 해결 방법

### 1. 충돌 후 물체가 붙어있음
```java
// 잘못된 코드 - 겹침을 해결하지 않음
if (areColliding(ball1, ball2)) {
    resolveCollision(ball1, ball2);
}

// 올바른 코드 - 겹침 해결 포함
if (areColliding(ball1, ball2)) {
    resolveCollision(ball1, ball2);
    separateBalls(ball1, ball2); // 추가!
}
```

### 2. 이중 충돌 처리
```java
// 잘못된 코드 - 같은 충돌을 여러 번 처리
for (Ball ball : balls) {
    checkCollisions(ball, balls);
}

// 올바른 코드 - 각 쌍을 한 번만 검사
for (int i = 0; i < balls.size(); i++) {
    for (int j = i + 1; j < balls.size(); j++) {
        checkCollision(balls.get(i), balls.get(j));
    }
}
```

### 3. 부정확한 반사
```java
// 잘못된 코드 - 단순 반전
ball.setDx(-ball.getDx());

// 올바른 코드 - 반발 계수 적용
ball.setDx(-ball.getDx() * restitution);
```

## 구현 검증용 테스트 코드

아래 테스트 코드를 사용하여 구현한 클래스들이 올바르게 작동하는지 확인하세요:

### Bounds 추상 클래스와 하위 클래스 테스트

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BoundsTest {
    
    @Test
    public void testCircleBounds() {
        CircleBounds circle = new CircleBounds(100, 100, 50);
        
        assertEquals(100, circle.getCenterX(), 0.001, "원 중심 X 좌표가 잘못되었습니다");
        assertEquals(100, circle.getCenterY(), 0.001, "원 중심 Y 좌표가 잘못되었습니다");
        assertEquals(50, circle.getRadius(), 0.001, "원 반지름이 잘못되었습니다");
        
        // 경계 박스 확인
        assertEquals(50, circle.getMinX(), 0.001, "원 최소 X가 잘못되었습니다");
        assertEquals(150, circle.getMaxX(), 0.001, "원 최대 X가 잘못되었습니다");
        assertEquals(50, circle.getMinY(), 0.001, "원 최소 Y가 잘못되었습니다");
        assertEquals(150, circle.getMaxY(), 0.001, "원 최대 Y가 잘못되었습니다");
    }
    
    @Test
    public void testRectangleBounds() {
        RectangleBounds rect = new RectangleBounds(50, 75, 100, 80);
        
        assertEquals(50, rect.getX(), 0.001, "사각형 X 좌표가 잘못되었습니다");
        assertEquals(75, rect.getY(), 0.001, "사각형 Y 좌표가 잘못되었습니다");
        assertEquals(100, rect.getWidth(), 0.001, "사각형 너비가 잘못되었습니다");
        assertEquals(80, rect.getHeight(), 0.001, "사각형 높이가 잘못되었습니다");
        
        // 경계 박스 확인
        assertEquals(50, rect.getMinX(), 0.001, "사각형 최소 X가 잘못되었습니다");
        assertEquals(150, rect.getMaxX(), 0.001, "사각형 최대 X가 잘못되었습니다");
        assertEquals(75, rect.getMinY(), 0.001, "사각형 최소 Y가 잘못되었습니다");
        assertEquals(155, rect.getMaxY(), 0.001, "사각형 최대 Y가 잘못되었습니다");
    }
    
    @Test
    public void testCircleCircleIntersection() {
        CircleBounds circle1 = new CircleBounds(100, 100, 30);
        CircleBounds circle2 = new CircleBounds(140, 100, 25);
        
        // 교차하는 경우
        assertTrue(circle1.intersects(circle2), "교차하는 원들이 교차하지 않는다고 판단되었습니다");
        
        // 교차하지 않는 경우
        CircleBounds circle3 = new CircleBounds(200, 100, 20);
        assertFalse(circle1.intersects(circle3), "교차하지 않는 원들이 교차한다고 판단되었습니다");
        
        // 경계선에서 접촉
        CircleBounds circle4 = new CircleBounds(155, 100, 25); // 거리 = 55, 반지름 합 = 55
        assertTrue(circle1.intersects(circle4), "접촉하는 원들이 교차하지 않는다고 판단되었습니다");
    }
    
    @Test
    public void testRectangleRectangleIntersection() {
        RectangleBounds rect1 = new RectangleBounds(50, 50, 100, 80);
        RectangleBounds rect2 = new RectangleBounds(100, 75, 80, 60);
        
        // 교차하는 경우
        assertTrue(rect1.intersects(rect2), "교차하는 사각형들이 교차하지 않는다고 판단되었습니다");
        
        // 교차하지 않는 경우
        RectangleBounds rect3 = new RectangleBounds(200, 200, 50, 50);
        assertFalse(rect1.intersects(rect3), "교차하지 않는 사각형들이 교차한다고 판단되었습니다");
        
        // 경계선에서 접촉
        RectangleBounds rect4 = new RectangleBounds(150, 50, 50, 50); // 딱 맞닿음
        assertTrue(rect1.intersects(rect4), "접촉하는 사각형들이 교차하지 않는다고 판단되었습니다");
    }
    
    @Test
    public void testCircleRectangleIntersection() {
        CircleBounds circle = new CircleBounds(100, 100, 25);
        RectangleBounds rect = new RectangleBounds(75, 90, 60, 40);
        
        // 교차하는 경우
        assertTrue(circle.intersects(rect), "교차하는 원과 사각형이 교차하지 않는다고 판단되었습니다");
        assertTrue(rect.intersects(circle), "상호 교차 검사 결과가 일치하지 않습니다");
        
        // 원이 사각형 내부에 있는 경우
        CircleBounds innerCircle = new CircleBounds(100, 110, 10);
        assertTrue(innerCircle.intersects(rect), "사각형 내부의 원이 교차하지 않는다고 판단되었습니다");
        
        // 교차하지 않는 경우
        CircleBounds farCircle = new CircleBounds(200, 200, 20);
        assertFalse(farCircle.intersects(rect), "교차하지 않는 원과 사각형이 교차한다고 판단되었습니다");
    }
    
    @Test
    public void testContains() {
        CircleBounds circle = new CircleBounds(100, 100, 50);
        
        // 원 내부 점
        assertTrue(circle.contains(100, 100), "원 중심점이 포함되지 않았습니다");
        assertTrue(circle.contains(120, 120), "원 내부 점이 포함되지 않았습니다");
        
        // 원 외부 점
        assertFalse(circle.contains(200, 200), "원 외부 점이 포함되었습니다");
        
        RectangleBounds rect = new RectangleBounds(50, 50, 100, 80);
        
        // 사각형 내부 점
        assertTrue(rect.contains(100, 90), "사각형 내부 점이 포함되지 않았습니다");
        
        // 사각형 외부 점
        assertFalse(rect.contains(200, 200), "사각형 외부 점이 포함되었습니다");
    }
}
```

### BoundedBall 클래스 테스트

```java
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BoundedBallTest {
    
    private BoundedBall ball;
    private BoundedWorld world;
    
    @BeforeEach
    public void setUp() {
        ball = new BoundedBall(100, 100, 20, Color.RED);
        world = new BoundedWorld(800, 600);
        world.addBall(ball);
    }
    
    @Test
    public void testBoundedBallCreation() {
        // 부모 클래스 속성 확인
        assertEquals(100, ball.getX(), 0.001, "X 좌표가 올바르게 설정되지 않았습니다");
        assertEquals(100, ball.getY(), 0.001, "Y 좌표가 올바르게 설정되지 않았습니다");
        assertEquals(20, ball.getRadius(), 0.001, "반지름이 올바르게 설정되지 않았습니다");
        assertEquals(Color.RED, ball.getColor(), "색상이 올바르게 설정되지 않았습니다");
        
        // 기본 반발 계수 확인
        assertEquals(0.8, ball.getRestitution(), 0.001, "기본 반발 계수가 잘못되었습니다");
    }
    
    @Test
    public void testRestitutionSetterGetter() {
        ball.setRestitution(0.9);
        assertEquals(0.9, ball.getRestitution(), 0.001, "반발 계수 설정이 올바르지 않습니다");
        
        // 유효하지 않은 반발 계수
        assertThrows(IllegalArgumentException.class, () -> {
            ball.setRestitution(-0.1); // 음수
        }, "음수 반발 계수에 대해 예외가 발생하지 않았습니다");
        
        assertThrows(IllegalArgumentException.class, () -> {
            ball.setRestitution(1.1); // 1보다 큰 값
        }, "1보다 큰 반발 계수에 대해 예외가 발생하지 않았습니다");
    }
    
    @Test
    public void testGetBounds() {
        Bounds bounds = ball.getBounds();
        assertTrue(bounds instanceof CircleBounds, "getBounds()는 CircleBounds를 반환해야 합니다");
        
        CircleBounds circleBounds = (CircleBounds) bounds;
        assertEquals(100, circleBounds.getCenterX(), 0.001, "Bounds 중심 X가 잘못되었습니다");
        assertEquals(100, circleBounds.getCenterY(), 0.001, "Bounds 중심 Y가 잘못되었습니다");
        assertEquals(20, circleBounds.getRadius(), 0.001, "Bounds 반지름이 잘못되었습니다");
    }
    
    @Test
    public void testWallCollisionLeft() {
        ball.setX(10); // 왼쪽 벽 근처
        ball.setDx(-50); // 왼쪽으로 이동
        
        double initialDx = ball.getDx();
        ball.handleWallCollision(WallCollision.Wall.LEFT);
        
        assertTrue(ball.getDx() > 0, "왼쪽 벽 충돌 후 X 속도가 양수가 되어야 합니다");
        assertEquals(Math.abs(initialDx) * ball.getRestitution(), ball.getDx(), 0.001, 
                    "반발 계수가 올바르게 적용되지 않았습니다");
    }
    
    @Test
    public void testWallCollisionRight() {
        ball.setX(780); // 오른쪽 벽 근처 (800 - 20)
        ball.setDx(50); // 오른쪽으로 이동
        
        double initialDx = ball.getDx();
        ball.handleWallCollision(WallCollision.Wall.RIGHT);
        
        assertTrue(ball.getDx() < 0, "오른쪽 벽 충돌 후 X 속도가 음수가 되어야 합니다");
        assertEquals(-Math.abs(initialDx) * ball.getRestitution(), ball.getDx(), 0.001,
                    "반발 계수가 올바르게 적용되지 않았습니다");
    }
    
    @Test
    public void testWallCollisionTop() {
        ball.setY(10); // 위쪽 벽 근처
        ball.setDy(-50); // 위쪽으로 이동
        
        double initialDy = ball.getDy();
        ball.handleWallCollision(WallCollision.Wall.TOP);
        
        assertTrue(ball.getDy() > 0, "위쪽 벽 충돌 후 Y 속도가 양수가 되어야 합니다");
        assertEquals(Math.abs(initialDy) * ball.getRestitution(), ball.getDy(), 0.001,
                    "반발 계수가 올바르게 적용되지 않았습니다");
    }
    
    @Test
    public void testWallCollisionBottom() {
        ball.setY(580); // 아래쪽 벽 근처 (600 - 20)
        ball.setDy(50); // 아래쪽으로 이동
        
        double initialDy = ball.getDy();
        ball.handleWallCollision(WallCollision.Wall.BOTTOM);
        
        assertTrue(ball.getDy() < 0, "아래쪽 벽 충돌 후 Y 속도가 음수가 되어야 합니다");
        assertEquals(-Math.abs(initialDy) * ball.getRestitution(), ball.getDy(), 0.001,
                    "반발 계수가 올바르게 적용되지 않았습니다");
    }
    
    @Test
    public void testInheritance() {
        // BoundedBall이 MovableBall을 상속받는지 확인
        assertTrue(ball instanceof MovableBall, "BoundedBall은 MovableBall을 상속받아야 합니다");
        assertTrue(ball instanceof PaintableBall, "BoundedBall은 PaintableBall을 상속받아야 합니다");
        assertTrue(ball instanceof Ball, "BoundedBall은 Ball을 상속받아야 합니다");
        
        // 부모 클래스의 메서드 사용 가능한지 확인
        ball.setDx(100);
        ball.setDy(75);
        assertEquals(100, ball.getDx(), 0.001, "상속받은 속도 설정이 작동하지 않습니다");
        assertEquals(75, ball.getDy(), 0.001, "상속받은 속도 설정이 작동하지 않습니다");
        
        ball.move(0.1);
        assertEquals(110, ball.getX(), 0.001, "상속받은 move 메서드가 작동하지 않습니다");
        assertEquals(107.5, ball.getY(), 0.001, "상속받은 move 메서드가 작동하지 않습니다");
    }
}
```

### CollisionDetector 클래스 테스트

```java
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CollisionDetectorTest {
    
    @Test
    public void testBallBallCollisionDetection() {
        BoundedBall ball1 = new BoundedBall(100, 100, 20, Color.RED);
        BoundedBall ball2 = new BoundedBall(130, 100, 15, Color.BLUE);
        
        // 충돌하는 경우 (거리: 30, 반지름 합: 35)
        assertTrue(CollisionDetector.areColliding(ball1, ball2), 
                  "충돌하는 공들이 충돌하지 않는다고 판단되었습니다");
        
        // 충돌하지 않는 경우
        ball2.setX(200);
        assertFalse(CollisionDetector.areColliding(ball1, ball2),
                   "충돌하지 않는 공들이 충돌한다고 판단되었습니다");
        
        // 경계선에서 접촉
        ball2.setX(135); // 거리: 35, 반지름 합: 35
        assertTrue(CollisionDetector.areColliding(ball1, ball2),
                  "접촉하는 공들이 충돌하지 않는다고 판단되었습니다");
    }
    
    @Test
    public void testWallCollisionDetection() {
        BoundedBall ball = new BoundedBall(15, 100, 20, Color.RED);
        
        // 왼쪽 벽 충돌 (x - radius < 0)
        WallCollision collision = CollisionDetector.checkWallCollision(ball, 0, 0, 800, 600);
        assertNotNull(collision, "벽 충돌이 감지되지 않았습니다");
        assertEquals(WallCollision.Wall.LEFT, collision.getWall(), "잘못된 벽이 감지되었습니다");
        assertEquals(5, collision.getPenetration(), 0.001, "침투 깊이가 잘못 계산되었습니다");
        
        // 오른쪽 벽 충돌
        ball.setX(785); // x + radius > 800
        collision = CollisionDetector.checkWallCollision(ball, 0, 0, 800, 600);
        assertEquals(WallCollision.Wall.RIGHT, collision.getWall(), "오른쪽 벽 충돌이 감지되지 않았습니다");
        assertEquals(5, collision.getPenetration(), 0.001, "오른쪽 벽 침투 깊이가 잘못되었습니다");
        
        // 위쪽 벽 충돌
        ball.setX(100);
        ball.setY(15); // y - radius < 0
        collision = CollisionDetector.checkWallCollision(ball, 0, 0, 800, 600);
        assertEquals(WallCollision.Wall.TOP, collision.getWall(), "위쪽 벽 충돌이 감지되지 않았습니다");
        
        // 아래쪽 벽 충돌
        ball.setY(585); // y + radius > 600
        collision = CollisionDetector.checkWallCollision(ball, 0, 0, 800, 600);
        assertEquals(WallCollision.Wall.BOTTOM, collision.getWall(), "아래쪽 벽 충돌이 감지되지 않았습니다");
        
        // 충돌하지 않는 경우
        ball.setX(400);
        ball.setY(300);
        collision = CollisionDetector.checkWallCollision(ball, 0, 0, 800, 600);
        assertNull(collision, "벽과 충돌하지 않는데 충돌이 감지되었습니다");
    }
    
    @Test
    public void testCollisionResponse() {
        BoundedBall ball1 = new BoundedBall(100, 100, 20, Color.RED);
        BoundedBall ball2 = new BoundedBall(130, 100, 20, Color.BLUE);
        
        // 초기 속도 설정
        ball1.setDx(50);
        ball1.setDy(0);
        ball2.setDx(-30);
        ball2.setDy(0);
        
        // 충돌 전 운동량 계산 (질량이 같다고 가정)
        double totalMomentumX = ball1.getDx() + ball2.getDx();
        
        CollisionDetector.resolveCollision(ball1, ball2);
        
        // 충돌 후 운동량 보존 확인
        double newTotalMomentumX = ball1.getDx() + ball2.getDx();
        assertEquals(totalMomentumX, newTotalMomentumX, 0.01, 
                    "운동량이 보존되지 않았습니다");
        
        // 공들이 서로 멀어지는 방향으로 움직이는지 확인
        assertTrue(ball1.getDx() < 50, "첫 번째 공의 속도가 감소해야 합니다");
        assertTrue(ball2.getDx() > -30, "두 번째 공의 속도가 증가해야 합니다");
    }
    
    @Test
    public void testBallSeparation() {
        BoundedBall ball1 = new BoundedBall(100, 100, 20, Color.RED);
        BoundedBall ball2 = new BoundedBall(125, 100, 20, Color.BLUE);
        
        // 겹친 상태 (거리: 25, 반지름 합: 40, 겹침: 15)
        assertTrue(CollisionDetector.areColliding(ball1, ball2), "공들이 겹쳐있어야 합니다");
        
        CollisionDetector.separateBalls(ball1, ball2);
        
        // 분리 후 더 이상 겹치지 않아야 함
        assertFalse(CollisionDetector.areColliding(ball1, ball2), 
                   "분리 후에도 공들이 겹쳐있습니다");
        
        // 공들 사이의 거리가 반지름 합과 같거나 커야 함
        double distance = Math.sqrt(Math.pow(ball2.getX() - ball1.getX(), 2) + 
                                   Math.pow(ball2.getY() - ball1.getY(), 2));
        assertTrue(distance >= 40, "분리 후 거리가 반지름 합보다 작습니다");
    }
}
```

### BoundedWorld 클래스 테스트

```java
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;

public class BoundedWorldTest {
    
    private BoundedWorld world;
    
    @BeforeEach
    public void setUp() {
        world = new BoundedWorld(800, 600);
    }
    
    @Test
    public void testBoundedWorldCreation() {
        assertEquals(800, world.getWidth(), "World 너비가 올바르지 않습니다");
        assertEquals(600, world.getHeight(), "World 높이가 올바르지 않습니다");
    }
    
    @Test
    public void testWallCollisionHandling() {
        BoundedBall ball = new BoundedBall(10, 300, 20, Color.RED);
        ball.setDx(-50); // 왼쪽으로 이동 (벽 충돌 예정)
        world.addBall(ball);
        
        world.update(0.1);
        
        // 벽 충돌 후 속도가 반대 방향으로 바뀌어야 함
        assertTrue(ball.getDx() > 0, "벽 충돌 후 X 속도가 양수가 되어야 합니다");
        
        // 공이 벽 안쪽에 위치해야 함
        assertTrue(ball.getX() >= ball.getRadius(), "공이 왼쪽 벽을 벗어났습니다");
    }
    
    @Test
    public void testBallBallCollisionHandling() {
        BoundedBall ball1 = new BoundedBall(100, 300, 20, Color.RED);
        BoundedBall ball2 = new BoundedBall(200, 300, 20, Color.BLUE);
        
        ball1.setDx(100); // 오른쪽으로 이동
        ball2.setDx(-50); // 왼쪽으로 이동 (충돌 예정)
        
        world.addBall(ball1);
        world.addBall(ball2);
        
        // 충돌 전 총 운동량
        double initialMomentum = ball1.getDx() + ball2.getDx();
        
        world.update(0.5); // 충분한 시간으로 충돌 발생
        
        // 충돌 후 운동량 보존 확인
        double finalMomentum = ball1.getDx() + ball2.getDx();
        assertEquals(initialMomentum, finalMomentum, 1.0, "운동량이 보존되지 않았습니다");
        
        // 공들이 분리되어야 함
        assertFalse(CollisionDetector.areColliding(ball1, ball2), 
                   "충돌 처리 후에도 공들이 겹쳐있습니다");
    }
    
    @Test
    public void testMultipleBallCollisions() {
        // 여러 공이 한 번에 충돌하는 상황
        BoundedBall ball1 = new BoundedBall(100, 300, 15, Color.RED);
        BoundedBall ball2 = new BoundedBall(200, 300, 15, Color.BLUE);
        BoundedBall ball3 = new BoundedBall(300, 300, 15, Color.GREEN);
        
        ball1.setDx(150);
        ball2.setDx(0);
        ball3.setDx(-100);
        
        world.addBall(ball1);
        world.addBall(ball2);
        world.addBall(ball3);
        
        // 여러 번 업데이트하여 모든 충돌 처리
        for (int i = 0; i < 10; i++) {
            world.update(0.01);
        }
        
        // 모든 공이 분리되어야 함
        assertFalse(CollisionDetector.areColliding(ball1, ball2), "공 1과 2가 겹쳐있습니다");
        assertFalse(CollisionDetector.areColliding(ball2, ball3), "공 2와 3이 겹쳐있습니다");
        assertFalse(CollisionDetector.areColliding(ball1, ball3), "공 1과 3이 겹쳐있습니다");
    }
    
    @Test
    public void testCornerBounce() {
        // 모서리에서의 반사 테스트
        BoundedBall ball = new BoundedBall(25, 25, 20, Color.YELLOW);
        ball.setDx(-50);
        ball.setDy(-50);
        world.addBall(ball);
        
        world.update(0.1);
        
        // 모서리 충돌 후 두 방향 모두 반사되어야 함
        assertTrue(ball.getDx() > 0, "모서리 충돌 후 X 속도가 양수가 되어야 합니다");
        assertTrue(ball.getDy() > 0, "모서리 충돌 후 Y 속도가 양수가 되어야 합니다");
    }
    
    @Test
    public void testInheritance() {
        // BoundedWorld가 MovableWorld를 상속받는지 확인
        assertTrue(world instanceof MovableWorld, "BoundedWorld는 MovableWorld를 상속받아야 합니다");
        assertTrue(world instanceof World, "BoundedWorld는 World를 상속받아야 합니다");
        
        // 부모 클래스 메서드 사용 가능한지 확인
        BoundedBall ball = new BoundedBall(100, 100, 20, Color.CYAN);
        world.addBall(ball);
        assertEquals(1, world.getBallCount(), "상속받은 addBall이 작동하지 않습니다");
    }
    
    @Test
    public void testRender() {
        GraphicsContext gc = Mockito.mock(GraphicsContext.class);
        BoundedBall ball = new BoundedBall(100, 100, 20, Color.MAGENTA);
        world.addBall(ball);
        
        assertDoesNotThrow(() -> {
            world.render(gc);
        }, "BoundedWorld 렌더링 중 예외가 발생했습니다");
    }
}
```

### WallCollision 클래스 테스트

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WallCollisionTest {
    
    @Test
    public void testWallCollisionCreation() {
        WallCollision collision = new WallCollision(WallCollision.Wall.LEFT, 5.0);
        
        assertEquals(WallCollision.Wall.LEFT, collision.getWall(), "벽 종류가 올바르지 않습니다");
        assertEquals(5.0, collision.getPenetration(), 0.001, "침투 깊이가 올바르지 않습니다");
    }
    
    @Test
    public void testWallEnum() {
        // 모든 벽 종류가 존재하는지 확인
        WallCollision.Wall[] walls = WallCollision.Wall.values();
        assertEquals(4, walls.length, "벽의 종류가 4개가 아닙니다");
        
        // 각 벽 종류 확인
        boolean hasLeft = false, hasRight = false, hasTop = false, hasBottom = false;
        for (WallCollision.Wall wall : walls) {
            switch (wall) {
                case LEFT: hasLeft = true; break;
                case RIGHT: hasRight = true; break;
                case TOP: hasTop = true; break;
                case BOTTOM: hasBottom = true; break;
            }
        }
        
        assertTrue(hasLeft && hasRight && hasTop && hasBottom, 
                  "모든 벽 종류(LEFT, RIGHT, TOP, BOTTOM)가 정의되지 않았습니다");
    }
    
    @Test
    public void testInvalidPenetration() {
        // 음수 침투 깊이는 허용하지 않아야 함
        assertThrows(IllegalArgumentException.class, () -> {
            new WallCollision(WallCollision.Wall.LEFT, -1.0);
        }, "음수 침투 깊이에 대해 예외가 발생하지 않았습니다");
    }
}
```

## 다음 장 미리보기

5장에서는 추상 데이터 타입을 다룹니다:
- Bounds 추상 클래스
- Vector 클래스 확장
- 코드 재사용성 향상
- 디자인 패턴 적용

## 추가 학습 자료

- [2D 충돌 감지와 응답](https://www.metanetsoftware.com/technique/tutorialA.html)
- [게임 물리 엔진 기초](https://brm.io/game-physics-for-beginners/)
- [Real-Time Collision Detection](https://realtimecollisiondetection.net/)

## 학습 체크포인트

- [ ] BoundedBall 클래스를 구현했습니다
- [ ] 벽과의 충돌을 처리할 수 있습니다
- [ ] 공 간의 충돌을 감지하고 해결할 수 있습니다
- [ ] 반발 계수를 이해하고 적용했습니다
- [ ] 운동량 보존 법칙을 구현했습니다