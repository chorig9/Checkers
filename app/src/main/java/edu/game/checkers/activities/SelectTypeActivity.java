package edu.game.checkers.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import edu.board.checkers.R;

public class SelectTypeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_select_type);
    }

    public void selectGame(View view) {
        Intent gameIntent;

        switch(view.getId())
        {
            case R.id.local:
                gameIntent = new Intent(this, LocalGameActivity.class);
                break;
            case R.id.network:
                gameIntent = new Intent(this, NetworkGameActivity.class);
                break;
            default:
                return;
        }

        Bundle bundle = getIntent().getExtras();
        int options = bundle.getInt("options");

        gameIntent.putExtra("options", options);
        startActivity(gameIntent);
    }
}
