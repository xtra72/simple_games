package com.nhnacademy.breakout.objects;

import com.nhnacademy.game.entity.StaticObject;
import com.nhnacademy.game.behavior.MultiHit;
import com.nhnacademy.game.collision.Collidable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 여러 번 맞아야 깨지는 벽돌
 * MultiHit 인터페이스를 구현하여 체력 시스템을 가집니다.
 */
public class MultiHitBrick extends StaticObject implements MultiHit {
    private int maxHits;
    private int currentHits;
    private int points;
    private Color originalColor;
    
    public MultiHitBrick(double x, double y, double width, double height, 
                        Color color, int points, int maxHits) {
        super(x, y, width, height, color);
        this.points = points;
        this.maxHits = maxHits;
        this.currentHits = maxHits;
        this.originalColor = color;
    }
    
    @Override
    public void hit(int damage) {
        currentHits = Math.max(0, currentHits - damage);
        updateColorBasedOnDamage();
    }
    
    @Override
    public boolean isBroken() {
        return currentHits <= 0;
    }
    
    @Override
    public int getPoints() {
        return points;
    }
    
    @Override
    public int getMaxHits() {
        return maxHits;
    }
    
    @Override
    public int getCurrentHits() {
        return currentHits;
    }
    
    @Override
    public DamageState getDamageState() {
        float healthRatio = (float) currentHits / maxHits;
        return DamageState.fromHealthRatio(healthRatio);
    }
    
    @Override
    public void handleCollision(Collidable other) {
        if (other instanceof BreakoutBall) {
            hit(1);
        }
    }
    
    @Override
    public void draw(GraphicsContext gc) {
        super.draw(gc);
        
        // 체력 표시
        if (currentHits < maxHits && currentHits > 0) {
            // 체력바 배경
            gc.setFill(Color.BLACK);
            gc.fillRect(getX() + 2, getY() + 2, getWidth() - 4, 4);
            
            // 체력바
            double healthRatio = (double) currentHits / maxHits;
            gc.setFill(Color.GREEN.interpolate(Color.RED, 1 - healthRatio));
            gc.fillRect(getX() + 2, getY() + 2, (getWidth() - 4) * healthRatio, 4);
        }
        
        // 체력 숫자 표시
        if (currentHits > 1) {
            gc.setFill(Color.WHITE);
            gc.fillText(String.valueOf(currentHits), 
                       getCenterX() - 5, getCenterY() + 5);
        }
    }
    
    /**
     * 피해 정도에 따라 색상을 업데이트합니다.
     */
    private void updateColorBasedOnDamage() {
        DamageState state = getDamageState();
        switch (state) {
            case PERFECT:
                setColor(originalColor);
                break;
            case SLIGHTLY_DAMAGED:
                setColor(originalColor.deriveColor(0, 0.8, 0.9, 1.0));
                break;
            case DAMAGED:
                setColor(originalColor.deriveColor(0, 0.6, 0.7, 1.0));
                break;
            case HEAVILY_DAMAGED:
                setColor(originalColor.deriveColor(0, 0.4, 0.5, 1.0));
                break;
            case CRITICAL:
                setColor(originalColor.deriveColor(0, 0.2, 0.3, 1.0));
                break;
        }
    }
}