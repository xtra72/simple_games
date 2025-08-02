package com.nhnacademy.game.graphics;

import javafx.scene.canvas.GraphicsContext;

/**
 * 화면에 렌더링 가능한 객체의 인터페이스
 */
public interface Renderable {
    /**
     * 객체를 화면에 그립니다.
     * @param gc GraphicsContext
     */
    void draw(GraphicsContext gc);
}