package com.nhnacademy.breakout;

/**
 * 게임 상태를 나타내는 열거형
 */
public enum GameState {
    READY("준비"),
    PLAYING("플레이 중"),
    PAUSED("일시정지"),
    GAME_OVER("게임 오버"),
    LEVEL_COMPLETE("레벨 완료");
    
    private final String description;
    
    GameState(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}