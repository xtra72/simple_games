package com.nhnacademy.game.behavior;

/**
 * 여러 번 충격을 받아야 파괴되는 객체의 인터페이스
 */
public interface MultiHit extends Breakable {
    /**
     * 최대 체력을 반환합니다.
     * @return 최대 체력
     */
    int getMaxHits();
    
    /**
     * 현재 남은 체력을 반환합니다.
     * @return 현재 체력
     */
    int getCurrentHits();
    
    /**
     * 체력에 따른 시각적 상태를 반환합니다.
     * 예: 100% -> PERFECT, 50% -> DAMAGED, 25% -> CRITICAL
     * @return 상태
     */
    DamageState getDamageState();
    
    /**
     * 피해 상태를 나타내는 열거형
     */
    enum DamageState {
        PERFECT(1.0f),
        SLIGHTLY_DAMAGED(0.75f),
        DAMAGED(0.5f),
        HEAVILY_DAMAGED(0.25f),
        CRITICAL(0.1f);
        
        private final float healthRatio;
        
        DamageState(float healthRatio) {
            this.healthRatio = healthRatio;
        }
        
        public float getHealthRatio() {
            return healthRatio;
        }
        
        public static DamageState fromHealthRatio(float ratio) {
            for (DamageState state : values()) {
                if (ratio >= state.healthRatio) {
                    return state;
                }
            }
            return CRITICAL;
        }
    }
}