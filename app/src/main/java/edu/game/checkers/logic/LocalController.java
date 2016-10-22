package edu.game.checkers.logic;

public class LocalController extends GameController{

    public LocalController(Board board, BoardView boardView) {
        super(board, boardView, Board.Player.WHITE);
    }

    @Override
    public boolean isCurrentPlayer(Board.Player player){
        return true; // for local board, every player has touch input
    }

    @Override
    protected  void onEvent(Position position)
    {
        boardView.setHints(null);
        boardView.postInvalidate();
    }
}
