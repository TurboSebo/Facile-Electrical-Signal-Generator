package org.fesg.UI;

import org.fesg.service.ArduinoService;

import javax.swing.*;
import java.awt.*;

public class ManualControlPanel extends JPanel {

    private final ConsoleLogger consoleLogger;
    private ArduinoService arduinoService;

    private JSlider dacSlider;
    private JTextField dacValueField;
    private JLabel voltageCalcLabel;
    private JLabel voltageDisplayLabel;
    private JButton btnSetDac;
    private JButton btnReadVoltage;

    public ManualControlPanel(ConsoleLogger consoleLogger, ArduinoService arduinoService) {
        this.consoleLogger = consoleLogger;
        this.arduinoService = arduinoService;
        initUI();
    }

    private void initUI() {
        setLayout(new GridLayout(1, 2, 10, 10));
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Manual"));

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

        dacSlider.addChangeListener(e -> {
            int value = dacSlider.getValue();
            dacValueField.setText(String.valueOf(value));
            double voltage = (value / 4095.0) * 5.0;
            voltageCalcLabel.setText(String.format("~ %.2f V", voltage));
        });

        btnSetDac = new JButton("USTAW WYJŚCIE");
        btnSetDac.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnSetDac.setPreferredSize(new Dimension(150, 40));

        btnSetDac.addActionListener(e -> {
            String value = dacValueField.getText();
            consoleLogger.appendToConsole(">>> [CMD] Ustawiam DAC na: " + value);
            if (arduinoService != null) {
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

        JPanel sensorPanel = new JPanel(new GridBagLayout());
        sensorPanel.setBorder(BorderFactory.createTitledBorder("Wejście analogowe (A0)"));

        voltageDisplayLabel = new JLabel("--- V");
        voltageDisplayLabel.setFont(new Font("SansSerif", Font.BOLD, 48));
        voltageDisplayLabel.setForeground(new Color(0, 100, 200));

        btnReadVoltage = new JButton("POBIERZ NAPIĘCIE");
        btnReadVoltage.setPreferredSize(new Dimension(150, 40));

        btnReadVoltage.addActionListener(e -> {
            consoleLogger.appendToConsole(">>> [CMD] Pytam o napięcie...");
            if (arduinoService != null) {
                arduinoService.send("READV?");
            }
        });

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(10, 10, 10, 10);
        gbc2.gridx = 0; gbc2.gridy = 0;
        sensorPanel.add(voltageDisplayLabel, gbc2);
        gbc2.gridy = 1;
        sensorPanel.add(btnReadVoltage, gbc2);

        add(dacPanel);
        add(sensorPanel);
    }

    public void updateVoltage(float voltage) {
        voltageDisplayLabel.setText(String.format("%.3f V", voltage));
    }

    public void setControlsEnabled(boolean enabled) {
        if (btnSetDac != null) btnSetDac.setEnabled(enabled);
        if (btnReadVoltage != null) btnReadVoltage.setEnabled(enabled);
    }

    public void setArduinoService(ArduinoService arduinoService) {
        this.arduinoService = arduinoService;
    }
}

