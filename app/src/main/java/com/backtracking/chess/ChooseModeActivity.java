package com.backtracking.chess;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ChooseModeActivity extends AppCompatActivity {

    String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_mode);

        category = "online";

        handleBack();
        handleFormatGame();
        handleStartGame();
    }

    private void handleBack() {
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void handleFormatGame() {
        Button playOnline = findViewById(R.id.play_online_button);
        Button playLocal = findViewById(R.id.play_local_button);
        Button playAI = findViewById(R.id.play_AI_button);
        playOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                category = "online";
                playOnline.setBackground(getResources().getDrawable(R.drawable.active_category_button));
                playLocal.setBackground(getResources().getDrawable(R.drawable.category_button));
                playAI.setBackground(getResources().getDrawable(R.drawable.category_button));
            }
        });

        playLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                category = "local";
                playOnline.setBackground(getResources().getDrawable(R.drawable.category_button));
                playLocal.setBackground(getResources().getDrawable(R.drawable.active_category_button));
                playAI.setBackground(getResources().getDrawable(R.drawable.category_button));
            }
        });

        playAI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                category = "AI";
                playOnline.setBackground(getResources().getDrawable(R.drawable.category_button));
                playLocal.setBackground(getResources().getDrawable(R.drawable.category_button));
                playAI.setBackground(getResources().getDrawable(R.drawable.active_category_button));
            }
        });
    }

    private void handleStartGame() {
        LinearLayout startClassicButton = findViewById(R.id.classic_mode_button);
        startClassicButton.setOnClickListener(view -> {
            Intent startGameActivity = new Intent(getApplicationContext(), GameActivity.class);
            startGameActivity.putExtra("mode", "classic");
            startGameActivity.putExtra("category", category);
            startActivity(startGameActivity);
        });

        LinearLayout startTransformerButton = findViewById(R.id.transformer_mode_button);
        startTransformerButton.setOnClickListener(view -> {
            Intent startGameActivity = new Intent(getApplicationContext(), GameActivity.class);
            startGameActivity.putExtra("mode", "transformer");
            startGameActivity.putExtra("category", category);
            startActivity(startGameActivity);
        });

        LinearLayout startHiddenPieceButton = findViewById(R.id.hidden_piece_mode_button);
        startHiddenPieceButton.setOnClickListener(view -> {
            Intent startGameActivity = new Intent(getApplicationContext(), GameActivity.class);
            startGameActivity.putExtra("mode", "hidden");
            startGameActivity.putExtra("category", category);
            startActivity(startGameActivity);
        });
    }
}
