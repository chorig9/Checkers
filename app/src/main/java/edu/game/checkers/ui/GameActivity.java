package edu.game.checkers.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import edu.board.checkers.R;
import edu.game.checkers.logic.GameView;
import edu.game.checkers.logic.Game;
import edu.game.checkers.logic.Position;

public class GameActivity extends AppCompatActivity {

    protected int options;
    protected Game board;
    protected GameView boardView;
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
        boardView = new GameView(this);
        board = new Game(options, boardView);

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
        turnView.setColor(board.getCurrentPlayer() == Game.Player.WHITE ?
                Color.WHITE : Color.BLACK);
    }

    protected TouchManager createTouchManager(){
        return new TouchManager();
    }

    private void gameOver()
    {
        new PostAlertDialog(this, new Handler()).createExitDialog("End", "Game Over");
    }

    protected class TouchManager implements View.OnTouchListener{

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int tileSize = v.getWidth() / 8;
            GameView gameView = (GameView) v;
            Position touchPosition = new Position(
                    (int) (event.getX() / tileSize) ,
                    (int) (event.getY() / tileSize)
            );

            Position realPosition = gameView.calculatePosition(touchPosition);

            if(realPosition == null)
                return false;

            board.clicked(realPosition, true);
            updateTurnView();

            return true;
        }
    }

    protected void updateTurnView(){
        turnView.setColor(board.getCurrentPlayer() == Game.Player.WHITE ?
                Color.WHITE : Color.BLACK);
        turnView.postInvalidate();
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
