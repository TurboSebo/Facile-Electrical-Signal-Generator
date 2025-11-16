package org.fesg.service;


import com.fazecast.jSerialComm.SerialPort;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.function.Consumer;

import org.fesg.i18n.LanguageManager;
import org.fesg.i18n.TranslationKey;

public class ArduinoVerifier {

    private static final int BAUD_RATE = 9600;
    private static final String IDENTIFY_COMMAND = "*IDN?\n";
    private static final String EXPECTED_RESPONSE_PREFIX = "MY_FESG_ARDUINO";

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

            // Ustawiamy timeouty i strumienie
            commPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1500, 0);

            try (OutputStream output = commPort.getOutputStream();
                 InputStream input = commPort.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {

                // Krok 2: wysłanie komendy identyfikacyjnej
                progress(progressCallBack, TranslationKey.VERIFICATION_STEP_SEND);
                commPort.flushIOBuffers();
                output.write(IDENTIFY_COMMAND.getBytes());
                output.flush();

                // Krok 3: oczekiwanie na identyfikator
                progress(progressCallBack, TranslationKey.VERIFICATION_STEP_WAIT);
                String response = reader.readLine();

                if (response != null && response.startsWith(EXPECTED_RESPONSE_PREFIX)) {
                    // Używamy oryginalnego komunikatu sukcesu z LanguageManager (krok 3/3)
                    progress(progressCallBack, TranslationKey.STATUS_CONNECTED);
                    success = true;
                } else {
                    String msg = MessageFormat.format(
                            LanguageManager.getInstance().getString(TranslationKey.VERIFICATION_ERROR_UNKNOWN_ID),
                            response
                    );
                    System.err.println(msg);
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
