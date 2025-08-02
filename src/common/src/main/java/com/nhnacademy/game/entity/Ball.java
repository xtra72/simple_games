package com.nhnacademy.game.entity;

import com.nhnacademy.game.core.GameObject;
import com.nhnacademy.game.movement.Movable;
import com.nhnacademy.game.collision.Collidable;
import com.nhnacademy.game.collision.Boundable;
import com.nhnacademy.game.collision.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 공 객체 클래스
 * 이동 가능하고, 충돌 가능하며, 경계를 가진 원형 객체입니다.
 */
public class Ball extends GameObject implements Movable, Collidable, Boundable {
    private double radius;
    private double vx, vy;
    private Color color;
    
    public Ball(double x, double y, double radius) {
        super(x - radius, y - radius, radius * 2, radius * 2);
        this.radius = radius;
        this.vx = 0;
        this.vy = 0;
        this.color = Color.RED;
    }
    
    public Ball(double x, double y, double radius, Color color) {
        this(x, y, radius);
        this.color = color;
    }
    
    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillOval(x, y, width, height);
    }
    
    @Override
    public void update(double deltaTime) {
        move(deltaTime);
    }
    
    // Movable 인터페이스 구현
    @Override
    public void move(double deltaTime) {
        x += vx * deltaTime;
        y += vy * deltaTime;
    }
    
    @Override
    public double getVelocityX() {
        return vx;
    }
    
    @Override
    public double getVelocityY() {
        return vy;
    }
    
    @Override
    public void setVelocity(double vx, double vy) {
        this.vx = vx;
        this.vy = vy;
    }
    
    @Override
    public void reverseX() {
        vx = -vx;
    }
    
    @Override
    public void reverseY() {
        vy = -vy;
    }
    
    // Collidable 인터페이스 구현
    @Override
    public boolean collidesWith(Collidable other) {
        Bounds otherBounds = other.getBounds();
        double centerX = getCenterX();
        double centerY = getCenterY();
        
        // 원형 충돌 검사
        return otherBounds.intersectsCircle(centerX, centerY, radius);
    }
    
    @Override
    public void handleCollision(Collidable other) {
        // 기본 충돌 처리: 속도 반전
        // 실제 충돌 방향에 따라 더 정교한 처리가 필요할 수 있음
        Bounds otherBounds = other.getBounds();
        double centerX = getCenterX();
        double centerY = getCenterY();
        
        // 충돌 방향 판단
        double overlapLeft = (centerX + radius) - otherBounds.getMinX();
        double overlapRight = otherBounds.getMaxX() - (centerX - radius);
        double overlapTop = (centerY + radius) - otherBounds.getMinY();
        double overlapBottom = otherBounds.getMaxY() - (centerY - radius);
        
        double minOverlapX = Math.min(overlapLeft, overlapRight);
        double minOverlapY = Math.min(overlapTop, overlapBottom);
        
        if (minOverlapX < minOverlapY) {
            reverseX();
        } else {
            reverseY();
        }
    }
    
    @Override
    public Bounds getBounds() {
        return new Bounds(x, y, width, height);
    }
    
    // Boundable 인터페이스 구현
    @Override
    public boolean isInBounds(Bounds boundary) {
        double centerX = getCenterX();
        double centerY = getCenterY();
        
        return centerX - radius >= boundary.getMinX() &&
               centerX + radius <= boundary.getMaxX() &&
               centerY - radius >= boundary.getMinY() &&
               centerY + radius <= boundary.getMaxY();
    }
    
    @Override
    public void handleBoundaryCollision(Bounds boundary) {
        BoundaryEdge edge = checkBoundaryEdge(boundary);
        
        switch (edge) {
            case LEFT:
                x = boundary.getMinX();
                reverseX();
                break;
            case RIGHT:
                x = boundary.getMaxX() - width;
                reverseX();
                break;
            case TOP:
                y = boundary.getMinY();
                reverseY();
                break;
            case BOTTOM:
                y = boundary.getMaxY() - height;
                reverseY();
                break;
            case NONE:
                break;
        }
    }
    
    @Override
    public BoundaryEdge checkBoundaryEdge(Bounds boundary) {
        double centerX = getCenterX();
        double centerY = getCenterY();
        
        if (centerX - radius <= boundary.getMinX()) {
            return BoundaryEdge.LEFT;
        } else if (centerX + radius >= boundary.getMaxX()) {
            return BoundaryEdge.RIGHT;
        } else if (centerY - radius <= boundary.getMinY()) {
            return BoundaryEdge.TOP;
        } else if (centerY + radius >= boundary.getMaxY()) {
            return BoundaryEdge.BOTTOM;
        }
        
        return BoundaryEdge.NONE;
    }
    
    // Getters and Setters
    public double getRadius() {
        return radius;
    }
    
    public void setRadius(double radius) {
        this.radius = radius;
        this.width = radius * 2;
        this.height = radius * 2;
    }
    
    public Color getColor() {
        return color;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
}