# 4장: Bounded World 완전한 구현 가이드

## 목차
1. [학습 목표와 핵심 개념](#1-학습-목표와-핵심-개념)
2. [Bounds 시스템 완전 구현](#2-bounds-시스템-완전-구현)
3. [BoundedBall 클래스 - 경계 인식](#3-boundedball-클래스---경계-인식)
4. [정확한 충돌 검사 알고리즘](#4-정확한-충돌-검사-알고리즘)
5. [BoundedWorld 클래스 - 물리적 경계](#5-boundedworld-클래스---물리적-경계)
6. [테스트 코드와 검증](#6-테스트-코드와-검증)
7. [일반적인 실수와 해결법](#7-일반적인-실수와-해결법)

---

## 1. 학습 목표와 핵심 개념

### 1.1 학습 목표
- **정확한 충돌 검사**: 다양한 형태 간 충돌 감지 알고리즘
- **경계 시스템**: 유연하고 확장 가능한 경계 관리
- **수학적 모델링**: 기하학적 연산의 프로그래밍 구현
- **성능 최적화**: 효율적인 충돌 검사 기법
- **공간 분할**: 넓은 공간에서의 효율적 객체 관리

### 1.2 핵심 개념 요약

**Bounding Volume이란?**
```java
// 복잡한 형태를 간단한 형태로 근사
// 빠른 충돌 검사 → 정확한 충돌 검사 순서로 최적화
if (roughCollisionCheck(obj1, obj2)) {
    if (preciseCollisionCheck(obj1, obj2)) {
        handleCollision(obj1, obj2);
    }
}
```

**AABB (Axis-Aligned Bounding Box)**
- 축에 정렬된 사각형으로 빠른 충돌 검사
- 회전하지 않는 사각형만 지원
- 매우 빠른 연산 속도

---

## 2. Bounds 시스템 완전 구현

### 2.1 Bounds 추상 클래스

```java
/**
 * 모든 경계 타입의 기본 클래스
 * 다양한 형태의 경계를 통일된 인터페이스로 처리합니다.
 */
public abstract class Bounds {
    // === 추상 메서드들 ===
    
    /**
     * 다른 경계와 교차하는지 확인합니다
     * @param other 다른 경계
     * @return 교차하면 true
     */
    public abstract boolean intersects(Bounds other);
    
    /**
     * 주어진 점이 경계 안에 있는지 확인합니다
     * @param x X 좌표
     * @param y Y 좌표
     * @return 점이 경계 안에 있으면 true
     */
    public abstract boolean contains(double x, double y);
    
    /**
     * 경계의 중심점을 반환합니다
     * @return 중심점 좌표
     */
    public abstract Point2D getCenter();
    
    /**
     * 경계를 포함하는 AABB를 반환합니다
     * @return AABB 경계
     */
    public abstract RectangleBounds getAABB();
    
    /**
     * 경계의 면적을 반환합니다
     * @return 면적
     */
    public abstract double getArea();
    
    /**
     * 경계의 둘레를 반환합니다
     * @return 둘레
     */
    public abstract double getPerimeter();
    
    // === 유틸리티 메서드들 ===
    
    /**
     * 다른 경계와의 거리를 계산합니다
     * @param other 다른 경계
     * @return 최단 거리
     */
    public double distanceTo(Bounds other) {
        Point2D thisCenter = getCenter();
        Point2D otherCenter = other.getCenter();
        return thisCenter.distance(otherCenter);
    }
    
    /**
     * 경계가 다른 경계를 완전히 포함하는지 확인합니다
     * @param other 다른 경계
     * @return 완전히 포함하면 true
     */
    public boolean contains(Bounds other) {
        // 기본 구현: 모든 모서리 점이 포함되는지 확인
        RectangleBounds otherAABB = other.getAABB();
        return contains(otherAABB.getX(), otherAABB.getY()) &&
               contains(otherAABB.getX() + otherAABB.getWidth(), otherAABB.getY()) &&
               contains(otherAABB.getX(), otherAABB.getY() + otherAABB.getHeight()) &&
               contains(otherAABB.getX() + otherAABB.getWidth(), otherAABB.getY() + otherAABB.getHeight());
    }
    
    /**
     * 경계를 이동시킵니다
     * @param dx X 방향 이동량
     * @param dy Y 방향 이동량
     * @return 이동된 새 경계
     */
    public abstract Bounds translate(double dx, double dy);
    
    /**
     * 경계를 확대/축소합니다
     * @param scale 배율
     * @return 크기가 변경된 새 경계
     */
    public abstract Bounds scale(double scale);
}

/**
 * 2D 점을 나타내는 간단한 클래스
 */
class Point2D {
    private final double x, y;
    
    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    
    public double distance(Point2D other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    public Point2D add(double dx, double dy) {
        return new Point2D(x + dx, y + dy);
    }
    
    @Override
    public String toString() {
        return String.format("(%.2f, %.2f)", x, y);
    }
}
```

### 2.2 CircleBounds 클래스

```java
/**
 * 원형 경계를 나타내는 클래스
 * 빠른 충돌 검사와 정확한 기하학적 연산을 제공합니다.
 */
public class CircleBounds extends Bounds {
    private final double centerX;
    private final double centerY;
    private final double radius;
    
    /**
     * 원형 경계 생성자
     * @param centerX 중심 X 좌표
     * @param centerY 중심 Y 좌표
     * @param radius 반지름
     */
    public CircleBounds(double centerX, double centerY, double radius) {
        if (radius <= 0) {
            throw new IllegalArgumentException("반지름은 0보다 커야 합니다: " + radius);
        }
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
    }
    
    // === Bounds 추상 메서드 구현 ===
    
    @Override
    public boolean intersects(Bounds other) {
        if (other instanceof CircleBounds) {
            return intersectsCircle((CircleBounds) other);
        } else if (other instanceof RectangleBounds) {
            return intersectsRectangle((RectangleBounds) other);
        }
        // 다른 타입은 상대방의 구현을 사용
        return other.intersects(this);
    }
    
    @Override
    public boolean contains(double x, double y) {
        double dx = x - centerX;
        double dy = y - centerY;
        return (dx * dx + dy * dy) <= (radius * radius);
    }
    
    @Override
    public Point2D getCenter() {
        return new Point2D(centerX, centerY);
    }
    
    @Override
    public RectangleBounds getAABB() {
        return new RectangleBounds(
            centerX - radius, centerY - radius,
            radius * 2, radius * 2
        );
    }
    
    @Override
    public double getArea() {
        return Math.PI * radius * radius;
    }
    
    @Override
    public double getPerimeter() {
        return 2 * Math.PI * radius;
    }
    
    @Override
    public Bounds translate(double dx, double dy) {
        return new CircleBounds(centerX + dx, centerY + dy, radius);
    }
    
    @Override
    public Bounds scale(double scale) {
        if (scale <= 0) {
            throw new IllegalArgumentException("배율은 0보다 커야 합니다: " + scale);
        }
        return new CircleBounds(centerX, centerY, radius * scale);
    }
    
    // === 특화된 충돌 검사 메서드들 ===
    
    /**
     * 다른 원과의 교차 검사
     * @param other 다른 원
     * @return 교차하면 true
     */
    private boolean intersectsCircle(CircleBounds other) {
        double dx = centerX - other.centerX;
        double dy = centerY - other.centerY;
        double distanceSquared = dx * dx + dy * dy;
        double radiusSum = radius + other.radius;
        return distanceSquared <= (radiusSum * radiusSum);
    }
    
    /**
     * 사각형과의 교차 검사
     * @param rect 사각형
     * @return 교차하면 true
     */
    private boolean intersectsRectangle(RectangleBounds rect) {
        // 원의 중심에서 사각형의 가장 가까운 점까지의 거리 계산
        double closestX = Math.max(rect.getX(), 
                         Math.min(centerX, rect.getX() + rect.getWidth()));
        double closestY = Math.max(rect.getY(), 
                         Math.min(centerY, rect.getY() + rect.getHeight()));
        
        double dx = centerX - closestX;
        double dy = centerY - closestY;
        
        return (dx * dx + dy * dy) <= (radius * radius);
    }
    
    // === 추가 유틸리티 메서드들 ===
    
    /**
     * 다른 원과의 중심간 거리를 계산합니다
     * @param other 다른 원
     * @return 중심간 거리
     */
    public double distanceToCenter(CircleBounds other) {
        double dx = centerX - other.centerX;
        double dy = centerY - other.centerY;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * 다른 원과의 겹침 정도를 계산합니다
     * @param other 다른 원
     * @return 겹침 깊이 (음수면 분리됨)
     */
    public double getOverlapDepth(CircleBounds other) {
        double distance = distanceToCenter(other);
        return (radius + other.radius) - distance;
    }
    
    /**
     * 원 위의 특정 각도에 해당하는 점을 반환합니다
     * @param angle 각도 (라디안)
     * @return 원 위의 점
     */
    public Point2D getPointOnCircle(double angle) {
        double x = centerX + radius * Math.cos(angle);
        double y = centerY + radius * Math.sin(angle);
        return new Point2D(x, y);
    }
    
    /**
     * 특정 점에서 원의 중심으로의 방향을 계산합니다
     * @param x X 좌표
     * @param y Y 좌표
     * @return 방향 각도 (라디안)
     */
    public double getDirectionFromPoint(double x, double y) {
        return Math.atan2(centerY - y, centerX - x);
    }
    
    // === 접근자 메서드들 ===
    
    public double getCenterX() { return centerX; }
    public double getCenterY() { return centerY; }
    public double getRadius() { return radius; }
    
    @Override
    public String toString() {
        return String.format("CircleBounds[center=(%.2f, %.2f), radius=%.2f]", 
                           centerX, centerY, radius);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CircleBounds)) return false;
        
        CircleBounds other = (CircleBounds) obj;
        return Double.compare(other.centerX, centerX) == 0 &&
               Double.compare(other.centerY, centerY) == 0 &&
               Double.compare(other.radius, radius) == 0;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(centerX, centerY, radius);
    }
}
```

### 2.3 RectangleBounds 클래스

```java
/**
 * 사각형 경계를 나타내는 클래스 (AABB)
 * 축에 정렬된 사각형으로 빠른 충돌 검사를 제공합니다.
 */
public class RectangleBounds extends Bounds {
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    
    /**
     * 사각형 경계 생성자
     * @param x 좌상단 X 좌표
     * @param y 좌상단 Y 좌표
     * @param width 너비
     * @param height 높이
     */
    public RectangleBounds(double x, double y, double width, double height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("너비와 높이는 0보다 커야 합니다");
        }
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    // === Bounds 추상 메서드 구현 ===
    
    @Override
    public boolean intersects(Bounds other) {
        if (other instanceof RectangleBounds) {
            return intersectsRectangle((RectangleBounds) other);
        } else if (other instanceof CircleBounds) {
            return ((CircleBounds) other).intersects(this);  // 원의 구현 사용
        }
        return other.intersects(this);
    }
    
    @Override
    public boolean contains(double px, double py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }
    
    @Override
    public Point2D getCenter() {
        return new Point2D(x + width / 2, y + height / 2);
    }
    
    @Override
    public RectangleBounds getAABB() {
        return this;  // 이미 AABB
    }
    
    @Override
    public double getArea() {
        return width * height;
    }
    
    @Override
    public double getPerimeter() {
        return 2 * (width + height);
    }
    
    @Override
    public Bounds translate(double dx, double dy) {
        return new RectangleBounds(x + dx, y + dy, width, height);
    }
    
    @Override
    public Bounds scale(double scale) {
        if (scale <= 0) {
            throw new IllegalArgumentException("배율은 0보다 커야 합니다: " + scale);
        }
        Point2D center = getCenter();
        double newWidth = width * scale;
        double newHeight = height * scale;
        return new RectangleBounds(
            center.getX() - newWidth / 2,
            center.getY() - newHeight / 2,
            newWidth, newHeight
        );
    }
    
    // === 특화된 충돌 검사 메서드들 ===
    
    /**
     * 다른 사각형과의 교차 검사 (AABB 교차 검사)
     * @param other 다른 사각형
     * @return 교차하면 true
     */
    private boolean intersectsRectangle(RectangleBounds other) {
        return !(x + width < other.x || 
                other.x + other.width < x ||
                y + height < other.y || 
                other.y + other.height < y);
    }
    
    // === 추가 유틸리티 메서드들 ===
    
    /**
     * 다른 사각형과의 겹침 영역을 계산합니다
     * @param other 다른 사각형
     * @return 겹침 영역 (겹치지 않으면 null)
     */
    public RectangleBounds getIntersection(RectangleBounds other) {
        if (!intersectsRectangle(other)) {
            return null;
        }
        
        double left = Math.max(x, other.x);
        double top = Math.max(y, other.y);
        double right = Math.min(x + width, other.x + other.width);
        double bottom = Math.min(y + height, other.y + other.height);
        
        return new RectangleBounds(left, top, right - left, bottom - top);
    }
    
    /**
     * 다른 사각형을 포함하는 최소 사각형을 계산합니다
     * @param other 다른 사각형
     * @return 합집합 사각형
     */
    public RectangleBounds getUnion(RectangleBounds other) {
        double left = Math.min(x, other.x);
        double top = Math.min(y, other.y);
        double right = Math.max(x + width, other.x + other.width);
        double bottom = Math.max(y + height, other.y + other.height);
        
        return new RectangleBounds(left, top, right - left, bottom - top);
    }
    
    /**
     * 사각형의 네 모서리 점을 반환합니다
     * @return 모서리 점들 [좌상, 우상, 우하, 좌하]
     */
    public Point2D[] getCorners() {
        return new Point2D[] {
            new Point2D(x, y),                    // 좌상
            new Point2D(x + width, y),            // 우상
            new Point2D(x + width, y + height),   // 우하
            new Point2D(x, y + height)            // 좌하
        };
    }
    
    /**
     * 주어진 점에서 사각형까지의 최단 거리를 계산합니다
     * @param px X 좌표
     * @param py Y 좌표
     * @return 최단 거리
     */
    public double distanceToPoint(double px, double py) {
        if (contains(px, py)) {
            return 0;  // 점이 사각형 안에 있음
        }
        
        double dx = Math.max(0, Math.max(x - px, px - (x + width)));
        double dy = Math.max(0, Math.max(y - py, py - (y + height)));
        
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * 사각형이 다른 사각형을 완전히 포함하는지 확인합니다
     * @param other 다른 사각형
     * @return 완전히 포함하면 true
     */
    public boolean fullyContains(RectangleBounds other) {
        return x <= other.x && 
               y <= other.y && 
               x + width >= other.x + other.width && 
               y + height >= other.y + other.height;
    }
    
    // === 접근자 메서드들 ===
    
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    
    public double getMinX() { return x; }
    public double getMinY() { return y; }
    public double getMaxX() { return x + width; }
    public double getMaxY() { return y + height; }
    
    @Override
    public String toString() {
        return String.format("RectangleBounds[x=%.2f, y=%.2f, w=%.2f, h=%.2f]", 
                           x, y, width, height);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RectangleBounds)) return false;
        
        RectangleBounds other = (RectangleBounds) obj;
        return Double.compare(other.x, x) == 0 &&
               Double.compare(other.y, y) == 0 &&
               Double.compare(other.width, width) == 0 &&
               Double.compare(other.height, height) == 0;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(x, y, width, height);
    }
}
```

---

## 3. BoundedBall 클래스 - 경계 인식

### 3.1 BoundedBall 완전 구현

```java
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 정확한 경계를 가진 공 클래스
 * MovableBall을 상속받아 정밀한 충돌 검사 기능을 추가합니다.
 */
public class BoundedBall extends MovableBall {
    // === 경계 관련 필드 ===
    private CircleBounds bounds;        // 현재 경계
    private boolean boundsVisible;      // 경계 표시 여부
    private Color boundsColor;          // 경계 색상
    
    // === 충돌 관련 필드 ===
    private double restitution;         // 반발 계수
    private CollisionInfo lastCollision; // 마지막 충돌 정보
    private long collisionCount;        // 총 충돌 횟수
    
    // === 생성자들 ===
    
    public BoundedBall(double x, double y, double radius) {
        this(x, y, radius, Color.RED, 0, 0);
    }
    
    public BoundedBall(double x, double y, double radius, Color color) {
        this(x, y, radius, color, 0, 0);
    }
    
    public BoundedBall(double x, double y, double radius, double dx, double dy) {
        this(x, y, radius, Color.RED, dx, dy);
    }
    
    public BoundedBall(double x, double y, double radius, Color color, double dx, double dy) {
        super(x, y, radius, color, dx, dy);
        
        this.bounds = new CircleBounds(x, y, radius);
        this.boundsVisible = false;
        this.boundsColor = Color.YELLOW;
        this.restitution = 0.8;  // 80% 에너지 보존
        this.lastCollision = null;
        this.collisionCount = 0;
    }
    
    // === 경계 관련 메서드들 ===
    
    /**
     * 현재 경계를 반환합니다
     * @return 원형 경계
     */
    public CircleBounds getBounds() {
        return bounds;
    }
    
    /**
     * 경계를 업데이트합니다 (위치 변경 시 호출)
     */
    private void updateBounds() {
        bounds = new CircleBounds(getX(), getY(), getRadius());
    }
    
    /**
     * 다른 BoundedBall과 충돌하는지 확인합니다
     * @param other 다른 공
     * @return 충돌하면 true
     */
    public boolean isColliding(BoundedBall other) {
        return bounds.intersects(other.bounds);
    }
    
    /**
     * 경계와 충돌하는지 확인합니다
     * @param boundary 경계
     * @return 충돌하면 true
     */
    public boolean isColliding(Bounds boundary) {
        return bounds.intersects(boundary);
    }
    
    /**
     * 특정 점이 공 안에 있는지 확인합니다
     * @param x X 좌표
     * @param y Y 좌표
     * @return 점이 공 안에 있으면 true
     */
    public boolean containsPoint(double x, double y) {
        return bounds.contains(x, y);
    }
    
    // === 이동 메서드 오버라이드 ===
    
    @Override
    public void move(double deltaTime) {
        super.move(deltaTime);
        updateBounds();  // 이동 후 경계 업데이트
    }
    
    @Override
    public void setX(double x) {
        super.setX(x);
        updateBounds();
    }
    
    @Override
    public void setY(double y) {
        super.setY(y);
        updateBounds();
    }
    
    // === 충돌 처리 메서드들 ===
    
    /**
     * 다른 공과의 탄성 충돌을 처리합니다
     * @param other 충돌한 다른 공
     */
    public void handleElasticCollision(BoundedBall other) {
        if (!isColliding(other)) return;
        
        // 충돌 정보 생성
        CollisionInfo collision = calculateCollisionInfo(other);
        recordCollision(collision);
        other.recordCollision(collision);
        
        // 공들을 분리
        separateFromBall(other, collision);
        
        // 속도 변경 (탄성 충돌)
        applyElasticCollisionResponse(other, collision);
    }
    
    /**
     * 충돌 정보를 계산합니다
     * @param other 다른 공
     * @return 충돌 정보
     */
    private CollisionInfo calculateCollisionInfo(BoundedBall other) {
        double dx = other.getX() - getX();
        double dy = other.getY() - getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        // 충돌 법선 벡터 (정규화됨)
        double normalX = (distance > 0) ? dx / distance : 1.0;
        double normalY = (distance > 0) ? dy / distance : 0.0;
        
        // 겹침 깊이
        double overlap = (getRadius() + other.getRadius()) - distance;
        
        // 충돌점 (두 공의 경계 사이)
        double contactX = getX() + normalX * getRadius();
        double contactY = getY() + normalY * getRadius();
        
        return new CollisionInfo(
            normalX, normalY, overlap, 
            contactX, contactY, 
            System.currentTimeMillis()
        );
    }
    
    /**
     * 다른 공으로부터 분리시킵니다
     * @param other 다른 공
     * @param collision 충돌 정보
     */
    private void separateFromBall(BoundedBall other, CollisionInfo collision) {
        if (collision.overlap <= 0) return;
        
        // 질량 비율에 따른 분리
        double totalMass = getMass() + other.getMass();
        double myRatio = other.getMass() / totalMass;
        double otherRatio = getMass() / totalMass;
        
        // 분리 거리 계산
        double separationDistance = collision.overlap / 2;
        double mySeparation = separationDistance * myRatio;
        double otherSeparation = separationDistance * otherRatio;
        
        // 위치 조정
        setX(getX() - collision.normalX * mySeparation);
        setY(getY() - collision.normalY * mySeparation);
        other.setX(other.getX() + collision.normalX * otherSeparation);
        other.setY(other.getY() + collision.normalY * otherSeparation);
    }
    
    /**
     * 탄성 충돌 응답을 적용합니다
     * @param other 다른 공
     * @param collision 충돌 정보
     */
    private void applyElasticCollisionResponse(BoundedBall other, CollisionInfo collision) {
        // 상대 속도
        double relativeVelX = other.getDx() - getDx();
        double relativeVelY = other.getDy() - getDy();
        
        // 법선 방향 상대 속도
        double normalVelocity = relativeVelX * collision.normalX + relativeVelY * collision.normalY;
        
        // 이미 분리되고 있는 경우
        if (normalVelocity > 0) return;
        
        // 반발 계수 적용
        double combinedRestitution = Math.min(restitution, other.restitution);
        double impulse = -(1 + combinedRestitution) * normalVelocity;
        impulse /= (1 / getMass() + 1 / other.getMass());
        
        // 충격량 적용
        double impulseX = impulse * collision.normalX;
        double impulseY = impulse * collision.normalY;
        
        applyImpulse(-impulseX, -impulseY);
        other.applyImpulse(impulseX, impulseY);
    }
    
    /**
     * 벽과의 충돌을 처리합니다
     * @param wallBounds 벽 경계
     */
    public void handleWallCollision(RectangleBounds wallBounds) {
        if (!isColliding(wallBounds)) return;
        
        // 벽과의 충돌 정보 계산
        WallCollisionInfo wallCollision = calculateWallCollisionInfo(wallBounds);
        
        // 벽에서 분리
        separateFromWall(wallBounds, wallCollision);
        
        // 속도 반사
        applyWallCollisionResponse(wallCollision);
        
        // 충돌 기록
        recordWallCollision(wallCollision);
    }
    
    /**
     * 벽과의 충돌 정보를 계산합니다
     */
    private WallCollisionInfo calculateWallCollisionInfo(RectangleBounds wall) {
        double ballX = getX();
        double ballY = getY();
        double radius = getRadius();
        
        // 벽의 경계
        double wallLeft = wall.getX();
        double wallRight = wall.getX() + wall.getWidth();
        double wallTop = wall.getY();
        double wallBottom = wall.getY() + wall.getHeight();
        
        // 가장 가까운 벽면 결정
        double distToLeft = Math.abs(ballX - wallLeft);
        double distToRight = Math.abs(ballX - wallRight);
        double distToTop = Math.abs(ballY - wallTop);
        double distToBottom = Math.abs(ballY - wallBottom);
        
        double minDist = Math.min(Math.min(distToLeft, distToRight), 
                                 Math.min(distToTop, distToBottom));
        
        WallSide side;
        double normalX, normalY, overlap;
        
        if (minDist == distToLeft) {
            side = WallSide.LEFT;
            normalX = -1; normalY = 0;
            overlap = radius - (ballX - wallLeft);
        } else if (minDist == distToRight) {
            side = WallSide.RIGHT;
            normalX = 1; normalY = 0;
            overlap = radius - (wallRight - ballX);
        } else if (minDist == distToTop) {
            side = WallSide.TOP;
            normalX = 0; normalY = -1;
            overlap = radius - (ballY - wallTop);
        } else {
            side = WallSide.BOTTOM;
            normalX = 0; normalY = 1;
            overlap = radius - (wallBottom - ballY);
        }
        
        return new WallCollisionInfo(side, normalX, normalY, overlap);
    }
    
    /**
     * 벽에서 분리시킵니다
     */
    private void separateFromWall(RectangleBounds wall, WallCollisionInfo collision) {
        if (collision.overlap <= 0) return;
        
        setX(getX() + collision.normalX * collision.overlap);
        setY(getY() + collision.normalY * collision.overlap);
    }
    
    /**
     * 벽 충돌 응답을 적용합니다
     */
    private void applyWallCollisionResponse(WallCollisionInfo collision) {
        // 벽에 수직인 속도 성분만 반사
        double normalVelocity = getDx() * collision.normalX + getDy() * collision.normalY;
        
        if (normalVelocity < 0) {  // 벽 쪽으로 이동하는 경우만
            double reflectedVelX = getDx() - 2 * normalVelocity * collision.normalX;
            double reflectedVelY = getDy() - 2 * normalVelocity * collision.normalY;
            
            // 반발 계수 적용
            setDx(reflectedVelX * restitution);
            setDy(reflectedVelY * restitution);
        }
    }
    
    // === 충돌 기록 메서드들 ===
    
    /**
     * 공 충돌을 기록합니다
     */
    private void recordCollision(CollisionInfo collision) {
        lastCollision = collision;
        collisionCount++;
    }
    
    /**
     * 벽 충돌을 기록합니다
     */
    private void recordWallCollision(WallCollisionInfo collision) {
        collisionCount++;
        // 필요시 벽 충돌 전용 기록 시스템 추가
    }
    
    // === 렌더링 메서드들 ===
    
    @Override
    public void paint(GraphicsContext gc, boolean showVelocity) {
        // 기본 공 그리기
        super.paint(gc, showVelocity);
        
        // 경계 표시
        if (boundsVisible) {
            drawBounds(gc);
        }
        
        // 충돌 정보 표시
        if (lastCollision != null && showCollisionInfo()) {
            drawCollisionInfo(gc);
        }
    }
    
    /**
     * 경계를 그립니다
     */
    private void drawBounds(GraphicsContext gc) {
        gc.setStroke(boundsColor);
        gc.setLineWidth(1);
        gc.strokeOval(getX() - getRadius(), getY() - getRadius(), 
                     getRadius() * 2, getRadius() * 2);
    }
    
    /**
     * 충돌 정보를 그립니다
     */
    private void drawCollisionInfo(GraphicsContext gc) {
        if (lastCollision == null) return;
        
        // 충돌점 표시
        gc.setFill(Color.RED);
        double pointSize = 4;
        gc.fillOval(lastCollision.contactX - pointSize/2, 
                   lastCollision.contactY - pointSize/2, 
                   pointSize, pointSize);
        
        // 법선 벡터 표시
        gc.setStroke(Color.RED);
        gc.setLineWidth(2);
        double arrowLength = 20;
        gc.strokeLine(lastCollision.contactX, lastCollision.contactY,
                     lastCollision.contactX + lastCollision.normalX * arrowLength,
                     lastCollision.contactY + lastCollision.normalY * arrowLength);
    }
    
    // === 상태 확인 메서드들 ===
    
    /**
     * 최근에 충돌했는지 확인합니다
     * @param timeWindowMs 시간 윈도우 (밀리초)
     * @return 최근 충돌 여부
     */
    public boolean hasRecentCollision(long timeWindowMs) {
        if (lastCollision == null) return false;
        return (System.currentTimeMillis() - lastCollision.timestamp) < timeWindowMs;
    }
    
    /**
     * 충돌 정보 표시 여부를 확인합니다
     */
    private boolean showCollisionInfo() {
        return hasRecentCollision(1000);  // 1초간 표시
    }
    
    // === Getter/Setter 메서드들 ===
    
    public boolean isBoundsVisible() { return boundsVisible; }
    public void setBoundsVisible(boolean visible) { this.boundsVisible = visible; }
    
    public Color getBoundsColor() { return boundsColor; }
    public void setBoundsColor(Color color) { this.boundsColor = color; }
    
    public double getRestitution() { return restitution; }
    public void setRestitution(double restitution) {
        this.restitution = Math.max(0, Math.min(1, restitution));
    }
    
    public long getCollisionCount() { return collisionCount; }
    public CollisionInfo getLastCollision() { return lastCollision; }
    
    @Override
    public String toString() {
        return String.format("BoundedBall[center=(%.2f, %.2f), radius=%.2f, collisions=%d]",
                           getX(), getY(), getRadius(), collisionCount);
    }
}

// === 보조 클래스들 ===

/**
 * 충돌 정보를 담는 클래스
 */
class CollisionInfo {
    final double normalX, normalY;    // 충돌 법선 벡터
    final double overlap;             // 겹침 깊이
    final double contactX, contactY;  // 충돌점
    final long timestamp;             // 충돌 시간
    
    public CollisionInfo(double normalX, double normalY, double overlap,
                        double contactX, double contactY, long timestamp) {
        this.normalX = normalX;
        this.normalY = normalY;
        this.overlap = overlap;
        this.contactX = contactX;
        this.contactY = contactY;
        this.timestamp = timestamp;
    }
}

/**
 * 벽 충돌 정보를 담는 클래스
 */
class WallCollisionInfo {
    final WallSide side;              // 충돌한 벽면
    final double normalX, normalY;    // 벽의 법선 벡터
    final double overlap;             // 겹침 깊이
    
    public WallCollisionInfo(WallSide side, double normalX, double normalY, double overlap) {
        this.side = side;
        this.normalX = normalX;
        this.normalY = normalY;
        this.overlap = overlap;
    }
}

/**
 * 벽면을 나타내는 열거형
 */
enum WallSide {
    LEFT, RIGHT, TOP, BOTTOM
}
```

---

## 4. 정확한 충돌 검사 알고리즘

### 4.1 충돌 검사 최적화 기법

```java
/**
 * 효율적인 충돌 검사를 위한 유틸리티 클래스
 */
public class CollisionDetector {
    
    /**
     * 2단계 충돌 검사: 빠른 검사 → 정확한 검사
     * @param obj1 첫 번째 객체
     * @param obj2 두 번째 객체
     * @return 충돌 여부
     */
    public static boolean detectCollision(BoundedBall obj1, BoundedBall obj2) {
        // 1단계: AABB 빠른 검사
        if (!quickAABBCheck(obj1, obj2)) {
            return false;
        }
        
        // 2단계: 정확한 원-원 충돌 검사
        return preciseCircleCheck(obj1, obj2);
    }
    
    /**
     * AABB 빠른 충돌 검사
     */
    private static boolean quickAABBCheck(BoundedBall obj1, BoundedBall obj2) {
        RectangleBounds aabb1 = obj1.getBounds().getAABB();
        RectangleBounds aabb2 = obj2.getBounds().getAABB();
        return aabb1.intersects(aabb2);
    }
    
    /**
     * 정확한 원-원 충돌 검사
     */
    private static boolean preciseCircleCheck(BoundedBall obj1, BoundedBall obj2) {
        return obj1.getBounds().intersects(obj2.getBounds());
    }
    
    /**
     * 연속 충돌 검사 (CCD - Continuous Collision Detection)
     * 빠르게 움직이는 객체의 터널링 방지
     * @param obj1 첫 번째 객체
     * @param obj2 두 번째 객체
     * @param deltaTime 시간 간격
     * @return 충돌 시간 (0-1 범위, 충돌 없으면 -1)
     */
    public static double continuousCollisionDetection(BoundedBall obj1, BoundedBall obj2, double deltaTime) {
        // 현재 위치에서의 거리
        double currentDistance = obj1.getBounds().distanceTo(obj2.getBounds());
        double minDistance = obj1.getRadius() + obj2.getRadius();
        
        if (currentDistance <= minDistance) {
            return 0.0;  // 이미 충돌 중
        }
        
        // 상대 속도
        double relativeVx = obj2.getDx() - obj1.getDx();
        double relativeVy = obj2.getDy() - obj1.getDy();
        double relativeSpeed = Math.sqrt(relativeVx * relativeVx + relativeVy * relativeVy);
        
        if (relativeSpeed == 0) {
            return -1;  // 상대 속도가 0이면 충돌 없음
        }
        
        // 가장 가까워지는 시간 계산
        double dx = obj2.getX() - obj1.getX();
        double dy = obj2.getY() - obj1.getY();
        
        double timeToClosest = -(dx * relativeVx + dy * relativeVy) / (relativeSpeed * relativeSpeed);
        
        if (timeToClosest < 0 || timeToClosest > deltaTime) {
            return -1;  // 이 프레임 내에서 가까워지지 않음
        }
        
        // 가장 가까운 시점에서의 거리
        double futureX1 = obj1.getX() + obj1.getDx() * timeToClosest;
        double futureY1 = obj1.getY() + obj1.getDy() * timeToClosest;
        double futureX2 = obj2.getX() + obj2.getDx() * timeToClosest;
        double futureY2 = obj2.getY() + obj2.getDy() * timeToClosest;
        
        double futureDx = futureX2 - futureX1;
        double futureDy = futureY2 - futureY1;
        double futureDistance = Math.sqrt(futureDx * futureDx + futureDy * futureDy);
        
        if (futureDistance <= minDistance) {
            return timeToClosest / deltaTime;  // 0-1 범위로 정규화
        }
        
        return -1;  // 충돌하지 않음
    }
}
```

### 4.2 공간 분할 최적화

```java
/**
 * 공간 분할을 이용한 충돌 검사 최적화
 * 넓은 공간에서 많은 객체의 충돌을 효율적으로 처리
 */
public class SpatialGrid {
    private final int gridWidth;
    private final int gridHeight;
    private final double cellWidth;
    private final double cellHeight;
    private final List<BoundedBall>[][] grid;
    
    @SuppressWarnings("unchecked")
    public SpatialGrid(double worldWidth, double worldHeight, int cellCount) {
        this.gridWidth = cellCount;
        this.gridHeight = cellCount;
        this.cellWidth = worldWidth / gridWidth;
        this.cellHeight = worldHeight / gridHeight;
        
        // 그리드 초기화
        grid = new List[gridHeight][gridWidth];
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                grid[y][x] = new ArrayList<>();
            }
        }
    }
    
    /**
     * 모든 객체를 그리드에 배치합니다
     */
    public void updateGrid(List<BoundedBall> objects) {
        // 그리드 초기화
        clearGrid();
        
        // 각 객체를 해당 셀에 배치
        for (BoundedBall obj : objects) {
            addToGrid(obj);
        }
    }
    
    /**
     * 그리드를 비웁니다
     */
    private void clearGrid() {
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                grid[y][x].clear();
            }
        }
    }
    
    /**
     * 객체를 그리드에 추가합니다
     */
    private void addToGrid(BoundedBall obj) {
        RectangleBounds aabb = obj.getBounds().getAABB();
        
        // 객체가 차지하는 셀 범위 계산
        int minX = Math.max(0, (int)(aabb.getX() / cellWidth));
        int maxX = Math.min(gridWidth - 1, (int)((aabb.getX() + aabb.getWidth()) / cellWidth));
        int minY = Math.max(0, (int)(aabb.getY() / cellHeight));
        int maxY = Math.min(gridHeight - 1, (int)((aabb.getY() + aabb.getHeight()) / cellHeight));
        
        // 해당 셀들에 객체 추가
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                grid[y][x].add(obj);
            }
        }
    }
    
    /**
     * 효율적인 충돌 검사를 수행합니다
     * @return 충돌하는 객체 쌍들
     */
    public List<CollisionPair> detectCollisions() {
        List<CollisionPair> collisions = new ArrayList<>();
        Set<CollisionPair> checkedPairs = new HashSet<>();
        
        // 각 셀에서 충돌 검사
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                List<BoundedBall> cellObjects = grid[y][x];
                
                // 셀 내 객체들 간 충돌 검사
                for (int i = 0; i < cellObjects.size(); i++) {
                    for (int j = i + 1; j < cellObjects.size(); j++) {
                        BoundedBall obj1 = cellObjects.get(i);
                        BoundedBall obj2 = cellObjects.get(j);
                        
                        CollisionPair pair = new CollisionPair(obj1, obj2);
                        if (checkedPairs.contains(pair)) continue;
                        
                        checkedPairs.add(pair);
                        
                        if (CollisionDetector.detectCollision(obj1, obj2)) {
                            collisions.add(pair);
                        }
                    }
                }
            }
        }
        
        return collisions;
    }
    
    /**
     * 특정 위치 주변의 객체들을 반환합니다
     */
    public List<BoundedBall> getObjectsNear(double x, double y, double radius) {
        Set<BoundedBall> nearbyObjects = new HashSet<>();
        
        // 검색 범위의 셀들 계산
        int minX = Math.max(0, (int)((x - radius) / cellWidth));
        int maxX = Math.min(gridWidth - 1, (int)((x + radius) / cellWidth));
        int minY = Math.max(0, (int)((y - radius) / cellHeight));
        int maxY = Math.min(gridHeight - 1, (int)((y + radius) / cellHeight));
        
        // 해당 셀들의 객체들 수집
        for (int gridY = minY; gridY <= maxY; gridY++) {
            for (int gridX = minX; gridX <= maxX; gridX++) {
                nearbyObjects.addAll(grid[gridY][gridX]);
            }
        }
        
        return new ArrayList<>(nearbyObjects);
    }
}

/**
 * 충돌하는 객체 쌍을 나타내는 클래스
 */
class CollisionPair {
    final BoundedBall obj1, obj2;
    
    public CollisionPair(BoundedBall obj1, BoundedBall obj2) {
        // 항상 같은 순서로 저장 (해시 일관성을 위해)
        if (obj1.hashCode() <= obj2.hashCode()) {
            this.obj1 = obj1;
            this.obj2 = obj2;
        } else {
            this.obj1 = obj2;
            this.obj2 = obj1;
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CollisionPair)) return false;
        CollisionPair pair = (CollisionPair) o;
        return obj1.equals(pair.obj1) && obj2.equals(pair.obj2);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(obj1, obj2);
    }
}
```

---

## 5. BoundedWorld 클래스 - 물리적 경계

### 5.1 BoundedWorld 완전 구현

```java
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * 정확한 경계와 충돌 검사를 제공하는 세계 클래스
 * 공간 분할을 통한 성능 최적화를 포함합니다.
 */
public class BoundedWorld {
    // === 기본 속성 ===
    private final double width;
    private final double height;
    private final List<BoundedBall> balls;
    private final List<RectangleBounds> walls;
    
    // === 물리 설정 ===
    private double gravity;
    private boolean gravityEnabled;
    private double airResistance;
    private double globalRestitution;
    
    // === 성능 최적화 ===
    private SpatialGrid spatialGrid;
    private boolean useSpatialGrid;
    private int maxObjectsForBruteForce;
    
    // === 시각화 설정 ===
    private boolean showBounds;
    private boolean showGrid;
    private boolean showCollisionInfo;
    private Color backgroundColor;
    
    // === 통계 정보 ===
    private long totalCollisions;
    private double averageFPS;
    private int lastFrameCollisions;
    
    public BoundedWorld(double width, double height) {
        this.width = width;
        this.height = height;
        this.balls = new ArrayList<>();
        this.walls = new ArrayList<>();
        
        // 물리 설정
        this.gravity = 98.0;
        this.gravityEnabled = true;
        this.airResistance = 0.01;
        this.globalRestitution = 0.8;
        
        // 성능 설정
        this.spatialGrid = new SpatialGrid(width, height, 10);  // 10x10 그리드
        this.useSpatialGrid = true;
        this.maxObjectsForBruteForce = 50;
        
        // 시각화 설정
        this.showBounds = false;
        this.showGrid = false;
        this.showCollisionInfo = false;
        this.backgroundColor = Color.LIGHTGRAY;
        
        // 통계 초기화
        this.totalCollisions = 0;
        this.averageFPS = 0;
        this.lastFrameCollisions = 0;
        
        // 기본 경계 벽 생성
        createBoundaryWalls();
    }
    
    // === 초기화 메서드들 ===
    
    /**
     * 경계 벽들을 생성합니다
     */
    private void createBoundaryWalls() {
        double thickness = 20;
        
        // 상단 벽
        walls.add(new RectangleBounds(-thickness, -thickness, width + 2*thickness, thickness));
        
        // 하단 벽
        walls.add(new RectangleBounds(-thickness, height, width + 2*thickness, thickness));
        
        // 좌측 벽
        walls.add(new RectangleBounds(-thickness, 0, thickness, height));
        
        // 우측 벽
        walls.add(new RectangleBounds(width, 0, thickness, height));
    }
    
    // === 객체 관리 메서드들 ===
    
    public void addBall(BoundedBall ball) {
        if (ball != null && !balls.contains(ball)) {
            balls.add(ball);
        }
    }
    
    public boolean removeBall(BoundedBall ball) {
        return balls.remove(ball);
    }
    
    public void clearBalls() {
        balls.clear();
    }
    
    /**
     * 랜덤한 BoundedBall을 생성합니다
     */
    public BoundedBall createRandomBoundedBall() {
        double radius = 10 + Math.random() * 15;  // 10-25 픽셀
        double x = radius + Math.random() * (width - 2 * radius);
        double y = radius + Math.random() * (height - 2 * radius);
        
        double maxSpeed = 150;
        double dx = (Math.random() - 0.5) * 2 * maxSpeed;
        double dy = (Math.random() - 0.5) * 2 * maxSpeed;
        
        Color color = Color.color(Math.random(), Math.random(), Math.random());
        
        BoundedBall ball = new BoundedBall(x, y, radius, color, dx, dy);
        ball.setRestitution(0.7 + Math.random() * 0.3);  // 0.7-1.0
        addBall(ball);
        return ball;
    }
    
    public void createRandomBoundedBalls(int count) {
        for (int i = 0; i < count; i++) {
            createRandomBoundedBall();
        }
    }
    
    // === 물리 시뮬레이션 ===
    
    /**
     * 세계를 업데이트합니다
     */
    public void update(double deltaTime) {
        if (deltaTime <= 0) return;
        
        // 프레임별 통계 초기화
        lastFrameCollisions = 0;
        
        // 1단계: 물리 법칙 적용
        applyPhysics(deltaTime);
        
        // 2단계: 객체 이동
        moveAllBalls(deltaTime);
        
        // 3단계: 충돌 검사 및 처리
        handleCollisions();
        
        // 4단계: 경계 충돌 처리
        handleWallCollisions();
        
        // 5단계: 비활성 객체 정리
        cleanupInactiveBalls();
        
        // 6단계: 통계 업데이트
        updateStatistics();
    }
    
    /**
     * 물리 법칙을 적용합니다
     */
    private void applyPhysics(double deltaTime) {
        for (BoundedBall ball : balls) {
            if (!ball.isActive()) continue;
            
            // 중력 적용
            if (gravityEnabled) {
                ball.applyGravity(gravity, deltaTime);
            }
            
            // 공기 저항 적용
            if (airResistance > 0) {
                double speed = ball.getSpeed();
                if (speed > 0) {
                    double resistance = airResistance * speed * speed;
                    double angle = ball.getDirection();
                    ball.applyForce(-resistance * Math.cos(angle), 
                                  -resistance * Math.sin(angle), deltaTime);
                }
            }
        }
    }
    
    /**
     * 모든 공을 이동시킵니다
     */
    private void moveAllBalls(double deltaTime) {
        for (BoundedBall ball : balls) {
            ball.move(deltaTime);
        }
    }
    
    /**
     * 공 간 충돌을 처리합니다
     */
    private void handleCollisions() {
        List<CollisionPair> collisions;
        
        // 객체 수에 따라 충돌 검사 방법 선택
        if (useSpatialGrid && balls.size() > maxObjectsForBruteForce) {
            spatialGrid.updateGrid(balls);
            collisions = spatialGrid.detectCollisions();
        } else {
            collisions = bruteForceCollisionDetection();
        }
        
        // 충돌 처리
        for (CollisionPair pair : collisions) {
            pair.obj1.handleElasticCollision(pair.obj2);
            lastFrameCollisions++;
            totalCollisions++;
        }
    }
    
    /**
     * 브루트 포스 충돌 검사
     */
    private List<CollisionPair> bruteForceCollisionDetection() {
        List<CollisionPair> collisions = new ArrayList<>();
        
        for (int i = 0; i < balls.size(); i++) {
            for (int j = i + 1; j < balls.size(); j++) {
                BoundedBall ball1 = balls.get(i);
                BoundedBall ball2 = balls.get(j);
                
                if (CollisionDetector.detectCollision(ball1, ball2)) {
                    collisions.add(new CollisionPair(ball1, ball2));
                }
            }
        }
        
        return collisions;
    }
    
    /**
     * 벽과의 충돌을 처리합니다
     */
    private void handleWallCollisions() {
        for (BoundedBall ball : balls) {
            for (RectangleBounds wall : walls) {
                if (ball.isColliding(wall)) {
                    ball.handleWallCollision(wall);
                    lastFrameCollisions++;
                    totalCollisions++;
                }
            }
        }
    }
    
    /**
     * 비활성 공들을 정리합니다
     */
    private void cleanupInactiveBalls() {
        balls.removeIf(ball -> !ball.isActive() || 
                              ball.getX() < -100 || ball.getX() > width + 100 ||
                              ball.getY() < -100 || ball.getY() > height + 100);
    }
    
    /**
     * 통계 정보를 업데이트합니다
     */
    private void updateStatistics() {
        // FPS 계산은 외부에서 제공받음
        // 여기서는 충돌 통계만 관리
    }
    
    // === 렌더링 ===
    
    /**
     * 세계를 렌더링합니다
     */
    public void render(GraphicsContext gc) {
        // 배경 그리기
        gc.setFill(backgroundColor);
        gc.fillRect(0, 0, width, height);
        
        // 그리드 표시
        if (showGrid) {
            drawGrid(gc);
        }
        
        // 벽 그리기
        drawWalls(gc);
        
        // 공들 그리기
        for (BoundedBall ball : balls) {
            ball.setBoundsVisible(showBounds);
            ball.paint(gc, showCollisionInfo);
        }
        
        // 정보 표시
        drawInfo(gc);
    }
    
    /**
     * 그리드를 그립니다
     */
    private void drawGrid(GraphicsContext gc) {
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);
        
        double cellWidth = width / 10;
        double cellHeight = height / 10;
        
        // 세로 선들
        for (int x = 1; x < 10; x++) {
            double lineX = x * cellWidth;
            gc.strokeLine(lineX, 0, lineX, height);
        }
        
        // 가로 선들
        for (int y = 1; y < 10; y++) {
            double lineY = y * cellHeight;
            gc.strokeLine(0, lineY, width, lineY);
        }
    }
    
    /**
     * 벽들을 그립니다
     */
    private void drawWalls(GraphicsContext gc) {
        gc.setFill(Color.DARKGRAY);
        
        for (RectangleBounds wall : walls) {
            // 화면에 보이는 부분만 그리기
            double x = Math.max(0, wall.getX());
            double y = Math.max(0, wall.getY());
            double w = Math.min(width - x, wall.getWidth());
            double h = Math.min(height - y, wall.getHeight());
            
            if (w > 0 && h > 0) {
                gc.fillRect(x, y, w, h);
            }
        }
    }
    
    /**
     * 정보를 화면에 표시합니다
     */
    private void drawInfo(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.setFont(javafx.scene.text.Font.font(12));
        
        int line = 1;
        gc.fillText(String.format("공 개수: %d", balls.size()), 10, line * 15);
        line++;
        
        gc.fillText(String.format("총 충돌: %d", totalCollisions), 10, line * 15);
        line++;
        
        gc.fillText(String.format("이번 프레임 충돌: %d", lastFrameCollisions), 10, line * 15);
        line++;
        
        if (averageFPS > 0) {
            gc.fillText(String.format("FPS: %.1f", averageFPS), 10, line * 15);
            line++;
        }
        
        gc.fillText(String.format("충돌 검사: %s", 
                   (useSpatialGrid && balls.size() > maxObjectsForBruteForce) ? "공간분할" : "브루트포스"), 
                   10, line * 15);
        line++;
        
        if (gravityEnabled) {
            gc.fillText(String.format("중력: %.1f", gravity), 10, line * 15);
            line++;
        }
    }
    
    // === 유틸리티 메서드들 ===
    
    /**
     * 특정 위치의 공을 찾습니다
     */
    public BoundedBall findBallAt(double x, double y) {
        for (int i = balls.size() - 1; i >= 0; i--) {
            BoundedBall ball = balls.get(i);
            if (ball.containsPoint(x, y)) {
                return ball;
            }
        }
        return null;
    }
    
    /**
     * 특정 위치 주변의 공들을 찾습니다
     */
    public List<BoundedBall> findBallsNear(double x, double y, double radius) {
        if (useSpatialGrid) {
            return spatialGrid.getObjectsNear(x, y, radius);
        } else {
            List<BoundedBall> nearbyBalls = new ArrayList<>();
            for (BoundedBall ball : balls) {
                double distance = Math.sqrt(Math.pow(ball.getX() - x, 2) + Math.pow(ball.getY() - y, 2));
                if (distance <= radius) {
                    nearbyBalls.add(ball);
                }
            }
            return nearbyBalls;
        }
    }
    
    /**
     * 폭발 효과를 생성합니다
     */
    public void explodeAt(double x, double y, double force, double radius) {
        List<BoundedBall> affectedBalls = findBallsNear(x, y, radius);
        
        for (BoundedBall ball : affectedBalls) {
            double dx = ball.getX() - x;
            double dy = ball.getY() - y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            if (distance > 0) {
                double explosionForce = force / (distance * distance);
                double impulseX = (dx / distance) * explosionForce;
                double impulseY = (dy / distance) * explosionForce;
                
                ball.applyImpulse(impulseX, impulseY);
            }
        }
    }
    
    // === Getter/Setter 메서드들 ===
    
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public int getBallCount() { return balls.size(); }
    public List<BoundedBall> getBalls() { return new ArrayList<>(balls); }
    
    public boolean isGravityEnabled() { return gravityEnabled; }
    public void setGravityEnabled(boolean enabled) { this.gravityEnabled = enabled; }
    
    public double getGravity() { return gravity; }
    public void setGravity(double gravity) { this.gravity = gravity; }
    
    public boolean isShowBounds() { return showBounds; }
    public void setShowBounds(boolean show) { this.showBounds = show; }
    
    public boolean isShowGrid() { return showGrid; }
    public void setShowGrid(boolean show) { this.showGrid = show; }
    
    public boolean isShowCollisionInfo() { return showCollisionInfo; }
    public void setShowCollisionInfo(boolean show) { this.showCollisionInfo = show; }
    
    public boolean isUseSpatialGrid() { return useSpatialGrid; }
    public void setUseSpatialGrid(boolean use) { this.useSpatialGrid = use; }
    
    public long getTotalCollisions() { return totalCollisions; }
    public int getLastFrameCollisions() { return lastFrameCollisions; }
    
    public void setAverageFPS(double fps) { this.averageFPS = fps; }
}
```

---

## 6. 테스트 코드와 검증

### 6.1 Bounds 시스템 테스트

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BoundsTest {
    
    @Test
    @DisplayName("CircleBounds 생성 및 기본 기능 테스트")
    void testCircleBounds() {
        CircleBounds circle = new CircleBounds(100, 100, 25);
        
        assertEquals(100, circle.getCenterX(), 0.001);
        assertEquals(100, circle.getCenterY(), 0.001);
        assertEquals(25, circle.getRadius(), 0.001);
        
        // 면적과 둘레
        assertEquals(Math.PI * 25 * 25, circle.getArea(), 0.001);
        assertEquals(2 * Math.PI * 25, circle.getPerimeter(), 0.001);
        
        // 점 포함 테스트
        assertTrue(circle.contains(100, 100));  // 중심
        assertTrue(circle.contains(110, 110));  // 안쪽
        assertFalse(circle.contains(130, 130)); // 바깥쪽
    }
    
    @Test
    @DisplayName("RectangleBounds 생성 및 기본 기능 테스트")
    void testRectangleBounds() {
        RectangleBounds rect = new RectangleBounds(50, 50, 100, 80);
        
        assertEquals(50, rect.getX(), 0.001);
        assertEquals(50, rect.getY(), 0.001);
        assertEquals(100, rect.getWidth(), 0.001);
        assertEquals(80, rect.getHeight(), 0.001);
        
        // 면적과 둘레
        assertEquals(8000, rect.getArea(), 0.001);
        assertEquals(360, rect.getPerimeter(), 0.001);
        
        // 중심점
        Point2D center = rect.getCenter();
        assertEquals(100, center.getX(), 0.001);
        assertEquals(90, center.getY(), 0.001);
        
        // 점 포함 테스트
        assertTrue(rect.contains(75, 75));   // 안쪽
        assertTrue(rect.contains(50, 50));   // 모서리
        assertFalse(rect.contains(40, 40));  // 바깥쪽
    }
    
    @Test
    @DisplayName("원-원 충돌 검사 테스트")
    void testCircleCircleCollision() {
        CircleBounds circle1 = new CircleBounds(100, 100, 20);
        CircleBounds circle2 = new CircleBounds(130, 100, 15);  // 겹침
        CircleBounds circle3 = new CircleBounds(160, 100, 10);  // 분리됨
        
        assertTrue(circle1.intersects(circle2), "겹치는 원들이 충돌로 감지되어야 합니다");
        assertFalse(circle1.intersects(circle3), "분리된 원들은 충돌로 감지되지 않아야 합니다");
        
        // 경계선상 접촉
        CircleBounds circle4 = new CircleBounds(135, 100, 15);  // 정확히 접촉
        assertTrue(circle1.intersects(circle4), "접촉하는 원들은 충돌로 감지되어야 합니다");
    }
    
    @Test
    @DisplayName("원-사각형 충돌 검사 테스트")
    void testCircleRectangleCollision() {
        CircleBounds circle = new CircleBounds(100, 100, 20);
        RectangleBounds rect1 = new RectangleBounds(110, 110, 50, 40);  // 겹침
        RectangleBounds rect2 = new RectangleBounds(150, 150, 30, 30);  // 분리됨
        
        assertTrue(circle.intersects(rect1), "겹치는 원과 사각형이 충돌로 감지되어야 합니다");
        assertFalse(circle.intersects(rect2), "분리된 원과 사각형은 충돌로 감지되지 않아야 합니다");
        
        // 모서리 접촉
        RectangleBounds rect3 = new RectangleBounds(120, 80, 20, 20);
        assertTrue(circle.intersects(rect3), "모서리 접촉하는 경우 충돌로 감지되어야 합니다");
    }
    
    @Test
    @DisplayName("사각형-사각형 충돌 검사 테스트")
    void testRectangleRectangleCollision() {
        RectangleBounds rect1 = new RectangleBounds(50, 50, 100, 80);
        RectangleBounds rect2 = new RectangleBounds(100, 80, 60, 60);  // 겹침
        RectangleBounds rect3 = new RectangleBounds(200, 200, 50, 50); // 분리됨
        
        assertTrue(rect1.intersects(rect2), "겹치는 사각형들이 충돌로 감지되어야 합니다");
        assertFalse(rect1.intersects(rect3), "분리된 사각형들은 충돌로 감지되지 않아야 합니다");
        
        // 경계선상 접촉
        RectangleBounds rect4 = new RectangleBounds(150, 50, 30, 30);  // 우측 경계 접촉
        assertTrue(rect1.intersects(rect4), "경계선 접촉하는 사각형들은 충돌로 감지되어야 합니다");
    }
    
    @Test
    @DisplayName("Bounds 변환 테스트")
    void testBoundsTransformation() {
        CircleBounds circle = new CircleBounds(100, 100, 20);
        
        // 이동
        CircleBounds moved = (CircleBounds) circle.translate(50, 30);
        assertEquals(150, moved.getCenterX(), 0.001);
        assertEquals(130, moved.getCenterY(), 0.001);
        assertEquals(20, moved.getRadius(), 0.001);
        
        // 크기 변경
        CircleBounds scaled = (CircleBounds) circle.scale(1.5);
        assertEquals(100, scaled.getCenterX(), 0.001);
        assertEquals(100, scaled.getCenterY(), 0.001);
        assertEquals(30, scaled.getRadius(), 0.001);
    }
}
```

### 6.2 BoundedBall 테스트

```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BoundedBallTest {
    private BoundedBall ball1;
    private BoundedBall ball2;
    
    @BeforeEach
    void setUp() {
        ball1 = new BoundedBall(100, 100, 20, Color.RED, 50, 30);
        ball2 = new BoundedBall(130, 100, 15, Color.BLUE, -30, 20);
    }
    
    @Test
    @DisplayName("BoundedBall 생성 및 경계 업데이트 테스트")
    void testBoundedBallCreation() {
        assertEquals(100, ball1.getX(), 0.001);
        assertEquals(100, ball1.getY(), 0.001);
        assertEquals(20, ball1.getRadius(), 0.001);
        
        CircleBounds bounds = ball1.getBounds();
        assertEquals(100, bounds.getCenterX(), 0.001);
        assertEquals(100, bounds.getCenterY(), 0.001);
        assertEquals(20, bounds.getRadius(), 0.001);
    }
    
    @Test
    @DisplayName("이동 시 경계 업데이트 테스트")
    void testBoundsUpdateOnMove() {
        ball1.move(1.0);  // 1초 이동
        
        CircleBounds bounds = ball1.getBounds();
        assertEquals(150, bounds.getCenterX(), 0.001);  // 100 + 50*1
        assertEquals(130, bounds.getCenterY(), 0.001);  // 100 + 30*1
    }
    
    @Test
    @DisplayName("충돌 검사 테스트")
    void testCollisionDetection() {
        // 초기 상태에서 충돌 확인 (반지름 합: 35, 거리: 30)
        assertTrue(ball1.isColliding(ball2), "겹치는 공들이 충돌로 감지되어야 합니다");
        
        // 공을 멀리 이동시켜 분리
        ball2.setX(200);
        assertFalse(ball1.isColliding(ball2), "분리된 공들은 충돌로 감지되지 않아야 합니다");
    }
    
    @Test
    @DisplayName("탄성 충돌 처리 테스트")
    void testElasticCollision() {
        // 충돌 전 속도 저장
        double ball1DxBefore = ball1.getDx();
        double ball1DyBefore = ball1.getDy();
        double ball2DxBefore = ball2.getDx();
        double ball2DyBefore = ball2.getDy();
        
        // 탄성 충돌 처리
        ball1.handleElasticCollision(ball2);
        
        // 충돌 후 속도 변화 확인
        assertNotEquals(ball1DxBefore, ball1.getDx(), "충돌 후 ball1의 X 속도가 변경되어야 합니다");
        assertNotEquals(ball2DxBefore, ball2.getDx(), "충돌 후 ball2의 X 속도가 변경되어야 합니다");
        
        // 운동량 보존 검증 (근사값)
        double momentumXBefore = ball1.getMass() * ball1DxBefore + ball2.getMass() * ball2DxBefore;
        double momentumXAfter = ball1.getMass() * ball1.getDx() + ball2.getMass() * ball2.getDx();
        assertEquals(momentumXBefore, momentumXAfter, 0.1);
    }
    
    @Test
    @DisplayName("벽 충돌 처리 테스트")
    void testWallCollision() {
        // 좌측 벽과 충돌하도록 설정
        ball1.setX(15);  // 반지름 20, 벽 위치 0
        ball1.setDx(-50); // 왼쪽으로 이동
        
        RectangleBounds leftWall = new RectangleBounds(-10, 0, 10, 200);
        
        ball1.handleWallCollision(leftWall);
        
        // 벽에서 분리되었는지 확인
        assertTrue(ball1.getX() >= 20, "공이 벽에서 분리되어야 합니다");
        
        // 속도가 반사되었는지 확인
        assertTrue(ball1.getDx() > 0, "X 속도가 양수로 반사되어야 합니다");
    }
    
    @Test
    @DisplayName("충돌 카운트 테스트")
    void testCollisionCount() {
        long initialCount = ball1.getCollisionCount();
        
        ball1.handleElasticCollision(ball2);
        
        assertEquals(initialCount + 1, ball1.getCollisionCount(), "충돌 카운트가 증가해야 합니다");
        assertEquals(initialCount + 1, ball2.getCollisionCount(), "양쪽 공 모두 카운트가 증가해야 합니다");
    }
    
    @Test
    @DisplayName("반발 계수 테스트")
    void testRestitution() {
        ball1.setRestitution(0.5);  // 50% 에너지 보존
        ball2.setRestitution(0.8);  // 80% 에너지 보존
        
        double speedBefore = ball1.getSpeed();
        
        // 벽과 충돌
        RectangleBounds wall = new RectangleBounds(ball1.getX() + 25, 0, 10, 200);
        ball1.handleWallCollision(wall);
        
        double speedAfter = ball1.getSpeed();
        
        // 에너지 손실 확인
        assertTrue(speedAfter < speedBefore, "충돌 후 속력이 감소해야 합니다");
    }
    
    @Test
    @DisplayName("점 포함 테스트")
    void testPointContainment() {
        assertTrue(ball1.containsPoint(100, 100), "중심점이 포함되어야 합니다");
        assertTrue(ball1.containsPoint(110, 110), "내부 점이 포함되어야 합니다");
        assertFalse(ball1.containsPoint(130, 130), "외부 점은 포함되지 않아야 합니다");
    }
}
```

### 6.3 BoundedWorld 통합 테스트

```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BoundedWorldTest {
    private BoundedWorld world;
    
    @BeforeEach
    void setUp() {
        world = new BoundedWorld(800, 600);
    }
    
    @Test
    @DisplayName("세계 생성 및 기본 설정 테스트")
    void testWorldCreation() {
        assertEquals(800, world.getWidth(), 0.001);
        assertEquals(600, world.getHeight(), 0.001);
        assertEquals(0, world.getBallCount());
        assertTrue(world.isGravityEnabled());
    }
    
    @Test
    @DisplayName("공 추가 및 관리 테스트")
    void testBallManagement() {
        BoundedBall ball = new BoundedBall(100, 100, 20);
        world.addBall(ball);
        
        assertEquals(1, world.getBallCount());
        assertTrue(world.getBalls().contains(ball));
        
        // 제거 테스트
        assertTrue(world.removeBall(ball));
        assertEquals(0, world.getBallCount());
        assertFalse(world.getBalls().contains(ball));
    }
    
    @Test
    @DisplayName("랜덤 공 생성 테스트")
    void testRandomBallCreation() {
        world.createRandomBoundedBalls(5);
        assertEquals(5, world.getBallCount());
        
        // 모든 공이 경계 안에 있는지 확인
        for (BoundedBall ball : world.getBalls()) {
            assertTrue(ball.getX() >= ball.getRadius());
            assertTrue(ball.getX() <= world.getWidth() - ball.getRadius());
            assertTrue(ball.getY() >= ball.getRadius());
            assertTrue(ball.getY() <= world.getHeight() - ball.getRadius());
        }
    }
    
    @Test
    @DisplayName("물리 시뮬레이션 테스트")
    void testPhysicsSimulation() {
        BoundedBall ball = new BoundedBall(100, 100, 20, Color.RED, 50, 0);
        world.addBall(ball);
        
        double initialX = ball.getX();
        double initialY = ball.getY();
        
        world.update(1.0);  // 1초 업데이트
        
        // 이동 확인
        assertNotEquals(initialX, ball.getX());
        
        // 중력이 적용되었는지 확인
        if (world.isGravityEnabled()) {
            assertTrue(ball.getDy() > 0, "중력에 의해 Y 속도가 증가해야 합니다");
        }
    }
    
    @Test
    @DisplayName("충돌 시스템 테스트")
    void testCollisionSystem() {
        BoundedBall ball1 = new BoundedBall(100, 100, 20, Color.RED, 50, 0);
        BoundedBall ball2 = new BoundedBall(130, 100, 15, Color.BLUE, -50, 0);
        
        world.addBall(ball1);
        world.addBall(ball2);
        
        long initialCollisions = world.getTotalCollisions();
        
        world.update(0.1);  // 짧은 시간 업데이트
        
        // 충돌이 발생했는지 확인
        if (ball1.isColliding(ball2)) {
            assertTrue(world.getTotalCollisions() > initialCollisions, "충돌이 감지되고 카운트되어야 합니다");
        }
    }
    
    @Test
    @DisplayName("경계 충돌 테스트")
    void testBoundaryCollision() {
        // 좌측 경계 근처에 공 생성
        BoundedBall ball = new BoundedBall(15, 300, 20, Color.RED, -100, 0);
        world.addBall(ball);
        
        world.update(1.0);  // 1초 후 경계와 충돌
        
        // 경계에서 반사되어 오른쪽으로 이동하는지 확인
        assertTrue(ball.getDx() > 0, "경계 충돌 후 속도가 반사되어야 합니다");
        assertTrue(ball.getX() >= ball.getRadius(), "공이 경계 안에 있어야 합니다");
    }
    
    @Test
    @DisplayName("특정 위치 공 찾기 테스트")
    void testFindBallAt() {
        BoundedBall ball = new BoundedBall(150, 200, 25);
        world.addBall(ball);
        
        // 공 중심에서 찾기
        assertEquals(ball, world.findBallAt(150, 200));
        
        // 공 내부에서 찾기
        assertEquals(ball, world.findBallAt(160, 210));
        
        // 공 외부에서 찾기
        assertNull(world.findBallAt(200, 250));
    }
    
    @Test
    @DisplayName("폭발 효과 테스트")
    void testExplosion() {
        BoundedBall ball = new BoundedBall(100, 100, 20, Color.RED, 0, 0);
        world.addBall(ball);
        
        world.explodeAt(50, 100, 1000, 100);  // 왼쪽에서 폭발
        
        // 공이 오른쪽으로 밀려났는지 확인
        assertTrue(ball.getDx() > 0, "폭발에 의해 공이 밀려나야 합니다");
    }
    
    @Test
    @DisplayName("성능 모드 전환 테스트")
    void testPerformanceMode() {
        // 많은 공 생성
        world.createRandomBoundedBalls(60);
        
        // 공간 분할 모드
        world.setUseSpatialGrid(true);
        long startTime = System.nanoTime();
        world.update(0.016);
        long spatialTime = System.nanoTime() - startTime;
        
        // 브루트 포스 모드
        world.setUseSpatialGrid(false);
        startTime = System.nanoTime();
        world.update(0.016);
        long bruteForceTime = System.nanoTime() - startTime;
        
        // 공간 분할이 더 빨라야 함 (객체가 많을 때)
        System.out.println("Spatial Grid: " + spatialTime + "ns");
        System.out.println("Brute Force: " + bruteForceTime + "ns");
    }
}
```

---

## 7. 일반적인 실수와 해결법

### 7.1 충돌 검사 정밀도 실수

#### ❌ 잘못된 충돌 검사
```java
// 부동소수점 직접 비교
if (distance == radius1 + radius2) {
    // 거의 실행되지 않음!
}
```

#### ✅ 올바른 충돌 검사
```java
// 임계값을 이용한 비교
private static final double EPSILON = 0.001;

if (distance <= radius1 + radius2 + EPSILON) {
    // 충돌로 판단
}
```

### 7.2 경계 업데이트 누락

#### ❌ 경계 업데이트 누락
```java
public void setX(double x) {
    this.x = x;
    // bounds 업데이트 누락!
}
```

#### ✅ 올바른 경계 업데이트
```java
public void setX(double x) {
    this.x = x;
    updateBounds();  // 경계 업데이트 필수!
}
```

### 7.3 충돌 응답 중복 적용

#### ❌ 중복 충돌 처리
```java
// 양쪽에서 충돌 처리
ball1.handleCollision(ball2);
ball2.handleCollision(ball1);  // 중복 처리!
```

#### ✅ 올바른 충돌 처리
```java
// 한 번만 처리하고 양쪽에 영향
if (ball1.getId() < ball2.getId()) {  // ID로 순서 보장
    handleMutualCollision(ball1, ball2);
}
```

### 7.4 분리 처리 실수

#### ❌ 분리 없는 충돌 처리
```java
public void handleCollision(Ball other) {
    // 속도만 변경, 분리는 하지 않음
    this.dx = -this.dx;
    // 결과: 공들이 계속 겹친 상태로 진동
}
```

#### ✅ 올바른 분리 처리
```java
public void handleCollision(Ball other) {
    // 1단계: 분리
    separateFrom(other);
    
    // 2단계: 속도 변경
    applyCollisionResponse(other);
}
```

### 7.5 성능 최적화 실수

#### ❌ 비효율적인 충돌 검사
```java
// 모든 객체를 매번 비교
for (Ball ball1 : balls) {
    for (Ball ball2 : balls) {  // O(n²) + 자기 자신과도 비교
        if (ball1.collidesWith(ball2)) {
            // ...
        }
    }
}
```

#### ✅ 효율적인 충돌 검사
```java
// 중복 제거 + 자기 자신 제외
for (int i = 0; i < balls.size(); i++) {
    for (int j = i + 1; j < balls.size(); j++) {  // i+1부터 시작
        Ball ball1 = balls.get(i);
        Ball ball2 = balls.get(j);
        
        if (ball1.collidesWith(ball2)) {
            // ...
        }
    }
}
```

### 7.6 실수 방지 체크리스트

- [ ] **경계 업데이트**: 위치 변경 시 항상 경계도 업데이트하는가?
- [ ] **충돌 정밀도**: 부동소수점 비교에 임계값을 사용하는가?
- [ ] **중복 처리**: 충돌 처리가 중복으로 적용되지 않는가?
- [ ] **분리 처리**: 충돌 후 객체들이 올바르게 분리되는가?
- [ ] **성능 최적화**: 불필요한 중복 검사를 피하고 있는가?
- [ ] **경계 확인**: 모든 경계 타입 조합이 올바르게 작동하는가?
- [ ] **메모리 관리**: 충돌 정보 객체들이 적절히 정리되는가?

---

## 학습 포인트 정리

### 4장에서 배운 핵심 개념들

1. **정확한 충돌 검사 시스템**
   - 다양한 형태 간 충돌 알고리즘
   - 2단계 검사 (빠른 → 정확한)
   - 연속 충돌 검사 (CCD)

2. **Bounds 추상화**
   - 통일된 인터페이스로 다양한 형태 처리
   - 확장 가능한 구조 설계
   - 변환과 유틸리티 메서드

3. **물리적 정확성**
   - 분리 처리의 중요성
   - 탄성 충돌과 에너지 보존
   - 반발 계수와 현실감

4. **성능 최적화**
   - 공간 분할을 통한 최적화
   - 브루트 포스 vs 공간 분할 선택
   - 메모리와 연산 시간의 균형

5. **확장성과 유지보수성**
   - 새로운 형태 추가 용이성
   - 충돌 정보 기록과 디버깅
   - 시각화와 분석 도구

이제 5장에서는 추상 클래스를 도입하여 코드 중복을 줄이고 더 나은 구조를 만들어보겠습니다!