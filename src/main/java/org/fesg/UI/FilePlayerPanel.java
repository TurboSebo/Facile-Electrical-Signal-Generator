package org.fesg.UI;

import javax.swing.*;
import java.awt.*;

/**
 * Prosty panel placeholder dla zakładki "Odtwarzacz Plików".
 * W przyszłości można tu dodać logikę wczytywania i odtwarzania plików CSV/TXT.
 */
public class FilePlayerPanel extends JPanel {

    public FilePlayerPanel() {
        setLayout(new GridBagLayout());
        add(new JLabel("Tu będzie możliwość wczytania pliku CSV/TXT"));
    }
}

