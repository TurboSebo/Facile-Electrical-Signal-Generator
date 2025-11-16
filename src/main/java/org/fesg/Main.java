package org.fesg;

import org.fesg.UI.MainWindow;
import org.fesg.service.ArduinoCommunicator;
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
            ArduinoCommunicator communicator = new ArduinoCommunicator(mainWindow::setError);

            // Uruchomienie wątku do wykrywania Arduino
            ArduinoDetector detector = new ArduinoDetector(
                    mainWindow::setStatus,
                    mainWindow::setStatusText,
                    mainWindow::setError,
                    communicator
            );
            mainWindow.setArduinoDetector(detector); // Przekazanie detektora do okna
            detector.start();

            mainWindow.setVisible(true);
            detector.start();

            //listener do poprawnego zamykania wątku
            mainWindow.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    detector.stop();
                    communicator.disconnect();
                }
            });
        });
    }
}