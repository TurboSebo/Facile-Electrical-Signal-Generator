package org.fesg.UI;

import org.fesg.i18n.LanguageManager;
import org.fesg.i18n.TranslationKey;

import javax.swing.*;
import java.awt.*;

/**
 * Prosty panel placeholder dla zakładki "Odtwarzacz Plików".
 * W przyszłości można tu dodać logikę wczytywania i odtwarzania plików CSV/TXT.
 */
public class FilePlayerPanel extends JPanel {

    private final LanguageManager languageManager = LanguageManager.getInstance();

    public FilePlayerPanel() {
        setLayout(new GridBagLayout());
        add(new JLabel(languageManager.getString(TranslationKey.PANEL_FILE_PLAYER_PLACEHOLDER)));
    }
}
