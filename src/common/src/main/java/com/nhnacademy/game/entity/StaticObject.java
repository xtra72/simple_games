package com.nhnacademy.game.entity;

import com.nhnacademy.game.core.GameObject;
import com.nhnacademy.game.collision.Collidable;
import com.nhnacademy.game.collision.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 정적인 객체 클래스
 * 움직이지 않고 충돌만 가능한 객체입니다.
 */
public class StaticObject extends GameObject implements Collidable {
    private Color color;
    private boolean filled;
    
    public StaticObject(double x, double y, double width, double height) {
        super(x, y, width, height);
        this.color = Color.GRAY;
        this.filled = true;
    }
    
    public StaticObject(double x, double y, double width, double height, Color color) {
        this(x, y, width, height);
        this.color = color;
    }
    
    @Override
    public void draw(GraphicsContext gc) {
        if (filled) {
            gc.setFill(color);
            gc.fillRect(x, y, width, height);
        } else {
            gc.setStroke(color);
            gc.setLineWidth(2);
            gc.strokeRect(x, y, width, height);
        }
    }
    
    // Collidable 인터페이스 구현
    @Override
    public boolean collidesWith(Collidable other) {
        return getBounds().intersects(other.getBounds());
    }
    
    @Override
    public void handleCollision(Collidable other) {
        // 정적 객체는 충돌해도 변화하지 않음
        // 필요한 경우 하위 클래스에서 오버라이드
    }
    
    @Override
    public Bounds getBounds() {
        return new Bounds(x, y, width, height);
    }
    
    // Getters and Setters
    public Color getColor() {
        return color;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public boolean isFilled() {
        return filled;
    }
    
    public void setFilled(boolean filled) {
        this.filled = filled;
    }
}