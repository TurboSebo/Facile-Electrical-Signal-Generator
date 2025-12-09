package org.fesg.UI;

import org.fesg.i18n.LanguageManager;
import org.fesg.i18n.TranslationKey;
import org.fesg.service.ArduinoCommands;
import org.fesg.service.ArduinoService;

import javax.swing.*;
import java.awt.*;

public class GeneratorPanel extends JPanel {

    private final ConsoleLogger consoleLogger;
    private final LanguageManager languageManager = LanguageManager.getInstance();
    private ArduinoService arduinoService;

    private JTextField freqField;
    private JTextField burstField;
    private JButton btnStartSine;
    private JButton btnStopGen;
    private JButton btnOnce;
    private JButton btnBurst;
    private JButton btnSetFreq;
    private JRadioButton waveSineButton;
    private JRadioButton waveTriangleButton;

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
        freqPanel.setBorder(BorderFactory.createTitledBorder(
                languageManager.getString(TranslationKey.PANEL_GENERATOR_CONFIG_TITLE)));

        freqField = new JTextField("1.0", 5);
        freqField.setFont(new Font("Monospaced", Font.BOLD, 16));
        btnSetFreq = new JButton(languageManager.getString(TranslationKey.PANEL_GENERATOR_BUTTON_SET_FREQ));

        btnSetFreq.addActionListener(e -> {
            try {
                String val = freqField.getText().replace(",", ".");
                float f = Float.parseFloat(val);
                if (arduinoService != null) {
                    arduinoService.send(ArduinoCommands.setFrequency(f));
                    consoleLogger.appendToConsole(">>> [GEN] Ustawianie częstotliwości: " + f + " Hz");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Podaj poprawną liczbę (np. 1.5)", "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        });

        freqPanel.add(new JLabel(languageManager.getString(TranslationKey.PANEL_GENERATOR_FREQUENCY_LABEL)));
        freqPanel.add(freqField);
        freqPanel.add(btnSetFreq);

        // Wybór typu fali
        JPanel wavePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wavePanel.add(new JLabel(languageManager.getString(TranslationKey.PANEL_GENERATOR_WAVE_TYPE_LABEL)));
        waveSineButton = new JRadioButton(languageManager.getString(TranslationKey.PANEL_GENERATOR_WAVE_TYPE_SINE));
        waveTriangleButton = new JRadioButton(languageManager.getString(TranslationKey.PANEL_GENERATOR_WAVE_TYPE_TRIANGLE));
        ButtonGroup waveGroup = new ButtonGroup();
        waveGroup.add(waveSineButton);
        waveGroup.add(waveTriangleButton);
        waveSineButton.setSelected(true);

        waveSineButton.addActionListener(e -> {
            if (arduinoService != null) {
                arduinoService.send(ArduinoCommands.WAVE_SINE);
                consoleLogger.appendToConsole(">>> [GEN] Ustawianie fali: SIN");
            }
        });
        waveTriangleButton.addActionListener(e -> {
            if (arduinoService != null) {
                arduinoService.send(ArduinoCommands.WAVE_TRIANGLE);
                consoleLogger.appendToConsole(">>> [GEN] Ustawianie fali: TRI");
            }
        });

        wavePanel.add(waveSineButton);
        wavePanel.add(waveTriangleButton);

        JPanel controlPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        controlPanel.setBorder(BorderFactory.createTitledBorder(
                languageManager.getString(TranslationKey.PANEL_GENERATOR_CONTINUOUS_TITLE)));

        btnStartSine = new JButton(languageManager.getString(TranslationKey.PANEL_GENERATOR_BUTTON_START_CONTINUOUS));
        btnStartSine.setBackground(new Color(150, 255, 150));
        btnStartSine.addActionListener(e -> {
            if (arduinoService != null) {
                arduinoService.send(ArduinoCommands.START);
                consoleLogger.appendToConsole(">>> [GEN] Start trybu ciągłego");
            }
        });

        btnStopGen = new JButton(languageManager.getString(TranslationKey.PANEL_GENERATOR_BUTTON_STOP));
        btnStopGen.setBackground(new Color(255, 150, 150));
        btnStopGen.addActionListener(e -> {
            if (arduinoService != null) {
                arduinoService.send(ArduinoCommands.STOP);
                consoleLogger.appendToConsole(">>> [GEN] Zatrzymanie");
            }
        });

        btnOnce = new JButton(languageManager.getString(TranslationKey.PANEL_GENERATOR_BUTTON_ONCE));
        btnOnce.addActionListener(e -> {
            if (arduinoService != null) {
                arduinoService.send(ArduinoCommands.ONCE);
                consoleLogger.appendToConsole(">>> [GEN] Wyzwolenie pojedynczego cyklu");
            }
        });

        controlPanel.add(btnStartSine);
        controlPanel.add(btnStopGen);
        controlPanel.add(btnOnce);

        JPanel burstPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        burstPanel.setBorder(BorderFactory.createTitledBorder(
                languageManager.getString(TranslationKey.PANEL_GENERATOR_BURST_TITLE)));

        burstField = new JTextField("3", 4);
        burstField.setFont(new Font("Monospaced", Font.BOLD, 16));
        btnBurst = new JButton(languageManager.getString(TranslationKey.PANEL_GENERATOR_BUTTON_BURST));

        btnBurst.addActionListener(e -> {
            try {
                int count = Integer.parseInt(burstField.getText().trim());
                if (arduinoService != null) {
                    arduinoService.send(ArduinoCommands.burst(count));
                    consoleLogger.appendToConsole(">>> [GEN] Seria: " + count + " powtórzeń");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Podaj liczbę całkowitą!", "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        });

        burstPanel.add(new JLabel(languageManager.getString(TranslationKey.PANEL_GENERATOR_BURST_COUNT_LABEL)));
        burstPanel.add(burstField);
        burstPanel.add(btnBurst);

        gbc.gridx = 0; gbc.gridy = 0;
        add(freqPanel, gbc);

        gbc.gridy = 1;
        add(wavePanel, gbc);

        gbc.gridy = 2;
        add(controlPanel, gbc);

        gbc.gridy = 3;
        add(burstPanel, gbc);

        gbc.gridy = 4; gbc.weighty = 1.0;
        add(new JPanel(), gbc);
    }

    public void setControlsEnabled(boolean enabled) {
        if (btnStartSine != null) btnStartSine.setEnabled(enabled);
        if (btnStopGen != null) btnStopGen.setEnabled(enabled);
        if (btnOnce != null) btnOnce.setEnabled(enabled);
        if (btnBurst != null) btnBurst.setEnabled(enabled);
        if (btnSetFreq != null) btnSetFreq.setEnabled(enabled);
        if (waveSineButton != null) waveSineButton.setEnabled(enabled);
        if (waveTriangleButton != null) waveTriangleButton.setEnabled(enabled);
    }

    public void setArduinoService(ArduinoService arduinoService) {
        this.arduinoService = arduinoService;
    }
}
