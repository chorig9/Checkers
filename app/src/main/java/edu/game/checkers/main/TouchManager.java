package edu.game.checkers.main;

import android.view.MotionEvent;
import android.view.View;

import edu.game.checkers.logic.PlayerLocal;
import edu.game.checkers.logic.Position;

public class TouchManager implements View.OnTouchListener{

    private PlayerLocal user;

    public void setUser(PlayerLocal user)
    {
        this.user = user;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        if(user == null)
            return false;

        BoardView boardView;

        try
        {
            boardView = (BoardView) view;

            int tileSize = view.getWidth() / 8;

            int x = ((int) motionEvent.getX()) / tileSize;
            int y = ((int) motionEvent.getY()) / tileSize;

            if(x < 0 || x >= 8 || y < 0 || y >= 8)
                return false;

            // piece exists and is owned by 'user' and if there is any piece that must jump(capture) this is one of them
            if(user.getPieces()[x][y] != null && user.getPieces()[x][y].getOwner() == user &&
                    (!user.canAnyPieceJump() || (user.canAnyPieceJump() && user.getPieces()[x][y].canJump(user.getOptions(), user.getPieces()))))
            {
                boardView.setHints(user.getPieces()[x][y].getValidPositions(user.getOptions(), user.getPieces()));
                boardView.postInvalidate();

                user.setSource(new Position(x, y));
            }
            else
            {
                boardView.setHints(null);
                boardView.postInvalidate();

                user.setTarget(new Position(x, y));
            }

            return true;
        }
        catch(ClassCastException e) /// TODO-??????
        {
            return false;
        }
    }
}
