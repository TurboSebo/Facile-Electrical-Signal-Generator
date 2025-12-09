package org.fesg.UI;

import org.fesg.service.ArduinoService;

import javax.swing.*;
import java.awt.*;

public class GeneratorPanel extends JPanel {

    private final ConsoleLogger consoleLogger;
    private ArduinoService arduinoService;

    private JTextField freqField;
    private JTextField burstField;
    private JButton btnStartSine;
    private JButton btnStopGen;
    private JButton btnOnce;
    private JButton btnBurst;
    private JButton btnSetFreq;

    public GeneratorPanel(ConsoleLogger consoleLogger, ArduinoService arduinoService) {
        this.consoleLogger = consoleLogger;
        this.arduinoService = arduinoService;
        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel freqPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        freqPanel.setBorder(BorderFactory.createTitledBorder("Konfiguracja"));

        freqField = new JTextField("1.0", 5);
        freqField.setFont(new Font("Monospaced", Font.BOLD, 16));
        btnSetFreq = new JButton("Ustaw Hz");

        btnSetFreq.addActionListener(e -> {
            try {
                String val = freqField.getText().replace(",", ".");
                float f = Float.parseFloat(val);
                if (arduinoService != null) {
                    arduinoService.send("FREQ " + f);
                    consoleLogger.appendToConsole(">>> [GEN] Ustawianie częstotliwości: " + f + " Hz");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Podaj poprawną liczbę (np. 1.5)", "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        });

        freqPanel.add(new JLabel("Częstotliwość (Hz): "));
        freqPanel.add(freqField);
        freqPanel.add(btnSetFreq);

        JPanel controlPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Sterowanie Ciągłe"));

        btnStartSine = new JButton("START (Ciągły)");
        btnStartSine.setBackground(new Color(150, 255, 150));
        btnStartSine.addActionListener(e -> {
            if (arduinoService != null) {
                arduinoService.send("START");
                consoleLogger.appendToConsole(">>> [GEN] Start trybu ciągłego");
            }
        });

        btnStopGen = new JButton("STOP");
        btnStopGen.setBackground(new Color(255, 150, 150));
        btnStopGen.addActionListener(e -> {
            if (arduinoService != null) {
                arduinoService.send("STOP");
                consoleLogger.appendToConsole(">>> [GEN] Zatrzymanie");
            }
        });

        btnOnce = new JButton("JEDEN CYKL");
        btnOnce.addActionListener(e -> {
            if (arduinoService != null) {
                arduinoService.send("ONCE");
                consoleLogger.appendToConsole(">>> [GEN] Wyzwolenie pojedynczego cyklu");
            }
        });

        controlPanel.add(btnStartSine);
        controlPanel.add(btnStopGen);
        controlPanel.add(btnOnce);

        JPanel burstPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        burstPanel.setBorder(BorderFactory.createTitledBorder("Tryb Serii (Burst)"));

        burstField = new JTextField("3", 4);
        burstField.setFont(new Font("Monospaced", Font.BOLD, 16));
        btnBurst = new JButton("WYKONAJ SERIĘ");

        btnBurst.addActionListener(e -> {
            try {
                int count = Integer.parseInt(burstField.getText().trim());
                if (arduinoService != null) {
                    arduinoService.send("BURST " + count);
                    consoleLogger.appendToConsole(">>> [GEN] Seria: " + count + " powtórzeń");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Podaj liczbę całkową!", "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        });

        burstPanel.add(new JLabel("Ilość powtórzeń: "));
        burstPanel.add(burstField);
        burstPanel.add(btnBurst);

        gbc.gridx = 0; gbc.gridy = 0;
        add(freqPanel, gbc);

        gbc.gridy = 1;
        add(controlPanel, gbc);

        gbc.gridy = 2;
        add(burstPanel, gbc);

        gbc.gridy = 3; gbc.weighty = 1.0;
        add(new JPanel(), gbc);
    }

    public void setControlsEnabled(boolean enabled) {
        if (btnStartSine != null) btnStartSine.setEnabled(enabled);
        if (btnStopGen != null) btnStopGen.setEnabled(enabled);
        if (btnOnce != null) btnOnce.setEnabled(enabled);
        if (btnBurst != null) btnBurst.setEnabled(enabled);
        if (btnSetFreq != null) btnSetFreq.setEnabled(enabled);
    }

    public void setArduinoService(ArduinoService arduinoService) {
        this.arduinoService = arduinoService;
    }
}

