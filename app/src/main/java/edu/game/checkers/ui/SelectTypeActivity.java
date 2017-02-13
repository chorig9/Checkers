package edu.game.checkers.ui;

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
        Intent intent;

        switch(view.getId())
        {
            case R.id.local:
                intent = new Intent(this, LocalGameActivity.class);
                break;
            case R.id.network:
                intent = new Intent(this, NetworkActivity.class);
                break;
            default:
                return;
        }

        startActivity(intent);
    }
}
