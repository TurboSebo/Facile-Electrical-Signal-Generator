package org.fesg.UI;

import org.fesg.i18n.LanguageManager;
import org.fesg.i18n.TranslationKey;
import org.fesg.service.ArduinoCommands;
import org.fesg.service.ArduinoService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class FilePlayerPanel extends JPanel {

    private final LanguageManager languageManager = LanguageManager.getInstance();
    private ArduinoService arduinoService;

    // prosty logger do konsoli
    private final ConsoleLogger consoleLogger;

    // flaga do uniknięcia spamu przy braku połączenia
    private boolean connectionWarningShown = false;

    // licznik wszystkich wysłanych próbek w trakcie bieżącego odtwarzania
    private long totalSamplesSent = 0;
    // indeks próbki w aktualnym przebiegu sekwencji (dla paska postępu)
    private int currentSampleIndex = 0;

    private JLabel selectedFileLabel;
    private JLabel statusLabel;
    private JTextField delayField;
    private JButton btnPlay;
    private JButton btnStop;
    private JButton btnLoad;
    private JProgressBar progressBar;
    private JCheckBox loopedCheckBox;
    boolean looped = false;
    private List<Integer> loadedSequence = new ArrayList<>();
    private volatile boolean isPlaying = false;
    private Thread playerThread;

    private int lastSentIndex = 0;  // indeks ostatnio wysłanej próbki – używany w wątku i w lambdzie

    public FilePlayerPanel(ConsoleLogger consoleLogger) {
        this.consoleLogger = consoleLogger;
        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_TITLE)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Sekcja wyboru pliku
        btnLoad = new JButton(languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_BUTTON_LOAD));
        selectedFileLabel = new JLabel(languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_NO_FILE));

        btnLoad.addActionListener(e -> chooseFile());

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(btnLoad, gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        add(selectedFileLabel, gbc);

        JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        configPanel.add(new JLabel(languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_DELAY_LABEL)));
        delayField = new JTextField("50", 5);
        configPanel.add(delayField);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        add(configPanel, gbc);


        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        statusLabel = new JLabel(languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_STATUS_READY));
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusLabel.setForeground(Color.BLUE); // ujednolicone

        gbc.gridy = 2;
        add(progressBar, gbc);
        gbc.gridy = 3;
        add(statusLabel, gbc);

        //przyciski
        JPanel controlPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPlay = new JButton(languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_BUTTON_PLAY));
        btnPlay.setBackground(new Color(150, 255, 150));
        btnPlay.setEnabled(false);

        btnStop = new JButton(languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_BUTTON_STOP));
        btnStop.setBackground(new Color(255, 150, 150)); // poprawiony kolor
        btnStop.setEnabled(false);

        loopedCheckBox = new JCheckBox(languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_CHECKBOX_LOOP), false);
        btnPlay.addActionListener(e -> startPlaying());
        btnStop.addActionListener(e -> stopPlaying());
        loopedCheckBox.addActionListener(e -> {
            looped = loopedCheckBox.isSelected();
            consoleLogger.appendToConsole(languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_LOG_LOOP) + " " + (looped ? languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_LOG_ON) : languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_LOG_OFF)));
        });

        controlPanel.add(btnPlay);
        controlPanel.add(btnStop);
        controlPanel.add(loopedCheckBox);
        gbc.gridy = 4;
        add(controlPanel, gbc);

        gbc.gridy = 5;
        gbc.weighty = 1.0;
        add(new JPanel(), gbc);
    }

    private void chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_FILTER_DESC), "txt", "csv");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile() != null) {
            loadFile(fileChooser.getSelectedFile());
        } else {
            // użytkownik anulował – nie zmieniamy bieżącego stanu
            statusLabel.setText(languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_STATUS_CANCELLED));
        }
    }

    private void loadFile(File file) {
        loadedSequence.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    if (line.contains(",")) {
                        line = line.split(",")[0];
                    }
                    try {
                        int val = Integer.parseInt(line);
                        if (val < 0) val = 0;
                        if (val > 4095) val = 4095;
                        loadedSequence.add(val);
                    } catch (NumberFormatException ignored) {
                        // Ignoruj linie, które nie są liczbami
                    }
                }
            }
            selectedFileLabel.setText(languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_FILE_LABEL_PREFIX) + file.getName() + " [" + loadedSequence.size() + languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_FILE_LABEL_SUFFIX));
            btnPlay.setEnabled(!loadedSequence.isEmpty());
            progressBar.setMaximum(Math.max(1, loadedSequence.size()));
            progressBar.setValue(0);
            statusLabel.setForeground(Color.BLUE);
            statusLabel.setText(languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_STATUS_LOADED));
        } catch (Exception e) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText(languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_STATUS_ERROR_READ));
            btnPlay.setEnabled(false);
            progressBar.setValue(0);
        }
    }

    private void startPlaying() {
        if (loadedSequence.isEmpty() || arduinoService == null) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText(languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_STATUS_NO_CONNECTION));

            // pojedynczy komunikat do konsoli przy braku połączenia
            if (arduinoService == null && !connectionWarningShown) {
                if (consoleLogger != null) {
                    consoleLogger.appendToConsole(languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_LOG_NO_CONNECTION));
                }
                connectionWarningShown = true;
            }
            return;
        }

        int delay;
        try {
            delay = Integer.parseInt(delayField.getText().trim());
            if (delay < 5) delay = 5;
        } catch (NumberFormatException e) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText(languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_STATUS_INVALID_DELAY));
            delay = 50;
            delayField.setText("50");
        }
        final int finalDelay = delay;

        // reset liczników przed startem wątku
        totalSamplesSent = 0;
        lastSentIndex = 0;
        currentSampleIndex = 0;

        isPlaying = true;
        btnPlay.setEnabled(false);
        btnStop.setEnabled(true);
        btnLoad.setEnabled(false);
        delayField.setEnabled(false);
        loopedCheckBox.setEnabled(false);
        progressBar.setValue(0);
        statusLabel.setForeground(Color.BLUE);
        statusLabel.setText(languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_STATUS_PLAYING));

        // informacja do konsoli o starcie
        if (consoleLogger != null) {
            consoleLogger.appendToConsole(languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_LOG_START_PREFIX) + loadedSequence.size() + languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_LOG_SAMPLES) + finalDelay + languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_LOG_MS_SUFFIX));
        }

        playerThread = new Thread(() -> {
            // konfiguracja ograniczenia aktualizacji UI
            final int UI_UPDATE_EVERY_N_SAMPLES = 10;     // co 10 próbek
            final long UI_UPDATE_MIN_INTERVAL_MS = 100L;  // ale nie częściej niż co 100 ms
            long lastUiUpdateTime = 0L;

            try {
                arduinoService.send(ArduinoCommands.STOP);

                int loopIteration = 0;

                do {
                    loopIteration++;
                    currentSampleIndex = 0;

                    final int loopIterationForLabel = loopIteration;// użyj zmiennej finalnej dla lambdy

                    // zresetuj pasek dla nowego przebiegu sekwencji
                    SwingUtilities.invokeLater(() -> {
                        progressBar.setMaximum(Math.max(1, loadedSequence.size()));
                        progressBar.setValue(0);
                        statusLabel.setText(languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_STATUS_PLAYING_LOOP) + loopIterationForLabel + languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_STATUS_PLAYING_LOOP_SUFFIX));
                    });

                    for (int i = 0; i < loadedSequence.size(); i++) {
                        if (!isPlaying || Thread.currentThread().isInterrupted()) {
                            break;
                        }

                        Integer value = loadedSequence.get(i);
                        arduinoService.send(ArduinoCommands.setDac(value));   //  setDac(int) jest żeby uniknąć niepotrzebnego String.valueOf w pętli


                        // aktualizacja liczników
                        totalSamplesSent++;
                        lastSentIndex = i;
                        currentSampleIndex = i;

                        // decyduje czy zaktualizować UI
                        long now = System.currentTimeMillis();
                        boolean shouldUpdateUi =
                                ((currentSampleIndex + 1) % UI_UPDATE_EVERY_N_SAMPLES == 0) ||
                                        (now - lastUiUpdateTime >= UI_UPDATE_MIN_INTERVAL_MS);

                        if (shouldUpdateUi) {
                            lastUiUpdateTime = now;
                            final int progressCurrent = currentSampleIndex + 1;
                            final int totalInSequence = loadedSequence.size();
                            SwingUtilities.invokeLater(() -> {
                                progressBar.setMaximum(Math.max(1, totalInSequence));
                                progressBar.setValue(progressCurrent);
                                statusLabel.setText(languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_STATUS_SENT) + progressCurrent + " / " + totalInSequence);
                            });
                        }

                        // lekkie logowanie co większy krok, żeby nie spamować
                        if (consoleLogger != null && totalSamplesSent % 500 == 0) {
                            final long progressForLog = totalSamplesSent;
                            consoleLogger.appendToConsole(languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_LOG_SENT_PREFIX) + progressForLog + languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_LOG_SENT_SUFFIX));
                        }

                        try {
                            Thread.sleep(finalDelay);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                } while (looped && isPlaying && !Thread.currentThread().isInterrupted());
            } finally {
                isPlaying = false;
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setForeground(Color.BLUE);
                    statusLabel.setText(languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_STATUS_FINISHED));
                    resetControls();
                    if (consoleLogger != null) {
                        consoleLogger.appendToConsole(languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_LOG_FINISHED_PREFIX) + totalSamplesSent + languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_LOG_TOTAL_SUFFIX));
                    }
                });
            }
        }, "file-player-thread");
        playerThread.start();
    }

    private void stopPlaying() {
        isPlaying = false;
        if (playerThread != null) {
            playerThread.interrupt();
        }
        progressBar.setValue(0);
        statusLabel.setForeground(Color.BLUE);
        statusLabel.setText(languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_STATUS_STOPPED));
        resetControls();

        if (consoleLogger != null) {
            consoleLogger.appendToConsole(languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_LOG_STOPPED_PREFIX) + totalSamplesSent + languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_LOG_TOTAL_SUFFIX));
        }
    }

    private void resetControls() {
        btnPlay.setEnabled(!loadedSequence.isEmpty());
        btnStop.setEnabled(false);
        btnLoad.setEnabled(true);
        delayField.setEnabled(true);
        loopedCheckBox.setEnabled(true);
    }

    public void setArduinoService(ArduinoService arduinoService) {
        this.arduinoService = arduinoService;
        setEnabled(arduinoService != null);
    }

    /**
     * Wywoływane z MainWindow przy zmianie stanu połączenia.
     * Resetuje flagę, aby komunikat o braku połączenia mógł znów pojawić się
     * tylko raz po kolejnym rozłączeniu.
     */
    public void onConnectionStateChanged(boolean connected) {
        if (connected) {
            connectionWarningShown = false;
        }
    }
}
