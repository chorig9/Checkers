package edu.game.checkers.logic;

class GameState {

    Piece[][] pieces;
    Position selectedPiecePosition;
    boolean moved;
    Game.Player currentPlayer;

    GameState(Piece[][] pieces, Position selectedPiecePosition,
              boolean moved, Game.Player currentPlayer)
    {
        this.pieces = pieces;
        this.selectedPiecePosition = selectedPiecePosition;
        this.moved = moved;
        this.currentPlayer = currentPlayer;
    }
}
