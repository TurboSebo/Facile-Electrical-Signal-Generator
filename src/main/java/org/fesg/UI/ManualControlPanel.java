package org.fesg.UI;

import org.fesg.i18n.LanguageManager;
import org.fesg.i18n.TranslationKey;
import org.fesg.service.ArduinoCommands;
import org.fesg.service.ArduinoService;

import javax.swing.*;
import java.awt.*;

public class ManualControlPanel extends JPanel {

    private final ConsoleLogger consoleLogger;
    private final LanguageManager languageManager = LanguageManager.getInstance();
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
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                languageManager.getString(TranslationKey.PANEL_MANUAL_TITLE)));

        JPanel dacPanel = new JPanel(new GridBagLayout());
        dacPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                languageManager.getString(TranslationKey.PANEL_MANUAL_DAC_TITLE)));
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

        btnSetDac = new JButton(languageManager.getString(TranslationKey.PANEL_MANUAL_BUTTON_SET_OUTPUT));
        btnSetDac.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnSetDac.setPreferredSize(new Dimension(150, 40));

        btnSetDac.addActionListener(e -> {
            String value = dacValueField.getText();
            consoleLogger.appendToConsole(">>> [CMD] Ustawiam DAC na: " + value);
            if (arduinoService != null) {
                arduinoService.send(ArduinoCommands.setDac(value));
            }
        });

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        dacPanel.add(new JLabel(languageManager.getString(TranslationKey.PANEL_MANUAL_DAC_LEVEL_LABEL)), gbc);
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
        sensorPanel.setBorder(BorderFactory.createTitledBorder(
                languageManager.getString(TranslationKey.PANEL_MANUAL_ANALOG_INPUT_TITLE)));

        voltageDisplayLabel = new JLabel("--- V");
        voltageDisplayLabel.setFont(new Font("SansSerif", Font.BOLD, 48));
        voltageDisplayLabel.setForeground(new Color(0, 100, 200));

        btnReadVoltage = new JButton(languageManager.getString(TranslationKey.PANEL_MANUAL_BUTTON_READ_VOLTAGE));
        btnReadVoltage.setPreferredSize(new Dimension(150, 40));

        btnReadVoltage.addActionListener(e -> {
            consoleLogger.appendToConsole(">>> [CMD] Pytam o napięcie...");
            if (arduinoService != null) {
                arduinoService.send(ArduinoCommands.READ_VOLTAGE);
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
