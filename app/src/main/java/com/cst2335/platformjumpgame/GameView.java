package com.cst2335.platformjumpgame;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

    private Thread gameThread;
    private volatile boolean isPlaying;
    private Player player;
    private final List<Platform> platforms;
    private final DatabaseHelper databaseHelper;
    private int score = 0;
    private boolean isGameOver;

    private final SoundPool soundPool;
    private final int collisionSoundId;
    private final int gameOverSoundId;
    private final MediaPlayer backgroundMusic;

    private Platform currentPlatform = null;

    public GameView(Context context) {
        super(context);
        player = new Player();
        platforms = new ArrayList<>();
        platforms.add(new Platform(getWidth(), 800, 200));
        databaseHelper = new DatabaseHelper(context);
        getHolder().addCallback(this);

        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                .build();

        soundPool = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .setMaxStreams(2)
                .build();

        collisionSoundId = soundPool.load(context, R.raw.collision_sound, 1);
        gameOverSoundId = soundPool.load(context, R.raw.game_over_sound, 1);

        backgroundMusic = MediaPlayer.create(context, R.raw.background_music);
        backgroundMusic.setLooping(true);

        isGameOver = false;
    }

    public void resume() {
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
        backgroundMusic.start();
    }

    @Override
    public void run() {
        while (isPlaying) {
            update();
            draw();
            sleep();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (isGameOver) {
                restartGame();
            } else {
                player.jump();
                if (!isPlaying) {
                    isPlaying = true;
                    resume();
                }
            }
        }
        return true;
    }

    public void pause() {
        isPlaying = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        isGameOver = false;
        resume();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        pause();
        soundPool.release();
        pauseBackgroundMusic();
    }

    private void update() {
        player.update();

        Iterator<Platform> iterator = platforms.iterator();
        while (iterator.hasNext()) {
            Platform platform = iterator.next();
            platform.update();

            if (player.collidesWith(platform) && player.isAbove(platform)) {
                player.y = platform.y - 100;
                player.velocityY = 0;

                if (currentPlatform != platform) {
                    score += 1;
                    currentPlatform = platform;

                    databaseHelper.saveScore(score);
                    soundPool.play(collisionSoundId, 1, 1, 0, 0, 1);
                }
            }

            if (platform.x + 300 < 0) {
                iterator.remove();
            }
        }

        if (Math.random() < 0.03) {
            float yPosition = (float) (100 + Math.random() * 1000);
            float platformWidth = (float) (100 + Math.random() * 300);

            boolean overlap = false;
            float xThreshold = 300;
            float yThreshold = 150;

            for (Platform platform : platforms) {
                float xDistance = Math.abs(platform.x - getWidth());
                float yDistance = Math.abs(platform.y - yPosition);

                if (xDistance < platform.width + platformWidth + xThreshold &&
                        yDistance < yThreshold) {
                    overlap = true;
                    break;
                }
            }

            if (!overlap) {
                platforms.add(new Platform(getWidth(), yPosition, platformWidth));
                Log.d("GameView", "New platform added. Total platforms: " + platforms.size());
            }
        }

        if (player.y > getHeight()) {
            gameOver();
        }
    }

    /** @noinspection IntegerDivisionInFloatingPointContext*/
    private void draw() {
        if (getHolder().getSurface().isValid()) {
            Canvas canvas = getHolder().lockCanvas();
            canvas.drawColor(Color.WHITE);

            Paint playerPaint = new Paint();
            playerPaint.setColor(Color.RED);
            canvas.drawRect(player.x, player.y, player.x + 100, player.y + 100, playerPaint);

            Paint platformPaint = new Paint();
            platformPaint.setColor(Color.GREEN);
            for (Platform platform : platforms) {
                float platformHeight = Platform.HEIGHT;
                canvas.drawRect(platform.x, platform.y, platform.x + platform.width, platform.y + platformHeight, platformPaint);
            }

            Paint textPaint = new Paint();
            textPaint.setTextSize(50);
            textPaint.setColor(Color.BLACK);

            canvas.drawText("Score: " + score, 20, 50, textPaint);

            if (isGameOver) {
                float textWidth = textPaint.measureText("Game Over! Tap to restart.");
                float textX = (getWidth() - textWidth) / 2;
                float textY = getHeight() / 2;
                canvas.drawText("Game Over! Tap to restart.", textX, textY, textPaint);
            }

            int topScore = databaseHelper.getTopScore();
            canvas.drawText("Top Score: " + topScore, getWidth() - 300, 50, textPaint);

            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    private void sleep() {
        try {
            Thread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void gameOver() {
        isPlaying = false;
        isGameOver = true;
        int topScore = databaseHelper.getTopScore();

        if (score > topScore) {
            topScore = score;
            databaseHelper.saveScore(topScore);
        }

        score = 0;
        Log.d("GameView", "Game Over. Score: " + topScore);

        soundPool.play(gameOverSoundId, 1, 1, 0, 0, 1);

        pauseBackgroundMusic();
    }

    private void restartGame() {
        isPlaying = true;
        isGameOver = false;

        player = new Player();
        platforms.clear();
        platforms.add(new Platform(getWidth(), 800, 200));

        soundPool.autoPause();

        if (gameThread != null && !gameThread.isAlive()) {
            gameThread = new Thread(this);
            gameThread.start();
        }

        backgroundMusic.seekTo(0);
        backgroundMusic.start();
    }

    private void pauseBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }
}


