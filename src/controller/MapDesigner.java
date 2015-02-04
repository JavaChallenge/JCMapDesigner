package controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import model.map.Cell;
import model.map.Map;
import model.object.GameObject;
import model.object.GameObjectType;
import parser.MapParser;
import view.MainView;
import view.PropertiesView;
import view.SingleStructure;
import view.StructuresView;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
        mMainView.getMenu().getSaveMap().addActionListener(e -> onSaveMapClicked(false));
        mMainView.getMenu().getSaveCompMap().addActionListener(e -> onSaveMapClicked(true));
        mMainView.getMenu().getDefineColoring().addActionListener(e -> defineColoring());
        mMainView.getMenu().getGenClasses().addActionListener(e -> onGenClassesClicked());

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

        JsonObject root = Json.GSON.fromJson(json, JsonElement.class).getAsJsonObject();

        map = Json.GSON.fromJson(root.getAsJsonObject("map"), Map.class);

        JsonArray types = root.getAsJsonArray("objects");
        mTypes.clear();
        types.forEach(jt -> {
            GameObjectType type = Json.GSON.fromJson(jt, GameObjectType.class);
            addObjectType(type.getName(), type);
        });

        // complete loading
        mTypes.forEach((n, t) -> {
            t.completeLoading();
            map.completeLoading();
            t.foreachObject(o -> map.at(o.getX(), o.getY()).addObject(o));
        });

        updateAll();
    }

    private void onSaveMapClicked(boolean compressed) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setApproveButtonText("Save");
        fileChooser.setDialogTitle("Save Map");
        int result = fileChooser.showOpenDialog(mMainView);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();
            try {
                JsonElement root = saveMap();
                String json = compressed ? Json.CGSON.toJson(root) : Json.GSON.toJson(root);
                //System.out.println(MapParser.parse(json));
                Files.write(selected.toPath(), json.getBytes(ENCODING), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(mMainView, e);
            }
        }
    }

    public JsonElement saveMap() {
        JsonObject root = new JsonObject();

        root.add("map", Json.GSON.toJsonTree(map));

        JsonArray types = new JsonArray();
        mTypes.forEach((n, t) -> types.add(Json.GSON.toJsonTree(t)));
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

    private void onGenClassesClicked() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setApproveButtonText("Save");
        fileChooser.setDialogTitle("Generate Java Classes");
        int result = fileChooser.showOpenDialog(mMainView);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();
            String pckg = JOptionPane.showInputDialog(mMainView, "Enter package of classes");
            if (pckg == null)
                return;
            try {
                generateClasses(selected, pckg);
            } catch (IOException | URISyntaxException e) {
                JOptionPane.showMessageDialog(mMainView, e);
            }
        }
    }

    public void generateClasses(File zipFile, String pckg) throws IOException, URISyntaxException {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
        // save map
        JsonElement root = saveMap();
        out.putNextEntry(new ZipEntry("map"));
        out.write(Json.GSON.toJson(root).getBytes(ENCODING));
        out.putNextEntry(new ZipEntry("map_compressed"));
        out.write(Json.CGSON.toJson(root).getBytes(ENCODING));
        // save map class
        out.putNextEntry(new ZipEntry("Map.java"));
        out.write(("package " + pckg + ";\n\n" + generateMapJava()).getBytes(ENCODING));
        // save map parser
        out.putNextEntry(new ZipEntry("MapParser.java"));
        out.write(("package " + pckg + ";\n\n" + generateMapParserJava()).getBytes(ENCODING));
        // save blocks
        out.putNextEntry(new ZipEntry("Cell.java"));
        out.write(("package " + pckg + ";\n\n" + generateCellJava()).getBytes(ENCODING));
        // save objects
        out.putNextEntry(new ZipEntry("GameObject.java"));
        out.write(("package " + pckg + ";\n\n" + generateGameObjectJava()).getBytes(ENCODING));

        Set<java.util.Map.Entry<String, String>> entrySet = generateObjectsJava().entrySet();
        for (java.util.Map.Entry<String, String> e : entrySet) {
            out.putNextEntry(new ZipEntry(e.getKey()));
            out.write(("package " + pckg + ";\n\n" + e.getValue()).getBytes(ENCODING));
        }
        out.close();
    }

    private String generateMapJava() throws URISyntaxException, IOException {
//        ClassGenerator gen = new ClassGenerator("Map", null);
//        gen.addField("cells", "Cell[][]");
//        gen.addMethod("at", new String[] {"int x", "int y"}, "Block", "return blocks[y][x];");
//        return gen.toString();
        byte[] bytes = Files.readAllBytes(Paths.get(MapParser.class.getResource("resources/Map").toURI()));
        return new String(bytes, ENCODING);
    }

    private String generateMapParserJava() throws URISyntaxException, IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(MapParser.class.getResource("resources/MapParser").toURI()));
        return new String(bytes, ENCODING);
    }

    private String generateGameObjectJava() throws URISyntaxException, IOException {
//        ClassGenerator gen = new ClassGenerator("Game Object", null);
//        gen.addField("x", "int");
//        gen.addField("y", "int");
//        return gen.toString();
        byte[] bytes = Files.readAllBytes(Paths.get(MapParser.class.getResource("resources/GameObject").toURI()));
        return new String(bytes, ENCODING);
    }

    private String generateCellJava() throws URISyntaxException, IOException {
//        ClassGenerator gen = new ClassGenerator("Cell", null);
//
//        gen.addField("x", "int");
//        gen.addField("y", "int");
//        return gen.toString();
        byte[] bytes = Files.readAllBytes(Paths.get(MapParser.class.getResource("resources/Cell").toURI()));
        String str = new String(bytes, ENCODING);
        StringBuilder sb = new StringBuilder();
        String keys[] = map.getKeys();
        for (String key : keys)
            sb.append("\tprivate Number m").append(toCamelCase(key)).append(";\n");
        return str.replace("/* fields */", sb.toString());
    }

    private HashMap<String, String> generateObjectsJava() {
        HashMap<String, String> javaFiles = new HashMap<>();
        mTypes.forEach((name, type) -> javaFiles.put(toCamelCase(name)+".java", getObjectJava(type)));
        return javaFiles;
    }

    private String getObjectJava(GameObjectType type) {
        ClassGenerator gen = new ClassGenerator(type.getName(), "game object");
        gen.setHaveConstructor(false);
        String[] keys = type.getKeys();
        for (String key : keys)
            gen.addField(key, "Number");
        return gen.toString();
    }

    private String toCamelCase(String name) {
        StringBuilder sb = new StringBuilder();
        String[] parts = name.replaceAll("[^ a-zA-Z0-9]", "").split("\\s+");
        for (String part : parts)
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1).toLowerCase());
        return sb.toString();
    }

}
