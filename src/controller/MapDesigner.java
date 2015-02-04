package controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import model.map.Cell;
import model.map.Map;
import model.object.GameObject;
import model.object.GameObjectType;
import view.MainView;
import view.PropertiesView;
import view.SingleStructure;
import view.StructuresView;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;

/**
 * Created by Hadi on 2/3/2015 10:45 AM.
 */
public class MapDesigner {

    public static final Charset ENCODING = Charset.forName("UTF-8");

    private Map map;
    private HashMap<String, GameObjectType> mTypes;

    private GameObjectType mCurrentType;
    private ClickMode mClickMode;
    private MainView mMainView;
    private boolean mRightClickPressed;
    private ScriptEngine mEngine = new ScriptEngineManager().getEngineByName("JavaScript");

    public MapDesigner() {
        initUI();
        reset();
        mMainView.setVisible(true);
    }

    private void initUI() {
        mMainView = new MainView();
        mMainView.getMapPanel().setMapProvider(this::getMap);

        // init menu bar
        mMainView.getMenu().getNewMap().addActionListener(e -> onNewMapClicked());
        mMainView.getMenu().getLoadMap().addActionListener(e -> onLoadMapClicked());
        mMainView.getMenu().getSaveMap().addActionListener(e -> onSaveMapClicked());
        mMainView.getMenu().getDefineColoring().addActionListener(e -> defineColoring());

        mMainView.getPropertyView().getPick().addActionListener(e -> onPickClicked());
        mMainView.getPropertyView().getClone().addActionListener(e -> onCloneClicked());
        mMainView.getPropertyView().getDefaults().addActionListener(e -> onDefaultsClicked());
        mMainView.getPropertyView().getRemove().addActionListener(e -> onRemoveClicked());
        mMainView.getPropertyView().getNewProp().addActionListener(e -> addProperty());
        mMainView.getPropertyView().getRemoveProp().addActionListener(e -> removeProperty());

        mMainView.getStructuresView().getAddObjectType().addActionListener(e -> onAddObjectTypeClicked());
        mMainView.getStructuresView().getRemoveObjectType().addActionListener(e -> removeObjectType());

        mMainView.getMapPanel().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == 1)
                    onMapClicked(mMainView.getMapPanel().getMapPoint(e.getPoint()));
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == 3)
                    mRightClickPressed = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == 3)
                    mRightClickPressed = false;
            }
        });
        mMainView.getMapPanel().addMouseMotionListener(new MouseAdapter() {
            private long lastTime = 0;
            private Point lastPoint;

            @Override
            public void mouseDragged(MouseEvent e) {
                if (mRightClickPressed) {
                    long time = System.currentTimeMillis();
                    Point point = e.getPoint();
                    if (time < lastTime + 100)
                        mMainView.getMapPanel().translate(point.x - lastPoint.x, point.y - lastPoint.y);
                    lastTime = time;
                    lastPoint = point;
                    mMainView.getMapPanel().repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mMainView.getStatusBar().setPoint(mMainView.getMapPanel().getMapPoint(e.getPoint()));
            }
        });
        mMainView.getMapPanel().addMouseWheelListener(e -> mMainView.getMapPanel().zoom(e.getPoint(), e.getPreciseWheelRotation()));
    }

    public void reset() {
        map = new Map(10, 10);
        mTypes = new HashMap<>();
        mCurrentType = null;
        mClickMode = ClickMode.NONE;
        mMainView.getMapPanel().resetView();
        mMainView.getStructuresView().removeAllStructures();
        updateAll();
    }

    public void onPickClicked() {
        mClickMode = mClickMode == ClickMode.PICK ? ClickMode.NONE : ClickMode.PICK;
        revalidate();
    }

    public void onCloneClicked() {
        mClickMode = mClickMode == ClickMode.CLONE ? ClickMode.NONE : ClickMode.CLONE;
        revalidate();
    }

    public void onDefaultsClicked() {
        loadDefaults();
    }

    public void onRemoveClicked() {
        mClickMode = mClickMode == ClickMode.REMOVE ? ClickMode.NONE : ClickMode.REMOVE;
        revalidate();
    }

    private void onMapClicked(Point p) {
        if (p.x < 0 || p.x >= map.getWidth() || p.y < 0 || p.y >= map.getHeight())
            return;
        switch (mClickMode) {
            case PICK: {
                PropertiesView pv = mMainView.getPropertyView();
                Number[] fields = pv.getFields();
                if (mCurrentType == null) {
                    Cell src = map.at(p.x, p.y);
                    for (int i = 0; i < fields.length; i++)
                        pv.setField(i, src.getValue(i));
                } else {
                    GameObject src = map.at(p.x, p.y).getObjects()[0]; // todo multiple objects
                    if (src == null)
                        return;
                    for (int i = 0; i < fields.length; i++)
                        pv.setField(i, src.getValue(i));
                }
                revalidate();
            } break;
            case CLONE: {
                PropertiesView pv = mMainView.getPropertyView();
                Number[] fields = pv.getFields();
                if (mCurrentType == null) {
                    Cell dst = map.at(p.x, p.y);
                    for (int i = 0; i < fields.length; i++)
                        if (fields[i] != null)
                            dst.setValue(i, fields[i]);
                    updateCellColor(p.y, p.x);
                } else {
                    GameObject dst = mCurrentType.newObject(p.x, p.y);
                    Cell pos = map.at(p.x, p.y);
                    pos.addObject(dst);
                    for (int i = 0; i < fields.length; i++)
                        if (fields[i] != null)
                            dst.setValue(i, fields[i]);
                    updateObjectColor(dst);
                }
                revalidate();
            } break;
            case REMOVE: {
                if (mCurrentType == null)
                    break;
                map.at(p.x, p.y).removeObject(0); // todo multiple objects
                revalidate();
            } break;
        }
    }

    public void loadDefaults() {
        if (mCurrentType == null)
            mMainView.getPropertyView().setFields(map.getKeys(), map.getDefaults());
        else
            mMainView.getPropertyView().setFields(mCurrentType.getKeys(), mCurrentType.getDefaults());
    }

    private void onNewMapClicked() {
        boolean retry = true;
        while (retry)
            try {
                String dim = JOptionPane.showInputDialog(mMainView, "Enter dimensions, e.g. 10, 12 (width = 10 & height = 12)");
                if (dim == null)
                    return;
                String dims[] = dim.split("\\s*,\\s*");
                newMap(Integer.parseInt(dims[0]), Integer.parseInt(dims[1]));
                retry = false;
            } catch (Exception ignored) {}
    }

    public void newMap(int w, int h) {
        reset();
        map = new Map(w, h);
    }

    private void onLoadMapClicked() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setApproveButtonText("Load");
        fileChooser.setDialogTitle("Load Map");
        fileChooser.setFileFilter(new FileNameExtensionFilter("JavaChallenge Map File", "jcm"));
        int result = fileChooser.showOpenDialog(mMainView);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();
            try {
                byte[] bytes = Files.readAllBytes(selected.toPath());
                String content = new String(bytes, ENCODING);
                loadMap(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadMap(String json) {
        reset();

        JsonObject root = Json.gson().fromJson(json, JsonElement.class).getAsJsonObject();

        map = Json.gson().fromJson(root.getAsJsonObject("map"), Map.class);

        JsonArray types = root.getAsJsonArray("objects");
        mTypes.clear();
        types.forEach(jt -> {
            GameObjectType type = Json.gson().fromJson(jt, GameObjectType.class);
            addObjectType(type.getName(), type);
        });

        // complete loading
        mTypes.forEach((n, t) -> {
            t.completeLoading();
            map.foreachCell(Cell::completeLoading);
            t.foreachObject(o -> map.at(o.getX(), o.getY()).addObject(o));
        });

        updateAll();
    }

    private void onSaveMapClicked() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setApproveButtonText("Save");
        fileChooser.setDialogTitle("Save Map");
        fileChooser.setFileFilter(new FileNameExtensionFilter("JavaChallenge Map File", "jcm"));
        int result = fileChooser.showOpenDialog(mMainView);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();
            try {
                Files.write(selected.toPath(), Json.gson().toJson(saveMap()).getBytes(ENCODING), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(mMainView, e);
            }
        }
    }

    public JsonElement saveMap() {
        JsonObject root = new JsonObject();

        root.add("map", Json.gson().toJsonTree(map));

        JsonArray types = new JsonArray();
        mTypes.forEach((n, t) -> types.add(Json.gson().toJsonTree(t)));
        root.add("objects", types);

        return root;
    }

    public void onAddObjectTypeClicked() {
        String name = JOptionPane.showInputDialog(mMainView, "Type name");
        if (name == null)
            return;
        addObjectType(name, new GameObjectType(name));
        revalidate();
    }

    public void addObjectType(String name, GameObjectType type) {
        mTypes.put(name, type);
        SingleStructure structure = mMainView.getStructuresView().addStructure(name);
        structure.getButton().addActionListener(e -> changeType(type));
    }

    public void removeObjectType() {
        String name = JOptionPane.showInputDialog(mMainView, "Type name");
        if (name == null)
            return;
        GameObjectType type = mTypes.remove(name);
        if (type == null)
            return;
        mMainView.getStructuresView().removeStructure(name);
        revalidate();
    }

    private void changeType(GameObjectType type) {
        if (mCurrentType == type)
            mCurrentType = null;
        else
            mCurrentType = type;
        loadDefaults();
        revalidate();
    }

    public void addProperty() {
        String name = JOptionPane.showInputDialog(mMainView, "Property name");
        if (name == null)
            return;
        Number value = promptNumber("Default value");
        if (value == null)
            return;
        if (mCurrentType == null)
            map.addKey(name, value);
        else
            mCurrentType.addKey(name, value);
        loadDefaults();
        revalidate();
    }

    public void removeProperty() {
        String name = JOptionPane.showInputDialog(mMainView, "Property name");
        if (name == null)
            return;
        if (mCurrentType == null)
            map.removeKey(name);
        else
            mCurrentType.removeKey(name);
        loadDefaults();
        revalidate();
    }

    public void defineColoring() {
        String msg = "Enter coloring formula.\nValues of properties are given by p1, p2, etc.\nColoring formula must be in the from R,G,B where each one is between 0 and 1.";
        if (mCurrentType == null) {
            String expr = JOptionPane.showInputDialog(mMainView, msg, map.getColoringString());
            if (expr == null)
                return;
            String rgb[] = expr.split(",");
            if (rgb.length != 3)
                return;
            map.setColoring(rgb[0], rgb[1], rgb[2]);
            updateCellColors();
        } else {
            String expr = JOptionPane.showInputDialog(mMainView, msg, mCurrentType.getColoringString());
            if (expr == null)
                return;
            String rgb[] = expr.split(",");
            if (rgb.length != 3)
                return;
            mCurrentType.setColoring(rgb[0], rgb[1], rgb[2]);
            updateObjectColors();
        }
    }

    public Map getMap() {
        return map;
    }

    private Number promptNumber(String message) {
        Number value = null;
        while (value == null) {
            String number = JOptionPane.showInputDialog(mMainView, message);
            if (number == null)
                return null;
            value = parseNumber(number);
        }
        return value;
    }

    public static Number parseNumber(String number) {
        Number value = null;
        try {
            value = new BigDecimal(number);
            value = new BigInteger(number);
            value = Double.parseDouble(number);
            value = Float.parseFloat(number);
            value = Long.parseLong(number);
            value = Integer.parseInt(number);
        } catch (Exception ignored) {
        }
        return value;
    }

    private void updateAll() {
        updateCellColors();
        updateObjectColors();
        loadDefaults();
        revalidate();
    }

    private void revalidate() {
        PropertiesView pv = mMainView.getPropertyView();
        pv.getClone().setBackground(Color.lightGray);
        pv.getPick().setBackground(Color.lightGray);
        pv.getRemove().setBackground(Color.lightGray);
        pv.getDefaults().setBackground(Color.lightGray);
        pv.getNewProp().setBackground(Color.lightGray);
        pv.getRemoveProp().setBackground(Color.lightGray);

        StructuresView sv = mMainView.getStructuresView();
        sv.getAddObjectType().setBackground(Color.lightGray);
        sv.getRemoveObjectType().setBackground(Color.lightGray);

        switch (mClickMode) {
            case CLONE:
                pv.getClone().setBackground(Color.yellow);
                break;
            case PICK:
                pv.getPick().setBackground(Color.yellow);
                break;
            case REMOVE:
                pv.getRemove().setBackground(Color.yellow);
                break;
        }

        SingleStructure[] structures = sv.getStructures();
        for (SingleStructure structure : structures)
            structure.getButton().setBackground(Color.lightGray);
        if (mCurrentType != null)
            sv.getStructure(mCurrentType.getName()).getButton().setBackground(Color.orange);
        mMainView.getStatusBar().setMode(mClickMode);
        mMainView.revalidate();
        mMainView.repaint();
    }

    public void updateCellColors() {
        boolean s = true;
        int w = map.getWidth(), h = map.getHeight();
        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++)
                s &= updateCellColor(i, j);
        if (!s)
            JOptionPane.showMessageDialog(mMainView, "Error in color evaluation.");
        revalidate();
    }

    public void updateObjectColors() {
        boolean s = true;
        int w = map.getWidth(), h = map.getHeight();
        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++) {
                GameObject[] objects = map.at(j, i).getObjects();
                for (GameObject object : objects)
                    s &= updateObjectColor(object);
            }
        if (!s)
            JOptionPane.showMessageDialog(mMainView, "Error in color evaluation.");
        revalidate();
    }

    public boolean updateCellColor(int i, int j) {
        Number[] values = map.at(j, i).getValues();
        int size = values.length;
        for (int k = 0; k < size; k++)
            mEngine.put("p"+(k+1), values[k]);
        try {
            String[] cellColor = map.getColoring();
            Number r = (Number) mEngine.eval(cellColor[0]);
            Number g = (Number) mEngine.eval(cellColor[1]);
            Number b = (Number) mEngine.eval(cellColor[2]);
            map.at(j, i).setColor((int) Math.min(Math.max(256 * r.doubleValue(), 0), 255),
                    (int) Math.min(Math.max(256 * g.doubleValue(), 0), 255),
                    (int) Math.min(Math.max(256 * b.doubleValue(), 0), 255));
        } catch (ScriptException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean updateObjectColor(GameObject object) {
        Number[] values = object.getValues();
        int size = values.length;
        for (int k = 0; k < size; k++)
            mEngine.put("p"+(k+1), values[k]);
        try {
            String[] objectColor = object.getType().getColoring();
            Number r = (Number) mEngine.eval(objectColor[0]);
            Number g = (Number) mEngine.eval(objectColor[1]);
            Number b = (Number) mEngine.eval(objectColor[2]);
            object.setColor((int) Math.min(Math.max(256 * r.doubleValue(), 0), 255),
                    (int) Math.min(Math.max(256 * g.doubleValue(), 0), 255),
                    (int) Math.min(Math.max(256 * b.doubleValue(), 0), 255));
        } catch (ScriptException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
