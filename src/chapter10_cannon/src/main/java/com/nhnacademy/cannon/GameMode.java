package com.nhnacademy.cannon;

/**
 * 게임 모드를 나타내는 열거형
 */
public enum GameMode {
    CLASSIC("클래식", "제한된 탄약으로 모든 타겟 파괴"),
    TIME_ATTACK("타임 어택", "무제한 탄약으로 최고 점수 도전"),
    SURVIVAL("서바이벌", "웨이브 기반 생존 모드"),
    PUZZLE("퍼즐", "전략적 사고가 필요한 퍼즐 모드");
    
    private final String displayName;
    private final String description;
    
    GameMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}