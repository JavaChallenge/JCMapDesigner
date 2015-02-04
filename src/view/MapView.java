package view;

import model.map.Cell;
import model.map.Map;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

/**
 * Created by Hadi on 2/3/2015 9:41 AM.
 */
public class MapView extends JPanel {

    public static int SIZE_MIN = 7, SIZE_MAX = 70;
    public static int DEFAULT_SIZE = 20, DEFAULT_OFF_X = -10, DEFAULT_OFF_Y = -10;

    private Supplier<Map> mMapProvider;
    private int mSize = DEFAULT_SIZE, mOffX = DEFAULT_OFF_X, mOffY = DEFAULT_OFF_Y;

    public MapView(Supplier<Map> mapProvider) {
        mMapProvider = mapProvider;
    }

    public void zoom(Point origin, double amount) {
        double cx = origin.getX(), cy = origin.getY();
        int newSize = (int) Math.round(mSize * Math.exp(-amount / 10));
        newSize = Math.max(newSize, SIZE_MIN);
        newSize = Math.min(newSize, SIZE_MAX);
        double c = (double)newSize/ mSize;
        mOffX = (int) (c*(mOffX + cx) - cx);
        mOffY = (int) (c*(mOffY + cy) - cy);
        mSize = newSize;
        repaint();
    }

    public void translate(int offX, int offY) {
        mOffX -= offX;
        mOffY -= offY;
    }

    public void resetView() {
        mSize = DEFAULT_SIZE;
        mOffX = DEFAULT_OFF_X;
        mOffY = DEFAULT_OFF_Y;
        repaint();
    }

    public void setMapProvider(Supplier<Map> mapProvider) {
        mMapProvider = mapProvider;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Rectangle b = g.getClip().getBounds();
        g.setColor(Color.WHITE);
        g.fillRect(b.x, b.y, b.width, b.height);

        if (mMapProvider == null)
            return;
        Map map = mMapProvider.get();
        int h = map.getHeight(), w = map.getWidth();
        g.translate(-mOffX, -mOffY);
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                map.at(j, i).paint(g, mSize);
                g.translate(mSize, 0);
            }
            g.translate(-w* mSize, mSize);
        }
        g.translate(0, -h* mSize);
        g.translate(mOffX, mOffY);
    }

    public Point getMapPoint(Point mousePoint) {
        return new Point((mousePoint.x + mOffX)/ mSize, (mousePoint.y + mOffY)/ mSize);
    }

}
