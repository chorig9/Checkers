package edu.game.checkers.logic;

public final class Position {
    public int x, y;

    public Position(int x, int y){
        this.x = x; this.y = y;
    }

    @Override
    public boolean equals(Object object)
    {
        if(this == object)
            return true;

        if(object == null)
            return false;

        if(getClass() != object.getClass())
            return false;

        Position position = (Position) object;
        return this.x == position.x && this.y == position.y;
    }

    public boolean isInRange()
    {
        return x >= 0 && x < 8 && y >= 0 && y < 8;
    }
}
