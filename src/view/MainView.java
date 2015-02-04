package view;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Hadi on 2/3/2015 12:18 AM.
 */
public class MainView extends JFrame {

    private MapView mMapView = new MapView(null);
    private JPanel mSideBar = new JPanel();
    private PropertiesView mPropertiesView = new PropertiesView();
    private StructuresView mStructuresView = new StructuresView();
    private Menu mMenu = new Menu();
    private StatusBar mStatusBar = new StatusBar();

    public MainView() {
        setTitle("Map Designer");
        setLayout(new BorderLayout());
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        mSideBar.setLayout(new GridLayout(2, 1));
        mSideBar.setPreferredSize(new Dimension(225, 0));
        mSideBar.setBackground(new Color(225, 225, 225));

        mSideBar.add(mPropertiesView);
        mSideBar.add(mStructuresView);

        add(mSideBar, BorderLayout.EAST);
        add(mMapView, BorderLayout.CENTER);
        add(mStatusBar, BorderLayout.SOUTH);
        setJMenuBar(mMenu);
    }

    public MapView getMapPanel() {
        return mMapView;
    }

    public JPanel getSideBar() {
        return mSideBar;
    }

    public PropertiesView getPropertyView() {
        return mPropertiesView;
    }

    public StructuresView getStructuresView() {
        return mStructuresView;
    }

    public Menu getMenu() {
        return mMenu;
    }

    public StatusBar getStatusBar() {
        return mStatusBar;
    }

}
