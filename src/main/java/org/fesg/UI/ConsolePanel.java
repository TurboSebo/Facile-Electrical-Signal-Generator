package org.fesg.UI;

import javax.swing.*;
import java.awt.*;

public class ConsolePanel extends JPanel {

    private final JTextArea consoleArea;

    public ConsolePanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Konsola"));
        setPreferredSize(new Dimension(800, 200));

        consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        consoleArea.setBackground(new Color(30, 30, 30));
        consoleArea.setForeground(new Color(200, 255, 200));

        JScrollPane scrollPane = new JScrollPane(consoleArea);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void append(String text) {
        consoleArea.append(text + "\n");
        consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
    }

    public void clear() {
        consoleArea.setText("");
    }
}

