package edu.game.checkers.logic;

import android.view.MotionEvent;
import android.view.View;

public abstract class GameController {

    final Board board;
    final BoardView boardView;

    // localPlayer - which player is local (if both are then it doesn't matter)
    public GameController(final Board board, final BoardView boardView,
                          final Board.Player localPlayer)
    {
        this.board = board;
        this.boardView = boardView;

        boardView.setPieces(board.getPieces());

        boardView.setOnTouchListener(new View.OnTouchListener(){

            Board.Player player = localPlayer;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int tileSize = v.getWidth() / 8;

                int x = ((int) event.getX()) / tileSize;
                int y = ((int) event.getY()) / tileSize;

                if(x < 0 || x >= 8 || y < 0 || y >= 8)
                    return false;

                Position position = new Position(x, y);

                // TODO - extend if
                if(isCurrentPlayer(player))
                {
                    if(board.canBeSelected(position)){
                        board.selectPiece(position);
                        boardView.setHints(board.getSelectedPiece().
                                getValidPositions(board.getOptions(), board.getPieces()));
                        boardView.postInvalidate();
                    }
                    else if(board.canSelectedPieceBeMoved(position)){
                        board.moveSelectedPiece(position);
                        onEvent(position);
                    }
                }

                return true;
            }
        });
    }

    public void clicked(Position position)
    {
        if(board.canBeSelected(position)){
            board.selectPiece(position);
        }
        else if(board.canSelectedPieceBeMoved(position)){
            board.moveSelectedPiece(position);
            onEvent(position);
        }
    }

    public boolean isCurrentPlayer(Board.Player player)
    {
        return board.getCurrentPlayer() == player;
    }

    // called when piece is moved or moved is undone
    protected abstract void onEvent(Position position);
}
