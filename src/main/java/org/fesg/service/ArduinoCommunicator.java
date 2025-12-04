package org.fesg.service;

import com.fazecast.jSerialComm.SerialPort;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class ArduinoCommunicator {
    private static final int BAUD_RATE = 9600;
    private SerialPort commPort;
    private OutputStream output;
    private BufferedReader reader;
    private final Consumer<String> errorCallback;
    private Consumer<String> dataReceivedCallback;
    private Thread listenerThread;
    private volatile boolean isListening = false;

    public ArduinoCommunicator(Consumer<String> errorCallback) {
        this.errorCallback = errorCallback;
    }

    public void setDataReceivedCallback(Consumer<String> dataReceivedCallback) {
        this.dataReceivedCallback = dataReceivedCallback;
    }

    public synchronized boolean connect(SerialPort port) {
        try {
            if (isConnected()) disconnect();
            this.commPort = port;
            commPort.setBaudRate(BAUD_RATE);

            //  SEMI_BLOCKING ustawiony na 100ms.
            // readLine rzuci wyjątek, jeśli przez 100ms nic nie przyjdzie - to normalne!
            commPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);

            // Ważne: Pobieramy strumienie. Dzięki poprawce w Verifierze, "rura" powinna być drożna.
            InputStream in = commPort.getInputStream();
            this.output = commPort.getOutputStream();
            this.reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.US_ASCII));

            System.out.println("Communicator: Start na porcie " + commPort.getSystemPortName());
            startListening();
            return true;
        } catch (Exception e) {
            errorCallback.accept("Błąd connect(): " + e.getMessage());
            disconnect();
            return false;
        }
    }

    public synchronized void sendData(String data) {
        if (!isConnected()) return;
        try {
            String framed = data.endsWith("\n") ? data : (data + "\n");
            output.write(framed.getBytes(StandardCharsets.US_ASCII));
            output.flush();
            System.out.println("TX >> " + framed.trim());
        } catch (Exception e) {
            handleConnectionError("Błąd wysyłania: " + e.getMessage());
        }
    }

    public synchronized void startListening(){
        if (isListening) return;
        isListening = true;
        listenerThread = new Thread(() -> {
            while (isListening && isConnected()) {
                try {
                    // To zablokuje się na max 100ms.
                    String line = reader.readLine();

                    if (line != null) {
                        line = line.trim();
                        if (!line.isEmpty() && dataReceivedCallback != null) {
                            System.out.println("RX << " + line); // Logujemy sukces
                            dataReceivedCallback.accept(line);
                        }
                    }
                } catch (IOException e) {
                    /* jSerialComm w trybie SEMI_BLOCKING rzuca wyjątek przy timeoucie.
                     Trzeba go złapać i dalej puścić pętle.
                     Nie traktujemy tego jako błędu połączenia!*/
                    String msg = e.getMessage();
                    if (msg != null && msg.toLowerCase().contains("timed out")) {
                        continue; // Po prostu spróbuj czytać jeszcze raz
                    }
                    // Inne błędy IO (np. wyrwanie kabla)
                    if (isListening) handleConnectionError("Błąd IO Listenera: " + e.getMessage());
                } catch (Exception e) {
                    if (isListening) handleConnectionError("Błąd Listenera: " + e.getMessage());
                }
            }
        }, "ArduinoListenerThread");
        listenerThread.start();
    }

    public void disconnect() {
        isListening = false;
        try {
            if (commPort != null) commPort.closePort(); // To zamknie też strumienie
        } catch (Exception e) { /* ignore */ }
        commPort = null;
        reader = null;
        output = null;
    }

    public synchronized boolean isConnected() {
        return commPort != null && commPort.isOpen();
    }

    private void handleConnectionError(String errorMessage) {
        if (!isListening) return;
        System.err.println(errorMessage);
        errorCallback.accept("Rozłączono: " + errorMessage);
        disconnect();
    }
}