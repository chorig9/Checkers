package edu.game.checkers.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
    protected TurnView turnView;

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
        int opts = -1;
        if(bundle != null)
            opts = bundle.getInt("options", -1);
        if(opts != -1)
            options = opts;
        else {
            SharedPreferences preferences = getSharedPreferences(OptionsActivity.OPTIONS_FILE,
                    MODE_PRIVATE);
            options = preferences.getInt(OptionsActivity.OPTIONS_KEY, 0);
        }
        setContentView(R.layout.activity_game);

        LinearLayout surface = (LinearLayout) findViewById(R.id.game_layout);
        boardView = new BoardView(this);
        board = new Board(options, boardView);

        boardView.setPieces(board.getPieces());
        boardView.setOnTouchListener(createTouchManager());
        surface.addView(boardView);

        LinearLayout turnLayout = (LinearLayout) findViewById(R.id.turn_layout);
        turnView = new TurnView(this);
        turnLayout.addView(turnView);

        turnView.setColor(Color.WHITE);
    }

    public void undoMove(View view) {
        board.undoMove();
        turnView.setColor(board.getCurrentPlayer() == Board.Player.WHITE ?
                Color.WHITE : Color.BLACK);
    }

    protected TouchManager createTouchManager(){
        return new TouchManager();
    }

//    private void gameOver()
//    {
//        new PostAlertDialog(this).createExitDialog("End", "Game Over");
//    }

    protected class TouchManager implements View.OnTouchListener{

        protected Position calculatePosition(View v, MotionEvent event){
            int tileSize = v.getWidth() / 8;

            int x = ((int) event.getX()) / tileSize;
            int y = ((int) event.getY()) / tileSize;

            if(x < 0 || x >= 8 || y < 0 || y >= 8)
                return null;

            if(((BoardView)v).getBoardRotation()){
                x = 7 - x;
                y = 7 - y;
            }

           return new Position(x, y);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Position position = calculatePosition(v, event);

            if(position == null)
                return false;

            board.clicked(position, true);
            turnView.setColor(board.getCurrentPlayer() == Board.Player.WHITE ?
                    Color.WHITE : Color.BLACK);

            return true;
        }
    }

    protected class TurnView extends View{

        private int color;
        private Paint paint;

        public TurnView(Context context) {
            super(context);
            paint = new Paint();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            paint.setColor(Color.BLACK);
            canvas.drawRect(0,0, canvas.getWidth(), canvas.getHeight(), paint);
            paint.setColor(color);
            canvas.drawRect(3,3, canvas.getWidth() - 3, canvas.getHeight() - 3, paint);
        }

        public void setColor(int color){
            this.color = color;
            invalidate();
        }
    }
}
