package edu.game.checkers.main;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

import edu.game.checkers.R;
import edu.game.checkers.logic.Game;
import edu.game.checkers.logic.PlayerLocal;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        BoardView view = new BoardView(this);
        Game game = new Game(view, PlayerLocal.class, PlayerLocal.class, Game.backwardCapture | Game.obligatoryCapture | Game.flyingKing);
        game.start();
        setContentView(view);
    }
}
