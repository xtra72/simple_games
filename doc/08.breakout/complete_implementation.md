# 8장: Breakout Game - 완전한 구현 가이드

## 목차
1. [게임 개요](#1-게임-개요)
2. [프로젝트 구조](#2-프로젝트-구조)
3. [BreakoutPaddle 구현](#3-breakoutpaddle-구현)
4. [BreakoutBall 구현](#4-breakoutball-구현)
5. [벽돌 시스템 구현](#5-벽돌-시스템-구현)
6. [PowerUp 시스템](#6-powerup-시스템)
7. [BreakoutWorld](#7-breakoutworld)
8. [BreakoutGame 메인 클래스](#8-breakoutgame-메인-클래스)
9. [테스트 코드](#9-테스트-코드)
10. [실행 가이드](#10-실행-가이드)

---

## 1. 게임 개요

이 장에서는 2~7장에서 배운 클래스들을 최대한 활용하여 Breakout 게임을 구현합니다.

### 핵심 설계 원칙
- **상속 활용**: Box → BreakoutPaddle, Ball → BreakoutBall/PowerUp
- **인터페이스 활용**: Breakable, MultiHit, Exploding, PowerUpProvider
- **기존 클래스 재사용**: StaticObject, Ball, Box 등

### 사용되는 기존 클래스와 인터페이스
```java
// 2~7장에서 정의된 클래스들
import com.nhnacademy.game.entity.Ball;
import com.nhnacademy.game.entity.Box;
import com.nhnacademy.game.entity.StaticObject;
import com.nhnacademy.game.movement.Movable;
import com.nhnacademy.game.collision.Collidable;
import com.nhnacademy.game.collision.Boundable;
import com.nhnacademy.game.behavior.Breakable;
import com.nhnacademy.game.behavior.MultiHit;
import com.nhnacademy.game.behavior.Exploding;
import com.nhnacademy.game.behavior.PowerUpProvider;
```

---

## 2. 프로젝트 구조

```
src/chapter08_breakout/
├── src/main/java/com/nhnacademy/breakout/
│   ├── BreakoutGame.java           # 메인 애플리케이션
│   ├── world/
│   │   ├── BreakoutWorld.java      # 게임 월드 관리
│   │   ├── BreakoutBall.java       # Ball 확장
│   │   ├── PowerUp.java             # Ball 확장 (떨어지는 아이템)
│   │   └── LevelManager.java        # 레벨 관리
│   └── objects/
│       ├── BreakoutPaddle.java      # Box 확장
│       ├── UnbreakableBrick.java    # StaticObject 확장 (벽)
│       ├── SimpleBrick.java         # StaticObject + Breakable
│       ├── MultiHitBrick.java       # StaticObject + MultiHit
│       ├── ExplodingBrick.java      # StaticObject + Exploding
│       └── PowerUpBrick.java        # SimpleBrick + PowerUpProvider
└── pom.xml
```

---

## 3. BreakoutPaddle 구현

Box 클래스를 확장하여 패들을 구현합니다.

```java
package com.nhnacademy.breakout.objects;

import com.nhnacademy.game.entity.Box;
import com.nhnacademy.game.collision.Collidable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import java.util.Map;
import java.util.HashMap;

/**
 * Breakout 게임의 패들
 * Box 클래스를 확장하여 구현합니다.
 */
public class BreakoutPaddle extends Box {
    private static final double DEFAULT_SPEED = 400.0;
    private static final double MAX_BOUNCE_ANGLE = Math.PI / 3; // 60도
    
    // 파워업 효과 관리
    private Map<PowerUpType, TimedPowerUp> activePowerUps;
    private double defaultWidth;
    private boolean isSticky;
    
    public enum PowerUpType {
        WIDER_PADDLE,
        STICKY_PADDLE,
        LASER
    }
    
    /**
     * 시간 제한 파워업 관리 클래스
     */
    private static class TimedPowerUp {
        PowerUpType type;
        double remainingTime;
        double originalValue;
        
        TimedPowerUp(PowerUpType type, double duration, double originalValue) {
            this.type = type;
            this.remainingTime = duration;
            this.originalValue = originalValue;
        }
    }
    
    public BreakoutPaddle(double x, double y) {
        super(x, y, 100, 15, Color.BLUE);
        this.defaultWidth = 100;
        this.activePowerUps = new HashMap<>();
        this.isSticky = false;
        setVelocity(0, 0); // 패들은 수동 제어
    }
    
    /**
     * 패들 이동 (좌측)
     */
    public void moveLeft(double deltaTime) {
        setVelocity(-DEFAULT_SPEED, 0);
        update(deltaTime);
        setVelocity(0, 0);
    }
    
    /**
     * 패들 이동 (우측)
     */
    public void moveRight(double deltaTime) {
        setVelocity(DEFAULT_SPEED, 0);
        update(deltaTime);
        setVelocity(0, 0);
    }
    
    /**
     * 화면 경계 내로 제한
     */
    public void constrainToBounds(double minX, double maxX) {
        if (getX() < minX) {
            setX(minX);
        } else if (getX() + getWidth() > maxX) {
            setX(maxX - getWidth());
        }
    }
    
    /**
     * 파워업 적용
     */
    public void applyPowerUp(PowerUpType type, double duration) {
        switch (type) {
            case WIDER_PADDLE:
                if (!activePowerUps.containsKey(type)) {
                    activePowerUps.put(type, new TimedPowerUp(type, duration, getWidth()));
                    setWidth(getWidth() * 1.5);
                } else {
                    // 시간 연장
                    activePowerUps.get(type).remainingTime = duration;
                }
                break;
            case STICKY_PADDLE:
                isSticky = true;
                activePowerUps.put(type, new TimedPowerUp(type, duration, 0));
                break;
            case LASER:
                // 레이저 기능은 별도 구현 필요
                activePowerUps.put(type, new TimedPowerUp(type, duration, 0));
                break;
        }
    }
    
    /**
     * 파워업 시간 업데이트
     */
    public void updatePowerUps(double deltaTime) {
        var iterator = activePowerUps.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            TimedPowerUp powerUp = entry.getValue();
            powerUp.remainingTime -= deltaTime;
            
            if (powerUp.remainingTime <= 0) {
                // 파워업 효과 제거
                deactivatePowerUp(entry.getKey(), powerUp);
                iterator.remove();
            }
        }
    }
    
    /**
     * 파워업 비활성화
     */
    private void deactivatePowerUp(PowerUpType type, TimedPowerUp powerUp) {
        switch (type) {
            case WIDER_PADDLE:
                setWidth(defaultWidth);
                break;
            case STICKY_PADDLE:
                isSticky = false;
                break;
        }
    }
    
    @Override
    public void handleCollision(Collidable other) {
        if (other instanceof BreakoutBall) {
            BreakoutBall ball = (BreakoutBall) other;
            
            // 공 반사 각도 계산
            double relativeX = (ball.getCenterX() - getCenterX()) / (getWidth() / 2);
            relativeX = Math.max(-1, Math.min(1, relativeX)); // -1 ~ 1 범위로 제한
            
            double angle = relativeX * MAX_BOUNCE_ANGLE;
            double speed = ball.getSpeed();
            
            ball.setVelocity(Math.sin(angle) * speed, -Math.abs(Math.cos(angle) * speed));
            
            // 끈끈한 패들
            if (isSticky && !ball.isSticky()) {
                ball.setSticky(true);
                ball.setPosition(ball.getCenterX(), getY() - ball.getRadius());
                ball.setVelocity(0, 0);
            }
        }
    }
    
    @Override
    public void draw(GraphicsContext gc) {
        // 그라데이션 효과
        LinearGradient gradient = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, getColor().brighter()),
            new Stop(0.5, getColor()),
            new Stop(1, getColor().darker())
        );
        
        gc.setFill(gradient);
        gc.fillRoundRect(getX(), getY(), getWidth(), getHeight(), 10, 10);
        
        // 파워업 효과 표시
        if (activePowerUps.containsKey(PowerUpType.STICKY_PADDLE)) {
            gc.setStroke(Color.LIME);
            gc.setLineWidth(2);
            gc.strokeRoundRect(getX(), getY(), getWidth(), getHeight(), 10, 10);
        }
    }
    
    public boolean isSticky() { return isSticky; }
}
```

---

## 4. BreakoutBall 구현

Ball 클래스를 확장하여 게임공을 구현합니다.

```java
package com.nhnacademy.breakout.world;

import com.nhnacademy.game.entity.Ball;
import com.nhnacademy.game.collision.Collidable;
import com.nhnacademy.breakout.objects.BreakoutPaddle;
import javafx.scene.paint.Color;
import javafx.scene.canvas.GraphicsContext;

/**
 * Breakout 게임의 공
 * Ball 클래스를 확장하여 구현합니다.
 */
public class BreakoutBall extends Ball {
    private boolean isFireBall;
    private boolean isSticky;
    private static final double DEFAULT_SPEED = 300.0;
    
    public BreakoutBall(double x, double y) {
        super(x, y, 8, Color.WHITE);
        this.isFireBall = false;
        this.isSticky = false;
    }
    
    /**
     * 파이어볼 모드 설정
     */
    public void setFireBall(boolean fireBall) {
        this.isFireBall = fireBall;
        if (fireBall) {
            setColor(Color.ORANGERED);
        } else {
            setColor(Color.WHITE);
        }
    }
    
    /**
     * 끈끈한 상태 설정
     */
    public void setSticky(boolean sticky) {
        this.isSticky = sticky;
    }
    
    /**
     * 공 발사
     */
    public void launch() {
        if (isSticky) {
            isSticky = false;
            double angle = (Math.random() - 0.5) * Math.PI / 3; // -30도 ~ 30도
            setVelocity(
                Math.sin(angle) * DEFAULT_SPEED,
                -Math.cos(angle) * DEFAULT_SPEED
            );
        }
    }
    
    /**
     * 속도 조정
     */
    public void adjustSpeed(double factor) {
        setVelocity(getVelocityX() * factor, getVelocityY() * factor);
    }
    
    /**
     * 패들과의 특수 충돌 처리
     */
    public void handlePaddleCollision(BreakoutPaddle paddle) {
        // Box의 handleCollision이 호출되어 기본 반사 처리
        // 패들의 handleCollision에서 각도 조정
        paddle.handleCollision(this);
    }
    
    @Override
    public void handleCollision(Collidable other) {
        if (!isFireBall) {
            // 일반 공은 반사
            super.handleCollision(other);
        }
        // 파이어볼은 관통 (반사하지 않음)
    }
    
    @Override
    public void draw(GraphicsContext gc) {
        super.draw(gc);
        
        // 파이어볼 효과
        if (isFireBall) {
            gc.setStroke(Color.ORANGE);
            gc.setLineWidth(2);
            gc.strokeOval(getX(), getY(), getWidth(), getHeight());
        }
    }
    
    public boolean isFireBall() { return isFireBall; }
    public boolean isSticky() { return isSticky; }
    public double getSpeed() {
        return Math.sqrt(getVelocityX() * getVelocityX() + getVelocityY() * getVelocityY());
    }
}
```

---

## 5. 벽돌 시스템 구현

### 5.1 UnbreakableBrick (게임 벽)

```java
package com.nhnacademy.breakout.objects;

import com.nhnacademy.game.entity.StaticObject;
import com.nhnacademy.game.collision.Collidable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

/**
 * 깨지지 않는 벽돌 (벽)
 * 게임 공간의 경계를 정의하는데 사용됩니다.
 */
public class UnbreakableBrick extends StaticObject {
    private static final Color WALL_COLOR = Color.DARKGRAY;
    
    public UnbreakableBrick(double x, double y, double width, double height) {
        super(x, y, width, height, WALL_COLOR);
        setFilled(true);
    }
    
    @Override
    public void handleCollision(Collidable other) {
        // 깨지지 않는 벽돌은 충돌해도 변화 없음
        // 공이 튕겨나가는 것은 공 쪽에서 처리
    }
    
    @Override
    public void draw(GraphicsContext gc) {
        // 그라데이션으로 벽돌 효과
        LinearGradient gradient = new LinearGradient(
            0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.LIGHTGRAY),
            new Stop(0.5, WALL_COLOR),
            new Stop(1, Color.DARKGRAY.darker())
        );
        
        gc.setFill(gradient);
        gc.fillRect(getX(), getY(), getWidth(), getHeight());
        
        // 테두리
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(getX(), getY(), getWidth(), getHeight());
        
        // 벽돌 패턴
        drawBrickPattern(gc);
    }
    
    /**
     * 벽돌 패턴을 그립니다.
     */
    private void drawBrickPattern(GraphicsContext gc) {
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(0.5);
        
        // 수평선
        double brickHeight = 10;
        for (double y = getY() + brickHeight; y < getY() + getHeight(); y += brickHeight) {
            gc.strokeLine(getX(), y, getX() + getWidth(), y);
        }
        
        // 수직선 (엇갈리게)
        double brickWidth = 20;
        boolean offset = false;
        for (double y = getY(); y < getY() + getHeight(); y += brickHeight) {
            double startX = offset ? getX() + brickWidth / 2 : getX();
            for (double x = startX; x < getX() + getWidth(); x += brickWidth) {
                gc.strokeLine(x, y, x, Math.min(y + brickHeight, getY() + getHeight()));
            }
            offset = !offset;
        }
    }
    
    /**
     * 게임 영역의 벽을 생성하는 정적 팩토리 메서드
     */
    public static class WallFactory {
        public static UnbreakableBrick createTopWall(double width, double thickness) {
            return new UnbreakableBrick(0, 0, width, thickness);
        }
        
        public static UnbreakableBrick createLeftWall(double height, double thickness) {
            return new UnbreakableBrick(0, 0, thickness, height);
        }
        
        public static UnbreakableBrick createRightWall(double x, double height, double thickness) {
            return new UnbreakableBrick(x - thickness, 0, thickness, height);
        }
        
        public static UnbreakableBrick createBottomWall(double y, double width, double thickness) {
            return new UnbreakableBrick(0, y - thickness, width, thickness);
        }
    }
}
```

### 5.2 SimpleBrick (기본 벽돌)

```java
package com.nhnacademy.breakout.objects;

import com.nhnacademy.game.entity.StaticObject;
import com.nhnacademy.game.behavior.Breakable;
import com.nhnacademy.game.collision.Collidable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 기본 벽돌
 * StaticObject를 확장하고 Breakable 인터페이스를 구현합니다.
 */
public class SimpleBrick extends StaticObject implements Breakable {
    private int hitPoints;
    private int maxHitPoints;
    private int points;
    private boolean isBroken;
    
    // 애니메이션
    private boolean isHit;
    private double hitTimer;
    
    public SimpleBrick(double x, double y, double width, double height, Color color, int points) {
        super(x, y, width, height, color);
        this.hitPoints = 1;
        this.maxHitPoints = 1;
        this.points = points;
        this.isBroken = false;
        this.isHit = false;
        this.hitTimer = 0;
        setFilled(true);
    }
    
    @Override
    public void hit(int damage) {
        if (!isBroken) {
            hitPoints -= damage;
            isHit = true;
            hitTimer = 0.2;
            
            if (hitPoints <= 0) {
                isBroken = true;
            }
        }
    }
    
    @Override
    public boolean isBroken() {
        return isBroken;
    }
    
    @Override
    public int getPoints() {
        return points;
    }
    
    @Override
    public void handleCollision(Collidable other) {
        if (other instanceof BreakoutBall) {
            hit(1);
        }
    }
    
    public void update(double deltaTime) {
        if (isHit && hitTimer > 0) {
            hitTimer -= deltaTime;
            if (hitTimer <= 0) {
                isHit = false;
            }
        }
    }
    
    @Override
    public void draw(GraphicsContext gc) {
        if (isBroken) return;
        
        // 타격 애니메이션
        double offsetX = 0, offsetY = 0;
        if (isHit) {
            offsetX = (Math.random() - 0.5) * 4;
            offsetY = (Math.random() - 0.5) * 4;
        }
        
        // 3D 효과
        gc.setFill(Color.BLACK.deriveColor(0, 1, 0.3, 0.3));
        gc.fillRect(getX() + offsetX + 2, getY() + offsetY + 2, getWidth(), getHeight());
        
        // 본체
        gc.setFill(getColor());
        gc.fillRect(getX() + offsetX, getY() + offsetY, getWidth() - 2, getHeight() - 2);
        
        // 하이라이트
        gc.setFill(getColor().brighter());
        gc.fillRect(getX() + offsetX + 2, getY() + offsetY + 2, getWidth() - 4, getHeight() / 3);
        
        // 테두리
        gc.setStroke(getColor().darker());
        gc.setLineWidth(2);
        gc.strokeRect(getX() + offsetX, getY() + offsetY, getWidth() - 2, getHeight() - 2);
    }
}
```

### 5.3 MultiHitBrick (다중 타격 벽돌)

```java
package com.nhnacademy.breakout.objects;

import com.nhnacademy.game.entity.StaticObject;
import com.nhnacademy.game.behavior.MultiHit;
import com.nhnacademy.game.collision.Collidable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 다중 타격 벽돌
 * StaticObject를 확장하고 MultiHit 인터페이스를 구현합니다.
 */
public class MultiHitBrick extends StaticObject implements MultiHit {
    private int hitPoints;
    private int maxHitPoints;
    private int points;
    private boolean isBroken;
    private DamageState damageState;
    
    public MultiHitBrick(double x, double y, double width, double height, 
                        Color color, int points, int maxHitPoints) {
        super(x, y, width, height, color);
        this.hitPoints = maxHitPoints;
        this.maxHitPoints = maxHitPoints;
        this.points = points;
        this.isBroken = false;
        this.damageState = DamageState.NONE;
        setFilled(true);
    }
    
    @Override
    public void hit(int damage) {
        if (!isBroken) {
            hitPoints -= damage;
            updateDamageState();
            
            if (hitPoints <= 0) {
                isBroken = true;
            }
        }
    }
    
    private void updateDamageState() {
        double ratio = (double) hitPoints / maxHitPoints;
        if (ratio > 0.66) {
            damageState = DamageState.LIGHT;
        } else if (ratio > 0.33) {
            damageState = DamageState.MODERATE;
        } else {
            damageState = DamageState.HEAVY;
        }
    }
    
    @Override
    public boolean isBroken() {
        return isBroken;
    }
    
    @Override
    public int getPoints() {
        return points;
    }
    
    @Override
    public int getHitPoints() {
        return hitPoints;
    }
    
    @Override
    public int getMaxHitPoints() {
        return maxHitPoints;
    }
    
    @Override
    public DamageState getDamageState() {
        return damageState;
    }
    
    @Override
    public void handleCollision(Collidable other) {
        if (other instanceof BreakoutBall) {
            hit(1);
        }
    }
    
    @Override
    public void draw(GraphicsContext gc) {
        if (isBroken) return;
        
        super.draw(gc);
        
        // 손상 표시
        if (damageState != DamageState.NONE) {
            drawCracks(gc, damageState);
        }
        
        // 남은 체력 표시
        drawHitPointsIndicator(gc);
    }
    
    private void drawCracks(GraphicsContext gc, DamageState state) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        
        int cracks = state == DamageState.LIGHT ? 1 : 
                    state == DamageState.MODERATE ? 2 : 3;
        
        for (int i = 0; i < cracks; i++) {
            double startX = getX() + Math.random() * getWidth();
            double startY = getY() + Math.random() * getHeight();
            double endX = getX() + Math.random() * getWidth();
            double endY = getY() + Math.random() * getHeight();
            gc.strokeLine(startX, startY, endX, endY);
        }
    }
    
    private void drawHitPointsIndicator(GraphicsContext gc) {
        // 중앙에 숫자로 표시
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font(14));
        String text = String.valueOf(hitPoints);
        double textWidth = text.length() * 7; // 대략적인 텍스트 너비
        gc.fillText(text, getCenterX() - textWidth/2, getCenterY() + 5);
    }
}
```

### 5.4 ExplodingBrick (폭발 벽돌)

```java
package com.nhnacademy.breakout.objects;

import com.nhnacademy.game.entity.StaticObject;
import com.nhnacademy.game.behavior.Exploding;
import com.nhnacademy.game.collision.Bounds;
import com.nhnacademy.game.collision.CircleBounds;
import com.nhnacademy.game.collision.Collidable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.List;
import java.util.ArrayList;

/**
 * 폭발하는 벽돌
 * StaticObject를 확장하고 Exploding 인터페이스를 구현합니다.
 */
public class ExplodingBrick extends StaticObject implements Exploding {
    private int hitPoints;
    private int points;
    private boolean isBroken;
    private double explosionRadius;
    private int explosionDamage;
    
    public ExplodingBrick(double x, double y, double width, double height, 
                         Color color, int points) {
        super(x, y, width, height, color);
        this.hitPoints = 1;
        this.points = points;
        this.isBroken = false;
        this.explosionRadius = 100;
        this.explosionDamage = 2;
        setFilled(true);
    }
    
    @Override
    public void hit(int damage) {
        if (!isBroken) {
            hitPoints -= damage;
            if (hitPoints <= 0) {
                isBroken = true;
            }
        }
    }
    
    @Override
    public boolean isBroken() {
        return isBroken;
    }
    
    @Override
    public int getPoints() {
        return points;
    }
    
    @Override
    public List<ExplosionEffect> explode() {
        List<ExplosionEffect> effects = new ArrayList<>();
        if (isBroken) {
            effects.add(new ExplosionEffect(getCenterX(), getCenterY(), explosionRadius));
        }
        return effects;
    }
    
    @Override
    public Bounds getExplosionBounds() {
        return new CircleBounds(getCenterX(), getCenterY(), explosionRadius);
    }
    
    @Override
    public int getExplosionDamage() {
        return explosionDamage;
    }
    
    @Override
    public void handleCollision(Collidable other) {
        if (other instanceof BreakoutBall) {
            hit(1);
        }
    }
    
    @Override
    public void draw(GraphicsContext gc) {
        if (isBroken) return;
        
        super.draw(gc);
        
        // 폭발 아이콘
        gc.setFill(Color.YELLOW);
        gc.setFont(javafx.scene.text.Font.font(16));
        gc.fillText("💥", getCenterX() - 8, getCenterY() + 6);
    }
}
```

### 5.5 PowerUpBrick (파워업 벽돌)

```java
package com.nhnacademy.breakout.objects;

import com.nhnacademy.game.behavior.PowerUpProvider;
import com.nhnacademy.game.collision.Collidable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.Random;

/**
 * 파워업을 제공하는 벽돌
 * SimpleBrick을 확장하고 PowerUpProvider 인터페이스를 구현합니다.
 */
public class PowerUpBrick extends SimpleBrick implements PowerUpProvider {
    private double powerUpChance;
    private PowerUpType specificType;
    private static final Random random = new Random();
    
    public PowerUpBrick(double x, double y, double width, double height, 
                       Color color, int points, double powerUpChance) {
        super(x, y, width, height, color, points);
        this.powerUpChance = powerUpChance;
        this.specificType = null; // 랜덤 타입
    }
    
    public PowerUpBrick(double x, double y, double width, double height, 
                       Color color, int points, PowerUpType type) {
        super(x, y, width, height, color, points);
        this.powerUpChance = 1.0; // 특정 타입은 100% 드롭
        this.specificType = type;
    }
    
    @Override
    public double getPowerUpChance() {
        return powerUpChance;
    }
    
    @Override
    public boolean shouldDropPowerUp() {
        return isBroken() && random.nextDouble() < powerUpChance;
    }
    
    @Override
    public PowerUpType getPowerUpType() {
        if (specificType != null) {
            return specificType;
        }
        
        // 랜덤 타입 선택
        PowerUpType[] types = PowerUpType.values();
        return types[random.nextInt(types.length)];
    }
    
    @Override
    public void draw(GraphicsContext gc) {
        super.draw(gc);
        
        // 파워업 벽돌 표시 (물음표)
        if (!isBroken()) {
            gc.setFill(Color.YELLOW);
            gc.setFont(javafx.scene.text.Font.font(16));
            gc.fillText("?", getCenterX() - 5, getCenterY() + 5);
        }
    }
}
```

---

## 6. PowerUp 시스템

Ball 클래스를 확장하여 떨어지는 파워업을 구현합니다.

```java
package com.nhnacademy.breakout.world;

import com.nhnacademy.game.entity.Ball;
import com.nhnacademy.game.behavior.PowerUpProvider;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 파워업 아이템
 * Ball을 상속받아 떨어지는 파워업을 구현합니다.
 */
public class PowerUp extends Ball {
    private PowerUpProvider.PowerUpType type;
    private static final double RADIUS = 15;
    private static final double FALL_SPEED = 100;
    
    public PowerUp(double x, double y, PowerUpProvider.PowerUpType type) {
        super(x, y, RADIUS, getColorForType(type));
        this.type = type;
        setVelocity(0, FALL_SPEED);
    }
    
    /**
     * 파워업 타입에 따른 색상을 반환합니다.
     */
    private static Color getColorForType(PowerUpProvider.PowerUpType type) {
        switch (type) {
            case WIDER_PADDLE:
                return Color.BLUE;
            case MULTI_BALL:
                return Color.ORANGE;
            case EXTRA_LIFE:
                return Color.GREEN;
            case LASER:
                return Color.RED;
            case SLOW_BALL:
                return Color.CYAN;
            case STICKY_PADDLE:
                return Color.PURPLE;
            default:
                return Color.YELLOW;
        }
    }
    
    @Override
    public void draw(GraphicsContext gc) {
        super.draw(gc);
        
        // 파워업 아이콘
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font(12));
        String icon = getIconForType(type);
        gc.fillText(icon, getCenterX() - 8, getCenterY() + 4);
        
        // 테두리
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeOval(getX(), getY(), getWidth(), getHeight());
    }
    
    /**
     * 파워업 타입에 따른 아이콘을 반환합니다.
     */
    private String getIconForType(PowerUpProvider.PowerUpType type) {
        switch (type) {
            case WIDER_PADDLE:
                return "W";
            case MULTI_BALL:
                return "M";
            case EXTRA_LIFE:
                return "+1";
            case LASER:
                return "L";
            case SLOW_BALL:
                return "S";
            case STICKY_PADDLE:
                return "G";
            default:
                return "?";
        }
    }
    
    public PowerUpProvider.PowerUpType getType() {
        return type;
    }
}
```

---

## 7. BreakoutWorld

게임 월드를 관리하는 클래스입니다.

```java
package com.nhnacademy.breakout.world;

import com.nhnacademy.breakout.objects.*;
import com.nhnacademy.game.behavior.*;
import com.nhnacademy.game.collision.Bounds;
import com.nhnacademy.game.collision.Collidable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.*;

/**
 * Breakout 게임 월드
 * 2~7장에서 배운 개념을 활용하여 게임 세계를 관리합니다.
 */
public class BreakoutWorld {
    private double width;
    private double height;
    private static final double WALL_THICKNESS = 20;
    
    // 게임 객체들
    private List<UnbreakableBrick> walls;
    private List<Breakable> bricks;
    private List<BreakoutBall> balls;
    private BreakoutPaddle paddle;
    private List<PowerUp> powerUps;
    private List<Exploding.ExplosionEffect> explosions;
    
    // 게임 상태
    private int score = 0;
    private int lives = 3;
    private int level = 1;
    
    public BreakoutWorld(double width, double height) {
        this.width = width;
        this.height = height;
        this.walls = new ArrayList<>();
        this.bricks = new ArrayList<>();
        this.balls = new ArrayList<>();
        this.powerUps = new ArrayList<>();
        this.explosions = new ArrayList<>();
        
        initializeWalls();
        initializePaddle();
        initializeBall();
    }
    
    /**
     * 벽을 초기화합니다.
     * 상, 좌, 우 벽은 깨지지 않는 벽돌로 만듭니다.
     */
    private void initializeWalls() {
        // 상단 벽
        walls.add(UnbreakableBrick.WallFactory.createTopWall(width, WALL_THICKNESS));
        
        // 좌측 벽
        walls.add(UnbreakableBrick.WallFactory.createLeftWall(height, WALL_THICKNESS));
        
        // 우측 벽
        walls.add(UnbreakableBrick.WallFactory.createRightWall(width, height, WALL_THICKNESS));
    }
    
    /**
     * 패들을 초기화합니다.
     */
    private void initializePaddle() {
        double paddleX = (width - 100) / 2;
        double paddleY = height - 60;
        paddle = new BreakoutPaddle(paddleX, paddleY);
    }
    
    /**
     * 공을 초기화합니다.
     */
    private void initializeBall() {
        balls.clear();
        BreakoutBall ball = new BreakoutBall(width / 2, height - 80);
        ball.setVelocity(150, -150);
        balls.add(ball);
    }
    
    /**
     * 레벨에 따른 벽돌을 생성합니다.
     */
    public void createLevel(int level) {
        bricks.clear();
        this.level = level;
        
        // 레벨에 따른 벽돌 배치
        double brickWidth = 60;
        double brickHeight = 20;
        double startX = WALL_THICKNESS + 20;
        double startY = WALL_THICKNESS + 40;
        
        int rows = Math.min(5 + level, 10);
        int cols = (int)((width - 2 * WALL_THICKNESS - 40) / (brickWidth + 5));
        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double x = startX + col * (brickWidth + 5);
                double y = startY + row * (brickHeight + 5);
                
                // 레벨에 따른 벽돌 타입 결정
                Breakable brick = createBrickForLevel(x, y, brickWidth, brickHeight, row, col, level);
                if (brick != null) {
                    bricks.add(brick);
                }
            }
        }
    }
    
    /**
     * 레벨과 위치에 따른 벽돌을 생성합니다.
     */
    private Breakable createBrickForLevel(double x, double y, double width, double height, 
                                         int row, int col, int level) {
        Color color = Color.hsb(row * 40, 0.8, 0.9);
        int points = (5 - row) * 10 * level;
        
        // 레벨에 따른 특수 벽돌 배치
        if (level >= 3 && (row + col) % 7 == 0) {
            // 폭발 벽돌
            return new ExplodingBrick(x, y, width, height, Color.ORANGE, points * 2);
        } else if (level >= 2 && row < 2) {
            // 다중 히트 벽돌
            return new MultiHitBrick(x, y, width, height, color, points, 2 + level / 3);
        } else if ((row + col) % 5 == 0) {
            // 파워업 벽돌
            return new PowerUpBrick(x, y, width, height, color, points, 0.3);
        } else {
            // 일반 벽돌
            return new SimpleBrick(x, y, width, height, color, points);
        }
    }
    
    /**
     * 월드를 업데이트합니다.
     */
    public void update(double deltaTime) {
        // 패들 업데이트
        paddle.updatePowerUps(deltaTime);
        
        // 공 업데이트
        updateBalls(deltaTime);
        
        // 파워업 업데이트
        updatePowerUps(deltaTime);
        
        // 폭발 효과 업데이트
        updateExplosions(deltaTime);
        
        // 충돌 처리
        handleCollisions();
        
        // 게임 상태 확인
        checkGameState();
    }
    
    /**
     * 공들을 업데이트합니다.
     */
    private void updateBalls(double deltaTime) {
        List<BreakoutBall> toRemove = new ArrayList<>();
        
        for (BreakoutBall ball : balls) {
            if (!ball.isSticky()) {
                ball.update(deltaTime);
                
                // 하단 경계 확인 (공을 놓친 경우)
                if (ball.getCenterY() > height) {
                    toRemove.add(ball);
                }
            } else {
                // 끈끈한 공은 패들을 따라 이동
                ball.setPosition(paddle.getCenterX(), paddle.getY() - ball.getRadius());
            }
        }
        
        balls.removeAll(toRemove);
        
        // 모든 공을 놓친 경우
        if (balls.isEmpty()) {
            lives--;
            if (lives > 0) {
                initializeBall();
            }
        }
    }
    
    /**
     * 파워업을 업데이트합니다.
     */
    private void updatePowerUps(double deltaTime) {
        List<PowerUp> toRemove = new ArrayList<>();
        
        for (PowerUp powerUp : powerUps) {
            powerUp.update(deltaTime);
            
            // 화면 밖으로 나간 파워업 제거
            if (powerUp.getY() > height) {
                toRemove.add(powerUp);
            }
        }
        
        powerUps.removeAll(toRemove);
    }
    
    /**
     * 폭발 효과를 업데이트합니다.
     */
    private void updateExplosions(double deltaTime) {
        List<Exploding.ExplosionEffect> toRemove = new ArrayList<>();
        
        for (Exploding.ExplosionEffect explosion : explosions) {
            explosion.update(deltaTime);
            if (explosion.isFinished()) {
                toRemove.add(explosion);
            }
        }
        
        explosions.removeAll(toRemove);
    }
    
    /**
     * 충돌을 처리합니다.
     */
    private void handleCollisions() {
        // 공과 벽 충돌
        for (BreakoutBall ball : balls) {
            for (UnbreakableBrick wall : walls) {
                if (ball.collidesWith(wall)) {
                    ball.handleCollision(wall);
                }
            }
        }
        
        // 공과 패들 충돌
        for (BreakoutBall ball : balls) {
            if (ball.collidesWith(paddle)) {
                ball.handlePaddleCollision(paddle);
            }
        }
        
        // 공과 벽돌 충돌
        List<Breakable> brokenBricks = new ArrayList<>();
        for (BreakoutBall ball : balls) {
            for (Breakable brick : bricks) {
                if (brick instanceof Collidable) {
                    Collidable collidableBrick = (Collidable) brick;
                    if (ball.collidesWith(collidableBrick)) {
                        ball.handleCollision(collidableBrick);
                        collidableBrick.handleCollision(ball);
                        
                        if (brick.isBroken()) {
                            brokenBricks.add(brick);
                            score += brick.getPoints();
                            
                            // 파워업 생성
                            if (brick instanceof PowerUpProvider) {
                                PowerUpProvider provider = (PowerUpProvider) brick;
                                if (provider.shouldDropPowerUp()) {
                                    createPowerUp(collidableBrick, provider.getPowerUpType());
                                }
                            }
                            
                            // 폭발 처리
                            if (brick instanceof Exploding) {
                                handleExplosion((Exploding) brick);
                            }
                        }
                        break; // 한 프레임에 하나의 벽돌만 충돌
                    }
                }
            }
        }
        bricks.removeAll(brokenBricks);
        
        // 패들과 파워업 충돌
        List<PowerUp> collectedPowerUps = new ArrayList<>();
        for (PowerUp powerUp : powerUps) {
            if (powerUp.collidesWith(paddle)) {
                applyPowerUp(powerUp);
                collectedPowerUps.add(powerUp);
            }
        }
        powerUps.removeAll(collectedPowerUps);
    }
    
    /**
     * 폭발을 처리합니다.
     */
    private void handleExplosion(Exploding explodingBrick) {
        explosions.addAll(explodingBrick.explode());
        Bounds explosionBounds = explodingBrick.getExplosionBounds();
        
        // 폭발 범위 내의 벽돌에 피해
        List<Breakable> affectedBricks = new ArrayList<>();
        for (Breakable brick : bricks) {
            if (brick instanceof Collidable) {
                Collidable collidable = (Collidable) brick;
                if (explosionBounds.intersects(collidable.getBounds())) {
                    brick.hit(explodingBrick.getExplosionDamage());
                    if (brick.isBroken()) {
                        affectedBricks.add(brick);
                        score += brick.getPoints();
                    }
                }
            }
        }
        bricks.removeAll(affectedBricks);
    }
    
    /**
     * 파워업을 생성합니다.
     */
    private void createPowerUp(Collidable brick, PowerUpProvider.PowerUpType type) {
        if (brick instanceof StaticObject) {
            StaticObject obj = (StaticObject) brick;
            PowerUp powerUp = new PowerUp(obj.getCenterX(), obj.getCenterY(), type);
            powerUps.add(powerUp);
        }
    }
    
    /**
     * 파워업을 적용합니다.
     */
    private void applyPowerUp(PowerUp powerUp) {
        switch (powerUp.getType()) {
            case WIDER_PADDLE:
                paddle.applyPowerUp(BreakoutPaddle.PowerUpType.WIDER_PADDLE, 
                                  powerUp.getType().getDuration());
                break;
            case STICKY_PADDLE:
                paddle.applyPowerUp(BreakoutPaddle.PowerUpType.STICKY_PADDLE, 
                                  powerUp.getType().getDuration());
                break;
            case LASER:
                paddle.applyPowerUp(BreakoutPaddle.PowerUpType.LASER, 
                                  powerUp.getType().getDuration());
                break;
            case MULTI_BALL:
                createMultiBalls();
                break;
            case SLOW_BALL:
                for (BreakoutBall ball : balls) {
                    ball.adjustSpeed(0.5);
                }
                break;
            case EXTRA_LIFE:
                lives++;
                break;
        }
    }
    
    /**
     * 멀티볼을 생성합니다.
     */
    private void createMultiBalls() {
        if (!balls.isEmpty()) {
            BreakoutBall originalBall = balls.get(0);
            for (int i = 0; i < 2; i++) {
                BreakoutBall newBall = new BreakoutBall(
                    originalBall.getCenterX(), 
                    originalBall.getCenterY()
                );
                double angle = (i + 1) * Math.PI / 6;
                double speed = 200;
                newBall.setVelocity(
                    Math.cos(angle) * speed,
                    -Math.sin(angle) * speed
                );
                balls.add(newBall);
            }
        }
    }
    
    /**
     * 게임 상태를 확인합니다.
     */
    private void checkGameState() {
        // 모든 벽돌이 깨진 경우
        if (bricks.isEmpty()) {
            // 다음 레벨로
            level++;
            createLevel(level);
            initializeBall();
        }
    }
    
    /**
     * 월드를 렌더링합니다.
     */
    public void render(GraphicsContext gc) {
        // 배경
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, width, height);
        
        // 벽
        for (UnbreakableBrick wall : walls) {
            wall.draw(gc);
        }
        
        // 벽돌
        for (Breakable brick : bricks) {
            if (brick instanceof StaticObject) {
                ((StaticObject) brick).draw(gc);
            }
        }
        
        // 패들
        paddle.draw(gc);
        
        // 공
        for (BreakoutBall ball : balls) {
            ball.draw(gc);
        }
        
        // 파워업
        for (PowerUp powerUp : powerUps) {
            powerUp.draw(gc);
        }
        
        // 폭발 효과
        for (Exploding.ExplosionEffect explosion : explosions) {
            renderExplosion(gc, explosion);
        }
        
        // UI
        renderUI(gc);
    }
    
    /**
     * 폭발 효과를 렌더링합니다.
     */
    private void renderExplosion(GraphicsContext gc, Exploding.ExplosionEffect explosion) {
        double opacity = 1.0 - explosion.getProgress();
        gc.setGlobalAlpha(opacity);
        gc.setFill(Color.ORANGE);
        gc.fillOval(
            explosion.getX() - explosion.getCurrentRadius(),
            explosion.getY() - explosion.getCurrentRadius(),
            explosion.getCurrentRadius() * 2,
            explosion.getCurrentRadius() * 2
        );
        gc.setGlobalAlpha(1.0);
    }
    
    /**
     * UI를 렌더링합니다.
     */
    private void renderUI(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font(20));
        gc.fillText("Score: " + score, WALL_THICKNESS + 10, WALL_THICKNESS + 20);
        gc.fillText("Lives: " + lives, width / 2 - 40, WALL_THICKNESS + 20);
        gc.fillText("Level: " + level, width - 100, WALL_THICKNESS + 20);
    }
    
    // 입력 처리
    public void movePaddleLeft(double deltaTime) {
        paddle.moveLeft(deltaTime);
        paddle.constrainToBounds(WALL_THICKNESS, width - WALL_THICKNESS);
    }
    
    public void movePaddleRight(double deltaTime) {
        paddle.moveRight(deltaTime);
        paddle.constrainToBounds(WALL_THICKNESS, width - WALL_THICKNESS);
    }
    
    public void launchBall() {
        for (BreakoutBall ball : balls) {
            if (ball.isSticky()) {
                ball.launch();
            }
        }
    }
    
    // Getters
    public int getScore() { return score; }
    public int getLives() { return lives; }
    public int getLevel() { return level; }
    public boolean isGameOver() { return lives <= 0; }
    public boolean hasWon() { return level > 10; }
}
```

---

## 8. BreakoutGame 메인 클래스

```java
package com.nhnacademy.breakout;

import com.nhnacademy.breakout.world.BreakoutWorld;
import javafx.application.Application;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Breakout 게임의 메인 클래스
 * 2~7장의 개념을 활용한 완전한 게임 구현
 */
public class BreakoutGame extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    
    private BreakoutWorld world;
    private GraphicsContext gc;
    private AnimationTimer gameLoop;
    private long lastUpdate;
    
    // 키 입력 상태
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    
    @Override
    public void start(Stage stage) {
        stage.setTitle("Breakout Game - Chapter 8");
        
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
        
        BorderPane root = new BorderPane(canvas);
        Scene scene = new Scene(root);
        
        // 월드 초기화
        world = new BreakoutWorld(WIDTH, HEIGHT);
        world.createLevel(1);
        
        // 키 입력 처리
        scene.setOnKeyPressed(e -> handleKeyPress(e.getCode()));
        scene.setOnKeyReleased(e -> handleKeyRelease(e.getCode()));
        
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        
        // 게임 루프
        lastUpdate = System.nanoTime();
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;
                
                update(deltaTime);
                render();
            }
        };
        gameLoop.start();
    }
    
    private void handleKeyPress(KeyCode code) {
        switch (code) {
            case LEFT:
            case A:
                leftPressed = true;
                break;
            case RIGHT:
            case D:
                rightPressed = true;
                break;
            case SPACE:
                world.launchBall();
                break;
        }
    }
    
    private void handleKeyRelease(KeyCode code) {
        switch (code) {
            case LEFT:
            case A:
                leftPressed = false;
                break;
            case RIGHT:
            case D:
                rightPressed = false;
                break;
        }
    }
    
    private void update(double deltaTime) {
        // 패들 이동
        if (leftPressed) {
            world.movePaddleLeft(deltaTime);
        }
        if (rightPressed) {
            world.movePaddleRight(deltaTime);
        }
        
        // 월드 업데이트
        world.update(deltaTime);
        
        // 게임 오버 체크
        if (world.isGameOver()) {
            gameLoop.stop();
            showGameOver();
        } else if (world.hasWon()) {
            gameLoop.stop();
            showVictory();
        }
    }
    
    private void render() {
        world.render(gc);
    }
    
    private void showGameOver() {
        gc.setFill(javafx.scene.paint.Color.RED);
        gc.setFont(javafx.scene.text.Font.font(48));
        gc.fillText("GAME OVER", WIDTH/2 - 150, HEIGHT/2);
        gc.setFont(javafx.scene.text.Font.font(24));
        gc.fillText("Final Score: " + world.getScore(), WIDTH/2 - 80, HEIGHT/2 + 40);
    }
    
    private void showVictory() {
        gc.setFill(javafx.scene.paint.Color.GREEN);
        gc.setFont(javafx.scene.text.Font.font(48));
        gc.fillText("YOU WIN!", WIDTH/2 - 120, HEIGHT/2);
        gc.setFont(javafx.scene.text.Font.font(24));
        gc.fillText("Final Score: " + world.getScore(), WIDTH/2 - 80, HEIGHT/2 + 40);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
```

---

## 9. 테스트 코드

```java
package com.nhnacademy.breakout;

import com.nhnacademy.breakout.objects.*;
import com.nhnacademy.breakout.world.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class BreakoutGameTest {
    
    private BreakoutWorld world;
    
    @BeforeEach
    void setup() {
        world = new BreakoutWorld(800, 600);
    }
    
    @Test
    void testPaddleMovement() {
        BreakoutPaddle paddle = new BreakoutPaddle(400, 550);
        double initialX = paddle.getX();
        
        paddle.moveLeft(0.1);
        assertTrue(paddle.getX() < initialX);
        
        paddle.moveRight(0.2);
        assertTrue(paddle.getX() > initialX);
    }
    
    @Test
    void testBallCollisionWithPaddle() {
        BreakoutPaddle paddle = new BreakoutPaddle(400, 550);
        BreakoutBall ball = new BreakoutBall(400, 540);
        ball.setVelocity(0, 100);
        
        assertTrue(ball.collidesWith(paddle));
        ball.handlePaddleCollision(paddle);
        
        // 공이 위로 튕겨나가야 함
        assertTrue(ball.getVelocityY() < 0);
    }
    
    @Test
    void testBrickDestruction() {
        SimpleBrick brick = new SimpleBrick(100, 100, 60, 20, 
                                          javafx.scene.paint.Color.RED, 10);
        assertFalse(brick.isBroken());
        
        brick.hit(1);
        assertTrue(brick.isBroken());
        assertEquals(10, brick.getPoints());
    }
    
    @Test
    void testMultiHitBrick() {
        MultiHitBrick brick = new MultiHitBrick(100, 100, 60, 20, 
                                               javafx.scene.paint.Color.BLUE, 20, 3);
        
        assertFalse(brick.isBroken());
        assertEquals(3, brick.getHitPoints());
        
        brick.hit(1);
        assertFalse(brick.isBroken());
        assertEquals(2, brick.getHitPoints());
        
        brick.hit(2);
        assertTrue(brick.isBroken());
    }
    
    @Test
    void testPowerUpCollection() {
        BreakoutPaddle paddle = new BreakoutPaddle(400, 550);
        double originalWidth = paddle.getWidth();
        
        paddle.applyPowerUp(BreakoutPaddle.PowerUpType.WIDER_PADDLE, 5.0);
        assertTrue(paddle.getWidth() > originalWidth);
    }
    
    @Test
    void testFireBallMode() {
        BreakoutBall ball = new BreakoutBall(400, 300);
        ball.setFireBall(true);
        
        assertTrue(ball.isFireBall());
        assertEquals(javafx.scene.paint.Color.ORANGERED, ball.getColor());
    }
    
    @Test
    void testExplodingBrick() {
        ExplodingBrick brick = new ExplodingBrick(200, 200, 60, 20, 
                                                  javafx.scene.paint.Color.ORANGE, 50);
        
        brick.hit(1);
        assertTrue(brick.isBroken());
        
        var effects = brick.explode();
        assertFalse(effects.isEmpty());
    }
    
    @Test
    void testWorldInitialization() {
        world.createLevel(1);
        
        assertEquals(1, world.getLevel());
        assertEquals(3, world.getLives());
        assertEquals(0, world.getScore());
        assertFalse(world.isGameOver());
    }
}
```

---

## 10. 실행 가이드

### Maven 설정 (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>com.nhnacademy</groupId>
        <artifactId>cannongame</artifactId>
        <version>1.0-SNAPSHOT</version>
        <relativePath>../../pom.xml</relativePath>
    </parent>
    
    <artifactId>chapter08-breakout</artifactId>
    <name>Chapter 08: Breakout Game</name>
    
    <dependencies>
        <!-- Common 모듈 (2~7장의 클래스들) -->
        <dependency>
            <groupId>com.nhnacademy</groupId>
            <artifactId>common</artifactId>
            <version>${project.version}</version>
        </dependency>
        
        <!-- JavaFX -->
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
        </dependency>
        
        <!-- JUnit 5 -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <configuration>
                    <mainClass>com.nhnacademy.breakout.BreakoutGame</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 실행 방법

```bash
# 프로젝트 빌드
mvn clean install

# 게임 실행
cd src/chapter08_breakout
mvn javafx:run
```

### 게임 조작법

- **←/→** 또는 **A/D**: 패들 이동
- **스페이스바**: 공 발사
- **ESC**: 게임 종료

### 게임 특징

1. **상속 활용**
   - Box → BreakoutPaddle
   - Ball → BreakoutBall, PowerUp
   - StaticObject → 모든 벽돌 클래스

2. **인터페이스 활용**
   - Breakable: 파괴 가능한 벽돌
   - MultiHit: 다중 타격 벽돌
   - Exploding: 폭발 벽돌
   - PowerUpProvider: 파워업 제공

3. **기존 기능 재사용**
   - Ball의 물리 엔진
   - Box의 충돌 감지
   - StaticObject의 렌더링

---

## 요약

이 구현은 2~7장에서 배운 클래스들을 최대한 활용하여 Breakout 게임을 만들었습니다:

1. **코드 재사용**: 기존 클래스의 기능을 상속받아 중복 구현 최소화
2. **인터페이스 설계**: 특수 기능을 인터페이스로 정의하여 유연한 구조
3. **확장성**: 새로운 벽돌 타입이나 파워업을 쉽게 추가 가능
4. **통합 관리**: 인터페이스를 통한 일관된 객체 관리

이를 통해 "상속, 인터페이스 등의 특징을 활용하여 최대한 재활용하고 통합 관리"하는 목적을 달성했습니다.