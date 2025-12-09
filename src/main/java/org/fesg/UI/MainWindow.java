package org.fesg.UI;

import org.fesg.i18n.AppLanguage;
import org.fesg.i18n.LanguageManager;
import org.fesg.i18n.TranslationKey;
import org.fesg.service.ArduinoService;
import org.fesg.service.ConnectionState;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame implements ConsoleLogger {

    private final LanguageManager languageManager;
    private StatusBar statusBar;
    private ArduinoService arduinoService;

    private ConsolePanel consolePanel;
    private ManualControlPanel manualPanel;
    private GeneratorPanel generatorPanel;
    private FilePlayerPanel filePlayerPanel;

    public MainWindow() {
        this.languageManager = LanguageManager.getInstance();
        initializeUI();
    }

    private void initializeUI() {
        setTitle(languageManager.getString(TranslationKey.APP_TITLE));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 14));

        manualPanel = new ManualControlPanel(this, arduinoService);
        generatorPanel = new GeneratorPanel(this, arduinoService);
        filePlayerPanel = new FilePlayerPanel();

        tabbedPane.addTab(languageManager.getString(TranslationKey.TAB_MANUAL_CONTROL), manualPanel);
        tabbedPane.addTab(languageManager.getString(TranslationKey.TAB_WAVE_GENERATOR), generatorPanel);
        tabbedPane.addTab(languageManager.getString(TranslationKey.TAB_FILE_PLAYER), filePlayerPanel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(tabbedPane);
        consolePanel = new ConsolePanel();
        splitPane.setBottomComponent(consolePanel);
        splitPane.setResizeWeight(0.7);

        add(splitPane, BorderLayout.CENTER);

        statusBar = new StatusBar();
        add(statusBar, BorderLayout.SOUTH);

        setupMenuBar();
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu(languageManager.getString(TranslationKey.MENU_FILE));
        JMenuItem exitMenu = new JMenuItem(languageManager.getString(TranslationKey.MENU_FILE_EXIT));
        exitMenu.addActionListener(e -> System.exit(0));
        fileMenu.add(exitMenu);

        JMenu toolsMenu = new JMenu(languageManager.getString(TranslationKey.MENU_TOOLS));
        toolsMenu.add(buildSelectPortMenu());
        toolsMenu.addSeparator();
        JMenuItem clearConsoleItem = new JMenuItem(languageManager.getString(TranslationKey.MENU_TOOLS_CLEAR_CONSOLE));
        clearConsoleItem.addActionListener(e -> consolePanel.clear());
        toolsMenu.add(clearConsoleItem);

        // Podmenu języka wewnątrz "Narzędzia"
        JMenu languageMenu = new JMenu(languageManager.getString(TranslationKey.MENU_LANGUAGE));
        JRadioButtonMenuItem langPl = new JRadioButtonMenuItem(languageManager.getString(TranslationKey.MENU_LANGUAGE_PL));
        JRadioButtonMenuItem langEn = new JRadioButtonMenuItem(languageManager.getString(TranslationKey.MENU_LANGUAGE_EN));

        ButtonGroup langGroup = new ButtonGroup();
        langGroup.add(langPl);
        langGroup.add(langEn);

        // Domyślnie PL
        langPl.setSelected(true);

        langPl.addActionListener(e -> changeLanguage(AppLanguage.PL));
        langEn.addActionListener(e -> changeLanguage(AppLanguage.EN));

        languageMenu.add(langPl);
        languageMenu.add(langEn);
        toolsMenu.addSeparator();
        toolsMenu.add(languageMenu);

        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        setJMenuBar(menuBar);
    }

    private void changeLanguage(AppLanguage language) {
        languageManager.setLanguage(language);
        SwingUtilities.invokeLater(this::refreshTexts);
    }

    private void refreshTexts() {
        setTitle(languageManager.getString(TranslationKey.APP_TITLE));

        if (getJMenuBar() != null) {
            setupMenuBar();
        }

        statusBar.setStatus();
        revalidate();
        repaint();
    }

    public void setArduinoService(ArduinoService arduinoService) {
        this.arduinoService = arduinoService;
        this.arduinoService.setMessageListener(this::handleIncomingData);
        manualPanel.setArduinoService(arduinoService);
        generatorPanel.setArduinoService(arduinoService);
        setupMenuBar();
    }

    private void handleIncomingData(String data) {
        SwingUtilities.invokeLater(() -> {
            try {
                if (data.length() < 8) {
                    float voltage = Float.parseFloat(data);
                    if (manualPanel != null) {
                        manualPanel.updateVoltage(voltage);
                    }
                    appendToConsole("<<< [ADC]: " + data + " V");
                } else {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                appendToConsole("<<< " + data);
            }
        });
    }

    private JMenu buildSelectPortMenu() {
        JMenu selectPortMenu = new JMenu(languageManager.getString(TranslationKey.MENU_TOOLS_SELECT_PORT));
        selectPortMenu.addMenuListener(new javax.swing.event.MenuListener() {
            @Override
            public void menuSelected(javax.swing.event.MenuEvent menuEvent) {
                selectPortMenu.removeAll();
                if (arduinoService == null) return;
                com.fazecast.jSerialComm.SerialPort[] ports = arduinoService.getAvailablePorts();

                if (ports.length > 0) {
                    for (com.fazecast.jSerialComm.SerialPort port : ports) {
                        String portName = port.getSystemPortName() + " (" + port.getPortDescription() + ")";
                        JMenuItem portItem = new JMenuItem(portName);
                        portItem.addActionListener(event -> arduinoService.connectToPort(port));
                        selectPortMenu.add(portItem);
                    }
                } else {
                    JMenuItem noPortsItem = new JMenuItem(languageManager.getString(TranslationKey.MENU_TOOLS_NO_PORTS));
                    noPortsItem.setEnabled(false);
                    selectPortMenu.add(noPortsItem);
                }
            }
            @Override public void menuDeselected(javax.swing.event.MenuEvent e) {}
            @Override public void menuCanceled(javax.swing.event.MenuEvent e) {}
        });
        return selectPortMenu;
    }

    @Override
    public void appendToConsole(String text) {
        SwingUtilities.invokeLater(() -> {
            if (consolePanel != null) {
                consolePanel.append(text);
            }
        });
    }

    public void setStatus(ConnectionState connectionState) {
        SwingUtilities.invokeLater(() -> {
            if (connectionState == ConnectionState.CONNECTED && arduinoService != null) {
                statusBar.setDetectedPort(arduinoService.getDetectedPort());
                enableControls(true);
                appendToConsole("--- POŁĄCZONO Z URZĄRDZENIEM ---");
            } else {
                enableControls(false);
            }
            statusBar.setStatus(connectionState);
        });
    }

    private void enableControls(boolean enable) {
        if (manualPanel != null) manualPanel.setControlsEnabled(enable);
        if (generatorPanel != null) generatorPanel.setControlsEnabled(enable);
    }

    public void setStatusText(String text) {
        SwingUtilities.invokeLater(() -> {
            statusBar.setStatusText(text);
            appendToConsole("[STATUS]: " + text);
        });
    }

    public void setError(String error) {
        SwingUtilities.invokeLater(() -> {
            statusBar.setError(error);
            if (error != null && !error.isBlank()) {
                appendToConsole("[BŁĄD]: " + error);
            }
        });
    }
}
