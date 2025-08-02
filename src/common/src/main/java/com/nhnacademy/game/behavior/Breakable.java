package com.nhnacademy.game.behavior;

/**
 * 파괴 가능한 객체의 인터페이스
 */
public interface Breakable {
    /**
     * 객체가 충격을 받았을 때 호출됩니다.
     * @param damage 받은 피해량
     */
    void hit(int damage);
    
    /**
     * 객체가 파괴되었는지 확인합니다.
     * @return 파괴 여부
     */
    boolean isBroken();
    
    /**
     * 객체가 파괴될 때 제공하는 점수를 반환합니다.
     * @return 점수
     */
    int getPoints();
}