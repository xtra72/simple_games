package com.nhnacademy.breakout;

import com.nhnacademy.game.entity.Box;
import com.nhnacademy.game.collision.Collidable;
import javafx.scene.paint.Color;

/**
 * Breakout 게임의 패들 클래스
 * Box를 상속받아 플레이어가 조작하는 패들을 구현합니다.
 * 패들은 좌우로만 이동하므로 Y축 속도는 항상 0입니다.
 */
public class Paddle extends Box {
    
    public Paddle(double x, double y, double width, double height) {
        super(x, y, width, height, Color.WHITE);
        // 패들은 정적이므로 기본 속도는 0
        setVelocity(0, 0);
    }
    
    /**
     * 패들을 이동시킵니다.
     * @param dx X축 이동 거리
     */
    public void move(double dx) {
        setX(getX() + dx);
    }
    
    /**
     * 패들은 공과 충돌할 때 특별한 처리를 합니다.
     * 공의 각도를 패들 충돌 위치에 따라 조정합니다.
     */
    @Override
    public void handleCollision(Collidable other) {
        // 패들은 충돌해도 움직이지 않음
        // 공의 각도 조정은 공 클래스에서 처리
    }
}