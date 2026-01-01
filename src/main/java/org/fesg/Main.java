package org.fesg;

import org.fesg.UI.MainWindow;
import org.fesg.i18n.AppLanguage;
import org.fesg.i18n.LanguageManager;
import org.fesg.service.ArduinoCommunicator;
import org.fesg.service.ArduinoService;
import org.fesg.service.ConfigManager;

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

            ConfigManager config = ConfigManager.getInstance();
            AppLanguage savedLanguage = config.getLanguage();
            LanguageManager.getInstance().setLanguage(savedLanguage);

            MainWindow mainWindow = new MainWindow();
            ArduinoCommunicator communicator = new ArduinoCommunicator(mainWindow::setError);

            ArduinoService arduinoService = new ArduinoService(
                    mainWindow::setStatus,
                    mainWindow::setStatusText,
                    mainWindow::setError,
                    communicator
            );
            mainWindow.setArduinoService(arduinoService);
            mainWindow.setVisible(true);


            mainWindow.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    communicator.disconnect();
                }
            });
        });
    }
}