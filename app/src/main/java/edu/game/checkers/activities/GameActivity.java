package edu.game.checkers.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import edu.board.checkers.R;
import edu.game.checkers.logic.BoardView;
import edu.game.checkers.logic.Board;
import edu.game.checkers.logic.GameController;
import edu.game.checkers.logic.LocalController;

public class GameActivity extends AppCompatActivity {

    private Board board;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_game);

        Bundle bundle = getIntent().getExtras();
        int options = bundle.getInt("options");

        LinearLayout surface = (LinearLayout) findViewById(R.id.game_layout);
        BoardView boardView = new BoardView(this);
        surface.addView(boardView);

        board = new Board(options);
        GameController controller = new LocalController(board, boardView);
    }

    public void undoMove(View view) {
        board.undoMove();
    }
}
