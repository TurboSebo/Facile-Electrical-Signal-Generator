package org.fesg.UI;

import org.fesg.i18n.LanguageManager;
import org.fesg.i18n.TranslationKey;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    private final LanguageManager languageManager;
    private StatusBar statusBar;

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

        setupMenuBar();

        // Dodanie paska statusu
        statusBar = new StatusBar();
        add(statusBar, BorderLayout.SOUTH);
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu(languageManager.getString(TranslationKey.MENU_FILE));
        JMenuItem exitMenu = new JMenuItem(languageManager.getString(TranslationKey.MENU_FILE_EXIT));
        exitMenu.addActionListener(e -> System.exit(0));
        fileMenu.add(exitMenu);

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }

    public void setStatus(String status) {
        // Upewnij się, że aktualizacja GUI jest w wątku EDT
        SwingUtilities.invokeLater(() -> statusBar.setStatus(status));
    }

    public void setError(String error) {
        SwingUtilities.invokeLater(() -> statusBar.setError(error));
    }
}
