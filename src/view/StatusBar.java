package view;

import controller.ClickMode;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Hadi on 1/31/2015 9:40 PM.
 */
public class StatusBar extends JLabel {
    public static final String STATUS = "Status. ";
    public static final String MODE = "Click Mode: ";
    public static final String POINT = "Map Point: ";

    private ClickMode mCurrentMode = ClickMode.NONE;
    private Point mCurrentPoint = new Point(0, 0);

    public StatusBar() {
        super("Status: ");
    }

    public void setMode(ClickMode mode) {
        mCurrentMode = mode;
        repaintBar();
    }

    public void setPoint(Point p) {
        mCurrentPoint = p;
        repaintBar();
    }

    public void repaintBar() {
        setText(getText(mCurrentMode, mCurrentPoint));
        repaint();
    }

    public static String getText(ClickMode m, Point p) {
        return STATUS + MODE + m.toString() + ", " + POINT + "[" + p.x + ", " + p.y + "]";
    }

}
