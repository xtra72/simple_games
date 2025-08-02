package com.nhnacademy.breakout.objects;

import com.nhnacademy.game.entity.Box;
import com.nhnacademy.game.collision.Collidable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Breakout 게임의 패들
 * Chapter 2~7에서 정의한 Box 클래스를 상속받아 구현합니다.
 */
public class BreakoutPaddle extends Box {
    private static final double DEFAULT_WIDTH = 100;
    private static final double DEFAULT_HEIGHT = 10;
    private static final Color DEFAULT_COLOR = Color.WHITE;
    private static final double MOVE_SPEED = 400;
    
    private double normalWidth;
    private boolean isSticky = false;
    private boolean hasLaser = false;
    private double powerUpTimer = 0;
    
    public BreakoutPaddle(double x, double y) {
        super(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_COLOR);
        this.normalWidth = DEFAULT_WIDTH;
        setVelocity(0, 0); // 패들은 기본적으로 정지
    }
    
    /**
     * 패들을 왼쪽으로 이동시킵니다.
     * @param deltaTime 프레임 간 경과 시간
     */
    public void moveLeft(double deltaTime) {
        setVelocity(-MOVE_SPEED, 0);
        move(deltaTime);
        setVelocity(0, 0); // 이동 후 정지
    }
    
    /**
     * 패들을 오른쪽으로 이동시킵니다.
     * @param deltaTime 프레임 간 경과 시간
     */
    public void moveRight(double deltaTime) {
        setVelocity(MOVE_SPEED, 0);
        move(deltaTime);
        setVelocity(0, 0); // 이동 후 정지
    }
    
    /**
     * 경계 내에서만 이동하도록 제한합니다.
     * @param minX 최소 X 좌표
     * @param maxX 최대 X 좌표
     */
    public void constrainToBounds(double minX, double maxX) {
        if (getX() < minX) {
            setX(minX);
        } else if (getX() + getWidth() > maxX) {
            setX(maxX - getWidth());
        }
    }
    
    /**
     * 파워업을 적용합니다.
     * @param type 파워업 타입
     * @param duration 지속 시간
     */
    public void applyPowerUp(PowerUpType type, double duration) {
        switch (type) {
            case WIDER_PADDLE:
                setWidth(normalWidth * 1.5);
                break;
            case STICKY_PADDLE:
                isSticky = true;
                break;
            case LASER:
                hasLaser = true;
                break;
        }
        powerUpTimer = duration;
    }
    
    /**
     * 파워업 타이머를 업데이트합니다.
     * @param deltaTime 경과 시간
     */
    public void updatePowerUps(double deltaTime) {
        if (powerUpTimer > 0) {
            powerUpTimer -= deltaTime;
            if (powerUpTimer <= 0) {
                resetPowerUps();
            }
        }
    }
    
    /**
     * 모든 파워업을 초기화합니다.
     */
    private void resetPowerUps() {
        setWidth(normalWidth);
        isSticky = false;
        hasLaser = false;
        powerUpTimer = 0;
    }
    
    @Override
    public void draw(GraphicsContext gc) {
        super.draw(gc);
        
        // 파워업 상태 표시
        if (isSticky) {
            // 끈끈한 패들은 약간 다른 색상
            gc.setFill(Color.LIGHTGREEN);
            gc.fillRect(getX(), getY() - 2, getWidth(), 2);
        }
        
        if (hasLaser) {
            // 레이저 발사대 그리기
            gc.setFill(Color.RED);
            gc.fillRect(getX() + 10, getY() - 5, 5, 5);
            gc.fillRect(getX() + getWidth() - 15, getY() - 5, 5, 5);
        }
    }
    
    @Override
    public void handleCollision(Collidable other) {
        // 패들은 충돌해도 움직이지 않음
        // 공과의 충돌은 공 쪽에서 처리
    }
    
    // 파워업 타입
    public enum PowerUpType {
        WIDER_PADDLE,
        STICKY_PADDLE,
        LASER
    }
    
    // Getters
    public boolean isSticky() { return isSticky; }
    public boolean hasLaser() { return hasLaser; }
    public double getPowerUpTimer() { return powerUpTimer; }
}