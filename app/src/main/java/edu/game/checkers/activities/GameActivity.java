package edu.game.checkers.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import edu.board.checkers.R;
import edu.game.checkers.logic.BoardView;
import edu.game.checkers.logic.Board;
import edu.game.checkers.logic.Position;

public class GameActivity extends AppCompatActivity {

    protected int options;
    protected Board board;
    protected BoardView boardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    protected void initGame()
    {
        // override options if specified
        Bundle bundle = getIntent().getExtras();
        int opts = bundle.getInt("options", -1);
        if(opts != -1)
            options = opts;
        else {
            SharedPreferences preferences = getSharedPreferences(OptionsActivity.OPTIONS_FILE,
                    MODE_PRIVATE);
            options = preferences.getInt(OptionsActivity.OPTIONS_KEY, 0);
        }
        setContentView(R.layout.activity_game);

        board = new Board(options);

        LinearLayout surface = (LinearLayout) findViewById(R.id.game_layout);
        boardView = new BoardView(this, board.getPieces());
        boardView.setOnTouchListener(new TouchManager());
        surface.addView(boardView);
    }

    // click performed by local player
    public void localClick(Position position) {
        if (board.canBeSelected(position)){
            board.selectPiece(position);
            boardView.setHints(board.getSelectedPiece().
                    getValidPositions(board.getOptions(), board.getPieces()));
            boardView.postInvalidate();
        }
        else if (board.canSelectedPieceBeMoved(position)){
            board.moveSelectedPiece(position);
            boardView.setHints(null);
            boardView.postInvalidate();
        }
    }

    // click performed by remote player (computer or online)
    public void remoteClick(Position position)
    {
        if (board.canBeSelected(position)){
            board.selectPiece(position);
        }
        else if (board.canSelectedPieceBeMoved(position)){
            board.moveSelectedPiece(position);
            boardView.postInvalidate();
        }
    }

    public void undoMove(View view) {
        board.undoMove();
        boardView.setPieces(board.getPieces());
        if(board.getSelectedPiece() != null)
            boardView.setHints(board.getSelectedPiece()
                    .getValidPositions(board.getOptions(), board.getPieces()));
        else
            boardView.setHints(null);
        boardView.postInvalidate();
    }

    private class TouchManager implements View.OnTouchListener{

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int tileSize = v.getWidth() / 8;

            int x = ((int) event.getX()) / tileSize;
            int y = ((int) event.getY()) / tileSize;

            if(x < 0 || x >= 8 || y < 0 || y >= 8)
                return false;

            Position position = new Position(x, y);

            localClick(position);

            return true;
        }
    }
}
