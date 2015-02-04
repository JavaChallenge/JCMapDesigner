package model.object;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by Hadi on 2/2/2015 2:06 PM.
 */
public class GameObjectType {

    @Expose@SerializedName("name")
    private String mName;
    @Expose@SerializedName("keys")
    private ArrayList<String> mKeys = new ArrayList<>();
    @Expose@SerializedName("defaults")
    private ArrayList<Number> mDefaults = new ArrayList<>();
    @Expose@SerializedName("coloring")
    public String mColoring[] = new String[] {"1", "0.8", "0.8"};
    @Expose@SerializedName("instances")
    private ArrayList<GameObject> mObjects = new ArrayList<>();

    public GameObjectType(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public void addKey(String key, Number def) {
        mKeys.add(key);
        mDefaults.add(def);
    }

    public void removeKey(String key) {
        int index = mKeys.indexOf(key);
        mKeys.remove(index);
        mDefaults.remove(index);
        mObjects.forEach(o -> o.removeValue(index));
    }

    public void foreachKey(BiConsumer<String, Number> action) {
        for (int i = 0; i < mKeys.size(); i++)
            action.accept(mKeys.get(i), mDefaults.get(i));
    }

    public String[] getKeys() {
        return mKeys.toArray(new String[mKeys.size()]);
    }

    public void foreachObject(Consumer<GameObject> action) {
        mObjects.forEach(action);
    }

    public Number[] getDefaults() {
        return mDefaults.toArray(new Number[mDefaults.size()]);
    }

    public GameObject newObject(int x, int y) {
        GameObject o = new GameObject(this, x, y);
        mObjects.add(o);
        return o;
    }

    public void removeObject(GameObject o) {
        mObjects.remove(o);
    }

    @Override
    public String toString() {
        return mName;
    }

    @Override
    public int hashCode() {
        return mName.hashCode();
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
        foreachObject(o -> {
            o.completeLoading();
            o.setType(this);
        });
    }

}
