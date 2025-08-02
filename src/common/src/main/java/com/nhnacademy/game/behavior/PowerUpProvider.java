package com.nhnacademy.game.behavior;

/**
 * 파워업을 제공할 수 있는 객체의 인터페이스
 */
public interface PowerUpProvider {
    /**
     * 파워업을 생성할 확률을 반환합니다.
     * @return 0.0 ~ 1.0 사이의 확률
     */
    double getPowerUpChance();
    
    /**
     * 파워업을 생성해야 하는지 결정합니다.
     * @return 파워업 생성 여부
     */
    boolean shouldDropPowerUp();
    
    /**
     * 제공할 파워업 타입을 반환합니다.
     * @return 파워업 타입 (null이면 랜덤)
     */
    PowerUpType getPowerUpType();
    
    /**
     * 파워업 타입을 나타내는 열거형
     */
    enum PowerUpType {
        WIDER_PADDLE("패들 확장", 10.0),
        MULTI_BALL("멀티볼", 5.0),
        EXTRA_LIFE("추가 생명", 3.0),
        LASER("레이저", 7.0),
        SLOW_BALL("볼 감속", 8.0),
        STICKY_PADDLE("끈끈한 패들", 6.0);
        
        private final String description;
        private final double duration;
        
        PowerUpType(String description, double duration) {
            this.description = description;
            this.duration = duration;
        }
        
        public String getDescription() { return description; }
        public double getDuration() { return duration; }
    }
}