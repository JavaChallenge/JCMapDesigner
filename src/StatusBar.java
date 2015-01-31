import javax.swing.*;
import java.awt.*;

/**
 * Created by Hadi on 1/31/2015 9:40 PM.
 */
public class StatusBar extends JLabel {
    public static final String status = "Status. ";
    public static final String mode = "Click Mode: ";
    public static final String modes[] = new String[] {"None", "Pick", "Clone"};
    public static final String point = "Map Point: ";

    private Point currentPoint = new Point(0, 0);

    public StatusBar() {
        super("Status: ");
    }

    public void setPoint(Point p) {
        currentPoint = p;
        repaintme();
    }

    public void repaintme() {
        setText(getText(MapDesigner.clickMode, currentPoint));
        repaint();
    }

    public static String getText(int m, Point p) {
        return status + mode + modes[m] + ", " + point + "[" + p.x + ", " + p.y + "]";
    }

}
