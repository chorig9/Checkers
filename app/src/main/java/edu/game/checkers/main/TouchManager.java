package edu.game.checkers.main;

import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

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

        int tileSize = view.getWidth() / 8;

        int x = ((int) motionEvent.getX()) / tileSize;
        int y = ((int) motionEvent.getY()) / tileSize;

        if(x < 0 || x >= 8 || y < 0 || y >= 8) return false;

        user.clicked(new Position(x, y));

        return true;
    }
}
