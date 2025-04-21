package com.example.game_2d;

import android.app.Dialog;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class GameOverActivity extends Dialog implements View.OnClickListener {

    private GameActivity activity;
    private Button btnToMenu, btnPlayAgain;
    private TextView lblScore;
    private int score;

    public GameOverActivity(GameActivity activity, int score) {
        super(activity);
        this.activity = activity;
        this.score = score;

        // Bỏ tiêu đề của Dialog
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_game_over);
        setCancelable(false);

        // Khởi tạo các view
        lblScore = findViewById(R.id.lblScore);
        btnToMenu = findViewById(R.id.btnToMenu);
        btnPlayAgain = findViewById(R.id.btnPlayAgain);

        // Thiết lập nội dung
        lblScore.setText("Điểm: " + score);

        // Thiết lập sự kiện click
        btnToMenu.setOnClickListener(this);
        btnPlayAgain.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnToMenu) {
            // Quay về menu chính
            activity.startActivity(new Intent(activity, MainActivity.class));
            activity.finish();
        } else if (v.getId() == R.id.btnPlayAgain) {
            // Chơi lại
            activity.startActivity(new Intent(activity, GameActivity.class));
            activity.finish();
        }
        dismiss();
    }
}
