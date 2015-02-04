package model.map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import model.object.GameObject;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Hadi on 2/2/2015 2:57 PM.
 */
public class Cell {

    private int mX, mY;
    @Expose@SerializedName("values")
    private ArrayList<Number> mValues = new ArrayList<>();
    private ArrayList<GameObject> mObjects = new ArrayList<>();
    private int color[] = new int[3];

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

    void setX(int x) {
        mX = x;
    }

    void setY(int y) {
        mY = y;
    }

    public void addObject(GameObject object) {
        mObjects.add(object);
    }

    public void removeObject(int index) {
        mObjects.remove(index);
    }

    public GameObject[] getObjects() {
        return mObjects.toArray(new GameObject[mObjects.size()]);
    }

    public Number getValue(int index) {
        return mValues.get(index);
    }

    public Number[] getValues() {
        return mValues.toArray(new Number[mValues.size()]);
    }

    public void setValue(int index, Number value) {
        mValues.set(index, value);
    }

    public void removeValue(int index) {
        mValues.remove(index);
    }

    public void addValue(Number value) {
        mValues.add(value);
    }

    public void setColor(int r, int g, int b) {
        color[0] = r;
        color[1] = g;
        color[2] = b;
    }

    public void paint(Graphics g, int size) {
        g.setColor(new Color(color[0], color[1], color[2]));
        g.fillRect(0, 0, size, size);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, size, size);
        mObjects.forEach(o -> o.paint(g, size));
    }

    public void completeLoading() {
        mObjects = new ArrayList<>();
        color = new int[3];
    }

}
