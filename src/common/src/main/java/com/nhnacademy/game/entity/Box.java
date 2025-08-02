package com.nhnacademy.game.entity;

import com.nhnacademy.game.core.GameObject;
import com.nhnacademy.game.movement.Movable;
import com.nhnacademy.game.collision.Collidable;
import com.nhnacademy.game.collision.Boundable;
import com.nhnacademy.game.collision.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 박스 객체 클래스
 * 이동 가능하고, 충돌 가능하며, 경계를 가진 사각형 객체입니다.
 */
public class Box extends GameObject implements Movable, Collidable, Boundable {
    private double vx, vy;
    private Color color;
    private boolean filled;
    
    public Box(double x, double y, double width, double height) {
        super(x, y, width, height);
        this.vx = 0;
        this.vy = 0;
        this.color = Color.BLUE;
        this.filled = true;
    }
    
    public Box(double x, double y, double width, double height, Color color) {
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
        return getBounds().intersects(otherBounds);
    }
    
    @Override
    public void handleCollision(Collidable other) {
        // 기본 충돌 처리: 속도 반전
        Bounds otherBounds = other.getBounds();
        Bounds myBounds = getBounds();
        
        // 충돌 방향 판단
        double overlapLeft = myBounds.getMaxX() - otherBounds.getMinX();
        double overlapRight = otherBounds.getMaxX() - myBounds.getMinX();
        double overlapTop = myBounds.getMaxY() - otherBounds.getMinY();
        double overlapBottom = otherBounds.getMaxY() - myBounds.getMinY();
        
        double minOverlapX = Math.min(overlapLeft, overlapRight);
        double minOverlapY = Math.min(overlapTop, overlapBottom);
        
        if (minOverlapX < minOverlapY) {
            reverseX();
            // 위치 조정
            if (overlapLeft < overlapRight) {
                x = otherBounds.getMinX() - width;
            } else {
                x = otherBounds.getMaxX();
            }
        } else {
            reverseY();
            // 위치 조정
            if (overlapTop < overlapBottom) {
                y = otherBounds.getMinY() - height;
            } else {
                y = otherBounds.getMaxY();
            }
        }
    }
    
    @Override
    public Bounds getBounds() {
        return new Bounds(x, y, width, height);
    }
    
    // Boundable 인터페이스 구현
    @Override
    public boolean isInBounds(Bounds boundary) {
        return x >= boundary.getMinX() &&
               x + width <= boundary.getMaxX() &&
               y >= boundary.getMinY() &&
               y + height <= boundary.getMaxY();
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
        if (x <= boundary.getMinX()) {
            return BoundaryEdge.LEFT;
        } else if (x + width >= boundary.getMaxX()) {
            return BoundaryEdge.RIGHT;
        } else if (y <= boundary.getMinY()) {
            return BoundaryEdge.TOP;
        } else if (y + height >= boundary.getMaxY()) {
            return BoundaryEdge.BOTTOM;
        }
        
        return BoundaryEdge.NONE;
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