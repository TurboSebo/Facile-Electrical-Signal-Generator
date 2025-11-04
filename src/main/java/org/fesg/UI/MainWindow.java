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
        exitMenu.addActionListener(e -> System.exit(0));
        fileMenu.add(exitMenu);

        // -- Tools Menu / Narzędzia --
        JMenu toolsMenu = new JMenu(languageManager.getString(TranslationKey.MENU_TOOLS));

        JCheckBoxMenuItem autosearchCheckbox = new JCheckBoxMenuItem(languageManager.getString(TranslationKey.MENU_TOOLS_AUTOSEARCH));
        autosearchCheckbox.setSelected(arduinoDetector.isAutosearch());
        autosearchCheckbox.addActionListener(e -> {
            if (arduinoDetector != null) {
                arduinoDetector.toggleAutosearch();
                autosearchCheckbox.setSelected(arduinoDetector.isAutosearch());
            }
        });
        toolsMenu.add(autosearchCheckbox);
        toolsMenu.addSeparator(); // Linia oddzielająca

        // PodMenu do wyboru portu (wydzielone)
        toolsMenu.add(buildSelectPortMenu(autosearchCheckbox));

        // Dodanie menu do paska menu
        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        setJMenuBar(menuBar);
    }

    private JMenu buildSelectPortMenu(JCheckBoxMenuItem autosearchCheckbox) {
        JMenu selectPortMenu = new JMenu(languageManager.getString(TranslationKey.MENU_TOOLS_SELECT_PORT));

        selectPortMenu.addMenuListener(new javax.swing.event.MenuListener() {
            @Override
            public void menuSelected(javax.swing.event.MenuEvent menuEvent) {
                selectPortMenu.removeAll(); //czyszczenie menu przed wypelnieniem
                com.fazecast.jSerialComm.SerialPort[] ports = arduinoDetector.getAvailablePorts();

                if (ports.length > 0) {
                    for (com.fazecast.jSerialComm.SerialPort port : ports) { //każdy port jako klikalna opcja
                        String portName = port.getSystemPortName() + " (" + port.getPortDescription() + ")"; //tworzenie nazwy
                        JMenuItem portItem = new JMenuItem(portName);

                        //Akcja po kliknięciu na port
                        portItem.addActionListener(event -> {
                            arduinoDetector.forceConnect(port); //
                            autosearchCheckbox.setSelected(false); // wyłączenie autowyszukiwania
                        });
                        selectPortMenu.add(portItem);
                    }
                } else {
                    JMenuItem noPortsItem = new JMenuItem(languageManager.getString(TranslationKey.MENU_TOOLS_NO_PORTS));
                    noPortsItem.setEnabled(false);
                    selectPortMenu.add(noPortsItem);
                }

            }

            // Te metody muszą być zaimplementowane
            @Override public void menuDeselected(javax.swing.event.MenuEvent e) {}
            @Override public void menuCanceled(javax.swing.event.MenuEvent e) {}
        });

        return selectPortMenu;
    }

    public void setStatus(org.fesg.service.ConnectionState connectionState) {
        SwingUtilities.invokeLater(() -> {
            if (connectionState == org.fesg.service.ConnectionState.CONNECTED && arduinoDetector != null) {
                statusBar.setDetectedPort(arduinoDetector.getDetectedPort());
            }
            statusBar.setStatus(connectionState);
        });
    }

    public void setError(String error) {
        SwingUtilities.invokeLater(() -> statusBar.setError(error));
    }
}
