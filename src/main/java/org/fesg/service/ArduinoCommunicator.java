package org.fesg.service;

import com.fazecast.jSerialComm.SerialPort;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.function.Consumer;

public class ArduinoCommunicator {
    // Ustawienia portu
    private static final int BAUD_RATE = 9600;

    private SerialPort commPort;
    private OutputStream output;
    private BufferedReader reader;
    private final Consumer<String> errorCallback;

    public ArduinoCommunicator(Consumer<String> errorCallback) {
        this.errorCallback = errorCallback;
    }

    public void disconnect() {
        try {
            if (output != null) output.close();
            if (reader != null) reader.close();
            if (commPort != null && commPort.isOpen()) {
                commPort.closePort();
                System.out.println("Port " + commPort.getSystemPortName() + " zamknięty.");
            }
        } catch (Exception e) {
            errorCallback.accept("Disconnection error: " + e.getMessage());
        }
        commPort = null;
        reader = null;
        output = null;
    }

    public synchronized boolean isConnected() {
        return commPort != null && commPort.isOpen();
    }

    private void handleConnectionError(String errorMessage) {
        System.err.println(errorMessage);
        errorCallback.accept("Utracono połączenie z Arduino.");
        disconnect(); // Zamknij port po błędzie
    }
}
