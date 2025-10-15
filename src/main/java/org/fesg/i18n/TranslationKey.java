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
    public static final String MENU_TOOLS = "menu.tools";

    // Status Bar
    // Statusy połączenia
    public static final String STATUS_SEARCHING = "status.searching";
    public static final String STATUS_FOUND = "status.found";
    public static final String STATUS_ERROR = "status.error";
    public static final String STATUS_CONNECTED = "status.connected";
    public static final String STATUS_VERIFYING = "status.verifying";
}
