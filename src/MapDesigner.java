import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;


public class MapDesigner {
	public static final Charset ENCODING = Charset.forName("UTF-8");

	public static JFrame frame;
	public static MapPanel map;
	public static StatusBar status;
	public static JPanel fieldsPanel;
	public static JPanel toolsPanel;
	public static JButton selectButton, changeButton, cellButton, objectButton, defButton, remObjects;
	public static ArrayList<PropertyField> cellFields = new ArrayList<>();
	public static ArrayList<PropertyField> objectFields = new ArrayList<>();

	public static int w, h;
	public static Cell[][] cells;
	public static int clickMode = 0;
	public static boolean cellMode;
	public static boolean removeMode;
	public static String cellColor[];
	public static String objectColor[];
	public static ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

	public static void main(String[] args) {
		JFrame frame = new JFrame("Map Designer");
		frame.setLayout(new BorderLayout());
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		toolsPanel = new JPanel();
		toolsPanel.setLayout(new BorderLayout());
		toolsPanel.setPreferredSize(new Dimension(225, 0));
		toolsPanel.setBackground(new Color(225, 225, 225));
		frame.add(toolsPanel, BorderLayout.EAST);
		frame.add(map = new MapPanel(), BorderLayout.CENTER);
		frame.add(status = new StatusBar(), BorderLayout.SOUTH);

		JPanel toolsButtons = new JPanel();
		toolsButtons.setLayout(new GridLayout(3, 2));
		toolsPanel.add(toolsButtons, BorderLayout.SOUTH);

		fieldsPanel = new JPanel();
		fieldsPanel.setLayout(new GridLayout(0, 1));
		toolsPanel.add(fieldsPanel, BorderLayout.NORTH);

		selectButton = new JButton("Pick");
		selectButton.addActionListener(e -> {
			clickMode = clickMode == 1 ? 0 : 1;
			updateMode();
			status.repaintme();
		});
		toolsButtons.add(selectButton);

		changeButton = new JButton("Clone");
		changeButton.addActionListener(e -> {
			clickMode = clickMode == 2 ? 0 : 2;
			updateMode();
			status.repaintme();
		});
		toolsButtons.add(changeButton);

		cellButton = new JButton("Edit Cells");
		cellButton.addActionListener(e -> {
			cellMode = true;
			updateMode();
		});
		toolsButtons.add(cellButton);

		objectButton = new JButton("Edit Objects");
		objectButton.setBackground(Color.lightGray);
		objectButton.addActionListener(e -> {
			cellMode = false;
			updateMode();
		});
		toolsButtons.add(objectButton);

		defButton = new JButton("Load Defaults");
		defButton.setBackground(Color.lightGray);
		defButton.addActionListener(e -> loadDefaults());
		toolsButtons.add(defButton);

		remObjects = new JButton("Remove");
		remObjects.addActionListener(e -> {
			removeMode = !removeMode;
			updateMode();
		});
		toolsButtons.add(remObjects);

		updateMode();

		JMenuBar menuBar = new JMenuBar();
		menuBar.setBackground(new Color(235, 235, 235));
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		JMenu optionsMenu = new JMenu("Options");
		menuBar.add(optionsMenu);

		JMenuItem newItem = new JMenuItem("New Map");
		newItem.addActionListener(e -> MapDesigner.newMap());
		fileMenu.add(newItem);
		JMenuItem loadItem = new JMenuItem("Load Map");
		loadItem.addActionListener(e -> MapDesigner.loadMap());
		fileMenu.add(loadItem);
		JMenuItem saveItem = new JMenuItem("Save Map");
		saveItem.addActionListener(e -> MapDesigner.saveMap());
		fileMenu.add(saveItem);

		JMenuItem addProperty = new JMenuItem("Add Property");
		addProperty.addActionListener(e -> addProperty());
		optionsMenu.add(addProperty);
		JMenuItem cellColoring = new JMenuItem("Cell Coloring");
		cellColoring.addActionListener(e -> defineCellColoring());
		optionsMenu.add(cellColoring);
		JMenuItem objectColoring = new JMenuItem("Object Coloring");
		objectColoring.addActionListener(e -> defineObjectColoring());
		optionsMenu.add(objectColoring);

		menuBar.setVisible(true);
		frame.setJMenuBar(menuBar);

		frame.setVisible(true);

		newMap(10, 10);
	}

	private static void loadDefaults() {
		if (cellMode)
			for (int i = 0; i < cellFields.size(); i++)
				cellFields.get(i).field.setText(Cell.sDefault.get(i).toString());
		else
			for (int i = 0; i < objectFields.size(); i++)
				objectFields.get(i).field.setText(GameObject.sDefault.get(i).toString());
	}

	public static void newMap() {
		boolean retry = true;
		while (retry)
			try {
				String dim = JOptionPane.showInputDialog(frame, "Enter dimensions, e.g. 10, 10:");
				if (dim == null)
					return;
				String dims[] = dim.split("\\s*,\\s*");
				newMap(Integer.parseInt(dims[0]), Integer.parseInt(dims[1]));
				retry = false;
			} catch (Exception ignored) {}
	}

	public static void newMap(int width, int height) {
		w = width;
		h = height;
		cells = new Cell[h][w];
		Cell.sKeys.clear();
		cellFields.clear();
		objectFields.clear();
		cellColor = new String[] {"1", "1", "1"};
		objectColor = new String[] {"1", "0.8", "0.8"};
		for (int i = 0; i < h; i++)
			for (int j = 0; j < w; j++)
				cells[i][j] = new Cell();
		map.reset();
		cellMode = true;
		removeMode = false;
		clickMode = 0;
		updateMode();
		repaintFields();
		updateCellColors();
		updateObjectColors();
	}

	public static void saveMap() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setApproveButtonText("Save");
		fileChooser.setDialogTitle("Save Map");
		fileChooser.setFileFilter(new FileNameExtensionFilter("JavaChallenge Map File", "jcm"));
		int result = fileChooser.showOpenDialog(frame);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selected = fileChooser.getSelectedFile();
			try {
				Files.write(selected.toPath(), toJson().getBytes(ENCODING), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(frame, e);
			}
		}
	}

	public static void loadMap() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setApproveButtonText("Load");
		fileChooser.setDialogTitle("Load Map");
		fileChooser.setFileFilter(new FileNameExtensionFilter("JavaChallenge Map File", "jcm"));
		int result = fileChooser.showOpenDialog(frame);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selected = fileChooser.getSelectedFile();
			try {
				byte[] bytes = Files.readAllBytes(selected.toPath());
				Gson gson = new Gson();
				JsonObject root = gson.fromJson(new String(bytes, ENCODING), JsonObject.class);
				w = root.get("width").getAsJsonPrimitive().getAsInt();
				h = root.get("height").getAsJsonPrimitive().getAsInt();
				cells = new Cell[h][w];
				cellMode = true;
				removeMode = false;
				clickMode = 0;
				updateMode();
				Cell.sKeys.clear();
				Cell.sDefault.clear();
				GameObject.sKeys.clear();
				GameObject.sDefault.clear();
				cellFields.clear();
				objectFields.clear();
				root.get("cellKeys").getAsJsonArray().forEach(je -> Cell.sKeys.add(je.getAsString()));
				root.get("cellDefaults").getAsJsonArray().forEach(je -> Cell.sDefault.add(je.getAsNumber()));
				root.get("objectKeys").getAsJsonArray().forEach(je -> GameObject.sKeys.add(je.getAsString()));
				root.get("objectDefaults").getAsJsonArray().forEach(je -> GameObject.sDefault.add(je.getAsNumber()));
				cellColor = root.get("cellColoring").getAsString().split(",");
				objectColor = root.get("objectColoring").getAsString().split(",");
				JsonArray rows = root.get("cells").getAsJsonArray();
				for (int i = 0; i < h; i++) {
					JsonArray row = rows.get(i).getAsJsonArray();
					for (int j = 0; j < w; j++)
						cells[i][j] = new Cell(row.get(j).getAsJsonArray());
				}
				JsonArray objs = root.getAsJsonArray("objects").getAsJsonArray();
				for (int i = 0; i < objs.size(); i++) {
					GameObject obj = new GameObject(objs.get(i).getAsJsonArray());
					cells[obj.i][obj.j].object = obj;
				}
				for (int i = 0; i < Cell.sKeys.size(); i++)
					cellFields.add(new PropertyField(Cell.sKeys.get(i), Cell.sDefault.get(i)));
				for (int i = 0; i < GameObject.sKeys.size(); i++)
					objectFields.add(new PropertyField(GameObject.sKeys.get(i), GameObject.sDefault.get(i)));
				map.reset();
				repaintFields();
				updateCellColors();
				updateObjectColors();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(frame, e);
			}
		}
	}

	public static String toJson() {
		return new StringBuilder()
				.append("{\n")
				.append("\t\"width\": ").append(w).append(",\n")
				.append("\t\"height\": ").append(h).append(",\n")
				.append("\t\"cellKeys\": ").append(Cell.properties()).append(",\n")
				.append("\t\"cellDefaults\": ").append(Cell.sDefault).append(",\n")
				.append("\t\"cellColoring\": ").append('"').append(cellColor[0]).append(',').append(cellColor[1]).append(',').append(cellColor[2]).append('"').append(",\n")
				.append("\t\"cells\": ").append(Arrays.deepToString(cells)).append(",\n")
				.append("\t\"objectKeys\": ").append(GameObject.properties()).append(",\n")
				.append("\t\"objectDefaults\": ").append(GameObject.sDefault).append(",\n")
				.append("\t\"objectColoring\": ").append('"').append(objectColor[0]).append(',').append(objectColor[1]).append(',').append(objectColor[2]).append('"').append(",\n")
				.append("\t\"objects\": ").append(objectsToString()).append("\n")
				.append("}")
				.toString();
	}

	public static String objectsToString() {
		ArrayList<GameObject> tmp = new ArrayList<>();
		foreachGameObject(tmp::add);
		return tmp.toString();
	}

	public static void addProperty() {
		String name = JOptionPane.showInputDialog(frame, "Property name");
		if (name == null)
			return;
		Number value = null;
		while (value == null) {
			String number = JOptionPane.showInputDialog(frame, "Default value");
			if (number == null)
				return;
			value = parseNumber(number);
		}
		final Number finalValue = value;
		if (cellMode) {
			Cell.sKeys.add(name);
			Cell.sDefault.add(value);
			foreachCell(c -> c.mValues.add(finalValue));
			cellFields.add(new PropertyField(name, finalValue));
		} else {
			GameObject.sKeys.add(name);
			GameObject.sDefault.add(value);
			foreachGameObject(o -> o.mValues.add(finalValue));
			objectFields.add(new PropertyField(name, finalValue));
		}
		repaintFields();
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

	public static void foreachCell(Consumer<Cell> action) {
		for (int i = 0; i < h; i++)
			for (int j = 0; j < w; j++)
				action.accept(cells[i][j]);
	}

	public static void foreachGameObject(Consumer<GameObject> action) {
		foreachCell(c -> {
			if (c.object != null)
				action.accept(c.object);
		});
	}

	public static void repaintFields() {
		fieldsPanel.removeAll();
		if (cellMode)
			cellFields.forEach(fieldsPanel::add);
		else
			objectFields.forEach(fieldsPanel::add);
		fieldsPanel.revalidate();
		fieldsPanel.repaint();
	}

	public static void onClick(Point p) {
		if (p.x < 0 || p.x >= w || p.y < 0 || p.y >= h)
			return;
		if (removeMode) {
			cells[p.y][p.x].object = null;
			map.repaint();
			return;
		}
		if (cellMode) {
			if (clickMode == 1) { // pick
				Cell src = cells[p.y][p.x];
				for (int i = 0; i < cellFields.size(); i++)
					cellFields.get(i).field.setText(src.mValues.get(i).toString());
				fieldsPanel.revalidate();
			} else if (clickMode == 2) { // clone
				Cell dst = cells[p.y][p.x];
				for (int i = 0; i < cellFields.size(); i++) {
					Number num = parseNumber(cellFields.get(i).field.getText());
					if (num != null)
						dst.mValues.set(i, num);
				}
				updateCellColor(p.y, p.x);
				map.repaint();
			}
		} else {
			if (clickMode == 1) { // pick
				GameObject src = cells[p.y][p.x].object;
				if (src == null)
					return;
				for (int i = 0; i < objectFields.size(); i++)
					objectFields.get(i).field.setText(src.mValues.get(i).toString());
				fieldsPanel.revalidate();
			} else if (clickMode == 2) { // clone
				GameObject dst = new GameObject();
				Cell cell = cells[p.y][p.x];
				dst.i = p.y;
				dst.j = p.x;
				cell.object = dst;
				for (int i = 0; i < objectFields.size(); i++) {
					Number num = parseNumber(objectFields.get(i).field.getText());
					if (num != null)
						dst.mValues.set(i, num);
				}
				updateObjectColor(dst);
				map.repaint();
			}
		}
	}

	public static void defineCellColoring() {
		String expr = JOptionPane.showInputDialog(frame, "Enter coloring formula.\nValues of properties are given by p1, p2, etc.\nColoring formula must be in the from R,G,B where each one is between 0 and 1.", cellColor[0] + "," + cellColor[1] + "," + cellColor[2]);
		if (expr == null)
			return;
		String rgb[] = expr.split(",");
		if (rgb.length != 3)
			return;
		cellColor = rgb;
		updateCellColors();
	}

	public static void defineObjectColoring() {
		String expr = JOptionPane.showInputDialog(frame, "Enter coloring formula.\nValues of properties are given by p1, p2, etc.\nColoring formula must be in the from R,G,B where each one is between 0 and 1.", objectColor[0] + "," + objectColor[1] + "," + objectColor[2]);
		if (expr == null)
			return;
		String rgb[] = expr.split(",");
		if (rgb.length != 3)
			return;
		objectColor = rgb;
		updateObjectColors();
	}

	public static void updateCellColors() {
		boolean s = true;
		for (int i = 0; i < h; i++)
			for (int j = 0; j < w; j++)
				s &= updateCellColor(i, j);
		if (!s)
			JOptionPane.showMessageDialog(frame, "Error in color evaluation.");
		map.repaint();
	}

	public static void updateObjectColors() {
		boolean s = true;
		for (int i = 0; i < h; i++)
			for (int j = 0; j < w; j++)
				if (cells[i][j].object != null)
					s &= updateObjectColor(cells[i][j].object);
		if (!s)
			JOptionPane.showMessageDialog(frame, "Error in color evaluation.");
		map.repaint();
	}

	public static boolean updateCellColor(int i, int j) {
		int size = cells[i][j].mValues.size();
		for (int k = 0; k < size; k++)
			engine.put("p"+(k+1), cells[i][j].mValues.get(k));
		try {
			Number r = (Number) engine.eval(cellColor[0]);
			Number g = (Number) engine.eval(cellColor[1]);
			Number b = (Number) engine.eval(cellColor[2]);
			cells[i][j].color[0] = (int) Math.min(Math.max(256 * r.doubleValue(), 0), 255);
			cells[i][j].color[1] = (int) Math.min(Math.max(256 * g.doubleValue(), 0), 255);
			cells[i][j].color[2] = (int) Math.min(Math.max(256 * b.doubleValue(), 0), 255);
		} catch (ScriptException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean updateObjectColor(GameObject object) {
		int size = object.mValues.size();
		for (int k = 0; k < size; k++)
			engine.put("p"+(k+1), object.mValues.get(k));
		try {
			Number r = (Number) engine.eval(objectColor[0]);
			Number g = (Number) engine.eval(objectColor[1]);
			Number b = (Number) engine.eval(objectColor[2]);
			object.color[0] = (int) Math.min(Math.max(256 * r.doubleValue(), 0), 255);
			object.color[1] = (int) Math.min(Math.max(256 * g.doubleValue(), 0), 255);
			object.color[2] = (int) Math.min(Math.max(256 * b.doubleValue(), 0), 255);
		} catch (ScriptException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static void updateMode() {
		if (removeMode) {
			remObjects.setBackground(Color.orange);
			selectButton.setBackground(Color.lightGray);
			changeButton.setBackground(Color.lightGray);
			cellButton.setBackground(Color.lightGray);
			objectButton.setBackground(Color.yellow);
			return;
		}
		remObjects.setBackground(Color.lightGray);
		switch (clickMode) {
			case 1:
				selectButton.setBackground(Color.yellow);
				changeButton.setBackground(Color.lightGray);
				break;
			case 2:
				selectButton.setBackground(Color.lightGray);
				changeButton.setBackground(Color.yellow);
				break;
			default:
				selectButton.setBackground(Color.lightGray);
				changeButton.setBackground(Color.lightGray);
		}
		if (cellMode) {
			cellButton.setBackground(Color.orange);
			objectButton.setBackground(Color.lightGray);
		} else {
			cellButton.setBackground(Color.lightGray);
			objectButton.setBackground(Color.orange);
		}
		repaintFields();
	}

}