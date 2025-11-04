package org.fesg.service;

import com.fazecast.jSerialComm.SerialPort;

import javax.swing.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.concurrent.CopyOnWriteArrayList;

public class ArduinoDetector implements Runnable {

    private final Consumer<ConnectionState> statusUpdater;
    private final Consumer<String> errorMessageUpdater;

    private volatile boolean autosearch = false;
    public boolean isAutosearch() {
        return autosearch;
    }

    public SerialPort[] getAvailablePorts() { // Zwraca listę wszystkich dostępnych portów szeregowych
        return SerialPort.getCommPorts();
    }
    public void setAutosearch(boolean autosearch) {
        this.autosearch = autosearch;
        notifyAutosearchChanged();
    }
    public void toggleAutosearch() {
        this.autosearch = !this.autosearch;
        notifyAutosearchChanged();
       // if (this.autosearch) {new Thread(this).start();  } // zakomentowane ze względu, że uruchamiało kolejny, ten sam wątek
    }

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scanTask;

    // przeniesiono stan do pól instancji (usunięto static)
    private volatile boolean arduinoConnected = false;
    private volatile ConnectionState currentState = ConnectionState.SEARCHING;
    volatile String lastErrorMessage = "";
    volatile String detectedPort = "";

    // --- Słuchacze zmian autosearch ---
    private final CopyOnWriteArrayList<Consumer<Boolean>> autosearchListeners = new CopyOnWriteArrayList<>();
    public void addAutosearchListener(Consumer<Boolean> listener) {
        if (listener != null) autosearchListeners.add(listener);
    }
    public void removeAutosearchListener(Consumer<Boolean> listener) {
        autosearchListeners.remove(listener);
    }
    private void notifyAutosearchChanged() {
        final boolean value = autosearch;
        // Zapewniamy wykonanie na EDT, bo słuchacz zwykle dotyka UI
        SwingUtilities.invokeLater(() -> autosearchListeners.forEach(l -> l.accept(value)));
    }

    public ArduinoDetector(Consumer<ConnectionState> statusUpdater, Consumer<String> errorMessageUpdater) {
        this.statusUpdater = statusUpdater;
        this.errorMessageUpdater = errorMessageUpdater;
    }

    // usunięto static
    public boolean isArduinoConnected() {
        return arduinoConnected;
    }

    public Consumer<ConnectionState> getStatusUpdater() {
        return statusUpdater;
    }

    @Override
    public void run() {
        start();
    }

    public synchronized void start() {
        if (scheduler == null || scheduler.isShutdown() || scheduler.isTerminated()) {
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "arduino-detector");
                t.setDaemon(true);
                return t;
            });
        }
        if (scanTask == null || scanTask.isCancelled()) {
            updateState(ConnectionState.SEARCHING, "");
            //autosearch = true; //zakomentowane, gdyż przes to zawsze przy uruchamianiu wyszukiwał
            scanTask = scheduler.scheduleAtFixedRate(this::scanTick, 0, 2, TimeUnit.SECONDS);
        }
    }

    public synchronized void stop() {
        autosearch = false;
        notifyAutosearchChanged();
        if (scanTask != null) {
            scanTask.cancel(true);
            scanTask = null;
        }
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    private void scanTick() {
        if (!autosearch) {
            return;
        }
        try {
            SerialPort[] ports = SerialPort.getCommPorts();
            boolean found = scanForArduino(ports);
            handleArduinoState(found);
        } catch (Exception e) {
            handleError("Błąd skanowania portów: " + e.getMessage());
        }
    }

    boolean scanForArduino(SerialPort[] ports) {
        System.out.println("=== SKANOWANIE PORTÓW ===");
        for (SerialPort port : ports) {
            String description = port.getPortDescription().toLowerCase();
            String systemName = port.getSystemPortName().toLowerCase();

            System.out.println("Port: " + systemName + " - " + description);

            if (systemName.contains("arduino")
                    || description.contains("arduino")
                    || description.contains("ch340")
                    //|| description.contains("serial port")
                    || description.contains("usb serial")) {
                detectedPort = systemName;
                System.out.println("Znaleziono arduino na porcie " + detectedPort);
                return true;
            }
        }
        detectedPort = "";
        return false;
    }

    void handleArduinoState(boolean found) {
        if (found) {
            if (currentState == ConnectionState.SEARCHING || currentState == ConnectionState.ERROR) {
                updateState(ConnectionState.FOUND, "");
                startVerification();
            }
        } else {
            if (currentState == ConnectionState.FOUND || currentState == ConnectionState.CONNECTED) {
                updateState(ConnectionState.SEARCHING, "Arduino odłączone");
            }
        }
    }

    private void handleError(String errorMessage) {
        // Bez usypiania, aby nie blokować planera
        updateState(ConnectionState.ERROR, errorMessage);
    }

    private void startVerification() {
        new Thread(() -> {
            try {
                for (int i = 1; i <= 3; i++) {
                    if (currentState != ConnectionState.FOUND) {
                        return;
                    }
                    // Aktualizacja stanu zamiast tekstu
                    SwingUtilities.invokeLater(() -> statusUpdater.accept(ConnectionState.VERIFYING));
                    Thread.sleep(1000);
                }
                if (currentState == ConnectionState.FOUND) {
                    updateState(ConnectionState.CONNECTED, "");
                    arduinoConnected = true;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "arduino-verify").start();
    }

    void updateState(ConnectionState newState, String errorMessage) {
        ConnectionState oldState = currentState;
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

        System.out.println("Status zmieniony: " + oldState + " -> " + newState
                + (detectedPort.isEmpty() ? "" : " [Port: " + detectedPort + "]"));
    }

    public synchronized void forceConnect(SerialPort port) {
        // zatrzymywanie automatycznego skanowania, bo przechodzimy w tryb ręczny
        stop();
        this.autosearch = false;
        notifyAutosearchChanged();
        System.out.println("Trying to manually connect to arduino with port: " + port.getSystemPortName());
        this.detectedPort = port.getSystemPortName();
        updateState(ConnectionState.FOUND, "");

         startVerification();



    }

    public ConnectionState getCurrentState() {return currentState;}

    public String getDetectedPort() {return detectedPort;}

    public String getLastErrorMessage() {return lastErrorMessage;}

    public void setConnectionState(ConnectionState newState) {
        currentState = newState;
    }
}
