package com.nhnacademy.game.physics;

import com.nhnacademy.game.core.GameObject;

/**
 * 바람 효과를 구현하는 클래스
 */
public class WindEffect implements Effect {
    private double x, y, width, height;
    private double forceX, forceY;
    
    /**
     * 바람 효과를 생성합니다.
     * @param x 효과 영역의 X 좌표
     * @param y 효과 영역의 Y 좌표
     * @param width 효과 영역의 너비
     * @param height 효과 영역의 높이
     * @param forceX X축 방향 바람의 세기
     * @param forceY Y축 방향 바람의 세기
     */
    public WindEffect(double x, double y, double width, double height, double forceX, double forceY) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.forceX = forceX;
        this.forceY = forceY;
    }
    
    @Override
    public void apply(GameObject object, double deltaTime) {
        // 객체가 효과 영역 내에 있는지 확인
        if (object.getX() >= x && object.getX() <= x + width &&
            object.getY() >= y && object.getY() <= y + height) {
            // 바람의 힘을 속도에 적용
            object.setVelocity(
                object.getVx() + forceX * deltaTime,
                object.getVy() + forceY * deltaTime
            );
        }
    }
    
    // Getters and Setters
    public double getForceX() { return forceX; }
    public double getForceY() { return forceY; }
    public void setForceX(double forceX) { this.forceX = forceX; }
    public void setForceY(double forceY) { this.forceY = forceY; }
}