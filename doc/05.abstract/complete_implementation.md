# 5장: Abstract - 추상 클래스로 코드 정리

## 목차
1. [학습 목표와 핵심 개념](#1-학습-목표와-핵심-개념)
2. [GameObject 추상 클래스 설계](#2-gameobject-추상-클래스-설계)
3. [Ball과 Box의 리팩토링](#3-ball과-box의-리팩토링)
4. [AbstractWorld - 템플릿 메서드 패턴](#4-abstractworld---템플릿-메서드-패턴)
5. [테스트 코드와 검증](#5-테스트-코드와-검증)
6. [일반적인 실수와 해결법](#6-일반적인-실수와-해결법)

---

## 1. 학습 목표와 핵심 개념

### 1.1 학습 목표
- **추상 클래스**: 공통 기능을 추상화하여 코드 중복 제거
- **템플릿 메서드 패턴**: 알고리즘의 구조는 고정하고 세부사항만 변경
- **코드 재사용**: 상속을 통한 효과적인 코드 공유
- **다형성 활용**: 추상 타입을 통한 일관된 처리
- **설계 원칙**: DRY, SOLID 원칙의 실제 적용

### 1.2 핵심 개념 요약

**추상 클래스 vs 인터페이스**
```java
// 추상 클래스: 공통 구현 + 추상 메서드
public abstract class Animal {
    protected String name;              // 공통 필드
    public void sleep() { /* 공통 구현 */ }  // 공통 메서드
    public abstract void makeSound();   // 추상 메서드
}

// 인터페이스: 순수 계약
public interface Flyable {
    void fly();  // 추상 메서드만
}
```

**템플릿 메서드 패턴**
```java
// 템플릿 메서드: 알고리즘 구조 정의
public void process() {
    step1();      // 구현됨
    step2();      // 추상 - 하위 클래스에서 구현
    step3();      // 구현됨
}
```

---

## 2. GameObject 추상 클래스 설계

### 2.1 GameObject 완전 구현

```java
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 모든 게임 객체의 기본 클래스
 * 공통 속성과 기본 동작을 제공하고, 특화된 동작은 하위 클래스에서 구현합니다.
 */
public abstract class GameObject {
    // === 공통 필드들 ===
    protected double x;                    // 위치 X
    protected double y;                    // 위치 Y
    protected Color color;                 // 색상
    protected boolean active;              // 활성 상태
    protected boolean visible;             // 가시성
    protected long creationTime;           // 생성 시간
    protected String id;                   // 고유 식별자
    
    // === 정적 카운터 ===
    private static long nextId = 1;
    
    // === 생성자 ===
    
    /**
     * GameObject 생성자
     * @param x 초기 X 좌표
     * @param y 초기 Y 좌표
     * @param color 색상
     */
    protected GameObject(double x, double y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color != null ? color : Color.BLACK;
        this.active = true;
        this.visible = true;
        this.creationTime = System.currentTimeMillis();
        this.id = "GameObject_" + (nextId++);
    }
    
    // === 추상 메서드들 (하위 클래스에서 반드시 구현) ===
    
    /**
     * 객체를 화면에 그립니다
     * @param gc 그래픽스 컨텍스트
     */
    public abstract void paint(GraphicsContext gc);
    
    /**
     * 객체의 경계를 반환합니다
     * @return 경계 정보
     */
    public abstract Bounds getBounds();
    
    /**
     * 객체를 업데이트합니다
     * @param deltaTime 경과 시간
     */
    public abstract void update(double deltaTime);
    
    /**
     * 객체의 면적을 반환합니다
     * @return 면적
     */
    public abstract double getArea();
    
    // === 공통 구현 메서드들 ===
    
    /**
     * 다른 객체와 충돌하는지 확인합니다
     * @param other 다른 객체
     * @return 충돌하면 true
     */
    public boolean isColliding(GameObject other) {
        if (other == null || !this.active || !other.active) {
            return false;
        }
        return getBounds().intersects(other.getBounds());
    }
    
    /**
     * 특정 점이 객체 안에 있는지 확인합니다
     * @param px X 좌표
     * @param py Y 좌표
     * @return 점이 객체 안에 있으면 true
     */
    public boolean contains(double px, double py) {
        return getBounds().contains(px, py);
    }
    
    /**
     * 다른 객체와의 거리를 계산합니다
     * @param other 다른 객체
     * @return 중심간 거리
     */
    public double distanceTo(GameObject other) {
        if (other == null) return Double.MAX_VALUE;
        
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * 객체를 지정한 위치로 이동시킵니다
     * @param newX 새 X 좌표
     * @param newY 새 Y 좌표
     */
    public void moveTo(double newX, double newY) {
        this.x = newX;
        this.y = newY;
        onPositionChanged();
    }
    
    /**
     * 객체를 상대적으로 이동시킵니다
     * @param deltaX X 방향 이동량
     * @param deltaY Y 방향 이동량
     */
    public void moveBy(double deltaX, double deltaY) {
        this.x += deltaX;
        this.y += deltaY;
        onPositionChanged();
    }
    
    /**
     * 위치 변경 시 호출되는 훅 메서드
     * 하위 클래스에서 오버라이드하여 추가 처리 가능
     */
    protected void onPositionChanged() {
        // 기본 구현은 비어있음 - 하위 클래스에서 필요시 오버라이드
    }
    
    /**
     * 색상 변경 시 호출되는 훅 메서드
     * @param oldColor 이전 색상
     * @param newColor 새 색상
     */
    protected void onColorChanged(Color oldColor, Color newColor) {
        // 기본 구현은 비어있음 - 하위 클래스에서 필요시 오버라이드
    }
    
    /**
     * 활성 상태 변경 시 호출되는 훅 메서드
     * @param wasActive 이전 활성 상태
     * @param isActive 현재 활성 상태
     */
    protected void onActiveChanged(boolean wasActive, boolean isActive) {
        // 기본 구현은 비어있음
    }
    
    // === 템플릿 메서드들 ===
    
    /**
     * 안전한 렌더링을 위한 템플릿 메서드
     * @param gc 그래픽스 컨텍스트
     */
    public final void safePaint(GraphicsContext gc) {
        if (!visible || !active || gc == null) {
            return;
        }
        
        // 렌더링 전 처리
        beforePaint(gc);
        
        // 실제 렌더링 (하위 클래스 구현)
        paint(gc);
        
        // 렌더링 후 처리
        afterPaint(gc);
    }
    
    /**
     * 렌더링 전 처리 (훅 메서드)
     * @param gc 그래픽스 컨텍스트
     */
    protected void beforePaint(GraphicsContext gc) {
        // 기본 구현: 투명도 설정
        if (getOpacity() < 1.0) {
            gc.setGlobalAlpha(getOpacity());
        }
    }
    
    /**
     * 렌더링 후 처리 (훅 메서드)
     * @param gc 그래픽스 컨텍스트
     */
    protected void afterPaint(GraphicsContext gc) {
        // 기본 구현: 투명도 복원
        if (getOpacity() < 1.0) {
            gc.setGlobalAlpha(1.0);
        }
    }
    
    /**
     * 안전한 업데이트를 위한 템플릿 메서드
     * @param deltaTime 경과 시간
     */
    public final void safeUpdate(double deltaTime) {
        if (!active || deltaTime <= 0) {
            return;
        }
        
        // 업데이트 전 처리
        beforeUpdate(deltaTime);
        
        // 실제 업데이트 (하위 클래스 구현)
        update(deltaTime);
        
        // 업데이트 후 처리
        afterUpdate(deltaTime);
    }
    
    /**
     * 업데이트 전 처리 (훅 메서드)
     * @param deltaTime 경과 시간
     */
    protected void beforeUpdate(double deltaTime) {
        // 기본 구현은 비어있음
    }
    
    /**
     * 업데이트 후 처리 (훅 메서드)
     * @param deltaTime 경과 시간
     */
    protected void afterUpdate(double deltaTime) {
        // 기본 구현은 비어있음
    }
    
    // === 상태 관리 메서드들 ===
    
    /**
     * 객체를 활성화합니다
     */
    public void activate() {
        boolean wasActive = this.active;
        this.active = true;
        if (!wasActive) {
            onActiveChanged(false, true);
        }
    }
    
    /**
     * 객체를 비활성화합니다
     */
    public void deactivate() {
        boolean wasActive = this.active;
        this.active = false;
        if (wasActive) {
            onActiveChanged(true, false);
        }
    }
    
    /**
     * 객체를 표시합니다
     */
    public void show() {
        this.visible = true;
    }
    
    /**
     * 객체를 숨깁니다
     */
    public void hide() {
        this.visible = false;
    }
    
    /**
     * 객체의 생존 시간을 반환합니다
     * @return 생존 시간 (밀리초)
     */
    public long getAge() {
        return System.currentTimeMillis() - creationTime;
    }
    
    /**
     * 투명도를 계산합니다 (나이에 따른 페이드 등)
     * @return 투명도 (0.0 ~ 1.0)
     */
    protected double getOpacity() {
        return 1.0;  // 기본적으로 불투명
    }
    
    // === 비교와 정렬을 위한 메서드들 ===
    
    /**
     * Z-order 비교를 위한 깊이 값 반환
     * @return 깊이 값 (작을수록 뒤에 그림)
     */
    public int getZOrder() {
        return 0;  // 기본값
    }
    
    /**
     * 객체 타입을 반환합니다
     * @return 객체 타입 문자열
     */
    public String getObjectType() {
        return getClass().getSimpleName();
    }
    
    // === Getter/Setter ===
    
    public double getX() { return x; }
    public double getY() { return y; }
    public Color getColor() { return color; }
    public boolean isActive() { return active; }
    public boolean isVisible() { return visible; }
    public String getId() { return id; }
    public long getCreationTime() { return creationTime; }
    
    public void setX(double x) { 
        this.x = x; 
        onPositionChanged();
    }
    
    public void setY(double y) { 
        this.y = y; 
        onPositionChanged();
    }
    
    public void setColor(Color color) {
        if (color != null) {
            Color oldColor = this.color;
            this.color = color;
            onColorChanged(oldColor, color);
        }
    }
    
    public void setActive(boolean active) {
        boolean wasActive = this.active;
        this.active = active;
        if (wasActive != active) {
            onActiveChanged(wasActive, active);
        }
    }
    
    public void setVisible(boolean visible) { 
        this.visible = visible; 
    }
    
    // === Object 메서드 오버라이드 ===
    
    @Override
    public String toString() {
        return String.format("%s[id=%s, pos=(%.2f,%.2f), active=%s]",
                           getObjectType(), id, x, y, active);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        GameObject other = (GameObject) obj;
        return Objects.equals(id, other.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```

### 2.2 Movable과 Drawable 인터페이스 정의

```java
/**
 * 움직일 수 있는 객체를 위한 인터페이스
 */
public interface Movable {
    void move(double deltaTime);
    
    double getDx();
    double getDy();
    void setDx(double dx);
    void setDy(double dy);
    
    default double getSpeed() {
        return Math.sqrt(getDx() * getDx() + getDy() * getDy());
    }
    
    default void stop() {
        setDx(0);
        setDy(0);
    }
}

/**
 * 고급 그리기 기능을 위한 인터페이스
 */
public interface Drawable {
    void paint(GraphicsContext gc);
    
    default void paintWithOpacity(GraphicsContext gc, double opacity) {
        double oldOpacity = gc.getGlobalAlpha();
        gc.setGlobalAlpha(opacity);
        paint(gc);
        gc.setGlobalAlpha(oldOpacity);
    }
    
    default void paintWithRotation(GraphicsContext gc, double angle) {
        // 회전 변환 적용하여 그리기
        gc.save();
        gc.rotate(Math.toDegrees(angle));
        paint(gc);
        gc.restore();
    }
}
```

---

## 3. Ball과 Box의 리팩토링

### 3.1 AbstractBall 클래스

```java
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 모든 공 타입의 기본 클래스
 * GameObject를 상속받아 공 공통 기능을 구현합니다.
 */
public abstract class AbstractBall extends GameObject implements Movable, Drawable {
    // === 공 고유 필드들 ===
    protected double radius;               // 반지름
    protected double dx, dy;              // 속도
    protected double mass;                // 질량
    protected double restitution;         // 반발 계수
    protected double friction;            // 마찰 계수
    
    // === 시각적 속성 ===
    protected boolean showBounds;         // 경계 표시 여부
    protected Color boundsColor;          // 경계 색상
    
    // === 생성자 ===
    
    protected AbstractBall(double x, double y, double radius, Color color) {
        super(x, y, color);
        
        if (radius <= 0) {
            throw new IllegalArgumentException("반지름은 0보다 커야 합니다: " + radius);
        }
        
        this.radius = radius;
        this.dx = 0;
        this.dy = 0;
        this.mass = Math.PI * radius * radius;  // 면적에 비례하는 질량
        this.restitution = 0.8;
        this.friction = 0.99;
        this.showBounds = false;
        this.boundsColor = Color.YELLOW;
    }
    
    // === GameObject 추상 메서드 구현 ===
    
    @Override
    public Bounds getBounds() {
        return new CircleBounds(x, y, radius);
    }
    
    @Override
    public double getArea() {
        return Math.PI * radius * radius;
    }
    
    @Override
    public void update(double deltaTime) {
        // 기본 업데이트: 위치 변경
        move(deltaTime);
        
        // 마찰 적용
        applyFriction(deltaTime);
    }
    
    // === Movable 인터페이스 구현 ===
    
    @Override
    public void move(double deltaTime) {
        x += dx * deltaTime;
        y += dy * deltaTime;
        onPositionChanged();
    }
    
    @Override
    public double getDx() { return dx; }
    
    @Override
    public double getDy() { return dy; }
    
    @Override
    public void setDx(double dx) { this.dx = dx; }
    
    @Override
    public void setDy(double dy) { this.dy = dy; }
    
    // === Drawable 인터페이스 구현 ===
    
    @Override
    public void paint(GraphicsContext gc) {
        // 기본 공 그리기
        paintBall(gc);
        
        // 경계 표시
        if (showBounds) {
            paintBounds(gc);
        }
    }
    
    /**
     * 공 본체를 그립니다 (하위 클래스에서 커스터마이징 가능)
     * @param gc 그래픽스 컨텍스트
     */
    protected void paintBall(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        
        // 기본 테두리
        gc.setStroke(color.darker());
        gc.setLineWidth(1);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
    }
    
    /**
     * 경계를 그립니다
     * @param gc 그래픽스 컨텍스트
     */
    protected void paintBounds(GraphicsContext gc) {
        gc.setStroke(boundsColor);
        gc.setLineWidth(1);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
    }
    
    // === 물리 시뮬레이션 메서드들 ===
    
    /**
     * 마찰을 적용합니다
     * @param deltaTime 경과 시간
     */
    protected void applyFriction(double deltaTime) {
        double frictionFactor = Math.pow(friction, deltaTime);
        dx *= frictionFactor;
        dy *= frictionFactor;
        
        // 매우 작은 속도는 0으로 처리
        if (Math.abs(dx) < 0.1) dx = 0;
        if (Math.abs(dy) < 0.1) dy = 0;
    }
    
    /**
     * 힘을 적용합니다
     * @param forceX X 방향 힘
     * @param forceY Y 방향 힘
     * @param deltaTime 적용 시간
     */
    public void applyForce(double forceX, double forceY, double deltaTime) {
        if (mass <= 0) return;
        
        double accelerationX = forceX / mass;
        double accelerationY = forceY / mass;
        
        dx += accelerationX * deltaTime;
        dy += accelerationY * deltaTime;
    }
    
    /**
     * 충격량을 적용합니다
     * @param impulseX X 방향 충격량
     * @param impulseY Y 방향 충격량
     */
    public void applyImpulse(double impulseX, double impulseY) {
        if (mass <= 0) return;
        
        dx += impulseX / mass;
        dy += impulseY / mass;
    }
    
    /**
     * 속도를 반사시킵니다
     * @param normalX 반사면 법선 X
     * @param normalY 반사면 법선 Y
     */
    public void reflect(double normalX, double normalY) {
        // 법선 벡터 정규화
        double length = Math.sqrt(normalX * normalX + normalY * normalY);
        if (length == 0) return;
        
        normalX /= length;
        normalY /= length;
        
        // 법선 방향 속도 성분
        double dotProduct = dx * normalX + dy * normalY;
        
        // 반사
        dx -= (1 + restitution) * dotProduct * normalX;
        dy -= (1 + restitution) * dotProduct * normalY;
    }
    
    // === 유틸리티 메서드들 ===
    
    /**
     * 다른 공과의 탄성 충돌을 처리합니다
     * @param other 다른 공
     */
    public void handleElasticCollision(AbstractBall other) {
        if (!isColliding(other)) return;
        
        // 충돌 벡터 계산
        double dx = other.x - this.x;
        double dy = other.y - this.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance == 0) return;
        
        // 정규화된 충돌 벡터
        double normalX = dx / distance;
        double normalY = dy / distance;
        
        // 분리
        double overlap = (radius + other.radius) - distance;
        if (overlap > 0) {
            double separation = overlap / 2;
            this.x -= normalX * separation;
            this.y -= normalY * separation;
            other.x += normalX * separation;
            other.y += normalY * separation;
            onPositionChanged();
            other.onPositionChanged();
        }
        
        // 속도 교환 (간단한 탄성 충돌)
        double relativeVelX = other.dx - this.dx;
        double relativeVelY = other.dy - this.dy;
        
        double normalVelocity = relativeVelX * normalX + relativeVelY * normalY;
        
        if (normalVelocity > 0) return;  // 이미 분리되고 있음
        
        double combinedRestitution = Math.min(restitution, other.restitution);
        double impulse = -(1 + combinedRestitution) * normalVelocity;
        impulse /= (1 / mass + 1 / other.mass);
        
        double impulseX = impulse * normalX;
        double impulseY = impulse * normalY;
        
        this.dx -= impulseX / mass;
        this.dy -= impulseY / mass;
        other.dx += impulseX / other.mass;
        other.dy += impulseY / other.mass;
    }
    
    // === Getter/Setter ===
    
    public double getRadius() { return radius; }
    public double getMass() { return mass; }
    public double getRestitution() { return restitution; }
    public double getFriction() { return friction; }
    public boolean isShowBounds() { return showBounds; }
    
    public void setMass(double mass) {
        if (mass > 0) this.mass = mass;
    }
    
    public void setRestitution(double restitution) {
        this.restitution = Math.max(0, Math.min(1, restitution));
    }
    
    public void setFriction(double friction) {
        this.friction = Math.max(0, Math.min(1, friction));
    }
    
    public void setShowBounds(boolean show) {
        this.showBounds = show;
    }
    
    @Override
    public String toString() {
        return String.format("%s[id=%s, pos=(%.2f,%.2f), radius=%.2f, vel=(%.2f,%.2f)]",
                           getObjectType(), getId(), x, y, radius, dx, dy);
    }
}
```

### 3.2 구체적인 Ball 클래스들

```java
/**
 * 기본 공 구현
 */
public class SimpleBall extends AbstractBall {
    
    public SimpleBall(double x, double y, double radius, Color color) {
        super(x, y, radius, color);
    }
    
    public SimpleBall(double x, double y, double radius) {
        this(x, y, radius, Color.RED);
    }
    
    // AbstractBall의 기본 구현을 그대로 사용
}

/**
 * 특수 효과가 있는 공
 */
public class GlowingBall extends AbstractBall {
    private double glowIntensity;
    private double glowPhase;
    
    public GlowingBall(double x, double y, double radius, Color color) {
        super(x, y, radius, color);
        this.glowIntensity = 0.5;
        this.glowPhase = 0;
    }
    
    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);
        
        // 글로우 효과 애니메이션
        glowPhase += deltaTime * 3; // 3 라디안/초
        glowIntensity = 0.3 + 0.2 * Math.sin(glowPhase);
    }
    
    @Override
    protected void paintBall(GraphicsContext gc) {
        // 글로우 효과
        Color glowColor = color.deriveColor(0, 1, 1, glowIntensity);
        gc.setFill(glowColor);
        gc.fillOval(x - radius * 1.2, y - radius * 1.2, 
                   radius * 2.4, radius * 2.4);
        
        // 기본 공 그리기
        super.paintBall(gc);
    }
}

/**
 * 궤적을 남기는 공
 */
public class TrailBall extends AbstractBall {
    private List<Point2D> trail;
    private int maxTrailLength;
    private Color trailColor;
    
    public TrailBall(double x, double y, double radius, Color color) {
        super(x, y, radius, color);
        this.trail = new ArrayList<>();
        this.maxTrailLength = 20;
        this.trailColor = color.deriveColor(0, 1, 1, 0.5);
    }
    
    @Override
    protected void onPositionChanged() {
        super.onPositionChanged();
        
        // 궤적에 현재 위치 추가
        trail.add(new Point2D(x, y));
        
        // 최대 길이 제한
        while (trail.size() > maxTrailLength) {
            trail.remove(0);
        }
    }
    
    @Override
    public void paint(GraphicsContext gc) {
        // 궤적 그리기
        paintTrail(gc);
        
        // 공 그리기
        super.paint(gc);
    }
    
    private void paintTrail(GraphicsContext gc) {
        if (trail.size() < 2) return;
        
        gc.setStroke(trailColor);
        
        for (int i = 1; i < trail.size(); i++) {
            Point2D prev = trail.get(i - 1);
            Point2D curr = trail.get(i);
            
            // 점점 얇아지는 선
            double alpha = (double) i / trail.size();
            gc.setLineWidth(alpha * 3);
            gc.setGlobalAlpha(alpha * 0.7);
            
            gc.strokeLine(prev.getX(), prev.getY(), 
                         curr.getX(), curr.getY());
        }
        
        gc.setGlobalAlpha(1.0);
    }
}
```

### 3.3 AbstractBox 클래스

```java
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 모든 박스 타입의 기본 클래스
 */
public abstract class AbstractBox extends GameObject implements Drawable {
    // === 박스 고유 필드들 ===
    protected double width;               
    protected double height;              
    protected boolean filled;             // 채우기 여부
    protected Color strokeColor;          // 테두리 색상
    protected double strokeWidth;         // 테두리 두께
    
    // === 생성자 ===
    
    protected AbstractBox(double x, double y, double width, double height, Color color) {
        super(x, y, color);
        
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("너비와 높이는 0보다 커야 합니다");
        }
        
        this.width = width;
        this.height = height;
        this.filled = true;
        this.strokeColor = color.darker();
        this.strokeWidth = 1.0;
    }
    
    // === GameObject 추상 메서드 구현 ===
    
    @Override
    public Bounds getBounds() {
        return new RectangleBounds(x, y, width, height);
    }
    
    @Override
    public double getArea() {
        return width * height;
    }
    
    @Override
    public void update(double deltaTime) {
        // 기본 박스는 업데이트할 것이 없음
    }
    
    // === Drawable 인터페이스 구현 ===
    
    @Override
    public void paint(GraphicsContext gc) {
        if (filled) {
            gc.setFill(color);
            gc.fillRect(x, y, width, height);
        }
        
        if (strokeWidth > 0) {
            gc.setStroke(strokeColor);
            gc.setLineWidth(strokeWidth);
            gc.strokeRect(x, y, width, height);
        }
    }
    
    // === 유틸리티 메서드들 ===
    
    /**
     * 박스 중심점을 반환합니다
     * @return 중심점
     */
    public Point2D getCenter() {
        return new Point2D(x + width / 2, y + height / 2);
    }
    
    /**
     * 박스의 네 모서리를 반환합니다
     * @return 모서리 점들
     */
    public Point2D[] getCorners() {
        return new Point2D[] {
            new Point2D(x, y),                    // 좌상
            new Point2D(x + width, y),            // 우상
            new Point2D(x + width, y + height),   // 우하
            new Point2D(x, y + height)            // 좌하
        };
    }
    
    // === Getter/Setter ===
    
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public boolean isFilled() { return filled; }
    public Color getStrokeColor() { return strokeColor; }
    public double getStrokeWidth() { return strokeWidth; }
    
    public void setFilled(boolean filled) { this.filled = filled; }
    public void setStrokeColor(Color color) { this.strokeColor = color; }
    public void setStrokeWidth(double width) { this.strokeWidth = Math.max(0, width); }
    
    @Override
    public String toString() {
        return String.format("%s[id=%s, pos=(%.2f,%.2f), size=(%.2f,%.2f)]",
                           getObjectType(), getId(), x, y, width, height);
    }
}

/**
 * 기본 박스 구현
 */
public class SimpleBox extends AbstractBox {
    
    public SimpleBox(double x, double y, double width, double height, Color color) {
        super(x, y, width, height, color);
    }
    
    public SimpleBox(double x, double y, double width, double height) {
        this(x, y, width, height, Color.GRAY);
    }
}

/**
 * 움직이는 박스
 */
public class MovableBox extends AbstractBox implements Movable {
    private double dx, dy;
    private double mass;
    
    public MovableBox(double x, double y, double width, double height, Color color) {
        super(x, y, width, height, color);
        this.dx = 0;
        this.dy = 0;
        this.mass = width * height;  // 면적에 비례하는 질량
    }
    
    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);
        move(deltaTime);
    }
    
    @Override
    public void move(double deltaTime) {
        x += dx * deltaTime;
        y += dy * deltaTime;
        onPositionChanged();
    }
    
    @Override
    public double getDx() { return dx; }
    
    @Override
    public double getDy() { return dy; }
    
    @Override
    public void setDx(double dx) { this.dx = dx; }
    
    @Override
    public void setDy(double dy) { this.dy = dy; }
    
    public double getMass() { return mass; }
    public void setMass(double mass) { 
        if (mass > 0) this.mass = mass; 
    }
}
```

---

## 4. AbstractWorld - 템플릿 메서드 패턴

### 4.1 AbstractWorld 완전 구현

```java
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 모든 게임 세계의 기본 클래스
 * 템플릿 메서드 패턴을 사용하여 게임 루프의 구조를 정의합니다.
 */
public abstract class AbstractWorld {
    // === 기본 속성 ===
    protected final double width;
    protected final double height;
    protected final List<GameObject> gameObjects;
    protected Color backgroundColor;
    
    // === 시간 관리 ===
    protected double totalTime;
    protected long frameCount;
    
    // === 설정 ===
    protected boolean paused;
    protected boolean debugMode;
    protected boolean sortByZOrder;
    
    // === 통계 ===
    protected int addedThisFrame;
    protected int removedThisFrame;
    protected double lastUpdateTime;
    protected double lastRenderTime;
    
    // === 생성자 ===
    
    protected AbstractWorld(double width, double height) {
        this.width = width;
        this.height = height;
        this.gameObjects = new CopyOnWriteArrayList<>();  // 동시 수정 안전
        this.backgroundColor = Color.LIGHTGRAY;
        this.totalTime = 0;
        this.frameCount = 0;
        this.paused = false;
        this.debugMode = false;
        this.sortByZOrder = true;
        
        // 초기화
        initialize();
    }
    
    // === 추상 메서드들 (하위 클래스에서 구현) ===
    
    /**
     * 세계별 초기 설정을 수행합니다
     */
    protected abstract void initialize();
    
    /**
     * 세계별 물리 법칙을 적용합니다
     * @param deltaTime 경과 시간
     */
    protected abstract void applyPhysics(double deltaTime);
    
    /**
     * 충돌 검사 및 처리를 수행합니다
     */
    protected abstract void handleCollisions();
    
    /**
     * 세계별 추가 업데이트 로직을 수행합니다
     * @param deltaTime 경과 시간
     */
    protected abstract void updateWorld(double deltaTime);
    
    /**
     * 세계별 추가 렌더링을 수행합니다
     * @param gc 그래픽스 컨텍스트
     */
    protected abstract void renderWorld(GraphicsContext gc);
    
    // === 템플릿 메서드 (최종 게임 루프 구조) ===
    
    /**
     * 게임 루프의 업데이트 단계 (템플릿 메서드)
     * @param deltaTime 경과 시간
     */
    public final void update(double deltaTime) {
        if (paused || deltaTime <= 0) return;
        
        long startTime = System.nanoTime();
        
        // 프레임 시작 처리
        beginFrame(deltaTime);
        
        // 1단계: 물리 법칙 적용
        applyPhysics(deltaTime);
        
        // 2단계: 모든 객체 업데이트
        updateAllObjects(deltaTime);
        
        // 3단계: 충돌 처리
        handleCollisions();
        
        // 4단계: 세계별 업데이트
        updateWorld(deltaTime);
        
        // 5단계: 비활성 객체 정리
        cleanupInactiveObjects();
        
        // 프레임 종료 처리
        endFrame(deltaTime);
        
        lastUpdateTime = (System.nanoTime() - startTime) / 1_000_000.0;  // ms
    }
    
    /**
     * 게임 루프의 렌더링 단계 (템플릿 메서드)
     * @param gc 그래픽스 컨텍스트
     */
    public final void render(GraphicsContext gc) {
        if (gc == null) return;
        
        long startTime = System.nanoTime();
        
        // 렌더링 준비
        prepareRender(gc);
        
        // 배경 그리기
        renderBackground(gc);
        
        // 모든 객체 렌더링
        renderAllObjects(gc);
        
        // 세계별 렌더링
        renderWorld(gc);
        
        // 디버그 정보 표시
        if (debugMode) {
            renderDebugInfo(gc);
        }
        
        // 렌더링 완료
        finalizeRender(gc);
        
        lastRenderTime = (System.nanoTime() - startTime) / 1_000_000.0;  // ms
    }
    
    // === 훅 메서드들 (하위 클래스에서 선택적으로 오버라이드) ===
    
    /**
     * 프레임 시작 시 호출됩니다
     * @param deltaTime 경과 시간
     */
    protected void beginFrame(double deltaTime) {
        totalTime += deltaTime;
        frameCount++;
        addedThisFrame = 0;
        removedThisFrame = 0;
    }
    
    /**
     * 프레임 종료 시 호출됩니다
     * @param deltaTime 경과 시간
     */
    protected void endFrame(double deltaTime) {
        // 기본 구현은 비어있음
    }
    
    /**
     * 렌더링 준비 작업을 수행합니다
     * @param gc 그래픽스 컨텍스트
     */
    protected void prepareRender(GraphicsContext gc) {
        // Z-order 정렬
        if (sortByZOrder) {
            gameObjects.sort(Comparator.comparingInt(GameObject::getZOrder));
        }
    }
    
    /**
     * 렌더링 완료 작업을 수행합니다
     * @param gc 그래픽스 컨텍스트
     */
    protected void finalizeRender(GraphicsContext gc) {
        // 기본 구현은 비어있음
    }
    
    // === 구현된 공통 메서드들 ===
    
    /**
     * 모든 객체를 업데이트합니다
     * @param deltaTime 경과 시간
     */
    protected void updateAllObjects(double deltaTime) {
        for (GameObject obj : gameObjects) {
            if (obj.isActive()) {
                obj.safeUpdate(deltaTime);
            }
        }
    }
    
    /**
     * 배경을 렌더링합니다
     * @param gc 그래픽스 컨텍스트
     */
    protected void renderBackground(GraphicsContext gc) {
        gc.setFill(backgroundColor);
        gc.fillRect(0, 0, width, height);
    }
    
    /**
     * 모든 객체를 렌더링합니다
     * @param gc 그래픽스 컨텍스트
     */
    protected void renderAllObjects(GraphicsContext gc) {
        for (GameObject obj : gameObjects) {
            if (obj.isVisible() && obj.isActive()) {
                obj.safePaint(gc);
            }
        }
    }
    
    /**
     * 비활성 객체들을 정리합니다
     */
    protected void cleanupInactiveObjects() {
        int initialSize = gameObjects.size();
        gameObjects.removeIf(obj -> !obj.isActive());
        removedThisFrame = initialSize - gameObjects.size();
    }
    
    /**
     * 디버그 정보를 렌더링합니다
     * @param gc 그래픽스 컨텍스트
     */
    protected void renderDebugInfo(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.fillRect(5, 5, 200, 120);
        
        gc.setFill(Color.BLACK);
        gc.setFont(javafx.scene.text.Font.font(10));
        
        int line = 1;
        gc.fillText(String.format("Objects: %d", gameObjects.size()), 10, line * 12);
        line++;
        gc.fillText(String.format("Frame: %d", frameCount), 10, line * 12);
        line++;
        gc.fillText(String.format("Time: %.2fs", totalTime), 10, line * 12);
        line++;
        gc.fillText(String.format("Update: %.2fms", lastUpdateTime), 10, line * 12);
        line++;
        gc.fillText(String.format("Render: %.2fms", lastRenderTime), 10, line * 12);
        line++;
        gc.fillText(String.format("Added: %d", addedThisFrame), 10, line * 12);
        line++;
        gc.fillText(String.format("Removed: %d", removedThisFrame), 10, line * 12);
    }
    
    // === 객체 관리 메서드들 ===
    
    /**
     * 객체를 추가합니다
     * @param obj 추가할 객체
     */
    public void addObject(GameObject obj) {
        if (obj != null && !gameObjects.contains(obj)) {
            gameObjects.add(obj);
            addedThisFrame++;
            onObjectAdded(obj);
        }
    }
    
    /**
     * 객체를 제거합니다
     * @param obj 제거할 객체
     * @return 성공적으로 제거되었으면 true
     */
    public boolean removeObject(GameObject obj) {
        if (gameObjects.remove(obj)) {
            removedThisFrame++;
            onObjectRemoved(obj);
            return true;
        }
        return false;
    }
    
    /**
     * 모든 객체를 제거합니다
     */
    public void clearObjects() {
        int count = gameObjects.size();
        gameObjects.clear();
        removedThisFrame += count;
    }
    
    /**
     * 특정 타입의 객체들을 찾습니다
     * @param type 찾을 객체 타입
     * @param <T> 객체 타입
     * @return 해당 타입의 객체 리스트
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getObjectsOfType(Class<T> type) {
        List<T> result = new ArrayList<>();
        for (GameObject obj : gameObjects) {
            if (type.isInstance(obj)) {
                result.add((T) obj);
            }
        }
        return result;
    }
    
    /**
     * 특정 위치의 객체를 찾습니다
     * @param x X 좌표
     * @param y Y 좌표
     * @return 해당 위치의 객체 (없으면 null)
     */
    public GameObject findObjectAt(double x, double y) {
        // 뒤에서부터 검사 (위에 그려진 객체 우선)
        for (int i = gameObjects.size() - 1; i >= 0; i--) {
            GameObject obj = gameObjects.get(i);
            if (obj.isActive() && obj.contains(x, y)) {
                return obj;
            }
        }
        return null;
    }
    
    // === 이벤트 훅 메서드들 ===
    
    /**
     * 객체가 추가될 때 호출됩니다
     * @param obj 추가된 객체
     */
    protected void onObjectAdded(GameObject obj) {
        // 기본 구현은 비어있음
    }
    
    /**
     * 객체가 제거될 때 호출됩니다
     * @param obj 제거된 객체
     */
    protected void onObjectRemoved(GameObject obj) {
        // 기본 구현은 비어있음
    }
    
    // === 상태 관리 ===
    
    public void pause() { paused = true; }
    public void resume() { paused = false; }
    public void togglePause() { paused = !paused; }
    
    public void enableDebugMode() { debugMode = true; }
    public void disableDebugMode() { debugMode = false; }
    public void toggleDebugMode() { debugMode = !debugMode; }
    
    // === Getter/Setter ===
    
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public int getObjectCount() { return gameObjects.size(); }
    public List<GameObject> getObjects() { return new ArrayList<>(gameObjects); }
    public Color getBackgroundColor() { return backgroundColor; }
    public boolean isPaused() { return paused; }
    public boolean isDebugMode() { return debugMode; }
    public double getTotalTime() { return totalTime; }
    public long getFrameCount() { return frameCount; }
    
    public void setBackgroundColor(Color color) {
        if (color != null) this.backgroundColor = color;
    }
    
    public void setSortByZOrder(boolean sort) { this.sortByZOrder = sort; }
}
```

### 4.2 구체적인 World 구현 예제

```java
/**
 * 기본적인 물리 시뮬레이션을 제공하는 세계
 */
public class PhysicsWorld extends AbstractWorld {
    private double gravity;
    private boolean gravityEnabled;
    private double airResistance;
    
    public PhysicsWorld(double width, double height) {
        super(width, height);
        this.gravity = 98.0;
        this.gravityEnabled = true;
        this.airResistance = 0.01;
    }
    
    @Override
    protected void initialize() {
        // 경계 벽 생성
        createBoundaryWalls();
    }
    
    private void createBoundaryWalls() {
        double thickness = 20;
        
        // 경계 벽들 추가
        addObject(new SimpleBox(-thickness, -thickness, width + 2*thickness, thickness));
        addObject(new SimpleBox(-thickness, height, width + 2*thickness, thickness));
        addObject(new SimpleBox(-thickness, 0, thickness, height));
        addObject(new SimpleBox(width, 0, thickness, height));
    }
    
    @Override
    protected void applyPhysics(double deltaTime) {
        for (GameObject obj : getObjects()) {
            if (obj instanceof AbstractBall && obj.isActive()) {
                AbstractBall ball = (AbstractBall) obj;
                
                // 중력 적용
                if (gravityEnabled) {
                    ball.applyForce(0, ball.getMass() * gravity, deltaTime);
                }
                
                // 공기 저항 적용
                if (airResistance > 0) {
                    double speed = ball.getSpeed();
                    if (speed > 0) {
                        double resistance = airResistance * speed * speed * ball.getMass();
                        double angle = Math.atan2(ball.getDy(), ball.getDx());
                        ball.applyForce(-resistance * Math.cos(angle), 
                                       -resistance * Math.sin(angle), deltaTime);
                    }
                }
            }
        }
    }
    
    @Override
    protected void handleCollisions() {
        List<AbstractBall> balls = getObjectsOfType(AbstractBall.class);
        List<AbstractBox> boxes = getObjectsOfType(AbstractBox.class);
        
        // 공-공 충돌
        for (int i = 0; i < balls.size(); i++) {
            for (int j = i + 1; j < balls.size(); j++) {
                AbstractBall ball1 = balls.get(i);
                AbstractBall ball2 = balls.get(j);
                
                if (ball1.isColliding(ball2)) {
                    ball1.handleElasticCollision(ball2);
                }
            }
        }
        
        // 공-박스 충돌
        for (AbstractBall ball : balls) {
            for (AbstractBox box : boxes) {
                if (ball.isColliding(box)) {
                    handleBallBoxCollision(ball, box);
                }
            }
        }
    }
    
    private void handleBallBoxCollision(AbstractBall ball, AbstractBox box) {
        // 간단한 박스 충돌 처리
        RectangleBounds boxBounds = (RectangleBounds) box.getBounds();
        
        double ballCenterX = ball.getX();
        double ballCenterY = ball.getY();
        
        // 가장 가까운 박스 면 결정
        double leftDist = Math.abs(ballCenterX - boxBounds.getX());
        double rightDist = Math.abs(ballCenterX - (boxBounds.getX() + boxBounds.getWidth()));
        double topDist = Math.abs(ballCenterY - boxBounds.getY());
        double bottomDist = Math.abs(ballCenterY - (boxBounds.getY() + boxBounds.getHeight()));
        
        double minDist = Math.min(Math.min(leftDist, rightDist), Math.min(topDist, bottomDist));
        
        if (minDist == leftDist || minDist == rightDist) {
            ball.reflect(minDist == leftDist ? -1 : 1, 0);
        } else {
            ball.reflect(0, minDist == topDist ? -1 : 1);
        }
    }
    
    @Override
    protected void updateWorld(double deltaTime) {
        // 추가적인 세계 업데이트 로직
    }
    
    @Override
    protected void renderWorld(GraphicsContext gc) {
        // 추가적인 렌더링
    }
    
    // === Getter/Setter ===
    
    public double getGravity() { return gravity; }
    public void setGravity(double gravity) { this.gravity = gravity; }
    
    public boolean isGravityEnabled() { return gravityEnabled; }
    public void setGravityEnabled(boolean enabled) { this.gravityEnabled = enabled; }
    
    public double getAirResistance() { return airResistance; }
    public void setAirResistance(double resistance) { this.airResistance = Math.max(0, resistance); }
}
```

---

## 5. 테스트 코드와 검증

### 5.1 GameObject 추상 클래스 테스트

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

// 테스트용 구체 클래스
class TestGameObject extends GameObject {
    private double area;
    
    public TestGameObject(double x, double y, Color color, double area) {
        super(x, y, color);
        this.area = area;
    }
    
    @Override
    public void paint(GraphicsContext gc) {
        // 테스트용 빈 구현
    }
    
    @Override
    public Bounds getBounds() {
        return new CircleBounds(x, y, 10);
    }
    
    @Override
    public void update(double deltaTime) {
        // 테스트용 빈 구현
    }
    
    @Override
    public double getArea() {
        return area;
    }
}

public class GameObjectTest {
    private TestGameObject obj1;
    private TestGameObject obj2;
    
    @BeforeEach
    void setUp() {
        obj1 = new TestGameObject(100, 100, Color.RED, 25.0);
        obj2 = new TestGameObject(150, 150, Color.BLUE, 36.0);
    }
    
    @Test
    @DisplayName("GameObject 기본 생성 테스트")
    void testGameObjectCreation() {
        assertEquals(100, obj1.getX(), 0.001);
        assertEquals(100, obj1.getY(), 0.001);
        assertEquals(Color.RED, obj1.getColor());
        assertTrue(obj1.isActive());
        assertTrue(obj1.isVisible());
        assertNotNull(obj1.getId());
    }
    
    @Test
    @DisplayName("위치 이동 테스트")
    void testPositionMovement() {
        obj1.moveTo(200, 250);
        assertEquals(200, obj1.getX(), 0.001);
        assertEquals(250, obj1.getY(), 0.001);
        
        obj1.moveBy(50, -25);
        assertEquals(250, obj1.getX(), 0.001);
        assertEquals(225, obj1.getY(), 0.001);
    }
    
    @Test
    @DisplayName("상태 관리 테스트")
    void testStateManagement() {
        assertTrue(obj1.isActive());
        
        obj1.deactivate();
        assertFalse(obj1.isActive());
        
        obj1.activate();
        assertTrue(obj1.isActive());
        
        obj1.hide();
        assertFalse(obj1.isVisible());
        
        obj1.show();
        assertTrue(obj1.isVisible());
    }
    
    @Test
    @DisplayName("거리 계산 테스트")
    void testDistanceCalculation() {
        double distance = obj1.distanceTo(obj2);
        double expected = Math.sqrt(50 * 50 + 50 * 50);  // 50√2
        assertEquals(expected, distance, 0.001);
    }
    
    @Test
    @DisplayName("충돌 검사 테스트")
    void testCollisionDetection() {
        // 멀리 떨어진 객체들
        assertFalse(obj1.isColliding(obj2));
        
        // 가까운 객체들
        obj2.moveTo(105, 105);
        assertTrue(obj1.isColliding(obj2));
        
        // 비활성 객체와는 충돌 없음
        obj2.deactivate();
        assertFalse(obj1.isColliding(obj2));
    }
    
    @Test
    @DisplayName("점 포함 테스트")
    void testPointContainment() {
        assertTrue(obj1.contains(100, 100));  // 중심
        assertTrue(obj1.contains(105, 105));  // 내부
        assertFalse(obj1.contains(120, 120)); // 외부
    }
    
    @Test
    @DisplayName("생존 시간 테스트")
    void testAge() throws InterruptedException {
        long initialAge = obj1.getAge();
        Thread.sleep(10);
        long laterAge = obj1.getAge();
        
        assertTrue(laterAge > initialAge);
    }
    
    @Test
    @DisplayName("템플릿 메서드 테스트")
    void testTemplateMethod() {
        GraphicsContext gc = Mockito.mock(GraphicsContext.class);
        
        // 정상적인 렌더링
        assertDoesNotThrow(() -> obj1.safePaint(gc));
        
        // 비활성 객체는 렌더링되지 않음
        obj1.deactivate();
        assertDoesNotThrow(() -> obj1.safePaint(gc));
        
        // null GC는 안전하게 처리됨
        assertDoesNotThrow(() -> obj1.safePaint(null));
    }
}
```

### 5.2 AbstractBall 테스트

```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AbstractBallTest {
    private SimpleBall ball1;
    private SimpleBall ball2;
    
    @BeforeEach
    void setUp() {
        ball1 = new SimpleBall(100, 100, 20, Color.RED);
        ball2 = new SimpleBall(130, 100, 15, Color.BLUE);
    }
    
    @Test
    @DisplayName("AbstractBall 생성 테스트")
    void testAbstractBallCreation() {
        assertEquals(100, ball1.getX(), 0.001);
        assertEquals(100, ball1.getY(), 0.001);
        assertEquals(20, ball1.getRadius(), 0.001);
        assertEquals(Color.RED, ball1.getColor());
        assertEquals(0, ball1.getDx(), 0.001);
        assertEquals(0, ball1.getDy(), 0.001);
    }
    
    @Test
    @DisplayName("Movable 인터페이스 구현 테스트")
    void testMovableInterface() {
        ball1.setDx(50);
        ball1.setDy(30);
        
        assertEquals(50, ball1.getDx(), 0.001);
        assertEquals(30, ball1.getDy(), 0.001);
        
        double expectedSpeed = Math.sqrt(50*50 + 30*30);
        assertEquals(expectedSpeed, ball1.getSpeed(), 0.001);
        
        ball1.stop();
        assertEquals(0, ball1.getDx(), 0.001);
        assertEquals(0, ball1.getDy(), 0.001);
    }
    
    @Test
    @DisplayName("이동 기능 테스트")
    void testMovement() {
        ball1.setDx(40);
        ball1.setDy(60);
        
        ball1.move(1.0);
        
        assertEquals(140, ball1.getX(), 0.001);
        assertEquals(160, ball1.getY(), 0.001);
    }
    
    @Test
    @DisplayName("물리 시뮬레이션 테스트")
    void testPhysicsSimulation() {
        ball1.setMass(10);
        ball1.setDx(0);
        ball1.setDy(0);
        
        // 힘 적용 테스트
        ball1.applyForce(100, 50, 1.0);
        assertEquals(10, ball1.getDx(), 0.001);  // F/m = 100/10 = 10
        assertEquals(5, ball1.getDy(), 0.001);   // F/m = 50/10 = 5
        
        // 충격량 적용 테스트
        ball1.applyImpulse(50, 30);
        assertEquals(15, ball1.getDx(), 0.001);  // 10 + 50/10 = 15
        assertEquals(8, ball1.getDy(), 0.001);   // 5 + 30/10 = 8
    }
    
    @Test
    @DisplayName("반사 기능 테스트")
    void testReflection() {
        ball1.setDx(10);
        ball1.setDy(5);
        ball1.setRestitution(1.0);  // 완전 탄성
        
        // 수직 벽 반사
        ball1.reflect(-1, 0);
        assertEquals(-10, ball1.getDx(), 0.001);
        assertEquals(5, ball1.getDy(), 0.001);
        
        // 수평 벽 반사
        ball1.reflect(0, -1);
        assertEquals(-10, ball1.getDx(), 0.001);
        assertEquals(-5, ball1.getDy(), 0.001);
    }
    
    @Test
    @DisplayName("탄성 충돌 테스트")
    void testElasticCollision() {
        ball1.setDx(50);
        ball1.setDy(0);
        ball1.setMass(10);
        
        ball2.setDx(-30);
        ball2.setDy(0);
        ball2.setMass(15);
        
        // 충돌 전 운동량
        double momentumBefore = ball1.getMass() * ball1.getDx() + ball2.getMass() * ball2.getDx();
        
        ball1.handleElasticCollision(ball2);
        
        // 충돌 후 운동량 보존 확인
        double momentumAfter = ball1.getMass() * ball1.getDx() + ball2.getMass() * ball2.getDx();
        assertEquals(momentumBefore, momentumAfter, 0.1);
        
        // 속도가 변경되었는지 확인
        assertNotEquals(50, ball1.getDx(), 0.001);
        assertNotEquals(-30, ball2.getDx(), 0.001);
    }
    
    @Test
    @DisplayName("마찰 적용 테스트")
    void testFriction() {
        ball1.setDx(100);
        ball1.setDy(100);
        ball1.setFriction(0.9);  // 90% 속도 유지
        
        double initialSpeed = ball1.getSpeed();
        ball1.update(1.0);  // 1초 업데이트
        double finalSpeed = ball1.getSpeed();
        
        assertTrue(finalSpeed < initialSpeed);
    }
}
```

### 5.3 AbstractWorld 테스트

```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// 테스트용 World 구현
class TestWorld extends AbstractWorld {
    public boolean initializeCalled = false;
    public boolean applyPhysicsCalled = false;
    public boolean handleCollisionsCalled = false;
    public boolean updateWorldCalled = false;
    public boolean renderWorldCalled = false;
    
    public TestWorld(double width, double height) {
        super(width, height);
    }
    
    @Override
    protected void initialize() {
        initializeCalled = true;
    }
    
    @Override
    protected void applyPhysics(double deltaTime) {
        applyPhysicsCalled = true;
    }
    
    @Override
    protected void handleCollisions() {
        handleCollisionsCalled = true;
    }
    
    @Override
    protected void updateWorld(double deltaTime) {
        updateWorldCalled = true;
    }
    
    @Override
    protected void renderWorld(GraphicsContext gc) {
        renderWorldCalled = true;
    }
}

public class AbstractWorldTest {
    private TestWorld world;
    private SimpleBall ball1;
    private SimpleBall ball2;
    
    @BeforeEach
    void setUp() {
        world = new TestWorld(800, 600);
        ball1 = new SimpleBall(100, 100, 20, Color.RED);
        ball2 = new SimpleBall(200, 200, 15, Color.BLUE);
    }
    
    @Test
    @DisplayName("World 생성 및 초기화 테스트")
    void testWorldCreation() {
        assertEquals(800, world.getWidth(), 0.001);
        assertEquals(600, world.getHeight(), 0.001);
        assertEquals(0, world.getObjectCount());
        assertFalse(world.isPaused());
        assertFalse(world.isDebugMode());
        assertTrue(world.initializeCalled);
    }
    
    @Test
    @DisplayName("객체 관리 테스트")
    void testObjectManagement() {
        // 객체 추가
        world.addObject(ball1);
        assertEquals(1, world.getObjectCount());
        assertTrue(world.getObjects().contains(ball1));
        
        world.addObject(ball2);
        assertEquals(2, world.getObjectCount());
        
        // 중복 추가 방지
        world.addObject(ball1);
        assertEquals(2, world.getObjectCount());
        
        // 객체 제거
        assertTrue(world.removeObject(ball1));
        assertEquals(1, world.getObjectCount());
        assertFalse(world.getObjects().contains(ball1));
        
        // 없는 객체 제거
        assertFalse(world.removeObject(ball1));
        assertEquals(1, world.getObjectCount());
        
        // 모든 객체 제거
        world.clearObjects();
        assertEquals(0, world.getObjectCount());
    }
    
    @Test
    @DisplayName("타입별 객체 검색 테스트")
    void testObjectTypeSearch() {
        SimpleBall ball3 = new SimpleBall(300, 300, 25);
        SimpleBox box1 = new SimpleBox(400, 400, 50, 50);
        
        world.addObject(ball1);
        world.addObject(ball2);
        world.addObject(ball3);
        world.addObject(box1);
        
        List<SimpleBall> balls = world.getObjectsOfType(SimpleBall.class);
        assertEquals(3, balls.size());
        
        List<SimpleBox> boxes = world.getObjectsOfType(SimpleBox.class);
        assertEquals(1, boxes.size());
        
        List<GameObject> allObjects = world.getObjectsOfType(GameObject.class);
        assertEquals(4, allObjects.size());
    }
    
    @Test
    @DisplayName("위치 기반 객체 검색 테스트")
    void testPositionBasedSearch() {
        world.addObject(ball1);  // (100, 100, r=20)
        world.addObject(ball2);  // (200, 200, r=15)
        
        // ball1 중심에서 검색
        GameObject found = world.findObjectAt(100, 100);
        assertEquals(ball1, found);
        
        // ball2 근처에서 검색
        found = world.findObjectAt(195, 195);
        assertEquals(ball2, found);
        
        // 빈 공간에서 검색
        found = world.findObjectAt(50, 50);
        assertNull(found);
    }
    
    @Test
    @DisplayName("템플릿 메서드 패턴 테스트")
    void testTemplateMethodPattern() {
        world.addObject(ball1);
        
        // 업데이트 템플릿 메서드 실행
        world.update(0.016);
        
        assertTrue(world.applyPhysicsCalled);
        assertTrue(world.handleCollisionsCalled);
        assertTrue(world.updateWorldCalled);
        assertTrue(world.getFrameCount() > 0);
        assertTrue(world.getTotalTime() > 0);
        
        // 렌더링 템플릿 메서드 실행
        GraphicsContext gc = Mockito.mock(GraphicsContext.class);
        world.render(gc);
        
        assertTrue(world.renderWorldCalled);
    }
    
    @Test
    @DisplayName("일시정지 기능 테스트")
    void testPauseFeature() {
        world.addObject(ball1);
        ball1.setDx(50);
        
        // 정상 업데이트
        double initialX = ball1.getX();
        world.update(1.0);
        assertNotEquals(initialX, ball1.getX());
        
        // 일시정지 후 업데이트
        world.pause();
        double pausedX = ball1.getX();
        world.update(1.0);
        assertEquals(pausedX, ball1.getX(), 0.001);  // 위치 변화 없음
        
        // 재개 후 업데이트
        world.resume();
        world.update(1.0);
        assertNotEquals(pausedX, ball1.getX());
    }
    
    @Test
    @DisplayName("비활성 객체 정리 테스트")
    void testInactiveObjectCleanup() {
        world.addObject(ball1);
        world.addObject(ball2);
        assertEquals(2, world.getObjectCount());
        
        // 하나 비활성화
        ball1.deactivate();
        world.update(0.016);
        
        assertEquals(1, world.getObjectCount());
        assertFalse(world.getObjects().contains(ball1));
        assertTrue(world.getObjects().contains(ball2));
    }
    
    @Test
    @DisplayName("상태 토글 테스트")
    void testStateToggles() {
        assertFalse(world.isPaused());
        world.togglePause();
        assertTrue(world.isPaused());
        world.togglePause();
        assertFalse(world.isPaused());
        
        assertFalse(world.isDebugMode());
        world.toggleDebugMode();
        assertTrue(world.isDebugMode());
        world.toggleDebugMode();
        assertFalse(world.isDebugMode());
    }
}
```

---

## 6. 일반적인 실수와 해결법

### 6.1 추상 클래스 설계 실수

#### ❌ 잘못된 추상화
```java
// 너무 구체적인 추상 클래스
public abstract class RedCircle {
    protected Color color = Color.RED;  // 하드코딩된 색상
    protected double radius = 10;       // 하드코딩된 크기
    // 확장성이 떨어짐
}
```

#### ✅ 올바른 추상화
```java
// 적절한 수준의 추상화
public abstract class Shape {
    protected Color color;              // 유연한 색상
    protected double x, y;              // 공통 위치
    
    protected Shape(double x, double y, Color color) {
        this.x = x; this.y = y; this.color = color;
    }
    
    public abstract double getArea();   // 형태별 구현 필요
    public abstract void paint(GraphicsContext gc);
}
```

### 6.2 템플릿 메서드 패턴 실수

#### ❌ 잘못된 템플릿 메서드
```java
// final 없이 오버라이드 가능한 템플릿 메서드
public void update(double deltaTime) {  // final이 없음!
    step1();
    step2();  // 하위 클래스에서 전체 구조를 바꿀 수 있음
    step3();
}
```

#### ✅ 올바른 템플릿 메서드
```java
// final로 구조 보호
public final void update(double deltaTime) {
    beforeUpdate(deltaTime);  // 훅 메서드
    doUpdate(deltaTime);      // 추상 메서드
    afterUpdate(deltaTime);   // 훅 메서드
}

protected void beforeUpdate(double deltaTime) { }  // 선택적 오버라이드
protected abstract void doUpdate(double deltaTime); // 필수 구현
protected void afterUpdate(double deltaTime) { }   // 선택적 오버라이드
```

### 6.3 상속 계층 설계 실수

#### ❌ 깊은 상속 계층
```java
// 너무 깊은 상속 (5단계)
GameObject → MovableObject → BallObject → ColoredBall → SpecialBall
// 복잡하고 이해하기 어려움
```

#### ✅ 얕은 상속 + 인터페이스
```java
// 얕은 상속 + 인터페이스 조합
GameObject → AbstractBall → SimpleBall
             ↓
             implements Movable, Drawable, Collidable
// 유연하고 이해하기 쉬움
```

### 6.4 추상 메서드 과용

#### ❌ 너무 많은 추상 메서드
```java
public abstract class GameObject {
    public abstract void update();
    public abstract void render();
    public abstract void move();
    public abstract void rotate();
    public abstract void scale();
    public abstract void animate();
    // 모든 하위 클래스가 이 모든 메서드를 구현해야 함
}
```

#### ✅ 필수만 추상, 나머지는 기본 구현
```java
public abstract class GameObject {
    public abstract void update(double deltaTime);  // 필수
    public abstract void paint(GraphicsContext gc);  // 필수
    
    // 선택적 기능들은 기본 구현 제공
    public void rotate(double angle) { }
    public void scale(double factor) { }
    protected void onDestroyed() { }
}
```

### 6.5 훅 메서드 활용 실수

#### ❌ 훅 메서드 미활용
```java
public void setColor(Color color) {
    this.color = color;  // 변경 사실을 하위 클래스가 알 수 없음
}
```

#### ✅ 훅 메서드 활용
```java
public void setColor(Color color) {
    Color oldColor = this.color;
    this.color = color;
    onColorChanged(oldColor, color);  // 훅 메서드 호출
}

protected void onColorChanged(Color oldColor, Color newColor) {
    // 하위 클래스에서 필요시 오버라이드
}
```

### 6.6 실수 방지 체크리스트

- [ ] **추상화 수준**: 너무 구체적이거나 너무 일반적이지 않은가?
- [ ] **템플릿 메서드**: final로 구조를 보호했는가?
- [ ] **훅 메서드**: 확장 포인트를 제공했는가?
- [ ] **추상 메서드**: 정말 필요한 것만 추상으로 만들었는가?
- [ ] **상속 깊이**: 3단계를 넘지 않는가?
- [ ] **인터페이스 활용**: 상속보다 인터페이스가 적합하지 않은가?
- [ ] **코드 중복**: 공통 기능을 효과적으로 공유하는가?

---

## 학습 포인트 정리

### 5장에서 배운 핵심 개념들

1. **추상 클래스의 역할**
   - 공통 기능의 구현과 공유
   - 추상 메서드를 통한 계약 정의
   - 코드 중복 제거와 일관성 확보

2. **템플릿 메서드 패턴**
   - 알고리즘 구조의 고정화
   - 세부 구현의 유연성 제공
   - 프레임워크 설계의 핵심 패턴

3. **상속과 인터페이스의 조합**
   - 상속: 구현 공유와 is-a 관계
   - 인터페이스: 계약 정의와 can-do 관계
   - 둘의 조합으로 최적의 설계

4. **훅 메서드 활용**
   - 확장 포인트 제공
   - 하위 클래스의 선택적 커스터마이징
   - 프레임워크의 유연성 확보

5. **설계 원칙 적용**
   - DRY: 중복 코드 제거
   - SOLID: 단일 책임, 확장/수정 원칙
   - 템플릿 메서드로 구조 안정성

이제 6장에서는 새로운 객체 타입들을 추가하면서 상속만으로는 해결하기 어려운 문제들을 경험해보겠습니다!