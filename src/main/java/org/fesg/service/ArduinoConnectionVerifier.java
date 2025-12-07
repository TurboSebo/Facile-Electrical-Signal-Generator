package org.fesg.service;

import com.fazecast.jSerialComm.SerialPort;
import org.fesg.i18n.LanguageManager;
import org.fesg.i18n.TranslationKey;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.function.Consumer;

public class ArduinoConnectionVerifier {

    private static final int BAUD_RATE = 9600;
    private static final String IDENTIFY_COMMAND = "*IDN?\n";
    private static final String EXPECTED_RESPONSE_PREFIX = "MY_FESG_ARDUINO_V1.1_WAVE";
    private static final int READ_TIMEOUT_MS = 200;
    private static final int TOTAL_RESPONSE_TIMEOUT_MS = 2000;

    public SerialPort verifyAndConnect(String portName, Consumer<String> progressCallBack){
        SerialPort commPort = null;
        boolean success = false;

        // Zmienne strumieni poza blokiem try, abyśmy ich nie zamknęli automatycznie
        InputStream input = null;
        OutputStream output = null;

        try {
            commPort = SerialPort.getCommPort(portName);
            commPort.setBaudRate(BAUD_RATE);

            progress(progressCallBack, TranslationKey.VERIFICATION_STEP_OPEN);
            if(!commPort.openPort(2000)){
                throw new Exception(LanguageManager.getInstance().getString(TranslationKey.VERIFICATION_ERROR_CANNOT_OPEN));
            }

            commPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, READ_TIMEOUT_MS, 0);

            // Otwieramy strumienie RĘCZNIE (bez try-with-resources)
            input = commPort.getInputStream();
            output = commPort.getOutputStream();

            progress(progressCallBack, TranslationKey.VERIFICATION_STEP_SEND);
            commPort.flushIOBuffers();
            output.write(IDENTIFY_COMMAND.getBytes(StandardCharsets.US_ASCII));
            output.flush();

            progress(progressCallBack, TranslationKey.VERIFICATION_STEP_WAIT);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream(128);
            byte[] tmp = new byte[64];
            long deadline = System.currentTimeMillis() + TOTAL_RESPONSE_TIMEOUT_MS;

            while (System.currentTimeMillis() < deadline) {
                int read = input.read(tmp);
                if (read > 0) {
                    buffer.write(tmp, 0, read);
                    byte[] arr = buffer.toByteArray();
                    for (byte b : arr) {
                        if (b == '\n' || b == '\r') {
                            deadline = 0;
                            break;
                        }
                    }
                }
            }

            String response = new String(buffer.toByteArray(), StandardCharsets.US_ASCII).trim();

            if (!response.isEmpty() && response.startsWith(EXPECTED_RESPONSE_PREFIX)) {
                success = true;
                System.out.println("Weryfikacja OK: " + response);
            } else {
                System.err.println("Błąd weryfikacji. Odpowiedź: " + response);
            }

        } catch (Exception e){
            System.err.println("Wyjątek weryfikacji: " + e.getMessage());
            success = false;
        } finally {
            // Zamykamy WSZYSTKO tylko jeśli się NIE udało.
            // Jeśli się udało -> zostawiamy otwarte dla Communicatora!
            if (!success) {
                try {
                    if (output != null) output.close();
                    if (input != null) input.close();
                    if (commPort != null && commPort.isOpen()) commPort.closePort();
                } catch (Exception ex) { /* ignoruj */ }
                commPort = null;
            }
        }

        return success ? commPort : null;
    }

    private static void progress(Consumer<String> progressCallBack, String key) {
        if (progressCallBack != null) {
            progressCallBack.accept(LanguageManager.getInstance().getString(key));
        }
    }
}