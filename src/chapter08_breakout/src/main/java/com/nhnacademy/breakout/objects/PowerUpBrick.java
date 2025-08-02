package com.nhnacademy.breakout.objects;

import com.nhnacademy.game.behavior.PowerUpProvider;
import com.nhnacademy.game.collision.Collidable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Random;

/**
 * 파워업을 제공하는 벽돌
 * 파괴될 때 일정 확률로 파워업을 드롭합니다.
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