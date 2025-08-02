package com.nhnacademy.cannon;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 대포 클래스
 */
public class Cannon {
    private double x, y;
    private double angle;
    private ProjectileType currentType = ProjectileType.STANDARD;
    
    public Cannon(double x, double y) {
        this.x = x;
        this.y = y;
        this.angle = -Math.PI / 4; // 기본 45도 각도
    }
    
    /**
     * 목표 지점을 향해 조준합니다.
     * @param targetX 목표 X 좌표
     * @param targetY 목표 Y 좌표
     */
    public void aim(double targetX, double targetY) {
        angle = Math.atan2(targetY - y, targetX - x);
    }
    
    /**
     * 발사체를 발사합니다.
     * @param angle 발사 각도
     * @param power 발사 힘
     * @return 생성된 발사체
     */
    public Projectile fire(double angle, double power) {
        // 대포 끝 위치 계산
        double tipX = x + Math.cos(angle) * 50;
        double tipY = y + Math.sin(angle) * 50;
        
        // 발사체 크기 설정
        double radius = currentType == ProjectileType.STANDARD ? 5 : 7;
        Projectile projectile = new Projectile(tipX, tipY, radius, currentType);
        
        // 속도 설정
        double vx = Math.cos(angle) * power;
        double vy = Math.sin(angle) * power;
        projectile.setVelocity(vx, vy);
        
        return projectile;
    }
    
    /**
     * 대포를 화면에 그립니다.
     * @param gc GraphicsContext
     */
    public void draw(GraphicsContext gc) {
        // 대포 받침대
        gc.setFill(Color.DARKGRAY);
        gc.fillRect(x - 30, y - 10, 60, 20);
        
        // 대포 포신
        gc.save();
        gc.translate(x, y);
        gc.rotate(Math.toDegrees(angle));
        gc.setFill(Color.BLACK);
        gc.fillRect(0, -5, 50, 10);
        gc.restore();
        
        // 바퀴
        gc.setFill(Color.BLACK);
        gc.fillOval(x - 25, y + 5, 20, 20);
        gc.fillOval(x + 5, y + 5, 20, 20);
    }
    
    // Getters and Setters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getAngle() { return angle; }
    
    public ProjectileType getProjectileType() {
        return currentType;
    }
    
    public void setProjectileType(ProjectileType type) {
        this.currentType = type;
    }
}