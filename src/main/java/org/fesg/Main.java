package org.fesg;

import org.fesg.UI.MainWindow;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                //e.printStackTrace();
                throw new RuntimeException(e);
            }
            new MainWindow().setVisible(true);
        });
    }
}