package parser;

import java.util.ArrayList;

public class Cell {

    private int mX, mY;
    private ArrayList<GameObject> objects = new ArrayList<>();

    public Cell(int x, int y) {
        mX = x;
        mY = y;
    }

    public int getX() {
        return mX;
    }

    public int getY() {
        return mY;
    }

    public void addObject(GameObject object) {
        objects.add(object);
    }

}
