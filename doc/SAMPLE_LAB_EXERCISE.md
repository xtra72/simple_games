# Lab 실습 과제 예시 - 상세 가이드

## Lab 2-3: BallWorld 애플리케이션 상세 구현 가이드

### 실습 목표
- JavaFX Application 클래스를 확장하여 GUI 애플리케이션 만들기
- 여러 개의 공을 화면에 표시하고 관리하기
- 마우스 이벤트 처리하기
- 실시간 렌더링 구현하기

### 사전 요구사항
- Lab 2-1 (Ball 클래스) 완료
- Lab 2-2 (PaintableBall 클래스) 완료
- JavaFX 환경 설정 완료

### 단계별 구현 가이드

#### Step 1: 기본 Application 구조 만들기

```java
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BallWorldApp extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    
    private World world;
    private Canvas canvas;
    private GraphicsContext gc;
    
    @Override
    public void start(Stage primaryStage) {
        // Step 2에서 구현
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
```

**체크포인트**: 
- [ ] Application 클래스를 상속받았나요?
- [ ] 필요한 import 문을 모두 추가했나요?
- [ ] 상수로 화면 크기를 정의했나요?

#### Step 2: JavaFX 초기화

```java
@Override
public void start(Stage primaryStage) {
    // World 생성
    world = new World(WIDTH, HEIGHT);
    
    // Canvas 생성
    canvas = new Canvas(WIDTH, HEIGHT);
    gc = canvas.getGraphicsContext2D();
    
    // Scene 구성
    Pane root = new Pane();
    root.getChildren().add(canvas);
    
    Scene scene = new Scene(root, WIDTH, HEIGHT);
    
    // Stage 설정
    primaryStage.setTitle("Ball World - Lab 2-3");
    primaryStage.setScene(scene);
    primaryStage.setResizable(false);
    primaryStage.show();
    
    // 초기 공들 생성
    createRandomBalls(5);
    
    // 초기 화면 그리기
    draw();
}
```

**체크포인트**:
- [ ] Canvas를 생성했나요?
- [ ] Scene과 Stage를 올바르게 설정했나요?
- [ ] 윈도우 제목을 설정했나요?

#### Step 3: 랜덤한 공 생성하기

```java
private void createRandomBalls(int count) {
    Random random = new Random();
    
    for (int i = 0; i < count; i++) {
        // 랜덤한 위치 (가장자리에서 반지름만큼 떨어진 곳)
        double radius = random.nextDouble() * 20 + 10; // 10-30
        double x = random.nextDouble() * (WIDTH - 2 * radius) + radius;
        double y = random.nextDouble() * (HEIGHT - 2 * radius) + radius;
        
        // 랜덤한 색상
        Color color = Color.rgb(
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256)
        );
        
        // PaintableBall 생성
        PaintableBall ball = new PaintableBall(x, y, radius, color);
        
        // World에 추가
        try {
            world.add(ball);
        } catch (IllegalArgumentException e) {
            System.err.println("Failed to add ball: " + e.getMessage());
            i--; // 다시 시도
        }
    }
}
```

**디버깅 팁**:
- 공이 화면 밖에 생성되지 않도록 주의하세요
- World의 경계 검사를 통과하지 못하면 예외가 발생합니다
- 색상은 RGB 각각 0-255 범위여야 합니다

#### Step 4: 화면 그리기

```java
private void draw() {
    // 배경 지우기
    gc.setFill(Color.LIGHTGRAY);
    gc.fillRect(0, 0, WIDTH, HEIGHT);
    
    // 격자 그리기 (선택사항)
    drawGrid();
    
    // 모든 공 그리기
    BallRenderer renderer = new BallRenderer();
    for (Ball ball : world.getBalls()) {
        if (ball instanceof PaintableBall) {
            renderer.drawPaintableBall(gc, (PaintableBall) ball);
        } else {
            renderer.drawBall(gc, ball);
        }
    }
    
    // 정보 표시
    drawInfo();
}

private void drawGrid() {
    gc.setStroke(Color.GRAY);
    gc.setLineWidth(0.5);
    
    // 수직선
    for (int x = 0; x <= WIDTH; x += 50) {
        gc.strokeLine(x, 0, x, HEIGHT);
    }
    
    // 수평선
    for (int y = 0; y <= HEIGHT; y += 50) {
        gc.strokeLine(0, y, WIDTH, y);
    }
}

private void drawInfo() {
    gc.setFill(Color.BLACK);
    gc.setFont(Font.font("Arial", 14));
    gc.fillText("Balls: " + world.getBalls().size(), 10, 20);
    gc.fillText("Click to add new ball", 10, 40);
}
```

#### Step 5: 마우스 이벤트 처리

```java
private void setupEventHandlers() {
    canvas.setOnMouseClicked(event -> {
        double x = event.getX();
        double y = event.getY();
        
        // 클릭 위치에 새로운 공 생성
        handleMouseClick(x, y);
    });
    
    canvas.setOnMouseMoved(event -> {
        // 마우스 위치 표시 (선택사항)
        updateMousePosition(event.getX(), event.getY());
    });
}

private void handleMouseClick(double x, double y) {
    Random random = new Random();
    
    // 랜덤한 크기와 색상
    double radius = random.nextDouble() * 15 + 5; // 5-20
    Color color = Color.hsb(
        random.nextDouble() * 360,  // Hue
        0.8,                        // Saturation
        0.9                         // Brightness
    );
    
    try {
        PaintableBall ball = new PaintableBall(x, y, radius, color);
        world.add(ball);
        draw(); // 화면 갱신
    } catch (IllegalArgumentException e) {
        // 경계 밖이거나 다른 공과 겹치는 경우
        System.out.println("Cannot place ball at (" + x + ", " + y + ")");
    }
}
```

### 확장 과제

#### 1. 공 삭제 기능
```java
canvas.setOnMouseClicked(event -> {
    if (event.getButton() == MouseButton.PRIMARY) {
        // 왼쪽 클릭: 공 추가
        handleMouseClick(event.getX(), event.getY());
    } else if (event.getButton() == MouseButton.SECONDARY) {
        // 오른쪽 클릭: 공 삭제
        removeBallAt(event.getX(), event.getY());
    }
});

private void removeBallAt(double x, double y) {
    List<Ball> balls = world.getBalls();
    for (int i = balls.size() - 1; i >= 0; i--) {
        Ball ball = balls.get(i);
        if (ball.contains(x, y)) {
            world.remove(ball);
            draw();
            break;
        }
    }
}
```

#### 2. 공 크기 조절
```java
scene.setOnScroll(event -> {
    if (event.getDeltaY() > 0) {
        // 스크롤 업: 크기 증가
        adjustBallSizes(1.1);
    } else {
        // 스크롤 다운: 크기 감소
        adjustBallSizes(0.9);
    }
});
```

#### 3. 색상 그라데이션
```java
private void drawGradientBall(GraphicsContext gc, PaintableBall ball) {
    RadialGradient gradient = new RadialGradient(
        0, 0,                           // 초점
        ball.getX() - ball.getRadius() * 0.3,  // 중심 X
        ball.getY() - ball.getRadius() * 0.3,  // 중심 Y
        ball.getRadius(),               // 반지름
        false,                          // 비례
        CycleMethod.NO_CYCLE,
        new Stop(0, ball.getColor().brighter()),
        new Stop(1, ball.getColor().darker())
    );
    
    gc.setFill(gradient);
    gc.fillOval(
        ball.getX() - ball.getRadius(),
        ball.getY() - ball.getRadius(),
        ball.getRadius() * 2,
        ball.getRadius() * 2
    );
}
```

### 평가 기준

#### 기본 요구사항 (70점)
- [ ] 5개의 랜덤 공 생성 (10점)
- [ ] 각 공이 다른 색상 (10점)
- [ ] 마우스 클릭으로 공 추가 (20점)
- [ ] 경계 검사 작동 (15점)
- [ ] 화면에 정보 표시 (15점)

#### 코드 품질 (20점)
- [ ] 적절한 메서드 분리 (5점)
- [ ] 예외 처리 (5점)
- [ ] 의미있는 변수명 (5점)
- [ ] 주석과 문서화 (5점)

#### 추가 기능 (10점)
- [ ] 그리드 표시 (2점)
- [ ] 공 삭제 기능 (3점)
- [ ] 시각적 효과 (3점)
- [ ] 창의적 기능 (2점)

### 제출 체크리스트

1. **코드 파일**
   - `BallWorldApp.java`
   - 수정된 `World.java` (필요시)
   - 추가 클래스 파일

2. **실행 화면 캡처**
   - 초기 화면
   - 공 추가 후
   - 특수 기능 실행

3. **테스트 결과**
   - JUnit 테스트 통과 여부
   - 수동 테스트 시나리오

4. **문서**
   - 구현 설명서
   - 어려웠던 점과 해결 방법
   - 개선 아이디어

### 자주 발생하는 문제와 해결

#### 1. "JavaFX runtime components are missing"
```bash
# VM 옵션에 추가
--module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml
```

#### 2. ConcurrentModificationException
```java
// 잘못된 코드
for (Ball ball : world.getBalls()) {
    if (someCondition) {
        world.remove(ball); // 오류!
    }
}

// 올바른 코드
List<Ball> toRemove = new ArrayList<>();
for (Ball ball : world.getBalls()) {
    if (someCondition) {
        toRemove.add(ball);
    }
}
toRemove.forEach(world::remove);
```

#### 3. 공이 겹쳐서 생성됨
```java
private boolean isValidPosition(double x, double y, double radius) {
    for (Ball existing : world.getBalls()) {
        double distance = Math.sqrt(
            Math.pow(x - existing.getX(), 2) + 
            Math.pow(y - existing.getY(), 2)
        );
        if (distance < radius + existing.getRadius() + 5) { // 5픽셀 여유
            return false;
        }
    }
    return true;
}
```

### 학습 팁

1. **단계별 구현**
   - 한 번에 모든 기능을 구현하려 하지 마세요
   - 각 단계가 작동하는지 확인 후 다음 진행

2. **디버깅**
   - `System.out.println()` 활용
   - 디버거 사용법 익히기
   - 시각적 디버깅 (색상, 크기 변경)

3. **실험**
   - 매개변수 값을 바꿔보며 결과 관찰
   - 다양한 색상 조합 시도
   - 애니메이션 효과 추가

화이팅! 💪