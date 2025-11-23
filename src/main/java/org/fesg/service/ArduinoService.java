package org.fesg.service;

import com.fazecast.jSerialComm.SerialPort;
import javax.swing.*;
import java.util.function.Consumer;

// Lepsza nazwa to np. "ConnectionManager", ale na razie trzymajmy się starej
public class ArduinoService {

    // Te pola są niezbędne do komunikacji z UI i logiką
    private final Consumer<ConnectionState> statusUpdater;
    private final Consumer<String> statusTextUpdater;
    private final Consumer<String> errorMessageUpdater;
    private final ArduinoCommunicator communicator;

    // Te pola przechowują stan
    private volatile ConnectionState currentState = ConnectionState.DISCONNECTED; // Zaczynamy jako rozłączeni
    private volatile String detectedPort = "";
    private volatile String lastErrorMessage = "";

    // Konstruktor (bez zmian)
    public ArduinoService(Consumer<ConnectionState> statusUpdater,
                          Consumer<String> statusTextUpdater,
                          Consumer<String> errorMessageUpdater,
                          ArduinoCommunicator communicator) {
        this.statusUpdater = statusUpdater;
        this.statusTextUpdater = statusTextUpdater;
        this.errorMessageUpdater = errorMessageUpdater;
        this.communicator = communicator;
    }

    // --- Publiczne API dla UI---

    /**
     * Zwraca listę wszystkich dostępnych portów szeregowych.
     * Wywoływane przez MainWindow, gdy użytkownik klika menu "Wybierz port".
     */
    public SerialPort[] getAvailablePorts() {
        return SerialPort.getCommPorts();
    }

    /**
     * Główna metoda wywoływana przez UI, gdy użytkownik wybierze port z listy.
     */
    public synchronized void connectToPort(SerialPort port) {
        communicator.disconnect();// Rozłącz obecne połączenie, zanim spróbujesz nowego

        System.out.println("Ręczna próba połączenia z portem: " + port.getSystemPortName());
        this.detectedPort = port.getSystemPortName();

        updateState(ConnectionState.FOUND, ""); // Ustawiamy stan na "Found", aby UI wiedziało, że coś się dzieje
        attemptConnection();      // Uruchom weryfikację i połączenie w osobnym wątku

    }

    /**
     * Wywoływane, gdy użytkownik chce się ręcznie rozłączyć
     * (np. zamykając aplikację lub klikając przycisk "Rozłącz").
     */
    public synchronized void disconnect() {
        communicator.disconnect();
        updateState(ConnectionState.DISCONNECTED, "");
        System.out.println("Ręcznie rozłączono.");
    }

    // --- Logika wewnętrzna ---

    /**
     * Uruchamia proces weryfikacji i połączenia w osobnym wątku,
     * aby nie blokować interfejsu użytkownika.
     */
    private void attemptConnection() {
        // Uruchamiamy weryfikację w osobnym, krótkotrwałym wątku
        new Thread(() -> {
            SerialPort verifiedPort = null;
            try {
                // 1. Ustaw stan na VERIFYING
                updateState(ConnectionState.VERIFYING, "");
                ArduinoConnectionVerifier verifier = new ArduinoConnectionVerifier();

                // 2. Uruchom weryfikację
                verifiedPort = verifier.verifyAndConnect(
                        this.detectedPort,
                        (progressText) -> SwingUtilities.invokeLater(
                                () -> statusTextUpdater.accept(progressText)
                        )
                );

                // 3. Reakcja na wynik
                if (verifiedPort != null) {
                    // SUKCES! Przekaż port do Communicatora
                    //verifiedPort = SerialPort.getCommPort(this.detectedPort);
                    boolean commStarted = communicator.connect(verifiedPort);
                    if (commStarted) {
                        updateState(ConnectionState.CONNECTED, "");
                    } else {
                        handleError("Błąd: Nie można uruchomić Communicatora.");
                        verifiedPort.closePort(); // Posprzątaj
                    }
                } else {
                    // Weryfikacja nie powiodła się
                    handleError("Urządzenie nie jest naszym Arduino lub błąd komunikacji.");
                }

            } catch (Exception e) {
                handleError("Krytyczny błąd weryfikacji: " + e.getMessage());
                if (verifiedPort != null && verifiedPort.isOpen()) {
                    verifiedPort.closePort();
                }
            }
        }, "arduino-verify-connect").start();
    }

    // --- Metody Pomocnicze (bez zmian) ---

    private void handleError(String errorMessage) {
        updateState(ConnectionState.ERROR, errorMessage);
    }

    void updateState(ConnectionState newState, String errorMessage) {
        currentState = newState;
        lastErrorMessage = errorMessage;

        SwingUtilities.invokeLater(() -> {
            statusUpdater.accept(newState);
            if (newState == ConnectionState.ERROR) {
                errorMessageUpdater.accept(errorMessage);
            } else {
                errorMessageUpdater.accept("");
            }
        });

        System.out.println("Status zmieniony: " + newState
                + (detectedPort.isEmpty() ? "" : " [Port: " + detectedPort + "]"));
    }

    // --- Gettery (bez zmian) ---
    public ConnectionState getCurrentState() { return currentState; }
    public String getDetectedPort() { return detectedPort; }
    public String getLastErrorMessage() { return lastErrorMessage; }
}