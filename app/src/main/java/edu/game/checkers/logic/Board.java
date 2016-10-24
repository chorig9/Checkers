package edu.game.checkers.logic;

import java.util.ArrayList;

public class Board{

    public static final int backwardCapture = 1,
                            flyingKing = 2,
                            optimalCapture = 4;

    public enum Player {WHITE, BLACK}

    private Piece[][] pieces = new Piece[8][8];
    private int options;

    private Player currentPlayer;
    private Piece selectedPiece;
    private boolean moved = false; // have player moved any piece in this turn(is it multiple capture)?
    private ArrayList<GameState> history;

    public Board(int options)
    {
        this.options = options;
        history = new ArrayList<>();

        for(int x = 0; x < 8; x++)
        {
            for(int y = 0; y < 3; y++)
            {
                if((x + y) % 2 != 0)
                    pieces[x][y] = new Men(new Position(x, y), Player.BLACK);
            }
            for(int y = 7; y >= 5; y--)
            {
                if((x + y) % 2 != 0)
                    pieces[x][y] = new Men(new Position(x, y), Player.WHITE);
            }
        }

        currentPlayer = Player.WHITE;
    }

    public boolean canBeSelected(Position position)
    {
        boolean optimalCapture = isOptionEnabled(Board.optimalCapture);

        Piece piece = pieces[position.x][position.y];

        // if no piece can jump or this piece can jump and jump is optimal
        return !moved && piece != null && piece.getOwner() == currentPlayer
                && (!canAnyPieceJump(currentPlayer) || (piece.canCapture(options, pieces)
                && (!optimalCapture || getMaxNumberOfCaptures(currentPlayer)
                == piece.optimalMoveCaptures(options, pieces))));
    }

    public void selectPiece(Position position)
    {
        selectedPiece = pieces[position.x][position.y];
    }

    public boolean canSelectedPieceBeMoved(Position position)
    {
        return selectedPiece != null && selectedPiece.isMoveValid(position, options, pieces);
    }

    public void moveSelectedPiece(Position position)
    {
        if(selectedPiece != null && selectedPiece.isMoveValid(position, options, pieces))
        {
            saveState();

            boolean captured = selectedPiece.isMoveCapturing(position, options, pieces);
            selectedPiece.moveTo(position, pieces);

            if(!selectedPiece.canCapture(options, pieces) || !captured)
                nextTurn();

            if(isGameOver())
            {

            }
        }
    }

    private void nextTurn()
    {
        moved = false;
        selectedPiece = null;

        currentPlayer = currentPlayer == Player.WHITE ? Player.BLACK : Player.WHITE;
    }

    private boolean isGameOver()
    {
        //TODO
        return false;
    }

    private void saveState()
    {
        Piece[][] copyPieces = new Piece[8][8];

        for(int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (pieces[i][j] == null)
                    copyPieces[i][j] = null;
                else
                    copyPieces[i][j] = pieces[i][j].copy();
            }
        }

        history.add(new GameState(copyPieces, selectedPiece.position.copy(), moved, currentPlayer));
    }

    public void undoMove()
    {
        if(history.size() == 0)
            return;

        GameState pastState = history.get(history.size() - 1);

        moved = pastState.moved;
        currentPlayer = pastState.currentPlayer;
        pieces = pastState.pieces;

        // if in the middle of move - keep piece selected
        if(moved)
            selectedPiece = pieces[pastState.selectedPiecePosition.x][pastState.selectedPiecePosition.y];
        else
            selectedPiece = null;

        history.remove(history.size() - 1);
    }

    public Player getCurrentPlayer()
    {
        return currentPlayer;
    }

    public Piece getSelectedPiece()
    {
        return selectedPiece;
    }

    public boolean canAnyPieceJump(Player player)
    {
        for(int x = 0; x <8; x++)
        {
            for(int y = 0; y < 8; y++)
            {
                if(pieces[x][y] != null && pieces[x][y].getOwner() == player
                    && pieces[x][y].canCapture(options, pieces))
                    return true;
            }
        }

        return false;
    }

    public int getMaxNumberOfCaptures(Player player)
    {
        int max = 0;
        for(int x = 0; x <8; x++)
        {
            for(int y = 0; y < 8; y++)
            {
                Piece piece = pieces[x][y];
                if(piece != null && piece.getOwner() == player
                    && piece.canCapture(options, pieces))
                {
                    int n = piece.optimalMoveCaptures(options, pieces);

                    max = (n > max) ? n : max;
                }
            }
        }
        return max;
    }

    public int getOptions()
    {
        return options;
    }

    public Piece[][] getPieces()
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
