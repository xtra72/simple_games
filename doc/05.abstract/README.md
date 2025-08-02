# 5장: 추상 데이터 타입 (Abstract Data Types)

## 학습 목표

이 장을 완료하면 다음을 할 수 있습니다:
- 추상 클래스의 개념과 용도를 이해할 수 있습니다
- Bounds와 Vector 추상 데이터 타입을 설계할 수 있습니다
- 코드 재사용성을 높이는 방법을 적용할 수 있습니다
- Template Method 패턴을 구현할 수 있습니다
- 상속 계층을 효과적으로 설계할 수 있습니다

## 핵심 개념

### 5.1 추상 클래스의 이해

**추상 클래스(Abstract Class)란?**

추상 클래스는 완전하지 않은 클래스로, 직접 인스턴스를 만들 수 없고 상속만 가능합니다:
- **abstract 키워드**: 추상 클래스나 메서드를 선언
- **추상 메서드**: 구현이 없는 메서드, 하위 클래스에서 반드시 구현
- **구체 메서드**: 일반적인 메서드, 공통 기능 제공

**Shape 추상 클래스 설계**

**필드:**
- `x`, `y`: 위치 좌표 (protected)

**추상 메서드:**
- `getArea()`: 면적 계산 (하위 클래스에서 구현)
- `getPerimeter()`: 둘레 계산 (하위 클래스에서 구현)

**구체 메서드:**
- `getX()`, `getY()`: 위치 반환
- `move(double dx, double dy)`: 위치 이동
- `displayInfo()`: 정보 출력 (Template Method 패턴)

**Template Method 패턴:**
상위 클래스에서 알고리즘의 구조를 정의하고, 하위 클래스에서 세부 구현을 제공하는 패턴입니다.

```java
// 추상 클래스 선언 예시
public abstract class Shape {
    // 추상 메서드 - 구현 없음
    public abstract double getArea();
    
    // 구체 메서드 - 구현 있음
    public void move(double dx, double dy) {
        // 구현 코드
    }
}
```

### 5.2 Bounds 추상 클래스

**Bounds 추상 클래스 설계**

객체의 경계를 표현하는 추상 데이터 타입입니다:

**추상 메서드:**
- `getMinX()`: 최소 x 좌표
- `getMinY()`: 최소 y 좌표  
- `getMaxX()`: 최대 x 좌표
- `getMaxY()`: 최대 y 좌표

**구체 메서드 (base 구현 제공):**
- `getWidth()`: 너비 계산 (maxX - minX)
- `getHeight()`: 높이 계산 (maxY - minY)
- `getCenterX()`, `getCenterY()`: 중심 좌표
- `contains(double x, double y)`: 점 포함 여부
- `contains(Bounds other)`: 다른 Bounds 포함 여부
- `intersects(Bounds other)`: 교차 여부

**RectangleBounds 클래스 설계**

Bounds를 상속받아 사각형 경계를 구현:

**필드:**
- `x`, `y`: 왼쪽 상단 모서리
- `width`, `height`: 너비와 높이

**구현 요구사항:**
- 생성자에서 크기 유효성 검사
- 4개의 추상 메서드 모두 구현

**CircleBounds 클래스 설계**

Bounds를 상속받아 원형 경계를 구현:

**필드:**
- `centerX`, `centerY`: 중심 좌표
- `radius`: 반지름

**구현 힌트:**
```java
// 원의 경계 사각형
minX = centerX - radius
maxX = centerX + radius
minY = centerY - radius
maxY = centerY + radius
```

### 5.3 향상된 Vector 클래스

**Vector 추상 클래스 설계**

n차원 벡터를 표현하는 추상 클래스입니다:

**추상 메서드:**
- `getDimension()`: 벡터의 차원 반환
- `get(int index)`: 특정 인덱스의 값 반환
- `set(int index, double value)`: 특정 인덱스에 값 설정
- `createNew()`: 새 벡터 생성 (Factory Method 패턴)

**구체 메서드:**
- `magnitude()`: 벡터의 크기 계산
- `normalize()`: 정규화 (크기를 1로)
- `dot(Vector other)`: 내적 계산

**Vector2D 클래스 설계**

Vector를 상속받아 2차원 벡터 구현:

**필드:**
- `x`, `y`: 벡터 구성 요소

**추가 메서드:**
- `getX()`, `getY()`: 각 구성 요소 반환
- `add(Vector2D other)`: 벡터 덧셈
- `subtract(Vector2D other)`: 벡터 뻔셈
- `multiply(double scalar)`: 스칼라 곱셈
- `angle()`: 벡터의 각도 (라디안)

**Factory Method 패턴:**
하위 클래스에서 객체 생성 방법을 결정하는 패턴입니다.

**구현 힌트:**
```java
// 벡터 크기: √(Σ(vᵢ)²)
// 내적: Σ(aᵢ × bᵢ)
// 정규화: v / |v|
```

### 5.4 추상 Ball 클래스 리팩토링

**AbstractBall 추상 클래스 설계**

기존 Ball 클래스를 추상 클래스로 리팩토링:

**필드:**
- `position`: Vector2D로 위치 관리
- `radius`: 반지름
- `bounds`: Bounds 객체로 경계 관리

**Template Method 패턴 적용:**
- `update(double deltaTime)`: 업데이트 프로세스 정의
  1. `beforeUpdate()`: 전처리 (기본 구현 비어있음)
  2. `performUpdate()`: 핵심 로직 (추상 메서드)
  3. `afterUpdate()`: 후처리 (기본 구현 비어있음)
  4. `updateBounds()`: 경계 업데이트

**추상 메서드:**
- `performUpdate(double deltaTime)`: 하위 클래스에서 구현

**SimpleMovableBall 클래스 설계**

AbstractBall을 상속받아 움직이는 공 구현:

**추가 필드:**
- `velocity`: 속도 벡터

**구현 요구사항:**
- `performUpdate()`: 속도에 따른 위치 업데이트
- `setVelocity()`: 속도 설정

**리팩토링 이점:**
1. Vector2D를 사용한 깨끗한 위치 관리
2. Bounds 객체로 경계 처리 통합
3. Template Method로 업데이트 프로세스 표준화
4. 확장 가능한 구조

## 실습 과제

### Lab 5-1: Bounds 시스템 구현
다양한 Bounds 구현체를 만들고 테스트:
- RectangleBounds
- CircleBounds
- CompositeBounds (여러 Bounds의 조합)

```java
@Test
public void testBoundsIntersection() {
    Bounds rect = new RectangleBounds(0, 0, 100, 100);
    Bounds circle = new CircleBounds(150, 50, 30);
    
    assertFalse(rect.intersects(circle));
    
    circle = new CircleBounds(80, 50, 30);
    assertTrue(rect.intersects(circle));
}
```

### Lab 5-2: Vector 클래스 확장
3D 벡터 구현 및 벡터 연산:
- Vector3D 클래스
- 외적(cross product) 구현
- 각도 계산

### Lab 5-3: 추상 클래스 기반 게임 객체
추상 클래스를 사용한 게임 객체 계층:
**GameObject 추상 클래스 설계**

모든 게임 객체의 기본 클래스:

**필드:**
- `position`: 위치 (Vector2D)
- `bounds`: 경계 (Bounds)

**추상 메서드:**
- `update(double deltaTime)`: 상태 업데이트
- `render(GraphicsContext gc)`: 화면에 그리기
- `handleCollision(GameObject other)`: 충돌 처리

하위 클래스에서 각 게임 객체에 맞게 구현합니다.

### Lab 5-4: 디자인 패턴 적용
Factory Method와 Template Method 패턴 구현:
**BallFactory 추상 클래스 설계**

Factory Method 패턴을 사용한 공 생성:

**추상 메서드:**
- `createBall(double x, double y, double radius)`: 구체적인 Ball 생성

**구체 메서드:**
- `createRandomBalls(int count, Bounds area)`: 랜덤 공 여러 개 생성
  - 주어진 영역 내에 랜덤 위치
  - 랜덤 크기 (10~40)
  - createBall() 호출하여 생성

**Factory Method 패턴의 장점:**
1. 객체 생성 로직 캡슐화
2. 하위 클래스에서 생성 방식 결정
3. 확장 가능한 구조

**구현 힌트:**
```java
// 랜덤 위치 계산
x = minX + random() * width
y = minY + random() * height
```

## JUnit 테스트 예제

```java
public class AbstractTypeTest {
    
    @Test
    public void testVector2DOperations() {
        Vector2D v1 = new Vector2D(3, 4);
        assertEquals(5.0, v1.magnitude(), 0.001);
        
        Vector2D v2 = v1.normalize();
        assertEquals(1.0, v2.magnitude(), 0.001);
        assertEquals(0.6, v2.getX(), 0.001);
        assertEquals(0.8, v2.getY(), 0.001);
    }
    
    @Test
    public void testBoundsContainment() {
        Bounds outer = new RectangleBounds(0, 0, 200, 200);
        Bounds inner = new CircleBounds(100, 100, 50);
        
        assertTrue(outer.contains(inner));
        assertFalse(inner.contains(outer));
        
        assertTrue(outer.contains(100, 100));
        assertTrue(inner.contains(100, 100));
    }
    
    @Test
    public void testAbstractBallUpdate() {
        SimpleMovableBall ball = new SimpleMovableBall(100, 100, 20);
        ball.setVelocity(50, 30);
        
        double oldX = ball.getX();
        double oldY = ball.getY();
        
        ball.update(1.0);
        
        assertEquals(oldX + 50, ball.getX(), 0.001);
        assertEquals(oldY + 30, ball.getY(), 0.001);
        
        // Bounds도 업데이트되었는지 확인
        assertEquals(ball.getX(), ball.getBounds().getCenterX(), 0.001);
        assertEquals(ball.getY(), ball.getBounds().getCenterY(), 0.001);
    }
}
```

## 자가 평가 문제

1. **추상 클래스와 인터페이스의 차이점은?**
   - 추상 클래스: 부분 구현 가능, 단일 상속
   - 인터페이스: 구현 없음(Java 8 이전), 다중 구현

2. **Template Method 패턴의 장점은?**
   - 알고리즘의 구조를 정의
   - 세부 구현은 하위 클래스에 위임
   - 코드 중복 제거

3. **추상 데이터 타입(ADT)의 특징은?**
   - 데이터와 연산의 캡슐화
   - 구현 세부사항 은닉
   - 인터페이스를 통한 접근

4. **Factory Method 패턴을 사용하는 이유는?**
   - 객체 생성 로직의 캡슐화
   - 유연한 객체 생성
   - 의존성 역전

## 자주 하는 실수와 해결 방법

### 1. 추상 클래스 인스턴스화 시도
```java
// 잘못된 코드
AbstractBall ball = new AbstractBall(0, 0, 10); // 컴파일 에러!

// 올바른 코드
AbstractBall ball = new SimpleMovableBall(0, 0, 10);
```

### 2. 추상 메서드 구현 누락
```java
// 잘못된 코드 - 추상 메서드를 구현하지 않음
public class MyBall extends AbstractBall {
    // performUpdate 메서드 누락!
}

// 올바른 코드
public class MyBall extends AbstractBall {
    @Override
    protected void performUpdate(double deltaTime) {
        // 구현 필수
    }
}
```

### 3. Protected vs Private
```java
// 잘못된 코드 - 하위 클래스에서 접근 불가
public abstract class Base {
    private int value; // 하위 클래스에서 접근 불가
}

// 올바른 코드
public abstract class Base {
    protected int value; // 하위 클래스에서 접근 가능
}
```

## 구현 검증용 테스트 코드

아래 테스트 코드를 사용하여 구현한 클래스들이 올바르게 작동하는지 확인하세요:

### Shape 추상 클래스와 하위 클래스 테스트

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ShapeTest {
    
    @Test
    public void testCircleShapeCreation() {
        Circle circle = new Circle(100, 100, 30);
        
        assertEquals(100, circle.getX(), 0.001, "Circle X 좌표가 잘못되었습니다");
        assertEquals(100, circle.getY(), 0.001, "Circle Y 좌표가 잘못되었습니다");
        assertEquals(30, circle.getRadius(), 0.001, "Circle 반지름이 잘못되었습니다");
        
        // Shape로 타입 캐스팅 가능한지 확인
        Shape shape = circle;
        assertEquals("Circle", shape.getShapeType(), "Circle 타입이 올바르지 않습니다");
    }
    
    @Test
    public void testRectangleShapeCreation() {
        Rectangle rectangle = new Rectangle(50, 75, 100, 80);
        
        assertEquals(50, rectangle.getX(), 0.001, "Rectangle X 좌표가 잘못되었습니다");
        assertEquals(75, rectangle.getY(), 0.001, "Rectangle Y 좌표가 잘못되었습니다");
        assertEquals(100, rectangle.getWidth(), 0.001, "Rectangle 너비가 잘못되었습니다");
        assertEquals(80, rectangle.getHeight(), 0.001, "Rectangle 높이가 잘못되었습니다");
        
        Shape shape = rectangle;
        assertEquals("Rectangle", shape.getShapeType(), "Rectangle 타입이 올바르지 않습니다");
    }
    
    @Test
    public void testCircleArea() {
        Circle circle = new Circle(0, 0, 10);
        double expectedArea = Math.PI * 10 * 10;
        
        assertEquals(expectedArea, circle.getArea(), 0.001, "Circle 면적 계산이 잘못되었습니다");
    }
    
    @Test
    public void testRectangleArea() {
        Rectangle rectangle = new Rectangle(0, 0, 20, 15);
        double expectedArea = 20 * 15;
        
        assertEquals(expectedArea, rectangle.getArea(), 0.001, "Rectangle 면적 계산이 잘못되었습니다");
    }
    
    @Test
    public void testCirclePerimeter() {
        Circle circle = new Circle(0, 0, 10);
        double expectedPerimeter = 2 * Math.PI * 10;
        
        assertEquals(expectedPerimeter, circle.getPerimeter(), 0.001, "Circle 둘레 계산이 잘못되었습니다");
    }
    
    @Test
    public void testRectanglePerimeter() {
        Rectangle rectangle = new Rectangle(0, 0, 20, 15);
        double expectedPerimeter = 2 * (20 + 15);
        
        assertEquals(expectedPerimeter, rectangle.getPerimeter(), 0.001, "Rectangle 둘레 계산이 잘못되었습니다");
    }
    
    @Test
    public void testAbstractShapeCannotBeInstantiated() {
        // 추상 클래스는 직접 인스턴스화할 수 없음을 확인
        // 이는 컴파일 타임에 확인되므로 런타임 테스트로는 검증하기 어려움
        // 대신 하위 클래스가 올바르게 추상 메서드를 구현하는지 확인
        
        Circle circle = new Circle(0, 0, 10);
        assertTrue(circle instanceof Shape, "Circle은 Shape의 인스턴스여야 합니다");
        
        Rectangle rectangle = new Rectangle(0, 0, 10, 10);
        assertTrue(rectangle instanceof Shape, "Rectangle은 Shape의 인스턴스여야 합니다");
    }
    
    @Test
    public void testPolymorphism() {
        Shape[] shapes = {
            new Circle(0, 0, 10),
            new Rectangle(0, 0, 20, 15)
        };
        
        // 다형성을 통해 다양한 타입의 도형을 동일하게 처리
        for (Shape shape : shapes) {
            assertTrue(shape.getArea() > 0, "모든 도형의 면적은 양수여야 합니다");
            assertTrue(shape.getPerimeter() > 0, "모든 도형의 둘레는 양수여야 합니다");
            assertNotNull(shape.getShapeType(), "도형 타입이 null이면 안됩니다");
        }
    }
}
```

### Vector 클래스 테스트 (확장된 기능)

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class VectorExtendedTest {
    
    private Vector vector1;
    private Vector vector2;
    
    @BeforeEach
    public void setUp() {
        vector1 = new Vector(3.0, 4.0);
        vector2 = new Vector(1.0, 2.0);
    }
    
    @Test
    public void testVectorCreation() {
        assertEquals(3.0, vector1.getX(), 0.001, "Vector X 성분이 잘못되었습니다");
        assertEquals(4.0, vector1.getY(), 0.001, "Vector Y 성분이 잘못되었습니다");
    }
    
    @Test
    public void testPolarConstruction() {
        // 극좌표로 벡터 생성
        Vector polar = Vector.fromPolar(5.0, Math.PI / 2); // 크기 5, 각도 90도
        
        assertEquals(0.0, polar.getX(), 0.001, "극좌표 X 성분이 잘못되었습니다");
        assertEquals(5.0, polar.getY(), 0.001, "극좌표 Y 성분이 잘못되었습니다");
    }
    
    @Test
    public void testStaticFactoryMethods() {
        Vector zero = Vector.zero();
        assertEquals(0.0, zero.getX(), 0.001, "영벡터 X 성분이 0이 아닙니다");
        assertEquals(0.0, zero.getY(), 0.001, "영벡터 Y 성분이 0이 아닙니다");
        
        Vector unitX = Vector.unitX();
        assertEquals(1.0, unitX.getX(), 0.001, "단위 X 벡터의 X 성분이 1이 아닙니다");
        assertEquals(0.0, unitX.getY(), 0.001, "단위 X 벡터의 Y 성분이 0이 아닙니다");
        
        Vector unitY = Vector.unitY();
        assertEquals(0.0, unitY.getX(), 0.001, "단위 Y 벡터의 X 성분이 0이 아닙니다");
        assertEquals(1.0, unitY.getY(), 0.001, "단위 Y 벡터의 Y 성분이 1이 아닙니다");
    }
    
    @Test
    public void testVectorArithmetic() {
        Vector sum = vector1.add(vector2);
        assertEquals(4.0, sum.getX(), 0.001, "벡터 덧셈 X 성분이 잘못되었습니다");
        assertEquals(6.0, sum.getY(), 0.001, "벡터 덧셈 Y 성분이 잘못되었습니다");
        
        Vector diff = vector1.subtract(vector2);
        assertEquals(2.0, diff.getX(), 0.001, "벡터 뺄셈 X 성분이 잘못되었습니다");
        assertEquals(2.0, diff.getY(), 0.001, "벡터 뺄셈 Y 성분이 잘못되었습니다");
        
        Vector scaled = vector1.multiply(2.0);
        assertEquals(6.0, scaled.getX(), 0.001, "벡터 곱셈 X 성분이 잘못되었습니다");
        assertEquals(8.0, scaled.getY(), 0.001, "벡터 곱셈 Y 성분이 잘못되었습니다");
        
        Vector divided = vector1.divide(2.0);
        assertEquals(1.5, divided.getX(), 0.001, "벡터 나눗셈 X 성분이 잘못되었습니다");
        assertEquals(2.0, divided.getY(), 0.001, "벡터 나눗셈 Y 성분이 잘못되었습니다");
    }
    
    @Test
    public void testVectorMagnitudeAndDirection() {
        assertEquals(5.0, vector1.magnitude(), 0.001, "벡터 크기가 잘못되었습니다");
        
        double expectedAngle = Math.atan2(4.0, 3.0);
        assertEquals(expectedAngle, vector1.angle(), 0.001, "벡터 각도가 잘못되었습니다");
        
        Vector normalized = vector1.normalize();
        assertEquals(1.0, normalized.magnitude(), 0.001, "정규화된 벡터의 크기가 1이 아닙니다");
        assertEquals(0.6, normalized.getX(), 0.001, "정규화된 벡터 X 성분이 잘못되었습니다");
        assertEquals(0.8, normalized.getY(), 0.001, "정규화된 벡터 Y 성분이 잘못되었습니다");
    }
    
    @Test
    public void testDotAndCrossProduct() {
        double dotProduct = vector1.dot(vector2);
        assertEquals(11.0, dotProduct, 0.001, "내적 계산이 잘못되었습니다"); // 3*1 + 4*2 = 11
        
        double crossProduct = vector1.cross(vector2);
        assertEquals(2.0, crossProduct, 0.001, "외적 계산이 잘못되었습니다"); // 3*2 - 4*1 = 2
    }
    
    @Test
    public void testDistanceAndProjection() {
        double distance = vector1.distance(vector2);
        Vector diff = vector1.subtract(vector2);
        assertEquals(diff.magnitude(), distance, 0.001, "거리 계산이 잘못되었습니다");
        
        Vector projection = vector1.project(vector2);
        // v1 투영 v2 = (v1·v2/|v2|²) × v2
        double scalar = vector1.dot(vector2) / vector2.dot(vector2);
        Vector expected = vector2.multiply(scalar);
        assertEquals(expected.getX(), projection.getX(), 0.001, "투영 X 성분이 잘못되었습니다");
        assertEquals(expected.getY(), projection.getY(), 0.001, "투영 Y 성분이 잘못되었습니다");
    }
    
    @Test
    public void testVectorRotation() {
        Vector rotated90 = vector1.rotate(Math.PI / 2); // 90도 회전
        assertEquals(-4.0, rotated90.getX(), 0.001, "90도 회전 X 성분이 잘못되었습니다");
        assertEquals(3.0, rotated90.getY(), 0.001, "90도 회전 Y 성분이 잘못되었습니다");
        
        Vector rotated180 = vector1.rotate(Math.PI); // 180도 회전
        assertEquals(-3.0, rotated180.getX(), 0.001, "180도 회전 X 성분이 잘못되었습니다");
        assertEquals(-4.0, rotated180.getY(), 0.001, "180도 회전 Y 성분이 잘못되었습니다");
    }
    
    @Test
    public void testImmutability() {
        Vector original = new Vector(5.0, 6.0);
        Vector result = original.add(vector1);
        
        // 원본 벡터는 변경되지 않아야 함
        assertEquals(5.0, original.getX(), 0.001, "원본 벡터가 변경되었습니다");
        assertEquals(6.0, original.getY(), 0.001, "원본 벡터가 변경되었습니다");
        
        // 새로운 벡터가 반환되어야 함
        assertEquals(8.0, result.getX(), 0.001, "새 벡터 X 성분이 잘못되었습니다");
        assertEquals(10.0, result.getY(), 0.001, "새 벡터 Y 성분이 잘못되었습니다");
    }
    
    @Test
    public void testEqualsAndHashCode() {
        Vector vector3 = new Vector(3.0, 4.0);
        Vector vector4 = new Vector(3.1, 4.0);
        
        assertEquals(vector1, vector3, "동일한 성분의 벡터들이 같다고 판단되지 않았습니다");
        assertNotEquals(vector1, vector4, "다른 성분의 벡터들이 같다고 판단되었습니다");
        
        assertEquals(vector1.hashCode(), vector3.hashCode(), 
                    "동일한 벡터들의 해시코드가 다릅니다");
    }
    
    @Test
    public void testToString() {
        String vectorString = vector1.toString();
        assertTrue(vectorString.contains("3.0"), "toString에 X 성분이 포함되지 않았습니다");
        assertTrue(vectorString.contains("4.0"), "toString에 Y 성분이 포함되지 않았습니다");
    }
}
```

### AbstractBall 클래스 테스트 (Template Method 패턴)

```java
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

// AbstractBall을 상속받는 테스트용 구현체
class TestBall extends AbstractBall {
    private boolean updateCalled = false;
    private double lastDeltaTime;
    
    public TestBall(double x, double y, double radius) {
        super(x, y, radius);
    }
    
    @Override
    protected void performUpdate(double deltaTime) {
        this.updateCalled = true;
        this.lastDeltaTime = deltaTime;
        // 간단한 이동 로직
        setX(getX() + getDx() * deltaTime);
        setY(getY() + getDy() * deltaTime);
    }
    
    public boolean isUpdateCalled() {
        return updateCalled;
    }
    
    public double getLastDeltaTime() {
        return lastDeltaTime;
    }
    
    public void resetUpdateFlag() {
        updateCalled = false;
    }
}

public class AbstractBallTest {
    
    private TestBall ball;
    
    @BeforeEach
    public void setUp() {
        ball = new TestBall(100, 100, 20);
    }
    
    @Test
    public void testAbstractBallCreation() {
        assertEquals(100, ball.getX(), 0.001, "AbstractBall X 좌표가 잘못되었습니다");
        assertEquals(100, ball.getY(), 0.001, "AbstractBall Y 좌표가 잘못되었습니다");
        assertEquals(20, ball.getRadius(), 0.001, "AbstractBall 반지름이 잘못되었습니다");
        
        // 기본값 확인
        assertEquals(0.0, ball.getDx(), 0.001, "초기 X 속도가 0이 아닙니다");
        assertEquals(0.0, ball.getDy(), 0.001, "초기 Y 속도가 0이 아닙니다");
        assertEquals(Color.BLACK, ball.getColor(), "기본 색상이 BLACK이 아닙니다");
    }
    
    @Test
    public void testTemplateMethodPattern() {
        ball.setDx(50);
        ball.setDy(30);
        
        // Template Method인 update 호출
        ball.update(0.1);
        
        // performUpdate가 호출되었는지 확인
        assertTrue(ball.isUpdateCalled(), "performUpdate 메서드가 호출되지 않았습니다");
        assertEquals(0.1, ball.getLastDeltaTime(), 0.001, "deltaTime이 올바르게 전달되지 않았습니다");
        
        // 이동이 올바르게 수행되었는지 확인
        assertEquals(105, ball.getX(), 0.001, "Template Method를 통한 X 이동이 잘못되었습니다");
        assertEquals(103, ball.getY(), 0.001, "Template Method를 통한 Y 이동이 잘못되었습니다");
        
        // 후처리 확인 (충돌 체크, 경계 처리 등이 여기서 수행될 수 있음)
        // 이는 AbstractBall의 update 메서드에서 performUpdate 호출 후 수행
    }
    
    @Test
    public void testVelocityMethods() {
        ball.setVelocity(new Vector(75, 50));
        
        assertEquals(75, ball.getDx(), 0.001, "벡터 속도 설정 X가 잘못되었습니다");
        assertEquals(50, ball.getDy(), 0.001, "벡터 속도 설정 Y가 잘못되었습니다");
        
        Vector velocity = ball.getVelocity();
        assertEquals(75, velocity.getX(), 0.001, "벡터 속도 조회 X가 잘못되었습니다");
        assertEquals(50, velocity.getY(), 0.001, "벡터 속도 조회 Y가 잘못되었습니다");
    }
    
    @Test
    public void testBoundsIntegration() {
        Bounds bounds = ball.getBounds();
        
        assertTrue(bounds instanceof CircleBounds, "AbstractBall의 bounds는 CircleBounds여야 합니다");
        
        CircleBounds circleBounds = (CircleBounds) bounds;
        assertEquals(100, circleBounds.getCenterX(), 0.001, "Bounds 중심 X가 잘못되었습니다");
        assertEquals(100, circleBounds.getCenterY(), 0.001, "Bounds 중심 Y가 잘못되었습니다");
        assertEquals(20, circleBounds.getRadius(), 0.001, "Bounds 반지름이 잘못되었습니다");
    }
    
    @Test
    public void testMultipleUpdates() {
        ball.setDx(10);
        ball.setDy(5);
        
        // 여러 번 업데이트 수행
        for (int i = 0; i < 5; i++) {
            ball.resetUpdateFlag();
            ball.update(0.1);
            assertTrue(ball.isUpdateCalled(), "매번 performUpdate가 호출되어야 합니다");
        }
        
        // 최종 위치 확인
        assertEquals(105, ball.getX(), 0.001, "5번 업데이트 후 X 위치가 잘못되었습니다");
        assertEquals(102.5, ball.getY(), 0.001, "5번 업데이트 후 Y 위치가 잘못되었습니다");
    }
    
    @Test
    public void testHookMethods() {
        // Template Method 패턴에서 hook 메서드들이 있다면 테스트
        // 예: beforeUpdate, afterUpdate 등이 AbstractBall에 정의되어 있다면
        
        ball.update(0.1);
        
        // Hook 메서드들이 올바른 순서로 호출되는지 확인
        // 이는 실제 AbstractBall 구현에 따라 달라질 수 있음
        assertTrue(ball.isUpdateCalled(), "핵심 업데이트 로직이 호출되었습니다");
    }
}
```

### BoundsFactory 클래스 테스트 (Factory Method 패턴)

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BoundsFactoryTest {
    
    @Test
    public void testCreateCircleBounds() {
        Bounds bounds = BoundsFactory.createCircleBounds(100, 150, 25);
        
        assertTrue(bounds instanceof CircleBounds, "CircleBounds 타입이 생성되지 않았습니다");
        
        CircleBounds circle = (CircleBounds) bounds;
        assertEquals(100, circle.getCenterX(), 0.001, "Circle X 중심이 잘못되었습니다");
        assertEquals(150, circle.getCenterY(), 0.001, "Circle Y 중심이 잘못되었습니다");
        assertEquals(25, circle.getRadius(), 0.001, "Circle 반지름이 잘못되었습니다");
    }
    
    @Test
    public void testCreateRectangleBounds() {
        Bounds bounds = BoundsFactory.createRectangleBounds(50, 75, 120, 80);
        
        assertTrue(bounds instanceof RectangleBounds, "RectangleBounds 타입이 생성되지 않았습니다");
        
        RectangleBounds rect = (RectangleBounds) bounds;
        assertEquals(50, rect.getX(), 0.001, "Rectangle X가 잘못되었습니다");
        assertEquals(75, rect.getY(), 0.001, "Rectangle Y가 잘못되었습니다");
        assertEquals(120, rect.getWidth(), 0.001, "Rectangle 너비가 잘못되었습니다");
        assertEquals(80, rect.getHeight(), 0.001, "Rectangle 높이가 잘못되었습니다");
    }
    
    @Test
    public void testFactoryMethodPattern() {
        // Factory Method 패턴의 이점: 클라이언트 코드는 구체적인 클래스를 몰라도 됨
        Bounds[] bounds = {
            BoundsFactory.createCircleBounds(0, 0, 10),
            BoundsFactory.createRectangleBounds(0, 0, 20, 15)
        };
        
        // 다형성을 통해 동일하게 처리
        for (Bounds bound : bounds) {
            assertTrue(bound.getMinX() >= 0 || bound.getMinX() < 0, "Bounds가 올바르게 생성되었습니다");
            assertTrue(bound.getArea() > 0, "모든 Bounds의 면적은 양수여야 합니다");
        }
    }
    
    @Test
    public void testInvalidParameters() {
        // 유효하지 않은 매개변수에 대한 처리
        assertThrows(IllegalArgumentException.class, () -> {
            BoundsFactory.createCircleBounds(0, 0, -5); // 음수 반지름
        }, "음수 반지름에 대해 예외가 발생하지 않았습니다");
        
        assertThrows(IllegalArgumentException.class, () -> {
            BoundsFactory.createRectangleBounds(0, 0, -10, 5); // 음수 너비
        }, "음수 너비에 대해 예외가 발생하지 않았습니다");
        
        assertThrows(IllegalArgumentException.class, () -> {
            BoundsFactory.createRectangleBounds(0, 0, 10, -5); // 음수 높이
        }, "음수 높이에 대해 예외가 발생하지 않았습니다");
    }
    
    @Test
    public void testFactoryConsistency() {
        // 팩토리로 생성한 객체들이 일관된 행동을 보이는지 확인
        CircleBounds circle1 = new CircleBounds(50, 50, 20);
        Bounds circle2 = BoundsFactory.createCircleBounds(50, 50, 20);
        
        assertEquals(circle1.getArea(), circle2.getArea(), 0.001, 
                    "직접 생성과 팩토리 생성 결과가 다릅니다");
        assertEquals(circle1.getMinX(), circle2.getMinX(), 0.001,
                    "직접 생성과 팩토리 생성의 경계값이 다릅니다");
        
        assertTrue(circle1.intersects(circle2), "동일한 원들이 교차하지 않는다고 판단되었습니다");
    }
}
```

### 테스트 실행 가이드

```java
// 테스트 러너 예제 (필요시 사용)
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
    ShapeTest.class,
    VectorExtendedTest.class,
    AbstractBallTest.class,
    BoundsFactoryTest.class
})
public class Chapter5TestSuite {
    // 테스트 슈트 - 5장의 모든 테스트를 한 번에 실행
}
```

### 테스트 실행 및 검증 방법

1. **의존성 확인** (pom.xml):
   ```xml
   <dependency>
       <groupId>org.junit.jupiter</groupId>
       <artifactId>junit-jupiter</artifactId>
       <version>5.8.2</version>
       <scope>test</scope>
   </dependency>
   ```

2. **실행 명령어**:
   ```bash
   mvn test -Dtest="*Test"  # 모든 테스트 실행
   mvn test -Dtest="ShapeTest"  # 특정 테스트만 실행
   ```

3. **성공 기준**:
   - 모든 추상 메서드가 구현되었는지 확인
   - Template Method 패턴이 올바르게 작동하는지 확인
   - Factory Method 패턴이 일관된 객체를 생성하는지 확인
   - 다형성이 올바르게 작동하는지 확인

## 다음 장 미리보기

6장에서는 새로운 객체(Box)를 추가하며 상속만 사용할 때의 문제점을 확인합니다:
- Box 클래스 구현
- 다중 객체 타입 처리
- 상속의 한계 경험
- 인터페이스의 필요성 인식

## 추가 학습 자료

- [Abstract Data Types](https://en.wikipedia.org/wiki/Abstract_data_type)
- [Template Method Pattern](https://refactoring.guru/design-patterns/template-method)
- [Effective Java - 추상 클래스보다는 인터페이스를 우선하라](https://www.oreilly.com/library/view/effective-java/9780134686097/)

## 학습 체크포인트

- [ ] 추상 클래스의 개념을 이해했습니다
- [ ] Bounds 추상 타입을 구현했습니다
- [ ] Vector 클래스를 설계하고 구현했습니다
- [ ] Template Method 패턴을 적용했습니다
- [ ] 코드 재사용성이 향상되었습니다