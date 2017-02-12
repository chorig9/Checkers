package edu.game.checkers.logic;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class King extends Piece{

    public King(Position position, Game.Player owner, Board board)
    {
        super(position, owner, board);
    }

    @Override
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(owner == Game.Player.WHITE ? Color.WHITE : Color.BLACK);

        int tileSize = canvas.getWidth() / 8;

        float cx = (float)((position.x + 0.5) * tileSize);
        float cy = (float)((position.y + 0.5) * tileSize);
        float radius = (float)(0.9 * tileSize / 2);

        float width = (float)(0.1 * radius);
        canvas.drawCircle(cx, cy, radius, paint);
        paint.setColor(owner == Game.Player.BLACK ? Color.WHITE : Color.BLACK);
        canvas.drawCircle(cx, cy, radius / 2, paint);
        paint.setColor(owner == Game.Player.WHITE ? Color.WHITE : Color.BLACK);
        canvas.drawCircle(cx, cy, radius / 2 - width, paint);
    }

    @Override
    public boolean isMoveCorrectAndCapturing(Position target) {
        return isTargetPositionEmpty(target)
                && isCaptureDirectionAndLengthCorrect(target)
                && checkForPieces(target) == PiecesOnWay.One;
    }

    @Override
    public boolean isMoveCorrectAndDoNotCapture(Position target) {
        return isTargetPositionEmpty(target)
                && isMoveDirectionAndLengthCorrect(target)
                && checkForPieces(target) == PiecesOnWay.None;
    }

    @Override
    public King copy()
    {
        return new King(new Position(position.x, position.y), owner, board);
    }

    @Override
    public boolean canCapture() {
        int range;

        if(!Game.isOptionEnabled(board.options, Game.flyingKing))
            range = 2;
        else
            range = 7;

        for(int px = -range; px <= range; px++)
        {
            for(int k = -1; k <= 1; k += 2)
            {
                Position pos = new Position(position.x + px, position.y + k * px);
                if(!pos.equals(position) && pos.isInRange() && isMoveCorrectAndCapturing(pos))
                    return true;
            }
        }

        return false;
    }

    private boolean isMoveDirectionAndLengthCorrect(Position target){
        if(Game.isOptionEnabled(board.options, Game.flyingKing)){
            return Math.abs(target.x - position.x) == 1 && Math.abs(target.y - position.y) == 1;
        }
        else{
            return Math.abs(target.x - position.x) == Math.abs(target.y - position.y);
        }
    }

    private boolean isCaptureDirectionAndLengthCorrect(Position target){
        if(!Game.isOptionEnabled(board.options, Game.flyingKing)) {
            return Math.abs(target.x - position.x) == 2 && Math.abs(target.y - position.y) == 2;
        }
        else {
            return Math.abs(target.x - position.x) == Math.abs(target.y - position.y);
        }
    }

    // Multiple - more than one (owner doesn't matter) or 1 piece of current player
    private enum PiecesOnWay {None, One, Multiple}

    private PiecesOnWay checkForPieces(Position target) {
        boolean pieceOnWay = false;
        int px = (target.x > position.x) ? 1 : -1;
        int py = (target.y > position.y) ? 1 : -1;

        for(int x = position.x + px, y = position.y + py; x != target.x; x+=px, y+=py) {
            if(board.pieces[x][y] != null)
            {
                if(board.pieces[x][y].getOwner() == owner || pieceOnWay)
                    return PiecesOnWay.Multiple;
                else
                    pieceOnWay = true;
            }
        }

        return pieceOnWay ? PiecesOnWay.One : PiecesOnWay.None;
    }
}