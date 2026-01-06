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
    public static final String MENU_HELP = "menu.help";
    public static final String MENU_HELP_ABOUT = "menu.help.about";

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
    public static final String PANEL_MANUAL_LOG_SET_DAC = "panel.manual.log.setDac";
    public static final String PANEL_MANUAL_LOG_READ_VOLTAGE = "panel.manual.log.readVoltage";

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
    public static final String PANEL_GENERATOR_WAVE_TYPE_LABEL = "panel.generator.waveType.label";
    public static final String PANEL_GENERATOR_WAVE_TYPE_SINE = "panel.generator.waveType.sine";
    public static final String PANEL_GENERATOR_WAVE_TYPE_TRIANGLE = "panel.generator.waveType.triangle";

    // File player panel
    public static final String PANEL_FILE_PLAYER_PLACEHOLDER = "panel.filePlayer.placeholder";

    public static final String PANEL_FILE_PLAYER_TITLE = "panel.filePlayer.title";
    public static final String PANEL_FILE_PLAYER_BUTTON_LOAD = "panel.filePlayer.button.load";
    public static final String PANEL_FILE_PLAYER_NO_FILE = "panel.filePlayer.noFile";
    public static final String PANEL_FILE_PLAYER_DELAY_LABEL = "panel.filePlayer.delayLabel";
    public static final String PANEL_FILE_PLAYER_STATUS_READY = "panel.filePlayer.status.ready";
    public static final String PANEL_FILE_PLAYER_BUTTON_PLAY = "panel.filePlayer.button.play";
    public static final String PANEL_FILE_PLAYER_BUTTON_STOP = "panel.filePlayer.button.stop";
    public static final String PANEL_FILE_PLAYER_CHECKBOX_LOOP = "panel.filePlayer.checkbox.loop";
    public static final String PANEL_FILE_PLAYER_LOG_LOOP = "panel.filePlayer.log.loop";
    public static final String PANEL_FILE_PLAYER_LOG_ON = "panel.filePlayer.log.on";
    public static final String PANEL_FILE_PLAYER_LOG_OFF = "panel.filePlayer.log.off";
    public static final String PANEL_FILE_PLAYER_FILTER_DESC = "panel.filePlayer.filter.desc";
    public static final String PANEL_FILE_PLAYER_STATUS_CANCELLED = "panel.filePlayer.status.cancelled";
    public static final String PANEL_FILE_PLAYER_FILE_LABEL_PREFIX = "panel.filePlayer.fileLabel.prefix";
    public static final String PANEL_FILE_PLAYER_FILE_LABEL_SUFFIX = "panel.filePlayer.fileLabel.suffix";
    public static final String PANEL_FILE_PLAYER_STATUS_LOADED = "panel.filePlayer.status.loaded";
    public static final String PANEL_FILE_PLAYER_STATUS_ERROR_READ = "panel.filePlayer.status.errorRead";
    public static final String PANEL_FILE_PLAYER_STATUS_NO_CONNECTION = "panel.filePlayer.status.noConnection";
    public static final String PANEL_FILE_PLAYER_LOG_NO_CONNECTION = "panel.filePlayer.log.noConnection";
    public static final String PANEL_FILE_PLAYER_STATUS_INVALID_DELAY = "panel.filePlayer.status.invalidDelay";
    public static final String PANEL_FILE_PLAYER_STATUS_PLAYING = "panel.filePlayer.status.playing";
    public static final String PANEL_FILE_PLAYER_LOG_START_PREFIX = "panel.filePlayer.log.start.prefix";
    public static final String PANEL_FILE_PLAYER_LOG_SAMPLES = "panel.filePlayer.log.samples";
    public static final String PANEL_FILE_PLAYER_LOG_MS_SUFFIX = "panel.filePlayer.log.msSuffix";
    public static final String PANEL_FILE_PLAYER_STATUS_PLAYING_LOOP = "panel.filePlayer.status.playingLoop";
    public static final String PANEL_FILE_PLAYER_STATUS_PLAYING_LOOP_SUFFIX = "panel.filePlayer.status.playingLoopSuffix";
    public static final String PANEL_FILE_PLAYER_STATUS_SENT = "panel.filePlayer.status.sent";
    public static final String PANEL_FILE_PLAYER_LOG_SENT_PREFIX = "panel.filePlayer.log.sentPrefix";
    public static final String PANEL_FILE_PLAYER_LOG_SENT_SUFFIX = "panel.filePlayer.log.sentSuffix";
    public static final String PANEL_FILE_PLAYER_STATUS_FINISHED = "panel.filePlayer.status.finished";
    public static final String PANEL_FILE_PLAYER_LOG_FINISHED_PREFIX = "panel.filePlayer.log.finishedPrefix";
    public static final String PANEL_FILE_PLAYER_LOG_TOTAL_SUFFIX = "panel.filePlayer.log.totalSuffix";
    public static final String PANEL_FILE_PLAYER_STATUS_STOPPED = "panel.filePlayer.status.stopped";
    public static final String PANEL_FILE_PLAYER_LOG_STOPPED_PREFIX = "panel.filePlayer.log.stoppedPrefix";

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

    // Generator Panel logs and errors
    public static final String PANEL_GENERATOR_LOG_SET_FREQ = "panel.generator.log.setFreq";
    public static final String PANEL_GENERATOR_LOG_WAVE_SINE = "panel.generator.log.waveSine";
    public static final String PANEL_GENERATOR_LOG_WAVE_TRIANGLE = "panel.generator.log.waveTriangle";
    public static final String PANEL_GENERATOR_LOG_START_CONTINUOUS = "panel.generator.log.startContinuous";
    public static final String PANEL_GENERATOR_LOG_STOP = "panel.generator.log.stop";
    public static final String PANEL_GENERATOR_LOG_ONCE = "panel.generator.log.once";
    public static final String PANEL_GENERATOR_LOG_BURST_PREFIX = "panel.generator.log.burstPrefix";
    public static final String PANEL_GENERATOR_LOG_BURST_SUFFIX = "panel.generator.log.burstSuffix";

    public static final String PANEL_GENERATOR_ERROR_INVALID_FLOAT = "panel.generator.error.invalidFloat";
    public static final String PANEL_GENERATOR_ERROR_INVALID_INT = "panel.generator.error.invalidInt";

    public static final String ERROR_TITLE = "error.title";
}
