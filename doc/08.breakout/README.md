# 8장: 벽돌 깨기 (Breakout)

## 학습 목표

이 장을 완료하면 다음을 할 수 있습니다:
- 완전한 벽돌 깨기 게임을 구현할 수 있습니다
- 게임 상태 관리 시스템을 설계할 수 있습니다
- 점수와 생명 시스템을 구현할 수 있습니다
- 파워업과 특수 효과를 추가할 수 있습니다
- 사용자 입력을 처리하고 게임플레이를 제어할 수 있습니다

## 핵심 개념

### 8.1 게임 구성 요소

**벽돌 깨기 게임의 주요 구성 요소**

이 장에서는 2~7장에서 설계한 클래스와 인터페이스를 최대한 활용하여 완전한 게임을 구현합니다:

1. **패들(Paddle)**: Box 클래스를 확장하여 구현
2. **공(Ball)**: Ball 클래스를 확장하여 구현
3. **벽돌(Brick)**: StaticObject를 확장하고 인터페이스 구현
4. **파워업(PowerUp)**: Ball 클래스를 확장하여 떨어지는 아이템 구현
5. **게임 벽**: UnbreakableBrick으로 게임 공간 정의
6. **특수 효과**: 인터페이스로 정의 (Breakable, MultiHit, Exploding, PowerUpProvider)

**BreakoutGame 클래스 설계**

**필드:**
- `paddle`: 패들 객체
- `ball`: 공 객체 (나중에 여러 개 가능)
- `bricks`: 벽돌 리스트
- `powerUps`: 활성 파워업 리스트
- `gameState`: 현재 게임 상태
- `score`: 현재 점수
- `lives`: 남은 생명
- `level`: 현재 레벨

**상수:**
- `INITIAL_LIVES`: 시작 생명 수 (3)
- `POINTS_PER_BRICK`: 벽돌당 기본 점수 (10)
- `BALL_SPEED_INCREMENT`: 레벨당 속도 증가율 (1.1)

### 8.2 패들(Paddle) 구현

**BreakoutPaddle 클래스 설계**

Box 클래스를 확장하여 플레이어가 조작하는 패들을 구현합니다:

**클래스 선언:**
```java
public class BreakoutPaddle extends Box {
    // Box는 이미 Movable, Collidable, Boundable을 구현
    // 패들 특화 기능만 추가
}
```

**필드 (private):**
- `x`, `y`: 위치 (왼쪽 상단)
- `width`, `height`: 크기
- `speed`: 이동 속도
- `color`: 색상
- `targetX`: 마우스 X 좌표 (목표 위치)

**Box 클래스 상속의 장점:**

1. **이미 구현된 기능:**
   - Movable, Collidable, Boundable 인터페이스
   - 직사각형 충돌 감지
   - 경계 처리
   - 기본 이동 로직

2. **패들 특화 기능 추가:**
   - 파워업 효과 관리 (TimedPowerUp 내부 클래스)
   - 공 반사 각도 계산
   - 끈끈한 패들 기능
   - 레이저 발사 기능

**특수 메서드:**
- `setTargetX(double mouseX)`: 마우스 위치 설정
- `expand(double factor)`: 패들 너비 확장
- `shrink(double factor)`: 패들 너비 축소

**구현 힌트:**
```java
// 부드러운 이동
차이 = targetX - (x + width/2)
dx = 차이 * 0.1  // 느린 추적

// 반사 각도 계산
hitPosition = (ball.x - paddle.x) / paddle.width  // 0.0 ~ 1.0
angle = (hitPosition - 0.5) * π/3  // -60° ~ +60°

// 속도 벡터 설정
ball.dx = speed * sin(angle)
ball.dy = -abs(speed * cos(angle))  // 항상 위로
```

### 8.3 벽돌(Brick) 구현

**인터페이스 기반 벽돌 시스템**

다양한 유형의 벽돌을 인터페이스와 상속으로 구현합니다:

**기본 구조:**
- StaticObject 클래스 상속 (이미 Collidable 구현)
- Breakable 인터페이스 구현 (파괴 가능한 벽돌)
- 특수 효과는 추가 인터페이스로 구현

**벽돌 계층 구조:**
```java
// 깨지지 않는 벽돌 (게임 벽)
public class UnbreakableBrick extends StaticObject {
    // 게임 공간의 경계를 정의
    // WallFactory로 상/좌/우 벽 생성
}

// 기본 벽돌
public class SimpleBrick extends StaticObject implements Breakable {
    // 한 번에 깨지는 일반 벽돌
}

// 다중 타격 벽돌
public class MultiHitBrick extends SimpleBrick implements MultiHit {
    // 여러 번 타격해야 깨지는 벽돌
    // DamageState로 시각적 피드백
}

// 폭발 벽돌
public class ExplodingBrick extends SimpleBrick implements Exploding {
    // 파괴 시 주변 벽돌에 피해
    // ExplosionEffect 생성
}

// 파워업 벽돌
public class PowerUpBrick extends SimpleBrick implements PowerUpProvider {
    // 파괴 시 파워업 드롭
}
```

**필드 (private):**
- `x`, `y`, `width`, `height`: 위치와 크기
- `color`: 현재 색상
- `hitPoints`: 남은 타격 수
- `type`: 벽돌 유형 (BrickType)
- `isDestroyed`: 파괴 여부
- `points`: 파괴 시 획듍 점수

**핵심 메서드 설계:**

1. **paint(GraphicsContext gc)**:
   - 파괴된 벽돌은 그리지 않음
   - 3D 효과: 그림자로 입체감 표현
   - 타격 후 손상 표시 (hitPoints < maxHitPoints)

2. **handleCollision(Collidable other)**:
   - Ball과 충돌 시 `hit()` 호출
   - 파괴 시 특수 효과 발동

3. **hit()** (private):
   - hitPoints 1 감소
   - 0이 되면 isDestroyed = true
   - 특수 효과 체크

**특수 효과 메서드:**
- `triggerExplosion()`: 폭발 효과
- `dropPowerUp()`: 파워업 생성

**구현 힌트:**
```java
// 3D 효과
// 1. 어두운 색 그림자: fillRect(x+2, y+2, width, height)
// 2. 원래 색 본체: fillRect(x, y, width, height)

// 손상 표시
if (hitPoints < type.getMaxHitPoints()) {
    // 가로선으로 균열 표현
    gc.strokeLine(x+5, y+height/2, x+width-5, y+height/2);
}
```

### 8.4 파워업 시스템

**Ball 클래스를 활용한 PowerUp 구현**

파워업은 Ball 클래스를 확장하여 떨어지는 아이템으로 구현합니다:

**PowerUpProvider 인터페이스 활용:**
```java
// PowerUpProvider 인터페이스에 정의된 타입 사용
public enum PowerUpType {
    WIDER_PADDLE("W", 10.0),      // 패들 확장
    MULTI_BALL("M", 0),           // 멀티볼
    EXTRA_LIFE("+1", 0),          // 생명 추가
    LASER("L", 10.0),             // 레이저 발사
    SLOW_BALL("S", 15.0),         // 공 감속
    STICKY_PADDLE("G", 10.0);     // 끈끈한 패들
}
```

**PowerUp 클래스 선언:**
```java
public class PowerUp extends Ball {
    // Ball의 원형 충돌 감지와 물리 엔진 재사용
    // 떨어지는 효과만 추가 구현
    private PowerUpType type;
    
    // 생성자에서 수직 낙하 속도 설정
    // Ball의 draw 메서드 오버라이드로 파워업 아이콘 표시
}
```

**필드 (private):**
- `x`, `y`: 현재 위치
- `radius`: 반지름 (20)
- `dy`: 낙하 속도 (100 pixels/s)
- `type`: 파워업 종류 (PowerType)
- `collected`: 수집 여부

**핵심 메서드 설계:**

1. **paint(GraphicsContext gc)**:
   - 원형 배경 그리기
   - 타입별 색상 설정
   - 심볼 텍스트 중앙 정렬

2. **move(double deltaTime)**:
   - y 좌표만 업데이트
   - 일정한 속도로 낙하
   - 화면 밖으로 나가면 제거

3. **handleCollision(Collidable other)**:
   - Paddle과 충돌 시 collected = true

4. **applyEffect(BreakoutGame game)** (abstract):
   - 각 타입별 효과 적용
   - 하위 클래스에서 구현

**효과 구현 힌트:**
```java
// 파워업 생성 (PowerUpProvider 인터페이스 활용)
if (brick instanceof PowerUpProvider) {
    PowerUpProvider provider = (PowerUpProvider) brick;
    if (provider.shouldDropPowerUp()) {
        createPowerUp(brick, provider.getPowerUpType());
    }
}

// 파워업 효과 적용
private void applyPowerUp(PowerUp powerUp) {
    switch (powerUp.getType()) {
        case WIDER_PADDLE:
            paddle.applyPowerUp(PowerUpType.WIDER_PADDLE, duration);
            break;
        case MULTI_BALL:
            createMultiBalls();
            break;
        // ...
    }
}
```

### 8.5 게임 월드 및 상태 관리

**BreakoutWorld 클래스 설계**

2~7장의 개념을 통합하여 게임 월드를 관리:

```java
public class BreakoutWorld {
    // 게임 벽 (UnbreakableBrick)
    private List<UnbreakableBrick> walls;
    
    // 벽돌들 (Breakable 인터페이스)
    private List<Breakable> bricks;
    
    // 공들 (Ball 확장)
    private List<BreakoutBall> balls;
    
    // 패들 (Box 확장)
    private BreakoutPaddle paddle;
    
    // 파워업들 (Ball 확장)
    private List<PowerUp> powerUps;
    
    // 폭발 효과
    private List<ExplosionEffect> explosions;
}
```

**인터페이스 활용:**
- Collidable로 충돌 처리 통합
- Breakable로 다양한 벽돌 타입 관리
- PowerUpProvider로 파워업 생성 제어
- Exploding으로 폭발 효과 처리

**충돌 처리 통합:**
```java
private void handleCollisions() {
    // 공과 벽 충돌 (UnbreakableBrick)
    for (BreakoutBall ball : balls) {
        for (UnbreakableBrick wall : walls) {
            if (ball.collidesWith(wall)) {
                ball.handleCollision(wall);
            }
        }
    }
    
    // 공과 벽돌 충돌 (Breakable 인터페이스)
    for (BreakoutBall ball : balls) {
        for (Breakable brick : bricks) {
            if (brick instanceof Collidable) {
                Collidable collidable = (Collidable) brick;
                if (ball.collidesWith(collidable)) {
                    // 충돌 처리
                    brick.hit(1);
                    if (brick.isBroken()) {
                        handleBrickDestruction(brick);
                    }
                }
            }
        }
    }
}

// 벽돌 파괴 시 특수 효과 처리
private void handleBrickDestruction(Breakable brick) {
    score += brick.getPoints();
    
    // 폭발 효과
    if (brick instanceof Exploding) {
        handleExplosion((Exploding) brick);
    }
    
    // 파워업 드롭
    if (brick instanceof PowerUpProvider) {
        handlePowerUpDrop((PowerUpProvider) brick);
    }
}
```

**주요 필드 (private):**
- `world`: BreakoutWorld (게임 월드)
- `gameState`: GameState (현재 상태)
- `score`: int (현재 점수)
- `lives`: int (남은 생명)
- `level`: int (현재 레벨)
- `scoreLabel`, `livesLabel`: Label (UI 표시)
- `gameLoop`: AnimationTimer

**핵심 메서드 설계:**

1. **start(Stage stage)**:
   - 화면 구성 (Canvas + UI 패널)
   - 이벤트 핸들러 등록
   - 게임 루프 시작
   - 초기 레벨 로드

2. **updateGame(double deltaTime)**:
   ```java
   // 의사 코드
   if (gameState == GameState.PLAYING) {
       world.update(deltaTime);
       checkBallLost();      // 공 떨어짐 확인
       checkPowerUps();      // 파워업 수집
       checkLevelComplete(); // 벽돌 모두 파괴
       updateUI();           // 점수, 생명 표시
   }
   ```

3. **initializeLevel(int levelNumber)**:
   - 벽돌 행/열 계산
   - 각 위치에 벽돌 생성
   - 타입은 selectBrickType() 호출
   - 월드에 추가

4. **selectBrickType(int row, int col, int level)**:
   - 확률 기반 선택
   - 위쪽 행: 더 단단한 벽돌
   - 레벨 상승: 난이도 증가

**이벤트 처리 메서드:**
- `handleMouseMove(MouseEvent e)`: 패들 이동
- `handleMouseClick(MouseEvent e)`: 공 발사
- `handleKeyPress(KeyEvent e)`: 일시정지, 메뉴

**게임 상태 관리:**
```java
// BreakoutWorld에서 게임 상태 처리
private void checkGameState() {
    // 모든 벽돌이 깨진 경우
    if (bricks.isEmpty()) {
        level++;
        createLevel(level);
        initializeBall();
    }
    
    // 모든 공을 놓친 경우
    if (balls.isEmpty()) {
        lives--;
        if (lives > 0) {
            initializeBall();
        }
    }
}

// 벽돌 생성 시 인터페이스 활용
private Breakable createBrickForLevel(double x, double y, int row, int col, int level) {
    if (level >= 3 && (row + col) % 7 == 0) {
        // Exploding 인터페이스 구현
        return new ExplodingBrick(x, y, width, height, color, points);
    } else if (level >= 2 && row < 2) {
        // MultiHit 인터페이스 구현
        return new MultiHitBrick(x, y, width, height, color, points, hitCount);
    } else if ((row + col) % 5 == 0) {
        // PowerUpProvider 인터페이스 구현
        return new PowerUpBrick(x, y, width, height, color, points, chance);
    } else {
        // Breakable 인터페이스만 구현
        return new SimpleBrick(x, y, width, height, color, points);
    }
}
```

### 8.6 레벨 디자인과 진행

**LevelManager 클래스 설계**

레벨 설정과 진행을 관리하는 클래스:

**LevelConfig 내부 클래스:**
```java
public static class LevelConfig {
    private String name;
    private int rows, cols;
    private double ballSpeedMultiplier;
    private Map<BrickType, Double> brickProbabilities;
    // getter/setter
}
```

**LevelManager 필드:**
- `levels`: List<LevelConfig>
- `currentLevel`: int

**레벨 디자인 예시:**

1. **Level 1 - "시작"**:
   ```java
   rows: 5, cols: 10
   속도: 1.0x
   벽돌: NORMAL(0.9), POWERUP(0.1)
   ```

2. **Level 2 - "도전"**:
   ```java
   rows: 6, cols: 10
   속도: 1.2x
   벽돌: NORMAL(0.6), HARD(0.3), POWERUP(0.1)
   ```

3. **Level 3 - "폭발"**:
   ```java
   rows: 7, cols: 11
   속도: 1.3x
   벽돌: NORMAL(0.5), HARD(0.2), EXPLOSIVE(0.2), POWERUP(0.1)
   ```

**레벨 생성 메서드:**

```java
public LevelConfig createLevel(int levelNumber) {
    // 레벨 번호에 따른 설정 생성
}

public BrickType selectBrickType(Map<BrickType, Double> probabilities) {
    // 확률 기반 타입 선택
}
```

**레벨 진행 시스템:**
1. 모든 벽돌 파괴 시 레벨 완료
2. 다음 레벨로 진행
3. 공 속도 증가
4. 벽돌 배치 복잡도 증가
5. 특수 벽돌 비율 증가

**구현 힌트:**
```java
// 확률 기반 선택 알고리즘
double random = Math.random();
double cumulative = 0;
for (Map.Entry<BrickType, Double> entry : probabilities.entrySet()) {
    cumulative += entry.getValue();
    if (random < cumulative) {
        return entry.getKey();
    }
}
```

**추가 레벨 아이디어:**
- Level 4: 금속 벽돌 추가
- Level 5: 움직이는 벽돌
- Level 6: 보스 전투
- Level 7+: 패턴 기반 배치

## 실습 과제

### Lab 8-1: 기본 게임 구현
벽돌 깨기 게임의 핵심 기능 구현:
- 패들 컨트롤
- 공 발사와 반사
- 벽돌 파괴
- 점수 시스템

### Lab 8-2: 파워업 시스템
다양한 파워업 구현:
- 5가지 이상의 파워업
- 시간 제한 효과
- 중첩 가능한 효과

### Lab 8-3: 레벨 에디터
레벨을 디자인할 수 있는 에디터:
```java
public class LevelEditor extends Application {
    private Grid<BrickType> brickGrid;
    private BrickType selectedType;
    
    // 마우스로 벽돌 배치
    // 파일로 저장/불러오기
    // 미리보기 기능
}
```

### Lab 8-4: 고급 기능
게임을 더 재미있게 만드는 기능:
- 보스 벽돌
- 특수 공 (불공, 관통공 등)
- 콤보 시스템
- 리더보드

## JUnit 테스트 예제

```java
public class BreakoutTest {
    
    @Test
    public void testPaddleBallCollision() {
        Paddle paddle = new Paddle(400, 550);
        Ball ball = new Ball(450, 540, 10);
        ball.setDy(100); // 아래로 이동
        
        assertTrue(paddle.isColliding(ball));
        
        paddle.handleCollision(ball);
        
        // 공이 위로 반사됨
        assertTrue(ball.getDy() < 0);
    }
    
    @Test
    public void testBrickDestruction() {
        Brick brick = new Brick(100, 100, 70, 20, BrickType.NORMAL);
        Ball ball = new Ball(135, 110, 10);
        
        assertFalse(brick.isDestroyed());
        
        brick.handleCollision(ball);
        
        assertTrue(brick.isDestroyed());
        assertEquals(10, brick.getPoints());
    }
    
    @Test
    public void testPowerUpEffect() {
        BreakoutGame game = new BreakoutGame();
        Paddle paddle = game.getPaddle();
        double originalWidth = paddle.getWidth();
        
        PowerUp powerUp = new PowerUp(0, 0, PowerType.EXPAND_PADDLE);
        powerUp.applyEffect(game);
        
        assertEquals(originalWidth * 1.5, paddle.getWidth(), 0.001);
    }
    
    @Test
    public void testGameStateTransitions() {
        BreakoutGame game = new BreakoutGame();
        
        assertEquals(GameState.MENU, game.getGameState());
        
        game.startGame();
        assertEquals(GameState.PLAYING, game.getGameState());
        
        game.pauseGame();
        assertEquals(GameState.PAUSED, game.getGameState());
        
        // 모든 생명을 잃으면
        for (int i = 0; i < 3; i++) {
            game.loseLife();
        }
        assertEquals(GameState.GAME_OVER, game.getGameState());
    }
}
```

## 자가 평가 문제

1. **인터페이스 기반 설계의 장점은?**
   - 코드 재사용성 향상
   - 유연한 확장성
   - 관심사 분리
   - 테스트 용이성

2. **2~7장의 클래스를 활용한 예시는?**
   - Ball → BreakoutBall, PowerUp
   - Box → BreakoutPaddle
   - StaticObject → 모든 벽돌 클래스
   - 인터페이스 → Breakable, MultiHit, Exploding

3. **UnbreakableBrick의 역할은?**
   - 게임 공간의 경계 정의
   - 공이 튀겨나가는 벽 역할
   - StaticObject 상속으로 충돌 처리
   - WallFactory로 쉽게 생성

4. **특수 효과 인터페이스의 이점은?**
   - 다중 인터페이스 구현 가능
   - 효과를 독립적으로 관리
   - instanceof로 타입 확인
   - 새로운 효과 추가 용이

## 자주 하는 실수와 해결 방법

### 1. 프레임 의존적 물리
```java
// 잘못된 코드 - FPS에 따라 속도가 달라짐
ball.setY(ball.getY() + 5);

// 올바른 코드 - 델타 타임 사용
ball.setY(ball.getY() + velocity * deltaTime);
```

### 2. 동시 수정 오류
```java
// 잘못된 코드 - 순회 중 리스트 수정
for (Brick brick : bricks) {
    if (brick.isDestroyed()) {
        bricks.remove(brick); // ConcurrentModificationException!
    }
}

// 올바른 코드 - Iterator 사용 또는 별도 리스트
bricks.removeIf(Brick::isDestroyed);
```

### 3. 파워업 효과 누적
```java
// 문제 - 파워업이 영구적으로 적용됨
paddle.expand(1.5);

// 해결 - 시간 제한과 원래 크기 저장
class TimedEffect {
    double duration;
    double originalValue;
    
    void update(double deltaTime) {
        duration -= deltaTime;
        if (duration <= 0) {
            restore();
        }
    }
}
```

## 다음 장 미리보기

9장에서는 외부 효과를 추가합니다:
- 중력 시뮬레이션
- 바람 효과
- 마찰력
- 반발 계수

## 추가 학습 자료

- [Game Programming Patterns](https://gameprogrammingpatterns.com/)
- [Breakout Game Tutorial](https://developer.mozilla.org/en-US/docs/Games/Tutorials/2D_Breakout_game_pure_JavaScript)
- [JavaFX Game Development](https://github.com/AlmasB/FXGLGames)
- [Interface-Based Design Patterns](https://www.baeldung.com/java-interface-based-design)
- [SOLID Principles in Game Development](https://www.gamedev.net/tutorials/programming/general-and-gameplay-programming/solid-principles-in-game-development-r5101/)

## 학습 체크포인트

- [ ] 2~7장의 기본 클래스(Ball, Box, StaticObject)를 활용했습니다
- [ ] 인터페이스(Breakable, MultiHit, Exploding, PowerUpProvider)를 구현했습니다
- [ ] UnbreakableBrick으로 게임 공간을 정의했습니다
- [ ] 상속과 인터페이스로 재사용 가능한 구조를 만들었습니다
- [ ] 완전한 벽돌 깨기 게임을 구현했습니다
- [ ] 게임 상태를 관리할 수 있습니다
- [ ] 파워업 시스템을 구현했습니다