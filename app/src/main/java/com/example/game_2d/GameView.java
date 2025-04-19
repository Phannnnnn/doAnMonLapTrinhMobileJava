package com.example.game_2d;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable {

    private Thread thread;
    private boolean isPlaying, isGameOver = false;
    private int screenX, screenY, score = 0;
    public static float screenRatioX, screenRatioY;
    private Paint paint;
    private Bird[] birds;
    private SharedPreferences prefs;
    private Random random;
    private SoundPool soundPool;
    private List<Bullet> bullets;
    private int sound;
    private Flight flight;
    private GameActiviti activiti;
    private Background background1, background2;
    private long lastShootTime = 0;
    private final long SHOOT_INTERVAL = 200; // Thời gian giữa các lần bắn (milliseconds)

    private float previousTouchX;
    private float previousTouchY;
    private float flightVelocityX = 0;
    private float flightVelocityY = 0;
    private final float VELOCITY_DAMPING = 0.9f; // Hệ số giảm dần vận tốc khi không chạm
    private final float MAX_VELOCITY = 100 * screenRatioX; // Giới hạn vận tốc

    public GameView(GameActiviti activiti, int screenX, int screenY) {
        super(activiti);

        this.activiti = activiti;

        prefs = activiti.getSharedPreferences("game", Context.MODE_PRIVATE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else{
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        }

        sound = soundPool.load(activiti, R.raw.shoot, 1);

        this.screenX = screenX;
        this.screenY = screenY;
        screenRatioX = 1920f / screenX;
        screenRatioY = 1080f / screenY;

        background1 = new Background(screenX, screenY, getResources());
        background2 = new Background(screenX, screenY, getResources());
        background2.setX(screenX);

        flight = new Flight(this, screenY, getResources());
        bullets = new ArrayList<>();

        paint = new Paint();
        paint.setTextSize(128);
        paint.setColor(Color.WHITE);

        birds = new Bird[4];

        for (int i = 0;i < 4;i++) {
            Bird bird = new Bird(getResources());
            birds[i] = bird;
        }

        random = new Random();
    }

    @Override
    public void run() {

        while (isPlaying) {

            update ();
            draw ();
            sleep ();

        }

    }

    private void update () {

        background1.setX((int) (background1.getX() - (10*screenRatioX)));
        background2.setX((int) (background2.getX() - (10*screenRatioX)));


        if (background1.getX() + background1.getBackground().getWidth() < 0) {
            background1.setX(screenX);
        }
        if (background2.getX() + background2.getBackground().getWidth() < 0) {
            background2.setX(screenX);
        }

        if (flight.isGoingUp) {
            flight.x += flightVelocityX;
            flight.y += flightVelocityY;

            if (Math.abs(flightVelocityX) > MAX_VELOCITY) {
                flightVelocityX = Math.signum(flightVelocityX) * MAX_VELOCITY;
            }
            if (Math.abs(flightVelocityY) > MAX_VELOCITY) {
                flightVelocityY = Math.signum(flightVelocityY) * MAX_VELOCITY;
            }
        } else {
            flightVelocityX *= VELOCITY_DAMPING;
            flightVelocityY *= VELOCITY_DAMPING;

            flight.x += flightVelocityX;
            flight.y += flightVelocityY;

            if (Math.abs(flightVelocityX) < 1) {
                flightVelocityX = 0;
            }
            if (Math.abs(flightVelocityY) < 1) {
                flightVelocityY = 0;
            }
        }

        if (flight.y < 0) {
            flight.y = 0;
            flightVelocityY = 0;
        }
        if (flight.y >= screenY - flight.height) {
            flight.y = screenY - flight.height;
            flightVelocityY = 0;
        }
        if (flight.x < 0) {
            flight.x = 0;
            flightVelocityX = 0;
        }
        if (flight.x >= screenX - flight.width) {
            flight.x = screenX - flight.width;
            flightVelocityX = 0;
        }

        List<Bullet> trash = new ArrayList<>();

        for (Bullet bullet : bullets) {

            if (bullet.x > screenX)
                trash.add(bullet);

            bullet.x += 50 * screenRatioX;

            for (Bird bird : birds) {

                if (Rect.intersects(bird.getCollisionShape(),
                        bullet.getCollisionShape())) {

                    score++;
                    bird.x = -500;
                    bullet.x = screenX + 500;
                    bird.wasShot = true;
                }
            }
        }

        for (Bullet bullet : trash)
            bullets.remove(bullet);

        for (Bird bird : birds) {

            bird.x -= bird.speed;

            if (bird.x + bird.width < 0) {

                if (!bird.wasShot) {
                    isGameOver = true;
                    return;
                }

                int bound = (int) (30 * screenRatioX);
                bird.speed = random.nextInt(bound);

                if (bird.speed < 10 * screenRatioX)
                    bird.speed = (int) (10 * screenRatioX);

                bird.x = screenX;
                bird.y = random.nextInt(screenY - bird.height);

                bird.wasShot = false;
            }

            if (Rect.intersects(bird.getCollisionShape(), flight.getCollisionShape())) {
                isGameOver = true;
                return;
            }

        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShootTime > SHOOT_INTERVAL) {
            flight.isShooting = true;
            lastShootTime = currentTime;
        } else {
            flight.isShooting = false;
        }
    }

    private void draw () {

        if (getHolder().getSurface().isValid()) {

            Canvas canvas = getHolder().lockCanvas();
            canvas.drawBitmap(background1.getBackground(), background1.getX(), background1.getY(), paint);
            canvas.drawBitmap(background2.getBackground(), background2.getX(), background2.getY(), paint);

            for (Bird bird : birds)
                canvas.drawBitmap(bird.getBird(), bird.x, bird.y, paint);

            canvas.drawText(score + "", screenX / 2f, 164, paint);

            if (isGameOver) {
                isPlaying = false;
                canvas.drawBitmap(flight.getDead(), flight.x, flight.y, paint);
                getHolder().unlockCanvasAndPost(canvas);
                saveIfHighScore();
                waitBeforeExiting ();
                return;
            }

            canvas.drawBitmap(flight.getFlight(), flight.x, flight.y, paint);

            for (Bullet bullet : bullets)
                canvas.drawBitmap(bullet.bullet, bullet.x, bullet.y, paint);

            getHolder().unlockCanvasAndPost(canvas);

        }

    }

    private void waitBeforeExiting() {

        try {
            Thread.sleep(3000);
            activiti.startActivity(new Intent(activiti, MainActivity.class));
            activiti.finish();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void saveIfHighScore() {

        if (prefs.getInt("highscore", 0) < score) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("highscore", score);
            editor.apply();
        }

    }

    private void sleep () {
        try {
            Thread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume () {

        isPlaying = true;
        thread = new Thread(this);
        thread.start();

    }

    public void pause () {

        try {
            isPlaying = false;
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float currentTouchX = event.getX();
        float currentTouchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                flight.isGoingUp = true;
                previousTouchX = currentTouchX;
                previousTouchY = currentTouchY;
                flightVelocityX = 0; // Reset vận tốc khi bắt đầu chạm mới
                flightVelocityY = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = currentTouchX - previousTouchX;
                float deltaY = currentTouchY - previousTouchY;

                // Cập nhật vận tốc dựa trên sự thay đổi vị trí ngón tay
                flightVelocityX = deltaX;
                flightVelocityY = deltaY;

                previousTouchX = currentTouchX;
                previousTouchY = currentTouchY;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                flight.isGoingUp = false;
                break;
        }

        return true;
    }

    public void newBullet() {

        if (!prefs.getBoolean("isMute", false))
            soundPool.play(sound, 1, 1, 0, 0, 1);

        Bullet bullet = new Bullet(getResources());
        bullet.x = (int) (flight.x + flight.width);
        bullet.y = (int) (flight.y + (flight.height / 2));
        bullets.add(bullet);

    }
}