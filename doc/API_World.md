# World Class API Documentation

## Overview

The `World` class is a fundamental component that manages Ball objects in a 2D space. It extends JavaFX's `Pane` to provide graphical rendering capabilities and implements methods for adding, removing, and managing Ball objects.

## Class Definition

```java
public class World extends Pane
```

## Fields

### ballList
- **Type**: ObservableList<Ball> or List<Ball>
- **Purpose**: Stores and manages all Ball objects added to the world
- **Access**: Private

### logger
- **Type**: Logger
- **Purpose**: Logs operations and events related to Ball management
- **Access**: Private static final
- **Scope**: Class-level logger shared across all World instances

### canvas
- **Type**: Canvas (optional approach)
- **Purpose**: Alternative rendering surface for performance-critical applications
- **Access**: Private

### gc
- **Type**: GraphicsContext (if using Canvas)
- **Purpose**: Drawing context for Canvas-based rendering
- **Access**: Private

## Constructor

### World()
**Purpose**: Initializes a new World instance

**Behavior**:
- Creates internal data structure for Ball management
- Initializes the logger
- Sets up the JavaFX Pane for rendering
- Optionally creates Canvas for performance rendering
- Registers event handlers if needed

**Example**:
```java
World world = new World();
```

### World(double width, double height)
**Purpose**: Initializes a World with specific dimensions

**Parameters**:
- `width` - World width in pixels
- `height` - World height in pixels

**Example**:
```java
World world = new World(800, 600);
```

## Methods

### void addBall(Ball ball)

**Purpose**: Adds a Ball object to the world

**Parameters**:
- `ball` - The Ball object to add

**Exceptions**:
- `NullPointerException` - If the ball parameter is null
- `AlreadyExistException` - If the ball is already present in the world

**Behavior**:
- Validates that ball is not null
- Checks if ball already exists in the world
- Adds ball to the internal collection
- If ball is Paintable, adds to scene graph
- Logs the addition operation

**Example**:
```java
Ball ball = new Ball(100, 100, 10);
world.addBall(ball);
```

### void removeBall(Ball ball)

**Purpose**: Removes a specific Ball object from the world

**Parameters**:
- `ball` - The Ball object to remove

**Exceptions**:
- `NullPointerException` - If the ball parameter is null
- `NoSuchElementException` - If the ball is not found in the world

**Behavior**:
- Validates that ball is not null
- Checks if ball exists in the world
- Removes ball from the internal collection
- Removes from scene graph if applicable
- Logs the removal operation

**Example**:
```java
world.removeBall(ball);
```

### void removeBall(int index)

**Purpose**: Removes a Ball at a specific index

**Parameters**:
- `index` - The position of the Ball to remove (0-based)

**Exceptions**:
- `IndexOutOfBoundsException` - If index is negative or >= getBallCount()

**Behavior**:
- Validates index is within valid range
- Removes ball at the specified position
- Updates scene graph accordingly
- Logs the removal operation

**Example**:
```java
world.removeBall(0); // Removes the first ball
```

### int getBallCount()

**Purpose**: Returns the number of Ball objects currently in the world

**Returns**: The count of Ball objects

**Behavior**:
- Returns the size of the internal Ball collection
- Thread-safe operation when using ObservableList

**Example**:
```java
int count = world.getBallCount();
System.out.println("Number of balls: " + count);
```

### Ball getBall(int index)

**Purpose**: Retrieves a Ball at a specific index

**Parameters**:
- `index` - The position of the Ball to retrieve (0-based)

**Returns**: The Ball object at the specified index

**Exceptions**:
- `IndexOutOfBoundsException` - If index is negative or >= getBallCount()

**Behavior**:
- Validates index is within valid range
- Returns the Ball at the specified position

**Example**:
```java
Ball firstBall = world.getBall(0);
```

### void render()

**Purpose**: Renders all paintable Ball objects in the world (Canvas-based approach)

**Behavior**:
- Clears the canvas
- Iterates through all Ball objects
- Checks if each Ball is paintable
- Renders paintable balls using GraphicsContext
- Called automatically by animation timer

**Example**:
```java
// Typically called in animation loop
world.render();
```

## JavaFX Integration

### Scene Graph Approach
```java
public class World extends Pane {
    private ObservableList<Node> ballNodes;
    
    public void addBall(Ball ball) {
        // Add to internal list
        ballList.add(ball);
        
        // Add to scene graph if paintable
        if (ball instanceof PaintableBall) {
            Circle circle = ((PaintableBall) ball).getShape();
            getChildren().add(circle);
        }
    }
}
```

### Canvas Approach (Performance)
```java
public class World extends Pane {
    private Canvas canvas;
    private GraphicsContext gc;
    
    public World(double width, double height) {
        canvas = new Canvas(width, height);
        gc = canvas.getGraphicsContext2D();
        getChildren().add(canvas);
    }
    
    public void render() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (Ball ball : ballList) {
            if (ball instanceof PaintableBall) {
                ((PaintableBall) ball).draw(gc);
            }
        }
    }
}
```

## Usage Examples

### Basic World Setup with JavaFX
```java
// Create JavaFX application
public class GameApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        World world = new World(800, 600);
        
        // Add balls
        world.addBall(new PaintableBall(100, 100, 25, Color.BLUE));
        world.addBall(new PaintableBall(200, 200, 30, Color.RED));
        
        // Create scene
        Scene scene = new Scene(world);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Ball World");
        primaryStage.show();
    }
}
```

### Animation with Timeline
```java
Timeline timeline = new Timeline(new KeyFrame(
    Duration.millis(16), // ~60 FPS
    e -> {
        updatePhysics();
        world.render(); // If using Canvas
    }
));
timeline.setCycleCount(Timeline.INDEFINITE);
timeline.play();
```

### Animation with AnimationTimer
```java
AnimationTimer timer = new AnimationTimer() {
    @Override
    public void handle(long now) {
        // Update ball positions
        for (int i = 0; i < world.getBallCount(); i++) {
            Ball ball = world.getBall(i);
            if (ball instanceof MovableBall) {
                ((MovableBall) ball).move();
            }
        }
        
        // Render if using Canvas
        world.render();
    }
};
timer.start();
```

## Event Handling

### Mouse Interaction
```java
world.setOnMouseClicked(event -> {
    double x = event.getX();
    double y = event.getY();
    world.addBall(new PaintableBall(x, y, 20, Color.GREEN));
});
```

### Keyboard Input
```java
scene.setOnKeyPressed(event -> {
    switch (event.getCode()) {
        case SPACE:
            // Add random ball
            break;
        case CLEAR:
            // Clear all balls
            break;
    }
});
```

## Properties and Bindings

JavaFX properties can be used for reactive programming:

```java
private IntegerProperty ballCount = new SimpleIntegerProperty(0);

public IntegerProperty ballCountProperty() {
    return ballCount;
}

public void addBall(Ball ball) {
    ballList.add(ball);
    ballCount.set(ballList.size());
}
```

## Thread Safety

JavaFX requires UI updates on the JavaFX Application Thread:

```java
Platform.runLater(() -> {
    world.addBall(ball);
});
```

## Best Practices

1. **Choose appropriate rendering**: Use Scene Graph for simple scenarios, Canvas for performance
2. **JavaFX Thread**: Always update UI elements on the JavaFX Application Thread
3. **Properties**: Use JavaFX properties for reactive bindings
4. **Animation**: Use AnimationTimer for smooth animations
5. **Memory management**: Remove unused balls to prevent memory leaks
6. **Event handling**: Leverage JavaFX's event system for user interaction

## Performance Considerations

- **Scene Graph**: Better for interactive objects, automatic dirty region management
- **Canvas**: Better for many objects, manual rendering control
- **Hybrid**: Use both for different layers (UI vs game objects)

## See Also

- `Ball` - The base class for objects managed by World
- `PaintableBall` - Extension of Ball that can be rendered
- `Pane` - The JavaFX container that World extends
- `Canvas` - Alternative rendering approach for performance