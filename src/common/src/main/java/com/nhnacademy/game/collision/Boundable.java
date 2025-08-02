package com.nhnacademy.game.collision;

/**
 * 경계를 가진 객체의 인터페이스
 * 특정 영역 내에서만 움직일 수 있는 객체를 정의합니다.
 */
public interface Boundable {
    /**
     * 객체가 경계 내에 있는지 확인합니다.
     * @param boundary 경계 영역
     * @return 경계 내에 있으면 true
     */
    boolean isInBounds(Bounds boundary);
    
    /**
     * 경계와 충돌했을 때의 처리를 정의합니다.
     * @param boundary 경계 영역
     */
    void handleBoundaryCollision(Bounds boundary);
    
    /**
     * 객체의 현재 위치가 경계의 어느 면과 충돌했는지 확인합니다.
     * @param boundary 경계 영역
     * @return 충돌한 면 (LEFT, RIGHT, TOP, BOTTOM, NONE)
     */
    BoundaryEdge checkBoundaryEdge(Bounds boundary);
    
    /**
     * 경계 면을 나타내는 열거형
     */
    enum BoundaryEdge {
        LEFT, RIGHT, TOP, BOTTOM, NONE
    }
}