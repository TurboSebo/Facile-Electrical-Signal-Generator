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
    public static final String MENU_LANGUAGE_PL = "menu.language.pl";
    public static final String MENU_LANGUAGE_EN = "menu.language.en";
    public static final String MENU_TOOLS = "menu.tools";
    public static final String MENU_TOOLS_AUTOSEARCH = "menu.tools.autosearch";
    public static final String MENU_TOOLS_SELECT_PORT = "menu.tools.selectPort";
    public static final String MENU_TOOLS_NO_PORTS = "menu.tools.noPorts";
    public static final String MENU_TOOLS_CLEAR_CONSOLE = "menu.tools.clearConsole";

    // Tabs
    public static final String TAB_MANUAL_CONTROL = "tab.manualControl";
    public static final String TAB_WAVE_GENERATOR = "tab.waveGenerator";
    public static final String TAB_FILE_PLAYER = "tab.filePlayer";

    // Manual panel
    public static final String PANEL_MANUAL_TITLE = "panel.manual.title";
    public static final String PANEL_MANUAL_DAC_TITLE = "panel.manual.dac.title";
    public static final String PANEL_MANUAL_DAC_LEVEL_LABEL = "panel.manual.dac.levelLabel";
    public static final String PANEL_MANUAL_BUTTON_SET_OUTPUT = "panel.manual.button.setOutput";
    public static final String PANEL_MANUAL_ANALOG_INPUT_TITLE = "panel.manual.analogInput.title";
    public static final String PANEL_MANUAL_BUTTON_READ_VOLTAGE = "panel.manual.button.readVoltage";

    // Generator panel
    public static final String PANEL_GENERATOR_CONFIG_TITLE = "panel.generator.config.title";
    public static final String PANEL_GENERATOR_FREQUENCY_LABEL = "panel.generator.frequency.label";
    public static final String PANEL_GENERATOR_BUTTON_SET_FREQ = "panel.generator.button.setFreq";
    public static final String PANEL_GENERATOR_CONTINUOUS_TITLE = "panel.generator.continuous.title";
    public static final String PANEL_GENERATOR_BUTTON_START_CONTINUOUS = "panel.generator.button.startContinuous";
    public static final String PANEL_GENERATOR_BUTTON_STOP = "panel.generator.button.stop";
    public static final String PANEL_GENERATOR_BUTTON_ONCE = "panel.generator.button.once";
    public static final String PANEL_GENERATOR_BURST_TITLE = "panel.generator.burst.title";
    public static final String PANEL_GENERATOR_BURST_COUNT_LABEL = "panel.generator.burst.countLabel";
    public static final String PANEL_GENERATOR_BUTTON_BURST = "panel.generator.button.burst";

    // File player panel
    public static final String PANEL_FILE_PLAYER_PLACEHOLDER = "panel.filePlayer.placeholder";

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
