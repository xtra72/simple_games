package com.nhnacademy.breakout;

import com.nhnacademy.game.entity.StaticObject;
import com.nhnacademy.game.collision.Collidable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Breakout 게임의 벽돌 클래스
 * StaticObject를 상속받아 파괴 가능한 벽돌을 구현합니다.
 */
public class Brick extends StaticObject {
    private int points;
    private int hits;
    private int maxHits;
    
    public Brick(double x, double y, double width, double height, Color color, int points) {
        super(x, y, width, height, color);
        this.points = points;
        this.hits = 0;
        this.maxHits = 1;
    }
    
    /**
     * 벽돌이 공과 충돌했을 때 호출됩니다.
     */
    public void hit() {
        hits++;
    }
    
    /**
     * 벽돌이 파괴되었는지 확인합니다.
     * @return 파괴 여부
     */
    public boolean isDestroyed() {
        return hits >= maxHits;
    }
    
    @Override
    public void draw(GraphicsContext gc) {
        super.draw(gc);
        // 벽돌에 테두리 추가
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(x, y, width, height);
    }
    
    @Override
    public void handleCollision(Collidable other) {
        // 벽돌은 충돌했을 때 히트 카운트 증가
        hit();
    }
    
    public int getPoints() { 
        return points; 
    }
    
    public void setMaxHits(int maxHits) {
        this.maxHits = maxHits;
    }
    
    public int getHits() {
        return hits;
    }
}