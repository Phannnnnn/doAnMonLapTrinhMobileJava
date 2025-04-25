package com.example.game_2d;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import static com.example.game_2d.GameView.screenRatioX;
import static com.example.game_2d.GameView.screenRatioY;

import java.util.Random;

public class Collectible {
    int x, y;
    int width, height;
    boolean isCollected;
    int type; // 0: coin, 1: fuel, 2: star, etc.
    Bitmap bitmap;

    public Collectible(Resources res, int screenX, int screenY, int type) {
        this.type = type;
        isCollected = false;

        if (type == 0) {
            bitmap = BitmapFactory.decodeResource(res, R.drawable.coin);
        } else if (type == 1) {
            bitmap = BitmapFactory.decodeResource(res, R.drawable.heart_full);
        } else {
            bitmap = BitmapFactory.decodeResource(res, R.drawable.booster);
        }

        width = bitmap.getWidth();
        height = bitmap.getHeight();

        width /= 8;
        height /= 8;

        width = (int) (width * GameView.screenRatioX);
        height = (int) (height * GameView.screenRatioY);

        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
    }

    Rect getCollisionShape() {
        return new Rect(x, y, x + width, y + height);
    }
}