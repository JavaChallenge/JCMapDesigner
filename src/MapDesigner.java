import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;
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
	public static JButton selectButton, changeButton;
	public static int w, h;
	public static Cell[][] cells;
	public static int clickMode = 0;
	public static ArrayList<PropertyField> fields = new ArrayList<>();
	public static JPanel fieldsPanel;
	public static String colorExpr[];
	public static ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

	public static void main(String[] args) {
		JFrame frame = new JFrame("Map Designer");
		frame.setLayout(new BorderLayout());
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		JPanel toolsPanel = new JPanel();
		toolsPanel.setLayout(new BorderLayout());
		toolsPanel.setPreferredSize(new Dimension(200, 0));
		toolsPanel.setBackground(new Color(225, 225, 225));
		frame.add(toolsPanel, BorderLayout.EAST);
		frame.add(map = new MapPanel(), BorderLayout.CENTER);
		frame.add(status = new StatusBar(), BorderLayout.SOUTH);

		JPanel toolsButtons = new JPanel();
		toolsButtons.setLayout(new GridLayout(1, 0));
		toolsPanel.add(toolsButtons, BorderLayout.SOUTH);

		selectButton = new JButton("Pick");
		selectButton.addActionListener(e -> {
			clickMode = clickMode == 1 ? 0 : 1;
			updateBtnColors();
			status.repaintme();
		});
		toolsButtons.add(selectButton);

		changeButton = new JButton("Clone");
		changeButton.addActionListener(e -> {
			clickMode = clickMode == 2 ? 0 : 2;
			updateBtnColors();
			status.repaintme();
		});
		toolsButtons.add(changeButton);

		updateBtnColors();

		fieldsPanel = new JPanel();
		fieldsPanel.setLayout(new GridLayout(0, 1));
		toolsPanel.add(fieldsPanel, BorderLayout.NORTH);

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
		JMenuItem coloring = new JMenuItem("Define Coloring");
		coloring.addActionListener(e -> defineColoring());
		optionsMenu.add(coloring);

		menuBar.setVisible(true);
		frame.setJMenuBar(menuBar);

		frame.setVisible(true);

		newMap(10, 10);
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
		fields.clear();
		colorExpr = new String[] {"1", "1", "1"};
		for (int i = 0; i < h; i++)
			for (int j = 0; j < w; j++)
				cells[i][j] = new Cell();
		map.reset();
		repaintFields();
		updateColors();
	}

	public static void saveMap() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setApproveButtonText("Save");
		fileChooser.setDialogTitle("Save Map");
		int result = fileChooser.showOpenDialog(frame);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selected = fileChooser.getSelectedFile();
			try {
				Files.write(selected.toPath(), toJson().getBytes(ENCODING), StandardOpenOption.CREATE);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(frame, e);
			}
		}
	}

	public static void loadMap() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setApproveButtonText("Load");
		fileChooser.setDialogTitle("Load Map");
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
				Cell.sKeys.clear();
				fields.clear();
				root.get("keys").getAsJsonArray().forEach(je -> Cell.sKeys.add(je.getAsString()));
				root.get("default").getAsJsonArray().forEach(je -> Cell.sDefault.add(je.getAsNumber()));
				colorExpr = root.get("coloring").getAsString().split(",");
				JsonArray rows = root.get("cells").getAsJsonArray();
				for (int i = 0; i < h; i++) {
					JsonArray row = rows.get(i).getAsJsonArray();
					for (int j = 0; j < w; j++)
						cells[i][j] = new Cell(row.get(j).getAsJsonArray());
				}
				for (int i = 0; i < Cell.sKeys.size(); i++)
					fields.add(new PropertyField(Cell.sKeys.get(i), Cell.sDefault.get(i)));
				map.reset();
				repaintFields();
				updateColors();
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
				.append("\t\"keys\": ").append(Cell.properties()).append(",\n")
				.append("\t\"default\": ").append(Cell.sDefault).append(",\n")
				.append("\t\"coloring\": ").append('"').append(colorExpr[0]).append(',').append(colorExpr[1]).append(',').append(colorExpr[2]).append('"').append(",\n")
				.append("\t\"cells\": ").append(Arrays.deepToString(cells)).append("\n")
				.append("}")
				.toString();
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
		Cell.sKeys.add(name);
		Cell.sDefault.add(value);
		foreachCell(c -> c.mValues.add(finalValue));
		fields.add(new PropertyField(name, finalValue));
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

	public static void repaintFields() {
		fieldsPanel.removeAll();
		fields.forEach(fieldsPanel::add);
		fieldsPanel.revalidate();
	}

	public static void onClick(Point p) {
		if (p.x < 0 || p.x > w || p.y < 0 || p.y > h)
			return;
		if (clickMode == 1) { // pick
			Cell src = cells[p.y][p.x];
			for (int i = 0; i < fields.size(); i++)
				fields.get(i).field.setText(src.mValues.get(i).toString());
			fieldsPanel.revalidate();
		} else if (clickMode == 2) { // clone
			Cell dst = cells[p.y][p.x];
			for (int i = 0; i < fields.size(); i++) {
				Number num = parseNumber(fields.get(i).field.getText());
				if (num != null)
					dst.mValues.set(i, num);
			}
			updateColor(p.y, p.x);
			map.repaint();
		}
	}

	public static void defineColoring() {
		String expr = JOptionPane.showInputDialog(frame, "Enter coloring formula.\nValues of properties are given by p1, p2, etc.\nColoring formula must be in the from R,G,B where each one is between 0 and 1.", colorExpr[0] + "," + colorExpr[1] + "," + colorExpr[2]);
		if (expr == null)
			return;
		String rgb[] = expr.split(",");
		if (rgb.length != 3)
			return;
		colorExpr = rgb;
		updateColors();
	}

	public static void updateColors() {
		boolean s = true;
		for (int i = 0; i < h; i++)
			for (int j = 0; j < w; j++)
				s &= updateColor(i, j);
		if (!s)
			JOptionPane.showMessageDialog(frame, "Error in color evaluation.");
		map.repaint();
	}

	public static boolean updateColor(int i, int j) {
		int size = cells[i][j].mValues.size();
		for (int k = 0; k < size; k++)
			engine.put("p"+(k+1), cells[i][j].mValues.get(k));
		try {
			Number r = (Number) engine.eval(colorExpr[0]);
			Number g = (Number) engine.eval(colorExpr[1]);
			Number b = (Number) engine.eval(colorExpr[2]);
			cells[i][j].color[0] = (int) Math.min(Math.max(256 * r.doubleValue(), 0), 255);
			cells[i][j].color[1] = (int) Math.min(Math.max(256 * g.doubleValue(), 0), 255);
			cells[i][j].color[2] = (int) Math.min(Math.max(256 * b.doubleValue(), 0), 255);
		} catch (ScriptException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static void updateBtnColors() {
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
	}

}