package view;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Hadi on 2/3/2015 11:05 AM.
 */
public class PropertiesList extends JPanel {

    public PropertiesList(String[] fields, Number[] defaults) {
        setLayout(new GridLayout(0, 1));
        for (int i = 0; i < fields.length; i++)
            add(new PropertyField(fields[i], defaults[i]));
    }

}
