package com.nhnacademy.breakout.objects;

import com.nhnacademy.game.entity.StaticObject;
import com.nhnacademy.game.behavior.Breakable;
import com.nhnacademy.game.collision.Collidable;
import javafx.scene.paint.Color;

/**
 * 기본 벽돌 클래스
 * 한 번 맞으면 깨지는 단순한 벽돌입니다.
 */
public class SimpleBrick extends StaticObject implements Breakable {
    private int points;
    private boolean broken = false;
    
    public SimpleBrick(double x, double y, double width, double height, Color color, int points) {
        super(x, y, width, height, color);
        this.points = points;
    }
    
    @Override
    public void hit(int damage) {
        broken = true;
    }
    
    @Override
    public boolean isBroken() {
        return broken;
    }
    
    @Override
    public int getPoints() {
        return points;
    }
    
    @Override
    public void handleCollision(Collidable other) {
        if (other instanceof BreakoutBall) {
            hit(1);
        }
    }
}