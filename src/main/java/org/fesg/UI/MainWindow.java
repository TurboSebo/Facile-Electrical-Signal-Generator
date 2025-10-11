package org.fesg.UI;

import org.fesg.i18n.LanguageManager;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    private final LanguageManager languageManager;

    public MainWindow() {
        this.languageManager = LanguageManager.getInstance();
        initializeUI();
    }

    private void initializeUI() {
        setTitle(languageManager.getString("app.title"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); //ustawia okno na środku ekranu

        //Układ główny
        setLayout(new BorderLayout());

        setupMenuBar();
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu(languageManager.getString("menu.file"));
        JMenuItem exitMenu = new JMenuItem(languageManager.getString("menu.file.exit"));
        exitMenu.addActionListener(e -> System.exit(0));
        fileMenu.add(exitMenu);

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }


}
