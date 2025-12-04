package org.fesg.UI;

import org.fesg.i18n.LanguageManager;
import org.fesg.i18n.TranslationKey;
import org.fesg.service.ArduinoService;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    private final LanguageManager languageManager;
    private StatusBar statusBar;
    private ArduinoService arduinoService;

    // Komponenty UI
    private JTextArea consoleArea;

    //Zakładka 1: Manual
    private JSlider dacSlider;
    private JTextField dacValueField;
    private JLabel voltageCalcLabel;
    private JLabel voltageDisplayLabel;
    private JButton btnSetDac;
    private JButton btnReadVoltage;

    //Zakładka 2: Generator
    private JButton btnStartSine;
    private JButton btnStopGen;

    public MainWindow() {
        this.languageManager = LanguageManager.getInstance();
        initializeUI();
    }

    private void initializeUI() {
        setTitle(languageManager.getString(TranslationKey.APP_TITLE));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 650);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 14));

        tabbedPane.addTab("Sterowanie Ręczne", createManualPanel());
        tabbedPane.addTab("Generator Fal", createGeneratorPanel());
        tabbedPane.addTab("Odtwarzacz Plików", createFilePlayerPanel());

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(tabbedPane);
        splitPane.setBottomComponent(createConsolePanel());
        splitPane.setResizeWeight(0.7);

        add(splitPane, BorderLayout.CENTER);

        statusBar = new StatusBar();
        add(statusBar, BorderLayout.SOUTH);
    }

    // --- KLUCZOWA ZMIANA: Podpinamy listenera ---
    public void setArduinoService(ArduinoService arduinoService) {
        this.arduinoService = arduinoService;

        // Rejestrujemy metodę, która odbierze dane z Arduino
        this.arduinoService.setMessageListener(this::handleIncomingData);

        setupMenuBar();
    }

    // --- KLUCZOWA ZMIANA: Obsługa danych przychodzących ---
    private void handleIncomingData(String data) {
        SwingUtilities.invokeLater(() -> {
            // Logika: Jeśli to liczba (np. "2.54"), wyświetl na dużym ekranie.
            // Jeśli tekst (np. "OK: DAC set"), wypisz w konsoli.
            try {
                // Próba konwersji na liczbę
                float voltage = Float.parseFloat(data);
                // Jeśli się udało, to znaczy, że to odczyt napięcia
                voltageDisplayLabel.setText(String.format("%.2f V", voltage));
                appendToConsole("<<< [ODCZYT]: " + data + " V");
            } catch (NumberFormatException e) {
                // To nie liczba, więc zwykły komunikat tekstowy
                appendToConsole("<<< " + data);
            }
        });
    }

    private JPanel createManualPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Manual"));

        // LEWA STRONA: DAC
        JPanel dacPanel = new JPanel(new GridBagLayout());
        dacPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Wyjście napięcia DAC"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        dacSlider = new JSlider(JSlider.HORIZONTAL, 0, 4095, 0);
        dacSlider.setMajorTickSpacing(1024);
        dacSlider.setPaintTicks(true);
        dacSlider.setPaintLabels(true);

        dacValueField = new JTextField("0", 6);
        dacValueField.setFont(new Font("Monospaced", Font.BOLD, 14));
        dacValueField.setHorizontalAlignment(JTextField.CENTER);

        voltageCalcLabel = new JLabel("~ 0.00 V");
        voltageCalcLabel.setForeground(Color.DARK_GRAY);

        dacSlider.addChangeListener(e ->{
            int value = dacSlider.getValue();
            dacValueField.setText(String.valueOf(value));
            double voltage = (value / 4095.0) * 5.0;
            voltageCalcLabel.setText(String.format("~ %.2f V", voltage));
        });

        btnSetDac = new JButton("USTAW WYJŚCIE");
        btnSetDac.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnSetDac.setPreferredSize(new Dimension(150, 40));
        // --- KLUCZOWA ZMIANA: WYSYŁANIE ---
        btnSetDac.addActionListener(e -> {
           String value = dacValueField.getText();
           appendToConsole(">>> [CMD] Ustawiam DAC na: " + value);
           if(arduinoService != null) {
               arduinoService.send("DAC:" + value);
           }
        });

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        dacPanel.add(new JLabel("Poziom (0-4095):"), gbc);
        gbc.gridy = 1;
        dacPanel.add(dacSlider, gbc);
        gbc.gridy = 2; gbc.gridwidth = 1;
        dacPanel.add(dacValueField, gbc);
        gbc.gridx = 1;
        dacPanel.add(new JLabel("="), gbc);
        gbc.gridx = 2;
        dacPanel.add(voltageCalcLabel, gbc);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3;
        dacPanel.add(btnSetDac, gbc);

        // PRAWA STRONA: ODCZYT
        JPanel sensorPanel = new JPanel(new GridBagLayout());
        sensorPanel.setBorder(BorderFactory.createTitledBorder("Wejście analogowe (A0)"));

        voltageDisplayLabel = new JLabel("--- V");
        voltageDisplayLabel.setFont(new Font("SansSerif", Font.BOLD, 48));
        voltageDisplayLabel.setForeground(new Color(0, 100, 200));

        btnReadVoltage = new JButton("POBIERZ NAPIĘCIE");
        btnReadVoltage.setPreferredSize(new Dimension(150, 40));
        // --- KLUCZOWA ZMIANA: WYSYŁANIE ---
        btnReadVoltage.addActionListener(e -> {
            appendToConsole(">>> [CMD] Pytam o napięcie...");
            if(arduinoService != null) {
                arduinoService.send("READV?");
            }
        });

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(10, 10, 10, 10);
        gbc2.gridx = 0; gbc2.gridy = 0;
        sensorPanel.add(voltageDisplayLabel, gbc2);
        gbc2.gridy = 1;
        sensorPanel.add(btnReadVoltage, gbc2);

        mainPanel.add(dacPanel);
        mainPanel.add(sensorPanel);

        return mainPanel;
    }

    private JPanel createGeneratorPanel() {
        JPanel generatorPanel = new JPanel(new GridBagLayout());
        generatorPanel.add(new JLabel("Tutaj będzie panel generowania sinusoidy i trójkąta"));
        return generatorPanel;
    }

    private JPanel createFilePlayerPanel() {
        JPanel filePanel = new JPanel(new GridBagLayout());
        filePanel.add(new JLabel("Tu będzie możliwość wczytania pliku CSV/TXT"));
        return filePanel;
    }

    private JPanel createConsolePanel() {
        JPanel consolePanel = new JPanel(new BorderLayout());
        consolePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Konsola"));
        consolePanel.setPreferredSize(new Dimension(800, 150));

        consoleArea =  new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        consoleArea.setBackground(new Color(30, 30, 30));
        consoleArea.setForeground(new Color(200, 255, 200));

        JScrollPane scrollPane = new JScrollPane(consoleArea);
        consolePanel.add(scrollPane, BorderLayout.CENTER);

        return consolePanel;
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
        JMenuItem clearConsoleItem = new JMenuItem("Wyczyść konsolę");
        clearConsoleItem.addActionListener(e -> consoleArea.setText(""));
        toolsMenu.add(clearConsoleItem);

        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        setJMenuBar(menuBar);
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

    public void appendToConsole(String text){
        SwingUtilities.invokeLater(() -> {
            consoleArea.append(text + "\n");
            consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
        });
    }

    public void setStatus(org.fesg.service.ConnectionState connectionState) {
        SwingUtilities.invokeLater(() -> {
            if (connectionState == org.fesg.service.ConnectionState.CONNECTED && arduinoService != null) {
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
        if (btnSetDac != null) btnSetDac.setEnabled(enable);
        if (btnReadVoltage != null) btnReadVoltage.setEnabled(enable);
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
