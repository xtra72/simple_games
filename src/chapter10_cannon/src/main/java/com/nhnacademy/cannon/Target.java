package com.nhnacademy.cannon;

import com.nhnacademy.game.entity.Box;
import com.nhnacademy.game.collision.Collidable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * 타겟 클래스
 * Box를 상속받아 타겟을 구현합니다.
 */
public class Target extends Box {
    private TargetType type;
    private int health;
    private int maxHealth;
    private int points;
    
    public Target(double x, double y, double width, double height, TargetType type) {
        super(x, y, width, height, type.getColor());
        this.type = type;
        this.health = type.getDefaultHealth();
        this.maxHealth = health;
        this.points = type.getDefaultPoints();
    }
    
    public Target(double x, double y, double width, double height, TargetType type, int points) {
        super(x, y, width, height, type.getColor());
        this.type = type;
        this.health = type.getDefaultHealth();
        this.maxHealth = health;
        this.points = points;
    }
    
    /**
     * 타겟이 피해를 받습니다.
     * @param damage 피해량
     */
    public void takeDamage(int damage) {
        health -= damage;
        if (health < 0) health = 0;
    }
    
    /**
     * 타겟이 파괴되었는지 확인합니다.
     * @return 파괴 여부
     */
    public boolean isDestroyed() {
        return health <= 0;
    }
    
    @Override
    public void draw(GraphicsContext gc) {
        // 기본 박스 그리기
        super.draw(gc);
        
        // 테두리
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, width, height);
        
        // 체력바 (피해를 받은 경우에만 표시)
        if (health < maxHealth) {
            gc.setFill(Color.BLACK);
            gc.fillRect(x, y - 10, width, 5);
            gc.setFill(Color.GREEN);
            gc.fillRect(x, y - 10, width * health / maxHealth, 5);
        }
        
        // 타입 표시
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(12));
        gc.setTextAlign(TextAlignment.CENTER);
        String typeChar = type.name().substring(0, 1);
        gc.fillText(typeChar, getCenterX(), getCenterY() + 4);
    }
    
    @Override
    public void handleCollision(Collidable other) {
        // 타겟은 발사체와 충돌하면 피해를 받음
        if (other instanceof Projectile) {
            Projectile projectile = (Projectile) other;
            takeDamage(projectile.getDamage());
        }
    }
    
    // Getters and Setters
    public TargetType getType() { return type; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getPoints() { return points; }
    
    public void setHealth(int health) {
        this.health = health;
        if (this.health > maxHealth) {
            this.health = maxHealth;
        }
    }
}