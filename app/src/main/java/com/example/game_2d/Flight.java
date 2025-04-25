package com.example.game_2d;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

public class Flight {

    private GameView gameView;
    public int width;
    public int height;
    public float y;
    public float x;
    public boolean isGoingUp = false;
    public boolean isShooting = false;
    private Bitmap flight1;
    private Bitmap flight2;
    private Bitmap dead;
    private int flyCounter = 0;

    public Flight(GameView gameView, int screenY, android.content.res.Resources res) {
        this.gameView = gameView;

        flight1 = BitmapFactory.decodeResource(res, R.drawable.fly1);
        flight2 = BitmapFactory.decodeResource(res, R.drawable.fly2);

        width = flight1.getWidth();
        height = flight1.getHeight();
        width /= 4;
        height /= 4;
        width = (int) (width * GameView.screenRatioX);
        height = (int) (height * GameView.screenRatioY);

        flight1 = Bitmap.createScaledBitmap(flight1, width, height, false);
        flight2 = Bitmap.createScaledBitmap(flight2, width, height, false);

        dead = BitmapFactory.decodeResource(res, R.drawable.dead);
        dead = Bitmap.createScaledBitmap(dead, width, height, false);

        y = screenY / 2;
    }

    public Bitmap getFlight () {
        if (isShooting) {
            gameView.newBullet();
        }
        if (flyCounter == 0) {
            flyCounter++;
            return flight1;
        }
        flyCounter--;
        return flight2;
    }

    public Bitmap getDead () {
        return dead;
    }

    public Rect getCollisionShape () {
        return new Rect((int) x, (int) y, (int) (x + width), (int) (y + height));
    }
}