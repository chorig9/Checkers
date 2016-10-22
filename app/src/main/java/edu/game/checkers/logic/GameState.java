package edu.game.checkers.logic;

public class GameState {

    public Piece[][] pieces;
    public Position selectedPiecePosition;
    public boolean moved;
    public Board.Player currentPlayer;

    public GameState(Piece[][] pieces, Position selectedPiecePosition,
                     boolean moved, Board.Player currentPlayer)
    {
        this.pieces = pieces;
        this.selectedPiecePosition = selectedPiecePosition;
        this.moved = moved;
        this.currentPlayer = currentPlayer;
    }
}
