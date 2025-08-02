# 7장: 단순한 월드 (Simple World) - 인터페이스의 도입

## 학습 목표

이 장을 완료하면 다음을 할 수 있습니다:
- 인터페이스를 설계하고 구현할 수 있습니다
- 다중 인터페이스를 통해 유연한 객체를 만들 수 있습니다
- 다형성을 활용하여 코드를 단순화할 수 있습니다
- 상속과 인터페이스를 적절히 조합할 수 있습니다
- Box를 경계로 사용하고 충돌 액션을 구현할 수 있습니다

## 핵심 개념

### 7.1 인터페이스란?

**인터페이스(Interface)의 이해**

인터페이스는 클래스가 구현해야 할 메서드들의 집합입니다:
- **계약(Contract)**: 클래스가 무엇을 할 수 있는지 약속
- **다중 구현**: 한 클래스가 여러 인터페이스 구현 가능
- **순수 추상**: 메서드 선언만 있고 구현은 없음 (Java 8 이전)

**인터페이스의 장점:**
1. **유연성**: 다양한 클래스가 같은 기능 구현
2. **다형성**: 인터페이스 타입으로 여러 구현체 사용
3. **결합도 감소**: 구현과 사용의 분리

### 7.2 인터페이스 설계

**Paintable 인터페이스 설계**

화면에 그릴 수 있는 객체를 위한 인터페이스:

```java
public interface Paintable {
    void paint(GraphicsContext gc);
}
```

**Movable 인터페이스 설계**

움직일 수 있는 객체를 위한 인터페이스:

**메서드:**
- `move(double deltaTime)`: 시간 기반 이동
- `getDx()`, `getDy()`: 속도 조회
- `setDx(double dx)`, `setDy(double dy)`: 속도 설정

**Boundable 인터페이스 설계**

경계를 가진 객체를 위한 인터페이스:

**메서드:**
- `getBounds()`: Bounds 객체 반환
- `isColliding(Boundable other)`: 충돌 여부 확인

**Collidable 인터페이스 설계**

충돌 처리가 가능한 객체를 위한 인터페이스:

```java
// Boundable을 확장
public interface Collidable extends Boundable {
    // 추가 메서드
}
```

**메서드:**
- `handleCollision(Collidable other)`: 충돌 처리
- `getCollisionAction()`: 충돌 액션 반환

**CollisionAction 열거형:**
- BOUNCE: 반사
- DESTROY: 파괴
- STOP: 정지
- PASS: 통과
- CUSTOM: 사용자 정의

### 7.3 Ball 클래스 - 인터페이스 구현

**Ball 클래스 설계**

여러 인터페이스를 구현하는 유연한 Ball 클래스:

```java
public class Ball implements Paintable, Movable, Collidable {
    // 클래스 구현
}
```

**필드:**
- `x`, `y`, `radius`: 위치와 크기
- `dx`, `dy`: 속도
- `color`: 색상
- `collisionAction`: 충돌 시 행동

**구현 요구사항:**
1. **Paintable 구현**:
   - `paint()`: 원 그리기
   
2. **Movable 구현**:
   - `move()`: 위치 업데이트
   - 속도 getter/setter
   
3. **Collidable 구현**:
   - `getBounds()`: CircleBounds 반환
   - `isColliding()`: 교차 검사
   - `handleCollision()`: 액션에 따른 처리

**구현 힌트:**
```java
// 충돌 처리 switch 문
switch (collisionAction) {
    case BOUNCE:
        // 반사 로직
        break;
    case DESTROY:
        // 제거 표시
        break;
    // ...
}
```

### 7.4 Box 클래스 - 다양한 역할

**Box 클래스 설계**

경계나 장애물로 사용되는 Box 클래스:

**필드:**
- `x`, `y`, `width`, `height`: 위치와 크기
- `color`: 색상
- `collisionAction`: 충돌 액션

**구현 요구사항:**
1. **Paintable 구현**:
   - 사각형 그리기
   - 테두리 그리기
   
2. **Collidable 구현**:
   - RectangleBounds 반환
   - 충돌한 객체에 영향

**MovableBox 클래스 설계**

Box를 상속받아 Movable 인터페이스 추가:

```java
public class MovableBox extends Box implements Movable {
    // 추가 구현
}
```

### 7.5 SimpleWorld - 인터페이스 활용

**SimpleWorld 클래스 설계**

인터페이스를 활용한 단순화된 World:

**필드:**
- `width`, `height`: 세계 크기
- `gameObjects`: Object 리스트 (다양한 타입 저장)
- `boundaries`: 경계 Box들

**핵심 메서드:**
1. **createBoundaries()**:
   - 4개의 경계 Box 생성
   - 화면 밖에 위치
   - BOUNCE 액션 설정

2. **update(double deltaTime)**:
   - instanceof로 타입 확인
   - Movable 객체들 이동
   - Collidable 객체들 충돌 검사
   - 경계와의 충돌 처리
   - 객체 간 충돌 처리

3. **render(GraphicsContext gc)**:
   - 배경 그리기
   - Paintable 객체들만 그리기

**구현 힌트:**
```java
// 타입 확인과 캐스팅
if (obj instanceof Movable) {
    ((Movable) obj).move(deltaTime);
}

// 이중 루프로 충돌 검사
for (int i = 0; i < size; i++) {
    for (int j = i + 1; j < size; j++) {
        // 각 쌍을 한 번만 검사
    }
}
```

## 실습 과제

### Lab 7-1: 인터페이스 구현
삼각형과 별 모양 객체 구현:

**Triangle 클래스 설계**
- Paintable, Movable, Collidable 구현
- 3개의 꼭짓점으로 삼각형 정의
- `paint()`: 다각형 그리기
- `getBounds()`: 삼각형을 포함하는 사각형

**Star 클래스 설계**
- 같은 인터페이스들 구현
- 5각 별 모양
- 회전 기능 추가 가능

### Lab 7-2: 복합 객체
특수한 행동을 하는 객체들:

**BouncingTriangle 클래스 설계**

Triangle을 상속받아 특수 기능 추가:

**추가 필드:**
- `rotationSpeed`: 회전 속도
- `colorChangeSpeed`: 색상 변화 속도

**특수 기능:**
- `handleCollision()` 오버라이드:
  - 반사 시 회전 속도 반전
  - 색상을 랜덤하게 변경
- `move()` 오버라이드:
  - 위치 업데이트 + 회전 업데이트

**ExplodingBall 클래스 설계**

Ball을 상속받아 특수 기능 추가:

**추가 필드:**
- `hasExploded`: 폭발 여부
- `miniballCount`: 생성할 작은 공 개수 (3-5개)

**특수 기능:**
- `handleCollision()` 오버라이드:
  - CUSTOM 액션일 때 폭발
  - `explode()` 메서드 호출
- `explode()` 메서드:
  - 여러 개의 작은 Ball 생성
  - 랜덤 방향으로 발사
  - 원본 공은 제거 표시

**구현 힌트:**
```java
// 작은 공 생성
각도 = 0 ~ 2*PI 랜덤
속도 = 50 ~ 150 랜덤
dx = 속도 * cos(각도)
dy = 속도 * sin(각도)
```

### Lab 7-3: 동적 액션 변경
런타임에 충돌 액션을 변경하는 시스템:

**ActionController 클래스 설계**

**필드:**
- `selectedObject`: 현재 선택된 객체
- `actionChangeTimer`: 자동 변경 타이머
- `collisionCounts`: 각 객체의 충돌 횟수 기록

**기능 요구사항:**

1. **객체 선택**:
   - 마우스 클릭으로 객체 선택
   - 선택된 객체 하이라이트

2. **키보드 액션 변경**:
   ```java
   // 키 매핑
   B 키: BOUNCE
   D 키: DESTROY
   S 키: STOP
   P 키: PASS
   C 키: CUSTOM
   ```

3. **시간 기반 자동 변경**:
   - 10초마다 모든 객체의 액션 순환
   - BOUNCE → STOP → PASS → BOUNCE

4. **충돌 횟수 기반 진화**:
   - 5회 충돌: BOUNCE → STOP
   - 10회 충돌: STOP → DESTROY
   - 15회 충돌: 새로운 기능 해금

**구현 힌트:**
```java
// Scene에 이벤트 핸들러 등록
setOnKeyPressed()
setOnMouseClicked()

// instanceof로 타입 확인 후 캐스팅
if (obj instanceof Collidable) {
    ((Collidable) obj).setCollisionAction(newAction);
}
```

### Lab 7-4: 고급 경계 시스템
Box를 이용한 복잡한 레벨 디자인:

**MazeWorld 클래스 설계**

**미로 구성:**
```java
// 2차원 배열로 미로 정의
// 1 = 벽, 0 = 통로, 2 = 출구
int[][] maze = {
    {1,1,1,1,1},
    {1,0,0,0,1},
    {1,0,1,0,1},
    {1,0,0,2,1},
    {1,1,1,1,1}
};
```

**SpecialZone 클래스 설계**

Box를 상속받아 특수 효과 추가:

**ZoneType 열거형:**
- SPEED_UP: 속도 2배
- SLOW_DOWN: 속도 0.5배
- GRAVITY: 아래로 당김 (dy += 10)
- ANTI_GRAVITY: 위로 밂 (dy -= 10)
- TELEPORT: 다른 위치로 순간이동

**메서드 요구사항:**
- `applyEffect(Movable obj)`: 효과 적용
- `paint()` 오버라이드: 특수 시각 효과
  - SPEED_UP: 빨간색 반투명
  - SLOW_DOWN: 파란색 반투명
  - GRAVITY: 아래 화살표 표시
  - ANTI_GRAVITY: 위 화살표 표시

**구현 힌트:**
```java
// 효과 적용 로직
if (zone.isColliding(movableObject)) {
    zone.applyEffect(movableObject);
}

// 반투명 효과
gc.setGlobalAlpha(0.3);
gc.setFill(effectColor);
gc.fillRect(x, y, width, height);
gc.setGlobalAlpha(1.0);
```

## JUnit 테스트 예제

```java
public class InterfaceTest {
    
    @Test
    public void testMultipleInterfaces() {
        Ball ball = new Ball(100, 100, 20);
        
        // Ball은 여러 인터페이스를 구현
        assertTrue(ball instanceof Paintable);
        assertTrue(ball instanceof Movable);
        assertTrue(ball instanceof Collidable);
    }
    
    @Test
    public void testPolymorphism() {
        List<Paintable> paintables = new ArrayList<>();
        paintables.add(new Ball(0, 0, 10));
        paintables.add(new Box(0, 0, 20, 20));
        
        // 모든 Paintable을 동일하게 처리
        GraphicsContext gc = mock(GraphicsContext.class);
        for (Paintable p : paintables) {
            p.paint(gc);
        }
    }
    
    @Test
    public void testCollisionActions() {
        Ball ball = new Ball(50, 50, 10);
        ball.setDx(100);
        ball.setCollisionAction(CollisionAction.BOUNCE);
        
        Box wall = new Box(55, 0, 10, 100);
        
        assertTrue(ball.isColliding(wall));
        
        double oldDx = ball.getDx();
        ball.handleCollision(wall);
        
        // 반사 후 방향이 바뀜
        assertEquals(-oldDx, ball.getDx(), 0.001);
    }
}
```


## 자가 평가 문제

1. **인터페이스의 장점은?**
   - 다중 구현 가능
   - 유연한 타입 시스템
   - 구현과 선언의 분리

2. **상속 vs 인터페이스?**
   - 상속: is-a 관계
   - 인터페이스: can-do 관계

3. **다형성의 이점은?**
   - 코드 재사용
   - 확장성
   - 유지보수성

4. **CollisionAction의 목적은?**
   - 충돌 행동 캡슐화
   - 런타임 변경 가능
   - 코드 중복 제거

## 자주 하는 실수와 해결 방법

### 1. 인터페이스 분리 원칙 위반
```java
// 나쁜 예 - 너무 많은 메서드
public interface GameObject {
    void move();
    void paint();
    void collide();
    void playSound();
    void saveState();
    // ...
}

// 좋은 예 - 작은 인터페이스들
public interface Movable { void move(); }
public interface Paintable { void paint(); }
public interface Audible { void playSound(); }
```

### 2. 구현 누락
```java
// 컴파일 에러 - paint() 메서드 구현 누락
public class MyShape implements Paintable {
    // paint() 메서드가 없음!
}
```

### 3. 타입 캐스팅 없이 사용
```java
// 잘못된 코드
Object obj = new Ball(0, 0, 10);
obj.move(1.0); // 컴파일 에러!

// 올바른 코드
if (obj instanceof Movable) {
    ((Movable) obj).move(1.0);
}
```

## 다음 장 미리보기

8장에서는 벽돌 깨기 게임을 구현합니다:
- 게임 메커니즘 구현
- 점수 시스템
- 파워업
- 레벨 디자인

## 추가 학습 자료

- [Java Interfaces](https://docs.oracle.com/javase/tutorial/java/concepts/interface.html)
- [SOLID Interface Segregation Principle](https://en.wikipedia.org/wiki/Interface_segregation_principle)
- [Design Patterns - Strategy Pattern](https://refactoring.guru/design-patterns/strategy)

## 구현 검증용 테스트 코드

아래 테스트 코드를 사용하여 구현한 인터페이스와 클래스들이 올바르게 작동하는지 확인하고, 인터페이스가 6장의 문제점들을 어떻게 해결하는지 경험해보세요:

### 인터페이스 설계 테스트

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InterfaceDesignTest {
    
    @Test
    public void testPaintableInterface() {
        // Paintable 인터페이스 구현 확인
        Ball ball = new Ball(100, 100, 20);
        Box box = new Box(200, 200, 50, 40);
        Triangle triangle = new Triangle(300, 300, 30);
        
        // 모든 객체가 Paintable을 구현하는지 확인
        assertTrue(ball instanceof Paintable, "Ball이 Paintable을 구현하지 않았습니다");
        assertTrue(box instanceof Paintable, "Box가 Paintable을 구현하지 않았습니다");
        assertTrue(triangle instanceof Paintable, "Triangle이 Paintable을 구현하지 않았습니다");
        
        // paint 메서드 호출 가능한지 확인 (예외 발생하지 않아야 함)
        GraphicsContext gc = Mockito.mock(GraphicsContext.class);
        assertDoesNotThrow(() -> ball.paint(gc), "Ball.paint() 호출 시 예외 발생");
        assertDoesNotThrow(() -> box.paint(gc), "Box.paint() 호출 시 예외 발생");
        assertDoesNotThrow(() -> triangle.paint(gc), "Triangle.paint() 호출 시 예외 발생");
    }
    
    @Test
    public void testMovableInterface() {
        Ball ball = new Ball(100, 100, 20);
        MovableBox movableBox = new MovableBox(200, 200, 50, 40);
        Triangle triangle = new Triangle(300, 300, 30);
        
        // Movable 인터페이스 구현 확인
        assertTrue(ball instanceof Movable, "Ball이 Movable을 구현하지 않았습니다");
        assertTrue(movableBox instanceof Movable, "MovableBox가 Movable을 구현하지 않았습니다");
        assertTrue(triangle instanceof Movable, "Triangle이 Movable을 구현하지 않았습니다");
        
        // Movable 메서드들 작동 확인
        ball.setDx(50);
        ball.setDy(30);
        assertEquals(50, ball.getDx(), 0.001, "Ball getDx() 작동하지 않음");
        assertEquals(30, ball.getDy(), 0.001, "Ball getDy() 작동하지 않음");
        
        // move 메서드 호출 확인
        double oldX = ball.getX();
        double oldY = ball.getY();
        ball.move(1.0);
        assertNotEquals(oldX, ball.getX(), "Ball이 이동하지 않았습니다");
        assertNotEquals(oldY, ball.getY(), "Ball이 이동하지 않았습니다");
    }
    
    @Test
    public void testCollidableInterface() {
        Ball ball = new Ball(100, 100, 20);
        Box box = new Box(150, 150, 40, 30);
        
        // Collidable 인터페이스 구현 확인
        assertTrue(ball instanceof Collidable, "Ball이 Collidable을 구현하지 않았습니다");
        assertTrue(box instanceof Collidable, "Box가 Collidable을 구현하지 않았습니다");
        
        // getBounds 메서드 확인
        Bounds ballBounds = ball.getBounds();
        Bounds boxBounds = box.getBounds();
        assertNotNull(ballBounds, "Ball getBounds()가 null을 반환했습니다");
        assertNotNull(boxBounds, "Box getBounds()가 null을 반환했습니다");
        
        // isColliding 메서드 확인
        assertTrue(ball.isColliding(box), "충돌하는 객체들이 충돌하지 않는다고 판단되었습니다");
        
        // handleCollision 메서드 호출 확인 (예외 발생하지 않아야 함)
        assertDoesNotThrow(() -> ball.handleCollision(box), "handleCollision 호출 시 예외 발생");
    }
    
    @Test
    public void testBoundableInterface() {
        Ball ball = new Ball(100, 100, 20);
        Box box = new Box(200, 200, 50, 40);
        
        // Boundable은 Collidable의 부모 인터페이스
        assertTrue(ball instanceof Boundable, "Ball이 Boundable을 구현하지 않았습니다");
        assertTrue(box instanceof Boundable, "Box가 Boundable을 구현하지 않았습니다");
        
        // getBounds 메서드 확인
        Bounds ballBounds = ball.getBounds();
        assertTrue(ballBounds instanceof CircleBounds, "Ball은 CircleBounds를 반환해야 합니다");
        
        Bounds boxBounds = box.getBounds();
        assertTrue(boxBounds instanceof RectangleBounds, "Box는 RectangleBounds를 반환해야 합니다");
    }
}
```

### 다중 인터페이스 구현 테스트

```java
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MultipleInterfaceTest {
    
    @Test
    public void testBallMultipleInterfaces() {
        Ball ball = new Ball(100, 100, 20, Color.RED);
        
        // Ball이 모든 필요한 인터페이스를 구현하는지 확인
        assertTrue(ball instanceof Paintable, "Ball이 Paintable을 구현하지 않았습니다");
        assertTrue(ball instanceof Movable, "Ball이 Movable을 구현하지 않았습니다");
        assertTrue(ball instanceof Collidable, "Ball이 Collidable을 구현하지 않았습니다");
        assertTrue(ball instanceof Boundable, "Ball이 Boundable을 구현하지 않았습니다");
        
        // 모든 인터페이스의 메서드들이 작동하는지 확인
        GraphicsContext gc = Mockito.mock(GraphicsContext.class);
        ball.paint(gc); // Paintable
        
        ball.setDx(50);
        ball.setDy(30);
        ball.move(1.0); // Movable
        
        Bounds bounds = ball.getBounds(); // Boundable
        assertNotNull(bounds, "getBounds()가 null을 반환했습니다");
        
        Box wall = new Box(ball.getX() + 15, ball.getY(), 20, 20);
        if (ball.isColliding(wall)) {
            ball.handleCollision(wall); // Collidable
        }
    }
    
    @Test
    public void testBoxMultipleInterfaces() {
        Box box = new Box(100, 100, 50, 40, Color.BLUE);
        
        // Box의 인터페이스 구현 확인
        assertTrue(box instanceof Paintable, "Box가 Paintable을 구현하지 않았습니다");
        assertTrue(box instanceof Collidable, "Box가 Collidable을 구현하지 않았습니다");
        assertTrue(box instanceof Boundable, "Box가 Boundable을 구현하지 않았습니다");
        
        // Box는 기본적으로 Movable을 구현하지 않음
        assertFalse(box instanceof Movable, "일반 Box는 Movable을 구현하면 안됩니다");
    }
    
    @Test
    public void testMovableBoxMultipleInterfaces() {
        MovableBox movableBox = new MovableBox(100, 100, 50, 40, Color.GREEN);
        
        // MovableBox는 Box의 모든 인터페이스 + Movable 추가
        assertTrue(movableBox instanceof Paintable, "MovableBox가 Paintable을 구현하지 않았습니다");
        assertTrue(movableBox instanceof Movable, "MovableBox가 Movable을 구현하지 않았습니다");
        assertTrue(movableBox instanceof Collidable, "MovableBox가 Collidable을 구현하지 않았습니다");
        assertTrue(movableBox instanceof Boundable, "MovableBox가 Boundable을 구현하지 않았습니다");
        assertTrue(movableBox instanceof Box, "MovableBox가 Box를 상속받지 않았습니다");
        
        // 모든 기능이 작동하는지 확인
        movableBox.setDx(25);
        movableBox.setDy(35);
        double oldX = movableBox.getX();
        movableBox.move(1.0);
        assertNotEquals(oldX, movableBox.getX(), "MovableBox가 이동하지 않았습니다");
    }
    
    @Test
    public void testTriangleMultipleInterfaces() {
        Triangle triangle = new Triangle(100, 100, 30, Color.YELLOW);
        
        // Triangle의 인터페이스 구현 확인
        assertTrue(triangle instanceof Paintable, "Triangle이 Paintable을 구현하지 않았습니다");
        assertTrue(triangle instanceof Movable, "Triangle이 Movable을 구현하지 않았습니다");
        assertTrue(triangle instanceof Collidable, "Triangle이 Collidable을 구현하지 않았습니다");
        assertTrue(triangle instanceof Boundable, "Triangle이 Boundable을 구현하지 않았습니다");
        
        // Triangle 고유 기능 확인
        triangle.setDx(40);
        triangle.setDy(60);
        double oldX = triangle.getX();
        double oldY = triangle.getY();
        triangle.move(0.5);
        assertEquals(oldX + 20, triangle.getX(), 0.001, "Triangle X 이동이 올바르지 않습니다");
        assertEquals(oldY + 30, triangle.getY(), 0.001, "Triangle Y 이동이 올바르지 않습니다");
    }
}
```

### 다형성 활용 테스트 (6장 문제 해결 확인)

```java
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class PolymorphismSolutionTest {
    
    @Test
    public void testPolymorphicRendering() {
        // 6장의 문제: 각 타입별로 별도 렌더링 로직 필요
        // 7장의 해결: Paintable 인터페이스로 통합 처리
        
        List<Paintable> paintables = new ArrayList<>();
        paintables.add(new Ball(100, 100, 20, Color.RED));
        paintables.add(new Box(200, 200, 50, 40, Color.BLUE));
        paintables.add(new Triangle(300, 300, 30, Color.GREEN));
        paintables.add(new MovableBox(400, 400, 60, 50, Color.YELLOW));
        
        GraphicsContext gc = Mockito.mock(GraphicsContext.class);
        
        // 모든 타입을 동일한 방식으로 처리 가능!
        for (Paintable paintable : paintables) {
            assertDoesNotThrow(() -> paintable.paint(gc), 
                "Paintable 객체 렌더링 중 예외 발생: " + paintable.getClass().getSimpleName());
        }
        
        // 6장에서는 각 타입별로 instanceof 체크가 필요했지만
        // 7장에서는 단일 루프로 모든 객체 처리 가능!
        assertEquals(4, paintables.size(), "모든 타입의 객체가 하나의 리스트에서 관리됩니다");
    }
    
    @Test
    public void testPolymorphicMovement() {
        // 6장의 문제: MovableBall과 MovableBox를 별도로 처리
        // 7장의 해결: Movable 인터페이스로 통합 처리
        
        List<Movable> movables = new ArrayList<>();
        movables.add(new Ball(100, 100, 20, Color.RED));
        movables.add(new MovableBox(200, 200, 50, 40, Color.BLUE));
        movables.add(new Triangle(300, 300, 30, Color.GREEN));
        
        // 모든 객체에 동일한 속도 설정
        for (Movable movable : movables) {
            movable.setDx(50);
            movable.setDy(30);
        }
        
        // 위치 저장
        List<Double> oldXPositions = new ArrayList<>();
        List<Double> oldYPositions = new ArrayList<>();
        for (Movable movable : movables) {
            if (movable instanceof Ball) {
                oldXPositions.add(((Ball) movable).getX());
                oldYPositions.add(((Ball) movable).getY());
            } else if (movable instanceof MovableBox) {
                oldXPositions.add(((MovableBox) movable).getX());
                oldYPositions.add(((MovableBox) movable).getY());
            } else if (movable instanceof Triangle) {
                oldXPositions.add(((Triangle) movable).getX());
                oldYPositions.add(((Triangle) movable).getY());
            }
        }
        
        // 모든 객체를 동일한 방식으로 이동!
        for (Movable movable : movables) {
            movable.move(1.0);
        }
        
        // 이동 확인
        int index = 0;
        for (Movable movable : movables) {
            double newX, newY;
            if (movable instanceof Ball) {
                newX = ((Ball) movable).getX();
                newY = ((Ball) movable).getY();
            } else if (movable instanceof MovableBox) {
                newX = ((MovableBox) movable).getX();
                newY = ((MovableBox) movable).getY();
            } else if (movable instanceof Triangle) {
                newX = ((Triangle) movable).getX();
                newY = ((Triangle) movable).getY();
            } else {
                continue;
            }
            
            assertNotEquals(oldXPositions.get(index), newX, 
                "객체가 X 방향으로 이동하지 않았습니다: " + movable.getClass().getSimpleName());
            assertNotEquals(oldYPositions.get(index), newY, 
                "객체가 Y 방향으로 이동하지 않았습니다: " + movable.getClass().getSimpleName());
            index++;
        }
    }
    
    @Test
    public void testPolymorphicCollision() {
        // 6장의 문제: Ball-Ball, Box-Box, Ball-Box 등 조합별 메서드 필요
        // 7장의 해결: Collidable 인터페이스로 통합 처리
        
        List<Collidable> collidables = new ArrayList<>();
        Ball ball = new Ball(100, 100, 20, Color.RED);
        Box box = new Box(150, 150, 40, 30, Color.BLUE);
        Triangle triangle = new Triangle(200, 200, 25, Color.GREEN);
        
        ball.setCollisionAction(CollisionAction.BOUNCE);
        box.setCollisionAction(CollisionAction.BOUNCE);
        triangle.setCollisionAction(CollisionAction.BOUNCE);
        
        collidables.add(ball);
        collidables.add(box);
        collidables.add(triangle);
        
        // 모든 조합의 충돌을 동일한 로직으로 처리!
        for (int i = 0; i < collidables.size(); i++) {
            for (int j = i + 1; j < collidables.size(); j++) {
                Collidable obj1 = collidables.get(i);
                Collidable obj2 = collidables.get(j);
                
                if (obj1.isColliding(obj2)) {
                    // 두 객체 모두 충돌 처리
                    assertDoesNotThrow(() -> obj1.handleCollision(obj2), 
                        "충돌 처리 중 예외 발생: " + obj1.getClass().getSimpleName() + " vs " + obj2.getClass().getSimpleName());
                    assertDoesNotThrow(() -> obj2.handleCollision(obj1), 
                        "충돌 처리 중 예외 발생: " + obj2.getClass().getSimpleName() + " vs " + obj1.getClass().getSimpleName());
                }
            }
        }
        
        // 6장에서는 n(n+1)/2개의 별도 메서드가 필요했지만
        // 7장에서는 단일 이중 루프로 모든 조합 처리!
        System.out.println("3개 타입의 모든 충돌 조합을 단일 로직으로 처리 완료!");
    }
    
    @Test
    public void testNoMoreClassExplosion() {
        // 6장의 문제: 기능 조합마다 새 클래스 필요 (2^n 개)
        // 7장의 해결: 인터페이스를 통한 유연한 조합
        
        // 하나의 Ball 클래스가 모든 기능을 동시에 제공!
        Ball ball = new Ball(100, 100, 20, Color.RED);
        
        // 모든 기능을 하나의 객체에서 사용 가능
        assertTrue(ball instanceof Paintable, "그리기 기능");
        assertTrue(ball instanceof Movable, "움직임 기능");
        assertTrue(ball instanceof Collidable, "충돌 기능");
        assertTrue(ball instanceof Boundable, "경계 기능");
        
        // 하나의 Triangle 클래스도 마찬가지
        Triangle triangle = new Triangle(200, 200, 30, Color.BLUE);
        assertTrue(triangle instanceof Paintable, "Triangle 그리기 기능");
        assertTrue(triangle instanceof Movable, "Triangle 움직임 기능");
        assertTrue(triangle instanceof Collidable, "Triangle 충돌 기능");
        assertTrue(triangle instanceof Boundable, "Triangle 경계 기능");
        
        // 6장에서는 16개 클래스가 필요했지만 (Ball 8개 + Box 8개)
        // 7장에서는 기본 클래스 3개 + 특수 클래스 몇 개만 필요!
        System.out.println("클래스 폭발 문제 해결: 소수의 클래스로 모든 기능 조합 제공!");
    }
}
```

### CollisionAction 시스템 테스트

```java
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CollisionActionTest {
    
    @Test
    public void testBounceAction() {
        Ball ball = new Ball(50, 100, 15, Color.RED);
        ball.setDx(100);
        ball.setDy(50);
        ball.setCollisionAction(CollisionAction.BOUNCE);
        
        Box wall = new Box(80, 90, 20, 40, Color.GRAY);
        wall.setCollisionAction(CollisionAction.BOUNCE);
        
        // 충돌 전 속도 저장
        double oldDx = ball.getDx();
        double oldDy = ball.getDy();
        
        // 충돌 처리
        assertTrue(ball.isColliding(wall), "Ball과 Wall이 충돌해야 합니다");
        ball.handleCollision(wall);
        
        // BOUNCE 액션: 속도 반전
        assertNotEquals(oldDx, ball.getDx(), "BOUNCE 액션 후 X 속도가 변경되어야 합니다");
        // Y 속도는 수직 충돌이 아니면 변경되지 않을 수 있음
    }
    
    @Test
    public void testStopAction() {
        Ball ball = new Ball(100, 100, 20, Color.BLUE);
        ball.setDx(80);
        ball.setDy(60);
        ball.setCollisionAction(CollisionAction.STOP);
        
        Box obstacle = new Box(130, 130, 30, 30, Color.BROWN);
        
        // 충돌 처리
        assertTrue(ball.isColliding(obstacle), "Ball과 Obstacle이 충돌해야 합니다");
        ball.handleCollision(obstacle);
        
        // STOP 액션: 속도 0
        assertEquals(0, ball.getDx(), 0.001, "STOP 액션 후 X 속도가 0이어야 합니다");
        assertEquals(0, ball.getDy(), 0.001, "STOP 액션 후 Y 속도가 0이어야 합니다");
    }
    
    @Test
    public void testDestroyAction() {
        Ball ball = new Ball(100, 100, 20, Color.GREEN);
        ball.setCollisionAction(CollisionAction.DESTROY);
        
        Box destroyer = new Box(110, 110, 40, 40, Color.BLACK);
        
        // 충돌 처리
        assertTrue(ball.isColliding(destroyer), "Ball과 Destroyer가 충돌해야 합니다");
        ball.handleCollision(destroyer);
        
        // DESTROY 액션: 객체 제거 표시
        assertTrue(ball.isDestroyed(), "DESTROY 액션 후 객체가 제거 표시되어야 합니다");
    }
    
    @Test
    public void testPassAction() {
        Ball ball = new Ball(100, 100, 20, Color.YELLOW);
        ball.setDx(50);
        ball.setDy(30);
        ball.setCollisionAction(CollisionAction.PASS);
        
        Box passThrough = new Box(110, 110, 30, 30, Color.CYAN);
        
        // 충돌 전 속도 저장
        double oldDx = ball.getDx();
        double oldDy = ball.getDy();
        
        // 충돌 처리
        assertTrue(ball.isColliding(passThrough), "Ball과 PassThrough가 충돌해야 합니다");
        ball.handleCollision(passThrough);
        
        // PASS 액션: 속도 변화 없음
        assertEquals(oldDx, ball.getDx(), 0.001, "PASS 액션 후 X 속도가 변경되지 않아야 합니다");
        assertEquals(oldDy, ball.getDy(), 0.001, "PASS 액션 후 Y 속도가 변경되지 않아야 합니다");
    }
    
    @Test
    public void testCustomAction() {
        ExplodingBall explodingBall = new ExplodingBall(100, 100, 25, Color.ORANGE);
        explodingBall.setCollisionAction(CollisionAction.CUSTOM);
        
        Box trigger = new Box(115, 115, 20, 20, Color.RED);
        
        // 충돌 처리
        assertTrue(explodingBall.isColliding(trigger), "ExplodingBall과 Trigger가 충돌해야 합니다");
        explodingBall.handleCollision(trigger);
        
        // CUSTOM 액션: 폭발 (구현에 따라 다름)
        assertTrue(explodingBall.hasExploded(), "CUSTOM 액션 후 ExplodingBall이 폭발해야 합니다");
        assertTrue(explodingBall.isDestroyed(), "폭발 후 원본 객체는 제거되어야 합니다");
    }
    
    @Test
    public void testDynamicActionChange() {
        Ball ball = new Ball(100, 100, 20, Color.MAGENTA);
        
        // 런타임에 액션 변경 가능
        ball.setCollisionAction(CollisionAction.BOUNCE);
        assertEquals(CollisionAction.BOUNCE, ball.getCollisionAction(), "액션이 BOUNCE로 설정되지 않았습니다");
        
        ball.setCollisionAction(CollisionAction.STOP);
        assertEquals(CollisionAction.STOP, ball.getCollisionAction(), "액션이 STOP으로 변경되지 않았습니다");
        
        ball.setCollisionAction(CollisionAction.DESTROY);
        assertEquals(CollisionAction.DESTROY, ball.getCollisionAction(), "액션이 DESTROY로 변경되지 않았습니다");
        
        // 6장에서는 이런 유연성이 불가능했음!
        System.out.println("런타임 액션 변경 성공: 인터페이스의 유연성!");
    }
}
```

### SimpleWorld 통합 테스트

```java
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleWorldTest {
    
    private SimpleWorld world;
    
    @BeforeEach
    public void setUp() {
        world = new SimpleWorld(800, 600);
    }
    
    @Test
    public void testWorldCreation() {
        assertEquals(800, world.getWidth(), "World 너비가 올바르게 설정되지 않았습니다");
        assertEquals(600, world.getHeight(), "World 높이가 올바르게 설정되지 않았습니다");
        assertEquals(0, world.getObjectCount(), "초기 객체 수는 0이어야 합니다");
    }
    
    @Test
    public void testBoundaryCreation() {
        world.createBoundaries();
        
        // 경계가 생성되었는지 확인
        List<Box> boundaries = world.getBoundaries();
        assertEquals(4, boundaries.size(), "4개의 경계가 생성되어야 합니다");
        
        // 모든 경계가 BOUNCE 액션을 가지는지 확인
        for (Box boundary : boundaries) {
            assertEquals(CollisionAction.BOUNCE, boundary.getCollisionAction(), 
                "경계는 BOUNCE 액션을 가져야 합니다");
        }
    }
    
    @Test
    public void testMixedObjectManagement() {
        // 6장의 문제: Ball과 Box를 별도 리스트로 관리
        // 7장의 해결: 모든 객체를 하나의 리스트로 관리
        
        Ball ball = new Ball(100, 100, 20, Color.RED);
        Box box = new Box(200, 200, 50, 40, Color.BLUE);
        Triangle triangle = new Triangle(300, 300, 30, Color.GREEN);
        MovableBox movableBox = new MovableBox(400, 400, 60, 50, Color.YELLOW);
        
        world.addObject(ball);
        world.addObject(box);
        world.addObject(triangle);
        world.addObject(movableBox);
        
        assertEquals(4, world.getObjectCount(), "모든 타입의 객체가 추가되어야 합니다");
        
        // 모든 객체가 하나의 컬렉션에서 관리됨
        List<Object> objects = world.getObjects();
        assertEquals(4, objects.size(), "모든 객체가 하나의 리스트에서 관리됩니다");
    }
    
    @Test
    public void testPolymorphicUpdate() {
        Ball ball = new Ball(100, 100, 20, Color.RED);
        ball.setDx(50);
        ball.setDy(30);
        
        MovableBox movableBox = new MovableBox(200, 200, 40, 30, Color.BLUE);
        movableBox.setDx(-25);
        movableBox.setDy(35);
        
        Triangle triangle = new Triangle(300, 300, 25, Color.GREEN);
        triangle.setDx(40);
        triangle.setDy(-20);
        
        Box staticBox = new Box(400, 400, 50, 50, Color.GRAY); // 움직이지 않음
        
        world.addObject(ball);
        world.addObject(movableBox);
        world.addObject(triangle);
        world.addObject(staticBox);
        
        // 위치 저장
        double ballX = ball.getX();
        double ballY = ball.getY();
        double boxX = movableBox.getX();
        double boxY = movableBox.getY();
        double triangleX = triangle.getX();
        double triangleY = triangle.getY();
        double staticBoxX = staticBox.getX();
        double staticBoxY = staticBox.getY();
        
        // 업데이트 수행
        world.update(1.0);
        
        // Movable 객체들만 이동했는지 확인
        assertNotEquals(ballX, ball.getX(), "Ball이 이동하지 않았습니다");
        assertNotEquals(ballY, ball.getY(), "Ball이 이동하지 않았습니다");
        assertNotEquals(boxX, movableBox.getX(), "MovableBox가 이동하지 않았습니다");
        assertNotEquals(boxY, movableBox.getY(), "MovableBox가 이동하지 않았습니다");
        assertNotEquals(triangleX, triangle.getX(), "Triangle이 이동하지 않았습니다");
        assertNotEquals(triangleY, triangle.getY(), "Triangle이 이동하지 않았습니다");
        
        // 정적 Box는 이동하지 않음
        assertEquals(staticBoxX, staticBox.getX(), 0.001, "정적 Box는 이동하면 안됩니다");
        assertEquals(staticBoxY, staticBox.getY(), 0.001, "정적 Box는 이동하면 안됩니다");
    }
    
    @Test
    public void testPolymorphicRender() {
        Ball ball = new Ball(100, 100, 20, Color.RED);
        Box box = new Box(200, 200, 50, 40, Color.BLUE);
        Triangle triangle = new Triangle(300, 300, 30, Color.GREEN);
        
        world.addObject(ball);
        world.addObject(box);
        world.addObject(triangle);
        
        GraphicsContext gc = Mockito.mock(GraphicsContext.class);
        
        // 모든 객체가 동일한 방식으로 렌더링됨
        assertDoesNotThrow(() -> world.render(gc), "World 렌더링 중 예외 발생");
        
        // 6장에서는 각 타입별로 별도 렌더링 로직이 필요했지만
        // 7장에서는 Paintable 인터페이스로 통합 처리!
    }
    
    @Test
    public void testCollisionDetectionAndHandling() {
        Ball ball1 = new Ball(100, 100, 20, Color.RED);
        ball1.setDx(50);
        ball1.setCollisionAction(CollisionAction.BOUNCE);
        
        Ball ball2 = new Ball(130, 100, 15, Color.BLUE);
        ball2.setDx(-30);
        ball2.setCollisionAction(CollisionAction.BOUNCE);
        
        Box obstacle = new Box(200, 200, 40, 40, Color.GRAY);
        obstacle.setCollisionAction(CollisionAction.BOUNCE);
        
        world.addObject(ball1);
        world.addObject(ball2);
        world.addObject(obstacle);
        
        // 충돌 전 속도 저장
        double ball1DxBefore = ball1.getDx();
        double ball2DxBefore = ball2.getDx();
        
        // 충돌이 발생할 수 있도록 위치 조정
        assertTrue(ball1.isColliding(ball2), "두 Ball이 충돌해야 합니다");
        
        // 업데이트로 충돌 처리
        world.update(0.1);
        
        // 충돌 후 속도 변경 확인
        assertNotEquals(ball1DxBefore, ball1.getDx(), "Ball1의 속도가 변경되어야 합니다");
        assertNotEquals(ball2DxBefore, ball2.getDx(), "Ball2의 속도가 변경되어야 합니다");
    }
}
```

### 고급 기능 테스트

```java
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AdvancedFeatureTest {
    
    @Test
    public void testBouncingTriangle() {
        BouncingTriangle triangle = new BouncingTriangle(100, 100, 30, Color.PURPLE);
        triangle.setDx(60);
        triangle.setDy(40);
        triangle.setRotationSpeed(45); // 45도/초
        triangle.setCollisionAction(CollisionAction.BOUNCE);
        
        Box wall = new Box(140, 140, 20, 60, Color.GRAY);
        
        // 충돌 전 상태 저장
        double oldRotationSpeed = triangle.getRotationSpeed();
        Color oldColor = triangle.getColor();
        
        // 충돌 처리
        assertTrue(triangle.isColliding(wall), "Triangle과 Wall이 충돌해야 합니다");
        triangle.handleCollision(wall);
        
        // BouncingTriangle의 특수 기능 확인
        assertEquals(-oldRotationSpeed, triangle.getRotationSpeed(), 0.001, 
            "충돌 후 회전 속도가 반전되어야 합니다");
        assertNotEquals(oldColor, triangle.getColor(), 
            "충돌 후 색상이 변경되어야 합니다");
    }
    
    @Test
    public void testExplodingBall() {
        ExplodingBall explodingBall = new ExplodingBall(100, 100, 25, Color.ORANGE);
        explodingBall.setCollisionAction(CollisionAction.CUSTOM);
        explodingBall.setMiniballCount(5);
        
        Box trigger = new Box(120, 120, 20, 20, Color.RED);
        
        // 폭발 전 상태 확인
        assertFalse(explodingBall.hasExploded(), "폭발 전에는 hasExploded가 false여야 합니다");
        
        // 충돌로 폭발 유발
        assertTrue(explodingBall.isColliding(trigger), "ExplodingBall과 Trigger가 충돌해야 합니다");
        List<Ball> miniBalls = explodingBall.handleCollision(trigger);
        
        // 폭발 후 상태 확인
        assertTrue(explodingBall.hasExploded(), "폭발 후 hasExploded가 true여야 합니다");
        assertTrue(explodingBall.isDestroyed(), "폭발 후 원본 공은 제거되어야 합니다");
        assertEquals(5, miniBalls.size(), "5개의 작은 공이 생성되어야 합니다");
        
        // 작은 공들이 모두 움직이는지 확인
        for (Ball miniBall : miniBalls) {
            assertTrue(Math.abs(miniBall.getDx()) > 0 || Math.abs(miniBall.getDy()) > 0, 
                "작은 공들은 모두 움직여야 합니다");
            assertTrue(miniBall instanceof Movable, "작은 공들은 Movable이어야 합니다");
            assertTrue(miniBall instanceof Collidable, "작은 공들은 Collidable이어야 합니다");
        }
    }
    
    @Test
    public void testSpecialZone() {
        SpecialZone speedZone = new SpecialZone(150, 150, 100, 80, ZoneType.SPEED_UP);
        SpecialZone slowZone = new SpecialZone(300, 300, 120, 90, ZoneType.SLOW_DOWN);
        SpecialZone gravityZone = new SpecialZone(450, 450, 80, 100, ZoneType.GRAVITY);
        
        Ball ball = new Ball(175, 175, 15, Color.WHITE);
        ball.setDx(40);
        ball.setDy(30);
        
        // SPEED_UP 효과 테스트
        double oldDx = ball.getDx();
        double oldDy = ball.getDy();
        
        assertTrue(speedZone.isColliding(ball), "Ball이 SpeedZone에 있어야 합니다");
        speedZone.applyEffect(ball);
        
        assertEquals(oldDx * 2, ball.getDx(), 0.001, "SPEED_UP 효과로 X 속도가 2배가 되어야 합니다");
        assertEquals(oldDy * 2, ball.getDy(), 0.001, "SPEED_UP 효과로 Y 속도가 2배가 되어야 합니다");
        
        // SLOW_DOWN 효과 테스트
        ball.setX(350);
        ball.setY(350);
        oldDx = ball.getDx();
        oldDy = ball.getDy();
        
        assertTrue(slowZone.isColliding(ball), "Ball이 SlowZone에 있어야 합니다");
        slowZone.applyEffect(ball);
        
        assertEquals(oldDx * 0.5, ball.getDx(), 0.001, "SLOW_DOWN 효과로 X 속도가 절반이 되어야 합니다");
        assertEquals(oldDy * 0.5, ball.getDy(), 0.001, "SLOW_DOWN 효과로 Y 속도가 절반이 되어야 합니다");
        
        // GRAVITY 효과 테스트
        ball.setX(480);
        ball.setY(480);
        oldDy = ball.getDy();
        
        assertTrue(gravityZone.isColliding(ball), "Ball이 GravityZone에 있어야 합니다");
        gravityZone.applyEffect(ball);
        
        assertEquals(oldDy + 10, ball.getDy(), 0.001, "GRAVITY 효과로 Y 속도가 증가해야 합니다");
    }
    
    @Test
    public void testMazeWorld() {
        int[][] mazeLayout = {
            {1, 1, 1, 1, 1},
            {1, 0, 0, 0, 1},
            {1, 0, 1, 0, 1},
            {1, 0, 0, 2, 1},
            {1, 1, 1, 1, 1}
        };
        
        MazeWorld mazeWorld = new MazeWorld(500, 400, mazeLayout);
        
        // 미로가 올바르게 생성되었는지 확인
        List<Box> walls = mazeWorld.getWalls();
        List<Box> exits = mazeWorld.getExits();
        
        assertTrue(walls.size() > 0, "미로 벽이 생성되어야 합니다");
        assertTrue(exits.size() > 0, "출구가 생성되어야 합니다");
        
        // 모든 벽이 BOUNCE 액션을 가지는지 확인
        for (Box wall : walls) {
            assertEquals(CollisionAction.BOUNCE, wall.getCollisionAction(), 
                "미로 벽은 BOUNCE 액션을 가져야 합니다");
        }
        
        // 출구는 특별한 처리를 위해 CUSTOM 액션
        for (Box exit : exits) {
            assertEquals(CollisionAction.CUSTOM, exit.getCollisionAction(), 
                "출구는 CUSTOM 액션을 가져야 합니다");
        }
        
        // Ball이 통로에서만 이동 가능한지 테스트
        Ball player = new Ball(100, 100, 10, Color.YELLOW); // 통로 위치
        mazeWorld.addObject(player);
        
        // 미로 검증: Ball이 벽과 충돌하지 않는 위치에 있는지 확인
        boolean canMove = true;
        for (Box wall : walls) {
            if (player.isColliding(wall)) {
                canMove = false;
                break;
            }
        }
        assertTrue(canMove, "Ball이 통로에 올바르게 배치되어야 합니다");
    }
}
```

### 인터페이스 이점 종합 테스트

```java
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class InterfaceBenefitSummaryTest {
    
    @Test
    public void testFlexibilityImprovement() {
        // 6장의 문제: 기능 조합마다 새 클래스 필요
        // 7장의 해결: 인터페이스를 통한 유연한 기능 조합
        
        // 하나의 Ball 클래스가 모든 기능을 제공
        Ball ball = new Ball(100, 100, 20, Color.RED);
        
        // 런타임에 행동 변경 가능
        ball.setCollisionAction(CollisionAction.BOUNCE);
        assertEquals(CollisionAction.BOUNCE, ball.getCollisionAction());
        
        ball.setCollisionAction(CollisionAction.DESTROY);
        assertEquals(CollisionAction.DESTROY, ball.getCollisionAction());
        
        ball.setCollisionAction(CollisionAction.PASS);
        assertEquals(CollisionAction.PASS, ball.getCollisionAction());
        
        // 6장에서는 각 행동마다 별도 클래스가 필요했지만
        // 7장에서는 하나의 클래스로 모든 행동 구현!
        System.out.println("유연성 개선: 런타임 행동 변경 가능!");
    }
    
    @Test
    public void testMaintainabilityImprovement() {
        // 6장의 문제: 새 타입 추가 시 전체 코드 수정 필요
        // 7장의 해결: 새 타입 추가가 기존 코드에 영향 없음
        
        List<Paintable> paintables = new ArrayList<>();
        List<Movable> movables = new ArrayList<>();
        List<Collidable> collidables = new ArrayList<>();
        
        // 기존 타입들
        Ball ball = new Ball(100, 100, 20, Color.RED);
        Box box = new Box(200, 200, 50, 40, Color.BLUE);
        
        paintables.add(ball);
        paintables.add(box);
        movables.add(ball);
        collidables.add(ball);
        collidables.add(box);
        
        // 새로운 타입 추가 (Star 클래스)
        Star star = new Star(300, 300, 25, Color.YELLOW);
        
        // 기존 코드 수정 없이 새 타입 추가 가능!
        paintables.add(star);
        movables.add(star);
        collidables.add(star);
        
        // 모든 기존 처리 로직이 새 타입에도 동일하게 적용됨
        GraphicsContext gc = Mockito.mock(GraphicsContext.class);
        for (Paintable p : paintables) {
            assertDoesNotThrow(() -> p.paint(gc), 
                "새 타입도 기존 렌더링 로직에서 문제없이 작동");
        }
        
        for (Movable m : movables) {
            m.setDx(50);
            m.setDy(30);
            assertDoesNotThrow(() -> m.move(1.0), 
                "새 타입도 기존 이동 로직에서 문제없이 작동");
        }
        
        System.out.println("유지보수성 개선: 새 타입 추가가 기존 코드에 영향 없음!");
    }
    
    @Test
    public void testCodeReductionImprovement() {
        // 6장의 문제: 수많은 instanceof 체크와 중복 코드
        // 7장의 해결: 간결한 다형성 코드
        
        List<Object> mixedObjects = new ArrayList<>();
        mixedObjects.add(new Ball(100, 100, 20, Color.RED));
        mixedObjects.add(new Box(200, 200, 50, 40, Color.BLUE));
        mixedObjects.add(new Triangle(300, 300, 30, Color.GREEN));
        mixedObjects.add(new MovableBox(400, 400, 60, 50, Color.YELLOW));
        mixedObjects.add(new Star(500, 500, 25, Color.PURPLE));
        
        // 6장에서는 각 타입별로 복잡한 instanceof 체크가 필요했지만
        // 7장에서는 간단한 인터페이스 체크만 필요!
        
        int paintableCount = 0;
        int movableCount = 0;
        int collidableCount = 0;
        
        for (Object obj : mixedObjects) {
            // 간단한 인터페이스 체크
            if (obj instanceof Paintable) paintableCount++;
            if (obj instanceof Movable) movableCount++;
            if (obj instanceof Collidable) collidableCount++;
        }
        
        assertEquals(5, paintableCount, "모든 객체가 Paintable이어야 합니다");
        assertEquals(5, movableCount, "모든 객체가 Movable이어야 합니다");
        assertEquals(5, collidableCount, "모든 객체가 Collidable이어야 합니다");
        
        // 6장에서는 각 타입 조합마다 별도 체크가 필요했지만
        // 7장에서는 인터페이스 하나로 모든 구현체 처리!
        System.out.println("코드 간소화: instanceof 지옥 탈출!");
    }
    
    @Test
    public void testExtensibilityImprovement() {
        // 6장의 문제: 새 기능 추가 시 모든 클래스 수정 필요
        // 7장의 해결: 인터페이스 추가로 새 기능 확장
        
        // 새로운 인터페이스 추가 (예: Rotatable)
        Ball ball = new Ball(100, 100, 20, Color.RED);
        
        // 기존 객체에 새 기능 추가 가능
        if (ball instanceof Rotatable) {
            ((Rotatable) ball).rotate(45);
            ((Rotatable) ball).setRotationSpeed(90);
        }
        
        // 새 인터페이스를 구현하는 객체들
        List<Rotatable> rotatables = new ArrayList<>();
        rotatables.add(new BouncingTriangle(200, 200, 30, Color.BLUE));
        rotatables.add(new Star(300, 300, 25, Color.YELLOW));
        
        // 모든 회전 가능한 객체를 동일하게 처리
        for (Rotatable rotatable : rotatables) {
            assertDoesNotThrow(() -> rotatable.rotate(30), 
                "새 인터페이스 기능이 정상 작동해야 합니다");
            assertDoesNotThrow(() -> rotatable.setRotationSpeed(120), 
                "새 인터페이스 기능이 정상 작동해야 합니다");
        }
        
        System.out.println("확장성 개선: 새 인터페이스로 기능 확장 용이!");
    }
    
    @Test
    public void testPerformanceImprovement() {
        // 6장의 문제: 복잡한 타입 체크와 캐스팅으로 성능 저하
        // 7장의 해결: 효율적인 다형성 호출
        
        List<Movable> movables = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            if (i % 3 == 0) {
                movables.add(new Ball(i, i, 10, Color.RED));
            } else if (i % 3 == 1) {
                movables.add(new Triangle(i, i, 15, Color.BLUE));
            } else {
                movables.add(new MovableBox(i, i, 20, 20, Color.GREEN));
            }
        }
        
        long startTime = System.nanoTime();
        
        // 6장에서는 각 객체마다 복잡한 instanceof 체크가 필요했지만
        // 7장에서는 직접적인 인터페이스 메서드 호출!
        for (Movable movable : movables) {
            movable.setDx(1);
            movable.setDy(1);
            movable.move(0.016); // 60 FPS
        }
        
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        
        // 1000개 객체 처리가 빠르게 완료되어야 함
        assertTrue(duration < 10_000_000, // 10ms
            "1000개 객체 처리가 10ms 이내에 완료되어야 합니다: " + duration + "ns");
        
        System.out.println("성능 개선: " + (duration / 1_000_000.0) + "ms로 1000개 객체 처리!");
    }
}
```

### 테스트 실행 방법

1. **Maven 프로젝트의 경우**:
   ```bash
   mvn test
   ```

2. **IDE에서 실행**:
   - 테스트 클래스에서 우클릭 → "Run Tests"
   - 개별 테스트 메서드 실행 가능

3. **테스트를 통해 확인할 인터페이스의 이점들**:
   - **유연성**: 런타임에 행동 변경 가능
   - **확장성**: 새 타입 추가가 기존 코드에 영향 없음
   - **단순성**: 복잡한 instanceof 체크 제거
   - **성능**: 효율적인 다형성 호출
   - **유지보수성**: 새 기능 추가가 용이

### 테스트 해석 가이드

이 테스트들은 **인터페이스가 6장의 상속 문제들을 어떻게 해결하는지** 보여줍니다:

- **`testPolymorphicRendering`**: 모든 타입을 Paintable 인터페이스로 통일 처리
- **`testPolymorphicMovement`**: MovableBall과 MovableBox 중복 코드 해결
- **`testPolymorphicCollision`**: n(n+1)/2 충돌 메서드 문제 해결
- **`testNoMoreClassExplosion`**: 클래스 폭발 문제 해결
- **`testFlexibilityImprovement`**: 런타임 행동 변경으로 유연성 확보
- **`testMaintainabilityImprovement`**: 새 타입 추가 시 기존 코드 무수정
- **`testCodeReductionImprovement`**: instanceof 지옥 탈출
- **`testExtensibilityImprovement`**: 새 인터페이스로 기능 확장
- **`testPerformanceImprovement`**: 효율적인 다형성 호출

7장의 인터페이스 접근 방식이 6장의 모든 문제점들을 우아하게 해결함을 확인할 수 있습니다.

## 학습 체크포인트

- [ ] 인터페이스를 설계하고 구현했습니다
- [ ] 다중 인터페이스의 이점을 이해했습니다
- [ ] 다형성을 활용하여 코드를 단순화했습니다
- [ ] Box를 경계로 활용했습니다
- [ ] 다양한 충돌 액션을 구현했습니다