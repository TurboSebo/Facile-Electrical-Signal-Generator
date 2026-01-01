package org.fesg.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageManager {

    private static LanguageManager instance;
    private ResourceBundle resourceBundle;
    private Locale currentLocale;
    private AppLanguage currentLanguage;

    private LanguageManager(AppLanguage language) {
        setLanguage(language);
    }

    public static LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager(AppLanguage.PL); // Domyślny język
        }
        return instance;
    }

    public void setLanguage(AppLanguage language) {
        currentLanguage = language;
        currentLocale = new Locale(language.getLanguageCode(), language.getCountryCode());
        resourceBundle = ResourceBundle.getBundle("i18n.MessagesBundle", currentLocale);
        //resourceBundle = ResourceBundle.getBundle("i18n.MessagesBundle", currentLocale, new UTF8Control());
    }

    public String getString(String key) {
        return resourceBundle.getString(key);
    }

    public AppLanguage getCurrentLanguage() {
        return currentLanguage;
    }
}
