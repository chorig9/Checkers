package edu.game.checkers.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import edu.board.checkers.R;


public class MainActivity extends AppCompatActivity {

    private int options = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
    }

    public void startGame(View view) {
        Intent selectTypeIntent = new Intent(this, SelectTypeActivity.class);
        startActivity(selectTypeIntent);
    }

    public void showOptions(View view) {
        Intent optionsIntent = new Intent(this, OptionsActivity.class);
        startActivity(optionsIntent);
    }
}
