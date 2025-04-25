package com.example.game_2d;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private boolean isMute;
    TextView lblHighScore;
    int hightScore;
    ImageView imgMute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        addViews();
    }

    private void addViews() {
        imgMute = (ImageView) findViewById(R.id.imgMute);
        prefs = getSharedPreferences("game", MODE_PRIVATE);
        isMute = prefs.getBoolean("isMute", false);
        lblHighScore = (TextView) findViewById(R.id.lblHighScore);
        hightScore = prefs.getInt("highscore", 0);
        lblHighScore.setText(hightScore +"");
        if (isMute) {
            imgMute.setImageResource(R.drawable.baseline_volume_off_24);
        } else {
            imgMute.setImageResource(R.drawable.baseline_volume_up_24);
        }
    }

    public void playGame(View view) {

        Intent intent = new Intent(MainActivity.this, GameActivity.class);
        startActivity(intent);
    }

    public void mute(View view) {
        isMute = !isMute;
        if (isMute) {
            imgMute.setImageResource(R.drawable.baseline_volume_off_24);
        } else {
            imgMute.setImageResource(R.drawable.baseline_volume_up_24);
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isMute", isMute);
        editor.apply();
    }
}