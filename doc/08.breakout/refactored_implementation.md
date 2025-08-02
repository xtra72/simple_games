# 8장: Breakout Game - 리팩토링된 구현 가이드

이 구현은 2-7장에서 정의된 클래스들을 최대한 재활용하여 작성되었습니다.

## 목차
1. [리팩토링 개요](#1-리팩토링-개요)
2. [BreakoutPaddle 클래스 구현](#2-breakoutpaddle-클래스-구현)
3. [BreakoutBall 클래스 구현](#3-breakoutball-클래스-구현)
4. [Brick 클래스 구현](#4-brick-클래스-구현)
5. [PowerUp 시스템](#5-powerup-시스템)
6. [BreakoutGameWorld](#6-breakoutgameworld)
7. [BreakoutGameApp](#7-breakoutgameapp)

---

## 1. 리팩토링 개요

### 재활용되는 클래스들
- **Box 클래스** (com.nhnacademy.game.entity.Box): BreakoutPaddle의 부모 클래스
- **Ball 클래스** (com.nhnacademy.game.entity.Ball): BreakoutBall과 PowerUp의 부모 클래스
- **StaticObject 클래스** (com.nhnacademy.game.entity.StaticObject): 모든 Brick 클래스의 부모 클래스
- **GameObject 클래스** (com.nhnacademy.game.core.GameObject): 모든 게임 객체의 기본 클래스

### 재활용되는 인터페이스들
- **Movable** (com.nhnacademy.game.movement.Movable): 이동 가능한 객체
- **Collidable** (com.nhnacademy.game.collision.Collidable): 충돌 처리
- **Boundable** (com.nhnacademy.game.collision.Boundable): 경계 검사
- **Renderable** (com.nhnacademy.game.graphics.Renderable): 렌더링 (GameObject에 포함됨)
- **Breakable** (com.nhnacademy.game.behavior.Breakable): 파괴 가능한 벽돌
- **MultiHit** (com.nhnacademy.game.behavior.MultiHit): 다중 타격 벽돌
- **Exploding** (com.nhnacademy.game.behavior.Exploding): 폭발하는 벽돌
- **PowerUpProvider** (com.nhnacademy.game.behavior.PowerUpProvider): 파워업 제공

---

## 2. BreakoutPaddle 클래스 구현

Box 클래스를 확장하여 플레이어가 조작하는 패들을 구현합니다.

```java
package com.nhnacademy.breakout;

import com.nhnacademy.game.entity.Box;
import com.nhnacademy.game.collision.Collidable;
import com.nhnacademy.game.collision.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.input.KeyCode;
import java.util.Set;
import java.util.HashSet;

/**
 * Breakout 게임의 패들 클래스
 * Box 클래스를 확장하여 구현
 */
public class BreakoutPaddle extends Box {
    private Set<KeyCode> pressedKeys;
    private double speed;
    private double screenWidth;
    
    public BreakoutPaddle(double x, double y, double width, double height, double screenWidth) {
        super(x, y, width, height, Color.DARKBLUE);
        this.pressedKeys = new HashSet<>();
        this.speed = 300; // 픽셀/초
        this.screenWidth = screenWidth;
        setFilled(true);
    }
    
    @Override
    public void draw(GraphicsContext gc) {
        // 패들 몸체
        super.draw(gc);
        
        // 하이라이트 효과
        gc.setFill(getColor().brighter());
        gc.fillRoundRect(x + 2, y + 2, width - 4, height/3, 5, 5);
        
        // 테두리
        gc.setStroke(getColor().darker());
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, width, height, 10, 10);
    }
    
    @Override
    public void update(double deltaTime) {
        // 키보드 입력에 따른 이동
        if (pressedKeys.contains(KeyCode.LEFT) || pressedKeys.contains(KeyCode.A)) {
            moveLeft(deltaTime);
        }
        if (pressedKeys.contains(KeyCode.RIGHT) || pressedKeys.contains(KeyCode.D)) {
            moveRight(deltaTime);
        }
    }
    
    @Override
    public void handleCollision(Collidable other) {
        if (other instanceof BreakoutBall) {
            BreakoutBall ball = (BreakoutBall) other;
            
            // 공의 위치에 따른 반사 각도 조정
            double centerX = getCenterX();
            double relativePosition = (ball.getCenterX() - centerX) / (width / 2);
            relativePosition = Math.max(-1, Math.min(1, relativePosition));
            
            // 반사 각도 계산 (최대 60도)
            double angle = relativePosition * Math.PI / 3;
            double speed = Math.sqrt(Math.pow(ball.getVelocityX(), 2) + 
                                   Math.pow(ball.getVelocityY(), 2));
            
            // 새로운 속도 설정
            ball.setVelocity(speed * Math.sin(angle), 
                           -Math.abs(speed * Math.cos(angle))); // 항상 위로
            
            // 공을 패들 위로 이동 (중복 충돌 방지)
            ball.setY(y - ball.getRadius() * 2);
        }
    }
    
    private void moveLeft(double deltaTime) {
        double newX = x - speed * deltaTime;
        // 화면 경계 체크
        if (newX >= 0) {
            x = newX;
        } else {
            x = 0;
        }
    }
    
    private void moveRight(double deltaTime) {
        double newX = x + speed * deltaTime;
        // 화면 경계 체크
        if (newX + width <= screenWidth) {
            x = newX;
        } else {
            x = screenWidth - width;
        }
    }
    
    public void keyPressed(KeyCode code) {
        pressedKeys.add(code);
    }
    
    public void keyReleased(KeyCode code) {
        pressedKeys.remove(code);
    }
    
    public void reset() {
        x = (screenWidth - width) / 2;
        pressedKeys.clear();
    }
    
    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }
}
```

---

## 3. BreakoutBall 클래스 구현

Ball 클래스를 확장하여 게임의 공을 구현합니다.

```java
package com.nhnacademy.breakout;

import com.nhnacademy.game.entity.Ball;
import com.nhnacademy.game.collision.Collidable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;

/**
 * Breakout 게임의 공 클래스
 * Ball 클래스를 확장하여 구현
 */
public class BreakoutBall extends Ball {
    private boolean fireBall; // 파이어볼 모드
    
    public BreakoutBall(double x, double y, double radius) {
        super(x, y, radius, Color.WHITE);
        this.fireBall = false;
    }
    
    @Override
    public void draw(GraphicsContext gc) {
        // 그림자 효과
        gc.setEffect(new DropShadow(5, 2, 2, Color.rgb(0, 0, 0, 0.5)));
        
        if (fireBall) {
            // 파이어볼 효과
            gc.setFill(Color.ORANGERED);
            gc.fillOval(x - 2, y - 2, width + 4, height + 4);
        }
        
        // 기본 공 그리기
        super.draw(gc);
        
        // 하이라이트 효과
        gc.setFill(getColor().brighter());
        gc.fillOval(x + width/4, y + height/4, width/3, height/3);
        
        gc.setEffect(null);
    }
    
    @Override
    public void handleCollision(Collidable other) {
        if (!fireBall) {
            // 일반 모드에서는 기본 충돌 처리
            super.handleCollision(other);
        }
        // 파이어볼 모드에서는 관통 (속도 변경 없음)
    }
    
    public boolean isFireBall() { return fireBall; }
    public void setFireBall(boolean fireBall) { 
        this.fireBall = fireBall;
        setColor(fireBall ? Color.ORANGERED : Color.WHITE);
    }
}
```

---

## 4. Brick 클래스 구현

StaticObject를 확장하고 인터페이스를 구현하여 다양한 벽돌을 만듭니다.

### 4.1 기본 SimpleBrick 클래스

```java
package com.nhnacademy.breakout.bricks;

import com.nhnacademy.game.entity.StaticObject;
import com.nhnacademy.game.behavior.Breakable;
import com.nhnacademy.game.collision.Collidable;
import com.nhnacademy.breakout.BreakoutBall;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;

/**
 * 파괴 가능한 기본 벽돌
 * StaticObject를 확장하고 Breakable 인터페이스 구현
 */
public class SimpleBrick extends StaticObject implements Breakable {
    private int hitPoints;
    private int maxHitPoints;
    private int points;
    private boolean broken;
    
    // 애니메이션 상태
    private boolean isHit;
    private double hitAnimationTimer;
    private static final double HIT_ANIMATION_DURATION = 0.2;
    
    public SimpleBrick(double x, double y, double width, double height, 
                      int hitPoints, int points, Color color) {
        super(x, y, width, height, color);
        this.hitPoints = hitPoints;
        this.maxHitPoints = hitPoints;
        this.points = points;
        this.broken = false;
        this.isHit = false;
        this.hitAnimationTimer = 0;
    }
    
    @Override
    public void draw(GraphicsContext gc) {
        if (broken) return;
        
        // 타격 애니메이션 효과
        double offsetX = 0, offsetY = 0;
        if (isHit) {
            offsetX = (Math.random() - 0.5) * 4;
            offsetY = (Math.random() - 0.5) * 4;
        }
        
        // 그림자 효과
        gc.setEffect(new DropShadow(5, 2, 2, Color.rgb(0, 0, 0, 0.3)));
        
        // 벽돌 몸체
        Color currentColor = getColorForRemainingHits();
        gc.setFill(currentColor);
        gc.fillRect(x + offsetX + 2, y + offsetY + 2, width - 4, height - 4);
        
        // 하이라이트
        gc.setFill(currentColor.brighter());
        gc.fillRect(x + offsetX + 4, y + offsetY + 4, width - 8, height/3);
        
        // 테두리
        gc.setStroke(currentColor.darker());
        gc.setLineWidth(2);
        gc.strokeRect(x + offsetX + 2, y + offsetY + 2, width - 4, height - 4);
        
        // 크랙 효과 (데미지 표시)
        if (hitPoints < maxHitPoints) {
            drawCracks(gc, offsetX, offsetY);
        }
        
        gc.setEffect(null);
    }
    
    @Override
    public void update(double deltaTime) {
        if (isHit) {
            hitAnimationTimer += deltaTime;
            if (hitAnimationTimer >= HIT_ANIMATION_DURATION) {
                isHit = false;
                hitAnimationTimer = 0;
            }
        }
    }
    
    @Override
    public void handleCollision(Collidable other) {
        if (other instanceof BreakoutBall && !broken) {
            hit(1);
        }
    }
    
    // Breakable 인터페이스 구현
    @Override
    public void hit(int damage) {
        if (!broken) {
            hitPoints -= damage;
            isHit = true;
            hitAnimationTimer = 0;
            
            if (hitPoints <= 0) {
                broken = true;
            }
        }
    }
    
    @Override
    public boolean isBroken() {
        return broken;
    }
    
    @Override
    public int getPoints() {
        return broken ? points : 0;
    }
    
    private Color getColorForRemainingHits() {
        double ratio = (double) hitPoints / maxHitPoints;
        return getColor().deriveColor(0, 1, ratio, 1);
    }
    
    private void drawCracks(GraphicsContext gc, double offsetX, double offsetY) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        
        int cracks = maxHitPoints - hitPoints;
        for (int i = 0; i < cracks; i++) {
            double startX = x + offsetX + Math.random() * width;
            double startY = y + offsetY + Math.random() * height;
            double endX = x + offsetX + Math.random() * width;
            double endY = y + offsetY + Math.random() * height;
            
            gc.strokeLine(startX, startY, endX, endY);
        }
    }
}
```

### 4.2 UnbreakableBrick 클래스

```java
package com.nhnacademy.breakout.bricks;

import com.nhnacademy.game.entity.StaticObject;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;

/**
 * 파괴 불가능한 벽돌 (벽)
 * StaticObject를 확장하여 구현
 */
public class UnbreakableBrick extends StaticObject {
    
    public UnbreakableBrick(double x, double y, double width, double height) {
        super(x, y, width, height, Color.GRAY);
    }
    
    @Override
    public void draw(GraphicsContext gc) {
        // 그라디언트 효과로 금속 느낌 표현
        LinearGradient gradient = new LinearGradient(
            0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.LIGHTGRAY),
            new Stop(0.5, Color.GRAY),
            new Stop(1, Color.DARKGRAY)
        );
        
        gc.setFill(gradient);
        gc.fillRect(x, y, width, height);
        
        // 테두리
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, width, height);
        
        // 금속 광택 효과
        gc.setFill(Color.WHITE.deriveColor(0, 1, 1, 0.3));
        gc.fillRect(x + 2, y + 2, width - 4, height/3);
    }
}
```

### 4.3 MultiHitBrick 클래스

```java
package com.nhnacademy.breakout.bricks;

import com.nhnacademy.game.entity.StaticObject;
import com.nhnacademy.game.behavior.MultiHit;
import com.nhnacademy.game.collision.Collidable;
import com.nhnacademy.breakout.BreakoutBall;
import javafx.scene.paint.Color;

/**
 * 다중 타격 벽돌
 * StaticObject를 확장하고 MultiHit 인터페이스 구현
 */
public class MultiHitBrick extends StaticObject implements MultiHit {
    private int hitPoints;
    private int maxHitPoints;
    private int points;
    private boolean broken;
    private int hitMultiplier;
    
    public MultiHitBrick(double x, double y, double width, double height) {
        super(x, y, width, height, Color.DARKRED);
        this.maxHitPoints = 3;
        this.hitPoints = maxHitPoints;
        this.points = 30;
        this.broken = false;
        this.hitMultiplier = 2;
    }
    
    @Override
    public void handleCollision(Collidable other) {
        if (other instanceof BreakoutBall && !broken) {
            // 다중 타격 처리
            for (int i = 0; i < hitMultiplier; i++) {
                hit(1);
                if (broken) break;
            }
        }
    }
    
    // MultiHit 인터페이스 구현
    @Override
    public void hit(int damage) {
        if (!broken) {
            hitPoints -= damage;
            if (hitPoints <= 0) {
                broken = true;
            }
            // 색상 업데이트
            double ratio = (double) hitPoints / maxHitPoints;
            setColor(Color.DARKRED.deriveColor(0, 1, ratio, 1));
        }
    }
    
    @Override
    public boolean isBroken() {
        return broken;
    }
    
    @Override
    public int getPoints() {
        return broken ? points : 0;
    }
    
    @Override
    public int getHitMultiplier() {
        return hitMultiplier;
    }
    
    @Override
    public void setHitMultiplier(int multiplier) {
        this.hitMultiplier = multiplier;
    }
}
```

### 4.4 ExplodingBrick 클래스

```java
package com.nhnacademy.breakout.bricks;

import com.nhnacademy.game.entity.StaticObject;
import com.nhnacademy.game.behavior.Exploding;
import com.nhnacademy.game.collision.Collidable;
import com.nhnacademy.game.collision.Boundable;
import com.nhnacademy.breakout.BreakoutBall;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * 폭발하는 벽돌
 * StaticObject를 확장하고 Exploding 인터페이스 구현
 */
public class ExplodingBrick extends StaticObject implements Exploding {
    private int hitPoints;
    private int points;
    private boolean broken;
    private double explosionRadius;
    
    public ExplodingBrick(double x, double y, double width, double height) {
        super(x, y, width, height, Color.PURPLE);
        this.hitPoints = 1;
        this.points = 50;
        this.broken = false;
        this.explosionRadius = 100;
    }
    
    @Override
    public void draw(GraphicsContext gc) {
        super.draw(gc);
        
        if (!broken) {
            // 폭발 아이콘 표시
            gc.setFill(Color.YELLOW);
            gc.setFont(Font.font(16));
            gc.fillText("💥", getCenterX() - 8, getCenterY() + 6);
        }
    }
    
    @Override
    public void handleCollision(Collidable other) {
        if (other instanceof BreakoutBall && !broken) {
            hit(1);
        }
    }
    
    // Exploding 인터페이스 구현
    @Override
    public void hit(int damage) {
        if (!broken) {
            hitPoints -= damage;
            if (hitPoints <= 0) {
                broken = true;
            }
        }
    }
    
    @Override
    public boolean isBroken() {
        return broken;
    }
    
    @Override
    public int getPoints() {
        return broken ? points : 0;
    }
    
    @Override
    public void explode() {
        // 폭발 효과는 GameWorld에서 처리
        broken = true;
    }
    
    @Override
    public double getExplosionRadius() {
        return explosionRadius;
    }
    
    @Override
    public boolean isInExplosionRange(Boundable other) {
        if (!broken) return false;
        
        double centerX = getCenterX();
        double centerY = getCenterY();
        double otherCenterX = other.getBounds().getMinX() + 
                            (other.getBounds().getMaxX() - other.getBounds().getMinX()) / 2;
        double otherCenterY = other.getBounds().getMinY() + 
                            (other.getBounds().getMaxY() - other.getBounds().getMinY()) / 2;
        
        double distance = Math.sqrt(Math.pow(centerX - otherCenterX, 2) + 
                                  Math.pow(centerY - otherCenterY, 2));
        
        return distance <= explosionRadius;
    }
}
```

### 4.5 PowerUpBrick 클래스

```java
package com.nhnacademy.breakout.bricks;

import com.nhnacademy.game.behavior.PowerUpProvider;
import com.nhnacademy.breakout.powerups.PowerUpType;
import javafx.scene.paint.Color;

/**
 * 파워업을 제공하는 벽돌
 * SimpleBrick을 확장하고 PowerUpProvider 인터페이스 구현
 */
public class PowerUpBrick extends SimpleBrick implements PowerUpProvider {
    private PowerUpType powerUpType;
    private double dropChance;
    
    public PowerUpBrick(double x, double y, double width, double height) {
        super(x, y, width, height, 1, 20, Color.GOLD);
        this.dropChance = 1.0; // 100% 확률로 파워업 드롭
        // 랜덤 파워업 타입 설정
        PowerUpType[] types = PowerUpType.values();
        this.powerUpType = types[(int)(Math.random() * types.length)];
    }
    
    // PowerUpProvider 인터페이스 구현
    @Override
    public boolean shouldDropPowerUp() {
        return isBroken() && Math.random() < dropChance;
    }
    
    @Override
    public String getPowerUpType() {
        return powerUpType.name();
    }
    
    @Override
    public double getDropChance() {
        return dropChance;
    }
    
    @Override
    public void setDropChance(double chance) {
        this.dropChance = Math.max(0, Math.min(1, chance));
    }
}
```

---

## 5. PowerUp 시스템

Ball 클래스를 확장하여 파워업을 구현합니다.

### 5.1 PowerUpType 열거형

```java
package com.nhnacademy.breakout.powerups;

import javafx.scene.paint.Color;

/**
 * 파워업 타입 정의
 */
public enum PowerUpType {
    PADDLE_EXPAND("Expand", Color.GREEN, "⟷"),
    PADDLE_SHRINK("Shrink", Color.RED, "⟵⟶"),
    BALL_SPEED_UP("Speed+", Color.ORANGE, "⚡"),
    BALL_SPEED_DOWN("Speed-", Color.BLUE, "🐌"),
    MULTI_BALL("Multi", Color.PURPLE, "⚈⚈⚈"),
    EXTRA_LIFE("Life", Color.GOLD, "❤"),
    FIRE_BALL("Fire", Color.ORANGERED, "🔥"),
    STICKY_PADDLE("Sticky", Color.LIME, "🧲");
    
    private final String name;
    private final Color color;
    private final String symbol;
    
    PowerUpType(String name, Color color, String symbol) {
        this.name = name;
        this.color = color;
        this.symbol = symbol;
    }
    
    public String getName() { return name; }
    public Color getColor() { return color; }
    public String getSymbol() { return symbol; }
}
```

### 5.2 PowerUp 기본 클래스

```java
package com.nhnacademy.breakout.powerups;

import com.nhnacademy.game.entity.Ball;
import com.nhnacademy.game.collision.Collidable;
import com.nhnacademy.breakout.BreakoutPaddle;
import com.nhnacademy.breakout.BreakoutGameWorld;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.text.Font;

/**
 * 파워업 아이템 클래스
 * Ball 클래스를 확장하여 구현
 */
public abstract class PowerUp extends Ball {
    private PowerUpType type;
    private boolean collected;
    private double animationTimer;
    private double glowIntensity;
    
    public PowerUp(double x, double y, PowerUpType type) {
        super(x, y, 15, type.getColor());
        this.type = type;
        this.collected = false;
        this.animationTimer = 0;
        this.glowIntensity = 0.5;
        
        // 아래로만 떨어지도록 설정
        setVelocity(0, 100);
    }
    
    @Override
    public void draw(GraphicsContext gc) {
        // 빛나는 효과
        RadialGradient gradient = new RadialGradient(
            0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
            new Stop(0, type.getColor().deriveColor(0, 1, 1 + glowIntensity, 1)),
            new Stop(0.7, type.getColor()),
            new Stop(1, type.getColor().deriveColor(0, 1, 0.5, 0.8))
        );
        
        gc.setFill(gradient);
        gc.fillOval(x, y, width, height);
        
        // 심볼 표시
        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.setFont(Font.font(14));
        gc.fillText(type.getSymbol(), getCenterX() - 7, getCenterY() + 5);
        
        // 테두리
        gc.setStroke(type.getColor().darker());
        gc.setLineWidth(2);
        gc.strokeOval(x, y, width, height);
    }
    
    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);
        
        // 애니메이션 업데이트
        animationTimer += deltaTime;
        glowIntensity = 0.5 + 0.3 * Math.sin(animationTimer * 5);
    }
    
    @Override
    public void handleCollision(Collidable other) {
        if (other instanceof BreakoutPaddle) {
            collected = true;
        }
    }
    
    /**
     * 파워업 효과를 적용합니다
     * @param world 게임 월드
     */
    public abstract void applyEffect(BreakoutGameWorld world);
    
    // Getter 메서드들
    public PowerUpType getType() { return type; }
    public boolean isCollected() { return collected; }
    
    /**
     * 화면 밖으로 나갔는지 확인
     */
    public boolean isOutOfBounds(double screenHeight) {
        return y > screenHeight;
    }
}
```

### 5.3 구체적인 PowerUp 구현들

```java
package com.nhnacademy.breakout.powerups;

import com.nhnacademy.breakout.BreakoutGameWorld;
import com.nhnacademy.breakout.BreakoutPaddle;
import com.nhnacademy.breakout.BreakoutBall;
import java.util.ArrayList;
import java.util.List;

/**
 * 패들 확장 파워업
 */
public class PaddleExpandPowerUp extends PowerUp {
    public PaddleExpandPowerUp(double x, double y) {
        super(x, y, PowerUpType.PADDLE_EXPAND);
    }
    
    @Override
    public void applyEffect(BreakoutGameWorld world) {
        BreakoutPaddle paddle = world.getPaddle();
        double newWidth = Math.min(paddle.getWidth() * 1.5, world.getWidth() * 0.3);
        paddle.setWidth(newWidth);
        world.addPowerUpEffect("Paddle Expanded!", 3.0);
    }
}

/**
 * 멀티볼 파워업
 */
public class MultiBallPowerUp extends PowerUp {
    public MultiBallPowerUp(double x, double y) {
        super(x, y, PowerUpType.MULTI_BALL);
    }
    
    @Override
    public void applyEffect(BreakoutGameWorld world) {
        List<BreakoutBall> currentBalls = new ArrayList<>(world.getBalls());
        
        for (BreakoutBall ball : currentBalls) {
            // 각 공마다 2개의 추가 공 생성
            for (int i = 0; i < 2; i++) {
                BreakoutBall newBall = new BreakoutBall(
                    ball.getCenterX(), ball.getCenterY(), ball.getRadius()
                );
                
                // 랜덤한 방향으로 발사
                double angle = Math.random() * Math.PI * 2;
                double speed = Math.sqrt(Math.pow(ball.getVelocityX(), 2) + 
                                       Math.pow(ball.getVelocityY(), 2));
                newBall.setVelocity(speed * Math.cos(angle), speed * Math.sin(angle));
                
                world.addBall(newBall);
            }
        }
        
        world.addPowerUpEffect("Multi Ball!", 3.0);
    }
}

/**
 * 파이어볼 파워업
 */
public class FireBallPowerUp extends PowerUp {
    public FireBallPowerUp(double x, double y) {
        super(x, y, PowerUpType.FIRE_BALL);
    }
    
    @Override
    public void applyEffect(BreakoutGameWorld world) {
        for (BreakoutBall ball : world.getBalls()) {
            ball.setFireBall(true);
        }
        
        world.addPowerUpEffect("Fire Ball!", 5.0);
        world.scheduleTask(() -> {
            // 5초 후 원래대로
            for (BreakoutBall ball : world.getBalls()) {
                ball.setFireBall(false);
            }
        }, 5.0);
    }
}

/**
 * 추가 생명 파워업
 */
public class ExtraLifePowerUp extends PowerUp {
    public ExtraLifePowerUp(double x, double y) {
        super(x, y, PowerUpType.EXTRA_LIFE);
    }
    
    @Override
    public void applyEffect(BreakoutGameWorld world) {
        world.addLife();
        world.addPowerUpEffect("Extra Life!", 3.0);
    }
}
```

---

## 6. BreakoutGameWorld

게임의 핵심 로직을 관리하는 클래스입니다. 이전 구현과 유사하지만 리팩토링된 클래스들을 사용합니다.

```java
package com.nhnacademy.breakout;

import com.nhnacademy.game.collision.Bounds;
import com.nhnacademy.breakout.bricks.*;
import com.nhnacademy.breakout.powerups.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import java.util.*;

/**
 * Breakout 게임의 메인 월드 클래스
 * 리팩토링된 클래스들을 사용하여 구현
 */
public class BreakoutGameWorld {
    // 게임 상태
    public enum GameState {
        READY,      // 게임 시작 전
        PLAYING,    // 게임 진행 중
        PAUSED,     // 일시 정지
        GAME_OVER,  // 게임 오버
        LEVEL_CLEAR // 레벨 클리어
    }
    
    private double width;
    private double height;
    
    // 게임 객체들
    private BreakoutPaddle paddle;
    private List<BreakoutBall> balls;
    private List<StaticObject> bricks; // 모든 벽돌의 부모 클래스
    private List<PowerUp> powerUps;
    private List<EffectMessage> messages;
    
    // 게임 상태
    private GameState gameState;
    private int level;
    private int score;
    private int lives;
    private int combo;
    private double comboTimer;
    
    // 예약된 작업들
    private List<ScheduledTask> scheduledTasks;
    
    public BreakoutGameWorld(double width, double height) {
        this.width = width;
        this.height = height;
        
        this.balls = new ArrayList<>();
        this.bricks = new ArrayList<>();
        this.powerUps = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.scheduledTasks = new ArrayList<>();
        
        this.gameState = GameState.READY;
        this.level = 1;
        this.score = 0;
        this.lives = 3;
        this.combo = 0;
        this.comboTimer = 0;
        
        initializeGame();
    }
    
    private void initializeGame() {
        // 패들 생성
        paddle = new BreakoutPaddle(width / 2 - 40, height - 50, 80, 15, width);
        
        // 초기 공 생성
        resetBall();
        
        // 레벨 로드
        loadLevel(level);
    }
    
    private void resetBall() {
        balls.clear();
        BreakoutBall ball = new BreakoutBall(width / 2, height - 100, 8);
        ball.setVelocity(150, -200);
        balls.add(ball);
    }
    
    private void loadLevel(int levelNum) {
        bricks.clear();
        powerUps.clear();
        
        // 간단한 레벨 생성 (확장 가능)
        createLevel1();
        
        // 벽 생성 (UnbreakableBrick 사용)
        createWalls();
        
        gameState = GameState.READY;
    }
    
    private void createLevel1() {
        double brickWidth = 60;
        double brickHeight = 20;
        double spacing = 5;
        
        int cols = (int)((width - 20) / (brickWidth + spacing));
        int rows = 5;
        
        double startX = (width - (cols * (brickWidth + spacing) - spacing)) / 2;
        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double x = startX + col * (brickWidth + spacing);
                double y = 80 + row * (brickHeight + spacing);
                
                StaticObject brick = null;
                
                // 다양한 벽돌 타입 생성
                if (row == 0) {
                    // 첫 번째 줄: 다중 타격 벽돌
                    brick = new MultiHitBrick(x, y, brickWidth, brickHeight);
                } else if (row == 1 && col == cols / 2) {
                    // 두 번째 줄 중앙: 폭발 벽돌
                    brick = new ExplodingBrick(x, y, brickWidth, brickHeight);
                } else if (row == 2 && col % 3 == 0) {
                    // 세 번째 줄: 파워업 벽돌
                    brick = new PowerUpBrick(x, y, brickWidth, brickHeight);
                } else {
                    // 나머지: 일반 벽돌
                    Color color = row < 2 ? Color.RED : Color.ORANGE;
                    int hitPoints = row < 2 ? 2 : 1;
                    int points = row < 2 ? 20 : 10;
                    brick = new SimpleBrick(x, y, brickWidth, brickHeight, 
                                          hitPoints, points, color);
                }
                
                bricks.add(brick);
            }
        }
    }
    
    private void createWalls() {
        // 상단 벽
        bricks.add(new UnbreakableBrick(0, 0, width, 10));
        // 좌측 벽
        bricks.add(new UnbreakableBrick(0, 0, 10, height));
        // 우측 벽
        bricks.add(new UnbreakableBrick(width - 10, 0, 10, height));
    }
    
    public void update(double deltaTime) {
        if (gameState != GameState.PLAYING) return;
        
        // 게임 객체 업데이트
        paddle.update(deltaTime);
        
        for (BreakoutBall ball : balls) {
            ball.update(deltaTime);
        }
        
        for (PowerUp powerUp : powerUps) {
            powerUp.update(deltaTime);
        }
        
        for (StaticObject brick : bricks) {
            brick.update(deltaTime);
        }
        
        // 충돌 검사
        checkCollisions();
        
        // 정리 작업
        cleanupObjects();
        
        // 콤보 타이머 업데이트
        if (comboTimer > 0) {
            comboTimer -= deltaTime;
            if (comboTimer <= 0) {
                combo = 0;
            }
        }
        
        // 메시지 업데이트
        updateMessages(deltaTime);
        
        // 예약된 작업 처리
        processScheduledTasks(deltaTime);
        
        // 게임 상태 체크
        checkGameState();
    }
    
    private void checkCollisions() {
        // 공-패들 충돌
        for (BreakoutBall ball : balls) {
            if (ball.collidesWith(paddle)) {
                ball.handleCollision(paddle);
                paddle.handleCollision(ball);
            }
        }
        
        // 공-벽돌 충돌
        for (BreakoutBall ball : balls) {
            for (StaticObject brick : bricks) {
                if (brick.collidesWith(ball)) {
                    // 충돌 처리
                    brick.handleCollision(ball);
                    ball.handleCollision(brick);
                    
                    // 벽돌이 파괴되었는지 확인
                    if (brick instanceof Breakable) {
                        Breakable breakable = (Breakable) brick;
                        if (breakable.isBroken()) {
                            onBrickDestroyed(brick, breakable);
                        }
                    }
                }
            }
        }
        
        // 파워업-패들 충돌
        for (PowerUp powerUp : powerUps) {
            if (powerUp.collidesWith(paddle)) {
                powerUp.handleCollision(paddle);
                if (powerUp.isCollected()) {
                    powerUp.applyEffect(this);
                }
            }
        }
        
        // 폭발 벽돌 처리
        handleExplodingBricks();
        
        // 화면 경계 체크
        checkBoundaries();
    }
    
    private void onBrickDestroyed(StaticObject brick, Breakable breakable) {
        // 콤보 처리
        combo++;
        comboTimer = 1.0;
        
        // 점수 계산
        int points = breakable.getPoints() * combo;
        score += points;
        
        // 점수 메시지
        addEffectMessage("+" + points, brick.getCenterX(), 
                        brick.getCenterY(), Color.YELLOW);
        
        // 콤보 메시지
        if (combo > 1) {
            addEffectMessage("COMBO x" + combo + "!", width/2, height/2, Color.ORANGE);
        }
        
        // 파워업 생성 확인
        if (brick instanceof PowerUpProvider) {
            PowerUpProvider provider = (PowerUpProvider) brick;
            if (provider.shouldDropPowerUp()) {
                createPowerUp(brick.getCenterX(), brick.getCenterY(), 
                            provider.getPowerUpType());
            }
        }
    }
    
    private void handleExplodingBricks() {
        List<ExplodingBrick> explodingBricks = new ArrayList<>();
        
        // 폭발한 벽돌 찾기
        for (StaticObject brick : bricks) {
            if (brick instanceof ExplodingBrick) {
                ExplodingBrick explodingBrick = (ExplodingBrick) brick;
                if (explodingBrick.isBroken()) {
                    explodingBricks.add(explodingBrick);
                }
            }
        }
        
        // 폭발 범위 내의 벽돌 파괴
        for (ExplodingBrick explodingBrick : explodingBricks) {
            for (StaticObject brick : bricks) {
                if (brick instanceof Breakable && brick != explodingBrick) {
                    if (explodingBrick.isInExplosionRange(brick)) {
                        Breakable breakable = (Breakable) brick;
                        breakable.hit(999); // 즉시 파괴
                        if (breakable.isBroken()) {
                            onBrickDestroyed(brick, breakable);
                        }
                    }
                }
            }
        }
    }
    
    private void checkBoundaries() {
        // 공 경계 체크
        Iterator<BreakoutBall> ballIterator = balls.iterator();
        while (ballIterator.hasNext()) {
            BreakoutBall ball = ballIterator.next();
            
            // 화면 아래로 떨어진 공 제거
            if (ball.getCenterY() > height) {
                ballIterator.remove();
            }
        }
        
        // 파워업 경계 체크
        Iterator<PowerUp> powerUpIterator = powerUps.iterator();
        while (powerUpIterator.hasNext()) {
            PowerUp powerUp = powerUpIterator.next();
            if (powerUp.isOutOfBounds(height)) {
                powerUpIterator.remove();
            }
        }
    }
    
    private void cleanupObjects() {
        // 수집된 파워업 제거
        powerUps.removeIf(PowerUp::isCollected);
        
        // 파괴된 벽돌 제거 (UnbreakableBrick은 제외)
        bricks.removeIf(brick -> {
            if (brick instanceof Breakable) {
                return ((Breakable) brick).isBroken();
            }
            return false;
        });
    }
    
    private void createPowerUp(double x, double y, String typeName) {
        PowerUp powerUp = null;
        PowerUpType type = PowerUpType.valueOf(typeName);
        
        switch (type) {
            case PADDLE_EXPAND:
                powerUp = new PaddleExpandPowerUp(x, y);
                break;
            case MULTI_BALL:
                powerUp = new MultiBallPowerUp(x, y);
                break;
            case FIRE_BALL:
                powerUp = new FireBallPowerUp(x, y);
                break;
            case EXTRA_LIFE:
                powerUp = new ExtraLifePowerUp(x, y);
                break;
            default:
                powerUp = new PaddleExpandPowerUp(x, y); // 기본값
        }
        
        if (powerUp != null) {
            powerUps.add(powerUp);
        }
    }
    
    private void checkGameState() {
        // 모든 공이 사라졌는지 체크
        if (balls.isEmpty()) {
            lives--;
            if (lives > 0) {
                resetBall();
                paddle.reset();
                gameState = GameState.READY;
            } else {
                gameState = GameState.GAME_OVER;
            }
        }
        
        // 모든 파괴 가능한 벽돌이 파괴되었는지 체크
        boolean allBricksDestroyed = true;
        for (StaticObject brick : bricks) {
            if (brick instanceof Breakable && !((Breakable) brick).isBroken()) {
                allBricksDestroyed = false;
                break;
            }
        }
        
        if (allBricksDestroyed) {
            gameState = GameState.LEVEL_CLEAR;
        }
    }
    
    public void render(GraphicsContext gc) {
        // 배경
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, width, height);
        
        // 게임 객체들 그리기
        paddle.draw(gc);
        
        for (StaticObject brick : bricks) {
            brick.draw(gc);
        }
        
        for (BreakoutBall ball : balls) {
            ball.draw(gc);
        }
        
        for (PowerUp powerUp : powerUps) {
            powerUp.draw(gc);
        }
        
        // UI 그리기
        drawUI(gc);
        
        // 메시지 그리기
        drawMessages(gc);
        
        // 게임 상태별 오버레이
        drawGameStateOverlay(gc);
    }
    
    private void drawUI(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(16));
        gc.setTextAlign(TextAlignment.LEFT);
        
        // 점수
        gc.fillText("Score: " + score, 10, 25);
        
        // 레벨
        gc.fillText("Level: " + level, width/2 - 30, 25);
        
        // 생명
        gc.fillText("Lives: " + lives, width - 80, 25);
        
        // 콤보
        if (combo > 1) {
            gc.setFill(Color.ORANGE);
            gc.fillText("Combo x" + combo, 10, 50);
        }
    }
    
    private void drawMessages(GraphicsContext gc) {
        for (EffectMessage msg : messages) {
            msg.draw(gc);
        }
    }
    
    private void drawGameStateOverlay(GraphicsContext gc) {
        if (gameState == GameState.READY) {
            drawCenteredText(gc, "Press SPACE to Start", Color.WHITE);
        } else if (gameState == GameState.PAUSED) {
            drawCenteredText(gc, "PAUSED", Color.YELLOW);
        } else if (gameState == GameState.GAME_OVER) {
            drawCenteredText(gc, "GAME OVER\nFinal Score: " + score, Color.RED);
        } else if (gameState == GameState.LEVEL_CLEAR) {
            drawCenteredText(gc, "LEVEL CLEAR!\nPress SPACE for Next Level", Color.GREEN);
        }
    }
    
    private void drawCenteredText(GraphicsContext gc, String text, Color color) {
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, width, height);
        
        gc.setFill(color);
        gc.setFont(Font.font(32));
        gc.setTextAlign(TextAlignment.CENTER);
        
        String[] lines = text.split("\n");
        double y = height/2 - lines.length * 20;
        for (String line : lines) {
            gc.fillText(line, width/2, y);
            y += 40;
        }
    }
    
    public void addEffectMessage(String text, double x, double y, Color color) {
        messages.add(new EffectMessage(text, x, y, color));
    }
    
    public void addPowerUpEffect(String text, double duration) {
        addEffectMessage(text, width/2, height/3, Color.CYAN);
    }
    
    private void updateMessages(double deltaTime) {
        Iterator<EffectMessage> it = messages.iterator();
        while (it.hasNext()) {
            EffectMessage msg = it.next();
            msg.update(deltaTime);
            if (msg.isExpired()) {
                it.remove();
            }
        }
    }
    
    public void scheduleTask(Runnable task, double delay) {
        scheduledTasks.add(new ScheduledTask(task, delay));
    }
    
    private void processScheduledTasks(double deltaTime) {
        Iterator<ScheduledTask> it = scheduledTasks.iterator();
        while (it.hasNext()) {
            ScheduledTask task = it.next();
            task.update(deltaTime);
            if (task.isReady()) {
                task.execute();
                it.remove();
            }
        }
    }
    
    // 게임 제어 메서드들
    public void startGame() {
        if (gameState == GameState.READY) {
            gameState = GameState.PLAYING;
        } else if (gameState == GameState.LEVEL_CLEAR) {
            level++;
            loadLevel(level);
            resetBall();
            paddle.reset();
            gameState = GameState.PLAYING;
        }
    }
    
    public void pauseGame() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
        } else if (gameState == GameState.PAUSED) {
            gameState = GameState.PLAYING;
        }
    }
    
    public void addLife() {
        lives++;
    }
    
    public void addBall(BreakoutBall ball) {
        balls.add(ball);
    }
    
    // Getter 메서드들
    public BreakoutPaddle getPaddle() { return paddle; }
    public List<BreakoutBall> getBalls() { return new ArrayList<>(balls); }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public GameState getGameState() { return gameState; }
}

// 효과 메시지와 예약된 작업 클래스는 이전과 동일
```

---

## 7. BreakoutGameApp

JavaFX 애플리케이션 메인 클래스입니다. 이전 구현과 거의 동일하며, import문만 조정됩니다.

```java
package com.nhnacademy.breakout;

import javafx.application.Application;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Breakout 게임의 JavaFX 애플리케이션 클래스
 * 리팩토링된 클래스들을 사용
 */
public class BreakoutGameApp extends Application {
    private static final double WIDTH = 800;
    private static final double HEIGHT = 600;
    private static final String TITLE = "Breakout Game - Refactored";
    
    private BreakoutGameWorld gameWorld;
    private GraphicsContext gc;
    private AnimationTimer gameLoop;
    private long lastUpdate;
    
    @Override
    public void start(Stage primaryStage) {
        // 캔버스 생성
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
        
        // 게임 월드 생성
        gameWorld = new BreakoutGameWorld(WIDTH, HEIGHT);
        
        // 씬 구성
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);
        
        // 키 입력 처리
        setupKeyHandlers(scene);
        
        // 게임 루프 설정
        setupGameLoop();
        
        // 윈도우 설정
        primaryStage.setTitle(TITLE);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        
        // 게임 시작
        gameLoop.start();
    }
    
    private void setupKeyHandlers(Scene scene) {
        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            
            // 패들 제어
            BreakoutPaddle paddle = gameWorld.getPaddle();
            paddle.keyPressed(code);
            
            // 게임 제어
            switch (code) {
                case SPACE:
                    gameWorld.startGame();
                    break;
                case P:
                case ESCAPE:
                    gameWorld.pauseGame();
                    break;
                case R:
                    if (gameWorld.getGameState() == BreakoutGameWorld.GameState.GAME_OVER) {
                        restartGame();
                    }
                    break;
            }
        });
        
        scene.setOnKeyReleased(event -> {
            BreakoutPaddle paddle = gameWorld.getPaddle();
            paddle.keyReleased(event.getCode());
        });
    }
    
    private void setupGameLoop() {
        lastUpdate = System.nanoTime();
        
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // 델타 타임 계산
                double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;
                
                // 프레임 제한 (최대 델타 타임)
                deltaTime = Math.min(deltaTime, 0.02); // 50 FPS 최소
                
                // 게임 업데이트
                gameWorld.update(deltaTime);
                
                // 렌더링
                gameWorld.render(gc);
            }
        };
    }
    
    private void restartGame() {
        gameWorld = new BreakoutGameWorld(WIDTH, HEIGHT);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
```

---

## 요약

이번 리팩토링에서는 다음과 같이 코드 재사용을 최대화했습니다:

1. **BreakoutPaddle**: Box 클래스를 확장하여 구현
2. **BreakoutBall**: Ball 클래스를 확장하여 구현
3. **모든 Brick 클래스들**: StaticObject를 확장하고 적절한 인터페이스 구현
   - SimpleBrick: Breakable 인터페이스
   - MultiHitBrick: MultiHit 인터페이스
   - ExplodingBrick: Exploding 인터페이스
   - PowerUpBrick: PowerUpProvider 인터페이스
4. **PowerUp**: Ball 클래스를 확장하여 구현
5. **UnbreakableBrick**: StaticObject를 확장하여 벽 구현
6. **모든 인터페이스**: com.nhnacademy.game.* 패키지의 인터페이스 사용

이러한 리팩토링을 통해 코드 중복을 크게 줄이고, 상속과 인터페이스를 통한 객체지향 프로그래밍의 장점을 최대한 활용했습니다.