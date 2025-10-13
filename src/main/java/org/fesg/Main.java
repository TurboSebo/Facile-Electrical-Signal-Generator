package org.fesg;

import org.fesg.UI.MainWindow;
import org.fesg.service.ArduinoDetector;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                //e.printStackTrace();
                throw new RuntimeException(e);
            }
            MainWindow mainWindow = new MainWindow();
            mainWindow.setVisible(true);

            // Uruchomienie wątku do wykrywania Arduino
            ArduinoDetector detector = new ArduinoDetector(mainWindow::setStatus);
            Thread detectorThread = new Thread(detector);
            detectorThread.setDaemon(true); // Ustawienie wątku jako daemon, aby zakończył się wraz z aplikacją
            detectorThread.start();
        });
    }
}