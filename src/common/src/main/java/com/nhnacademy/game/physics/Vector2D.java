package com.nhnacademy.game.physics;

/**
 * 2D 벡터 클래스
 * 물리 연산을 위한 벡터 연산을 제공합니다.
 */
public class Vector2D {
    public double x, y;
    
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * 벡터 복사 생성자
     * @param other 복사할 벡터
     */
    public Vector2D(Vector2D other) {
        this.x = other.x;
        this.y = other.y;
    }
    
    /**
     * 다른 벡터를 더합니다.
     * @param other 더할 벡터
     */
    public void add(Vector2D other) {
        x += other.x;
        y += other.y;
    }
    
    /**
     * 다른 벡터를 뺍니다.
     * @param other 뺄 벡터
     */
    public void subtract(Vector2D other) {
        x -= other.x;
        y -= other.y;
    }
    
    /**
     * 벡터에 스칼라 값을 곱합니다.
     * @param factor 곱할 값
     */
    public void scale(double factor) {
        x *= factor;
        y *= factor;
    }
    
    /**
     * 벡터의 크기를 반환합니다.
     * @return 벡터의 크기
     */
    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }
    
    /**
     * 벡터를 정규화합니다 (크기를 1로 만듭니다).
     */
    public void normalize() {
        double mag = magnitude();
        if (mag > 0) {
            x /= mag;
            y /= mag;
        }
    }
    
    /**
     * 두 벡터의 내적을 계산합니다.
     * @param other 내적을 계산할 벡터
     * @return 내적 값
     */
    public double dot(Vector2D other) {
        return x * other.x + y * other.y;
    }
    
    /**
     * 두 벡터 사이의 거리를 계산합니다.
     * @param other 거리를 계산할 벡터
     * @return 거리
     */
    public double distance(Vector2D other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * 벡터를 주어진 각도만큼 회전시킵니다.
     * @param angle 회전 각도 (라디안)
     */
    public void rotate(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double newX = x * cos - y * sin;
        double newY = x * sin + y * cos;
        x = newX;
        y = newY;
    }
    
    /**
     * 벡터의 복사본을 생성합니다.
     * @return 새로운 벡터 객체
     */
    public Vector2D copy() {
        return new Vector2D(x, y);
    }
    
    /**
     * 두 벡터를 더한 새로운 벡터를 반환합니다.
     * @param a 첫 번째 벡터
     * @param b 두 번째 벡터
     * @return 더한 결과 벡터
     */
    public static Vector2D add(Vector2D a, Vector2D b) {
        return new Vector2D(a.x + b.x, a.y + b.y);
    }
    
    /**
     * 두 벡터를 뺀 새로운 벡터를 반환합니다.
     * @param a 첫 번째 벡터
     * @param b 두 번째 벡터
     * @return 뺀 결과 벡터
     */
    public static Vector2D subtract(Vector2D a, Vector2D b) {
        return new Vector2D(a.x - b.x, a.y - b.y);
    }
    
    @Override
    public String toString() {
        return String.format("Vector2D(%.2f, %.2f)", x, y);
    }
}