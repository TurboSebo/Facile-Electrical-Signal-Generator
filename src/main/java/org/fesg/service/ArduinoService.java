package org.fesg.service;

import com.fazecast.jSerialComm.SerialPort;
import javax.swing.*;
import java.util.function.Consumer;

public class ArduinoService {

    private final Consumer<ConnectionState> statusUpdater;
    private final Consumer<String> statusTextUpdater;
    private final Consumer<String> errorMessageUpdater;
    private final ArduinoCommunicator communicator;

    private volatile ConnectionState currentState = ConnectionState.DISCONNECTED;
    private volatile String detectedPort = "";
    private volatile String lastErrorMessage = "";

    public ArduinoService(Consumer<ConnectionState> statusUpdater,
                          Consumer<String> statusTextUpdater,
                          Consumer<String> errorMessageUpdater,
                          ArduinoCommunicator communicator) {
        this.statusUpdater = statusUpdater;
        this.statusTextUpdater = statusTextUpdater;
        this.errorMessageUpdater = errorMessageUpdater;
        this.communicator = communicator;
    }

    // --- Publiczne API dla UI ---

    public SerialPort[] getAvailablePorts() {
        return SerialPort.getCommPorts();
    }

    public synchronized void connectToPort(SerialPort port) {
        communicator.disconnect();
        System.out.println("Ręczna próba połączenia z portem: " + port.getSystemPortName());
        this.detectedPort = port.getSystemPortName();
        updateState(ConnectionState.FOUND, "");
        attemptConnection();
    }

    public synchronized void disconnect() {
        communicator.disconnect();
        updateState(ConnectionState.DISCONNECTED, "");
        System.out.println("Ręcznie rozłączono.");
    }


    /**
     * Wysyła komendę do Arduino (np. "DAC:2000" lub "READV?")
     */
    public void send(String command) {
        if (currentState == ConnectionState.CONNECTED) {
            communicator.sendData(command);
        } else {
            errorMessageUpdater.accept("Brak połączenia. Nie można wysłać komendy.");
        }
    }

    /**
     * Rejestruje funkcję, która zostanie wywołana, gdy Arduino coś odeśle.
     * Używane przez MainWindow do aktualizacji wyświetlacza napięcia/konsoli.
     */
    public void setMessageListener(Consumer<String> listener) {
        communicator.setDataReceivedCallback(listener);
    }

    // --- Logika wewnętrzna ---

    private void attemptConnection() {
        new Thread(() -> {
            SerialPort verifiedPort = null;
            try {
                updateState(ConnectionState.VERIFYING, "");
                ArduinoConnectionVerifier verifier = new ArduinoConnectionVerifier();

                verifiedPort = verifier.verifyAndConnect(
                        this.detectedPort,
                        (progressText) -> SwingUtilities.invokeLater(
                                () -> statusTextUpdater.accept(progressText)
                        )
                );

                if (verifiedPort != null) {
                    boolean commStarted = communicator.connect(verifiedPort);
                    if (commStarted) {
                        updateState(ConnectionState.CONNECTED, "");
                    } else {
                        handleError("Błąd: Nie można uruchomić Communicatora.");
                        verifiedPort.closePort();
                    }
                } else {
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

    private void handleError(String errorMessage) {
        updateState(ConnectionState.ERROR, errorMessage);
    }

    void updateState(ConnectionState newState, String errorMessage) {
        currentState = newState;
        lastErrorMessage = errorMessage;

        SwingUtilities.invokeLater(() -> {
            statusUpdater.accept(newState);
            if (newState == ConnectionState.ERROR && errorMessage != null && !errorMessage.isBlank()) {
                errorMessageUpdater.accept(errorMessage);
            } else {
                errorMessageUpdater.accept("");
            }
        });
    }

    public ConnectionState getCurrentState() { return currentState; }
    public String getDetectedPort() { return detectedPort; }
    public String getLastErrorMessage() { return lastErrorMessage; }
}