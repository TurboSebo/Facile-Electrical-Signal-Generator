package org.fesg.service;

import com.fazecast.jSerialComm.SerialPort;
import org.fesg.i18n.LanguageManager;

import javax.swing.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class ArduinoDetector implements Runnable {

    private final Consumer<ConnectionState> statusUpdater;
    private Consumer<String> errorMessageUpdater;
    private final LanguageManager languageManager;

    private volatile boolean autosearch = true;
    public boolean isAutosearch() {
        return autosearch;
    }

    public void setAutosearch(boolean autosearch) {
        this.autosearch = autosearch;
    }
    public void toogleAutosearch() {
        this.autosearch = !this.autosearch;
        if (this.autosearch) {
            new Thread(this).start();        }
    }

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scanTask;

    private static volatile boolean arduinoConnected = false;
    private static volatile ConnectionState currentState = ConnectionState.SEARCHING;
    static volatile String lastErrorMessage = "";
    static volatile String detectedPort = "";

    public ArduinoDetector(Consumer<ConnectionState> statusUpdater, Consumer<String> errorMessageUpdater) {
        this.statusUpdater = statusUpdater;
        this.languageManager = LanguageManager.getInstance();
        this.errorMessageUpdater = errorMessageUpdater;
    }

    public static boolean isArduinoConnected() {
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
            autosearch = true;
            scanTask = scheduler.scheduleAtFixedRate(this::scanTick, 0, 2, TimeUnit.SECONDS);
        }
    }

    public synchronized void stop() {
        autosearch = false;
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
                    final int step = i;
                    // Aktualizacja stanu zamiast tekstu
                    SwingUtilities.invokeLater(() -> statusUpdater.accept(ConnectionState.VERIFYING));
                    Thread.sleep(1000);
                }
                if (currentState == ConnectionState.FOUND) {
                    updateState(ConnectionState.CONNECTED, "");
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

  /*
    String getStatusText(ConnectionState state) {
        switch (state) {
            case SEARCHING: return "szukanie Arduino...";
            case FOUND: return "Arduino znalezione - weryfikacja...";
            case CONNECTED: return "Arduino gotowe! [Port: " + detectedPort + "]";
            case ERROR: return "Błąd - sprawdź połączenie";
            default: return "Nieznany status";
        }
    }
*/
    public static ConnectionState getCurrentState() {return currentState;}

    public static String getDetectedPort() {return detectedPort;}

    public static String getLastErrorMessage() {return lastErrorMessage;}

    public static void setConnectionState(ConnectionState newState) {
        currentState = newState;
    }
}
