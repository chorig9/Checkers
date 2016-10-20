package edu.game.checkers.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import edu.game.checkers.R;
import edu.game.checkers.logic.Game;
import edu.game.checkers.logic.Player;
import edu.game.checkers.logic.PlayerLocal;

public class GameActivity extends AppCompatActivity {

    private Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_game);

        Bundle bundle = getIntent().getExtras();
        int options = bundle.getInt("options");
        String playerTypeName = bundle.getString("type");
        Class<? extends Player> playerType;

        try
        {
            playerType = (Class<? extends Player>) Class.forName(playerTypeName);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            finish();
            return;
        }

        LinearLayout surface = (LinearLayout) findViewById(R.id.game_layout);
        BoardView boardView = new BoardView(this);
        surface.addView(boardView);

        game = new Game(boardView, PlayerLocal.class, playerType, options);
        game.start();
    }

    public void moveBack(View view) {
        game.moveBack();
    }
}
