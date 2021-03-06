package view;

import javax.swing.*;

/**
 * Created by Hadi on 2/3/2015 9:52 AM.
 */
public class Menu extends JMenuBar {

    private JMenu mFileMenu = new JMenu("File");
    private JMenu mOptionsMenu = new JMenu("Options");

    private JMenuItem mNewMap = new JMenuItem("New Map");
    private JMenuItem mLoadMap = new JMenuItem("Load Map");
    private JMenuItem mSaveMap = new JMenuItem("Save Map");
    private JMenuItem mSaveCompMap = new JMenuItem("Save Map (Compressed)");
    private JMenuItem mGenClasses = new JMenuItem("Generate Java Classes");

    private JMenuItem mDefineColoring = new JMenuItem("Define Coloring");

    public Menu() {
        add(mFileMenu);
        add(mOptionsMenu);

        mFileMenu.add(mNewMap);
        mFileMenu.add(mLoadMap);
        mFileMenu.add(mSaveMap);
        mFileMenu.add(mSaveCompMap);
        mFileMenu.add(mGenClasses);

        mOptionsMenu.add(mDefineColoring);
    }

    public JMenu getFileMenu() {
        return mFileMenu;
    }

    public JMenu getOptionsMenu() {
        return mOptionsMenu;
    }

    public JMenuItem getNewMap() {
        return mNewMap;
    }

    public JMenuItem getLoadMap() {
        return mLoadMap;
    }

    public JMenuItem getSaveMap() {
        return mSaveMap;
    }

    public JMenuItem getSaveCompMap() {
        return mSaveCompMap;
    }

    public JMenuItem getGenClasses() {
        return mGenClasses;
    }

    public JMenuItem getDefineColoring() {
        return mDefineColoring;
    }

}
