package edu.game.checkers.logic;

import java.util.ArrayList;

public class Game {

    public static final int backwardCapture = 1,
                            flyingKing = 2,
                            optimalCapture = 4;

    public enum Player {WHITE, BLACK}

    public enum ClickResult {MOVED, SELECTED, NONE};

    private Board board;
    private Player currentPlayer;
    private Piece selectedPiece;
    private boolean moved = false; // have player moved any piece in this turn(is this multiple capture)?
    private ArrayList<GameState> history;
    private GameView gameView;

    public Game(int options, GameView gameView) {
        board = new Board(new Piece[8][8], options);
        history = new ArrayList<>();
        this.gameView = gameView;

        for(int x = 0; x < 8; x++)
        {
            for(int y = 0; y < 3; y++)
            {
                if((x + y) % 2 != 0)
                    board.pieces[x][y] = new Men(new Position(x, y), Player.BLACK, board);
            }
            for(int y = 7; y >= 5; y--)
            {
                if((x + y) % 2 != 0)
                    board.pieces[x][y] = new Men(new Position(x, y), Player.WHITE, board);
            }
        }

        currentPlayer = Player.WHITE;
    }

    public ClickResult clicked(Position position, boolean show){

        if (canPieceBeSelected(position)){
            selectPiece(position);
            if(show) {
                gameView.setHints(selectedPiece.getValidPositions());
                gameView.postInvalidate();
            }
            return ClickResult.SELECTED;
        }
        else if (canSelectedPieceBeMoved(position)){
            moveSelectedPiece(position);
            if(show) {
                gameView.setHints(null);
                gameView.postInvalidate();
            }
            return ClickResult.MOVED;
        }
        return ClickResult.NONE;
    }

    public boolean isGameOver() {
        //TODO

        int p1 = 0, p2 = 0;
        for(int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if(board.pieces[i][j] != null && board.pieces[i][j].getOwner() == Player.WHITE)
                    p1++;
                else if(board.pieces[i][j] != null && board.pieces[i][j].getOwner() == Player.BLACK)
                    p2++;
            }
        }
        if(p1 == 0 || p2 == 0)
            return true;
        else return false;
    }

    public void undoMove() {
        if(history.size() == 0)
            return;

        GameState pastState = history.remove(history.size() - 1);

        board.replaceCurrentPieces(pastState.pieces);
        moved = pastState.moved;
        currentPlayer = pastState.currentPlayer;

        Position position = pastState.selectedPiecePosition;
        selectedPiece = board.pieces[position.x][position.y];

        gameView.setHints(null);
        gameView.postInvalidate();
    }

    public Player getCurrentPlayer()
    {
        return currentPlayer;
    }

    public Piece getSelectedPiece()
    {
        return selectedPiece;
    }

    public int getOptions()
    {
        return board.options;
    }

    public Piece[][] getPieces()
    {
        return board.pieces;
    }


    public static boolean isOptionEnabled(int options, int option) {
        return (option & options) != 0;
    }

    private boolean isOptionEnabled(int option)
    {
        return isOptionEnabled(board.options, option);
    }

    private void nextTurn() {
        moved = false;
        selectedPiece = null;

        currentPlayer = currentPlayer == Player.WHITE ? Player.BLACK : Player.WHITE;
    }

    private void saveState() {
        GameState savedState = new GameState(board.getCopyOfPieces(), selectedPiece.position.copy(),
                moved, currentPlayer);

        history.add(savedState);
    }

    private boolean canAnyPieceCapture(Player player) {
        for(int x = 0; x <8; x++)
        {
            for(int y = 0; y < 8; y++)
            {
                if(board.pieces[x][y] != null && board.pieces[x][y].getOwner() == player
                        && board.pieces[x][y].canCapture())
                    return true;
            }
        }

        return false;
    }

    private int getMaxNumberOfCaptures(Player player) {
        int maxNumberOfCaptures = 0;
        for(int x = 0; x <8; x++)
        {
            for(int y = 0; y < 8; y++)
            {
                Piece piece = board.pieces[x][y];
                if(piece != null && piece.getOwner() == player
                        && piece.canCapture())
                {
                    int numberOfCaptures = piece.getNumberOfPiecesCapturedByOptimalMove();

                    if(numberOfCaptures > maxNumberOfCaptures)
                        maxNumberOfCaptures = numberOfCaptures;
                }
            }
        }
        return maxNumberOfCaptures;
    }

    private boolean canPieceBeSelected(Position position) {
        Piece piece = board.pieces[position.x][position.y];

        return isSelectionValid(piece)
                && (!canAnyPieceCapture(currentPlayer) || canPiecePerformCapture(piece));
    }
    
    private boolean isSelectionValid(Piece piece){
        return !moved && piece != null && piece.getOwner() == currentPlayer;
    }

    private boolean canPiecePerformCapture(Piece piece){
        boolean optimalCapture = isOptionEnabled(Game.optimalCapture);

        return (!optimalCapture && piece.canCapture())
                || (piece.getNumberOfPiecesCapturedByOptimalMove() == getMaxNumberOfCaptures(currentPlayer));
    }

    private void selectPiece(Position position) {
        selectedPiece = board.pieces[position.x][position.y];
    }

    private boolean canSelectedPieceBeMoved(Position position) {
        return selectedPiece != null && selectedPiece.isMoveValid(position);
    }

    private void moveSelectedPiece(Position position) {
        if(selectedPiece != null && selectedPiece.isMoveValid(position)) {
            saveState();

            boolean captured = selectedPiece.isMoveCorrectAndCapturing(position);
            selectedPiece.moveTo(position);

            if(!selectedPiece.canCapture() || !captured)
                nextTurn();
        }
    }


}
