package com.nhnacademy.game.collision;

/**
 * 객체의 경계 영역을 나타내는 클래스
 */
public class Bounds {
    private double x, y, width, height;
    
    public Bounds(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    /**
     * 다른 경계와의 충돌을 검사합니다.
     * @param other 다른 경계
     * @return 충돌 여부
     */
    public boolean intersects(Bounds other) {
        return x < other.x + other.width &&
               x + width > other.x &&
               y < other.y + other.height &&
               y + height > other.y;
    }
    
    /**
     * 원형 경계와의 충돌을 검사합니다.
     * @param centerX 원의 중심 X
     * @param centerY 원의 중심 Y
     * @param radius 원의 반지름
     * @return 충돌 여부
     */
    public boolean intersectsCircle(double centerX, double centerY, double radius) {
        // 원의 중심에서 사각형까지의 최단 거리 계산
        double closestX = Math.max(x, Math.min(centerX, x + width));
        double closestY = Math.max(y, Math.min(centerY, y + height));
        
        // 거리 계산
        double distanceX = centerX - closestX;
        double distanceY = centerY - closestY;
        double distanceSquared = distanceX * distanceX + distanceY * distanceY;
        
        return distanceSquared <= radius * radius;
    }
    
    // Getters and Setters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setWidth(double width) { this.width = width; }
    public void setHeight(double height) { this.height = height; }
    
    public double getMinX() { return x; }
    public double getMaxX() { return x + width; }
    public double getMinY() { return y; }
    public double getMaxY() { return y + height; }
    
    public double getCenterX() { return x + width / 2; }
    public double getCenterY() { return y + height / 2; }
}