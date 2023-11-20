package com.cst2335.platformjumpgame;

public class Platform {
    public static final float SPEED = 3.0f;
    public float x, y;
    public float width;
    public static final float HEIGHT = 50.0f;

    public Platform(float x, float y, float width) {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    public void update() {
        x -= SPEED;
    }
}


