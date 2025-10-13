package org.fesg.i18n;

public final class TranslationKey {

    private TranslationKey() {
        // Prywatny konstruktor, aby uniemożliwić tworzenie instancji
    }

    // App
    public static final String APP_TITLE = "app.title";

    // Menu
    public static final String MENU_FILE = "menu.file";
    public static final String MENU_FILE_EXIT = "menu.file.exit";
    public static final String MENU_LANGUAGE = "menu.language";

    // Status Bar
    public static final String STATUS_READY = "status.ready";
    public static final String STATUS_ARDUINO_CONNECTED = "status.arduino.connected";
    public static final String STATUS_ARDUINO_DISCONNECTED = "status.arduino.disconnected";
}
