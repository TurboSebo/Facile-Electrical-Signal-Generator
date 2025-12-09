package org.fesg.service;

/**
 *  klasa z komendami protokołu do komunikacji z Arduino..
 */
public final class ArduinoCommands {

    private ArduinoCommands() {
        // utility class
    }

    // Proste komendy bez parametrów
    public static final String READ_VOLTAGE = "READV?";
    public static final String START = "START";
    public static final String STOP = "STOP";
    public static final String ONCE = "ONCE";

    // Komendy parametryzowane
    public static String setDac(String value) {
        return "DAC:" + value;
    }

    public static String setFrequency(float frequency) {
        return "FREQ " + frequency;
    }

    public static String burst(int count) {
        return "BURST " + count;
    }
}

