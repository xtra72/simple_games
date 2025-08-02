package com.nhnacademy.cannon;

import com.nhnacademy.game.entity.Ball;
import com.nhnacademy.game.physics.Vector2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * 발사체 클래스
 * Ball을 상속받아 발사체를 구현합니다.
 */
public class Projectile extends Ball {
    private ProjectileType type;
    private int damage;
    private double splitTime = -1;
    private List<Vector2D> trail;
    
    public Projectile(double x, double y, double radius, ProjectileType type) {
        super(x, y, radius, type.getColor());
        this.type = type;
        this.damage = type.getBaseDamage();
        this.trail = new ArrayList<>();
    }
    
    @Override
    public void update(double deltaTime) {
        // 궤적 추가
        trail.add(new Vector2D(getCenterX(), getCenterY()));
        if (trail.size() > 20) {
            trail.remove(0);
        }
        
        super.update(deltaTime);
        
        // 분열 타이머 업데이트
        if (splitTime > 0) {
            splitTime -= deltaTime;
        }
    }
    
    @Override
    public void draw(GraphicsContext gc) {
        // 궤적 그리기
        gc.setGlobalAlpha(0.3);
        for (int i = 0; i < trail.size(); i++) {
            Vector2D pos = trail.get(i);
            double size = radius * 2 * i / trail.size();
            gc.setFill(Color.ORANGE);
            gc.fillOval(pos.x - size/2, pos.y - size/2, size, size);
        }
        gc.setGlobalAlpha(1.0);
        
        // 발사체 그리기 (부모 클래스의 draw 호출)
        super.draw(gc);
        
        // 테두리
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeOval(getX(), getY(), getWidth(), getHeight());
    }
    
    /**
     * 분열 시간을 설정합니다.
     * @param time 분열까지의 시간 (초)
     */
    public void setSplitTime(double time) {
        this.splitTime = time;
    }
    
    /**
     * 분열 여부를 확인합니다.
     * @param deltaTime 프레임 시간
     * @return 분열 여부
     */
    public boolean shouldSplit(double deltaTime) {
        return splitTime >= 0 && splitTime <= deltaTime;
    }
    
    
    // Getters
    public ProjectileType getType() { return type; }
    public int getDamage() { return damage; }
}