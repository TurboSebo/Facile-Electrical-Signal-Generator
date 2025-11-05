package org.fesg.service;


import com.fazecast.jSerialComm.SerialPort;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.function.Consumer;

import org.fesg.i18n.LanguageManager;
import org.fesg.i18n.TranslationKey;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ArduinoVerifier {

    private static final int BAUD_RATE = 9600;
    private static final String IDENTIFY_COMMAND = "*IDN?\n";
    private static final String EXPECTED_RESPONSE_PREFIX = "MY_FESG_ARDUINO";

    public boolean verifyConnection(String portName, Consumer<String> progressCallBack){
        SerialPort commPort = null;
        boolean success = false;

        try{
            //1. Otwarcie portu
            commPort = SerialPort.getCommPort(portName);
            commPort.setBaudRate(BAUD_RATE);

            progressCallBack.accept(LanguageManager.getInstance().getString(TranslationKey.VERIFICATION_STEP_OPEN));


            if(!commPort.openPort(2000)){
                throw new Exception(LanguageManager.getInstance().getString(TranslationKey.VERIFICATION_ERROR_CANNOT_OPEN));
            }

            commPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1500, 0);

            OutputStream output = commPort.getOutputStream();
            InputStream input = commPort.getInputStream();


            BufferedReader reader = new BufferedReader(new InputStreamReader(input)); // Utwórz "czytnik", który potrafi czytać całe linie


            // 2. Wysyłanie komendy indetyfikacyjnej
            progressCallBack.accept(LanguageManager.getInstance().getString(TranslationKey.VERIFICATION_STEP_SEND));
            commPort.flushIOBuffers();
            output.write(IDENTIFY_COMMAND.getBytes());
            output.flush();

            progressCallBack.accept(LanguageManager.getInstance().getString(TranslationKey.VERIFICATION_STEP_WAIT)); // 3. Odbiór i weryfikacja odpowiedzi

            // Użycie readLine() zamiast input.read(readBuffer) dzięki czemu zaczeka na całą linię zakończoną znakiem '\n' lub na timeout
            String response = reader.readLine();

            if (response != null) {
                response = response.trim(); // readLine() nie zawiera '\n', ale trim() nie zaszkodzi
                System.out.println("ARDUINO RESPONSE: " + response);

                if (response.startsWith(EXPECTED_RESPONSE_PREFIX)) {  // odpowiedź zawiera prefiks - SUKCES !
                    success = true;
                } else {
                    System.err.println(MessageFormat.format(LanguageManager.getInstance().getString(TranslationKey.VERIFICATION_ERROR_UNKNOWN_ID), response));
                }
            } else {
                // readLine() zwrócił null, co oznacza timeout
                System.err.println(LanguageManager.getInstance().getString(TranslationKey.VERIFICATION_ERROR_NO_RESPONSE));
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

}
