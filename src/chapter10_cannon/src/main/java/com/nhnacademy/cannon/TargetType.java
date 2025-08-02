package com.nhnacademy.cannon;

import javafx.scene.paint.Color;

/**
 * 타겟 타입을 나타내는 열거형
 */
public enum TargetType {
    STATIC("정적", Color.RED, 100, 100),
    MOVING("이동", Color.ORANGE, 100, 150),
    FLYING("비행", Color.LIGHTBLUE, 75, 200),
    ARMORED("장갑", Color.DARKGRAY, 200, 250),
    SPECIAL("특별", Color.GOLD, 100, 500);
    
    private final String displayName;
    private final Color color;
    private final int defaultHealth;
    private final int defaultPoints;
    
    TargetType(String displayName, Color color, int defaultHealth, int defaultPoints) {
        this.displayName = displayName;
        this.color = color;
        this.defaultHealth = defaultHealth;
        this.defaultPoints = defaultPoints;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public Color getColor() {
        return color;
    }
    
    public int getDefaultHealth() {
        return defaultHealth;
    }
    
    public int getDefaultPoints() {
        return defaultPoints;
    }
}