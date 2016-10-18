package edu.game.checkers.logic;

import android.graphics.Color;
import android.util.Pair;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

import edu.game.checkers.main.BoardView;
import edu.game.checkers.main.TouchManager;

public class Game extends Thread{

    public static final int backwardCapture = 1,
                            flyingKing = 2,
                            optimalCapture = 4;

    private Player[] players = new Player[2];
    private Piece[][] pieces = new Piece[8][8];
    private BoardView view;
    private int options;
    private ArrayList<Piece[][]> history = new ArrayList<>();
    private TouchManager touchManager = new TouchManager();
    private boolean running = true;

    public Game(BoardView view, Class<? extends Player> playerClass1,
                Class<? extends Player> playerClass2, int options)
    {
        this.view = view;
        this.options = options;

        view.setOnTouchListener(touchManager);

        try
        {
            Constructor<? extends Player> constructor;
            constructor = playerClass1.getDeclaredConstructor(int.class, Game.class);
            players[0] = constructor.newInstance(Color.WHITE, this);

            constructor = playerClass2.getDeclaredConstructor(int.class, Game.class);
            players[1] = constructor.newInstance(Color.BLACK, this);

            for(int x = 0; x < 8; x++)
            {
                for(int y = 0; y < 3; y++)
                {
                    if((x + y) % 2 != 0)
                        pieces[x][y] = new Men(new Position(x, y), players[1]);
                }
                for(int y = 7; y >= 5; y--)
                {
                    if((x + y) % 2 != 0)
                        pieces[x][y] = new Men(new Position(x, y), players[0]);
                }
            }

            view.setPieces(pieces);
            view.postInvalidate();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void run()
    {
        while(!isEndOfGame())
        {
            for(Player player : players)
            {
                Pair<Position, Position> move;
                Piece piece;
                Position capturedPiecePos;
                boolean captured = false;

                do {
                    move = player.makeMove();
                    Position source = move.first, target = move.second;

                    // TODO - clone is not working
                    history.add(pieces.clone());

                    piece = pieces[source.x][source.y];
                    capturedPiecePos = piece.moveTo(target, pieces);

                    if(capturedPiecePos != null) {
                        pieces[capturedPiecePos.x][capturedPiecePos.y] = null;
                        captured = true;
                    }

                    view.postInvalidate();
                }while(captured && piece.canJump(options, pieces));
            }

            //TODO - end
        }
    }

    private boolean isEndOfGame()
    {
        //TODO
        return false;
    }

    public int getOptions()
    {
        return options;
    }

    TouchManager getTouchManager()
    {
        return touchManager;
    }

    Piece[][] getPieces()
    {
        return pieces;
    }

    boolean isOptionEnabled(int option)
    {
        return isOptionEnabled(options, option);
    }

    public static boolean isOptionEnabled(int options, int option)
    {
        return (option & options) != 0;
    }

    public void setRunning(boolean running)
    {
        this.running = running;
    }

}
