package com.nhnacademy.game.physics;

import com.nhnacademy.game.core.GameObject;

/**
 * 게임 객체에 적용될 수 있는 물리 효과의 인터페이스
 */
public interface Effect {
    /**
     * 게임 객체에 효과를 적용합니다.
     * @param object 효과를 적용할 객체
     * @param deltaTime 프레임 간 경과 시간 (초)
     */
    void apply(GameObject object, double deltaTime);
}