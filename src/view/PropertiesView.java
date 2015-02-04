package view;

import controller.MapDesigner;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Hadi on 2/3/2015 9:18 AM.
 */
public class PropertiesView extends JPanel {

    private PropertiesList propertiesList;
    private JButton mPick = new JButton("Pick");
    private JButton mClone = new JButton("Clone");
    private JButton mRemove = new JButton("Remove Obj");
    private JButton mDefaults = new JButton("Load Def");
    private JButton mNewProp = new JButton("New Prop");
    private JButton mRemoveProp = new JButton("Rem Prop");

    public PropertiesView() {
        setLayout(new BorderLayout());

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(3, 2));
        buttonsPanel.add(mPick);
        buttonsPanel.add(mClone);
        buttonsPanel.add(mRemove);
        buttonsPanel.add(mDefaults);
        buttonsPanel.add(mNewProp);
        buttonsPanel.add(mRemoveProp);
        add(buttonsPanel, BorderLayout.SOUTH);
    }

    public void setFields(String[] fields, Number[] defaults) {
        if (propertiesList != null)
            remove(propertiesList);
        propertiesList = new PropertiesList(fields, defaults);
        add(propertiesList, BorderLayout.NORTH);

        revalidate();
        repaint();
    }

    public Number[] getFields() {
        int n = propertiesList.getComponentCount();
        Number[] numbers = new Number[n];
        for (int i = 0; i < n; i++)
            numbers[i] = MapDesigner.parseNumber(((PropertyField) propertiesList.getComponent(i)).getField().getText());
        return numbers;
    }

    public void setField(int index, Number value) {
        ((PropertyField)propertiesList.getComponent(index)).getField().setText(value.toString());
    }

    public JButton getPick() {
        return mPick;
    }

    public JButton getClone() {
        return mClone;
    }

    public JButton getRemove() {
        return mRemove;
    }

    public JButton getDefaults() {
        return mDefaults;
    }

    public JButton getNewProp() {
        return mNewProp;
    }

    public JButton getRemoveProp() {
        return mRemoveProp;
    }

}
