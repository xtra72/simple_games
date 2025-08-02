# Cannon Game Developer Guide

## Architecture Overview

This guide provides a comprehensive overview of the Cannon Game project architecture, class hierarchies, and development patterns using JavaFX.

## Class Hierarchy

### Core Object Hierarchy

```
Ball
├── PaintableBall
│   ├── MovableBall
│   │   └── BoundedBall
│   └── BoundedBall
└── Box
    └── PaintableBox
```

### Interface Hierarchy

```
Paintable
├── PaintableBall (implements)
└── PaintableBox (implements)

Movable
├── MovableBall (implements)
└── MovableWorld (implements)

Boundable
├── BoundedBall (implements)
└── Box (implements)
```

### World Classes

```
Pane (JavaFX)
└── World
    └── MovableWorld
        └── BoundedWorld
```

## Core Classes

### Ball
**Purpose**: Base class for all ball objects

**Key Responsibilities**:
- Maintains position (x, y)
- Stores radius
- Provides boundary calculations
- Implements basic validation

**Key Methods**:
```java
public double getX()
public double getY()
public double getRadius()
public double getMinX()
public double getMaxX()
public double getMinY()
public double getMaxY()
```

### PaintableBall
**Purpose**: Adds visual rendering capabilities to Ball

**Key Features**:
- Color management
- JavaFX shape creation
- Visual representation

**Key Methods**:
```java
public void draw(GraphicsContext gc)  // For Canvas rendering
public Circle getShape()               // For Scene Graph rendering
public Color getColor()
public void setColor(Color color)
```

### MovableBall
**Purpose**: Adds movement capabilities

**Key Features**:
- Velocity components (dx, dy)
- Movement mechanics
- Position updates

**Key Methods**:
```java
public void move()
public void move(double deltaTime)
public void setDX(double dx)
public void setDY(double dy)
```

### BoundedBall
**Purpose**: Adds collision detection and boundary constraints

**Key Features**:
- Boundary checking
- Collision response
- Bounce mechanics

**Key Methods**:
```java
public boolean isColliding(Bounds bounds)
public void bounce(Bounds bounds)
```

## Core Interfaces

### Paintable
**Purpose**: Contract for renderable objects

```java
public interface Paintable {
    void draw(GraphicsContext gc);
    Node getNode();  // JavaFX node for scene graph
}
```

### Movable
**Purpose**: Contract for objects that can move

```java
public interface Movable {
    void move();
    void move(double deltaTime);
    void moveTo(double x, double y);
}
```

### Boundable
**Purpose**: Contract for objects with boundaries

```java
public interface Boundable {
    Bounds getBounds();
    boolean isColliding(Boundable other);
}
```

## World Management

### World
**Base Responsibilities**:
- Ball collection management
- Rendering coordination
- Object lifecycle

**Design Pattern**: Container/Manager pattern

### MovableWorld
**Additional Features**:
- Animation loop using JavaFX AnimationTimer
- Time management
- Movement coordination

### BoundedWorld
**Additional Features**:
- Collision detection
- Boundary enforcement
- Physics simulation

## JavaFX Rendering Approaches

### 1. Scene Graph Approach

Best for interactive objects with built-in event handling:

```java
public class World extends Pane {
    public void addBall(Ball ball) {
        if (ball instanceof PaintableBall) {
            Circle shape = ((PaintableBall) ball).getShape();
            getChildren().add(shape);
        }
    }
}
```

### 2. Canvas Approach

Best for performance with many objects:

```java
public class World extends Pane {
    private Canvas canvas;
    private GraphicsContext gc;
    
    public void render() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (Ball ball : balls) {
            if (ball instanceof Paintable) {
                ((Paintable) ball).draw(gc);
            }
        }
    }
}
```

### 3. Hybrid Approach

Combine both for optimal performance:

```java
public class World extends Pane {
    private Canvas gameCanvas;    // For game objects
    private Pane uiLayer;        // For UI elements
    
    public World() {
        gameCanvas = new Canvas(800, 600);
        uiLayer = new Pane();
        getChildren().addAll(gameCanvas, uiLayer);
    }
}
```

## Development Patterns

### 1. JavaFX Application Structure

```java
public class CannonGame extends Application {
    private World world;
    private AnimationTimer gameLoop;
    
    @Override
    public void start(Stage primaryStage) {
        world = new World(800, 600);
        Scene scene = new Scene(world);
        
        setupEventHandlers(scene);
        setupGameLoop();
        
        primaryStage.setScene(scene);
        primaryStage.setTitle("Cannon Game");
        primaryStage.show();
        
        gameLoop.start();
    }
}
```

### 2. Game Loop with AnimationTimer

```java
private void setupGameLoop() {
    gameLoop = new AnimationTimer() {
        private long lastUpdate = 0;
        
        @Override
        public void handle(long now) {
            if (lastUpdate == 0) {
                lastUpdate = now;
                return;
            }
            
            double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
            
            update(deltaTime);
            render();
            
            lastUpdate = now;
        }
    };
}
```

### 3. Event Handling

```java
private void setupEventHandlers(Scene scene) {
    // Mouse events
    scene.setOnMouseClicked(e -> handleMouseClick(e.getX(), e.getY()));
    scene.setOnMouseMoved(e -> handleMouseMove(e.getX(), e.getY()));
    
    // Keyboard events
    scene.setOnKeyPressed(e -> handleKeyPress(e.getCode()));
    scene.setOnKeyReleased(e -> handleKeyRelease(e.getCode()));
}
```

### 4. Properties and Bindings

```java
public class GameObject {
    private DoubleProperty x = new SimpleDoubleProperty();
    private DoubleProperty y = new SimpleDoubleProperty();
    
    public GameObject() {
        // Bind visual representation to properties
        shape.centerXProperty().bind(x);
        shape.centerYProperty().bind(y);
    }
}
```

## Key Design Decisions

### 1. Coordinate System
- Origin (0,0) at top-left (JavaFX default)
- X increases rightward
- Y increases downward
- All positions use double precision

### 2. Collision Detection
- Bounding box approach for efficiency
- Circle-specific collision for accuracy
- Spatial partitioning for optimization (advanced)

### 3. Rendering Pipeline
```
1. Clear canvas/update scene
2. Update physics
3. Check collisions
4. Update positions
5. Render objects
6. Update UI
```

### 4. Game Loop
```java
AnimationTimer gameLoop = new AnimationTimer() {
    @Override
    public void handle(long now) {
        processInput();
        updatePhysics(deltaTime);
        detectCollisions();
        resolveCollisions();
        render();
    }
};
```

## JavaFX-Specific Features

### 1. CSS Styling
```java
// Apply CSS to game elements
world.getStyleClass().add("game-world");
ball.getShape().getStyleClass().add("game-ball");
```

```css
.game-world {
    -fx-background-color: #2b2b2b;
}

.game-ball {
    -fx-fill: radial-gradient(center 30% 30%, radius 60%, red, darkred);
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 2, 2);
}
```

### 2. Effects and Transitions
```java
// Fade effect on ball destruction
FadeTransition fade = new FadeTransition(Duration.millis(500), ball.getShape());
fade.setToValue(0);
fade.setOnFinished(e -> world.removeBall(ball));
fade.play();
```

### 3. Property Animations
```java
// Animate ball size
Timeline timeline = new Timeline(
    new KeyFrame(Duration.ZERO, 
        new KeyValue(ball.radiusProperty(), 20)),
    new KeyFrame(Duration.seconds(1), 
        new KeyValue(ball.radiusProperty(), 30))
);
timeline.play();
```

## Best Practices

### 1. JavaFX Thread Management
```java
// Ensure UI updates on FX Application Thread
Platform.runLater(() -> {
    world.addBall(newBall);
    updateScoreLabel();
});
```

### 2. Resource Management
```java
@Override
public void stop() {
    // Clean up resources
    gameLoop.stop();
    executor.shutdown();
    mediaPlayer.dispose();
}
```

### 3. Performance Optimization
- Use Canvas for many objects (>100)
- Use Scene Graph for interactive objects
- Cache complex calculations
- Use object pooling for frequently created/destroyed objects

### 4. Memory Management
```java
// Remove references to prevent memory leaks
public void cleanup() {
    balls.clear();
    getChildren().clear();
    if (timeline != null) {
        timeline.stop();
    }
}
```

## Testing Strategy

### Unit Tests with TestFX
```java
@Test
public void testBallMovement(FxRobot robot) {
    // Given
    MovableBall ball = new MovableBall(0, 0, 10);
    ball.setDX(5);
    ball.setDY(3);
    
    // When
    Platform.runLater(() -> ball.move());
    WaitForAsyncUtils.waitForFxEvents();
    
    // Then
    assertEquals(5, ball.getX(), 0.001);
    assertEquals(3, ball.getY(), 0.001);
}
```

## Performance Considerations

### 1. Rendering Optimization
- Use viewport culling
- Implement dirty rectangle updates
- Batch similar draw operations

### 2. JavaFX Optimization
- Minimize scene graph changes
- Use cached nodes for static content
- Disable effects on low-end systems

### 3. Animation Optimization
- Use AnimationTimer over Timeline for game loops
- Implement frame skipping for consistent gameplay
- Profile with JavaFX Pulse Logger

## Common JavaFX Pitfalls

### 1. Threading Issues
```java
// Wrong - modifying UI from background thread
new Thread(() -> {
    world.addBall(ball); // Will throw exception
}).start();

// Correct
new Thread(() -> {
    Platform.runLater(() -> world.addBall(ball));
}).start();
```

### 2. Memory Leaks with Listeners
```java
// Add weak listeners to prevent leaks
ball.xProperty().addListener(new WeakInvalidationListener(e -> {
    updatePosition();
}));
```

### 3. Performance with Bindings
```java
// Avoid complex binding chains
// Bad: property1.bind(property2.add(property3).multiply(property4));
// Good: Calculate manually in animation loop
```

## Advanced JavaFX Topics

### Scene Builder Integration
- Design UI layouts visually
- FXML for UI structure
- Controller classes for logic

### 3D Graphics
```java
// JavaFX supports 3D
Sphere ball3D = new Sphere(radius);
ball3D.setMaterial(new PhongMaterial(Color.RED));
```

### Media Integration
```java
// Sound effects
MediaPlayer player = new MediaPlayer(
    new Media(getClass().getResource("/sounds/bounce.mp3").toString())
);
player.play();
```

## Debugging JavaFX Applications

1. **Pulse Logger**: `-Djavafx.pulseLogger=true`
2. **Scene Graph Dumps**: `scene.getRoot().snapshot(null, null)`
3. **Performance Monitor**: `-Dprism.showdirty=true`
4. **Thread Checker**: `-Djavafx.debug=true`

## Conclusion

This guide provides the foundation for understanding and extending the Cannon Game project using JavaFX. The framework offers powerful features for game development including hardware acceleration, rich graphics capabilities, and excellent tooling support.