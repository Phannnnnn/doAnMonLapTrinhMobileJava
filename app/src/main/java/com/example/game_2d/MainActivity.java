package com.example.game_2d;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private boolean isMute;
    ImageView imgMute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        addViews();
    }

    private void addViews() {
        imgMute = (ImageView) findViewById(R.id.imgMute);
        prefs = getSharedPreferences("game", MODE_PRIVATE);
        isMute = prefs.getBoolean("isMute", false);

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