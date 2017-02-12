package edu.game.checkers.logic;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Men extends  Piece{

    private int orientation;

    public Men(Position position, Game.Player owner, Board board)
    {
        super(position, owner, board);
        orientation = (owner == Game.Player.WHITE) ? -1 : 1;
    }

    @Override
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(owner == Game.Player.WHITE ? Color.WHITE : Color.BLACK);

        int tileSize = canvas.getWidth() / 8;

        float cx = (float)((position.x + 0.5) * tileSize);
        float cy = (float)((position.y + 0.5) * tileSize);
        float radius = (float)(0.9 * tileSize / 2);

        canvas.drawCircle(cx, cy, radius, paint);
    }

    @Override
    public boolean isMoveCorrectAndCapturing(Position target) {
        return isTargetPositionEmpty(target)
                && isThereOpponentPieceOnWay(target)
                && isCaptureDirectionAndLengthCorrect(target);
    }

    @Override
    public boolean isMoveCorrectAndDoNotCapture(Position target) {
        return isTargetPositionEmpty(target)
                && isMoveDirectionAndLengthCorrect(target);
    }

    @Override
    public boolean canCapture() {
        int forY = position.y + 2 * orientation;
        int backY = position.y - 2 * orientation;

        for(int px = -2; px <= 2; px+=4) {
            Position pos = new Position(position.x + px, forY);
            if(pos.isInRange() && isMoveCorrectAndCapturing(pos))
                return true;
        }

        if(Game.isOptionEnabled(board.options, Game.backwardCapture)) {
            for(int px = -2; px <= 2; px+=4)
            {
                Position pos = new Position(position.x + px, backY);
                if(pos.isInRange() && isMoveCorrectAndCapturing(pos))
                    return true;
            }
        }

        return false;
    }

    // additionally converts men to king
    @Override
    public void moveTo(Position position) {
        super.moveTo(position);

        if((orientation == 1 && position.y == 7) || (orientation == - 1 && position.y == 0))
            board.pieces[position.x][position.y] = new King(position, owner, board);
    }

    @Override
    public Men copy()
    {
        return new Men(new Position(position.x, position.y), owner, board);
    }


    private boolean isCaptureDirectionAndLengthCorrect(Position target){
        boolean backwardJumpAllowed = Game.isOptionEnabled(board.options, Game.backwardCapture);

        return Math.abs(target.y - position.y) == Math.abs(target.x - position.x)
                && ((backwardJumpAllowed && Math.abs(target.y - position.y) == 2)
                    || target.y - position.y == 2 * orientation);
    }

    private boolean isMoveDirectionAndLengthCorrect(Position target){
        return Math.abs(target.x - position.x) == 1 && target.y - position.y == orientation;
    }

    private boolean isThereOpponentPieceOnWay(Position target){
        return board.pieces[(target.x + position.x)/2][(target.y + position.y)/2] != null
                && board.pieces[(target.x + position.x)/2][(target.y + position.y)/2].getOwner() != owner;
    }
}
