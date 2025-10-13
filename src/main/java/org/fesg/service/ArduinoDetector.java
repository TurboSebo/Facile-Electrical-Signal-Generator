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
    private volatile String lastErrorMessage = "";

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
        // Celowo nieskończona pętla: wątek działa w tle jako daemon i kończy się wraz z aplikacją.
        updateState(ConnectionState.SEARCHING, "");

        while (!Thread.currentThread().isInterrupted()) {
            // 1) Pobierz dostępne porty COM/TTY z jSerialComm
            SerialPort[] ports = SerialPort.getCommPorts();

            // 2) Heurystyka wykrywania: sprawdzamy opis/systemową nazwę portu.
            //    Uwaga: to proste podejście – na różnych OS opis/systemowa nazwa może się różnić.
            //    Regex obejmuje m.in. Linux (ttyUSB/ttyACM) oraz macOS (cu.usbmodem...).
            boolean found = false;
            for (SerialPort port : ports) {
                String description = port.getPortDescription().toLowerCase();
                String systemName = port.getSystemPortName().toLowerCase();

                if (description.contains("arduino") ||
                        systemName.contains("arduino")||
                        systemName.matches("(ttyUSB|ttyACM|cu\\.usbmodem).*")
                ) {
                    found = true;
                    break;
                }
            }

            // 3) Reaguj tylko na zmianę stanu, aby nie spamować UI tym samym komunikatem
            if (found) {
                // Znaleziono urządzenie. Jeśli byliśmy w stanie wyszukiwania,
                // przejdź do stanu FOUND i rozpocznij weryfikację.
                if (currentState == ConnectionState.SEARCHING) {
                    updateState(ConnectionState.FOUND, "");
                    verifyArduinoProgram(); // Uruchamia weryfikację w tle (patrz opis w metodzie)
                }
                // Jeśli stan to już FOUND lub CONNECTED, nie robimy nic, czekamy.
            } else {
                // Nie znaleziono urządzenia. Jeśli poprzednio było znalezione lub połączone,
                // wróć do stanu wyszukiwania.
                if (currentState != ConnectionState.SEARCHING) {
                    updateState(ConnectionState.SEARCHING, "");
                }
            }

            try {
                // 4) Odczekaj 2 sekundy do kolejnego skanowania (odciążenie CPU/portów)
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // Jeśli ktoś przerwał wątek, zakończ działanie
                Thread.currentThread().interrupt();
                break; // Wyjdź z pętli while
            }
            System.out.println("Status: "+statusUpdater.getClass().getName() +" "+ isArduinoConnected());

        }
    }

    /**
     * Pseudoweryfikacja programu na Arduino.
     * Aktualnie: odczekuje 3 sekundy i jeśli nadal jesteśmy w stanie FOUND, przełącza na CONNECTED.
     * Docelowo: można tu otworzyć port szeregowy, wysłać "ping"/handshake i sprawdzić odpowiedź.
     * Uwaga na warunki wyścigu – stan może się zmienić w trakcie opóźnienia.
     */
    private void verifyArduinoProgram(){
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                if (currentState == ConnectionState.FOUND) {
                    updateState(ConnectionState.CONNECTED, "");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Centralne miejsce do zmiany stanu i notyfikacji UI.
     * - Aktualizuje pola stanu/błędu.
     * - Przekazuje nowy stan do UI na EDT przez SwingUtilities.invokeLater (bezpieczeństwo wątkowe).
     * Kontrakt: statusUpdater przyjmuje String z nazwą enum-a (np. "CONNECTED").
     */
    private void updateState(ConnectionState newState, String errorMessage) {
        currentState = newState;
        lastErrorMessage = errorMessage;

        SwingUtilities.invokeLater(() -> {
            statusUpdater.accept(String.valueOf(newState));
            if (newState == ConnectionState.ERROR && errorMessage != null) {
                errorMessageUpdater.accept(errorMessage);
            }
        });

    }

    /**
     * Umożliwia zewnętrzne wymuszenie stanu (np. z poziomu UI lub innego serwisu).
     * Uwaga: omija logikę przejść w tej klasie – używać rozważnie, aby nie wprowadzić niespójności.
     */
    public static void setConnectionState(ConnectionState newState) {
        currentState = newState;
    }
}
