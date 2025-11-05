// Kod do wgrania na płytkę Arduino

// Zmienna przechowująca odebraną komendę
String command = "";
// Flaga informująca o zakończeniu odczytu komendy
bool commandReady = false;

// UNIKALNY IDENTYFIKATOR URZĄDZENIA
const String DEVICE_ID = "MY_FESG_ARDUINO_V1.0";

void setup() {
  //Ustawienie prędkości transmisji na taką samą jak  w
  Serial.begin(9600);
  Serial.setTimeout(100);
}

void loop() {
  // 1. Sprawdzenie, czy są dostępne dane

  while (Serial.available()){
    // Odczyt jednego bajtu

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
      // Wysłanie unikalnego identyfikatora plus znak nowej linii
      Serial.print(DEVICE_ID);
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
