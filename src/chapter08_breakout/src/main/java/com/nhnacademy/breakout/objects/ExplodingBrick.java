package com.nhnacademy.breakout.objects;

import com.nhnacademy.game.entity.StaticObject;
import com.nhnacademy.game.behavior.Exploding;
import com.nhnacademy.game.collision.Collidable;
import com.nhnacademy.game.collision.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * 폭발하는 벽돌
 * 파괴될 때 주변 벽돌에도 피해를 줍니다.
 */
public class ExplodingBrick extends StaticObject implements Exploding {
    private int points;
    private boolean broken = false;
    private double explosionRadius = 100;
    private int explosionDamage = 1;
    
    public ExplodingBrick(double x, double y, double width, double height, 
                         Color color, int points) {
        super(x, y, width, height, color);
        this.points = points;
    }
    
    @Override
    public void hit(int damage) {
        broken = true;
    }
    
    @Override
    public boolean isBroken() {
        return broken;
    }
    
    @Override
    public int getPoints() {
        return points;
    }
    
    @Override
    public double getExplosionRadius() {
        return explosionRadius;
    }
    
    @Override
    public int getExplosionDamage() {
        return explosionDamage;
    }
    
    @Override
    public Bounds getExplosionBounds() {
        double centerX = getCenterX();
        double centerY = getCenterY();
        return new Bounds(
            centerX - explosionRadius,
            centerY - explosionRadius,
            explosionRadius * 2,
            explosionRadius * 2
        );
    }
    
    @Override
    public List<ExplosionEffect> explode() {
        List<ExplosionEffect> effects = new ArrayList<>();
        effects.add(new ExplosionEffect(getCenterX(), getCenterY(), 
                                       explosionRadius, 0.5));
        return effects;
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
        
        // 폭발 벽돌 표시 (TNT 모양)
        gc.setFill(Color.YELLOW);
        gc.setFont(javafx.scene.text.Font.font(12));
        gc.fillText("TNT", getCenterX() - 10, getCenterY() + 4);
        
        // 위험 표시
        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(2);
        gc.strokeRect(getX() + 2, getY() + 2, getWidth() - 4, getHeight() - 4);
    }
}