package com.nhnacademy.breakout.world;

import com.nhnacademy.breakout.objects.*;
import com.nhnacademy.game.behavior.*;
import com.nhnacademy.game.collision.Bounds;
import com.nhnacademy.game.collision.Collidable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.*;

/**
 * Breakout 게임 월드
 * 2~7장에서 배운 개념을 활용하여 게임 세계를 관리합니다.
 */
public class BreakoutWorld {
    private double width;
    private double height;
    private static final double WALL_THICKNESS = 20;
    
    // 게임 객체들
    private List<UnbreakableBrick> walls;
    private List<Breakable> bricks;
    private List<BreakoutBall> balls;
    private BreakoutPaddle paddle;
    private List<PowerUp> powerUps;
    private List<Exploding.ExplosionEffect> explosions;
    
    // 게임 상태
    private int score = 0;
    private int lives = 3;
    private int level = 1;
    
    public BreakoutWorld(double width, double height) {
        this.width = width;
        this.height = height;
        this.walls = new ArrayList<>();
        this.bricks = new ArrayList<>();
        this.balls = new ArrayList<>();
        this.powerUps = new ArrayList<>();
        this.explosions = new ArrayList<>();
        
        initializeWalls();
        initializePaddle();
        initializeBall();
    }
    
    /**
     * 벽을 초기화합니다.
     * 상, 좌, 우 벽은 깨지지 않는 벽돌로 만듭니다.
     */
    private void initializeWalls() {
        // 상단 벽
        walls.add(UnbreakableBrick.WallFactory.createTopWall(width, WALL_THICKNESS));
        
        // 좌측 벽
        walls.add(UnbreakableBrick.WallFactory.createLeftWall(height, WALL_THICKNESS));
        
        // 우측 벽
        walls.add(UnbreakableBrick.WallFactory.createRightWall(width, height, WALL_THICKNESS));
    }
    
    /**
     * 패들을 초기화합니다.
     */
    private void initializePaddle() {
        double paddleX = (width - 100) / 2;
        double paddleY = height - 60;
        paddle = new BreakoutPaddle(paddleX, paddleY);
    }
    
    /**
     * 공을 초기화합니다.
     */
    private void initializeBall() {
        balls.clear();
        BreakoutBall ball = new BreakoutBall(width / 2, height - 80);
        ball.setVelocity(150, -150);
        balls.add(ball);
    }
    
    /**
     * 레벨에 따른 벽돌을 생성합니다.
     */
    public void createLevel(int level) {
        bricks.clear();
        this.level = level;
        
        // 레벨에 따른 벽돌 배치
        double brickWidth = 60;
        double brickHeight = 20;
        double startX = WALL_THICKNESS + 20;
        double startY = WALL_THICKNESS + 40;
        
        int rows = Math.min(5 + level, 10);
        int cols = (int)((width - 2 * WALL_THICKNESS - 40) / (brickWidth + 5));
        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double x = startX + col * (brickWidth + 5);
                double y = startY + row * (brickHeight + 5);
                
                // 레벨에 따른 벽돌 타입 결정
                Breakable brick = createBrickForLevel(x, y, brickWidth, brickHeight, row, col, level);
                if (brick != null) {
                    bricks.add(brick);
                }
            }
        }
    }
    
    /**
     * 레벨과 위치에 따른 벽돌을 생성합니다.
     */
    private Breakable createBrickForLevel(double x, double y, double width, double height, 
                                         int row, int col, int level) {
        Color color = Color.hsb(row * 40, 0.8, 0.9);
        int points = (5 - row) * 10 * level;
        
        // 레벨에 따른 특수 벽돌 배치
        if (level >= 3 && (row + col) % 7 == 0) {
            // 폭발 벽돌
            return new ExplodingBrick(x, y, width, height, Color.ORANGE, points * 2);
        } else if (level >= 2 && row < 2) {
            // 다중 히트 벽돌
            return new MultiHitBrick(x, y, width, height, color, points, 2 + level / 3);
        } else if ((row + col) % 5 == 0) {
            // 파워업 벽돌
            return new PowerUpBrick(x, y, width, height, color, points, 0.3);
        } else {
            // 일반 벽돌
            return new SimpleBrick(x, y, width, height, color, points);
        }
    }
    
    /**
     * 월드를 업데이트합니다.
     */
    public void update(double deltaTime) {
        // 패들 업데이트
        paddle.updatePowerUps(deltaTime);
        
        // 공 업데이트
        updateBalls(deltaTime);
        
        // 파워업 업데이트
        updatePowerUps(deltaTime);
        
        // 폭발 효과 업데이트
        updateExplosions(deltaTime);
        
        // 충돌 처리
        handleCollisions();
        
        // 게임 상태 확인
        checkGameState();
    }
    
    /**
     * 공들을 업데이트합니다.
     */
    private void updateBalls(double deltaTime) {
        List<BreakoutBall> toRemove = new ArrayList<>();
        
        for (BreakoutBall ball : balls) {
            if (!ball.isSticky()) {
                ball.update(deltaTime);
                
                // 하단 경계 확인 (공을 놓친 경우)
                if (ball.getCenterY() > height) {
                    toRemove.add(ball);
                }
            } else {
                // 끈끈한 공은 패들을 따라 이동
                ball.setPosition(paddle.getCenterX(), paddle.getY() - ball.getRadius());
            }
        }
        
        balls.removeAll(toRemove);
        
        // 모든 공을 놓친 경우
        if (balls.isEmpty()) {
            lives--;
            if (lives > 0) {
                initializeBall();
            }
        }
    }
    
    /**
     * 파워업을 업데이트합니다.
     */
    private void updatePowerUps(double deltaTime) {
        List<PowerUp> toRemove = new ArrayList<>();
        
        for (PowerUp powerUp : powerUps) {
            powerUp.update(deltaTime);
            
            // 화면 밖으로 나간 파워업 제거
            if (powerUp.getY() > height) {
                toRemove.add(powerUp);
            }
        }
        
        powerUps.removeAll(toRemove);
    }
    
    /**
     * 폭발 효과를 업데이트합니다.
     */
    private void updateExplosions(double deltaTime) {
        List<Exploding.ExplosionEffect> toRemove = new ArrayList<>();
        
        for (Exploding.ExplosionEffect explosion : explosions) {
            explosion.update(deltaTime);
            if (explosion.isFinished()) {
                toRemove.add(explosion);
            }
        }
        
        explosions.removeAll(toRemove);
    }
    
    /**
     * 충돌을 처리합니다.
     */
    private void handleCollisions() {
        // 공과 벽 충돌
        for (BreakoutBall ball : balls) {
            for (UnbreakableBrick wall : walls) {
                if (ball.collidesWith(wall)) {
                    ball.handleCollision(wall);
                }
            }
        }
        
        // 공과 패들 충돌
        for (BreakoutBall ball : balls) {
            if (ball.collidesWith(paddle)) {
                ball.handlePaddleCollision(paddle);
            }
        }
        
        // 공과 벽돌 충돌
        List<Breakable> brokenBricks = new ArrayList<>();
        for (BreakoutBall ball : balls) {
            for (Breakable brick : bricks) {
                if (brick instanceof Collidable) {
                    Collidable collidableBrick = (Collidable) brick;
                    if (ball.collidesWith(collidableBrick)) {
                        ball.handleCollision(collidableBrick);
                        collidableBrick.handleCollision(ball);
                        
                        if (brick.isBroken()) {
                            brokenBricks.add(brick);
                            score += brick.getPoints();
                            
                            // 파워업 생성
                            if (brick instanceof PowerUpProvider) {
                                PowerUpProvider provider = (PowerUpProvider) brick;
                                if (provider.shouldDropPowerUp()) {
                                    createPowerUp(collidableBrick, provider.getPowerUpType());
                                }
                            }
                            
                            // 폭발 처리
                            if (brick instanceof Exploding) {
                                handleExplosion((Exploding) brick);
                            }
                        }
                        break; // 한 프레임에 하나의 벽돌만 충돌
                    }
                }
            }
        }
        bricks.removeAll(brokenBricks);
        
        // 패들과 파워업 충돌
        List<PowerUp> collectedPowerUps = new ArrayList<>();
        for (PowerUp powerUp : powerUps) {
            if (powerUp.collidesWith(paddle)) {
                applyPowerUp(powerUp);
                collectedPowerUps.add(powerUp);
            }
        }
        powerUps.removeAll(collectedPowerUps);
    }
    
    /**
     * 폭발을 처리합니다.
     */
    private void handleExplosion(Exploding explodingBrick) {
        explosions.addAll(explodingBrick.explode());
        Bounds explosionBounds = explodingBrick.getExplosionBounds();
        
        // 폭발 범위 내의 벽돌에 피해
        List<Breakable> affectedBricks = new ArrayList<>();
        for (Breakable brick : bricks) {
            if (brick instanceof Collidable) {
                Collidable collidable = (Collidable) brick;
                if (explosionBounds.intersects(collidable.getBounds())) {
                    brick.hit(explodingBrick.getExplosionDamage());
                    if (brick.isBroken()) {
                        affectedBricks.add(brick);
                        score += brick.getPoints();
                    }
                }
            }
        }
        bricks.removeAll(affectedBricks);
    }
    
    /**
     * 파워업을 생성합니다.
     */
    private void createPowerUp(Collidable brick, PowerUpProvider.PowerUpType type) {
        if (brick instanceof StaticObject) {
            StaticObject obj = (StaticObject) brick;
            PowerUp powerUp = new PowerUp(obj.getCenterX(), obj.getCenterY(), type);
            powerUps.add(powerUp);
        }
    }
    
    /**
     * 파워업을 적용합니다.
     */
    private void applyPowerUp(PowerUp powerUp) {
        switch (powerUp.getType()) {
            case WIDER_PADDLE:
                paddle.applyPowerUp(BreakoutPaddle.PowerUpType.WIDER_PADDLE, 
                                  powerUp.getType().getDuration());
                break;
            case STICKY_PADDLE:
                paddle.applyPowerUp(BreakoutPaddle.PowerUpType.STICKY_PADDLE, 
                                  powerUp.getType().getDuration());
                break;
            case LASER:
                paddle.applyPowerUp(BreakoutPaddle.PowerUpType.LASER, 
                                  powerUp.getType().getDuration());
                break;
            case MULTI_BALL:
                createMultiBalls();
                break;
            case SLOW_BALL:
                for (BreakoutBall ball : balls) {
                    ball.adjustSpeed(0.5);
                }
                break;
            case EXTRA_LIFE:
                lives++;
                break;
        }
    }
    
    /**
     * 멀티볼을 생성합니다.
     */
    private void createMultiBalls() {
        if (!balls.isEmpty()) {
            BreakoutBall originalBall = balls.get(0);
            for (int i = 0; i < 2; i++) {
                BreakoutBall newBall = new BreakoutBall(
                    originalBall.getCenterX(), 
                    originalBall.getCenterY()
                );
                double angle = (i + 1) * Math.PI / 6;
                double speed = 200;
                newBall.setVelocity(
                    Math.cos(angle) * speed,
                    -Math.sin(angle) * speed
                );
                balls.add(newBall);
            }
        }
    }
    
    /**
     * 게임 상태를 확인합니다.
     */
    private void checkGameState() {
        // 모든 벽돌이 깨진 경우
        if (bricks.isEmpty()) {
            // 다음 레벨로
            level++;
            createLevel(level);
            initializeBall();
        }
    }
    
    /**
     * 월드를 렌더링합니다.
     */
    public void render(GraphicsContext gc) {
        // 배경
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, width, height);
        
        // 벽
        for (UnbreakableBrick wall : walls) {
            wall.draw(gc);
        }
        
        // 벽돌
        for (Breakable brick : bricks) {
            if (brick instanceof StaticObject) {
                ((StaticObject) brick).draw(gc);
            }
        }
        
        // 패들
        paddle.draw(gc);
        
        // 공
        for (BreakoutBall ball : balls) {
            ball.draw(gc);
        }
        
        // 파워업
        for (PowerUp powerUp : powerUps) {
            powerUp.draw(gc);
        }
        
        // 폭발 효과
        for (Exploding.ExplosionEffect explosion : explosions) {
            renderExplosion(gc, explosion);
        }
        
        // UI
        renderUI(gc);
    }
    
    /**
     * 폭발 효과를 렌더링합니다.
     */
    private void renderExplosion(GraphicsContext gc, Exploding.ExplosionEffect explosion) {
        double opacity = 1.0 - explosion.getProgress();
        gc.setGlobalAlpha(opacity);
        gc.setFill(Color.ORANGE);
        gc.fillOval(
            explosion.getX() - explosion.getCurrentRadius(),
            explosion.getY() - explosion.getCurrentRadius(),
            explosion.getCurrentRadius() * 2,
            explosion.getCurrentRadius() * 2
        );
        gc.setGlobalAlpha(1.0);
    }
    
    /**
     * UI를 렌더링합니다.
     */
    private void renderUI(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font(20));
        gc.fillText("Score: " + score, WALL_THICKNESS + 10, WALL_THICKNESS + 20);
        gc.fillText("Lives: " + lives, width / 2 - 40, WALL_THICKNESS + 20);
        gc.fillText("Level: " + level, width - 100, WALL_THICKNESS + 20);
    }
    
    // 입력 처리
    public void movePaddleLeft(double deltaTime) {
        paddle.moveLeft(deltaTime);
        paddle.constrainToBounds(WALL_THICKNESS, width - WALL_THICKNESS);
    }
    
    public void movePaddleRight(double deltaTime) {
        paddle.moveRight(deltaTime);
        paddle.constrainToBounds(WALL_THICKNESS, width - WALL_THICKNESS);
    }
    
    public void launchBall() {
        for (BreakoutBall ball : balls) {
            if (ball.isSticky()) {
                ball.launch();
            }
        }
    }
    
    // Getters
    public int getScore() { return score; }
    public int getLives() { return lives; }
    public int getLevel() { return level; }
    public boolean isGameOver() { return lives <= 0; }
    public boolean hasWon() { return level > 10; }
}