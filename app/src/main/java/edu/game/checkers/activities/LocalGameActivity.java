package edu.game.checkers.activities;

import android.os.Bundle;

public class LocalGameActivity extends GameActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initGame();
    }
}
