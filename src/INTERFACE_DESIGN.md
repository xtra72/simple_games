# 인터페이스 기반 게임 설계

이 프로젝트는 상속과 인터페이스를 활용하여 재사용 가능하고 확장 가능한 게임 객체를 설계하는 방법을 보여줍니다.

## 핵심 설계 원칙

### 1. 인터페이스 분리 원칙
각 인터페이스는 하나의 책임만을 가집니다:
- `Movable`: 이동 가능한 객체
- `Collidable`: 충돌 가능한 객체
- `Boundable`: 경계를 가진 객체
- `Renderable`: 화면에 그릴 수 있는 객체

### 2. 상속과 조합
기본 클래스는 공통 속성을 제공하고, 인터페이스는 행동을 정의합니다:
```java
public class Ball extends GameObject implements Movable, Collidable, Boundable {
    // GameObject로부터 위치와 크기를 상속
    // 인터페이스로 이동, 충돌, 경계 처리 능력을 추가
}
```

## 아키텍처 구조

```
GameObject (추상 클래스)
├── Ball (Movable, Collidable, Boundable)
├── Box (Movable, Collidable, Boundable)
└── StaticObject (Collidable)

인터페이스
├── Renderable (모든 GameObject가 구현)
├── Movable (이동 가능한 객체)
├── Collidable (충돌 처리)
└── Boundable (경계 검사)
```

## 게임별 구현

### Chapter 8 - Breakout
- `Ball`: 공통 Ball 클래스를 상속받아 게임 특화
- `Paddle`: Box를 상속받아 패들 구현
- `Brick`: StaticObject를 상속받아 파괴 가능한 벽돌 구현
- `PowerUp`: Ball을 상속받아 떨어지는 파워업 구현

### Chapter 10 - Cannon
- `Projectile`: Ball을 상속받아 발사체 구현
- `Target`: Box를 상속받아 이동하는 타겟 구현
- 물리 효과는 Effect 인터페이스로 통일

## 장점

1. **재사용성**: 기본 클래스들을 다양한 게임에서 재사용
2. **확장성**: 새로운 기능은 인터페이스 추가로 쉽게 확장
3. **유지보수성**: 각 책임이 명확히 분리되어 수정이 용이
4. **다형성**: 인터페이스를 통한 다형적 처리 가능

## 예제: 새로운 게임 객체 만들기

```java
// 회전하는 상자 만들기
public class RotatingBox extends Box implements Rotatable {
    private double angularVelocity;
    
    @Override
    public void rotate(double deltaTime) {
        angle += angularVelocity * deltaTime;
    }
}

// 중력의 영향을 받는 정적 객체
public class FallingPlatform extends StaticObject implements Movable, Boundable {
    private double vy = 0;
    
    @Override
    public void move(double deltaTime) {
        y += vy * deltaTime;
        vy += GRAVITY * deltaTime;
    }
}
```

## 충돌 처리 시스템

모든 Collidable 객체는 다음을 구현합니다:
1. `collidesWith()`: 충돌 검사
2. `handleCollision()`: 충돌 처리
3. `getBounds()`: 경계 영역 반환

이를 통해 게임 로직에서 간단히 처리할 수 있습니다:
```java
if (object1.collidesWith(object2)) {
    object1.handleCollision(object2);
    object2.handleCollision(object1);
}
```

## 확장 가이드

새로운 기능이 필요할 때:
1. 기존 인터페이스로 표현 가능한지 확인
2. 불가능하다면 새 인터페이스 정의
3. 필요한 클래스에만 인터페이스 구현
4. 게임 로직에서 인터페이스로 처리

예: 회전 기능 추가
```java
public interface Rotatable {
    void rotate(double deltaTime);
    double getAngle();
    void setAngularVelocity(double velocity);
}
```