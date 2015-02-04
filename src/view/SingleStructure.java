package view;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Hadi on 2/3/2015 8:51 AM.
 */
public class SingleStructure extends JPanel {

    private String mName;
    private JLabel mLabel;
    private JButton mButton;

    public SingleStructure(String name) {
        mName = name;
        mLabel = new JLabel(mName);
        mButton = new JButton("(de)select");

        setLayout(new BorderLayout());
        add(mLabel, BorderLayout.WEST);
        add(mButton, BorderLayout.EAST);
    }

    public String getName() {
        return mName;
    }

    public JButton getButton() {
        return mButton;
    }

}
