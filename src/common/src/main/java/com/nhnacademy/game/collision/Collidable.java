package com.nhnacademy.game.collision;

/**
 * 충돌 가능한 객체의 인터페이스
 * 다른 객체와의 충돌을 검사하고 처리할 수 있는 객체를 정의합니다.
 */
public interface Collidable {
    /**
     * 다른 Collidable 객체와의 충돌을 검사합니다.
     * @param other 충돌을 검사할 다른 객체
     * @return 충돌 여부
     */
    boolean collidesWith(Collidable other);
    
    /**
     * 충돌이 발생했을 때의 처리를 정의합니다.
     * @param other 충돌한 다른 객체
     */
    void handleCollision(Collidable other);
    
    /**
     * 객체의 경계 영역을 반환합니다.
     * @return 경계 영역
     */
    Bounds getBounds();
}