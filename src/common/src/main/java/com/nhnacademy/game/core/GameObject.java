package com.nhnacademy.game.core;

import javafx.scene.canvas.GraphicsContext;
import com.nhnacademy.game.graphics.Renderable;

/**
 * 모든 게임 객체의 기본 추상 클래스
 * 위치와 크기 정보를 관리합니다.
 * 필요에 따라 Movable, Collidable, Boundable 인터페이스를 구현합니다.
 */
public abstract class GameObject implements Renderable {
    protected double x, y;
    protected double width, height;
    
    public GameObject(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    /**
     * 객체를 화면에 그립니다.
     * @param gc GraphicsContext
     */
    @Override
    public abstract void draw(GraphicsContext gc);
    
    /**
     * 객체의 상태를 업데이트합니다.
     * 하위 클래스에서 필요에 따라 오버라이드합니다.
     * @param deltaTime 프레임 간 경과 시간 (초)
     */
    public void update(double deltaTime) {
        // 기본 구현은 비어있음
        // Movable 인터페이스를 구현한 클래스에서 오버라이드
    }
    
    /**
     * 객체의 위치를 설정합니다.
     * @param x X 좌표
     * @param y Y 좌표
     */
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    
    // Setters
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setWidth(double width) { this.width = width; }
    public void setHeight(double height) { this.height = height; }
    
    // 유틸리티 메서드
    public double getCenterX() { return x + width / 2; }
    public double getCenterY() { return y + height / 2; }
}