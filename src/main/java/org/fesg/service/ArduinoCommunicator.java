package org.fesg.service;

import com.fazecast.jSerialComm.SerialPort;

import java.io.*;
import java.util.function.Consumer;

public class ArduinoCommunicator {
    // Ustawienia portu
    private static final int BAUD_RATE = 9600;

    private SerialPort commPort;
    private OutputStream output;
    private BufferedReader reader;

    //funkcje zwrotne (callbacki)
    private final Consumer<String> errorCallback;
    private Consumer<String> dataReceivedCallback;

    private Thread listenerThread; //wątek nasłuchujący
    private volatile boolean isListening = false; //Flaga sterująca pętlą - volatile, bo może być używane w wielu wątkach

    public ArduinoCommunicator(Consumer<String> errorCallback) {
        this.errorCallback = errorCallback;
    }


    public void setDataReceivedCallback(Consumer<String> dataReceivedCallback) {
        this.dataReceivedCallback = dataReceivedCallback;
    }
    // Przyjmij już zweryfikowany port i skonfiguruj strumienie
    public synchronized boolean connect(SerialPort port) {
        try {
            // Jeśli już coś jest otwarte, zamknij
            disconnect();

            this.commPort = port;
            commPort.setBaudRate(BAUD_RATE);
            // Ważne: TIMEOUT_READ_SEMI_BLOCKING pozwala readLine() czekać na dane, ale nie zawiesza całego programu na zawsze
            commPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);

            InputStream in = commPort.getInputStream();
            this.output = commPort.getOutputStream();
            this.reader = new BufferedReader(new InputStreamReader(in));

            System.out.println("Połączono z Arduino na porcie: " + commPort.getSystemPortName());

            startListening(); // Rozpocznij nasłuchiwanie danych
            return true;
        } catch (Exception e) {
            errorCallback.accept("Connection error: " + e.getMessage());
            disconnect();
            return false;
        }
    }
        // --- NOWA METODA: Wysyłanie ---

    public synchronized void sendData(String data) {
        if (!isConnected()) {
            errorCallback.accept("Cannot send data: Not connected to Arduino.");
        }
        try {
            output.write(data.getBytes());
            output.flush();
            System.out.println("Wysłano dane: " + data);
        } catch (IOException e){
            handleConnectionError("Send data IO error: " + e.getMessage());
        } catch (Exception e) {
            handleConnectionError("Send data error: " + e.getMessage());
        }
    }

    public synchronized void startListening(){
        if (isListening) return; // Już nasłuchuje

        isListening = true;
        listenerThread = new Thread(() -> {
            try {
                String line = reader.readLine();
                if (line != null) {
                    line = line.trim();
                    if (!line.isEmpty() && dataReceivedCallback != null) {
                        dataReceivedCallback.accept(line);
                        System.out.println("Odebrano dane: " + line);
                    }
                }

            } catch (IOException e) {
                if (isListening) {
                    String msg = e.getMessage();
                    // JSerialComm przy TIMEOUT_READ_SEMI_BLOCKING może rzucić IOException z tekstem o timeoucie
                    // To jest normalne przy pierwszym połączeniu, gdy Arduino jeszcze nic nie wysłało,
                    // więc nie traktujemy tego jako utraty połączenia.
                    if (msg != null && msg.toLowerCase().contains("timed out")) {
                        System.out.println("Listener timeout (brak danych) – ignoruję: " + msg);
                    } else {
                        handleConnectionError("Listener IO error: " + msg);
                    }
                }
            } catch (Exception e) {
                handleConnectionError("Listener error: " + e.getMessage());
                if (isListening) {
                    errorCallback.accept("Błąd odczytu " + e.getMessage());
                }
            }
        }, "ArduinoListenerThread");
        listenerThread.start();
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
