package com.nhnacademy.breakout;

import com.nhnacademy.game.collision.Bounds;
import com.nhnacademy.game.collision.Boundable;
import com.nhnacademy.game.collision.Collidable;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BreakoutGame extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PADDLE_WIDTH = 100;
    private static final int PADDLE_HEIGHT = 10;
    private static final int BALL_RADIUS = 5;
    private static final int BRICK_WIDTH = 75;
    private static final int BRICK_HEIGHT = 20;
    private static final int BRICK_ROWS = 5;
    private static final int BRICK_COLS = 10;
    
    private Canvas canvas;
    private GraphicsContext gc;
    private Paddle paddle;
    private Ball ball;
    private List<Brick> bricks;
    private List<PowerUp> powerUps;
    private boolean leftPressed, rightPressed;
    private int score = 0;
    private int lives = 3;
    private int level = 1;
    private GameState gameState = GameState.READY;
    
    @Override
    public void start(Stage primaryStage) {
        canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
        
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);
        
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.LEFT) leftPressed = true;
            if (e.getCode() == KeyCode.RIGHT) rightPressed = true;
            if (e.getCode() == KeyCode.SPACE) {
                if (gameState == GameState.READY) {
                    gameState = GameState.PLAYING;
                } else if (gameState == GameState.PLAYING) {
                    gameState = GameState.PAUSED;
                } else if (gameState == GameState.PAUSED) {
                    gameState = GameState.PLAYING;
                } else if (gameState == GameState.LEVEL_COMPLETE) {
                    nextLevel();
                }
            }
            if (e.getCode() == KeyCode.R && gameState == GameState.GAME_OVER) {
                resetGame();
            }
        });
        
        scene.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.LEFT) leftPressed = false;
            if (e.getCode() == KeyCode.RIGHT) rightPressed = false;
        });
        
        primaryStage.setTitle("Breakout Game - Chapter 8");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        
        initGame();
        
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
        paddle = new Paddle(WIDTH / 2 - PADDLE_WIDTH / 2, HEIGHT - 50, PADDLE_WIDTH, PADDLE_HEIGHT);
        ball = new Ball(WIDTH / 2, HEIGHT - 70, BALL_RADIUS);
        ball.setVelocity(150, -150);
        powerUps = new ArrayList<>();
        createBricks();
    }
    
    private void createBricks() {
        bricks = new ArrayList<>();
        for (int row = 0; row < BRICK_ROWS; row++) {
            for (int col = 0; col < BRICK_COLS; col++) {
                double x = col * (BRICK_WIDTH + 5) + 35;
                double y = row * (BRICK_HEIGHT + 5) + 50;
                Color color = Color.hsb(row * 60, 0.8, 0.9);
                int points = (BRICK_ROWS - row) * 10;
                bricks.add(new Brick(x, y, BRICK_WIDTH, BRICK_HEIGHT, color, points));
            }
        }
    }
    
    private void update(double deltaTime) {
        // Update paddle
        if (leftPressed && paddle.getX() > 0) {
            paddle.move(-300 * deltaTime);
        }
        if (rightPressed && paddle.getX() + paddle.getWidth() < WIDTH) {
            paddle.move(300 * deltaTime);
        }
        
        // Update ball
        ball.update(deltaTime);
        
        // Ball boundaries
        Bounds gameBounds = new Bounds(0, 0, WIDTH, HEIGHT);
        if (!ball.isInBounds(gameBounds)) {
            ball.handleBoundaryCollision(gameBounds);
        }
        
        // Ball missed paddle
        if (ball.getY() > HEIGHT) {
            lives--;
            if (lives <= 0) {
                gameState = GameState.GAME_OVER;
            } else {
                resetBall();
                gameState = GameState.READY;
            }
        }
        
        // Ball-paddle collision
        if (ball.collidesWith(paddle)) {
            ball.handleCollision(paddle);
            // Add spin based on hit position
            double hitPos = (ball.getCenterX() - paddle.getX()) / paddle.getWidth();
            ball.setVelocity(ball.getVelocityX() + (hitPos - 0.5) * 200, ball.getVelocityY());
        }
        
        // Ball-brick collisions
        List<Brick> toRemove = new ArrayList<>();
        for (Brick brick : bricks) {
            if (ball.collidesWith(brick)) {
                ball.handleCollision(brick);
                brick.handleCollision(ball);
                if (brick.isDestroyed()) {
                    toRemove.add(brick);
                    score += brick.getPoints();
                    
                    // Chance to spawn power-up
                    if (Math.random() < 0.2) {
                        powerUps.add(new PowerUp(brick.getCenterX(), brick.getCenterY()));
                    }
                }
                break;
            }
        }
        bricks.removeAll(toRemove);
        
        // Update power-ups
        List<PowerUp> powerUpsToRemove = new ArrayList<>();
        for (PowerUp powerUp : powerUps) {
            powerUp.update(deltaTime);
            if (powerUp.getCenterY() > HEIGHT) {
                powerUpsToRemove.add(powerUp);
            } else if (powerUp.collidesWith(paddle)) {
                applyPowerUp(powerUp);
                powerUpsToRemove.add(powerUp);
            }
        }
        powerUps.removeAll(powerUpsToRemove);
        
        // Check level complete
        if (bricks.isEmpty()) {
            gameState = GameState.LEVEL_COMPLETE;
        }
    }
    
    private void render() {
        // Clear screen
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Draw game objects
        if (gameState != GameState.GAME_OVER) {
            paddle.draw(gc);
            ball.draw(gc);
            for (Brick brick : bricks) {
                brick.draw(gc);
            }
            for (PowerUp powerUp : powerUps) {
                powerUp.draw(gc);
            }
        }
        
        // Draw UI
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(20));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Score: " + score, 10, 30);
        gc.fillText("Lives: " + lives, WIDTH - 100, 30);
        gc.fillText("Level: " + level, WIDTH / 2 - 30, 30);
        
        // Draw game state messages
        gc.setTextAlign(TextAlignment.CENTER);
        if (gameState == GameState.READY) {
            gc.setFont(Font.font(30));
            gc.fillText("Press SPACE to start", WIDTH / 2, HEIGHT / 2);
        } else if (gameState == GameState.PAUSED) {
            gc.setFont(Font.font(30));
            gc.fillText("PAUSED", WIDTH / 2, HEIGHT / 2);
        } else if (gameState == GameState.GAME_OVER) {
            gc.setFont(Font.font(40));
            gc.fillText("GAME OVER", WIDTH / 2, HEIGHT / 2);
            gc.setFont(Font.font(20));
            gc.fillText("Final Score: " + score, WIDTH / 2, HEIGHT / 2 + 40);
            gc.fillText("Press R to restart", WIDTH / 2, HEIGHT / 2 + 70);
        } else if (gameState == GameState.LEVEL_COMPLETE) {
            gc.setFont(Font.font(30));
            gc.fillText("Level Complete!", WIDTH / 2, HEIGHT / 2);
            gc.fillText("Press SPACE to continue", WIDTH / 2, HEIGHT / 2 + 40);
        }
    }
    
    private void resetBall() {
        ball.setPosition(paddle.getCenterX(), HEIGHT - 70);
        ball.setVelocity(150, -150);
    }
    
    private void nextLevel() {
        level++;
        createBricks();
        resetBall();
        gameState = GameState.READY;
    }
    
    private void resetGame() {
        score = 0;
        lives = 3;
        level = 1;
        initGame();
        gameState = GameState.READY;
    }
    
    private void applyPowerUp(PowerUp powerUp) {
        switch (powerUp.getType()) {
            case WIDER_PADDLE:
                paddle.setWidth(paddle.getWidth() * 1.5);
                break;
            case EXTRA_LIFE:
                lives++;
                break;
            case MULTI_BALL:
                // Simplified - just increase ball speed
                ball.setVelocity(ball.getVelocityX() * 1.2, ball.getVelocityY() * 1.2);
                break;
            case SLOW_BALL:
                // Slow down the ball
                ball.setVelocity(ball.getVelocityX() * 0.7, ball.getVelocityY() * 0.7);
                break;
            case LASER:
                // TODO: Implement laser functionality
                break;
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

