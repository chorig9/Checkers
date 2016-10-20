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

    private class State {

        Piece[][] pieces;
        Position selectedPiecePosition;
        boolean moved;
        int currentPlayer;

        State(Piece[][] pieces, Position selectedPiecePosition, boolean moved, int currentPlayer)
        {
            this.pieces = pieces;
            this.selectedPiecePosition = selectedPiecePosition;
            this.moved = moved;
            this.currentPlayer = currentPlayer;
        }
    }

    private Player[] players = new Player[2];
    private int currentPlayer = 0;
    private Piece selectedPiece;
    private boolean moved = false; // have player moved any piece in this turn(is it multiple capture)?
    private Piece[][] pieces = new Piece[8][8];
    private BoardView view;
    private int options;
    private ArrayList<State> history = new ArrayList<>();
    final TouchManager touchManager = new TouchManager();

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

            players[currentPlayer].turnOn();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }

    public void selectPiece(Position position)
    {
        selectedPiece = pieces[position.x][position.y];
    }

    public void showHints()
    {
        view.setHints(selectedPiece.getValidPositions(options, pieces));
        view.postInvalidate();
    }

    public boolean canBeSelected(Position position)
    {
        boolean optimalCapture = isOptionEnabled(Game.optimalCapture);

        Piece piece = pieces[position.x][position.y];

        // if no piece can jump or this piece can jump and jump is optimal
        return !moved && piece != null && piece.getOwner() == players[currentPlayer]
                && (!piece.getOwner().canAnyPieceJump() || (piece.canCapture(options, pieces)
                && (!optimalCapture || piece.getOwner().getMaxNumberOfCaptures()
                == piece.optimalMoveCaptures(options, pieces))));
    }

    public void moveSelectedPiece(Position position)
    {
        if(selectedPiece != null && selectedPiece.isMoveValid(position, options, pieces))
        {
            saveState();

            boolean captured = selectedPiece.isMoveCapturing(position, options, pieces);
            selectedPiece.moveTo(position, pieces);
            view.setHints(null);
            view.postInvalidate();

            if(!selectedPiece.canCapture(options, pieces) || !captured)
                nextTurn();

            if(isGameOver())
            {

            }
        }
    }

    public void nextTurn()
    {
        moved = false;
        selectedPiece = null;

        players[currentPlayer].turnOff();
        currentPlayer ^= 1;
        players[currentPlayer].turnOn();
    }

    private boolean isGameOver()
    {
        //TODO
        return false;
    }

    private void saveState()
    {
        Piece[][] copyPieces = new Piece[8][8];

        for(int i = 0; i < 8; i++)
        {
            for(int j = 0; j < 8; j++)
            {
                if(pieces[i][j] == null)
                    copyPieces[i][j] = null;
                else
                    copyPieces[i][j] = pieces[i][j].copy();
            }
        }

        history.add(new State(copyPieces, selectedPiece.position.copy(), moved, currentPlayer));
    }

    public void undoMove()
    {
        if(history.size() == 0)
            return;

        State pastState = history.get(history.size() - 1);

        moved = pastState.moved;

        if(currentPlayer != pastState.currentPlayer)
        {
            currentPlayer = pastState.currentPlayer;
            players[currentPlayer].turnOn();
            players[currentPlayer ^ 1].turnOff();
        }

        pieces = pastState.pieces;
        view.setPieces(pieces);

        selectedPiece = pieces[pastState.selectedPiecePosition.x][pastState.selectedPiecePosition.y];

        history.remove(history.size() - 1);

        if(players[currentPlayer] instanceof PlayerLocal)
            showHints();
        else
            view.postInvalidate();
    }

    public int getOptions()
    {
        return options;
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

}
