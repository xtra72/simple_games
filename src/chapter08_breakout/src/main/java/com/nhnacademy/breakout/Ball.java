package com.nhnacademy.breakout;

import com.nhnacademy.game.entity.Ball;
import javafx.scene.paint.Color;

/**
 * Breakout 게임의 공 클래스
 * 공통 Ball 클래스를 상속받아 Breakout 게임에 특화됩니다.
 */
public class Ball extends com.nhnacademy.game.entity.Ball {
    
    public Ball(double x, double y, double radius) {
        super(x, y, radius, Color.WHITE);
    }
}