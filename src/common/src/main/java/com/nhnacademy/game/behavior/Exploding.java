package com.nhnacademy.game.behavior;

import com.nhnacademy.game.collision.Bounds;
import java.util.List;

/**
 * 폭발하는 객체의 인터페이스
 * 파괴될 때 주변에 영향을 미치는 객체를 정의합니다.
 */
public interface Exploding extends Breakable {
    /**
     * 폭발 반경을 반환합니다.
     * @return 폭발 반경
     */
    double getExplosionRadius();
    
    /**
     * 폭발 피해량을 반환합니다.
     * @return 폭발 피해량
     */
    int getExplosionDamage();
    
    /**
     * 폭발 영향을 받는 영역을 반환합니다.
     * @return 폭발 영역
     */
    Bounds getExplosionBounds();
    
    /**
     * 폭발 효과를 시작합니다.
     * @return 폭발 애니메이션이나 효과 객체 리스트
     */
    List<ExplosionEffect> explode();
    
    /**
     * 폭발 효과를 나타내는 클래스
     */
    class ExplosionEffect {
        private double x, y;
        private double radius;
        private double duration;
        private double currentTime;
        
        public ExplosionEffect(double x, double y, double radius, double duration) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.duration = duration;
            this.currentTime = 0;
        }
        
        public void update(double deltaTime) {
            currentTime += deltaTime;
        }
        
        public boolean isFinished() {
            return currentTime >= duration;
        }
        
        public double getProgress() {
            return Math.min(currentTime / duration, 1.0);
        }
        
        // Getters
        public double getX() { return x; }
        public double getY() { return y; }
        public double getRadius() { return radius; }
        public double getCurrentRadius() { 
            return radius * getProgress(); 
        }
    }
}