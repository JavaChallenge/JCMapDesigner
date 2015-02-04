package parser;

public class Map {

    private int width, height;
    private Cell[][] cells;

    public Map(int w, int h) {
        width = w;
        height = h;
        cells = new Cell[h][w];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Cell at(int x, int y) {
        return cells[y][x];
    }

    public void set(int x, int y, Cell cell) {
        cells[y][x] = cell;
    }

}
