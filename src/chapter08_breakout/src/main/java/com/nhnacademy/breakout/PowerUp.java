package com.nhnacademy.breakout;

import com.nhnacademy.game.entity.Ball;
import com.nhnacademy.game.collision.Collidable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Random;

/**
 * Breakout 게임의 파워업 클래스
 * Ball을 상속받아 떨어지는 파워업을 구현합니다.
 */
public class PowerUp extends Ball {
    
    public enum Type {
        WIDER_PADDLE("패들 확장", Color.BLUE),
        EXTRA_LIFE("생명 추가", Color.GREEN),
        MULTI_BALL("멀티볼", Color.ORANGE),
        SLOW_BALL("볼 감속", Color.CYAN),
        LASER("레이저", Color.RED);
        
        private final String description;
        private final Color color;
        
        Type(String description, Color color) {
            this.description = description;
            this.color = color;
        }
        
        public String getDescription() { return description; }
        public Color getColor() { return color; }
    }
    
    private Type type;
    private static final double RADIUS = 10;
    private static final double FALL_SPEED = 100;
    
    public PowerUp(double x, double y) {
        super(x, y, RADIUS);
        
        // 랜덤 파워업 타입 선택
        Type[] types = Type.values();
        type = types[new Random().nextInt(types.length)];
        setColor(type.getColor());
        
        // 낙하 속도 설정
        setVelocity(0, FALL_SPEED);
    }
    
    public PowerUp(double x, double y, Type type) {
        super(x, y, RADIUS, type.getColor());
        this.type = type;
        setVelocity(0, FALL_SPEED);
    }
    
    @Override
    public void draw(GraphicsContext gc) {
        super.draw(gc);
        
        // 테두리 그리기
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeOval(getX(), getY(), getWidth(), getHeight());
        
        // 타입 표시 (첫 글자)
        gc.setFill(Color.WHITE);
        gc.fillText(type.name().substring(0, 1), getCenterX() - 4, getCenterY() + 4);
    }
    
    @Override
    public void handleCollision(Collidable other) {
        // 파워업은 패들과 충돌하면 사라짐
        // 실제 처리는 게임 로직에서 수행
    }
    
    public Type getType() { 
        return type; 
    }
}