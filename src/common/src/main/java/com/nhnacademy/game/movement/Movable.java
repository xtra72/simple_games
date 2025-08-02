package com.nhnacademy.game.movement;

/**
 * 이동 가능한 객체의 인터페이스
 * 속도를 가지고 시간에 따라 위치가 변경되는 객체를 정의합니다.
 */
public interface Movable {
    /**
     * 객체의 위치를 업데이트합니다.
     * @param deltaTime 프레임 간 경과 시간 (초)
     */
    void move(double deltaTime);
    
    /**
     * X축 속도를 반환합니다.
     * @return X축 속도
     */
    double getVelocityX();
    
    /**
     * Y축 속도를 반환합니다.
     * @return Y축 속도
     */
    double getVelocityY();
    
    /**
     * 속도를 설정합니다.
     * @param vx X축 속도
     * @param vy Y축 속도
     */
    void setVelocity(double vx, double vy);
    
    /**
     * X축 속도를 반전시킵니다.
     */
    void reverseX();
    
    /**
     * Y축 속도를 반전시킵니다.
     */
    void reverseY();
}