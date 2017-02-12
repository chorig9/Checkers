package edu.game.checkers.logic;

public class Board {

    Piece pieces[][];
    int options;

    Board(Piece pieces[][], int options){
        this.pieces = pieces;
        this.options = options;
    }

    Board copy(){
        Piece[][] copyPieces = new Piece[pieces.length][pieces.length];
        Board newBoard = new Board(copyPieces, options);

        for(int i = 0; i < pieces.length; i++) {
            for (int j = 0; j < pieces.length; j++) {
                if (pieces[i][j] == null)
                    copyPieces[i][j] = null;
                else {
                    copyPieces[i][j] = pieces[i][j].copy();
                    copyPieces[i][j].board = newBoard;
                }
            }
        }

        return newBoard;
    }

    Piece[][] getCopyOfPieces(){
        Piece[][] copyPieces = new Piece[pieces.length][pieces.length];
        for(int i = 0; i < pieces.length; i++) {
            for (int j = 0; j < pieces.length; j++) {
                if (pieces[i][j] == null)
                    copyPieces[i][j] = null;
                else {
                    copyPieces[i][j] = pieces[i][j].copy();
                }
            }
        }

        return copyPieces;
    }

    void replaceCurrentPieces(Piece newPieces[][]){
        for(int i = 0; i < pieces.length; i++) {
            for (int j = 0; j < pieces.length; j++) {
                pieces[i][j] = newPieces[i][j];
            }
        }
    }

}
