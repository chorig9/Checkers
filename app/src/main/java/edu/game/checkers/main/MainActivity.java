package edu.game.checkers.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import edu.game.checkers.R;
import edu.game.checkers.logic.Game;
import edu.game.checkers.logic.PlayerLocal;

public class MainActivity extends AppCompatActivity {

    private int options = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        loadOptions();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadOptions();
    }

    private void loadOptions()
    {
        SharedPreferences preferences = getSharedPreferences(OptionsActivity.OPTIONS_FILE,
                MODE_PRIVATE);

        options = preferences.getInt("options", 0);
    }

    public void startGame(View view) {
        Intent gameIntent = new Intent(this, GameActivity.class);
        gameIntent.putExtra("options", options);
        startActivity(gameIntent);
    }

    public void showOptions(View view) {
        Intent optionsIntent = new Intent(this, OptionsActivity.class);
        startActivity(optionsIntent);
    }
}
