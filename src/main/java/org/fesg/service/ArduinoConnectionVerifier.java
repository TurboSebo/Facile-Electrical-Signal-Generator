package org.fesg.service;


import com.fazecast.jSerialComm.SerialPort;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.function.Consumer;

import org.fesg.i18n.LanguageManager;
import org.fesg.i18n.TranslationKey;

public class ArduinoConnectionVerifier {

    private static final int BAUD_RATE = 9600;
    private static final String IDENTIFY_COMMAND = "*IDN?\n";
    private static final String EXPECTED_RESPONSE_PREFIX = "MY_FESG_ARDUINO";

    // Czas oczekiwania na pojedynczy odczyt i całkowity limit odpowiedzi
    private static final int READ_TIMEOUT_MS = 200;
    private static final int TOTAL_RESPONSE_TIMEOUT_MS = 2000;

    public boolean verifyConnection(String portName, Consumer<String> progressCallBack){
        SerialPort commPort = null;
        boolean success = false;

        try{
            commPort = SerialPort.getCommPort(portName);
            commPort.setBaudRate(BAUD_RATE);

            // Krok 1: otwarcie portu
            progress(progressCallBack, TranslationKey.VERIFICATION_STEP_OPEN);
            if(!commPort.openPort(2000)){
                throw new Exception(LanguageManager.getInstance().getString(TranslationKey.VERIFICATION_ERROR_CANNOT_OPEN));
            }

            // Semi-blocking: czekaj maksymalnie READ_TIMEOUT_MS na min. 1 bajt
            commPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, READ_TIMEOUT_MS, 0);

            try (OutputStream output = commPort.getOutputStream();
                 InputStream input = commPort.getInputStream()) {

                // Krok 2: wysłanie komendy identyfikacyjnej
                progress(progressCallBack, TranslationKey.VERIFICATION_STEP_SEND);
                commPort.flushIOBuffers();
                output.write(IDENTIFY_COMMAND.getBytes(StandardCharsets.US_ASCII));
                output.flush();

                // Krok 3: odbiór i weryfikacja odpowiedzi
                progress(progressCallBack, TranslationKey.VERIFICATION_STEP_WAIT);

                ByteArrayOutputStream buffer = new ByteArrayOutputStream(128);
                byte[] tmp = new byte[64];
                long deadline = System.currentTimeMillis() + TOTAL_RESPONSE_TIMEOUT_MS;

                while (System.currentTimeMillis() < deadline) {
                    int read = input.read(tmp); // z semi-blocking, czeka do READ_TIMEOUT_MS
                    if (read > 0) {
                        buffer.write(tmp, 0, read);
                        // sprawdź koniec linii
                        byte[] arr = buffer.toByteArray();
                        for (byte b : arr) {
                            if (b == '\n' || b == '\r') {
                                deadline = 0; // wymuś wyjście z pętli
                                break;
                            }
                        }
                    }
                }

                String response = new String(buffer.toByteArray(), StandardCharsets.US_ASCII).trim();

                if (!response.isEmpty()) {
                    System.out.println("ARDUINO RESPONSE: " + response);

                    if (response.startsWith(EXPECTED_RESPONSE_PREFIX)) {  // odpowiedź zawiera prefiks - SUKCES !
                        success = true;
                    } else {
                        System.err.println(MessageFormat.format(
                                LanguageManager.getInstance().getString(TranslationKey.VERIFICATION_ERROR_UNKNOWN_ID),
                                response));
                    }
                } else {
                    System.err.println(LanguageManager.getInstance().getString(TranslationKey.VERIFICATION_ERROR_NO_RESPONSE));
                }
            }

        } catch (Exception e){
            System.err.println(MessageFormat.format(LanguageManager.getInstance().getString(TranslationKey.VERIFICATION_ERROR_GENERIC), e.getMessage()));
        }finally {
            if(commPort != null && commPort.isOpen()){
                commPort.closePort();
                System.out.println(MessageFormat.format(LanguageManager.getInstance().getString(TranslationKey.VERIFICATION_INFO_PORT_CLOSED), portName));
            }
        }

        return success;
    }

    private static void progress(Consumer<String> progressCallBack, String key) {
        if (progressCallBack != null) {
            progressCallBack.accept(LanguageManager.getInstance().getString(key));
        }
    }
}
