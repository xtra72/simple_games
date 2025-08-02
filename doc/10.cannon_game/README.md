# 10장: 대포 게임 (Cannon Game)

## 학습 목표

이 장을 완료하면 다음을 할 수 있습니다:
- 완전한 대포 게임을 구현할 수 있습니다
- 발사 각도와 힘을 조절하는 UI를 만들 수 있습니다
- 포물선 운동을 시뮬레이션할 수 있습니다
- 다양한 목표물과 장애물을 구현할 수 있습니다
- 게임의 모든 요소를 통합할 수 있습니다

## 핵심 개념

### 10.1 대포(Cannon) 구현

**Cannon 클래스 설계**

포탄을 발사하는 대포를 구현합니다:

**필드:**
- `x`, `y`: 대포 위치
- `angle`: 발사 각도 (라디안)
- `power`: 발사 힘 (100~1000)
- `barrelLength`, `barrelWidth`: 포신 크기
- `isCharging`: 충전 상태
- `chargeStartTime`: 충전 시작 시간

**핵심 메서드:**

1. **paint(GraphicsContext gc)**:
   - 대포 본체: 원형으로 그리기
   - 포신: 각도에 따라 회전
   - 충전 표시: 색상 변화
   - 조준선: 점선으로 표시
   - 파워 게이지: 하단에 표시

2. **충전 시스템**:
   - `startCharging()`: 충전 시작
   - `stopCharging()`: 충전 종료 및 파워 계산
   - `getChargeLevel()`: 0~1 범위의 충전 레벨
   - 최대 충전 시간: 2초

3. **fire() 메서드**:
   - 포신 끝 위치 계산
   - 각도와 파워로 초기 속도 계산
   - Projectile 객체 생성 및 반환

4. **조작 메서드**:
   - `adjustAngle(double delta)`: 각도 조절 (-90° ~ 0°)
   - `setPower(double power)`: 파워 설정 (100~1000)

**구현 힌트:**
```java
// 포신 그리기
gc.translate(x, y);  // 대포 중심으로 이동
gc.rotate(Math.toDegrees(angle));  // 각도만큼 회전

// 충전 레벨에 따른 색상
빨간색 성분 = 255 * chargeLevel
초록색 성분 = 255 * (1 - chargeLevel)
```

### 10.2 포탄(Projectile) 구현

**Projectile 클래스 설계**

PhysicsBall을 상속받아 물리 법칙이 적용되는 포탄:

**ProjectileType 열거형:**
- NORMAL: 일반 포탄 (검정, 1.0x 질량, 50 폭발반경)
- HEAVY: 무거운 포탄 (회색, 2.0x 질량, 70 폭발반경)
- EXPLOSIVE: 폭발 포탄 (빨강, 1.0x 질량, 100 폭발반경)
- SCATTER: 산탄 (파랑, 0.8x 질량, 30 폭발반경)
- BOUNCY: 탄성 포탄 (초록, 1.0x 질량, 40 폭발반경)

**필드:**
- `trajectory`: 궤적 저장 리스트
- `type`: 포탄 유형
- `hasExploded`: 폭발 여부

**핵심 메서드:**

1. **생성자**:
   - 기본 반지름 8로 설정
   - 초기 속도 설정
   - 타입별 특성 적용 (색상, 질량, 반발계수)

2. **update(double deltaTime)**:
   - 현재 위치를 궤적에 추가
   - 궤적 크기 제한 (100개)
   - 화면 밖 검사 및 제거

3. **paint(GraphicsContext gc)**:
   - 궤적 그리기 (반투명)
   - 포탄 본체 그리기
   - 폭발 효과 표시

4. **handleCollision(Collidable other)**:
   - 목표물/장애물 충돌 → 폭발
   - 산탄은 충돌 시 분열
   - 탄성 포탄은 바닥에서 반사

5. **특수 기능**:
   - `explode()`: 폭발 처리
   - `scatter()`: 5개의 작은 포탄으로 분열

**구현 힌트:**
```java
// 궤적 그리기
for (i = 1; i < size; i++) {
    이전점과 현재점을 연결
}

// 산탄 분열
각도 = -90° ~ +90° 랜덤
속도 = 200~300 랜덤
```

### 10.3 목표물(Target) 구현

**Target 클래스 설계**

파괴할 목표물을 구현합니다:

**TargetType 열거형:**
- WOODEN: 나무 상자 (50점, 갈색, 내구도 1.0)
- STONE: 돌 상자 (100점, 회색, 내구도 2.0)
- METAL: 금속 상자 (200점, 은색, 내구도 3.0)
- GLASS: 유리 상자 (75점, 하늘색, 내구도 0.5)
- TNT: 폭발물 (300점, 빨강, 내구도 1.0)

**필드:**
- `x`, `y`, `width`, `height`: 위치와 크기
- `points`: 파괴 시 획듍 점수
- `type`: 목표물 유형
- `health`: 현재 내구도
- `isDestroyed`: 파괴 여부

**핵심 메서드:**

1. **paint(GraphicsContext gc)**:
   - 손상도에 따른 색상 변화
   - 테두리 그리기
   - TNT는 텍스트 표시
   - 손상 50% 이하 시 균열 표시

2. **drawCracks() (private)**:
   - 랜덤 균열 패턴 생성
   - 위치를 시드로 사용 (일관된 패턴)

3. **handleCollision(Collidable other)**:
   - 포탄과 충돌 시 데미지
   - TNT는 파괴 시 폭발

4. **takeDamage(double damage)**:
   - 체력 감소
   - 0 이하 시 파괴

5. **triggerExplosion()** (private):
   - 연쇄 폭발 효과
   - 주변 객체 피해 (구현 필요)

**구현 힌트:**
```java
// 손상 정도에 따른 색상
healthRatio = 현재체력 / 최대체력
색상밝기 = healthRatio

// 랜덤 균열
시드 = x * y  // 위치 기반
```

### 10.4 게임 월드 통합

**CannonGameWorld 클래스 설계**

PhysicsWorld를 상속받아 모든 게임 요소를 통합:

**GameMode 열거형:**
- CLASSIC: 모든 목표물 파괴
- TIME_ATTACK: 60초 내 최고 점수
- LIMITED_SHOTS: 10발로 최고 점수
- PUZZLE: 특정 순서로 파괴

**필드 설계:**
- `cannon`: 대포 객체
- `projectiles`: 활성 포탄 리스트
- `targets`: 목표물 리스트
- `obstacles`: 장애물 리스트
- `score`: 현재 점수
- `shotsRemaining`: 남은 포탄 수
- `gameMode`: 게임 모드
- `currentLevel`: 현재 레벨

**구현해야 할 핵심 메서드:**

1. **loadLevel(Level level)**:
   - 레벨 데이터로부터 객체 생성하기
   - 목표물과 장애물 적절히 배치하기
   - 게임 모드에 따른 포탄 수 설정하기

2. **fire()**:
   - 대포의 현재 상태로부터 포탄 생성하기
   - 생성된 포탄을 물리 엔진에 추가하기
   - 남은 포탄 수 감소시키기
   - 발사 불가능한 경우 처리하기

3. **update(double deltaTime)** 오버라이드:
   - 부모 클래스의 update 호출하기
   - 활성 포탄들의 폭발 상태 확인하기
   - 파괴된 객체들을 리스트에서 제거하기
   - 점수 계산 및 업데이트하기
   - 게임 종료 조건 확인하기

4. **handleExplosion(Projectile projectile)**:
   - 폭발 위치와 범위 계산하기
   - 범위 내의 모든 목표물 찾기
   - 거리에 따른 피해량 계산하기 (힌트 참조)
   - 각 목표물에 피해 적용하기
   - 시각적 폭발 효과 추가하기 (선택사항)

5. **checkWinCondition()**:
   - 현재 게임 모드 확인하기
   - 각 모드별 승리/패배 조건 체크하기
   - 게임 상태 업데이트하기
   - 승리/패배 시 적절한 처리하기

**UI 시스템 구현:**
- HBox 또는 VBox로 UI 패널 구성하기
- Label로 점수와 남은 포탄 표시하기
- 게임 모드 이름 표시하기
- 실시간으로 업데이트되도록 바인딩하기

**구현 힌트:**
```java
// 폭발 피해 계산
거리 = √((x₂-x₁)² + (y₂-y₁)²)
피해비율 = 1 - (거리 / 폭발반경)
피해량 = 피해비율 × 2
```

### 10.5 게임 컨트롤러

**CannonGame 클래스 설계**

게임의 메인 컨트롤러 클래스입니다. Application을 상속받아 구현하세요:

**필드 설계:**
- `world`: CannonGameWorld 객체
- `primaryStage`: Stage (메인 윈도우)
- `gameLoop`: Timeline (게임 루프)
- `canvas`: Canvas (게임 화면)
- `gc`: GraphicsContext

**구현해야 할 핵심 메서드:**

1. **start(Stage primaryStage)** 오버라이드:
   - Stage 저장하기
   - showMainMenu() 호출하기

2. **showMainMenu()**:
   - Scene 생성하기 (800x600)
   - VBox로 메뉴 레이아웃 구성하기
   - 게임 제목 Label 추가하기
   - 버튼들 생성 및 이벤트 핸들러 연결하기:
     * "게임 시작" → startGame()
     * "레벨 선택" → showLevelSelect()
     * "설정" → showSettings()
     * "종료" → Platform.exit()
   - 중앙 정렬 설정하기

3. **startGame()**:
   - CannonGameWorld 객체 생성하기
   - 첫 번째 레벨 로드하기
   - BorderPane으로 레이아웃 구성하기
   - Canvas를 중앙에 배치하기
   - 컨트롤 패널을 하단에 배치하기
   - 입력 이벤트 핸들러 등록하기
   - 게임 루프 시작하기

4. **setupInputHandlers(Scene scene)**:
   - 마우스 이벤트 처리하기:
     * setOnMousePressed: 대포 충전 시작
     * setOnMouseReleased: 충전 종료 및 발사
     * setOnMouseMoved: 대포 각도 조정
   - 키보드 이벤트 처리하기:
     * setOnKeyPressed: handleKeyPress() 호출

5. **createControlPanel()**:
   - HBox 생성하여 컨트롤들 배치하기
   - ComboBox<ProjectileType> 생성하기
   - Slider로 파워 조절기 만들기 (100~1000)
   - Label로 현재 각도 표시하기
   - Button으로 발사 버튼 만들기
   - 각 컨트롤에 이벤트 리스너 연결하기

6. **handleKeyPress(KeyCode code)**:
   - switch문으로 키 처리하기
   - UP/DOWN: 대포 각도 조절하기
   - LEFT/RIGHT: 발사 파워 조절하기
   - SPACE: 발사하기
   - ESCAPE: 일시정지 토글하기

7. **createGameLoop()**:
   - Timeline 생성하기
   - KeyFrame으로 60 FPS 설정하기 (Duration.millis(16))
   - 매 프레임마다:
     * deltaTime 계산하기
     * world.update(deltaTime) 호출하기
     * world.render(gc) 호출하기
   - Timeline.INDEFINITE로 무한 반복 설정하기

**구현 힌트:**
```java
// 마우스 위치로 각도 계산
angle = atan2(dy, dx)

// Timeline 게임 루프
new KeyFrame(Duration.millis(16), e -> {
    // 60 FPS 업데이트
})
```

## 실습 과제

### Lab 10-1: 기본 대포 게임
핵심 기능 구현:
- 대포 조작 (각도, 파워)
- 포물선 운동
- 목표물 파괴
- 점수 시스템

### Lab 10-2: 다양한 포탄
특수 포탄 구현:
- 분열탄
- 유도탄
- 시간 지연 폭탄
- 연막탄

### Lab 10-3: 레벨 디자인

**LevelDesigner 클래스 설계**

창의적인 레벨을 만들 수 있는 레벨 디자이너를 구현하세요:

**구현 요구사항:**
1. **구조물 패턴**:
   - 피라미드 형태로 상자 쌓기
   - 아치 구조 만들기
   - 탑 형태 구조물 만들기

2. **도미노 효과**:
   - 연쇄적으로 무너지는 구조 설계하기
   - TNT 상자를 전략적으로 배치하기
   - 하나의 샷으로 모든 목표물 파괴 가능하게 하기

3. **퍼즐 요소**:
   - 특정 순서로만 파괴 가능한 구조 만들기
   - 숨겨진 목표물 배치하기
   - 반사를 이용해야 하는 레벨 설계하기

4. **레벨 저장/불러오기**:
   - JSON 또는 XML 형식으로 레벨 데이터 저장하기
   - 파일에서 레벨 불러오기 기능 구현하기
   - 레벨 검증 기능 추가하기

### Lab 10-4: 멀티플레이어
2인용 대전 모드:
- 턴제 대전
- 동시 대전
- 방어/공격 모드

## JUnit 테스트 예제

```java
public class CannonGameTest {
    
    @Test
    public void testProjectileTrajectory() {
        Projectile projectile = new Projectile(0, 0, 100, -100);
        
        // 초기 속도
        assertEquals(100, projectile.getVelocity().getX(), 0.001);
        assertEquals(-100, projectile.getVelocity().getY(), 0.001);
        
        // 중력 적용
        Gravity gravity = new Gravity(500);
        gravity.apply(projectile, 0.1);
        projectile.update(0.1);
        
        // y 속도가 증가 (아래 방향)
        assertTrue(projectile.getVelocity().getY() > -100);
    }
    
    @Test
    public void testCannonAngleLimits() {
        Cannon cannon = new Cannon(100, 500);
        
        // 최대 각도 제한
        cannon.adjustAngle(-Math.PI); // 큰 음수
        assertTrue(cannon.getAngle() >= -Math.PI/2);
        
        // 최소 각도 제한
        cannon.adjustAngle(Math.PI); // 큰 양수
        assertTrue(cannon.getAngle() <= 0);
    }
    
    @Test
    public void testTargetDamage() {
        Target target = new Target(100, 100, 50, 50, TargetType.WOODEN);
        
        assertFalse(target.isDestroyed());
        
        target.takeDamage(1.0); // 나무는 내구도 1.0
        
        assertTrue(target.isDestroyed());
        assertEquals(50, target.getPoints());
    }
    
    @Test
    public void testExplosionDamage() {
        CannonGameWorld world = new CannonGameWorld(800, 600);
        Target target = new Target(200, 200, 50, 50, TargetType.STONE);
        world.addTarget(target);
        
        // 근처에서 폭발
        Projectile explosive = new Projectile(225, 225, 0, 0);
        explosive.setType(ProjectileType.EXPLOSIVE);
        explosive.explode();
        
        world.handleExplosion(explosive);
        
        // 돌은 내구도가 높아서 한 번에 파괴되지 않음
        assertFalse(target.isDestroyed());
        assertTrue(target.getHealth() < 2.0); // 손상은 입음
    }
}
```

## 자가 평가 문제

1. **포물선 운동의 특징은?**
   - 수평: 등속 운동
   - 수직: 등가속 운동
   - 최고점에서 수직 속도 = 0

2. **발사각 45도가 최대 사거리인 이유는?**
   - 수평/수직 속도 성분의 최적 균형
   - 공기 저항이 없을 때만 성립

3. **게임 밸런스를 맞추는 방법은?**
   - 난이도 곡선 조절
   - 보상과 도전의 균형
   - 다양한 전략 허용

4. **물리 시뮬레이션의 한계는?**
   - 계산 정밀도
   - 성능 제약
   - 게임성과의 타협

## 자주 하는 실수와 해결 방법

### 1. 발사 각도 계산
```java
// 잘못된 코드 - 좌표계 혼동
double angle = Math.atan2(dx, dy); // x와 y가 바뀜

// 올바른 코드
double angle = Math.atan2(dy, dx); // y가 먼저
```

### 2. 충돌 폭발 중복
```java
// 문제 - 같은 포탄이 여러 번 폭발
if (projectile.hasExploded()) {
    return; // 이미 폭발했으면 무시
}
```

### 3. 메모리 누수
```java
// 궤적을 무한히 저장
trajectory.add(position);

// 해결 - 크기 제한
if (trajectory.size() > MAX_TRAJECTORY_POINTS) {
    trajectory.remove(0);
}
```

## 과정 마무리

축하합니다! 10장의 과정을 모두 완료했습니다.

**학습한 내용:**
- 객체 지향 프로그래밍의 핵심 개념
- 상속과 인터페이스의 적절한 사용
- 게임 물리와 충돌 처리
- JavaFX를 이용한 그래픽 프로그래밍
- 완전한 게임 개발 과정

**다음 단계:**
- 자신만의 게임 만들기
- 3D 그래픽 도전
- 네트워크 멀티플레이어
- 모바일 게임 개발

## 추가 학습 자료

- [Game Development Patterns and Best Practices](https://www.packtpub.com/product/game-development-patterns-and-best-practices/9781787127838)
- [Physics for Game Developers](https://www.oreilly.com/library/view/physics-for-game/9781449392512/)
- [JavaFX Game Development Framework](https://github.com/AlmasB/FXGL)

## 최종 체크포인트

- [ ] 완전한 대포 게임을 구현했습니다
- [ ] 다양한 게임 모드를 추가했습니다
- [ ] 레벨 시스템을 구현했습니다
- [ ] 물리 엔진을 통합했습니다
- [ ] 게임 개발의 전 과정을 경험했습니다