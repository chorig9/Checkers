package edu.game.checkers.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import edu.game.checkers.logic.Game;
import edu.game.checkers.logic.PlayerLocal;

public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Bundle bundle = getIntent().getExtras();
        int options = bundle.getInt("options");

        BoardView boardView = new BoardView(this);

        Game game = new Game(boardView, PlayerLocal.class, PlayerLocal.class, options);
        game.start();
        setContentView(boardView);
    }
}
