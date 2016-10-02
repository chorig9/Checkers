package edu.game.checkers.logic;

import android.util.Pair;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

import edu.game.checkers.main.BoardView;
import edu.game.checkers.main.TouchManager;

public class Game extends Thread{

    public static final int obligatoryCapture = 1,
                            backwardCapture = 2,
                            flyingKing = 4;

    private Player[] players = new Player[2];
    private Piece[][] pieces;
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
            pieces = new Piece[8][8];
            //TODO - initialize pieces

            Constructor<? extends Player> constructor;
            constructor = playerClass1.getDeclaredConstructor(Player.Color.class, Game.class);
            players[0] = constructor.newInstance(Player.Color.WHITE, this);

            constructor = playerClass2.getDeclaredConstructor(Player.Color.class, Game.class);
            players[1] = constructor.newInstance(Player.Color.BLACK, this);

            view.setPieces(pieces);
        }
        catch(Exception e)
        {
            //TODO
        }

    }

    public int getOptions()
    {
        return options;
    }

    public TouchManager getTouchManager()
    {
        return touchManager;
    }

    public Piece[][] getPieces()
    {
        return pieces;
    }

    public void setRunning(boolean running)
    {
        this.running = running;
    }

    @Override
    public void run()
    {
        boolean end = false;

        while(!end)
        {
            Pair<Position, Position> move;
            Piece piece;

            for(Player player : players)
            {
                do {
                    move = player.makeMove();
                    Position source = move.first, target = move.second;

                    history.add(pieces.clone());

                    piece = pieces[source.x][source.y];
                    piece.moveTo(target, pieces);

                    view.postInvalidate();
                }while(piece.canJump(pieces) && ((1 << obligatoryCapture) & options) != 0);
            }

            //TODO - end
        }
    }



}
