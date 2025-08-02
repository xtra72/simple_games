package com.nhnacademy.cannon;

/**
 * 게임 상태를 나타내는 열거형
 */
public enum GameState {
    MENU("메뉴"),
    PLAYING("플레이 중"),
    PAUSED("일시정지"),
    GAME_OVER("게임 오버"),
    VICTORY("승리");
    
    private final String description;
    
    GameState(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}