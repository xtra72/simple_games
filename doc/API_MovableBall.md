# MovableBall Class API Documentation

## Overview

The `MovableBall` class extends `PaintableBall` to add movement capabilities. It represents a ball that can move in 2D space with velocity components along the X and Y axes.

## Class Definition

```java
public class MovableBall extends PaintableBall
```

## Inheritance Hierarchy

```
Ball
 └── PaintableBall
      └── MovableBall
```

## Fields

### dx
- **Type**: double
- **Purpose**: Displacement along the X-axis per unit time (velocity component)
- **Access**: Private
- **Unit**: Pixels per time unit

### dy
- **Type**: double
- **Purpose**: Displacement along the Y-axis per unit time (velocity component)
- **Access**: Private
- **Unit**: Pixels per time unit

### logger (inherited)
- **Type**: Logger
- **Purpose**: Logs movement and state changes
- **Access**: Protected (if needed in subclasses)
- **Note**: Can be defined in Ball or PaintableBall base class

## JavaFX Properties (Alternative Implementation)

```java
private DoubleProperty dx = new SimpleDoubleProperty();
private DoubleProperty dy = new SimpleDoubleProperty();
```

## Constructors

### MovableBall(double x, double y, double radius)

**Purpose**: Creates a MovableBall with default color and zero velocity

**Parameters**:
- `x` - Initial X coordinate
- `y` - Initial Y coordinate
- `radius` - Ball radius

**Example**:
```java
MovableBall ball = new MovableBall(100, 100, 20);
```

### MovableBall(double x, double y, double radius, Color color)

**Purpose**: Creates a MovableBall with specified color and zero velocity

**Parameters**:
- `x` - Initial X coordinate
- `y` - Initial Y coordinate
- `radius` - Ball radius
- `color` - Ball color (JavaFX Color)

**Example**:
```java
MovableBall ball = new MovableBall(100, 100, 20, Color.RED);
```

### MovableBall(double x, double y, double radius, Color color, double dx, double dy)

**Purpose**: Creates a MovableBall with specified color and velocity

**Parameters**:
- `x` - Initial X coordinate
- `y` - Initial Y coordinate
- `radius` - Ball radius
- `color` - Ball color (JavaFX Color)
- `dx` - Initial X-axis velocity
- `dy` - Initial Y-axis velocity

**Example**:
```java
MovableBall ball = new MovableBall(100, 100, 20, Color.BLUE, 5, -3);
```

## Methods

### Accessor Methods

#### double getDX()

**Purpose**: Returns the X-axis displacement per unit time

**Returns**: The current dx value

**Example**:
```java
double velocityX = ball.getDX();
```

#### double getDY()

**Purpose**: Returns the Y-axis displacement per unit time

**Returns**: The current dy value

**Example**:
```java
double velocityY = ball.getDY();
```

### Mutator Methods

#### void setDX(double dx)

**Purpose**: Sets the X-axis displacement per unit time

**Parameters**:
- `dx` - New X-axis velocity component

**Example**:
```java
ball.setDX(10); // Move 10 pixels right per time unit
ball.setDX(-5); // Move 5 pixels left per time unit
```

#### void setDY(double dy)

**Purpose**: Sets the Y-axis displacement per unit time

**Parameters**:
- `dy` - New Y-axis velocity component

**Example**:
```java
ball.setDY(8);  // Move 8 pixels down per time unit
ball.setDY(-3); // Move 3 pixels up per time unit
```

### Movement Methods

#### void move()

**Purpose**: Moves the ball by its velocity components

**Behavior**:
- Updates X position by dx
- Updates Y position by dy
- Updates JavaFX shape position if using scene graph
- Logs movement operation

**Example**:
```java
// Ball at (100, 100) with dx=5, dy=-3
ball.move();
// Ball is now at (105, 97)
```

#### void move(double deltaTime)

**Purpose**: Moves the ball based on time elapsed

**Parameters**:
- `deltaTime` - Time elapsed since last update (in seconds)

**Behavior**:
- Updates position based on velocity and time
- Provides frame-rate independent movement

**Example**:
```java
ball.move(0.016); // 16ms elapsed (60 FPS)
```

#### void moveTo(double x, double y)

**Purpose**: Directly moves the ball to a specific position

**Parameters**:
- `x` - Target X coordinate
- `y` - Target Y coordinate

**Access**: Package-private (recommended) to restrict direct positioning

**Behavior**:
- Sets ball position to exact coordinates
- Updates JavaFX shape position
- Does not affect velocity
- Bypasses normal movement constraints

**Example**:
```java
ball.moveTo(200, 150); // Instantly move to (200, 150)
```

## JavaFX Integration

### Scene Graph Approach
```java
public class MovableBall extends PaintableBall {
    private Circle shape;
    
    public void move() {
        double newX = getX() + dx;
        double newY = getY() + dy;
        
        setX(newX);
        setY(newY);
        
        // Update JavaFX shape
        if (shape != null) {
            shape.setCenterX(newX);
            shape.setCenterY(newY);
        }
    }
}
```

### Canvas Approach
```java
public class MovableBall extends PaintableBall {
    public void draw(GraphicsContext gc) {
        gc.setFill(getColor());
        gc.fillOval(getX() - getRadius(), 
                    getY() - getRadius(), 
                    getRadius() * 2, 
                    getRadius() * 2);
    }
}
```

### Property Binding
```java
public class MovableBall extends PaintableBall {
    private DoubleProperty xProperty = new SimpleDoubleProperty();
    private DoubleProperty yProperty = new SimpleDoubleProperty();
    
    public MovableBall() {
        // Bind shape position to properties
        shape.centerXProperty().bind(xProperty);
        shape.centerYProperty().bind(yProperty);
    }
    
    public void move() {
        xProperty.set(xProperty.get() + dx);
        yProperty.set(yProperty.get() + dy);
    }
}
```

## Usage Examples

### Basic Movement with AnimationTimer
```java
AnimationTimer timer = new AnimationTimer() {
    @Override
    public void handle(long now) {
        ball.move();
        // JavaFX automatically updates display
    }
};
timer.start();
```

### Frame-Rate Independent Movement
```java
AnimationTimer timer = new AnimationTimer() {
    private long lastUpdate = 0;
    
    @Override
    public void handle(long now) {
        if (lastUpdate == 0) {
            lastUpdate = now;
            return;
        }
        
        double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
        ball.move(deltaTime);
        lastUpdate = now;
    }
};
```

### Bouncing Logic
```java
public void updateBall(MovableBall ball, double maxX, double maxY) {
    ball.move();
    
    // Check boundaries and reverse direction
    if (ball.getX() - ball.getRadius() <= 0 || 
        ball.getX() + ball.getRadius() >= maxX) {
        ball.setDX(-ball.getDX());
    }
    
    if (ball.getY() - ball.getRadius() <= 0 || 
        ball.getY() + ball.getRadius() >= maxY) {
        ball.setDY(-ball.getDY());
    }
}
```

### Timeline-based Animation
```java
Timeline timeline = new Timeline(new KeyFrame(
    Duration.millis(16), // ~60 FPS
    e -> {
        ball.move();
    }
));
timeline.setCycleCount(Timeline.INDEFINITE);
timeline.play();
```

## Inherited Methods

From **PaintableBall**:
- `void draw(GraphicsContext gc)` - Renders the ball on canvas
- `Circle getShape()` - Gets JavaFX shape for scene graph
- `Color getColor()` - Gets ball color
- `void setColor(Color color)` - Sets ball color

From **Ball**:
- `double getX()` - Gets X coordinate
- `double getY()` - Gets Y coordinate
- `double getRadius()` - Gets radius
- `void setX(double x)` - Sets X coordinate (if accessible)
- `void setY(double y)` - Sets Y coordinate (if accessible)

## Design Considerations

### JavaFX Thread Safety
```java
public void move() {
    if (Platform.isFxApplicationThread()) {
        // Direct update
        updatePosition();
    } else {
        // Schedule update on FX thread
        Platform.runLater(this::updatePosition);
    }
}
```

### Performance Optimization
```java
public class MovableBall extends PaintableBall {
    private boolean needsUpdate = false;
    
    public void move() {
        setX(getX() + dx);
        setY(getY() + dy);
        needsUpdate = true;
    }
    
    public void updateVisual() {
        if (needsUpdate) {
            shape.setCenterX(getX());
            shape.setCenterY(getY());
            needsUpdate = false;
        }
    }
}
```

## Best Practices

1. **Thread Safety**: Ensure updates happen on JavaFX Application Thread
2. **Performance**: Use Canvas for many balls, Scene Graph for few interactive balls
3. **Properties**: Consider using JavaFX properties for reactive programming
4. **Animation**: Use AnimationTimer for smooth, consistent movement
5. **Frame Independence**: Implement time-based movement for consistent speed

## See Also

- `Ball` - Base class providing position and radius
- `PaintableBall` - Parent class adding rendering capabilities
- `World` - Container class that manages MovableBall objects
- `BoundedBall` - Extension that adds boundary constraints
- `AnimationTimer` - JavaFX class for game loops
- `Timeline` - JavaFX class for keyframe animations