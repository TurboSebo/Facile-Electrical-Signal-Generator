package org.fesg.UI;

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

    private ArduinoService arduinoService;

    private JLabel selectedFileLabel;
    private JLabel statusLabel;
    private JTextField delayField;
    private JButton btnPlay;
    private JButton btnStop;
    private JButton btnLoad;
    private JProgressBar progressBar;

    private List<Integer> loadedSequence = new ArrayList<>();
    private volatile boolean isPlaying = false;
    private Thread playerThread;

    public FilePlayerPanel() {
        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Odtwarzacz z pliku"
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Sekcja wyboru pliku
        btnLoad = new JButton("Wczytaj plik");
        selectedFileLabel = new JLabel("Brak pliku");

        btnLoad.addActionListener(e -> chooseFile());

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(btnLoad, gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0; // poprawione pole
        add(selectedFileLabel, gbc);

        JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        configPanel.add(new JLabel("Opóźnienie (ms):"));
        delayField = new JTextField("50", 5);
        configPanel.add(delayField);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        add(configPanel, gbc);

        //
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        statusLabel = new JLabel("Gotowy");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusLabel.setForeground(Color.BLUE); // ujednolicone

        gbc.gridy = 2;
        add(progressBar, gbc);
        gbc.gridy = 3;
        add(statusLabel, gbc);

        //przyciski
        JPanel controlPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPlay = new JButton("START");
        btnPlay.setBackground(new Color(150, 255, 150));
        btnPlay.setEnabled(false);

        btnStop = new JButton("STOP");
        btnStop.setBackground(new Color(255, 150, 150)); // poprawiony kolor
        btnStop.setEnabled(false);

        btnPlay.addActionListener(e -> startPlaying());
        btnStop.addActionListener(e -> stopPlaying());

        controlPanel.add(btnPlay);
        controlPanel.add(btnStop);

        gbc.gridy = 4;
        add(controlPanel, gbc);

        gbc.gridy = 5;
        gbc.weighty = 1.0;
        add(new JPanel(), gbc);
    }

    private void chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Pliki tekstowe i CSV", "txt", "csv");
        fileChooser.setFileFilter(filter); // użycie poprawnej zmiennej

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            loadFile(fileChooser.getSelectedFile());
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
            selectedFileLabel.setText("Plik: " + file.getName() + " [" + loadedSequence.size() + " próbek]");
            btnPlay.setEnabled(!loadedSequence.isEmpty());
            progressBar.setMaximum(Math.max(1, loadedSequence.size()));
            progressBar.setValue(0);
            statusLabel.setText("Plik wczytany poprawnie.");
        } catch (Exception e) {
            statusLabel.setText("Błąd odczytu pliku!");
            e.printStackTrace();
        }
    }


    private void startPlaying(){
        if (loadedSequence.isEmpty() || arduinoService == null) return;
        int delay;
        try {
            delay = Integer.parseInt(delayField.getText().trim());
            if (delay < 5) delay = 5;
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid delay value.");
            delay = 50;
            delayField.setText("50");
        }
        final int finalDelay = delay;

        isPlaying = true;
        btnPlay.setEnabled(false);
        btnStop.setEnabled(true);
        btnLoad.setEnabled(false);
        delayField.setEnabled(false);
        statusLabel.setText("Odtwarzanie...");

        playerThread = new Thread(() -> {
           int index = 0;
           arduinoService.send(ArduinoCommands.STOP);

           for (Integer value : loadedSequence) {
               if (!isPlaying) break;
               arduinoService.send(ArduinoCommands.setDac(String.valueOf(value)));
               index++;
               final int progress = index;
               SwingUtilities.invokeLater(() -> {
                   progressBar.setValue(progress);
                   statusLabel.setText("Wysłano: " + progress + " / " + loadedSequence.size());
               });
               try {
                   Thread.sleep(finalDelay);
               } catch (InterruptedException e) {
                   break;
               }
           }

           isPlaying = false;
           SwingUtilities.invokeLater(() -> {
               statusLabel.setText("Zakończono sekwencję");
                resetControls();
           });
        });
        playerThread.start();

    }
    private void stopPlaying() {
        isPlaying = false;
        if (playerThread != null) {
            playerThread.interrupt();
        }
    }
    private void resetControls() {
        btnPlay.setEnabled(true);
        btnStop.setEnabled(false);
        btnLoad.setEnabled(true);
        delayField.setEnabled(true);
    }

    public void setArduinoService(ArduinoService arduinoService) {
        this.arduinoService = arduinoService;
    }

}
