# 9장: 외부 효과 (External Effects)

## 학습 목표

이 장을 완료하면 다음을 할 수 있습니다:
- 물리 시뮬레이션을 구현할 수 있습니다
- 중력, 바람, 마찰력을 적용할 수 있습니다
- 반발 계수를 이용한 현실적인 충돌을 구현할 수 있습니다
- 외부 힘을 관리하는 시스템을 설계할 수 있습니다
- 게임에 현실감을 더할 수 있습니다

## 핵심 개념

### 9.1 물리 엔진 기초

**물리 시뮬레이션의 핵심 개념**

물리 엔진은 힘, 속도, 가속도를 관리하여 현실적인 움직임을 구현합니다:

1. **뉴턴의 제2법칙**: F = ma (힘 = 질량 × 가속도)
2. **속도와 가속도**: 속도 = 속도 + 가속도 × 시간
3. **위치 업데이트**: 위치 = 위치 + 속도 × 시간

**PhysicsObject 인터페이스 설계**

물리 법칙이 적용되는 객체를 위한 인터페이스:

**메서드:**
- `getPosition()`: 현재 위치
- `getVelocity()`, `setVelocity()`: 속도 관리
- `getMass()`: 질량 반환
- `applyForce(Vector2D force)`: 힘 적용
- `update(double deltaTime)`: 물리 업데이트

**PhysicsEngine 클래스 설계**

물리 시스템을 관리하는 엔진:

**필드:**
- `globalForces`: 모든 객체에 적용되는 힘들 (중력, 바람 등)
- `objects`: 물리 객체 리스트
- `airDensity`: 공기 밀도 (1.2 kg/m³)

**핵심 메서드:**
- `addGlobalForce()`: 전역 힘 추가
- `addObject()`: 물리 객체 추가
- `update()`: 모든 힘 적용 후 객체 업데이트

**구현 힌트:**
```java
// update 메서드 순서
1. 모든 객체에 힘 적용
2. 각 객체의 update() 호출
3. 충돌 검사 및 처리
```

### 9.2 중력 구현

**Gravity 클래스 설계**

중력을 구현하는 Force 구현체:

```java
public class Gravity implements Force {
    // 구현
}
```

**필드 (private):**
- `gravity`: double (중력가속도 g)
- `direction`: Vector2D (방향, 보통 (0, 1) 아래쪽)

**구현 원리:**
- F = m × g (중력 = 질량 × 중력가속도)
- 모든 객체에 같은 가속도 적용
- 질량에 비례하는 힘

**메서드:**
- `apply(PhysicsObject obj, double deltaTime)`: 중력 힘 적용

**PhysicsBall 클래스 설계**

물리 법칙이 적용되는 공:

```java
public class PhysicsBall extends Ball implements PhysicsObject {
    // 구현
}
```

**추가 필드 (private):**
- `velocity`: Vector2D (속도 벡터)
- `acceleration`: Vector2D (가속도 벡터)
- `mass`: double (질량)
- `restitution`: double (반발 계수, 0~1)

**핵심 메서드 구현:**

1. **applyForce(Vector2D force)**:
   ```java
   // F = ma ⇒ a = F/m
   // 힘을 질량으로 나눠 가속도 계산
   // acceleration에 누적
   ```

2. **update(double deltaTime)**:
   ```java
   // 1. 속도 업데이트: v = v + a × Δt
   // 2. 위치 업데이트: p = p + v × Δt
   // 3. 가속도 초기화: a = (0, 0) // 중요!
   ```

3. **handleCollision(Collidable other)**:
   ```java
   // 바닥과 충돌 시
   // 반발 계수 적용: v' = -v × e
   // 속도가 작으면 0으로 설정
   ```

**반발 계수(Coefficient of Restitution)**:
- 0: 완전 비탄성 충돌 (끈적임)
- 0.5: 에너지 50% 손실
- 0.8: 일반적인 공
- 1.0: 완전 탄성 충돌 (에너지 보존)

**구현 힌트:**
```java
// 질량 계산 (밀도 × 부피)
// 공의 부피 = (4/3) × π × r³
// 2D에서는 면적 사용: π × r²

// 충돌 후 속도
if (velocity.getY() > 0 && 바닥과 충돌) {
    velocity.setY(-velocity.getY() * restitution);
}
```

### 9.3 바람 효과

**Wind 클래스 설계**

바람의 효과를 구현하는 Force:

**필드:**
- `windVelocity`: 바람의 속도 벡터
- `strength`: 바람의 세기
- `noise`: 자연스러운 변화를 위한 노이즈 생성기

**물리 원리:**
- **항력(Drag Force)**: F = 0.5 × ρ × A × Cd × v²
  - ρ: 공기 밀도 (1.2 kg/m³)
  - A: 단면적
  - Cd: 항력 계수 (0.47 for sphere)
  - v: 상대 속도

**구현 특징:**
1. **상대 속도 계산**: 바람 속도 - 객체 속도
2. **노이즈 적용**: 자연스러운 바람 변화
3. **방향 변경**: setDirection() 메서드

**NoiseGenerator 클래스 설계**

Perlin Noise를 간단히 구현:

**주요 메서드:**
- `getValue(double t)`: 시간 t에서의 노이즈 값
- `smoothstep()`: 부드러운 보간
- `getOrGenerate()`: 값 생성 및 캐싱

**노이즈 특징:**
- -1 ~ 1 범위의 값
- 시간에 따라 부드럽게 변화
- 캐싱으로 성능 최적화

**구현 힌트:**
```java
// 바람 세기 변화
현재바람 = 기본바람 × (1 + 노이즈 × 0.3)

// smoothstep 함수
// t²(3-2t)로 부드러운 곡선
```

### 9.4 마찰력 구현

**Friction 클래스 설계**

바닥과의 마찰력을 구현:

**마찰력의 종류:**
1. **정지 마찰**: 물체가 움직이기 시작할 때까지의 마찰
2. **운동 마찰**: 물체가 움직이는 동안의 마찰

**필드:**
- `staticCoefficient`: 정지 마찰 계수 (보통 더 큼)
- `kineticCoefficient`: 운동 마찰 계수
- `threshold`: 정지 판단 임계값

**물리 원리:**
- 마찰력 = μ × N
- μ: 마찰 계수
- N: 수직항력 (여기서는 mg)

**일반적인 마찰 계수:**
- 고무-콘크리트: 0.7 (운동), 1.0 (정지)
- 나무-나무: 0.3 (운동), 0.5 (정지)
- 얼음-얼음: 0.03 (운동), 0.1 (정지)

**AirResistance 클래스 설계**

공기 저항을 구현:

**특징:**
- 속도의 제곱에 비례
- 항상 속도와 반대 방향
- 단면적에 비례

**항력 계수 예시:**
- 구: 0.47
- 자동차: 0.3
- 큰 트럭: 0.8
- 매끄러운 구: 0.1

**구현 힌트:**
```java
// 바닥 접촉 확인
if (y속도 ≈ 0 && y위치 ≈ 바닥) {
    // 마찰력 적용
}

// 공기 저항은 항상 적용
```

### 9.5 반발 계수와 충돌

**ElasticCollision 클래스 설계**

탄성 충돌을 처리하는 클래스:

**충돌 처리 단계:**
1. **충돌 방향 계산**: 두 공의 중심을 잇는 벡터
2. **상대 속도 계산**: 충돌 방향의 속도 차이
3. **충격량 계산**: 운동량 보존 법칙 적용
4. **속도 업데이트**: 새로운 속도 계산
5. **위치 보정**: 겹침 해결

**물리 원리:**
- **운동량 보존**: m₁v₁ + m₂v₂ = m₁v₁' + m₂v₂'
- **반발 계수**: e = (상대 분리 속도) / (상대 접근 속도)
- **충격량**: J = (1+e) × v_rel / (1/m₁ + 1/m₂)

**resolveCollision 메서드 구현:**
1. 충돌 방향 벡터 계산
2. 멀어지고 있으면 무시
3. 반발 계수 평균 계산
4. 충격량 계산 및 적용
5. 겹침 해결

**Material 열거형:**

다양한 재질의 반발 계수:
- RUBBER(0.9): 고무 - 높은 탄성
- STEEL(0.8): 강철 - 단단함
- WOOD(0.6): 나무 - 중간 탄성
- GLASS(0.7): 유리 - 깨지기 쉬움
- CLAY(0.2): 점토 - 비탄성
- SUPER_BALL(0.95): 슈퍼볼 - 거의 완전 탄성

**구현 힌트:**
```java
// 겹침 해결
overlap = r₁ + r₂ - distance
if (overlap > 0) {
    각 공을 overlap/2만큼 밀어냄
}

// 멀어지고 있는지 확인
if (v_rel · n <= 0) return;
```

### 9.6 통합 물리 시뮬레이션

**PhysicsWorld 클래스 설계**

모든 물리 효과를 통합한 World:

**필드:**
- `physicsEngine`: 물리 엔진
- `gravityEnabled`: 중력 활성화 여부
- `windEnabled`: 바람 활성화 여부
- `frictionEnabled`: 마찰 활성화 여부

**기본 물리 설정:**
- 중력: 500 pixels/s² (지구 중력의 약 절반)
- 공기 저항: 항력 계수 0.47 (구)
- 마찰: 정지 0.5, 운동 0.3

**PhysicsDemo 클래스 설계**

물리 시뮬레이션 데모 애플리케이션:

**UI 컨트롤:**
1. **중력 슬라이더**: 0~1000 pixels/s²
2. **바람 각도**: 0~360도
3. **바람 세기**: 0~200 pixels/s
4. **재질 선택**: 다양한 반발 계수
5. **공 생성 버튼**: 랜덤 위치에 생성

**createBall() 메서드:**
- 랜덤 위치 (x: 50~750)
- 상단에서 시작 (y: 50)
- 랜덤 크기 (10~30)
- 랜덤 초기 속도
- 선택된 재질 적용

**게임 루프 패턴:**
1. 델타 타임 계산
2. 물리 엔진 업데이트
3. 월드 업데이트 (충돌 검사)
4. 화면 렌더링

**구현 힌트:**
```java
// 동적 물리 효과 변경
if (gravityEnabled) {
    physicsEngine.setGravity(value);
} else {
    physicsEngine.setGravity(0);
}

// 실시간 조절
slider.valueProperty().addListener(...)
```

## 실습 과제

### Lab 9-1: 중력 시뮬레이션
다양한 중력 환경 구현:
- 지구, 달, 화성의 중력
- 역중력 모드
- 중력 방향 변경

### Lab 9-2: 바람 시뮬레이터
현실적인 바람 효과:
- 돌풍 (갑작스런 바람)
- 회오리 바람
- 바람 시각화 (파티클)

### Lab 9-3: 재질 실험실
다양한 재질의 물리적 특성:
```java
public class MaterialLab {
    // 다양한 재질의 공 생성
    // 동일 높이에서 떨어뜨려 반발 높이 비교
    // 마찰 계수에 따른 이동 거리 측정
}
```

### Lab 9-4: 복합 물리 효과
여러 효과를 조합한 시뮬레이션:
- 수중 환경 (부력 + 높은 항력)
- 우주 환경 (무중력 + 관성)
- 자기장 효과

## JUnit 테스트 예제

```java
public class PhysicsTest {
    
    @Test
    public void testGravity() {
        PhysicsBall ball = new PhysicsBall(100, 100, 20);
        Gravity gravity = new Gravity(10);
        
        double initialY = ball.getY();
        
        // 1초 동안 자유낙하
        for (int i = 0; i < 100; i++) {
            gravity.apply(ball, 0.01);
            ball.update(0.01);
        }
        
        // s = 0.5 * g * t²
        double expectedDistance = 0.5 * 10 * 1 * 1;
        assertEquals(initialY + expectedDistance, ball.getY(), 1.0);
    }
    
    @Test
    public void testRestitution() {
        PhysicsBall ball = new PhysicsBall(100, 100, 20);
        ball.setRestitution(0.8);
        ball.setVelocity(new Vector2D(0, 100));
        
        // 바닥과 충돌
        Floor floor = new Floor(0, 200, 800);
        ball.handleCollision(floor);
        
        // 속도가 80%로 감소
        assertEquals(-80, ball.getVelocity().getY(), 0.1);
    }
    
    @Test
    public void testEnergyConservation() {
        PhysicsBall ball = new PhysicsBall(100, 100, 20);
        ball.setRestitution(1.0); // 완전 탄성
        
        double initialEnergy = calculateEnergy(ball);
        
        // 시뮬레이션 실행
        // ...
        
        double finalEnergy = calculateEnergy(ball);
        
        // 에너지 보존 (약간의 오차 허용)
        assertEquals(initialEnergy, finalEnergy, initialEnergy * 0.01);
    }
    
    private double calculateEnergy(PhysicsBall ball) {
        // E = mgh + 0.5mv²
        double potentialEnergy = ball.getMass() * 10 * (600 - ball.getY());
        double kineticEnergy = 0.5 * ball.getMass() * 
                              ball.getVelocity().magnitude() * 
                              ball.getVelocity().magnitude();
        return potentialEnergy + kineticEnergy;
    }
}
```

## 자가 평가 문제

1. **반발 계수가 1보다 큰 경우는?**
   - 에너지가 추가되는 경우
   - 현실에서는 불가능
   - 게임에서는 특수 효과로 사용

2. **공기 저항이 속도의 제곱에 비례하는 이유는?**
   - 유체역학의 법칙
   - 높은 속도에서 난류 발생

3. **정지 마찰과 운동 마찰의 차이는?**
   - 정지 마찰이 더 큼
   - 물체가 움직이기 시작하면 감소

4. **중력 가속도를 픽셀 단위로 사용하는 이유는?**
   - 게임 좌표계에 맞춤
   - 직관적인 조절 가능

## 자주 하는 실수와 해결 방법

### 1. 힘의 누적
```java
// 잘못된 코드 - 힘이 계속 누적됨
acceleration = acceleration.add(force);

// 올바른 코드 - 매 프레임 초기화
public void update(double deltaTime) {
    velocity = velocity.add(acceleration.multiply(deltaTime));
    position = position.add(velocity.multiply(deltaTime));
    acceleration = new Vector2D(0, 0); // 초기화!
}
```

### 2. 단위 혼동
```java
// 주의 - 실제 물리 단위와 게임 단위 구분
double gravity = 9.81;  // m/s² (실제)
double gravity = 500;   // pixels/s² (게임)
```

### 3. 에너지 증가
```java
// 문제 - 반발 계수 > 1이면 에너지 증가
if (restitution > 1.0) {
    restitution = 1.0; // 제한
}
```

## 다음 장 미리보기

10장에서는 대포 게임을 완성합니다:
- 발사 각도와 힘 조절
- 목표물과 장애물
- 점수 시스템
- 다양한 포탄 종류

## 추가 학습 자료

- [Nature of Code - Physics](https://natureofcode.com/book/chapter-2-forces/)
- [Game Physics Engine Development](https://www.ian-millington.com/books/game-physics-engine-development)
- [Box2D Physics Engine](https://box2d.org/)

## 학습 체크포인트

- [ ] 중력을 구현했습니다
- [ ] 바람 효과를 추가했습니다
- [ ] 마찰력을 적용했습니다
- [ ] 반발 계수를 이용한 충돌을 구현했습니다
- [ ] 물리 엔진을 설계했습니다