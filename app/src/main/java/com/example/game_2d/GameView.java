package com.example.game_2d;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
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
    private SharedPreferences prefs;
    private Random random;
    private SoundPool soundPool;
    private List<Bullet> bullets;
    private int sound;
    private Flight flight;
    private GameActivity activity;
    private Background background1, background2;
    private long lastShootTime = 0;
    private long SHOOT_INTERVAL = 200;
    private int birdCurrent = 5;
    int speed_bird = 5;
    ArrayList<Bird> birds;

    private float previousTouchX;
    private float previousTouchY;
    private float flightVelocityX = 0;
    private float flightVelocityY = 0;
    private final float VELOCITY_DAMPING = 0.9f;
    private final float MAX_VELOCITY = 100 * screenRatioX;
    private int nextSpeedIncreaseScore = 100;

    private Paint scorePaint;
    private int lives = 3;
    private Bitmap heartFull;
    int heartHeight,heartWidth;

    private List<Collectible> collectibles;

    public GameView(GameActivity activity, int screenX, int screenY) {
        super(activity);
        this.activity = activity;
        //Tao file game.xml de luu diem
        prefs = activity.getSharedPreferences("game", Context.MODE_PRIVATE);

        //Tao am thanh trong game
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
        sound = soundPool.load(activity, R.raw.shoot, 1);

        //Khai bao cac doi tuong trong game
        this.screenX = screenX;
        this.screenY = screenY;
        screenRatioX = 1920f / screenX;
        screenRatioY = 1080f / screenY;

        background1 = new Background(screenX, screenY, getResources());
        background2 = new Background(screenX, screenY, getResources());
        background2.setX(screenX);

        flight = new Flight(this, screenY, getResources());
        bullets = new ArrayList<>();
        collectibles = new ArrayList<>();

        paint = new Paint();
        paint.setTextSize(128);
        paint.setColor(Color.WHITE);

        birds = new ArrayList<>();
        for (int i = 0; i < birdCurrent; i++) {
            birds.add(new Bird(getResources()));
        }

        random = new Random();

        scorePaint = new Paint();
        scorePaint.setTextSize(80);
        scorePaint.setColor(Color.WHITE);
        scorePaint.setShadowLayer(5, 2, 2, Color.BLACK);

        heartFull = BitmapFactory.decodeResource(getResources(), R.drawable.heart_full);
        heartWidth = (int) (50 * screenRatioX);
        heartHeight = (int) (50 * screenRatioY);
        heartFull = Bitmap.createScaledBitmap(heartFull, heartWidth, heartHeight, false);
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
        if (score >= nextSpeedIncreaseScore && speed_bird < 50) {
            speed_bird += 5;
            nextSpeedIncreaseScore += 100;
            // Thêm bird nếu chưa đủ 10 con
            if (birds.size() < 10) {
                birds.add(new Bird(getResources()));
            }
        }

        //Tao hieu ung background dong
        background1.setX((int) (background1.getX() - (10*screenRatioX)));
        background2.setX((int) (background2.getX() - (10*screenRatioX)));

        if (background1.getX() + background1.getBackground().getWidth() < 0) {
            background1.setX(screenX);
        }
        if (background2.getX() + background2.getBackground().getWidth() < 0) {
            background2.setX(screenX);
        }

        //Di chuyen doi tuong flight theo cu chi cham man hinh
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

        //Luu danh sach cac vien dan da di ra khoi man hinh
        List<Bullet> trash = new ArrayList<>();
        for (Bullet bullet : bullets) {
            if (bullet.x > screenX)
                trash.add(bullet);
            bullet.x += 50 * screenRatioX;

            //Su ly khi dan va cham voi doi tuong bird
            for (Bird bird : birds) {
                if (Rect.intersects(bird.getCollisionShape(),bullet.getCollisionShape())){
                    score++;
                    int collisionX = bird.x;
                    int collisionY = bird.y;

                    //Dua doi tuong bird ra khoi man hinh
                    score++;
                    bird.x = -500;
                    bullet.x = screenX + 500;
                    bird.wasShot = true;

                    int dropChance = random.nextInt(100);
                    if (dropChance < 30) {
                        int randomCollectibleType = random.nextInt(3);
                        Collectible newCollectible = new Collectible(getResources(), screenX, screenY, randomCollectibleType);
                        newCollectible.x = collisionX;
                        newCollectible.y = collisionY;
                        collectibles.add(newCollectible);
                    }
                }
            }
        }

        for (Bullet bullet : trash)
            bullets.remove(bullet);

        for (Bird bird : birds) {
            bird.x -= bird.speed;

            //Neu doi tuong bird ra khoi man hinh khi chua bi ban trung
            if (bird.x + bird.width < 0) {
                if (!bird.wasShot) {
                    lives--;
                    if (lives <= 0) {
                        isGameOver = true;
                        return;
                    }
                }
                // Tao lai doi tuong bird
                bird.speed = speed_bird;
                bird.x = screenX;
                bird.y = random.nextInt(screenY - bird.height);
                bird.wasShot = false;
            }

            //Khi doi tuong flight va cham voi doi tuong bird thi ket thuc tro choi
            if (Rect.intersects(bird.getCollisionShape(), flight.getCollisionShape())) {
                if (!bird.wasShot) {
                    lives--;
                    bird.x = -500;

                    if (lives <= 0) {
                        isGameOver = true;
                        return;
                    } else {
                        int bound = (int) (30 * screenRatioX);
                        bird.speed = random.nextInt(bound);
                        if (bird.speed < 10 * screenRatioX) {
                            bird.speed = (int) (10 * screenRatioX);
                        }
                        bird.x = screenX;
                        bird.y = random.nextInt(screenY - bird.height);
                        bird.wasShot = false;
                    }
                }
            }

            // Cập nhật vật phẩm
            List<Collectible> collectibleTrash = new ArrayList<>();
            for (Collectible collectible : collectibles) {
                collectible.update();

                if (collectible.y > screenY) {
                    collectibleTrash.add(collectible);
                }

                if (Rect.intersects(flight.getCollisionShape(), collectible.getCollisionShape())) {
                    if (collectible.type == 0) {
                        score += 10;
                    } else if (collectible.type == 1) {
                        lives = Math.min(lives + 1, 5);
                    } else if (collectible.type == 2 && SHOOT_INTERVAL > 11) {
                        SHOOT_INTERVAL -= 5;
                    }
                    collectibleTrash.add(collectible);
                }
            }
            collectibles.removeAll(collectibleTrash);
        }

        //Can chinh thoi gian giua nhung lan ban
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

            for (int i = 0; i < lives; i++) {
                int x = screenX - heartWidth * (i + 1) - 20 * (i + 1);
                if (i < lives) {
                    canvas.drawBitmap(heartFull, x, 30, paint);
                }
            }

            for (Bird bird : birds)
            {
                canvas.drawBitmap(bird.getBird(), bird.x, bird.y, paint);
            }
            String scoreText = "Điểm: " + score;
            Rect bounds = new Rect();
            scorePaint.getTextBounds(scoreText, 0, scoreText.length(), bounds);
            canvas.drawText(scoreText, 50, 100, scorePaint);

            if (isGameOver) {
                isPlaying = false;
                canvas.drawBitmap(flight.getDead(), flight.x, flight.y, paint);
                getHolder().unlockCanvasAndPost(canvas);
                saveIfHighScore();
                waitBeforeExiting ();
                return;
            }

            for (Collectible collectible : collectibles) {
                canvas.drawBitmap(collectible.bitmap, collectible.x, collectible.y, paint);
            }

            canvas.drawBitmap(flight.getFlight(), flight.x, flight.y, paint);
            for (Bullet bullet : bullets)
                canvas.drawBitmap(bullet.bullet, bullet.x, bullet.y, paint);
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    private void waitBeforeExiting() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                GameOverActivity gameOverDialog = new GameOverActivity(activity, score);
                gameOverDialog.show();
            }
        }, 17);
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
                flightVelocityX = 0;
                flightVelocityY = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = currentTouchX - previousTouchX;
                float deltaY = currentTouchY - previousTouchY;

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