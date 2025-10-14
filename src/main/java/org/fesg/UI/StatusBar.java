package org.fesg.UI;

import org.fesg.i18n.LanguageManager;
import org.fesg.i18n.TranslationKey;
import org.fesg.service.ArduinoDetector;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

public class StatusBar extends JPanel {

    private final JLabel statusLabel;
    private final JLabel errorLabel;
    private JLabel connectionIcon;

    JPanel contentPanel;

    public StatusBar() {
        setLayout(new BorderLayout());
        setBorder(new BevelBorder(BevelBorder.LOWERED));

        LanguageManager languageManager = LanguageManager.getInstance();
        // Tekst startowy – zostanie nadpisany przez ArduinoDetector przez callback setStatus

        //ikona połączenia z arduino
        connectionIcon = new JLabel("●");
        connectionIcon.setForeground(Color.GRAY);
        connectionIcon.setFont(new Font("SansSerif", Font.BOLD, 14));

        statusLabel = new JLabel("szukanie");

        errorLabel = new JLabel("");
        errorLabel.setForeground(Color.RED);

        contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        contentPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));

        contentPanel.add(connectionIcon);
        contentPanel.add(statusLabel);
        contentPanel.add(errorLabel);
        add(contentPanel, BorderLayout.CENTER);
    }

    public void setStatus(String status) {
        statusLabel.setText(" " + status);
    }

    public void setError(String error) {
        errorLabel.setText(error);
    }
}
