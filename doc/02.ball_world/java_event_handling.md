# Java와 JavaFX의 이벤트 처리 (Event Handling)

## 개요

이벤트 처리는 사용자의 마우스 클릭, 키보드 입력 등의 상호작용을 프로그램이 인식하고 반응하는 메커니즘입니다. Java와 JavaFX는 이벤트 기반 프로그래밍을 위한 강력한 프레임워크를 제공합니다.

## 이벤트 처리의 기본 개념

### 이벤트(Event)란?
- 사용자의 행동(마우스 클릭, 키보드 입력 등)이나 시스템의 변화
- 프로그램이 반응해야 할 상황을 나타내는 객체

### 이벤트 리스너(Event Listener)란?
- 특정 이벤트가 발생했을 때 실행될 코드를 담은 인터페이스
- "이벤트가 발생하면 무엇을 할지" 정의

### 이벤트 소스(Event Source)란?
- 이벤트를 발생시키는 객체 (버튼, 캔버스, 텍스트필드 등)

## JavaFX 이벤트 처리 방식

### 1. 람다 표현식을 사용한 이벤트 처리 (권장)

```java
// 마우스 클릭 이벤트 처리
canvas.setOnMouseClicked(event -> {
    double x = event.getX();  // 클릭한 위치의 X 좌표
    double y = event.getY();  // 클릭한 위치의 Y 좌표
    System.out.println("클릭 위치: (" + x + ", " + y + ")");
});

// 키보드 이벤트 처리
scene.setOnKeyPressed(event -> {
    KeyCode key = event.getCode();
    if (key == KeyCode.SPACE) {
        System.out.println("스페이스바가 눌렸습니다!");
    }
});
```

### 2. 메서드 참조를 사용한 이벤트 처리

```java
public class MyApp extends Application {
    
    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(800, 600);
        
        // 메서드 참조로 이벤트 핸들러 연결
        canvas.setOnMouseClicked(this::handleMouseClick);
        canvas.setOnMouseMoved(this::handleMouseMove);
    }
    
    // 마우스 클릭 이벤트 처리 메서드
    private void handleMouseClick(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        
        // 클릭 위치에 새로운 공 생성
        Ball newBall = new Ball(x, y, 20);
        world.add(newBall);
        
        // 화면 다시 그리기
        draw();
    }
    
    // 마우스 이동 이벤트 처리 메서드
    private void handleMouseMove(MouseEvent event) {
        // 마우스 위치 추적
        currentMouseX = event.getX();
        currentMouseY = event.getY();
    }
}
```

### 3. 익명 내부 클래스를 사용한 이벤트 처리 (구식 방법)

```java
canvas.setOnMouseClicked(new EventHandler<MouseEvent>() {
    @Override
    public void handle(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        // 이벤트 처리 코드
    }
});
```

## 주요 JavaFX 이벤트 타입

### MouseEvent - 마우스 관련 이벤트

```java
// 마우스 클릭 (press + release)
node.setOnMouseClicked(event -> {
    if (event.getButton() == MouseButton.PRIMARY) {  // 왼쪽 버튼
        // 처리 코드
    } else if (event.getButton() == MouseButton.SECONDARY) {  // 오른쪽 버튼
        // 처리 코드
    }
});

// 마우스 눌림
node.setOnMousePressed(event -> {
    startX = event.getX();
    startY = event.getY();
});

// 마우스 놓임
node.setOnMouseReleased(event -> {
    endX = event.getX();
    endY = event.getY();
});

// 마우스 드래그
node.setOnMouseDragged(event -> {
    double deltaX = event.getX() - startX;
    double deltaY = event.getY() - startY;
    // 드래그 처리
});

// 마우스 이동
node.setOnMouseMoved(event -> {
    // 마우스 커서 위치 추적
});

// 마우스 진입/퇴장
node.setOnMouseEntered(event -> { /* 마우스가 영역에 들어옴 */ });
node.setOnMouseExited(event -> { /* 마우스가 영역을 벗어남 */ });
```

### KeyEvent - 키보드 관련 이벤트

```java
// 키 눌림
scene.setOnKeyPressed(event -> {
    switch (event.getCode()) {
        case UP:
            moveUp();
            break;
        case DOWN:
            moveDown();
            break;
        case LEFT:
            moveLeft();
            break;
        case RIGHT:
            moveRight();
            break;
        case SPACE:
            shoot();
            break;
    }
});

// 키 놓임
scene.setOnKeyReleased(event -> {
    // 키를 놓았을 때 처리
});

// 키 타이핑 (문자 입력)
scene.setOnKeyTyped(event -> {
    String character = event.getCharacter();
    // 입력된 문자 처리
});
```

## 이벤트 객체의 주요 메서드

### MouseEvent 메서드

```java
// 마우스 위치 정보
double x = event.getX();        // 이벤트 소스 기준 X 좌표
double y = event.getY();        // 이벤트 소스 기준 Y 좌표
double screenX = event.getScreenX();  // 화면 기준 X 좌표
double screenY = event.getScreenY();  // 화면 기준 Y 좌표

// 마우스 버튼 정보
MouseButton button = event.getButton();
boolean isPrimary = event.isPrimaryButtonDown();    // 왼쪽 버튼
boolean isSecondary = event.isSecondaryButtonDown(); // 오른쪽 버튼
boolean isMiddle = event.isMiddleButtonDown();       // 가운데 버튼

// 보조키 정보
boolean isCtrlDown = event.isControlDown();
boolean isShiftDown = event.isShiftDown();
boolean isAltDown = event.isAltDown();

// 클릭 횟수
int clickCount = event.getClickCount();  // 더블클릭 감지 등
```

### KeyEvent 메서드

```java
// 키 코드 (물리적 키)
KeyCode code = event.getCode();

// 입력된 문자
String character = event.getCharacter();

// 텍스트 (Ctrl+A 등의 조합)
String text = event.getText();

// 보조키 상태
boolean isCtrlDown = event.isControlDown();
boolean isShiftDown = event.isShiftDown();
boolean isAltDown = event.isAltDown();
```

## 실제 예제: BallWorld에서의 이벤트 처리

### 마우스 클릭으로 공 추가하기

```java
public class BallWorldApp extends Application {
    private World world;
    private Canvas canvas;
    private GraphicsContext gc;
    
    @Override
    public void start(Stage stage) {
        world = new World(800, 600);
        canvas = new Canvas(800, 600);
        gc = canvas.getGraphicsContext2D();
        
        // 마우스 클릭 이벤트 설정
        canvas.setOnMouseClicked(this::handleMouseClick);
        
        Scene scene = new Scene(new StackPane(canvas));
        stage.setScene(scene);
        stage.setTitle("Ball World");
        stage.show();
        
        // 초기 화면 그리기
        draw();
    }
    
    private void handleMouseClick(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        
        // 랜덤한 크기와 색상의 공 생성
        double radius = 10 + Math.random() * 30;
        Color color = Color.color(Math.random(), Math.random(), Math.random());
        
        try {
            PaintableBall ball = new PaintableBall(x, y, radius, color);
            world.add(ball);
            draw();  // 화면 업데이트
        } catch (IllegalArgumentException e) {
            // 공이 경계를 벗어나는 경우 처리
            System.out.println("공을 추가할 수 없습니다: " + e.getMessage());
        }
    }
    
    private void draw() {
        // 화면 지우기
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // 모든 공 그리기
        BallRenderer renderer = new BallRenderer();
        for (Ball ball : world.getBalls()) {
            renderer.draw(gc, ball);
        }
    }
}
```

### 키보드로 공 제어하기

```java
public class ControllableBallApp extends Application {
    private Ball controlledBall;
    private double speed = 5.0;
    
    @Override
    public void start(Stage stage) {
        // ... 초기화 코드 ...
        
        // 키보드 이벤트 설정
        scene.setOnKeyPressed(this::handleKeyPress);
        
        // ... 나머지 코드 ...
    }
    
    private void handleKeyPress(KeyEvent event) {
        if (controlledBall == null) return;
        
        double x = controlledBall.getX();
        double y = controlledBall.getY();
        
        switch (event.getCode()) {
            case UP:
            case W:
                controlledBall.setY(y - speed);
                break;
            case DOWN:
            case S:
                controlledBall.setY(y + speed);
                break;
            case LEFT:
            case A:
                controlledBall.setX(x - speed);
                break;
            case RIGHT:
            case D:
                controlledBall.setX(x + speed);
                break;
            case SPACE:
                // 스페이스바로 공 멈추기
                if (controlledBall instanceof MovableBall) {
                    ((MovableBall) controlledBall).setVelocity(0, 0);
                }
                break;
        }
        
        draw();  // 화면 업데이트
    }
}
```

## 이벤트 전파와 소비

### 이벤트 버블링 (Event Bubbling)

이벤트는 자식 노드에서 부모 노드로 전파됩니다:

```java
// 부모 컨테이너
VBox container = new VBox();
container.setOnMouseClicked(event -> {
    System.out.println("컨테이너 클릭됨");
});

// 자식 버튼
Button button = new Button("클릭하세요");
button.setOnMouseClicked(event -> {
    System.out.println("버튼 클릭됨");
    
    // 이벤트 소비 - 부모로 전파되지 않음
    event.consume();
});

container.getChildren().add(button);
```

### 이벤트 필터 (Event Filter)

이벤트가 타겟에 도달하기 전에 처리:

```java
// 필터는 핸들러보다 먼저 실행됨
node.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
    System.out.println("필터에서 처리됨");
    // event.consume(); // 주석 해제하면 핸들러가 실행되지 않음
});

node.setOnMouseClicked(event -> {
    System.out.println("핸들러에서 처리됨");
});
```

## 애니메이션과 이벤트 처리 통합

```java
public class InteractiveBallWorld extends Application {
    private AnimationTimer timer;
    private Set<KeyCode> pressedKeys = new HashSet<>();
    
    @Override
    public void start(Stage stage) {
        // ... 초기화 코드 ...
        
        // 키 이벤트 설정
        scene.setOnKeyPressed(event -> pressedKeys.add(event.getCode()));
        scene.setOnKeyReleased(event -> pressedKeys.remove(event.getCode()));
        
        // 애니메이션 타이머
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                processInput();  // 눌린 키 처리
                updateWorld();   // 월드 업데이트
                draw();         // 화면 그리기
            }
        };
        timer.start();
    }
    
    private void processInput() {
        if (pressedKeys.contains(KeyCode.LEFT)) {
            // 왼쪽 이동 처리
        }
        if (pressedKeys.contains(KeyCode.RIGHT)) {
            // 오른쪽 이동 처리
        }
        // ... 다른 키 처리 ...
    }
}
```

## 모범 사례와 주의사항

### 1. 이벤트 핸들러는 간결하게
```java
// 좋은 예
canvas.setOnMouseClicked(event -> addBallAt(event.getX(), event.getY()));

// 나쁜 예 - 핸들러에 너무 많은 로직
canvas.setOnMouseClicked(event -> {
    // 100줄의 복잡한 로직... (피하세요)
});
```

### 2. 적절한 이벤트 선택
- 클릭: `setOnMouseClicked` (press + release)
- 드래그: `setOnMouseDragged`
- 즉각 반응: `setOnMousePressed`

### 3. 메모리 누수 방지
```java
// 이벤트 핸들러 제거
node.setOnMouseClicked(null);

// 또는 약한 참조 사용
node.setOnMouseClicked(new WeakEventHandler<>(this::handleClick));
```

### 4. 스레드 안전성
JavaFX 이벤트는 JavaFX Application Thread에서 실행됩니다:
```java
// 다른 스레드에서 UI 업데이트 시
Platform.runLater(() -> {
    // UI 업데이트 코드
});
```

## 연습 문제

1. **마우스 드래그로 공 이동**: 마우스로 공을 드래그하여 이동시키는 기능 구현
2. **더블클릭으로 공 제거**: 공을 더블클릭하면 제거되는 기능 구현
3. **키보드 단축키**: Ctrl+A로 모든 공 선택, Delete로 선택된 공 삭제

## 참고 자료

- [JavaFX Events Tutorial (Oracle)](https://docs.oracle.com/javafx/2/events/jfxpub-events.htm)
- [JavaFX Event Handling (OpenJFX)](https://openjfx.io/javadoc/17/javafx.base/javafx/event/package-summary.html)
- [Working with Event Handlers](https://docs.oracle.com/javase/8/javafx/events-tutorial/handlers.htm)