package com.backtracking.chess;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.backtracking.chess.UtilityFragments.AboutFragment;
import com.backtracking.chess.UtilityFragments.SettingsFragment;
import com.backtracking.chess.UtilityFragments.UtilityFragment;

public class MainActivity extends AppCompatActivity implements UtilityFragment.IUtilityFragment{

    private FrameLayout utilityFrame;
    private UtilityFragment utilityFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        utilityFrame = findViewById(R.id.utility_fragment_frame);
        utilityFrame.bringToFront();

        Button startGameButton = findViewById(R.id.start_game_button);
        startGameButton.setOnClickListener(view -> {
            Intent chooseModeActivity = new Intent(getApplicationContext(), CategoryActivity.class);
            startActivity(chooseModeActivity);
        });

        Button settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(view -> {
            utilityFragment = new SettingsFragment();
            getFragmentManager().beginTransaction().add(
                    R.id.utility_fragment_frame, utilityFragment).commit();
            utilityFrame.setVisibility(View.VISIBLE);
        });

        Button aboutButton = findViewById(R.id.about_button);
        aboutButton.setOnClickListener(view -> {
            utilityFragment = new AboutFragment();
            getFragmentManager().beginTransaction().add(
                    R.id.utility_fragment_frame, utilityFragment).commit();
            utilityFrame.setVisibility(View.VISIBLE);
        });

    }

    @Override
    public void closeUtility() {
        utilityFrame.setVisibility(View.GONE);
        getFragmentManager().beginTransaction().remove(utilityFragment).commit();
    }
}
