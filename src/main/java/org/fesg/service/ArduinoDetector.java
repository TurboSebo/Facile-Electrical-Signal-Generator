package org.fesg.service;

import com.fazecast.jSerialComm.SerialPort;
import org.fesg.i18n.LanguageManager;
import org.fesg.i18n.TranslationKey;

import javax.swing.*;
import java.util.function.Consumer;

/**
 * Wątek odpowiedzialny za okresowe wykrywanie podłączonego Arduino
 * i informowanie warstwy UI o zmianie statusu połączenia.
 *
 * Zasada działania:
 * - Co 2 sekundy pobiera listę dostępnych portów szeregowych.
 * - Szuka portu, którego opis zawiera słowo "arduino" (prosta heurystyka).
 * - Gdy wykryje zmianę stanu (połączono/rozłączono), publikuje odpowiedni komunikat przez {@code statusUpdater}.
 *
 * Maszyna stanów (ConnectionState):
 * SEARCHING -> FOUND -> CONNECTED, oraz powrót do SEARCHING przy utracie urządzenia.
 * W razie błędu możliwe ustawienie ERROR wraz z komunikatem dla UI.
 */
public class ArduinoDetector implements Runnable {

    /**
     * Funkcja typu callback, którą wywołujemy, aby zaktualizować status w UI (np. pasek stanu).
     * Powinna być bezpieczna wątkowo po stronie UI (zwykle wewnątrz niej wołamy SwingUtilities.invokeLater).
     */
    private final Consumer<String> statusUpdater;

    /**
     * Callback do wyświetlania komunikatu błędu w UI (wykorzystywany, gdy stan to ERROR).
     */
    private Consumer<String> errorMessageUpdater;

    /**
     * Zapewnia dostęp do tłumaczeń (i18n) wyświetlanych komunikatów.
     */
    private final LanguageManager languageManager;


    boolean autosearch = true;

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


    /**
     * Flaga połączenia – obecnie nie jest aktualizowana w kodzie (wartość pozostaje domyślna).
     * Pozostawiona na przyszłość: można ją zsynchronizować z przejściami stanów CONNECTED/SEARCHING.
     */
    private static volatile boolean arduinoConnected = false;

    /**
     * Aktualny stan połączenia. Volatile, aby inne wątki widziały zmiany natychmiast.
     * To pole jest fundamentem prostej maszyny stanów sterującej komunikatami dla UI.
     */
    private static volatile ConnectionState currentState = ConnectionState.SEARCHING;

    /**
     * Ostatni komunikat błędu (opcjonalny), prezentowany w UI gdy stan = ERROR.
     */

    static volatile String lastErrorMessage = "";

    static volatile String detectedPort = ""; // Dodajemy do śledzenia portu

    /**
     * @param statusUpdater funkcja aktualizująca status w interfejsie użytkownika
     * @param errorMessageUpdater funkcja do prezentacji błędu w UI przy stanie ERROR
     */
    public ArduinoDetector(Consumer<String> statusUpdater, Consumer<String> errorMessageUpdater) {
        this.statusUpdater = statusUpdater;
        this.languageManager = LanguageManager.getInstance();
        this.errorMessageUpdater = errorMessageUpdater;
    }


    /**
     * Zwraca flagę połączenia (aktualnie nieużywana/nieaktualizowana; pozostawiona dla kompatybilności).
     */
    public static boolean isArduinoConnected() {
        return arduinoConnected;
    }


    public Consumer<String> getStatusUpdater() {
        return statusUpdater;
    }

    @Override
    public void run() {
        updateState(ConnectionState.SEARCHING, "");

        while (!Thread.currentThread().isInterrupted() && autosearch) {

            try {
                SerialPort[] ports = SerialPort.getCommPorts();
                boolean found = scanForArduino(ports);

                handleArduinoState(found);
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                handleError("Błąd skanowania portów: " + e.getMessage());
            }
        }
    }

    boolean scanForArduino(SerialPort[] ports) {
        System.out.println("=== SKANOWANIE PORTÓW ===");
        for (SerialPort port : ports) {
            String description = port.getPortDescription().toLowerCase();
            String systemName = port.getSystemPortName().toLowerCase();

            System.out.println("Port: " + systemName + " - " + description);

            if (systemName.contains("arduino")||
                    description.contains("arduino")||
                    description.contains("ch340")||
                    //description.contains("serial port") ||
                    description.contains("\"usb serial")
            ) {
                detectedPort = systemName;
                System.out.println("Znaleziono arduino na porcie " + detectedPort);
                return true;
            }
        }
        detectedPort = "";
        return false;
    }

    void handleArduinoState(boolean found){
        if (found) {
            if (currentState == ConnectionState.SEARCHING || currentState == ConnectionState.ERROR) {
                updateState(ConnectionState.FOUND, "");
                startVerification();
            }
        }
        else {
            if (currentState == ConnectionState.FOUND  || currentState == ConnectionState.CONNECTED) {
                updateState(ConnectionState.SEARCHING, "Arduino odłączone");
            }
        }
    }

    private void handleError(String errorMessage) {
        updateState(ConnectionState.ERROR, errorMessage);
        /*
        try {
            Thread.sleep(5000); // Dłuższe oczekiwanie przy błędzie
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        */
    }

    private void startVerification() {
        new Thread(() -> {
            try {
                // Weryfikacja w 3 krokach z aktualizacjami
                for (int i = 1; i <= 3; i++) {
                    if (currentState != ConnectionState.FOUND) {
                        return; // Przerwano weryfikację
                    }

                    // Aktualizuj status co sekundę
                    final int step = i;
                    SwingUtilities.invokeLater(() ->
                            statusUpdater.accept("Arduino znalezione - weryfikacja (" + step + "/3)...")
                    );

                    Thread.sleep(1000);
                }

                // Tylko jeśli nadal jesteśmy w stanie FOUND
                if (currentState == ConnectionState.FOUND) {
                    updateState(ConnectionState.CONNECTED, "");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } ,"arduino-verify").start();
    }

    void updateState(ConnectionState newState, String errorMessage) {
        ConnectionState oldState = currentState;
        currentState = newState;
        lastErrorMessage = errorMessage;

        SwingUtilities.invokeLater(() ->{
            String statusText = getStatusText(newState);
            statusUpdater.accept(statusText);

            //if (newState == ConnectionState.ERROR && errorMessage != null && !errorMessage.isEmpty()) {
            if (newState == ConnectionState.ERROR ) {
                errorMessageUpdater.accept(errorMessage);
            } else {
                errorMessageUpdater.accept(""); // czyszczenie błędów
            }

        });

        System.out.println("Status zmieniony: " + oldState + " -> " + newState + (detectedPort.isEmpty() ? "" : " [Port: " + detectedPort + "]"));

    }

    String getStatusText(ConnectionState state) {
        switch (state){
            case SEARCHING: return "szukanie Arduino...";
            case FOUND: return "Arduino znalezione - weryfikacja...";
            case CONNECTED: return "Arduino gotowe! [Port: " + detectedPort + "]";
            case ERROR: return "Błąd - sprawdź połączenie";
            default: return "Nieznany status";
        }
    }

    public static ConnectionState getCurrentState() {
        return currentState;
    }

    public static String getDetectedPort() {
        return detectedPort;
    }

    public static String getLastErrorMessage() {
        return lastErrorMessage;
    }

    /**
     * Umożliwia zewnętrzne wymuszenie stanu (np. z poziomu UI lub innego serwisu).
     * Uwaga: omija logikę przejść w tej klasie – używać rozważnie, aby nie wprowadzić niespójności.
     */
    public static void setConnectionState(ConnectionState newState) {
        currentState = newState;

    }
}
