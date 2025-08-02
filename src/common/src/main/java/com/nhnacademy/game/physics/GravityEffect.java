package com.nhnacademy.game.physics;

import com.nhnacademy.game.core.GameObject;

/**
 * 중력 효과를 구현하는 클래스
 */
public class GravityEffect implements Effect {
    private double x, y, width, height;
    private double force;
    
    /**
     * 중력 효과를 생성합니다.
     * @param x 효과 영역의 X 좌표
     * @param y 효과 영역의 Y 좌표
     * @param width 효과 영역의 너비
     * @param height 효과 영역의 높이
     * @param force 중력의 세기 (양수: 아래 방향, 음수: 위 방향)
     */
    public GravityEffect(double x, double y, double width, double height, double force) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.force = force;
    }
    
    @Override
    public void apply(GameObject object, double deltaTime) {
        // 객체가 효과 영역 내에 있는지 확인
        if (object.getX() >= x && object.getX() <= x + width &&
            object.getY() >= y && object.getY() <= y + height) {
            // Y축 속도에 중력 가속도를 적용
            object.setVelocity(object.getVx(), object.getVy() + force * deltaTime);
        }
    }
    
    // Getters and Setters
    public double getForce() { return force; }
    public void setForce(double force) { this.force = force; }
}