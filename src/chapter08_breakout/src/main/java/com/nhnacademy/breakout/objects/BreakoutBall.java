package com.nhnacademy.breakout.objects;

import com.nhnacademy.game.entity.Ball;
import com.nhnacademy.game.collision.Collidable;
import com.nhnacademy.game.collision.Bounds;
import javafx.scene.paint.Color;

/**
 * Breakout 게임의 공
 * Chapter 2~7에서 정의한 Ball 클래스를 상속받아 구현합니다.
 */
public class BreakoutBall extends Ball {
    private static final double DEFAULT_RADIUS = 5;
    private static final Color DEFAULT_COLOR = Color.WHITE;
    private static final double MIN_SPEED = 100;
    private static final double MAX_SPEED = 500;
    
    private boolean isSticky = false;  // 끈끈한 패들에 붙어있는지
    private double speedMultiplier = 1.0;
    
    public BreakoutBall(double x, double y) {
        super(x, y, DEFAULT_RADIUS, DEFAULT_COLOR);
    }
    
    /**
     * 공의 속도를 조정합니다.
     * @param multiplier 속도 배수
     */
    public void adjustSpeed(double multiplier) {
        this.speedMultiplier = multiplier;
        double currentSpeed = Math.sqrt(getVelocityX() * getVelocityX() + 
                                      getVelocityY() * getVelocityY());
        double newSpeed = Math.max(MIN_SPEED, Math.min(MAX_SPEED, currentSpeed * multiplier));
        
        if (currentSpeed > 0) {
            double ratio = newSpeed / currentSpeed;
            setVelocity(getVelocityX() * ratio, getVelocityY() * ratio);
        }
    }
    
    /**
     * 패들과의 특별한 충돌 처리
     * 패들의 어느 부분에 맞았는지에 따라 반사 각도를 조정합니다.
     */
    public void handlePaddleCollision(BreakoutPaddle paddle) {
        // Y 속도 반전
        reverseY();
        
        // 패들 위치에 따른 X 속도 조정
        double hitPosition = (getCenterX() - paddle.getCenterX()) / (paddle.getWidth() / 2);
        hitPosition = Math.max(-1, Math.min(1, hitPosition)); // -1 ~ 1 범위로 제한
        
        // 현재 속도
        double speed = Math.sqrt(getVelocityX() * getVelocityX() + 
                               getVelocityY() * getVelocityY());
        
        // 새로운 각도 계산 (최대 60도)
        double angle = hitPosition * Math.PI / 3;
        
        // 새로운 속도 설정
        setVelocity(Math.sin(angle) * speed, -Math.abs(Math.cos(angle) * speed));
        
        // 끈끈한 패들 처리
        if (paddle.isSticky() && !isSticky) {
            isSticky = true;
            setVelocity(0, 0);
            setPosition(getCenterX(), paddle.getY() - getRadius());
        }
    }
    
    /**
     * 공을 발사합니다 (끈끈한 패들에서)
     */
    public void launch() {
        if (isSticky) {
            isSticky = false;
            setVelocity(150, -150);
        }
    }
    
    // Getters and Setters
    public boolean isSticky() { return isSticky; }
    public void setSticky(boolean sticky) { isSticky = sticky; }
    public double getSpeedMultiplier() { return speedMultiplier; }
}