package com.backtracking.chess;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ChooseModeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_mode);

        Button startClassicButton = findViewById(R.id.classic_mode_button);
        startClassicButton.setOnClickListener(view -> {
            Intent startGameActivity = new Intent(getApplicationContext(), GameActivity.class);
            startGameActivity.putExtra("mode", "classic");
            startActivity(startGameActivity);
        });

        Button startTransformerButton = findViewById(R.id.transformer_mode_button);
        startTransformerButton.setOnClickListener(view -> {
            Intent startGameActivity = new Intent(getApplicationContext(), GameActivity.class);
            startGameActivity.putExtra("mode", "transformer");
            startActivity(startGameActivity);
        });

        Button startHiddenPieceButton = findViewById(R.id.special_display_button);
        startHiddenPieceButton.setOnClickListener(view -> {
            Intent startGameActivity = new Intent(getApplicationContext(), GameActivity.class);
            startGameActivity.putExtra("mode", "hidden");
            startActivity(startGameActivity);
        });
    }
}
