package view;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Hadi on 2/3/2015 8:47 AM.
 */
public class StructuresView extends JPanel {

    private JPanel structuresPanel = new JPanel();
    private ArrayList<SingleStructure> structures = new ArrayList<>();
    private JButton mAddObjectType = new JButton("New Type");
    private JButton mRemoveObjectType = new JButton("Remove Type");

    public StructuresView() {
        setLayout(new BorderLayout());
        add(structuresPanel, BorderLayout.NORTH);

        structuresPanel.setLayout(new GridLayout(0, 1));

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(1, 2));
        buttonsPanel.add(mAddObjectType);
        buttonsPanel.add(mRemoveObjectType);

        add(buttonsPanel, BorderLayout.SOUTH);
    }

    public SingleStructure addStructure(String name) {
        SingleStructure structure = new SingleStructure(name);
        structures.add(structure);
        structuresPanel.add(structure);
        structuresPanel.revalidate();
        structuresPanel.repaint();
        return structure;
    }

    public SingleStructure removeStructure(String name) {
        int index = -1;
        for (int i = 0; i < structures.size() && index == -1; i++)
            if (structures.get(i).getName().equals(name))
                index = i;
        if (index == -1)
            return null;
        structuresPanel.remove(index);
        return structures.remove(index);
    }

    public void removeAllStructures() {
        structuresPanel.removeAll();
        structures.clear();
        revalidate();
        repaint();
    }

    public SingleStructure[] getStructures() {
        return structures.toArray(new SingleStructure[structures.size()]);
    }

    public SingleStructure getStructure(String name) {
        int index = -1;
        for (int i = 0; i < structures.size() && index == -1; i++)
            if (structures.get(i).getName().equals(name))
                index = i;
        return index == -1 ? null : structures.get(index);
    }

    public JButton getAddObjectType() {
        return mAddObjectType;
    }

    public JButton getRemoveObjectType() {
        return mRemoveObjectType;
    }

}
