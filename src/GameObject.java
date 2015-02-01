import com.google.gson.JsonArray;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Hadi on 2/1/2015 8:29 AM.
 */
public class GameObject {
    public static ArrayList<String> sKeys = new ArrayList<>();
    public static ArrayList<Number> sDefault = new ArrayList<>();

    public static String properties() {
        ArrayList<String> tmp = new ArrayList<>();
        sKeys.forEach(s -> tmp.add('"' + s + '"'));
        return tmp.toString();
    }

    int color[] = new int[] {255, 255, 255};
    public ArrayList<Number> mValues = new ArrayList<>();
    public int i, j;

    public GameObject() {
        mValues.addAll(sDefault);
    }

    public GameObject(JsonArray values) {
        i = values.get(0).getAsInt();
        j = values.get(1).getAsInt();
        for (int i = 0; i < sKeys.size(); i++)
            mValues.add(values.get(i+2).getAsNumber());
    }

    @Override
    public String toString() {
        ArrayList<Number> tmp = new ArrayList<>();
        tmp.add(i);
        tmp.add(j);
        tmp.addAll(mValues);
        return tmp.toString();
    }

    public void paint(Graphics g, int size) {
        g.setColor(new Color(color[0], color[1], color[2]));
        g.fillArc((int)(size*0.1), (int)(size*0.1), (int)(size*0.8), (int)(size*0.8), 0, 360);
        g.setColor(Color.BLACK);
        g.drawArc((int)(size*0.1), (int)(size*0.1), (int)(size * 0.8), (int)(size*0.8), 0, 360);
    }

}
