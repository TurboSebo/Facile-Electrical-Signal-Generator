package org.fesg.UI;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    public MainWindow() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Facile Electrical Signal Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); //ustawia okno na środku ekranu

        //Układ główny
        setLayout(new BorderLayout());

        setupMenuBar();
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem exitMenu = new JMenuItem("Exit");
        exitMenu.addActionListener(e -> System.exit(0));
        fileMenu.add(exitMenu);

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }


}
