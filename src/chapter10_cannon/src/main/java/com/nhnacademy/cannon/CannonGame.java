package com.nhnacademy.cannon;

import com.nhnacademy.game.physics.*;
import com.nhnacademy.game.collision.*;
import com.nhnacademy.game.movement.Movable;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;

import java.util.*;

public class CannonGame extends Application {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 700;
    
    private Canvas canvas;
    private GraphicsContext gc;
    private Cannon cannon;
    private List<Projectile> projectiles;
    private List<Target> targets;
    private List<Effect> effects;
    private Random random = new Random();
    
    private int score = 0;
    private int ammo = 50;
    private GameMode gameMode = GameMode.CLASSIC;
    private GameState gameState = GameState.MENU;
    private int wave = 1;
    private double windForce = 0;
    private double gravity = 300;
    
    @Override
    public void start(Stage primaryStage) {
        canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
        
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);
        
        scene.setOnMouseMoved(e -> {
            if (gameState == GameState.PLAYING && cannon != null) {
                cannon.aim(e.getX(), e.getY());
            }
        });
        
        scene.setOnMousePressed(e -> {
            if (gameState == GameState.PLAYING && ammo > 0) {
                fireProjectile(e);
            } else if (gameState == GameState.MENU) {
                handleMenuClick(e);
            }
        });
        
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE) {
                if (gameState == GameState.PLAYING) {
                    gameState = GameState.PAUSED;
                } else if (gameState == GameState.PAUSED) {
                    gameState = GameState.PLAYING;
                } else if (gameState == GameState.GAME_OVER || gameState == GameState.VICTORY) {
                    gameState = GameState.MENU;
                }
            }
            if (e.getCode() == KeyCode.ESCAPE) {
                gameState = GameState.MENU;
            }
            // Projectile type selection
            if (gameState == GameState.PLAYING && cannon != null) {
                if (e.getCode() == KeyCode.DIGIT1) cannon.setProjectileType(ProjectileType.STANDARD);
                if (e.getCode() == KeyCode.DIGIT2) cannon.setProjectileType(ProjectileType.EXPLOSIVE);
                if (e.getCode() == KeyCode.DIGIT3) cannon.setProjectileType(ProjectileType.PIERCING);
                if (e.getCode() == KeyCode.DIGIT4) cannon.setProjectileType(ProjectileType.SPLIT);
            }
        });
        
        primaryStage.setTitle("Cannon Game - Chapter 10");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        
        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;
            
            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }
                
                double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;
                
                if (gameState == GameState.PLAYING) {
                    update(deltaTime);
                }
                render();
            }
        };
        timer.start();
    }
    
    private void initGame() {
        cannon = new Cannon(100, HEIGHT - 100);
        projectiles = new ArrayList<>();
        targets = new ArrayList<>();
        effects = new ArrayList<>();
        
        // Reset game values based on mode
        switch (gameMode) {
            case CLASSIC:
                ammo = 50;
                createClassicTargets();
                break;
            case TIME_ATTACK:
                ammo = 999;
                createTimeAttackTargets();
                break;
            case SURVIVAL:
                ammo = 30;
                wave = 1;
                createSurvivalWave(wave);
                break;
            case PUZZLE:
                ammo = 10;
                createPuzzleTargets();
                break;
        }
        
        // Random wind
        windForce = (random.nextDouble() - 0.5) * 100;
        
        // Add gravity effect
        effects.add(new GravityEffect(0, 0, WIDTH, HEIGHT, gravity));
        if (Math.abs(windForce) > 0) {
            effects.add(new WindEffect(0, 0, WIDTH, HEIGHT, windForce, 0));
        }
        
        gameState = GameState.PLAYING;
    }
    
    private void createClassicTargets() {
        // Static targets
        for (int i = 0; i < 5; i++) {
            targets.add(new Target(600 + i * 80, HEIGHT - 150, 30, 60, TargetType.STATIC, 100));
        }
        
        // Moving targets
        for (int i = 0; i < 3; i++) {
            Target moving = new Target(700 + i * 100, 300 + i * 50, 40, 40, TargetType.MOVING, 150);
            moving.setVelocity((random.nextDouble() - 0.5) * 100, 0);
            targets.add(moving);
        }
        
        // Flying target
        Target flying = new Target(800, 200, 50, 30, TargetType.FLYING, 200);
        flying.setVelocity(0, -50);
        targets.add(flying);
    }
    
    private void createTimeAttackTargets() {
        // Continuously spawn targets
        for (int i = 0; i < 10; i++) {
            double x = 400 + random.nextDouble() * 500;
            double y = 100 + random.nextDouble() * 400;
            TargetType type = TargetType.values()[random.nextInt(TargetType.values().length)];
            Target target = new Target(x, y, 30 + random.nextInt(30), 30 + random.nextInt(30), type, 50 + type.ordinal() * 50);
            
            if (type == TargetType.MOVING) {
                target.setVelocity((random.nextDouble() - 0.5) * 150, 0);
            } else if (type == TargetType.FLYING) {
                target.setVelocity((random.nextDouble() - 0.5) * 100, (random.nextDouble() - 0.5) * 100);
            }
            
            targets.add(target);
        }
    }
    
    private void createSurvivalWave(int wave) {
        targets.clear();
        int targetCount = 5 + wave * 2;
        
        for (int i = 0; i < targetCount; i++) {
            double x = 400 + random.nextDouble() * 500;
            double y = 100 + random.nextDouble() * 400;
            TargetType type = wave > 3 ? TargetType.ARMORED : 
                             (random.nextBoolean() ? TargetType.STATIC : TargetType.MOVING);
            
            Target target = new Target(x, y, 40, 40, type, 100 * wave);
            
            if (type == TargetType.MOVING) {
                target.setVelocity((random.nextDouble() - 0.5) * 100 * wave, 0);
            }
            
            targets.add(target);
        }
    }
    
    private void createPuzzleTargets() {
        // Create specific puzzle layout
        // Tower structure
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3 - i; j++) {
                targets.add(new Target(700 + j * 40 + i * 20, HEIGHT - 100 - i * 40, 35, 35, TargetType.STATIC, 50));
            }
        }
        
        // Protected target
        Target special = new Target(800, HEIGHT - 200, 50, 50, TargetType.SPECIAL, 500);
        targets.add(special);
    }
    
    private void fireProjectile(MouseEvent e) {
        if (cannon != null && ammo > 0) {
            double angle = Math.atan2(e.getY() - cannon.getY(), e.getX() - cannon.getX());
            double power = Math.min(Math.sqrt(Math.pow(e.getX() - cannon.getX(), 2) + 
                                              Math.pow(e.getY() - cannon.getY(), 2)), 500);
            
            Projectile projectile = cannon.fire(angle, power);
            projectiles.add(projectile);
            ammo--;
            
            // Special projectile effects
            if (projectile.getType() == ProjectileType.SPLIT) {
                // Schedule split after 1 second
                projectile.setSplitTime(1.0);
            }
        }
    }
    
    private void update(double deltaTime) {
        // Update projectiles
        List<Projectile> toRemove = new ArrayList<>();
        List<Projectile> toAdd = new ArrayList<>();
        
        for (Projectile projectile : projectiles) {
            projectile.update(deltaTime);
            
            // Apply effects
            for (Effect effect : effects) {
                effect.apply(projectile, deltaTime);
            }
            
            // Check boundaries
            Bounds gameBounds = new Bounds(-100, -100, WIDTH + 200, HEIGHT + 200);
            if (!projectile.isInBounds(gameBounds)) {
                toRemove.add(projectile);
                continue;
            }
            
            // Handle split projectiles
            if (projectile.getType() == ProjectileType.SPLIT && projectile.shouldSplit(deltaTime)) {
                toRemove.add(projectile);
                // Create 3 smaller projectiles
                for (int i = -1; i <= 1; i++) {
                    Projectile split = new Projectile(projectile.getCenterX(), projectile.getCenterY(), 
                                                     projectile.getRadius() / 2, ProjectileType.STANDARD);
                    double angle = Math.atan2(projectile.getVelocityY(), projectile.getVelocityX()) + i * 0.3;
                    double speed = Math.sqrt(projectile.getVelocityX() * projectile.getVelocityX() + 
                                           projectile.getVelocityY() * projectile.getVelocityY()) * 0.7;
                    split.setVelocity(Math.cos(angle) * speed, Math.sin(angle) * speed);
                    toAdd.add(split);
                }
            }
            
            // Check collisions with targets
            List<Target> targetsHit = new ArrayList<>();
            for (Target target : targets) {
                if (projectile.collidesWith(target)) {
                    targetsHit.add(target);
                    
                    // Handle collision
                    target.handleCollision(projectile);
                    
                    // Handle different projectile types
                    switch (projectile.getType()) {
                        case EXPLOSIVE:
                            // Damage nearby targets
                            for (Target other : targets) {
                                if (other != target) {
                                    double dist = Math.sqrt(Math.pow(other.getCenterX() - target.getCenterX(), 2) + 
                                                          Math.pow(other.getCenterY() - target.getCenterY(), 2));
                                    if (dist < 100) {
                                        other.takeDamage(50);
                                    }
                                }
                            }
                            break;
                        case PIERCING:
                            // Continue through target - don't remove projectile
                            continue;
                        default:
                            break;
                    }
                    
                    if (!projectile.getType().equals(ProjectileType.PIERCING)) {
                        toRemove.add(projectile);
                    }
                    break;
                }
            }
        }
        
        projectiles.removeAll(toRemove);
        projectiles.addAll(toAdd);
        
        // Update targets
        List<Target> destroyedTargets = new ArrayList<>();
        for (Target target : targets) {
            target.update(deltaTime);
            
            // Boundary checks for moving targets
            Bounds targetBounds = new Bounds(0, 0, WIDTH, HEIGHT - 50);
            if (!target.isInBounds(targetBounds)) {
                target.handleBoundaryCollision(targetBounds);
            }
            
            if (target.isDestroyed()) {
                destroyedTargets.add(target);
                score += target.getPoints();
            }
        }
        targets.removeAll(destroyedTargets);
        
        // Check game conditions
        checkGameConditions();
        
        // Spawn new targets in time attack mode
        if (gameMode == GameMode.TIME_ATTACK && targets.size() < 5) {
            createTimeAttackTargets();
        }
    }
    
    private void checkGameConditions() {
        switch (gameMode) {
            case CLASSIC:
            case PUZZLE:
                if (targets.isEmpty()) {
                    gameState = GameState.VICTORY;
                } else if (ammo == 0 && projectiles.isEmpty()) {
                    gameState = GameState.GAME_OVER;
                }
                break;
            case SURVIVAL:
                if (targets.isEmpty()) {
                    wave++;
                    ammo += 10 + wave * 2;
                    createSurvivalWave(wave);
                } else if (ammo == 0 && projectiles.isEmpty()) {
                    gameState = GameState.GAME_OVER;
                }
                break;
            case TIME_ATTACK:
                // Time attack continues until player quits
                break;
        }
    }
    
    private void render() {
        // Clear screen
        gc.setFill(Color.SKYBLUE);
        gc.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Draw ground
        gc.setFill(Color.GREEN.darker());
        gc.fillRect(0, HEIGHT - 50, WIDTH, 50);
        
        if (gameState == GameState.MENU) {
            renderMenu();
        } else {
            // Draw game objects
            if (cannon != null) cannon.draw(gc);
            
            for (Target target : targets) {
                target.draw(gc);
            }
            
            for (Projectile projectile : projectiles) {
                projectile.draw(gc);
            }
            
            // Draw UI
            renderUI();
            
            // Draw game state overlays
            if (gameState == GameState.PAUSED) {
                renderPauseOverlay();
            } else if (gameState == GameState.GAME_OVER) {
                renderGameOverOverlay();
            } else if (gameState == GameState.VICTORY) {
                renderVictoryOverlay();
            }
        }
    }
    
    private void renderMenu() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, WIDTH, HEIGHT);
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(50));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("CANNON GAME", WIDTH / 2, 150);
        
        gc.setFont(Font.font(30));
        gc.fillText("Select Game Mode:", WIDTH / 2, 250);
        
        // Game mode buttons
        GameMode[] modes = GameMode.values();
        for (int i = 0; i < modes.length; i++) {
            gc.setFill(Color.DARKGRAY);
            gc.fillRect(WIDTH / 2 - 100, 300 + i * 60, 200, 50);
            gc.setFill(Color.WHITE);
            gc.fillText(modes[i].getDisplayName(), WIDTH / 2, 330 + i * 60);
        }
        
        gc.setFont(Font.font(20));
        gc.fillText("Controls: Mouse to aim and shoot", WIDTH / 2, 600);
        gc.fillText("1-4: Select projectile type | SPACE: Pause | ESC: Menu", WIDTH / 2, 630);
    }
    
    private void renderUI() {
        // UI Background
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(10, 10, 300, 120);
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(20));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Score: " + score, 20, 35);
        gc.fillText("Ammo: " + ammo, 20, 60);
        gc.fillText("Mode: " + gameMode, 20, 85);
        
        if (gameMode == GameMode.SURVIVAL) {
            gc.fillText("Wave: " + wave, 20, 110);
        }
        
        // Wind indicator
        gc.fillText("Wind: " + String.format("%.1f", windForce), 150, 35);
        
        // Projectile type indicator
        if (cannon != null) {
            gc.fillText("Type: " + cannon.getProjectileType(), 150, 60);
        }
        
        // Projectile type legend
        gc.setFont(Font.font(16));
        gc.fillText("1: Standard  2: Explosive  3: Piercing  4: Split", WIDTH - 350, 30);
    }
    
    private void renderPauseOverlay() {
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillRect(0, 0, WIDTH, HEIGHT);
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(40));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("PAUSED", WIDTH / 2, HEIGHT / 2);
        gc.setFont(Font.font(20));
        gc.fillText("Press SPACE to continue", WIDTH / 2, HEIGHT / 2 + 40);
    }
    
    private void renderGameOverOverlay() {
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, WIDTH, HEIGHT);
        
        gc.setFill(Color.RED);
        gc.setFont(Font.font(50));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("GAME OVER", WIDTH / 2, HEIGHT / 2);
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(30));
        gc.fillText("Final Score: " + score, WIDTH / 2, HEIGHT / 2 + 50);
        
        if (gameMode == GameMode.SURVIVAL) {
            gc.fillText("Waves Survived: " + (wave - 1), WIDTH / 2, HEIGHT / 2 + 90);
        }
        
        gc.setFont(Font.font(20));
        gc.fillText("Press SPACE to return to menu", WIDTH / 2, HEIGHT / 2 + 130);
    }
    
    private void renderVictoryOverlay() {
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, WIDTH, HEIGHT);
        
        gc.setFill(Color.GOLD);
        gc.setFont(Font.font(50));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("VICTORY!", WIDTH / 2, HEIGHT / 2);
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(30));
        gc.fillText("Final Score: " + score, WIDTH / 2, HEIGHT / 2 + 50);
        gc.fillText("Ammo Remaining: " + ammo, WIDTH / 2, HEIGHT / 2 + 90);
        
        gc.setFont(Font.font(20));
        gc.fillText("Press SPACE to return to menu", WIDTH / 2, HEIGHT / 2 + 130);
    }
    
    private void handleMenuClick(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();
        
        // Check which button was clicked
        for (int i = 0; i < 4; i++) {
            if (x >= WIDTH / 2 - 100 && x <= WIDTH / 2 + 100 &&
                y >= 300 + i * 60 && y <= 350 + i * 60) {
                gameMode = GameMode.values()[i];
                score = 0;
                initGame();
                break;
            }
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

