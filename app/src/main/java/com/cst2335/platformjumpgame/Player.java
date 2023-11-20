package com.cst2335.platformjumpgame;

public class Player {
    public float x = 100, y = 500;
    public float velocityY = 0;

    private static final float GRAVITY = 0.75f;
    private static final float JUMP_STRENGTH = -20f;

    public void update() {
        y += velocityY;
        velocityY += GRAVITY;
    }

    public void jump() {
        velocityY = JUMP_STRENGTH;
    }

    public boolean collidesWith(Platform platform) {
        return x < platform.x + platform.width &&
                x + 100 > platform.x &&
                y + 180 > platform.y &&
                y + 180 < platform.y + 100;
    }

    public boolean isAbove(Platform platform) {
        float playerRight = x + 100;
        float platformRight = platform.x + platform.width;

        return playerRight > platform.x &&
                x < platformRight &&
                y + 100 <= platform.y;
    }
}
