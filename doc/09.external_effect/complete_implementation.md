# 9장: 외부 효과 완전한 구현 가이드

## 목차
1. [물리 엔진 기초](#1-물리-엔진-기초)
2. [중력 시스템](#2-중력-시스템)
3. [바람 효과](#3-바람-효과)
4. [마찰력 구현](#4-마찰력-구현)
5. [반발 계수와 충돌](#5-반발-계수와-충돌)
6. [통합 물리 시뮬레이션](#6-통합-물리-시뮬레이션)
7. [메인 데모 애플리케이션](#7-메인-데모-애플리케이션)
8. [실습 프로젝트들](#8-실습-프로젝트들)

---

## 1. 물리 엔진 기초

### 1.1 Vector2D 클래스

2차원 벡터를 표현하는 기본 클래스입니다.

```java
/**
 * 2차원 벡터를 표현하는 클래스
 * 위치, 속도, 가속도, 힘 등을 나타내는데 사용됩니다
 */
public class Vector2D {
    private double x;
    private double y;
    
    /**
     * 벡터 생성자
     * @param x X 성분
     * @param y Y 성분
     */
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * 영벡터 생성자
     */
    public Vector2D() {
        this(0, 0);
    }
    
    /**
     * 복사 생성자
     * @param other 복사할 벡터
     */
    public Vector2D(Vector2D other) {
        this(other.x, other.y);
    }
    
    // 벡터 연산 메서드들
    
    /**
     * 벡터 덧셈
     * @param other 더할 벡터
     * @return 결과 벡터
     */
    public Vector2D add(Vector2D other) {
        return new Vector2D(x + other.x, y + other.y);
    }
    
    /**
     * 벡터 뺄셈
     * @param other 뺄 벡터
     * @return 결과 벡터
     */
    public Vector2D subtract(Vector2D other) {
        return new Vector2D(x - other.x, y - other.y);
    }
    
    /**
     * 스칼라 곱셈
     * @param scalar 곱할 스칼라 값
     * @return 결과 벡터
     */
    public Vector2D multiply(double scalar) {
        return new Vector2D(x * scalar, y * scalar);
    }
    
    /**
     * 스칼라 나눗셈
     * @param scalar 나눌 스칼라 값
     * @return 결과 벡터
     */
    public Vector2D divide(double scalar) {
        if (scalar == 0) {
            throw new IllegalArgumentException("0으로 나눌 수 없습니다");
        }
        return new Vector2D(x / scalar, y / scalar);
    }
    
    /**
     * 벡터의 크기(magnitude)
     * @return 벡터의 크기
     */
    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }
    
    /**
     * 벡터의 크기 제곱
     * @return 벡터 크기의 제곱
     */
    public double magnitudeSquared() {
        return x * x + y * y;
    }
    
    /**
     * 정규화된 벡터 (단위 벡터)
     * @return 정규화된 벡터
     */
    public Vector2D normalize() {
        double mag = magnitude();
        if (mag == 0) {
            return new Vector2D(0, 0);
        }
        return divide(mag);
    }
    
    /**
     * 내적 (dot product)
     * @param other 다른 벡터
     * @return 내적 값
     */
    public double dot(Vector2D other) {
        return x * other.x + y * other.y;
    }
    
    /**
     * 두 벡터 사이의 거리
     * @param other 다른 벡터
     * @return 거리
     */
    public double distance(Vector2D other) {
        return subtract(other).magnitude();
    }
    
    /**
     * 벡터를 특정 크기로 제한
     * @param maxMagnitude 최대 크기
     * @return 제한된 벡터
     */
    public Vector2D limit(double maxMagnitude) {
        if (magnitudeSquared() > maxMagnitude * maxMagnitude) {
            return normalize().multiply(maxMagnitude);
        }
        return new Vector2D(this);
    }
    
    // Getter/Setter 메서드들
    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    
    /**
     * 벡터를 설정합니다
     * @param x X 성분
     * @param y Y 성분
     */
    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * 다른 벡터로 설정합니다
     * @param other 설정할 벡터
     */
    public void set(Vector2D other) {
        this.x = other.x;
        this.y = other.y;
    }
    
    @Override
    public String toString() {
        return String.format("Vector2D(%.2f, %.2f)", x, y);
    }
}
```

### 1.2 PhysicsObject 인터페이스

물리 법칙이 적용되는 객체를 위한 인터페이스입니다.

```java
/**
 * 물리 법칙이 적용되는 객체를 나타내는 인터페이스
 * 위치, 속도, 질량을 가지고 힘의 영향을 받습니다
 */
public interface PhysicsObject {
    /**
     * 현재 위치를 반환합니다
     * @return 위치 벡터
     */
    Vector2D getPosition();
    
    /**
     * 위치를 설정합니다
     * @param position 새 위치
     */
    void setPosition(Vector2D position);
    
    /**
     * 현재 속도를 반환합니다
     * @return 속도 벡터
     */
    Vector2D getVelocity();
    
    /**
     * 속도를 설정합니다
     * @param velocity 새 속도
     */
    void setVelocity(Vector2D velocity);
    
    /**
     * 질량을 반환합니다
     * @return 질량 (kg)
     */
    double getMass();
    
    /**
     * 힘을 적용합니다
     * @param force 적용할 힘 벡터
     */
    void applyForce(Vector2D force);
    
    /**
     * 물리 상태를 업데이트합니다
     * @param deltaTime 경과 시간 (초)
     */
    void update(double deltaTime);
    
    /**
     * 반발 계수를 반환합니다
     * @return 반발 계수 (0~1)
     */
    double getRestitution();
}
```

### 1.3 Force 인터페이스

물리적 힘을 표현하는 인터페이스입니다.

```java
/**
 * 물리적 힘을 나타내는 인터페이스
 * 중력, 바람, 마찰 등 다양한 힘을 구현할 수 있습니다
 */
public interface Force {
    /**
     * 객체에 힘을 적용합니다
     * @param object 힘을 적용할 객체
     * @param deltaTime 경과 시간
     */
    void apply(PhysicsObject object, double deltaTime);
    
    /**
     * 힘의 이름을 반환합니다
     * @return 힘의 이름
     */
    String getName();
    
    /**
     * 힘이 활성화되어 있는지 반환합니다
     * @return 활성화 여부
     */
    boolean isEnabled();
    
    /**
     * 힘의 활성화 상태를 설정합니다
     * @param enabled 활성화 여부
     */
    void setEnabled(boolean enabled);
}
```

### 1.4 PhysicsEngine 클래스

물리 시스템을 관리하는 엔진입니다.

```java
import java.util.*;

/**
 * 물리 엔진
 * 모든 물리 객체와 힘을 관리하고 시뮬레이션을 수행합니다
 */
public class PhysicsEngine {
    private List<Force> globalForces;
    private List<PhysicsObject> objects;
    private double airDensity;
    
    // 물리 상수
    public static final double AIR_DENSITY_SEA_LEVEL = 1.225; // kg/m³
    
    /**
     * 물리 엔진 생성자
     */
    public PhysicsEngine() {
        this.globalForces = new ArrayList<>();
        this.objects = new ArrayList<>();
        this.airDensity = AIR_DENSITY_SEA_LEVEL;
    }
    
    /**
     * 전역 힘을 추가합니다
     * @param force 추가할 힘
     */
    public void addGlobalForce(Force force) {
        globalForces.add(force);
    }
    
    /**
     * 전역 힘을 제거합니다
     * @param force 제거할 힘
     */
    public void removeGlobalForce(Force force) {
        globalForces.remove(force);
    }
    
    /**
     * 물리 객체를 추가합니다
     * @param object 추가할 객체
     */
    public void addObject(PhysicsObject object) {
        objects.add(object);
    }
    
    /**
     * 물리 객체를 제거합니다
     * @param object 제거할 객체
     */
    public void removeObject(PhysicsObject object) {
        objects.remove(object);
    }
    
    /**
     * 물리 시뮬레이션을 업데이트합니다
     * @param deltaTime 경과 시간
     */
    public void update(double deltaTime) {
        // 모든 객체에 전역 힘 적용
        for (PhysicsObject object : objects) {
            for (Force force : globalForces) {
                if (force.isEnabled()) {
                    force.apply(object, deltaTime);
                }
            }
        }
        
        // 각 객체 업데이트
        for (PhysicsObject object : objects) {
            object.update(deltaTime);
        }
        
        // 충돌 검사 및 처리
        handleCollisions();
    }
    
    /**
     * 충돌을 처리합니다
     */
    private void handleCollisions() {
        // 모든 객체 쌍에 대해 충돌 검사
        for (int i = 0; i < objects.size(); i++) {
            for (int j = i + 1; j < objects.size(); j++) {
                PhysicsObject obj1 = objects.get(i);
                PhysicsObject obj2 = objects.get(j);
                
                // 충돌 검사 및 처리는 구체적인 구현에서 수행
            }
        }
    }
    
    /**
     * 특정 이름의 힘을 찾습니다
     * @param name 힘의 이름
     * @return 찾은 힘, 없으면 null
     */
    public Force getForce(String name) {
        return globalForces.stream()
            .filter(force -> force.getName().equals(name))
            .findFirst()
            .orElse(null);
    }
    
    // Getter/Setter 메서드들
    public double getAirDensity() { return airDensity; }
    public void setAirDensity(double airDensity) { this.airDensity = airDensity; }
    public List<PhysicsObject> getObjects() { return new ArrayList<>(objects); }
    public List<Force> getGlobalForces() { return new ArrayList<>(globalForces); }
}
```

---

## 2. 중력 시스템

### 2.1 Gravity 클래스

중력을 구현하는 Force 구현체입니다.

```java
/**
 * 중력을 구현하는 클래스
 * 모든 객체에 일정한 가속도를 적용합니다
 */
public class Gravity implements Force {
    private double gravity;           // 중력 가속도 (pixels/s²)
    private Vector2D direction;       // 중력 방향
    private boolean enabled;
    
    // 지구 중력 (실제값의 스케일 조정)
    public static final double EARTH_GRAVITY = 500;    // pixels/s²
    public static final double MOON_GRAVITY = 83;      // 지구의 1/6
    public static final double MARS_GRAVITY = 189;     // 지구의 38%
    
    /**
     * 중력 생성자
     * @param gravity 중력 가속도
     */
    public Gravity(double gravity) {
        this.gravity = gravity;
        this.direction = new Vector2D(0, 1); // 아래 방향
        this.enabled = true;
    }
    
    /**
     * 방향을 지정하는 생성자
     * @param gravity 중력 가속도
     * @param direction 중력 방향
     */
    public Gravity(double gravity, Vector2D direction) {
        this.gravity = gravity;
        this.direction = direction.normalize();
        this.enabled = true;
    }
    
    @Override
    public void apply(PhysicsObject object, double deltaTime) {
        if (!enabled) return;
        
        // F = m * g
        Vector2D force = direction.multiply(gravity * object.getMass());
        object.applyForce(force);
    }
    
    @Override
    public String getName() {
        return "Gravity";
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * 중력 가속도를 설정합니다
     * @param gravity 새 중력 가속도
     */
    public void setGravity(double gravity) {
        this.gravity = gravity;
    }
    
    /**
     * 중력 방향을 설정합니다
     * @param direction 새 방향
     */
    public void setDirection(Vector2D direction) {
        this.direction = direction.normalize();
    }
    
    /**
     * 지정된 환경의 중력을 생성합니다
     * @param environment 환경 이름 ("earth", "moon", "mars")
     * @return 생성된 중력
     */
    public static Gravity forEnvironment(String environment) {
        switch (environment.toLowerCase()) {
            case "moon":
                return new Gravity(MOON_GRAVITY);
            case "mars":
                return new Gravity(MARS_GRAVITY);
            case "earth":
            default:
                return new Gravity(EARTH_GRAVITY);
        }
    }
    
    // Getter 메서드들
    public double getGravity() { return gravity; }
    public Vector2D getDirection() { return new Vector2D(direction); }
}
```

### 2.2 PhysicsBall 클래스

물리 법칙이 적용되는 공입니다.

```java
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 물리 법칙이 적용되는 공
 * Ball 클래스를 확장하여 물리 시뮬레이션 기능을 추가합니다
 */
public class PhysicsBall extends Ball implements PhysicsObject {
    private Vector2D position;
    private Vector2D velocity;
    private Vector2D acceleration;
    private double mass;
    private double restitution;
    
    // 물리 상수
    private static final double DENSITY = 1.0; // 임의의 밀도
    
    /**
     * PhysicsBall 생성자
     * @param x X 좌표
     * @param y Y 좌표
     * @param radius 반지름
     */
    public PhysicsBall(double x, double y, double radius) {
        super(x, y, radius);
        
        this.position = new Vector2D(x, y);
        this.velocity = new Vector2D(0, 0);
        this.acceleration = new Vector2D(0, 0);
        
        // 질량 계산 (2D에서는 면적 사용)
        double area = Math.PI * radius * radius;
        this.mass = DENSITY * area / 1000; // 적절한 스케일로 조정
        
        this.restitution = 0.8; // 기본 반발 계수
    }
    
    @Override
    public Vector2D getPosition() {
        return new Vector2D(position);
    }
    
    @Override
    public void setPosition(Vector2D position) {
        this.position.set(position);
        setX(position.getX());
        setY(position.getY());
    }
    
    @Override
    public Vector2D getVelocity() {
        return new Vector2D(velocity);
    }
    
    @Override
    public void setVelocity(Vector2D velocity) {
        this.velocity.set(velocity);
        setDx(velocity.getX());
        setDy(velocity.getY());
    }
    
    @Override
    public double getMass() {
        return mass;
    }
    
    @Override
    public void applyForce(Vector2D force) {
        // F = ma → a = F/m
        Vector2D acc = force.divide(mass);
        acceleration = acceleration.add(acc);
    }
    
    @Override
    public void update(double deltaTime) {
        // 속도 업데이트: v = v + a × Δt
        velocity = velocity.add(acceleration.multiply(deltaTime));
        
        // 위치 업데이트: p = p + v × Δt
        position = position.add(velocity.multiply(deltaTime));
        
        // Ball 클래스의 위치 동기화
        setX(position.getX());
        setY(position.getY());
        setDx(velocity.getX());
        setDy(velocity.getY());
        
        // 가속도 초기화 (매우 중요!)
        acceleration = new Vector2D(0, 0);
    }
    
    @Override
    public double getRestitution() {
        return restitution;
    }
    
    /**
     * 반발 계수를 설정합니다
     * @param restitution 반발 계수 (0~1)
     */
    public void setRestitution(double restitution) {
        this.restitution = Math.max(0, Math.min(1, restitution));
    }
    
    /**
     * 재질을 설정합니다
     * @param material 재질
     */
    public void setMaterial(Material material) {
        this.restitution = material.getRestitution();
        setColor(material.getColor());
    }
    
    @Override
    public void handleCollision(Collidable other) {
        if (other instanceof Floor) {
            // 바닥과의 충돌
            Floor floor = (Floor) other;
            
            if (getY() + getRadius() >= floor.getY() && velocity.getY() > 0) {
                // 반발 계수 적용
                velocity.setY(-velocity.getY() * restitution);
                
                // 위치 보정 (바닥에 파묻히지 않도록)
                position.setY(floor.getY() - getRadius());
                setY(position.getY());
                
                // 속도가 너무 작으면 정지
                if (Math.abs(velocity.getY()) < 10) {
                    velocity.setY(0);
                }
            }
        } else if (other instanceof Wall) {
            // 벽과의 충돌
            Wall wall = (Wall) other;
            
            // 왼쪽 벽
            if (wall.isLeftWall() && getX() - getRadius() <= wall.getX() && velocity.getX() < 0) {
                velocity.setX(-velocity.getX() * restitution);
                position.setX(wall.getX() + getRadius());
                setX(position.getX());
            }
            // 오른쪽 벽
            else if (!wall.isLeftWall() && getX() + getRadius() >= wall.getX() && velocity.getX() > 0) {
                velocity.setX(-velocity.getX() * restitution);
                position.setX(wall.getX() - getRadius());
                setX(position.getX());
            }
        }
    }
    
    /**
     * 운동 에너지를 계산합니다
     * @return 운동 에너지 (J)
     */
    public double getKineticEnergy() {
        double speed = velocity.magnitude();
        return 0.5 * mass * speed * speed;
    }
    
    /**
     * 위치 에너지를 계산합니다
     * @param gravity 중력 가속도
     * @param referenceHeight 기준 높이
     * @return 위치 에너지 (J)
     */
    public double getPotentialEnergy(double gravity, double referenceHeight) {
        double height = referenceHeight - position.getY();
        return mass * gravity * height;
    }
    
    /**
     * 총 기계적 에너지를 계산합니다
     * @param gravity 중력 가속도
     * @param referenceHeight 기준 높이
     * @return 총 에너지 (J)
     */
    public double getTotalEnergy(double gravity, double referenceHeight) {
        return getKineticEnergy() + getPotentialEnergy(gravity, referenceHeight);
    }
}
```

---

## 3. 바람 효과

### 3.1 NoiseGenerator 클래스

자연스러운 변화를 위한 노이즈 생성기입니다.

```java
import java.util.HashMap;
import java.util.Map;

/**
 * Perlin Noise를 간단히 구현한 노이즈 생성기
 * 자연스러운 랜덤 변화를 만들어냅니다
 */
public class NoiseGenerator {
    private Map<Double, Double> cache;
    private double scale;
    
    /**
     * 노이즈 생성기 생성자
     * @param scale 노이즈 스케일 (작을수록 부드러움)
     */
    public NoiseGenerator(double scale) {
        this.cache = new HashMap<>();
        this.scale = scale;
    }
    
    /**
     * 주어진 시간에서의 노이즈 값을 반환합니다
     * @param t 시간
     * @return -1에서 1 사이의 노이즈 값
     */
    public double getValue(double t) {
        double scaledT = t * scale;
        return getOrGenerate(scaledT);
    }
    
    /**
     * 캐시에서 값을 가져오거나 생성합니다
     * @param t 스케일된 시간
     * @return 노이즈 값
     */
    private double getOrGenerate(double t) {
        // 정수 부분과 소수 부분 분리
        double intPart = Math.floor(t);
        double fracPart = t - intPart;
        
        // 두 정수 지점의 값
        double v1 = getRandomValue(intPart);
        double v2 = getRandomValue(intPart + 1);
        
        // smoothstep 보간
        double smooth = smoothstep(fracPart);
        return v1 * (1 - smooth) + v2 * smooth;
    }
    
    /**
     * 정수 지점의 랜덤 값을 반환합니다
     * @param i 정수 인덱스
     * @return -1에서 1 사이의 값
     */
    private double getRandomValue(double i) {
        return cache.computeIfAbsent(i, k -> {
            // 간단한 해시 함수
            long hash = Double.doubleToLongBits(k);
            hash = hash * 1664525L + 1013904223L;
            return (hash & 0xFFFFFFF) / (double) 0xFFFFFFF * 2 - 1;
        });
    }
    
    /**
     * smoothstep 함수
     * @param t 0에서 1 사이의 값
     * @return 부드럽게 보간된 값
     */
    private double smoothstep(double t) {
        // 3t² - 2t³
        return t * t * (3 - 2 * t);
    }
    
    /**
     * 2D 노이즈 값을 반환합니다
     * @param x X 좌표
     * @param y Y 좌표
     * @return -1에서 1 사이의 노이즈 값
     */
    public double getValue2D(double x, double y) {
        // 간단한 2D 노이즈 구현
        double nx = getValue(x);
        double ny = getValue(y);
        return (nx + ny) / 2;
    }
}
```

### 3.2 Wind 클래스

바람 효과를 구현하는 Force입니다.

```java
/**
 * 바람 효과를 구현하는 클래스
 * 공기 저항과 바람의 힘을 시뮬레이션합니다
 */
public class Wind implements Force {
    private Vector2D windVelocity;
    private double strength;
    private NoiseGenerator noiseX;
    private NoiseGenerator noiseY;
    private double time;
    private boolean enabled;
    
    // 공기 역학 상수
    private static final double DRAG_COEFFICIENT = 0.47; // 구의 항력 계수
    
    /**
     * 바람 생성자
     * @param windVelocity 바람 속도 벡터
     * @param strength 바람 세기 (0~1)
     */
    public Wind(Vector2D windVelocity, double strength) {
        this.windVelocity = new Vector2D(windVelocity);
        this.strength = Math.max(0, Math.min(1, strength));
        this.noiseX = new NoiseGenerator(0.5);
        this.noiseY = new NoiseGenerator(0.7);
        this.time = 0;
        this.enabled = true;
    }
    
    @Override
    public void apply(PhysicsObject object, double deltaTime) {
        if (!enabled) return;
        
        // 시간 업데이트
        time += deltaTime;
        
        // 노이즈를 적용한 바람 속도
        double noiseFactorX = 1 + noiseX.getValue(time) * 0.3;
        double noiseFactorY = 1 + noiseY.getValue(time) * 0.3;
        
        Vector2D currentWind = new Vector2D(
            windVelocity.getX() * noiseFactorX * strength,
            windVelocity.getY() * noiseFactorY * strength
        );
        
        // 상대 속도 계산
        Vector2D relativeVelocity = currentWind.subtract(object.getVelocity());
        
        // 항력 계산: F = 0.5 * ρ * A * Cd * v²
        // 단순화를 위해 단면적과 공기 밀도를 상수로 처리
        double speed = relativeVelocity.magnitude();
        if (speed > 0) {
            double dragMagnitude = 0.5 * DRAG_COEFFICIENT * speed * speed * 0.01;
            Vector2D dragForce = relativeVelocity.normalize().multiply(dragMagnitude);
            
            object.applyForce(dragForce);
        }
    }
    
    @Override
    public String getName() {
        return "Wind";
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * 바람 방향을 설정합니다
     * @param angle 각도 (라디안)
     */
    public void setDirection(double angle) {
        double magnitude = windVelocity.magnitude();
        windVelocity = new Vector2D(
            Math.cos(angle) * magnitude,
            Math.sin(angle) * magnitude
        );
    }
    
    /**
     * 바람 속도를 설정합니다
     * @param speed 속도
     */
    public void setSpeed(double speed) {
        windVelocity = windVelocity.normalize().multiply(speed);
    }
    
    /**
     * 바람 세기를 설정합니다
     * @param strength 세기 (0~1)
     */
    public void setStrength(double strength) {
        this.strength = Math.max(0, Math.min(1, strength));
    }
    
    /**
     * 돌풍을 생성합니다
     * @param duration 지속 시간
     * @param multiplier 세기 배수
     */
    public void createGust(double duration, double multiplier) {
        // 임시로 바람 세기 증가
        double originalStrength = strength;
        strength *= multiplier;
        
        // 일정 시간 후 원래대로 복구
        new Thread(() -> {
            try {
                Thread.sleep((long)(duration * 1000));
                strength = originalStrength;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    // Getter 메서드들
    public Vector2D getWindVelocity() { return new Vector2D(windVelocity); }
    public double getStrength() { return strength; }
    public double getAngle() { 
        return Math.atan2(windVelocity.getY(), windVelocity.getX()); 
    }
    public double getSpeed() { return windVelocity.magnitude(); }
}
```

---

## 4. 마찰력 구현

### 4.1 Friction 클래스

바닥과의 마찰력을 구현합니다.

```java
/**
 * 마찰력을 구현하는 클래스
 * 정지 마찰과 운동 마찰을 시뮬레이션합니다
 */
public class Friction implements Force {
    private double staticCoefficient;   // 정지 마찰 계수
    private double kineticCoefficient;  // 운동 마찰 계수
    private double threshold;           // 정지 판단 임계값
    private boolean enabled;
    
    // 일반적인 마찰 계수
    public static final double RUBBER_CONCRETE_STATIC = 1.0;
    public static final double RUBBER_CONCRETE_KINETIC = 0.7;
    public static final double WOOD_WOOD_STATIC = 0.5;
    public static final double WOOD_WOOD_KINETIC = 0.3;
    public static final double ICE_ICE_STATIC = 0.1;
    public static final double ICE_ICE_KINETIC = 0.03;
    
    /**
     * 마찰력 생성자
     * @param staticCoefficient 정지 마찰 계수
     * @param kineticCoefficient 운동 마찰 계수
     */
    public Friction(double staticCoefficient, double kineticCoefficient) {
        this.staticCoefficient = staticCoefficient;
        this.kineticCoefficient = kineticCoefficient;
        this.threshold = 5.0; // 속도 임계값 (pixels/s)
        this.enabled = true;
    }
    
    /**
     * 재질 조합으로 마찰력을 생성합니다
     * @param material 재질 이름
     * @return 생성된 마찰력
     */
    public static Friction forMaterial(String material) {
        switch (material.toLowerCase()) {
            case "rubber":
                return new Friction(RUBBER_CONCRETE_STATIC, RUBBER_CONCRETE_KINETIC);
            case "wood":
                return new Friction(WOOD_WOOD_STATIC, WOOD_WOOD_KINETIC);
            case "ice":
                return new Friction(ICE_ICE_STATIC, ICE_ICE_KINETIC);
            default:
                return new Friction(0.5, 0.3);
        }
    }
    
    @Override
    public void apply(PhysicsObject object, double deltaTime) {
        if (!enabled) return;
        
        // 바닥에 닿아있는지 확인 (Y 속도와 위치로 판단)
        boolean onGround = Math.abs(object.getVelocity().getY()) < threshold;
        
        if (onGround) {
            Vector2D velocity = object.getVelocity();
            double speed = velocity.magnitude();
            
            if (speed > 0) {
                // 수직항력 계산 (단순화: N = mg)
                double normalForce = object.getMass() * 500; // 중력 가속도 사용
                
                // 마찰 계수 선택
                double coefficient;
                if (speed < threshold) {
                    // 정지 마찰
                    coefficient = staticCoefficient;
                } else {
                    // 운동 마찰
                    coefficient = kineticCoefficient;
                }
                
                // 마찰력 = μ × N
                double frictionMagnitude = coefficient * normalForce;
                
                // 마찰력 방향 (속도와 반대)
                Vector2D frictionForce = velocity.normalize().multiply(-frictionMagnitude);
                
                // 마찰력이 속도보다 크면 정지
                if (frictionMagnitude * deltaTime > speed * object.getMass()) {
                    object.setVelocity(new Vector2D(0, velocity.getY()));
                } else {
                    object.applyForce(frictionForce);
                }
            }
        }
    }
    
    @Override
    public String getName() {
        return "Friction";
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    // Getter/Setter 메서드들
    public double getStaticCoefficient() { return staticCoefficient; }
    public void setStaticCoefficient(double coefficient) { 
        this.staticCoefficient = coefficient; 
    }
    
    public double getKineticCoefficient() { return kineticCoefficient; }
    public void setKineticCoefficient(double coefficient) { 
        this.kineticCoefficient = coefficient; 
    }
    
    public double getThreshold() { return threshold; }
    public void setThreshold(double threshold) { this.threshold = threshold; }
}
```

### 4.2 AirResistance 클래스

공기 저항을 구현합니다.

```java
/**
 * 공기 저항을 구현하는 클래스
 * 속도의 제곱에 비례하는 항력을 적용합니다
 */
public class AirResistance implements Force {
    private double dragCoefficient;
    private double airDensity;
    private boolean enabled;
    
    // 다양한 형태의 항력 계수
    public static final double SPHERE = 0.47;
    public static final double CUBE = 1.05;
    public static final double STREAMLINED = 0.04;
    public static final double FLAT_PLATE = 1.28;
    
    /**
     * 공기 저항 생성자
     * @param dragCoefficient 항력 계수
     * @param airDensity 공기 밀도
     */
    public AirResistance(double dragCoefficient, double airDensity) {
        this.dragCoefficient = dragCoefficient;
        this.airDensity = airDensity;
        this.enabled = true;
    }
    
    /**
     * 기본 생성자 (구 형태, 표준 공기 밀도)
     */
    public AirResistance() {
        this(SPHERE, PhysicsEngine.AIR_DENSITY_SEA_LEVEL);
    }
    
    @Override
    public void apply(PhysicsObject object, double deltaTime) {
        if (!enabled) return;
        
        Vector2D velocity = object.getVelocity();
        double speed = velocity.magnitude();
        
        if (speed > 0) {
            // 단면적 계산 (간단화: 반지름으로부터 추정)
            double area = Math.PI * 10 * 10 / 10000; // m² 단위로 변환
            
            // 항력 계산: F = 0.5 * ρ * A * Cd * v²
            double dragMagnitude = 0.5 * airDensity * area * 
                                  dragCoefficient * speed * speed;
            
            // 항력 방향 (속도와 반대)
            Vector2D dragForce = velocity.normalize().multiply(-dragMagnitude);
            
            object.applyForce(dragForce);
        }
    }
    
    @Override
    public String getName() {
        return "AirResistance";
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * 고도에 따른 공기 밀도를 계산합니다
     * @param altitude 고도 (m)
     * @return 공기 밀도 (kg/m³)
     */
    public static double getAirDensityAtAltitude(double altitude) {
        // 간단한 대기 모델
        return PhysicsEngine.AIR_DENSITY_SEA_LEVEL * 
               Math.exp(-altitude / 8000);
    }
    
    // Getter/Setter 메서드들
    public double getDragCoefficient() { return dragCoefficient; }
    public void setDragCoefficient(double coefficient) { 
        this.dragCoefficient = coefficient; 
    }
    
    public double getAirDensity() { return airDensity; }
    public void setAirDensity(double density) { this.airDensity = density; }
}
```

---

## 5. 반발 계수와 충돌

### 5.1 Material 열거형

다양한 재질의 물리적 특성을 정의합니다.

```java
import javafx.scene.paint.Color;

/**
 * 재질의 물리적 특성을 정의하는 열거형
 * 반발 계수와 시각적 특성을 포함합니다
 */
public enum Material {
    RUBBER(0.9, Color.DARKRED, "고무"),
    STEEL(0.8, Color.SILVER, "강철"),
    WOOD(0.6, Color.BROWN, "나무"),
    GLASS(0.7, Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.8), "유리"),
    CLAY(0.2, Color.SIENNA, "점토"),
    SUPER_BALL(0.95, Color.MAGENTA, "슈퍼볼"),
    CONCRETE(0.4, Color.GRAY, "콘크리트"),
    FOAM(0.3, Color.LIGHTYELLOW, "폼");
    
    private final double restitution;
    private final Color color;
    private final String name;
    
    Material(double restitution, Color color, String name) {
        this.restitution = restitution;
        this.color = color;
        this.name = name;
    }
    
    // Getter 메서드들
    public double getRestitution() { return restitution; }
    public Color getColor() { return color; }
    public String getName() { return name; }
}
```

### 5.2 ElasticCollision 클래스

탄성 충돌을 처리하는 유틸리티 클래스입니다.

```java
/**
 * 탄성 충돌을 처리하는 클래스
 * 운동량 보존과 반발 계수를 고려한 충돌 해결
 */
public class ElasticCollision {
    
    /**
     * 두 공의 충돌을 해결합니다
     * @param ball1 첫 번째 공
     * @param ball2 두 번째 공
     */
    public static void resolveCollision(PhysicsBall ball1, PhysicsBall ball2) {
        // 위치 벡터
        Vector2D pos1 = ball1.getPosition();
        Vector2D pos2 = ball2.getPosition();
        
        // 충돌 방향 벡터 (충돌 법선)
        Vector2D normal = pos2.subtract(pos1);
        double distance = normal.magnitude();
        
        // 충돌하지 않으면 무시
        if (distance > ball1.getRadius() + ball2.getRadius()) {
            return;
        }
        
        // 법선 벡터 정규화
        normal = normal.normalize();
        
        // 상대 속도
        Vector2D v1 = ball1.getVelocity();
        Vector2D v2 = ball2.getVelocity();
        Vector2D relativeVelocity = v1.subtract(v2);
        
        // 충돌 방향의 상대 속도
        double velocityAlongNormal = relativeVelocity.dot(normal);
        
        // 멀어지고 있으면 무시
        if (velocityAlongNormal > 0) {
            return;
        }
        
        // 반발 계수 (두 공의 평균)
        double e = (ball1.getRestitution() + ball2.getRestitution()) / 2;
        
        // 충격량 계산
        double m1 = ball1.getMass();
        double m2 = ball2.getMass();
        double j = -(1 + e) * velocityAlongNormal / (1/m1 + 1/m2);
        
        // 충격량 벡터
        Vector2D impulse = normal.multiply(j);
        
        // 새로운 속도 계산
        Vector2D v1New = v1.add(impulse.divide(m1));
        Vector2D v2New = v2.subtract(impulse.divide(m2));
        
        // 속도 업데이트
        ball1.setVelocity(v1New);
        ball2.setVelocity(v2New);
        
        // 위치 보정 (겹침 해결)
        double overlap = ball1.getRadius() + ball2.getRadius() - distance;
        if (overlap > 0) {
            Vector2D separation = normal.multiply(overlap / 2);
            ball1.setPosition(pos1.subtract(separation));
            ball2.setPosition(pos2.add(separation));
        }
    }
    
    /**
     * 공과 벽의 충돌을 해결합니다
     * @param ball 공
     * @param wallNormal 벽의 법선 벡터
     * @param wallPosition 벽의 위치
     */
    public static void resolveBallWallCollision(PhysicsBall ball, 
                                               Vector2D wallNormal, 
                                               double wallPosition) {
        Vector2D velocity = ball.getVelocity();
        Vector2D position = ball.getPosition();
        
        // 벽과의 거리
        double distance = Math.abs(position.dot(wallNormal) - wallPosition);
        
        // 충돌하지 않으면 무시
        if (distance > ball.getRadius()) {
            return;
        }
        
        // 벽으로 접근하는지 확인
        double velocityTowardWall = velocity.dot(wallNormal);
        if (velocityTowardWall > 0) {
            return;
        }
        
        // 반사 속도 계산
        Vector2D reflected = velocity.subtract(
            wallNormal.multiply(2 * velocityTowardWall)
        );
        
        // 반발 계수 적용
        reflected = reflected.multiply(ball.getRestitution());
        
        ball.setVelocity(reflected);
        
        // 위치 보정
        double penetration = ball.getRadius() - distance;
        if (penetration > 0) {
            ball.setPosition(position.add(wallNormal.multiply(penetration)));
        }
    }
    
    /**
     * 1차원 충돌 계산 (교육용)
     * @param m1 첫 번째 질량
     * @param m2 두 번째 질량
     * @param v1 첫 번째 속도
     * @param v2 두 번째 속도
     * @param e 반발 계수
     * @return [v1', v2'] 충돌 후 속도
     */
    public static double[] calculate1DCollision(double m1, double m2, 
                                               double v1, double v2, double e) {
        // 운동량 보존과 반발 계수를 이용한 계산
        double v1New = ((m1 - e*m2) * v1 + (1 + e) * m2 * v2) / (m1 + m2);
        double v2New = ((m2 - e*m1) * v2 + (1 + e) * m1 * v1) / (m1 + m2);
        
        return new double[]{v1New, v2New};
    }
}
```

---

## 6. 통합 물리 시뮬레이션

### 6.1 PhysicsWorld 클래스

모든 물리 효과를 통합한 월드입니다.

```java
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.*;

/**
 * 물리 시뮬레이션 월드
 * 모든 물리 효과와 객체를 통합 관리합니다
 */
public class PhysicsWorld extends SimpleWorld {
    private PhysicsEngine physicsEngine;
    private List<PhysicsBall> physicsBalls;
    
    // 물리 효과 활성화 플래그
    private boolean gravityEnabled = true;
    private boolean windEnabled = false;
    private boolean frictionEnabled = true;
    private boolean airResistanceEnabled = true;
    
    // 물리 효과 객체들
    private Gravity gravity;
    private Wind wind;
    private Friction friction;
    private AirResistance airResistance;
    
    // 기본 물리 설정
    private static final double DEFAULT_GRAVITY = 500; // pixels/s²
    
    /**
     * PhysicsWorld 생성자
     * @param width 월드 너비
     * @param height 월드 높이
     */
    public PhysicsWorld(double width, double height) {
        super(width, height);
        
        this.physicsEngine = new PhysicsEngine();
        this.physicsBalls = new ArrayList<>();
        
        initializePhysics();
        initializeBounds();
    }
    
    /**
     * 물리 시스템을 초기화합니다
     */
    private void initializePhysics() {
        // 중력
        gravity = new Gravity(DEFAULT_GRAVITY);
        physicsEngine.addGlobalForce(gravity);
        
        // 바람 (초기에는 비활성화)
        wind = new Wind(new Vector2D(100, 0), 0.5);
        wind.setEnabled(false);
        physicsEngine.addGlobalForce(wind);
        
        // 마찰
        friction = Friction.forMaterial("rubber");
        physicsEngine.addGlobalForce(friction);
        
        // 공기 저항
        airResistance = new AirResistance();
        physicsEngine.addGlobalForce(airResistance);
    }
    
    /**
     * 경계를 초기화합니다
     */
    private void initializeBounds() {
        // 바닥
        Floor floor = new Floor(0, getHeight() - 20, getWidth());
        add(floor);
        
        // 벽
        Wall leftWall = new Wall(10, 0, getHeight(), true);
        Wall rightWall = new Wall(getWidth() - 10, 0, getHeight(), false);
        add(leftWall);
        add(rightWall);
    }
    
    /**
     * 물리 공을 추가합니다
     * @param ball 추가할 공
     */
    public void addPhysicsBall(PhysicsBall ball) {
        physicsBalls.add(ball);
        physicsEngine.addObject(ball);
        add(ball);
    }
    
    /**
     * 물리 공을 제거합니다
     * @param ball 제거할 공
     */
    public void removePhysicsBall(PhysicsBall ball) {
        physicsBalls.remove(ball);
        physicsEngine.removeObject(ball);
        remove(ball);
    }
    
    @Override
    public void update(double deltaTime) {
        // 물리 엔진 업데이트
        physicsEngine.update(deltaTime);
        
        // 공 간 충돌 처리
        handleBallCollisions();
        
        // 부모 클래스 업데이트 (충돌 검사 등)
        super.update(deltaTime);
        
        // 화면 밖으로 나간 공 제거
        physicsBalls.removeIf(ball -> {
            if (ball.getY() > getHeight() + 100) {
                physicsEngine.removeObject(ball);
                remove(ball);
                return true;
            }
            return false;
        });
    }
    
    /**
     * 공 간 충돌을 처리합니다
     */
    private void handleBallCollisions() {
        for (int i = 0; i < physicsBalls.size(); i++) {
            for (int j = i + 1; j < physicsBalls.size(); j++) {
                PhysicsBall ball1 = physicsBalls.get(i);
                PhysicsBall ball2 = physicsBalls.get(j);
                
                double distance = ball1.getPosition().distance(ball2.getPosition());
                if (distance < ball1.getRadius() + ball2.getRadius()) {
                    ElasticCollision.resolveCollision(ball1, ball2);
                }
            }
        }
    }
    
    @Override
    public void render(GraphicsContext gc) {
        // 배경
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(0, 0, getWidth(), getHeight());
        
        // 물리 객체 렌더링
        super.render(gc);
        
        // 바람 시각화
        if (windEnabled) {
            renderWind(gc);
        }
        
        // 정보 표시
        renderInfo(gc);
    }
    
    /**
     * 바람을 시각화합니다
     * @param gc 그래픽스 컨텍스트
     */
    private void renderWind(GraphicsContext gc) {
        gc.setStroke(Color.DARKBLUE.deriveColor(0, 1, 1, 0.3));
        gc.setLineWidth(2);
        
        double angle = wind.getAngle();
        double speed = wind.getSpeed() / 5; // 시각화를 위한 스케일
        
        for (int y = 50; y < getHeight() - 50; y += 50) {
            for (int x = 50; x < getWidth() - 50; x += 100) {
                double endX = x + Math.cos(angle) * speed;
                double endY = y + Math.sin(angle) * speed;
                
                gc.strokeLine(x, y, endX, endY);
                
                // 화살표 머리
                double arrowAngle = angle + Math.PI * 0.8;
                gc.strokeLine(endX, endY, 
                    endX + Math.cos(arrowAngle) * 10,
                    endY + Math.sin(arrowAngle) * 10);
                
                arrowAngle = angle - Math.PI * 0.8;
                gc.strokeLine(endX, endY,
                    endX + Math.cos(arrowAngle) * 10,
                    endY + Math.sin(arrowAngle) * 10);
            }
        }
    }
    
    /**
     * 정보를 표시합니다
     * @param gc 그래픽스 컨텍스트
     */
    private void renderInfo(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.setFont(javafx.scene.text.Font.font(14));
        
        int y = 20;
        gc.fillText("물리 효과:", 10, y);
        y += 20;
        
        gc.fillText("중력: " + (gravityEnabled ? "ON" : "OFF") + 
                   " (" + gravity.getGravity() + " pixels/s²)", 20, y);
        y += 20;
        
        gc.fillText("바람: " + (windEnabled ? "ON" : "OFF") +
                   " (속도: " + String.format("%.1f", wind.getSpeed()) + ")", 20, y);
        y += 20;
        
        gc.fillText("마찰: " + (frictionEnabled ? "ON" : "OFF"), 20, y);
        y += 20;
        
        gc.fillText("공기 저항: " + (airResistanceEnabled ? "ON" : "OFF"), 20, y);
        y += 20;
        
        gc.fillText("공 개수: " + physicsBalls.size(), 20, y);
        
        // 에너지 정보 (첫 번째 공)
        if (!physicsBalls.isEmpty()) {
            y += 30;
            PhysicsBall ball = physicsBalls.get(0);
            double ke = ball.getKineticEnergy();
            double pe = ball.getPotentialEnergy(gravity.getGravity(), getHeight());
            double total = ke + pe;
            
            gc.fillText("첫 번째 공 에너지:", 10, y);
            y += 20;
            gc.fillText(String.format("운동: %.1f J", ke), 20, y);
            y += 20;
            gc.fillText(String.format("위치: %.1f J", pe), 20, y);
            y += 20;
            gc.fillText(String.format("총합: %.1f J", total), 20, y);
        }
    }
    
    // 물리 효과 제어 메서드들
    
    public void setGravityEnabled(boolean enabled) {
        this.gravityEnabled = enabled;
        gravity.setEnabled(enabled);
    }
    
    public void setWindEnabled(boolean enabled) {
        this.windEnabled = enabled;
        wind.setEnabled(enabled);
    }
    
    public void setFrictionEnabled(boolean enabled) {
        this.frictionEnabled = enabled;
        friction.setEnabled(enabled);
    }
    
    public void setAirResistanceEnabled(boolean enabled) {
        this.airResistanceEnabled = enabled;
        airResistance.setEnabled(enabled);
    }
    
    public void setGravityValue(double value) {
        gravity.setGravity(value);
    }
    
    public void setWindAngle(double angle) {
        wind.setDirection(angle);
    }
    
    public void setWindSpeed(double speed) {
        wind.setSpeed(speed);
    }
    
    public void setEnvironment(String environment) {
        switch (environment.toLowerCase()) {
            case "earth":
                gravity.setGravity(Gravity.EARTH_GRAVITY);
                airResistance.setAirDensity(PhysicsEngine.AIR_DENSITY_SEA_LEVEL);
                break;
            case "moon":
                gravity.setGravity(Gravity.MOON_GRAVITY);
                airResistance.setAirDensity(0); // 진공
                break;
            case "mars":
                gravity.setGravity(Gravity.MARS_GRAVITY);
                airResistance.setAirDensity(0.02); // 희박한 대기
                break;
            case "water":
                gravity.setGravity(Gravity.EARTH_GRAVITY);
                airResistance.setAirDensity(1000); // 물의 밀도
                airResistance.setDragCoefficient(1.0); // 높은 항력
                break;
        }
    }
    
    // Getter 메서드들
    public Gravity getGravity() { return gravity; }
    public Wind getWind() { return wind; }
    public Friction getFriction() { return friction; }
    public AirResistance getAirResistance() { return airResistance; }
    public List<PhysicsBall> getPhysicsBalls() { 
        return new ArrayList<>(physicsBalls); 
    }
}
```

---

## 7. 메인 데모 애플리케이션

### 7.1 PhysicsDemo 클래스

물리 시뮬레이션 데모 애플리케이션입니다.

```java
import javafx.application.Application;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * 물리 시뮬레이션 데모 애플리케이션
 * 다양한 물리 효과를 실시간으로 조절하고 관찰할 수 있습니다
 */
public class PhysicsDemo extends Application {
    private PhysicsWorld world;
    private Canvas canvas;
    private GraphicsContext gc;
    private AnimationTimer gameLoop;
    
    private long lastTime;
    
    // UI 컨트롤
    private CheckBox gravityCheckBox;
    private Slider gravitySlider;
    private CheckBox windCheckBox;
    private Slider windAngleSlider;
    private Slider windSpeedSlider;
    private CheckBox frictionCheckBox;
    private CheckBox airResistanceCheckBox;
    private ComboBox<Material> materialComboBox;
    private ComboBox<String> environmentComboBox;
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("물리 시뮬레이션 데모");
        
        BorderPane root = new BorderPane();
        
        // 게임 캔버스
        canvas = new Canvas(800, 600);
        gc = canvas.getGraphicsContext2D();
        root.setCenter(canvas);
        
        // 컨트롤 패널
        VBox controlPanel = createControlPanel();
        root.setRight(controlPanel);
        
        // 월드 생성
        world = new PhysicsWorld(800, 600);
        
        // 이벤트 핸들러
        setupEventHandlers();
        
        // 게임 루프 시작
        startGameLoop();
        
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /**
     * 컨트롤 패널을 생성합니다
     * @return 생성된 컨트롤 패널
     */
    private VBox createControlPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(250);
        panel.setStyle("-fx-background-color: #f0f0f0;");
        
        // 제목
        Label titleLabel = new Label("물리 효과 컨트롤");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // 중력 컨트롤
        VBox gravityBox = new VBox(5);
        gravityCheckBox = new CheckBox("중력");
        gravityCheckBox.setSelected(true);
        gravitySlider = new Slider(0, 1000, 500);
        gravitySlider.setShowTickLabels(true);
        gravitySlider.setShowTickMarks(true);
        Label gravityLabel = new Label("중력: 500 pixels/s²");
        gravityBox.getChildren().addAll(gravityCheckBox, gravitySlider, gravityLabel);
        
        // 바람 컨트롤
        VBox windBox = new VBox(5);
        windCheckBox = new CheckBox("바람");
        windCheckBox.setSelected(false);
        
        Label windAngleLabel = new Label("바람 방향:");
        windAngleSlider = new Slider(0, 360, 0);
        windAngleSlider.setShowTickLabels(true);
        
        Label windSpeedLabel = new Label("바람 속도:");
        windSpeedSlider = new Slider(0, 200, 100);
        windSpeedSlider.setShowTickLabels(true);
        
        windBox.getChildren().addAll(windCheckBox, windAngleLabel, 
                                    windAngleSlider, windSpeedLabel, windSpeedSlider);
        
        // 마찰 컨트롤
        frictionCheckBox = new CheckBox("마찰");
        frictionCheckBox.setSelected(true);
        
        // 공기 저항 컨트롤
        airResistanceCheckBox = new CheckBox("공기 저항");
        airResistanceCheckBox.setSelected(true);
        
        // 재질 선택
        Label materialLabel = new Label("공 재질:");
        materialComboBox = new ComboBox<>();
        materialComboBox.getItems().addAll(Material.values());
        materialComboBox.setValue(Material.RUBBER);
        
        // 환경 선택
        Label environmentLabel = new Label("환경:");
        environmentComboBox = new ComboBox<>();
        environmentComboBox.getItems().addAll("Earth", "Moon", "Mars", "Water");
        environmentComboBox.setValue("Earth");
        
        // 버튼들
        Button createBallButton = new Button("공 생성");
        createBallButton.setMaxWidth(Double.MAX_VALUE);
        createBallButton.setOnAction(e -> createBall());
        
        Button clearButton = new Button("모두 지우기");
        clearButton.setMaxWidth(Double.MAX_VALUE);
        clearButton.setOnAction(e -> clearBalls());
        
        Button gustButton = new Button("돌풍 생성");
        gustButton.setMaxWidth(Double.MAX_VALUE);
        gustButton.setOnAction(e -> createGust());
        
        // 패널에 추가
        panel.getChildren().addAll(
            titleLabel,
            new Separator(),
            gravityBox,
            new Separator(),
            windBox,
            new Separator(),
            frictionCheckBox,
            airResistanceCheckBox,
            new Separator(),
            materialLabel,
            materialComboBox,
            environmentLabel,
            environmentComboBox,
            new Separator(),
            createBallButton,
            clearButton,
            gustButton
        );
        
        return panel;
    }
    
    /**
     * 이벤트 핸들러를 설정합니다
     */
    private void setupEventHandlers() {
        // 중력 컨트롤
        gravityCheckBox.setOnAction(e -> 
            world.setGravityEnabled(gravityCheckBox.isSelected()));
        
        gravitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            world.setGravityValue(newVal.doubleValue());
            // 레이블 업데이트
            Label label = (Label) ((VBox) gravitySlider.getParent())
                .getChildren().get(2);
            label.setText(String.format("중력: %.0f pixels/s²", newVal.doubleValue()));
        });
        
        // 바람 컨트롤
        windCheckBox.setOnAction(e -> 
            world.setWindEnabled(windCheckBox.isSelected()));
        
        windAngleSlider.valueProperty().addListener((obs, oldVal, newVal) -> 
            world.setWindAngle(Math.toRadians(newVal.doubleValue())));
        
        windSpeedSlider.valueProperty().addListener((obs, oldVal, newVal) -> 
            world.setWindSpeed(newVal.doubleValue()));
        
        // 마찰 컨트롤
        frictionCheckBox.setOnAction(e -> 
            world.setFrictionEnabled(frictionCheckBox.isSelected()));
        
        // 공기 저항 컨트롤
        airResistanceCheckBox.setOnAction(e -> 
            world.setAirResistanceEnabled(airResistanceCheckBox.isSelected()));
        
        // 환경 선택
        environmentComboBox.setOnAction(e -> 
            world.setEnvironment(environmentComboBox.getValue()));
        
        // 캔버스 클릭으로 공 생성
        canvas.setOnMouseClicked(e -> {
            PhysicsBall ball = new PhysicsBall(e.getX(), e.getY(), 15);
            ball.setMaterial(materialComboBox.getValue());
            
            // 랜덤 초기 속도
            double speed = Math.random() * 200 - 100;
            ball.setVelocity(new Vector2D(speed, -Math.abs(speed)));
            
            world.addPhysicsBall(ball);
        });
    }
    
    /**
     * 게임 루프를 시작합니다
     */
    private void startGameLoop() {
        lastTime = System.nanoTime();
        
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double deltaTime = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                
                if (deltaTime > 0.05) deltaTime = 0.05;
                
                world.update(deltaTime);
                world.render(gc);
            }
        };
        
        gameLoop.start();
    }
    
    /**
     * 새 공을 생성합니다
     */
    private void createBall() {
        double x = 50 + Math.random() * 700;
        double y = 50;
        double radius = 10 + Math.random() * 20;
        
        PhysicsBall ball = new PhysicsBall(x, y, radius);
        ball.setMaterial(materialComboBox.getValue());
        
        // 랜덤 초기 속도
        double vx = Math.random() * 200 - 100;
        double vy = Math.random() * 100;
        ball.setVelocity(new Vector2D(vx, vy));
        
        world.addPhysicsBall(ball);
    }
    
    /**
     * 모든 공을 제거합니다
     */
    private void clearBalls() {
        List<PhysicsBall> balls = new ArrayList<>(world.getPhysicsBalls());
        for (PhysicsBall ball : balls) {
            world.removePhysicsBall(ball);
        }
    }
    
    /**
     * 돌풍을 생성합니다
     */
    private void createGust() {
        if (windCheckBox.isSelected()) {
            world.getWind().createGust(2.0, 3.0); // 2초간 3배 세기
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
```

---

## 8. 실습 프로젝트들

### 8.1 Lab 9-1: 중력 시뮬레이션

다양한 중력 환경을 구현하는 실습입니다.

```java
public class GravityLab extends Application {
    // 지구, 달, 화성의 중력 비교
    // 역중력 모드 구현
    // 중력 방향 변경 (옆으로 떨어지는 중력)
    
    @Override
    public void start(Stage primaryStage) {
        // 세 개의 캔버스로 지구, 달, 화성 동시 시뮬레이션
        // 같은 높이에서 떨어뜨려 낙하 속도 비교
    }
}
```

### 8.2 Lab 9-2: 바람 시뮬레이터

현실적인 바람 효과를 구현하는 실습입니다.

```java
public class WindSimulator extends Application {
    // 돌풍 (갑작스런 바람) 구현
    // 회오리 바람 구현 (중심으로 회전)
    // 바람 파티클로 시각화
    
    private void createTornado(double centerX, double centerY) {
        // 중심으로 회전하는 바람장 생성
        // 거리에 따라 세기 감소
    }
}
```

### 8.3 Lab 9-3: 재질 실험실

다양한 재질의 물리적 특성을 실험하는 실습입니다.

```java
public class MaterialLab extends Application {
    // 같은 높이에서 다른 재질의 공 떨어뜨리기
    // 반발 높이 측정 및 비교
    // 마찰에 따른 이동 거리 측정
    
    private void measureBounceHeight(PhysicsBall ball) {
        // 최대 높이 추적
        // 반발 계수와의 관계 분석
    }
}
```

### 8.4 Lab 9-4: 복합 물리 효과

여러 효과를 조합한 시뮬레이션입니다.

**구현 아이디어:**
1. **수중 환경**: 높은 밀도 + 부력 + 높은 항력
2. **우주 환경**: 무중력 + 진공 + 관성 운동
3. **자기장 효과**: 특정 방향으로 끌어당기는 힘
4. **스프링 시스템**: 후크의 법칙 구현

이것으로 9장 외부 효과의 완전한 구현 가이드를 마칩니다.