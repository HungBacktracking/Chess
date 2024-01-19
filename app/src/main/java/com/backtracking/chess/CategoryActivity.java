package com.backtracking.chess;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class CategoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        Button playOnlineButton = findViewById(R.id.find_match_button);
        playOnlineButton.setOnClickListener(view -> {
            Intent startGameActivity = new Intent(getApplicationContext(), ChooseModeActivity.class);
            startGameActivity.putExtra("category", "online");
            startActivity(startGameActivity);
        });

        Button playLocalButton = findViewById(R.id.play_local_button);
        playLocalButton.setOnClickListener(view -> {
            Intent startGameActivity = new Intent(getApplicationContext(), ChooseModeActivity.class);
            startGameActivity.putExtra("category", "local");
            startActivity(startGameActivity);
        });

        Button playWithAIButton = findViewById(R.id.play_with_AI_button);
        playWithAIButton.setOnClickListener(view -> {
            Intent startGameActivity = new Intent(getApplicationContext(), ChooseModeActivity.class);
            startGameActivity.putExtra("category", "AI");
            startActivity(startGameActivity);
        });
    }
}
