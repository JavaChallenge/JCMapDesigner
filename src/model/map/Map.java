package model.map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by Hadi on 2/2/2015 3:28 PM.
 */
public class Map {

    @Expose@SerializedName("width")
    private int mW;
    @Expose@SerializedName("height")
    private int mH;
    @Expose@SerializedName("keys")
    private ArrayList<String> mKeys = new ArrayList<>();
    @Expose@SerializedName("defaults")
    private ArrayList<Number> mDefaults = new ArrayList<>();
    @Expose@SerializedName("coloring")
    private String mColoring[] = new String[] {"1", "1", "1"};
    @Expose@SerializedName("cells")
    private Cell mCells[][];

    public Map(int w, int h) {
        mW = w;
        mH = h;
        mCells = new Cell[h][w];
        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++)
                mCells[i][j] = new Cell(j, i);
    }

    public int getWidth() {
        return mW;
    }

    public int getHeight() {
        return mH;
    }

    public Cell at(int x, int y) {
        return mCells[y][x];
    }

    public void addKey(String name, Number def) {
        mKeys.add(name);
        mDefaults.add(def);
        foreachCell(c -> c.addValue(def));
    }

    public void removeKey(String name) {
        int index = mKeys.indexOf(name);
        mKeys.remove(index);
        mDefaults.remove(index);
        foreachCell(c -> c.removeValue(index));
    }

    public String[] getKeys() {
        return mKeys.toArray(new String[mKeys.size()]);
    }

    public void foreachKey(BiConsumer<String, Number> action) {
        for (int i = 0; i < mKeys.size(); i++)
            action.accept(mKeys.get(i), mDefaults.get(i));
    }

    public Number[] getDefaults() {
        return mDefaults.toArray(new Number[mDefaults.size()]);
    }

    public void foreachCell(Consumer<Cell> action) {
        for (int i = 0; i < mH; i++)
            for (int j = 0; j < mW; j++)
                action.accept(mCells[i][j]);
    }

    public Cell[][] getCells() {
        return mCells;
    }

    public void setColoring(String r, String g, String b) {
        mColoring[0] = r;
        mColoring[1] = g;
        mColoring[2] = b;
    }

    public String[] getColoring() {
        return mColoring;
    }

    public String getColoringString() {
        return mColoring[0] + ", " + mColoring[1] + ", " + mColoring[2];
    }

    public void completeLoading() {
        for (int x = 0; x < mW; x++)
            for (int y = 0; y < mH; y++) {
                mCells[y][x].setX(x);
                mCells[y][x].setY(y);
                mCells[y][x].completeLoading();
            }
    }

}
