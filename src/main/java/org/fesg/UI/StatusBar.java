package org.fesg.UI;

import org.fesg.i18n.LanguageManager;
import org.fesg.i18n.TranslationKey;
import org.fesg.service.ConnectionState;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

public class StatusBar extends JPanel {

    private final JLabel statusLabel;
    private final JLabel errorLabel;
    private final JLabel connectionIcon;
    private final LanguageManager languageManager;

    JPanel contentPanel;

    private String detectedPort = "";

    public StatusBar() {
        this.languageManager = LanguageManager.getInstance();
        setLayout(new BorderLayout());
        setBorder(new BevelBorder(BevelBorder.LOWERED));

        //ikona połączenia z arduino
        connectionIcon = new JLabel("●");
        connectionIcon.setForeground(Color.GRAY);
        connectionIcon.setFont(new Font("SansSerif", Font.BOLD, 14));

        statusLabel = new JLabel(languageManager.getString(TranslationKey.STATUS_DISCONNECTED));

        errorLabel = new JLabel("");
        errorLabel.setForeground(Color.RED);

        contentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        contentPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));

        contentPanel.add(connectionIcon);
        contentPanel.add(statusLabel);
        contentPanel.add(errorLabel);
        add(contentPanel, BorderLayout.CENTER);
    }

    public void setStatus(ConnectionState connectionState) {
        String statusText;
        Color iconColor;

        switch (connectionState) {
            case SEARCHING:
                statusText = languageManager.getString(TranslationKey.STATUS_SEARCHING);
                iconColor = Color.GRAY;
                break;
            case FOUND:
                statusText = languageManager.getString(TranslationKey.STATUS_FOUND);
                iconColor = Color.ORANGE;
                break;
            case VERIFYING:
                statusText = languageManager.getString(TranslationKey.STATUS_VERIFYING);
                iconColor = Color.ORANGE;
                break;
            case CONNECTED:
                statusText = languageManager.getString(TranslationKey.STATUS_CONNECTED ) + " (" + detectedPort + ")";
                iconColor = new Color(0, 180, 0); // Zielony
                break;
            case DISCONNECTED:
                statusText = languageManager.getString(TranslationKey.STATUS_DISCONNECTED);
                iconColor = Color.GRAY;
                break;
            case ERROR:
                statusText = languageManager.getString(TranslationKey.STATUS_ERROR);
                iconColor = Color.RED;
                break;
            default:
                statusText = languageManager.getString(TranslationKey.STATUS_UNKNOWN);
                iconColor = Color.GRAY;
                break;
        }

        statusLabel.setText(statusText);
        connectionIcon.setForeground(iconColor);
    }

    public void setStatus() {
        statusLabel.setText(languageManager.getString(TranslationKey.STATUS_UNKNOWN));
        connectionIcon.setForeground(Color.GRAY);
    }
    public void setError(String error) {
        errorLabel.setText(error);
    }

    public void setDetectedPort(String detectedPort) {
        this.detectedPort = detectedPort != null ? detectedPort : "";

        // Jeśli pasek statusu aktualnie pokazuje stan połączenia, zaktualizuj wyświetlany port
        SwingUtilities.invokeLater(() -> {
            String connectedPrefix = languageManager.getString(TranslationKey.STATUS_CONNECTED);
            String current = statusLabel.getText();
            if (current != null && current.startsWith(connectedPrefix)) {
                statusLabel.setText(connectedPrefix + " (" + this.detectedPort + ")");
            }
        });
    }

    public void setStatusText(String text) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(text));
    }

}
