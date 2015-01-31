import javax.swing.*;
import java.awt.*;

/**
 * Created by Hadi on 1/31/2015 8:22 PM.
 */
public class PropertyField extends JPanel {
    public JLabel label;
    public JTextField field;

    public PropertyField(String name, Number value) {
        setLayout(new GridLayout(0, 1));
        add(label = new JLabel(name));
        add(field = new JTextField(value.toString()));
    }
}
