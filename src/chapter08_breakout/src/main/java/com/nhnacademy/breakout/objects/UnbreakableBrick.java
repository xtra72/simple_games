package com.nhnacademy.breakout.objects;

import com.nhnacademy.game.entity.StaticObject;
import com.nhnacademy.game.collision.Collidable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

/**
 * 깨지지 않는 벽돌 (벽)
 * 게임 공간의 경계를 정의하는데 사용됩니다.
 */
public class UnbreakableBrick extends StaticObject {
    private static final Color WALL_COLOR = Color.DARKGRAY;
    
    public UnbreakableBrick(double x, double y, double width, double height) {
        super(x, y, width, height, WALL_COLOR);
        setFilled(true);
    }
    
    @Override
    public void handleCollision(Collidable other) {
        // 깨지지 않는 벽돌은 충돌해도 변화 없음
        // 공이 튕겨나가는 것은 공 쪽에서 처리
    }
    
    @Override
    public void draw(GraphicsContext gc) {
        // 그라데이션으로 벽돌 효과
        LinearGradient gradient = new LinearGradient(
            0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.LIGHTGRAY),
            new Stop(0.5, WALL_COLOR),
            new Stop(1, Color.DARKGRAY.darker())
        );
        
        gc.setFill(gradient);
        gc.fillRect(getX(), getY(), getWidth(), getHeight());
        
        // 테두리
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(getX(), getY(), getWidth(), getHeight());
        
        // 벽돌 패턴
        drawBrickPattern(gc);
    }
    
    /**
     * 벽돌 패턴을 그립니다.
     */
    private void drawBrickPattern(GraphicsContext gc) {
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(0.5);
        
        // 수평선
        double brickHeight = 10;
        for (double y = getY() + brickHeight; y < getY() + getHeight(); y += brickHeight) {
            gc.strokeLine(getX(), y, getX() + getWidth(), y);
        }
        
        // 수직선 (엇갈리게)
        double brickWidth = 20;
        boolean offset = false;
        for (double y = getY(); y < getY() + getHeight(); y += brickHeight) {
            double startX = offset ? getX() + brickWidth / 2 : getX();
            for (double x = startX; x < getX() + getWidth(); x += brickWidth) {
                gc.strokeLine(x, y, x, Math.min(y + brickHeight, getY() + getHeight()));
            }
            offset = !offset;
        }
    }
    
    /**
     * 게임 영역의 벽을 생성하는 정적 팩토리 메서드
     */
    public static class WallFactory {
        public static UnbreakableBrick createTopWall(double width, double thickness) {
            return new UnbreakableBrick(0, 0, width, thickness);
        }
        
        public static UnbreakableBrick createLeftWall(double height, double thickness) {
            return new UnbreakableBrick(0, 0, thickness, height);
        }
        
        public static UnbreakableBrick createRightWall(double x, double height, double thickness) {
            return new UnbreakableBrick(x - thickness, 0, thickness, height);
        }
        
        public static UnbreakableBrick createBottomWall(double y, double width, double thickness) {
            return new UnbreakableBrick(0, y - thickness, width, thickness);
        }
    }
}