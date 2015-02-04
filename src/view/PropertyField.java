package view;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Hadi on 1/31/2015 8:22 PM.
 */
public class PropertyField extends JPanel {
    private JLabel mLabel;
    private JTextField mField;

    public PropertyField(String name, Number value) {
        setLayout(new GridLayout(0, 1));
        add(mLabel = new JLabel(name));
        add(mField = new JTextField(value == null ? "" : value.toString()));
    }

    public JTextField getField() {
        return mField;
    }

}
