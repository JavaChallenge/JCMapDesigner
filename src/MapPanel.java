import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by Hadi on 1/31/2015 12:09 PM.
 */
public class MapPanel extends JComponent {

    public static int SIZE_MIN = 7, SIZE_MAX = 70;
    public static int DEFAULT_SIZE = 20, DEFAULT_OFFX = -10, DEFAULT_OFFY = -10;

    public int size = DEFAULT_SIZE, offx = DEFAULT_OFFX, offy = DEFAULT_OFFY;

    public boolean mouseBtn[] = new boolean[10];

    public MapPanel() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mouseBtn[e.getButton()] = true;
                MapDesigner.onClick(getMapPoint(e.getPoint()));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mouseBtn[e.getButton()] = false;
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            private long lastTime = 0;
            private Point lastPoint;

            @Override
            public void mouseMoved(MouseEvent e) {
                Point mapPoint = getMapPoint(e.getPoint());
                MapDesigner.status.setPoint(mapPoint);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (mouseBtn[3]) { // right click!
                    long time = System.currentTimeMillis();
                    Point point = e.getPoint();
                    if (time < lastTime + 100) {
                        offx -= point.x - lastPoint.x;
                        offy -= point.y - lastPoint.y;
                    }
                    lastTime = time;
                    lastPoint = point;
                    repaint();
                } else if (mouseBtn[1]) { // left click!
                    MapDesigner.onClick(getMapPoint(e.getPoint()));
                }
            }
        });

        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int cx = e.getX(), cy = e.getY();
                double r = e.getPreciseWheelRotation();
                int nsize = (int) Math.round(size * Math.exp(-r / 10));
                nsize = Math.max(nsize, SIZE_MIN);
                nsize = Math.min(nsize, SIZE_MAX);
                double c = (double)nsize/size;
                offx = (int) (c*(offx + cx) - cx);
                offy = (int) (c*(offy + cy) - cy);
                size = nsize;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Rectangle b = g.getClip().getBounds();
        g.setColor(Color.WHITE);
        g.fillRect(b.x, b.y, b.width, b.height);
        Cell cells[][] = MapDesigner.cells;
        int h = MapDesigner.h, w = MapDesigner.w;
        if (cells == null)
            return;
        g.translate(-offx, -offy);
        for (int i = 0; i < MapDesigner.h; i++) {
            for (int j = 0; j < MapDesigner.w; j++) {
                cells[i][j].paint(g, size);
                g.translate(size, 0);
            }
            g.translate(-w*size, size);
        }
        g.translate(0, -h*size);
        g.translate(offx, offy);
    }

    public void reset() {
        size = DEFAULT_SIZE;
        offx = DEFAULT_OFFX;
        offy = DEFAULT_OFFY;
        repaint();
    }

    public Point getMapPoint(Point mousePoint) {
        return new Point((mousePoint.x + offx)/size, (mousePoint.y + offy)/size);
    }
}
