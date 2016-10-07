package edu.game.checkers.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import edu.game.checkers.R;

public class SelectTypeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.select_type_activity);
    }

    public void selectGame(View view) {
        Intent gameIntent = new Intent(this, GameActivity.class);

        Bundle bundle = getIntent().getExtras();
        int options = bundle.getInt("options");

        gameIntent.putExtra("options", options);

        // tags of buttons must be the same as class names of players (PlayerLocal, PlayerNetwork)
        gameIntent.putExtra("type", view.getTag().toString());
        startActivity(gameIntent);
    }
}
