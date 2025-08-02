package com.nhnacademy.cannon;

import javafx.scene.paint.Color;

/**
 * 발사체 타입을 나타내는 열거형
 */
public enum ProjectileType {
    STANDARD("표준", Color.BLACK, 50),
    EXPLOSIVE("폭발", Color.RED, 100),
    PIERCING("관통", Color.BLUE, 75),
    SPLIT("분열", Color.PURPLE, 50);
    
    private final String displayName;
    private final Color color;
    private final int baseDamage;
    
    ProjectileType(String displayName, Color color, int baseDamage) {
        this.displayName = displayName;
        this.color = color;
        this.baseDamage = baseDamage;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public Color getColor() {
        return color;
    }
    
    public int getBaseDamage() {
        return baseDamage;
    }
}