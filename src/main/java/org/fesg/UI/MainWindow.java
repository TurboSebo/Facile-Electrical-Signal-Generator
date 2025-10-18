package org.fesg.UI;

import org.fesg.i18n.LanguageManager;
import org.fesg.i18n.TranslationKey;
import org.fesg.service.ArduinoDetector;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    private final LanguageManager languageManager;
    private StatusBar statusBar;
    private ArduinoDetector arduinoDetector;


    public MainWindow() {
        this.languageManager = LanguageManager.getInstance();
        initializeUI();
    }

    private void initializeUI() {
        setTitle(languageManager.getString(TranslationKey.APP_TITLE));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); //ustawia okno na środku ekranu

        //Układ główny
        setLayout(new BorderLayout());


        // Dodanie paska statusu
        statusBar = new StatusBar();
        add(statusBar, BorderLayout.SOUTH);
    }

    public void setArduinoDetector(ArduinoDetector arduinoDetector) {
        this.arduinoDetector = arduinoDetector;
        setupMenuBar(); // Przenosimy tutaj, aby mieć pewność, że detektor jest dostępny
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu(languageManager.getString(TranslationKey.MENU_FILE));
        JMenuItem exitMenu = new JMenuItem(languageManager.getString(TranslationKey.MENU_FILE_EXIT));
        JMenu toolsMenu = new JMenu(languageManager.getString(TranslationKey.MENU_TOOLS));
        JCheckBoxMenuItem autosearchCheckbox = new JCheckBoxMenuItem("auto search?");
        autosearchCheckbox.setState(arduinoDetector.isAutosearch());


        exitMenu.addActionListener(e -> System.exit(0));
        autosearchCheckbox.addActionListener(e -> {
            if (arduinoDetector != null) {
                arduinoDetector.toogleAutosearch();
                autosearchCheckbox.setState(arduinoDetector.isAutosearch());
            }
        });
        fileMenu.add(exitMenu);
        toolsMenu.add(autosearchCheckbox);
        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        setJMenuBar(menuBar);
    }

    public void setStatus(org.fesg.service.ConnectionState connectionState) {
        SwingUtilities.invokeLater(() -> statusBar.setStatus(connectionState));
    }

    public void setError(String error) {
        SwingUtilities.invokeLater(() -> statusBar.setError(error));
    }
}
