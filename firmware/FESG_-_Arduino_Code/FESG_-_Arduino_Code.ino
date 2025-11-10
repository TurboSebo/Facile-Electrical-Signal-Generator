// Kod do wgrania na płytkę Arduino

#include <Wire.h>
#include <Adafruit_MCP4725.h>

Adafruit_MCP4725 dac;
String command = ""; // Zmienna przechowująca odebraną komendę
bool commandReady = false; // Flaga informująca o zakończeniu odczytu komendy


const String DEVICE_ID = "MY_FESG_ARDUINO_V1.0"; // UNIKALNY IDENTYFIKATOR URZĄDZENIA

const int VOLTAGE_READ_PIN = A0;

boolean CONNECTED_WITH_PC = false;
void setup() {
  //Ustawienie prędkości transmisji na taką samą jak  w
  Serial.begin(9600);
  Serial.setTimeout(100);
  
    pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, LOW);
  Serial.println("oczekiwanie na PC");
  while(!CONNECTED_WITH_PC){  
       while (Serial.available()){
      char inChar = (char)Serial.read();

     if (inChar == '\n'){ //Znak nowej linii (\n )jako koniec otrzymanej komendy
      commandReady = true;
      } else{
        command += inChar;
      }
    }
    if (commandReady){
        command.trim();     //usunięcie białych znaków
        command.toUpperCase(); //konwersja na wielkie litery
    
        if(command.equals("*IDN?")){
          // Wysłanie unikalnego identyfikatora plus znak nowej linii
          if (!dac.begin(0x60)) {
            Serial.println("Nie znaleziono MCP4725! Zatrzymałem.");
            while (1);
            }
           else{
         // Serial.print("OK_")
          Serial.print(DEVICE_ID);
          Serial.print('\n'); 
          Serial.flush(); // Upewnienie się, że dane zostały wysłane
          CONNECTED_WITH_PC = true;
          digitalWrite(LED_BUILTIN, HIGH);
          command = "";
        commandReady = false;
        break;
           }
        }else{
          Serial.println("Nieznana komenda"); //debug
        }
    
        //Resetowanie zmiennych do obsługi następnej komendy
        command = "";
        commandReady = false;
    } 
   
  }
}

void loop() {
  // 1. Sprawdzenie, czy są dostępne dane

  while (Serial.available()){
    Serial.print("ok");
    char inChar = (char)Serial.read();

    //Znak nowej linii (\n )jako koniec komendy
    if (inChar == '\n'){
      commandReady = true;
    } else{
      command += inChar;
    }
  }

  //2. Przetwarzanie komendy jeśli została odebrana
 
  if (commandReady){
    command.trim();     //usunięcie białych znaków
    command.toUpperCase(); //konwersja na wielkie litery

    if(command.equals("*IDN?")){
      Serial.print("ALREADY_CONNECTED");
      Serial.print('\n'); 
      Serial.flush(); // Upewnienie się, że dane zostały wysłane
    }else{
      Serial.println("Nieznana komenda"); //debug
    }

    //Resetowanie zmiennych do obsługi następnej komendy
    command = "";
    commandReady = false;
    
  }
}
