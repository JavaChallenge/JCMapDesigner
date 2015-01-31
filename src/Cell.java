import com.google.gson.JsonArray;

import java.awt.*;
import java.util.*;

public class Cell {
    public static ArrayList<String> sKeys = new ArrayList<>();
    public static ArrayList<Number> sDefault = new ArrayList<>();

    public static String properties() {
        ArrayList<String> tmp = new ArrayList<>();
        sKeys.forEach(s -> tmp.add('"' + s + '"'));
        return tmp.toString();
    }


    public ArrayList<Number> mValues = new ArrayList<>();
    int color[] = new int[] {255, 255, 255};

    public Cell() {
    }

    public Cell(JsonArray values) {
        for (int i = 0; i < sKeys.size(); i++)
            mValues.add(values.get(i).getAsNumber());
    }

    @Override
    public String toString() {
        return mValues.toString();
    }

    public void paint(Graphics g, int size) {
        g.setColor(new Color(color[0], color[1], color[2]));
        g.fillRect(0, 0, size, size);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, size, size);
    }
}
