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
    public static final String MENU_TOOLS_AUTOSEARCH = "menu.tools.autosearch";
    public static final String MENU_TOOLS_SELECT_PORT = "menu.tools.selectPort";
    public static final String MENU_TOOLS_NO_PORTS = "menu.tools.noPorts";

    // Status Bar
    // Statusy połączenia
    public static final String STATUS_SEARCHING = "status.searching";
    public static final String STATUS_FOUND = "status.found";
    public static final String STATUS_ERROR = "status.error";
    public static final String STATUS_CONNECTED = "status.connected";
    public static final String STATUS_VERIFYING = "status.verifying";
    public static final String STATUS_DISCONNECTED = "status.disconnected";
    public static final String STATUS_UNKNOWN = "status.unknown";

    // Verification / Weryfikacja
    public static final String VERIFICATION_STEP_OPEN = "verification.step.open";
    public static final String VERIFICATION_STEP_SEND = "verification.step.send";
    public static final String VERIFICATION_STEP_WAIT = "verification.step.wait";

    public static final String VERIFICATION_ERROR_CANNOT_OPEN = "verification.error.cannot_open";
    public static final String VERIFICATION_ERROR_UNKNOWN_ID = "verification.error.unknown_id";
    public static final String VERIFICATION_ERROR_NO_RESPONSE = "verification.error.no_response";
    public static final String VERIFICATION_ERROR_GENERIC = "verification.error.generic";

    public static final String VERIFICATION_INFO_PORT_CLOSED = "verification.info.port_closed";
}
