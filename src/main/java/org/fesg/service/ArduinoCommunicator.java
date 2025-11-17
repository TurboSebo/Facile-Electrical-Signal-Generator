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

    // Przyjmij już zweryfikowany port i skonfiguruj strumienie
    public synchronized boolean connect(SerialPort port) {
        try {
            // Jeśli już coś jest otwarte, zamknij
            disconnect();

            this.commPort = port;
            commPort.setBaudRate(BAUD_RATE);

            InputStream in = commPort.getInputStream();
            this.output = commPort.getOutputStream();
            this.reader = new BufferedReader(new InputStreamReader(in));

            System.out.println("Połączono z Arduino na porcie: " + commPort.getSystemPortName());
            return true;
        } catch (Exception e) {
            errorCallback.accept("Connection error: " + e.getMessage());
            disconnect();
            return false;
        }
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
