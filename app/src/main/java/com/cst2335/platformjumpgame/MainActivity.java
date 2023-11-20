package com.cst2335.platformjumpgame;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

public class MainActivity extends Activity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        if (getActionBar() != null) {
            getActionBar().hide();
        }

        gameView = new GameView(this);
        setContentView(gameView);

        int topScore;
        try (DatabaseHelper databaseHelper = new DatabaseHelper(this)) {
            topScore = databaseHelper.getTopScore();
        }
        Log.d("MainActivity", "Top Score: " + topScore);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }
}

