package model.object;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Hadi on 2/2/2015 2:14 PM.
 */
public class GameObject {

    @Expose@SerializedName("x")
    private int mX;
    @Expose@SerializedName("y")
    private int mY;
    private GameObjectType mType;
    @Expose@SerializedName("values")
    private ArrayList<Number> mValues = new ArrayList<>();
    private int mColor[] = new int[3];

    public GameObject(GameObjectType type, int x, int y) {
        mType = type;
        mX = x;
        mY = y;
        Number[] defaults = mType.getDefaults();
        Collections.addAll(mValues, defaults);
    }

    public void setValue(int index, Number value) {
        mValues.set(index, value);
    }

    public GameObjectType getType() {
        return mType;
    }

    public Number getValue(int index) {
        return mValues.get(index);
    }

    public Number[] getValues() {
        return mValues.toArray(new Number[mValues.size()]);
    }

    public void removeValue(int index) {
        mValues.remove(index);
    }

    public void setColor(int r, int g, int b) {
        mColor[0] = r;
        mColor[1] = g;
        mColor[2] = b;
    }

    public void paint(Graphics g, int size) {
        g.setColor(new Color(mColor[0], mColor[1], mColor[2]));
        g.fillArc((int)(size*0.1), (int)(size*0.1), (int)(size*0.8), (int)(size*0.8), 0, 360);
        g.setColor(Color.BLACK);
        g.drawArc((int)(size*0.1), (int)(size*0.1), (int)(size * 0.8), (int)(size*0.8), 0, 360);
    }

    public void setType(GameObjectType type) {
        mType = type;
    }

    public int getX() {
        return mX;
    }

    public int getY() {
        return mY;
    }

    public void completeLoading() {
        mColor = new int[3];
    }

}
