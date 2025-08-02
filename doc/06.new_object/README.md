# 6장: 새로운 객체들 (New Objects) - 상속의 한계

## 학습 목표

이 장을 완료하면 다음을 할 수 있습니다:
- Box 클래스를 상속 구조에 추가할 수 있습니다
- 상속만 사용할 때의 문제점을 식별할 수 있습니다
- 다중 객체 타입 처리의 복잡성을 경험합니다
- 코드 중복과 유지보수의 어려움을 인식합니다
- 인터페이스가 필요한 이유를 이해합니다

## 핵심 개념

### 6.1 Box 클래스 추가 - 상속의 딜레마

**문제 상황**

새로운 도형인 Box를 추가하려고 합니다. Box도 Ball처럼:
- 화면에 그려짐 수 있음
- 움직일 수 있음
- 충돌할 수 있음

**방법 1: 별개의 클래스 계층**

**Box 클래스 설계:**
- 필드: `x`, `y`, `width`, `height`
- 메서드: Ball과 유사한 기능들
- 문제: Ball과 코드 중복 발생!

**코드 중복 예시:**
```java
// Ball 클래스에서
public void move(double deltaTime) {
    x += dx * deltaTime;
    y += dy * deltaTime;
}

// Box 클래스에서도 똑같이
public void move(double deltaTime) {
    x += dx * deltaTime;  // 완전히 동일한 코드!
    y += dy * deltaTime;
}
```

**방법 2: 공통 부모 클래스**

**GameObject 추상 클래스:**
- 공통 필드: `x`, `y`
- 추상 메서드: `draw()`, `getBounds()`
- Ball과 Box가 GameObject 상속
- 문제: 계층 구조가 복잡해짐

**두 방법 모두의 문제점:**
1. 코드 중복 또는 복잡한 계층 구조
2. 새로운 기능 추가 시 어려움
3. 유연성 부족

### 6.2 PaintableBox - 또 다른 상속 문제

**PaintableBox 클래스 설계**

Box에 색상 기능을 추가하려고 합니다:

**문제점:**
1. PaintableBall과 동일한 color 관련 코드 중복
2. 색상 처리 로직이 여러 클래스에 흩어짐
3. 다중 상속이 필요하지만 Java는 지원 안 함

**코드 중복 예시:**
- `color` 필드
- `getColor()`, `setColor()` 메서드
- null 체크 로직
- 모두 PaintableBall과 동일!

**원하는 것 (불가능):**
```java
// Java는 다중 상속을 지원하지 않음
public class PaintableBox extends Box, Paintable { } // 컴파일 오류!
```

**다중 상속이란?**
여러 클래스로부터 동시에 상속받는 것을 의미합니다. C++에서는 가능하지만 Java에서는 불가능합니다. 이는 "다이아몬드 문제"를 방지하기 위함입니다.

### 6.3 MovableBox - 상속 계층의 폭발

**클래스 폭발 문제**

각 기능 조합마다 새로운 클래스가 필요합니다:

**MovableBox:**
- Box를 상속받아 움직임 추가
- 필드: `dx`, `dy`
- 문제: MovableBall과 코드 중복

**클래스 폭발이란?**
각 기능의 조합마다 새로운 클래스를 만들어야 하는 문제입니다. 예를 들어:
- Box만 있는 클래스
- 움직이는 Box 클래스
- 색깔이 있는 Box 클래스
- 움직이고 색깔이 있는 Box 클래스
- ... 계속 증가!

**PaintableMovableBox:**
- MovableBox를 상속받아 색상 추가
- 필드: `color`
- 문제: PaintableBox와 코드 중복

**BoundedPaintableMovableBox:**
- PaintableMovableBox를 상속받아 경계 처리 추가
- 문제: 클래스 이름이 너무 길어짐!

**조합 폭발:**
- 3개 기능 = 2³ = 8개 클래스
- 4개 기능 = 2⁴ = 16개 클래스
- n개 기능 = 2ⁿ개 클래스!

### 6.4 World 클래스의 복잡성 증가

**ComplexWorld 클래스 설계**

Ball과 Box를 모두 관리하려면:

**문제점들:**

1. **별도 컨테이너 필요**
   - `List<Ball> balls`
   - `List<Box> boxes`
   - 새 타입 추가 시 또 다른 리스트

2. **중복 업데이트 로직**
   - Ball 업데이트 루프
   - Box 업데이트 루프
   - 각각 instanceof 검사와 캐스팅

3. **충돌 처리 조합 폭발**
   - Ball-Ball 충돌
   - Box-Box 충돌
   - Ball-Box 충돌
   - n개 타입 = n(n+1)/2 조합

4. **타입별 렌더링**
   - 각 타입마다 별도 처리
   - instanceof 지옥

**유지보수 악몽:**
새로운 도형(Triangle, Polygon)이 추가되면 코드 전체를 수정해야 합니다!

### 6.5 충돌 처리의 복잡성

**CollisionHandler 클래스 설계**

각 타입 조합마다 별도의 충돌 처리 로직이 필요합니다:

**필요한 메서드들:**

1. **Ball-Ball 충돌**
   - 탄성 충돌 처리
   - 운동량 보존

2. **Box-Box 충돌**
   - 사각형 간 충돌
   - 별도 로직 필요

3. **Ball-Box 충돌**
   - 어느 면과 충돌했는지 확인
   - 면 충돌 vs 코너 충돌
   - 완전히 다른 처리 필요

**조합 폭발:**
- 2개 타입: 3개 메서드
- 3개 타입: 6개 메서드
- 4개 타입: 10개 메서드
- n개 타입: n(n+1)/2개 메서드

**CollisionSide enum:**
- TOP, BOTTOM, LEFT, RIGHT, CORNER
- 각 경우마다 다른 처리 필요

### 6.6 상속만 사용할 때의 문제점 정리

**문제점 1: 클래스 폭발**

3개의 기능(Paintable, Movable, Bounded)을 조합하면:
- Ball: 8개 클래스 필요
- Box: 8개 클래스 필요
- 총 16개 클래스!

**문제점 2: 코드 중복**

MovableBall과 MovableBox에 동일한 move() 메서드:
- 같은 코드를 여러 번 작성
- 수정 시 모두 찾아서 변경해야 함
- DRY 원칙 위반

**문제점 3: 타입 체크 지옥**

모든 곳에서 instanceof 검사:
- 복잡한 조건문
- 타입 추가 시 전체 코드 수정
- 유지보수 악몽

**문제점 4: 유연성 부족**

런타임에 기능 변경 불가:
- Ball을 MovableBall로 변경? 불가능!
- 새 객체 생성해야 함
- 데이터 복사 필요

## 실습 과제

### Lab 6-1: Box 클래스 구현

**Box 클래스 설계**

상속을 사용하여 Box 클래스 계층을 구현하세요:

**Box 클래스 요구사항:**

**필드 (private):**
- `x`, `y`: 왼쪽 상단 좌표
- `width`, `height`: 크기

**메서드:**
- 생성자: 위치와 크기를 받아 초기화
- getter/setter: 모든 필드에 대해
- `contains(double px, double py)`: 점이 박스 안에 있는지 확인
- `getBounds()`: RectangleBounds 객체 반환

**PaintableBox 클래스 요구사항:**
- Box를 상속받아 구현
- 추가 필드: `color` (Color 타입)
- 추가 메서드: `getColor()`, `setColor()`
- PaintableBall과 동일한 색상 처리 로직 필요

**구현 힌트:**
```java
// contains 메서드 로직
// px가 x와 x+width 사이에 있고
// py가 y와 y+height 사이에 있으면 true
```

**경험할 문제점:**
PaintableBall의 color 관련 코드를 그대로 복사해야 합니다!

### Lab 6-2: 혼합 World 구현

**MixedWorld 클래스 설계**

Ball과 Box를 모두 관리하는 World를 구현하세요:

**필드 (private):**
- `List<Ball> balls`: Ball 객체들 저장
- `List<Box> boxes`: Box 객체들 저장
- `width`, `height`: 화면 크기

**메서드 요구사항:**

1. **addBall(Ball ball)**:
   - balls 리스트에 추가

2. **addBox(Box box)**:
   - boxes 리스트에 추가

3. **update(double deltaTime)**:
   - 모든 Ball 순회하며 instanceof로 타입 체크
   - MovableBall이면 move() 호출
   - 모든 Box 순회하며 instanceof로 타입 체크
   - MovableBox이면 move() 호출

4. **render(GraphicsContext gc)**:
   - 배경 그리기
   - 모든 Ball 순회하며 instanceof로 타입 체크
   - PaintableBall이면 paint() 호출
   - 모든 Box 순회하며 instanceof로 타입 체크
   - PaintableBox이면 그리기 구현

**구현 힌트:**
```java
// update 메서드의 비효율성
// Ball용 루프와 Box용 루프가 별도로 필요
// 각 루프에서 instanceof 검사 반복
```

**경험할 문제점:**
- 코드가 중복되고 복잡해집니다
- 새로운 도형(Triangle)을 추가하면 모든 메서드 수정 필요

### Lab 6-3: 충돌 처리 확장

**ExtendedCollisionHandler 클래스 설계**

Ball-Box 충돌을 추가하세요:

**구현해야 할 메서드:**

1. **checkBallToBallCollisions(List<Ball> balls)**:
   - 이중 루프로 모든 Ball 쌍 검사
   - 거리 계산으로 충돌 판단
   - 충돌 시 반사 처리

2. **checkBoxToBoxCollisions(List<Box> boxes)**:
   - 이중 루프로 모든 Box 쌍 검사
   - 사각형 교차 검사 (AABB)
   - 충돌 시 처리 로직

3. **checkBallToBoxCollisions(List<Ball> balls, List<Box> boxes)**:
   - 모든 Ball-Box 쌍 검사
   - Box의 어느 면과 충돌했는지 확인
   - 면에 따라 다른 반사 방향 계산

**Ball-Box 충돌 판단 로직:**
```java
// 1. Box에서 Ball 중심까지 가장 가까운 점 찾기
// 2. 그 점과 Ball 중심의 거리 계산
// 3. 거리가 Ball 반지름보다 작으면 충돌
// 4. 충돌한 면 판단 (상/하/좌/우/코너)
```

**CollisionSide 판단:**
- TOP: Ball이 Box 위에서 충돌
- BOTTOM: Ball이 Box 아래에서 충돌
- LEFT: Ball이 Box 왼쪽에서 충돌
- RIGHT: Ball이 Box 오른쪽에서 충돌
- CORNER: Ball이 Box 모서리와 충돌

**조합 폭발 계산:**
- 2개 타입: 3개 메서드 (Ball-Ball, Box-Box, Ball-Box)
- 3개 타입: 6개 메서드 (+ Triangle-Triangle, Ball-Triangle, Box-Triangle)
- 4개 타입: 10개 메서드
- n개 타입: n(n+1)/2개 메서드!

### Lab 6-4: 문제점 분석
상속만 사용했을 때의 문제점을 문서화:
1. 몇 개의 클래스가 필요한가?
2. 코드 중복이 얼마나 발생하는가?
3. 새로운 도형(Triangle)을 추가한다면?
4. 새로운 기능(Rotatable)을 추가한다면?

## JUnit 테스트 예제

```java
public class InheritanceProblemTest {
    
    @Test
    public void testCodeDuplication() {
        // MovableBall과 MovableBox의 move 메서드가 동일한지 확인
        MovableBall ball = new MovableBall(100, 100, 20);
        MovableBox box = new MovableBox(200, 200, 40, 40);
        
        ball.setDx(50);
        ball.setDy(50);
        box.setDx(50);
        box.setDy(50);
        
        ball.move(1.0);
        box.move(1.0);
        
        // 동일한 이동을 했지만 코드는 중복됨
        assertEquals(150, ball.getX(), 0.001);
        assertEquals(250, box.getX(), 0.001);
    }
    
    @Test
    public void testTypeCheckingComplexity() {
        List<Object> objects = new ArrayList<>();
        objects.add(new Ball(0, 0, 10));
        objects.add(new MovableBall(0, 0, 10));
        objects.add(new Box(0, 0, 20, 20));
        objects.add(new MovableBox(0, 0, 20, 20));
        
        int movableCount = 0;
        for (Object obj : objects) {
            // 각 타입을 개별적으로 체크해야 함
            if (obj instanceof MovableBall || obj instanceof MovableBox) {
                movableCount++;
            }
        }
        
        assertEquals(2, movableCount);
    }
    
    @Test
    public void testCollisionComplexity() {
        Ball ball = new Ball(50, 50, 20);
        Box box = new Box(60, 60, 40, 40);
        
        // Ball과 Box의 충돌을 처리하는 별도 로직 필요
        boolean collision = checkBallBoxCollision(ball, box);
        assertTrue(collision);
    }
    
    private boolean checkBallBoxCollision(Ball ball, Box box) {
        // Box를 Bounds로 변환하여 검사
        Bounds boxBounds = new RectangleBounds(
            box.getX(), box.getY(), box.getWidth(), box.getHeight()
        );
        
        double closestX = Math.max(boxBounds.getMinX(), 
                         Math.min(ball.getX(), boxBounds.getMaxX()));
        double closestY = Math.max(boxBounds.getMinY(), 
                         Math.min(ball.getY(), boxBounds.getMaxY()));
        
        double distanceX = ball.getX() - closestX;
        double distanceY = ball.getY() - closestY;
        double distanceSquared = distanceX * distanceX + distanceY * distanceY;
        
        return distanceSquared < (ball.getRadius() * ball.getRadius());
    }
}
```

## 자가 평가 문제

1. **상속만 사용했을 때 클래스가 몇 개 필요한가?**
   - 기능 조합의 수만큼 (2^n개)
   - 3개 기능 = 8개 클래스

2. **코드 중복이 발생하는 이유는?**
   - Java는 단일 상속만 지원
   - 여러 클래스에서 동일한 기능 필요

3. **새로운 타입 추가 시 수정해야 할 곳은?**
   - World 클래스
   - 충돌 처리 로직
   - 렌더링 로직
   - 모든 타입 체크 코드

4. **이 문제를 해결하는 방법은?**
   - 인터페이스 사용
   - 컴포지션 패턴
   - 다형성 활용

## 자주 하는 실수와 문제점

### 1. 깊은 상속 계층
```java
// 나쁜 예 - 너무 깊은 상속
class GameObject {}
class Shape extends GameObject {}
class Circle extends Shape {}
class Ball extends Circle {}
class MovableBall extends Ball {}
class PaintableMovableBall extends MovableBall {}
// 6단계 상속!
```

### 2. 기능별 클래스 폭발
```java
// 3개의 기능 조합 = 8개 클래스
Ball
PaintableBall
MovableBall  
BoundedBall
PaintableMovableBall
PaintableBoundedBall
MovableBoundedBall
PaintableMovableBoundedBall
```

### 3. 타입 체크 지옥
```java
// 모든 곳에서 instanceof 체크
if (obj instanceof PaintableMovableBoundedBall) {
    // ...
} else if (obj instanceof PaintableMovableBall) {
    // ...
} else if (obj instanceof PaintableBoundedBall) {
    // ...
} // 계속...
```

## 다음 장 미리보기

7장에서는 인터페이스를 도입하여 이러한 문제를 해결합니다:
- Paintable, Movable, Boundable 인터페이스
- 다중 인터페이스 구현
- 다형성을 통한 코드 단순화
- 유연한 객체 조합

## 추가 학습 자료

- [Composition over Inheritance](https://en.wikipedia.org/wiki/Composition_over_inheritance)
- [The Diamond Problem](https://en.wikipedia.org/wiki/Multiple_inheritance#The_diamond_problem)
- [SOLID Principles](https://en.wikipedia.org/wiki/SOLID)

## 구현 검증용 테스트 코드

아래 테스트 코드를 사용하여 구현한 클래스들이 올바르게 작동하는지 확인하고, 상속만 사용할 때의 문제점들을 경험해보세요:

### Box 클래스 테스트

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class BoxTest {
    
    @Test
    public void testBoxCreation() {
        Box box = new Box(100, 200, 50, 30);
        assertEquals(100, box.getX(), 0.001, "X 좌표가 올바르게 설정되지 않았습니다");
        assertEquals(200, box.getY(), 0.001, "Y 좌표가 올바르게 설정되지 않았습니다");
        assertEquals(50, box.getWidth(), 0.001, "너비가 올바르게 설정되지 않았습니다");
        assertEquals(30, box.getHeight(), 0.001, "높이가 올바르게 설정되지 않았습니다");
    }
    
    @Test
    public void testBoxPosition() {
        Box box = new Box(50, 75, 40, 60);
        
        // setter 테스트
        box.setX(150);
        box.setY(175);
        box.setWidth(80);
        box.setHeight(120);
        
        assertEquals(150, box.getX(), 0.001, "setX가 올바르게 작동하지 않습니다");
        assertEquals(175, box.getY(), 0.001, "setY가 올바르게 작동하지 않습니다");
        assertEquals(80, box.getWidth(), 0.001, "setWidth가 올바르게 작동하지 않습니다");
        assertEquals(120, box.getHeight(), 0.001, "setHeight가 올바르게 작동하지 않습니다");
    }
    
    @Test
    public void testSizeValidation() {
        // 유효하지 않은 크기 테스트
        assertThrows(IllegalArgumentException.class, () -> {
            new Box(0, 0, -10, 20); // 음수 너비는 예외 발생해야 함
        }, "음수 너비에 대해 예외가 발생하지 않았습니다");
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Box(0, 0, 20, -10); // 음수 높이는 예외 발생해야 함
        }, "음수 높이에 대해 예외가 발생하지 않았습니다");
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Box(0, 0, 0, 10); // 0 너비도 예외 발생해야 함
        }, "0 너비에 대해 예외가 발생하지 않았습니다");
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Box(0, 0, 10, 0); // 0 높이도 예외 발생해야 함
        }, "0 높이에 대해 예외가 발생하지 않았습니다");
    }
    
    @Test
    public void testContains() {
        Box box = new Box(100, 100, 80, 60);
        
        // Box 내부의 점들
        assertTrue(box.contains(100, 100), "왼쪽 상단 모서리가 포함되지 않았습니다");
        assertTrue(box.contains(140, 130), "Box 내부 점이 포함되지 않았습니다");
        assertTrue(box.contains(179, 159), "오른쪽 하단 경계 근처 점이 포함되지 않았습니다");
        
        // Box 외부의 점들
        assertFalse(box.contains(50, 50), "Box 외부 점이 포함되었습니다");
        assertFalse(box.contains(200, 200), "Box 외부 점이 포함되었습니다");
        assertFalse(box.contains(180, 130), "오른쪽 경계 밖 점이 포함되었습니다");
        assertFalse(box.contains(140, 160), "아래쪽 경계 밖 점이 포함되었습니다");
    }
    
    @Test
    public void testGetBounds() {
        Box box = new Box(50, 75, 100, 80);
        RectangleBounds bounds = box.getBounds();
        
        assertEquals(50, bounds.getMinX(), 0.001, "경계의 최소 X가 올바르지 않습니다");
        assertEquals(75, bounds.getMinY(), 0.001, "경계의 최소 Y가 올바르지 않습니다");
        assertEquals(150, bounds.getMaxX(), 0.001, "경계의 최대 X가 올바르지 않습니다");
        assertEquals(155, bounds.getMaxY(), 0.001, "경계의 최대 Y가 올바르지 않습니다");
    }
}
```

### PaintableBox 클래스 테스트 (코드 중복 문제 확인)

```java
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PaintableBoxTest {
    
    @Test
    public void testPaintableBoxCreation() {
        PaintableBox box = new PaintableBox(100, 100, 50, 40, Color.BLUE);
        
        // 상속받은 Box의 속성들 확인
        assertEquals(100, box.getX(), 0.001, "X 좌표가 올바르게 설정되지 않았습니다");
        assertEquals(100, box.getY(), 0.001, "Y 좌표가 올바르게 설정되지 않았습니다");
        assertEquals(50, box.getWidth(), 0.001, "너비가 올바르게 설정되지 않았습니다");
        assertEquals(40, box.getHeight(), 0.001, "높이가 올바르게 설정되지 않았습니다");
        
        // PaintableBox의 고유 속성 확인
        assertEquals(Color.BLUE, box.getColor(), "색상이 올바르게 설정되지 않았습니다");
    }
    
    @Test
    public void testPaintableBoxInheritance() {
        PaintableBox paintableBox = new PaintableBox(100, 100, 50, 40, Color.GREEN);
        
        // Box의 메서드들이 작동하는지 확인 (상속 확인)
        assertTrue(paintableBox instanceof Box, "PaintableBox는 Box를 상속받아야 합니다");
        
        // Box의 메서드 사용 가능한지 확인
        paintableBox.setX(200);
        paintableBox.setY(300);
        paintableBox.setWidth(100);
        paintableBox.setHeight(80);
        assertEquals(200, paintableBox.getX(), 0.001, "상속받은 setX가 작동하지 않습니다");
        assertEquals(300, paintableBox.getY(), 0.001, "상속받은 setY가 작동하지 않습니다");
        assertEquals(100, paintableBox.getWidth(), 0.001, "상속받은 setWidth가 작동하지 않습니다");
        assertEquals(80, paintableBox.getHeight(), 0.001, "상속받은 setHeight가 작동하지 않습니다");
        
        // contains 메서드도 사용 가능한지 확인
        assertTrue(paintableBox.contains(250, 340), "상속받은 contains가 작동하지 않습니다");
    }
    
    @Test
    public void testColorHandling() {
        PaintableBox box = new PaintableBox(0, 0, 50, 50, Color.RED);
        
        // 색상 변경
        box.setColor(Color.YELLOW);
        assertEquals(Color.YELLOW, box.getColor(), "색상 변경이 올바르게 작동하지 않습니다");
        
        // null 색상 처리 (PaintableBall과 동일한 로직이어야 함)
        assertThrows(IllegalArgumentException.class, () -> {
            box.setColor(null);
        }, "null 색상 설정 시 예외가 발생해야 합니다");
    }
    
    @Test
    public void testDefaultColor() {
        // 색상 없이 생성하는 생성자가 있다면
        PaintableBox box = new PaintableBox(0, 0, 50, 50);
        assertNotNull(box.getColor(), "기본 색상이 설정되어야 합니다");
        // 기본 색상은 빨간색이어야 함
        assertEquals(Color.RED, box.getColor(), "기본 색상은 빨간색이어야 합니다");
    }
    
    @Test
    public void testCodeDuplicationWithPaintableBall() {
        // 이 테스트는 PaintableBox와 PaintableBall의 색상 처리 로직이
        // 동일하다는 것을 보여줍니다 (코드 중복 문제)
        PaintableBox box = new PaintableBox(0, 0, 50, 50, Color.PURPLE);
        PaintableBall ball = new PaintableBall(0, 0, 25, Color.PURPLE);
        
        // 동일한 색상 처리
        assertEquals(box.getColor(), ball.getColor(), "색상 처리 로직이 동일해야 합니다");
        
        // 동일한 색상 변경
        box.setColor(Color.ORANGE);
        ball.setColor(Color.ORANGE);
        assertEquals(box.getColor(), ball.getColor(), "색상 변경 로직이 동일해야 합니다");
        
        // 동일한 null 처리
        assertThrows(IllegalArgumentException.class, () -> box.setColor(null));
        assertThrows(IllegalArgumentException.class, () -> ball.setColor(null));
    }
}
```

### MovableBox 클래스 테스트 (클래스 폭발 문제 확인)

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MovableBoxTest {
    
    @Test
    public void testMovableBoxCreation() {
        MovableBox box = new MovableBox(100, 100, 50, 40);
        
        assertEquals(100, box.getX(), 0.001, "X 좌표가 올바르게 설정되지 않았습니다");
        assertEquals(100, box.getY(), 0.001, "Y 좌표가 올바르게 설정되지 않았습니다");
        assertEquals(50, box.getWidth(), 0.001, "너비가 올바르게 설정되지 않았습니다");
        assertEquals(40, box.getHeight(), 0.001, "높이가 올바르게 설정되지 않았습니다");
        assertEquals(0, box.getDx(), 0.001, "초기 X 속도는 0이어야 합니다");
        assertEquals(0, box.getDy(), 0.001, "초기 Y 속도는 0이어야 합니다");
    }
    
    @Test
    public void testMovement() {
        MovableBox box = new MovableBox(100, 100, 50, 40);
        box.setDx(50);
        box.setDy(30);
        
        // 1초 동안 이동
        box.move(1.0);
        
        assertEquals(150, box.getX(), 0.001, "X 좌표 이동이 올바르지 않습니다");
        assertEquals(130, box.getY(), 0.001, "Y 좌표 이동이 올바르지 않습니다");
    }
    
    @Test
    public void testVelocityHandling() {
        MovableBox box = new MovableBox(0, 0, 50, 40);
        
        // 속도 설정
        box.setDx(100);
        box.setDy(-50);
        
        assertEquals(100, box.getDx(), 0.001, "X 속도 설정이 올바르지 않습니다");
        assertEquals(-50, box.getDy(), 0.001, "Y 속도 설정이 올바르지 않습니다");
        
        // 작은 시간 간격으로 이동
        box.move(0.1);
        
        assertEquals(10, box.getX(), 0.001, "작은 시간 간격 이동이 올바르지 않습니다");
        assertEquals(-5, box.getY(), 0.001, "작은 시간 간격 이동이 올바르지 않습니다");
    }
    
    @Test
    public void testCodeDuplicationWithMovableBall() {
        // 이 테스트는 MovableBox와 MovableBall의 move 로직이
        // 동일하다는 것을 보여줍니다 (코드 중복 문제)
        MovableBox box = new MovableBox(0, 0, 50, 40);
        MovableBall ball = new MovableBall(0, 0, 25);
        
        // 동일한 속도 설정
        box.setDx(60);
        box.setDy(40);
        ball.setDx(60);
        ball.setDy(40);
        
        // 동일한 시간만큼 이동
        box.move(2.0);
        ball.move(2.0);
        
        // 이동 결과가 동일해야 함 (move 로직이 동일하기 때문)
        assertEquals(120, box.getX(), 0.001, "Box 이동 결과");
        assertEquals(80, box.getY(), 0.001, "Box 이동 결과");
        assertEquals(120, ball.getX(), 0.001, "Ball 이동 결과");
        assertEquals(80, ball.getY(), 0.001, "Ball 이동 결과");
    }
}
```

### PaintableMovableBox 클래스 테스트 (클래스 조합 폭발 문제)

```java
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PaintableMovableBoxTest {
    
    @Test
    public void testPaintableMovableBoxCreation() {
        PaintableMovableBox box = new PaintableMovableBox(100, 100, 50, 40, Color.CYAN);
        
        // Box 속성 확인
        assertEquals(100, box.getX(), 0.001, "X 좌표가 올바르게 설정되지 않았습니다");
        assertEquals(100, box.getY(), 0.001, "Y 좌표가 올바르게 설정되지 않았습니다");
        assertEquals(50, box.getWidth(), 0.001, "너비가 올바르게 설정되지 않았습니다");
        assertEquals(40, box.getHeight(), 0.001, "높이가 올바르게 설정되지 않았습니다");
        
        // Paintable 속성 확인
        assertEquals(Color.CYAN, box.getColor(), "색상이 올바르게 설정되지 않았습니다");
        
        // Movable 속성 확인
        assertEquals(0, box.getDx(), 0.001, "초기 X 속도는 0이어야 합니다");
        assertEquals(0, box.getDy(), 0.001, "초기 Y 속도는 0이어야 합니다");
    }
    
    @Test
    public void testMultipleInheritanceChain() {
        PaintableMovableBox box = new PaintableMovableBox(0, 0, 30, 30, Color.MAGENTA);
        
        // 상속 체인 확인
        assertTrue(box instanceof Box, "Box를 상속받아야 합니다");
        assertTrue(box instanceof MovableBox, "MovableBox를 상속받아야 합니다");
        assertTrue(box instanceof PaintableMovableBox, "PaintableMovableBox 타입이어야 합니다");
        
        // 모든 기능이 작동하는지 확인
        box.setX(100);
        box.setY(200);
        box.setWidth(60);
        box.setHeight(80);
        box.setColor(Color.LIME);
        box.setDx(25);
        box.setDy(35);
        
        assertEquals(100, box.getX(), 0.001, "Box 기능이 작동하지 않습니다");
        assertEquals(200, box.getY(), 0.001, "Box 기능이 작동하지 않습니다");
        assertEquals(60, box.getWidth(), 0.001, "Box 기능이 작동하지 않습니다");
        assertEquals(80, box.getHeight(), 0.001, "Box 기능이 작동하지 않습니다");
        assertEquals(Color.LIME, box.getColor(), "Paintable 기능이 작동하지 않습니다");
        assertEquals(25, box.getDx(), 0.001, "Movable 기능이 작동하지 않습니다");
        assertEquals(35, box.getDy(), 0.001, "Movable 기능이 작동하지 않습니다");
        
        // 이동 기능 확인
        box.move(1.0);
        assertEquals(125, box.getX(), 0.001, "move 기능이 작동하지 않습니다");
        assertEquals(235, box.getY(), 0.001, "move 기능이 작동하지 않습니다");
    }
    
    @Test
    public void testClassExplosionProblem() {
        // 이 테스트는 기능 조합마다 새로운 클래스가 필요하다는 것을 보여줍니다
        
        // 3개의 기능: Paintable, Movable, Bounded
        // 각 기능의 조합마다 클래스가 필요 = 2^3 = 8개 클래스
        
        Box basic = new Box(0, 0, 10, 10);
        PaintableBox paintable = new PaintableBox(0, 0, 10, 10, Color.RED);
        MovableBox movable = new MovableBox(0, 0, 10, 10);
        BoundedBox bounded = new BoundedBox(0, 0, 10, 10);
        PaintableMovableBox paintableMovable = new PaintableMovableBox(0, 0, 10, 10, Color.RED);
        PaintableBoundedBox paintableBounded = new PaintableBoundedBox(0, 0, 10, 10, Color.RED);
        MovableBoundedBox movableBounded = new MovableBoundedBox(0, 0, 10, 10);
        PaintableMovableBoundedBox all = new PaintableMovableBoundedBox(0, 0, 10, 10, Color.RED);
        
        // 8개의 서로 다른 클래스가 필요함을 확인
        assertNotEquals(basic.getClass(), paintable.getClass());
        assertNotEquals(basic.getClass(), movable.getClass());
        assertNotEquals(paintable.getClass(), paintableMovable.getClass());
        assertNotEquals(movable.getClass(), paintableMovable.getClass());
        
        // 이것이 클래스 폭발 문제입니다!
        System.out.println("필요한 클래스 수: 8개");
        System.out.println("기능이 하나 더 추가되면: 16개 클래스 필요!");
    }
}
```

### MixedWorld 클래스 테스트 (타입 체크 복잡성 확인)

```java
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;

public class MixedWorldTest {
    
    private MixedWorld world;
    
    @BeforeEach
    public void setUp() {
        world = new MixedWorld(800, 600);
    }
    
    @Test
    public void testWorldCreation() {
        assertEquals(800, world.getWidth(), "World의 너비가 올바르게 설정되지 않았습니다");
        assertEquals(600, world.getHeight(), "World의 높이가 올바르게 설정되지 않았습니다");
        assertEquals(0, world.getBallCount(), "초기 Ball 수는 0이어야 합니다");
        assertEquals(0, world.getBoxCount(), "초기 Box 수는 0이어야 합니다");
    }
    
    @Test
    public void testAddMixedObjects() {
        Ball ball = new Ball(100, 100, 20);
        PaintableBall paintableBall = new PaintableBall(200, 200, 25, Color.BLUE);
        Box box = new Box(300, 300, 40, 50);
        PaintableBox paintableBox = new PaintableBox(400, 400, 30, 35, Color.GREEN);
        
        world.addBall(ball);
        world.addBall(paintableBall);
        world.addBox(box);
        world.addBox(paintableBox);
        
        assertEquals(2, world.getBallCount(), "Ball이 올바르게 추가되지 않았습니다");
        assertEquals(2, world.getBoxCount(), "Box가 올바르게 추가되지 않았습니다");
    }
    
    @Test
    public void testTypeCheckingComplexity() {
        // 다양한 타입의 객체들 추가
        world.addBall(new Ball(0, 0, 10));
        world.addBall(new PaintableBall(0, 0, 10, Color.RED));
        world.addBall(new MovableBall(0, 0, 10));
        world.addBall(new PaintableMovableBall(0, 0, 10, Color.BLUE));
        world.addBox(new Box(0, 0, 20, 20));
        world.addBox(new PaintableBox(0, 0, 20, 20, Color.GREEN));
        world.addBox(new MovableBox(0, 0, 20, 20));
        world.addBox(new PaintableMovableBox(0, 0, 20, 20, Color.YELLOW));
        
        // update 메서드에서 각 타입별로 instanceof 체크가 필요함
        world.update(1.0);
        
        // 이 테스트는 컴파일 오류 없이 실행되지만,
        // 실제 구현에서는 수많은 instanceof 체크가 필요함을 보여줍니다
        assertEquals(4, world.getBallCount(), "Ball 타입 객체들이 관리되고 있습니다");
        assertEquals(4, world.getBoxCount(), "Box 타입 객체들이 관리되고 있습니다");
    }
    
    @Test
    public void testRenderComplexity() {
        GraphicsContext gc = Mockito.mock(GraphicsContext.class);
        
        // 다양한 타입의 객체들 추가
        world.addBall(new PaintableBall(100, 100, 20, Color.RED));
        world.addBall(new MovableBall(200, 200, 25));
        world.addBox(new PaintableBox(300, 300, 40, 50, Color.BLUE));
        world.addBox(new MovableBox(400, 400, 30, 35));
        
        // 렌더링 수행 (각 타입별로 다른 처리 필요)
        assertDoesNotThrow(() -> {
            world.render(gc);
        }, "MixedWorld 렌더링 중 예외가 발생했습니다");
        
        // 실제로는 render 메서드 내부에서 다음과 같은 복잡한 타입 체크가 필요:
        // if (ball instanceof PaintableBall) { ... }
        // else if (ball instanceof MovableBall) { ... }
        // if (box instanceof PaintableBox) { ... }
        // else if (box instanceof MovableBox) { ... }
    }
    
    @Test
    public void testUpdateComplexity() {
        MovableBall movableBall = new MovableBall(100, 100, 20);
        movableBall.setDx(50);
        movableBall.setDy(30);
        
        MovableBox movableBox = new MovableBox(200, 200, 40, 50);
        movableBox.setDx(25);
        movableBox.setDy(35);
        
        world.addBall(movableBall);
        world.addBox(movableBox);
        
        // 업데이트 전 위치
        double ballX = movableBall.getX();
        double ballY = movableBall.getY();
        double boxX = movableBox.getX();
        double boxY = movableBox.getY();
        
        // 1초 업데이트
        world.update(1.0);
        
        // 움직이는 객체들만 이동했는지 확인
        // (update 메서드에서 instanceof MovableBall, instanceof MovableBox 체크 필요)
        assertNotEquals(ballX, movableBall.getX(), "MovableBall이 이동하지 않았습니다");
        assertNotEquals(ballY, movableBall.getY(), "MovableBall이 이동하지 않았습니다");
        assertNotEquals(boxX, movableBox.getX(), "MovableBox가 이동하지 않았습니다");
        assertNotEquals(boxY, movableBox.getY(), "MovableBox가 이동하지 않았습니다");
    }
}
```

### ExtendedCollisionHandler 클래스 테스트 (충돌 처리 조합 폭발)

```java
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ExtendedCollisionHandlerTest {
    
    @Test
    public void testBallToBallCollisions() {
        ExtendedCollisionHandler handler = new ExtendedCollisionHandler();
        
        // 충돌하는 두 Ball
        MovableBall ball1 = new MovableBall(100, 100, 20);
        MovableBall ball2 = new MovableBall(130, 100, 15);
        ball1.setDx(50);
        ball1.setDy(0);
        ball2.setDx(-30);
        ball2.setDy(0);
        
        List<Ball> balls = Arrays.asList(ball1, ball2);
        
        // 충돌 처리 전 속도 저장
        double ball1VxBefore = ball1.getDx();
        double ball2VxBefore = ball2.getDx();
        
        handler.checkBallToBallCollisions(balls);
        
        // 충돌 후 속도가 변경되었는지 확인
        assertNotEquals(ball1VxBefore, ball1.getDx(), "Ball1의 속도가 변경되지 않았습니다");
        assertNotEquals(ball2VxBefore, ball2.getDx(), "Ball2의 속도가 변경되지 않았습니다");
    }
    
    @Test
    public void testBoxToBoxCollisions() {
        ExtendedCollisionHandler handler = new ExtendedCollisionHandler();
        
        // 충돌하는 두 Box
        MovableBox box1 = new MovableBox(100, 100, 40, 30);
        MovableBox box2 = new MovableBox(120, 110, 50, 40);
        box1.setDx(20);
        box1.setDy(10);
        box2.setDx(-15);
        box2.setDy(-5);
        
        List<Box> boxes = Arrays.asList(box1, box2);
        
        // 충돌 처리 전 속도 저장
        double box1VxBefore = box1.getDx();
        double box1VyBefore = box1.getDy();
        
        handler.checkBoxToBoxCollisions(boxes);
        
        // 충돌 후 속도가 변경되었는지 확인
        assertNotEquals(box1VxBefore, box1.getDx(), "Box1의 X 속도가 변경되지 않았습니다");
        assertNotEquals(box1VyBefore, box1.getDy(), "Box1의 Y 속도가 변경되지 않았습니다");
    }
    
    @Test
    public void testBallToBoxCollisions() {
        ExtendedCollisionHandler handler = new ExtendedCollisionHandler();
        
        // Ball이 Box와 충돌
        MovableBall ball = new MovableBall(50, 100, 15);
        Box box = new Box(80, 90, 60, 40);
        ball.setDx(40);
        ball.setDy(0);
        
        List<Ball> balls = Arrays.asList(ball);
        List<Box> boxes = Arrays.asList(box);
        
        // 충돌 처리 전 속도 저장
        double ballVxBefore = ball.getDx();
        
        handler.checkBallToBoxCollisions(balls, boxes);
        
        // Ball이 Box의 왼쪽 면과 충돌하여 속도가 반전되었는지 확인
        assertEquals(-ballVxBefore, ball.getDx(), 0.1, "Ball의 X 속도가 반전되지 않았습니다");
    }
    
    @Test
    public void testCollisionSideDetection() {
        ExtendedCollisionHandler handler = new ExtendedCollisionHandler();
        Box box = new Box(100, 100, 80, 60);
        
        // 각 면에서의 충돌 테스트
        assertEquals(CollisionSide.LEFT, handler.detectCollisionSide(70, 130, box), "왼쪽 면 충돌 감지 실패");
        assertEquals(CollisionSide.RIGHT, handler.detectCollisionSide(200, 130, box), "오른쪽 면 충돌 감지 실패");
        assertEquals(CollisionSide.TOP, handler.detectCollisionSide(140, 80, box), "위쪽 면 충돌 감지 실패");
        assertEquals(CollisionSide.BOTTOM, handler.detectCollisionSide(140, 180, box), "아래쪽 면 충돌 감지 실패");
        assertEquals(CollisionSide.CORNER, handler.detectCollisionSide(95, 95, box), "코너 충돌 감지 실패");
    }
    
    @Test
    public void testCollisionCombinationExplosion() {
        // 이 테스트는 타입이 증가할수록 필요한 충돌 메서드가 기하급수적으로 증가함을 보여줍니다
        
        // 현재: Ball, Box (2개 타입)
        // 필요한 충돌 메서드: 3개 (Ball-Ball, Box-Box, Ball-Box)
        
        ExtendedCollisionHandler handler = new ExtendedCollisionHandler();
        assertTrue(handler.hasMethod("checkBallToBallCollisions"), "Ball-Ball 충돌 메서드 필요");
        assertTrue(handler.hasMethod("checkBoxToBoxCollisions"), "Box-Box 충돌 메서드 필요");
        assertTrue(handler.hasMethod("checkBallToBoxCollisions"), "Ball-Box 충돌 메서드 필요");
        
        // Triangle이 추가되면 6개 메서드 필요:
        // Ball-Ball, Box-Box, Triangle-Triangle, Ball-Box, Ball-Triangle, Box-Triangle
        
        // Circle이 추가되면 10개 메서드 필요:
        // 기존 6개 + Circle-Circle, Ball-Circle, Box-Circle, Triangle-Circle
        
        // n개 타입 = n(n+1)/2개 메서드 필요!
        int typeCount = 2;
        int requiredMethods = typeCount * (typeCount + 1) / 2;
        assertEquals(3, requiredMethods, "2개 타입에 대해 3개 충돌 메서드 필요");
        
        // 타입이 4개로 증가하면?
        typeCount = 4;
        requiredMethods = typeCount * (typeCount + 1) / 2;
        assertEquals(10, requiredMethods, "4개 타입에 대해 10개 충돌 메서드 필요 - 조합 폭발!");
    }
}
```

### 상속 문제점 종합 테스트

```java
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InheritanceProblemSummaryTest {
    
    @Test
    public void testClassExplosionProblem() {
        // 클래스 폭발 문제: 3개 기능 조합 = 8개 클래스 필요
        
        // 기본 클래스들
        Box box = new Box(0, 0, 10, 10);
        Ball ball = new Ball(0, 0, 10);
        
        // 1개 기능 추가 클래스들
        PaintableBox paintableBox = new PaintableBox(0, 0, 10, 10, Color.RED);
        MovableBox movableBox = new MovableBox(0, 0, 10, 10);
        BoundedBox boundedBox = new BoundedBox(0, 0, 10, 10);
        
        // 2개 기능 조합 클래스들
        PaintableMovableBox paintableMovableBox = new PaintableMovableBox(0, 0, 10, 10, Color.BLUE);
        PaintableBoundedBox paintableBoundedBox = new PaintableBoundedBox(0, 0, 10, 10, Color.GREEN);
        MovableBoundedBox movableBoundedBox = new MovableBoundedBox(0, 0, 10, 10);
        
        // 3개 기능 모두 포함 클래스
        PaintableMovableBoundedBox allFeaturesBox = new PaintableMovableBoundedBox(0, 0, 10, 10, Color.YELLOW);
        
        // Box만으로도 8개 클래스 필요! (Ball도 마찬가지로 8개)
        // 총 16개 클래스가 필요함
        assertTrue(allFeaturesBox instanceof Box, "모든 기능 클래스도 Box를 상속받아야 함");
        assertTrue(allFeaturesBox instanceof PaintableBox, "Paintable 기능 포함");
        assertTrue(allFeaturesBox instanceof MovableBox, "Movable 기능 포함");
        assertTrue(allFeaturesBox instanceof BoundedBox, "Bounded 기능 포함");
        
        System.out.println("현재 필요한 클래스 수: 16개 (Box 8개 + Ball 8개)");
        System.out.println("새로운 기능 1개 추가 시: 32개 클래스 필요!");
        System.out.println("새로운 도형 1개 추가 시: 24개 클래스 추가 필요!");
    }
    
    @Test
    public void testCodeDuplicationProblem() {
        // 코드 중복 문제: 동일한 기능이 여러 클래스에 중복됨
        
        PaintableBox paintableBox = new PaintableBox(0, 0, 10, 10, Color.RED);
        PaintableBall paintableBall = new PaintableBall(0, 0, 10, Color.RED);
        
        // 색상 처리 로직이 완전히 동일함 (코드 중복!)
        paintableBox.setColor(Color.BLUE);
        paintableBall.setColor(Color.BLUE);
        assertEquals(paintableBox.getColor(), paintableBall.getColor());
        
        // null 처리 로직도 동일함
        assertThrows(IllegalArgumentException.class, () -> paintableBox.setColor(null));
        assertThrows(IllegalArgumentException.class, () -> paintableBall.setColor(null));
        
        MovableBox movableBox = new MovableBox(0, 0, 10, 10);
        MovableBall movableBall = new MovableBall(0, 0, 10);
        
        // 이동 처리 로직도 완전히 동일함 (코드 중복!)
        movableBox.setDx(50);
        movableBox.setDy(30);
        movableBall.setDx(50);
        movableBall.setDy(30);
        
        movableBox.move(1.0);
        movableBall.move(1.0);
        
        assertEquals(50, movableBox.getX(), 0.001);
        assertEquals(30, movableBox.getY(), 0.001);
        assertEquals(50, movableBall.getX(), 0.001);
        assertEquals(30, movableBall.getY(), 0.001);
    }
    
    @Test
    public void testTypeCheckingComplexity() {
        // 타입 체크 복잡성: instanceof 지옥
        
        Object[] objects = {
            new Ball(0, 0, 10),
            new PaintableBall(0, 0, 10, Color.RED),
            new MovableBall(0, 0, 10),
            new PaintableMovableBall(0, 0, 10, Color.BLUE),
            new Box(0, 0, 10, 10),
            new PaintableBox(0, 0, 10, 10, Color.GREEN),
            new MovableBox(0, 0, 10, 10),
            new PaintableMovableBox(0, 0, 10, 10, Color.YELLOW)
        };
        
        // 각 타입별로 처리하려면 복잡한 instanceof 체크 필요
        int paintableCount = 0;
        int movableCount = 0;
        
        for (Object obj : objects) {
            // Paintable 체크 - 모든 Paintable 타입을 나열해야 함
            if (obj instanceof PaintableBall || 
                obj instanceof PaintableMovableBall ||
                obj instanceof PaintableBox ||
                obj instanceof PaintableMovableBox) {
                paintableCount++;
            }
            
            // Movable 체크 - 모든 Movable 타입을 나열해야 함
            if (obj instanceof MovableBall || 
                obj instanceof PaintableMovableBall ||
                obj instanceof MovableBox ||
                obj instanceof PaintableMovableBox) {
                movableCount++;
            }
        }
        
        assertEquals(4, paintableCount, "Paintable 객체 수");
        assertEquals(4, movableCount, "Movable 객체 수");
        
        // 새로운 타입이 추가될 때마다 모든 instanceof 체크를 수정해야 함!
    }
    
    @Test
    public void testMaintenanceNightmare() {
        // 유지보수 악몽: 새로운 도형(Triangle) 추가 시나리오
        
        // 현재 2개 도형(Ball, Box) × 3개 기능 = 16개 클래스
        // Triangle 추가 시 3개 도형 × 3개 기능 = 24개 클래스 (8개 추가)
        
        // 추가해야 할 클래스들:
        // Triangle, PaintableTriangle, MovableTriangle, BoundedTriangle,
        // PaintableMovableTriangle, PaintableBoundedTriangle,
        // MovableBoundedTriangle, PaintableMovableBoundedTriangle
        
        // 수정해야 할 기존 코드들:
        // 1. World 클래스에 List<Triangle> triangles 추가
        // 2. 모든 update/render 메서드에 Triangle 처리 로직 추가
        // 3. 충돌 처리에 Triangle 관련 메서드 3개 추가 (Triangle-Ball, Triangle-Box, Triangle-Triangle)
        // 4. 모든 instanceof 체크 코드에 Triangle 타입들 추가
        
        System.out.println("Triangle 추가 시:");
        System.out.println("- 새로운 클래스: 8개");
        System.out.println("- 수정해야 할 기존 메서드: 수십 개");
        System.out.println("- 추가 충돌 메서드: 3개");
        System.out.println("- instanceof 체크 수정: 모든 타입 체크 코드");
        
        assertTrue(true, "이것이 상속만 사용했을 때의 유지보수 악몽입니다!");
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

3. **테스트를 통해 경험할 문제점들**:
   - **클래스 폭발**: 기능 조합마다 새 클래스 필요
   - **코드 중복**: 동일한 기능을 여러 클래스에서 구현
   - **타입 체크 복잡성**: instanceof 지옥
   - **유지보수 어려움**: 새 타입 추가 시 전체 코드 수정 필요

### 테스트 해석 가이드

이 테스트들은 실제 구현 검증뿐만 아니라 **상속만 사용했을 때의 문제점들을 체험**하도록 설계되었습니다:

- **`testClassExplosionProblem`**: 기능이 증가할수록 필요한 클래스 수가 기하급수적으로 증가
- **`testCodeDuplicationProblem`**: 동일한 로직이 여러 클래스에 중복으로 구현됨
- **`testTypeCheckingComplexity`**: 타입별 처리를 위해 복잡한 instanceof 체크 필요
- **`testCollisionCombinationExplosion`**: 타입이 증가할수록 충돌 처리 메서드가 n(n+1)/2개 필요
- **`testMaintenanceNightmare`**: 새로운 타입 추가 시 전체 코드베이스 수정 필요

이러한 문제점들을 경험한 후 7장에서 인터페이스를 통해 어떻게 해결할 수 있는지 배우게 됩니다.

## 학습 체크포인트

- [ ] Box 클래스를 상속 구조에 추가했습니다
- [ ] 클래스 폭발 문제를 경험했습니다
- [ ] 코드 중복 문제를 확인했습니다
- [ ] 타입 체크의 복잡성을 이해했습니다
- [ ] 인터페이스의 필요성을 인식했습니다