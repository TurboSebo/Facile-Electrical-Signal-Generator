package org.fesg.service;

import com.fazecast.jSerialComm.SerialPort;
import org.fesg.i18n.LanguageManager;
import org.fesg.i18n.TranslationKey;

import java.util.function.Consumer;

/**
 * Wątek odpowiedzialny za okresowe wykrywanie podłączonego Arduino
 * i informowanie warstwy UI o zmianie statusu połączenia.
 *
 * Zasada działania:
 * - Co 2 sekundy pobiera listę dostępnych portów szeregowych.
 * - Szuka portu, którego opis zawiera słowo "arduino" (prosta heurystyka).
 * - Gdy wykryje zmianę stanu (połączono/rozłączono), publikuje odpowiedni komunikat przez {@code statusUpdater}.
 */
public class ArduinoDetector implements Runnable {

    /**
     * Funkcja typu callback, którą wywołujemy, aby zaktualizować status w UI (np. pasek stanu).
     * Powinna być bezpieczna wątkowo po stronie UI (zwykle wewnątrz niej wołamy SwingUtilities.invokeLater).
     */
    private final Consumer<String> statusUpdater;

    /** Zapewnia dostęp do tłumaczeń (i18n) wyświetlanych komunikatów. */
    private final LanguageManager languageManager;

    /**
     * Zapamiętany poprzedni stan, aby nie emitować tego samego komunikatu wielokrotnie
     * przy kolejnych iteracjach pętli skanowania.
     */
    private static volatile boolean arduinoConnected = false;

    /**
     * @param statusUpdater funkcja aktualizująca status w interfejsie użytkownika
     */
    public ArduinoDetector(Consumer<String> statusUpdater) {
        this.statusUpdater = statusUpdater;
        this.languageManager = LanguageManager.getInstance();
    }

    /**
     * Zwraca ostatnio znany stan podłączenia Arduino.
     */
    public static boolean isArduinoConnected() {
        return arduinoConnected;
    }

    /**
     * Ekspozycja callbacku statusu (np. do testów/diagnostyki).
     */
    public Consumer<String> getStatusUpdater() {
        return statusUpdater;
    }

    @Override
    public void run() {
        // Celowo nieskończona pętla: wątek działa w tle jako daemon i kończy się wraz z aplikacją.
        //noinspection InfiniteLoopStatement
        while (true) {
            SerialPort[] ports = SerialPort.getCommPorts();  // 1) Pobierz dostępne porty COM/TTY z jSerialComm

            // 2) Prosta metoda wykrywania: opis portu zawiera słowo "arduino"
            boolean found = false;
            for (SerialPort port : ports) {
                if (port.getPortDescription().toLowerCase().contains("arduino")) {
                    found = true;
                    break;
                }
            }

            // 3) Reaguj tylko na zmianę stanu, aby nie spamować UI tym samym komunikatem
            if (found && !arduinoConnected) {
                arduinoConnected = true;
                statusUpdater.accept(languageManager.getString(TranslationKey.STATUS_ARDUINO_CONNECTED));
            } else if (!found && arduinoConnected) {
                arduinoConnected = false;
                statusUpdater.accept(languageManager.getString(TranslationKey.STATUS_ARDUINO_DISCONNECTED));
            }

            try {
                Thread.sleep(2000); // 4) Odczekaj 2 sekundy do kolejnego skanowania (odciążenie CPU/portów)
            } catch (InterruptedException e) {
                // Jeśli ktoś przerwał wątek, zakończ działanie
                Thread.currentThread().interrupt();
                break;
            }
            System.out.println("Status: "+statusUpdater.getClass().getName() +" "+ isArduinoConnected());
        }
    }
}
