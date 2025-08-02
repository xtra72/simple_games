package com.nhnacademy.breakout.world;

import com.nhnacademy.game.entity.Ball;
import com.nhnacademy.game.behavior.PowerUpProvider;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 파워업 아이템
 * Ball을 상속받아 떨어지는 파워업을 구현합니다.
 */
public class PowerUp extends Ball {
    private PowerUpProvider.PowerUpType type;
    private static final double RADIUS = 15;
    private static final double FALL_SPEED = 100;
    
    public PowerUp(double x, double y, PowerUpProvider.PowerUpType type) {
        super(x, y, RADIUS, getColorForType(type));
        this.type = type;
        setVelocity(0, FALL_SPEED);
    }
    
    /**
     * 파워업 타입에 따른 색상을 반환합니다.
     */
    private static Color getColorForType(PowerUpProvider.PowerUpType type) {
        switch (type) {
            case WIDER_PADDLE:
                return Color.BLUE;
            case MULTI_BALL:
                return Color.ORANGE;
            case EXTRA_LIFE:
                return Color.GREEN;
            case LASER:
                return Color.RED;
            case SLOW_BALL:
                return Color.CYAN;
            case STICKY_PADDLE:
                return Color.PURPLE;
            default:
                return Color.YELLOW;
        }
    }
    
    @Override
    public void draw(GraphicsContext gc) {
        super.draw(gc);
        
        // 파워업 아이콘
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font(12));
        String icon = getIconForType(type);
        gc.fillText(icon, getCenterX() - 8, getCenterY() + 4);
        
        // 테두리
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeOval(getX(), getY(), getWidth(), getHeight());
    }
    
    /**
     * 파워업 타입에 따른 아이콘을 반환합니다.
     */
    private String getIconForType(PowerUpProvider.PowerUpType type) {
        switch (type) {
            case WIDER_PADDLE:
                return "W";
            case MULTI_BALL:
                return "M";
            case EXTRA_LIFE:
                return "+1";
            case LASER:
                return "L";
            case SLOW_BALL:
                return "S";
            case STICKY_PADDLE:
                return "G";
            default:
                return "?";
        }
    }
    
    public PowerUpProvider.PowerUpType getType() {
        return type;
    }
}