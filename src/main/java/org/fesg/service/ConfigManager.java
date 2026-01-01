package org.fesg.service;

import org.fesg.i18n.AppLanguage;

import java.io.*;
import java.util.Properties;

public class ConfigManager {

    private static ConfigManager instance;
    private final Properties properties;
    private static final String CONFIG_FILE = "config.properties";

    private static final String KEY_LANGUAGE = "app.language";

    private ConfigManager() {
        properties = new Properties();
        loadConfig();
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private void loadConfig() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (InputStream input = new FileInputStream(file)) {
                properties.load(input);
                System.out.println("Konfiguracja wczytana z: " + file.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Nie udało się wczytać pliku konfiguracji: " + e.getMessage());
            }
        } else {
            System.out.println("Brak pliku konfiguracji. Używam ustawień domyślnych.");
        }
    }

    public void saveConfig() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            properties.store(output, "FESG Configuration File");
            System.out.println("Konfiguracja zapisana.");
        } catch (IOException e) {
            System.err.println("Błąd zapisu konfiguracji: " + e.getMessage());
        }
    }

    public AppLanguage getLanguage() {
        String langCode = properties.getProperty(KEY_LANGUAGE, "pl");
        return AppLanguage.fromCode(langCode);
    }

    public void setLanguage(AppLanguage language) {
        if (language == null) {
            return;
        }
        properties.setProperty(KEY_LANGUAGE, language.getLanguageCode());
        saveConfig();
    }
}

