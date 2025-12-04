#include <Wire.h>
#include <Adafruit_MCP4725.h>

Adafruit_MCP4725 dac;
String command = ""; 
bool commandReady = false; 

const String DEVICE_ID = "MY_FESG_ARDUINO_V1.0"; 
const int VOLTAGE_READ_PIN = A0;
boolean CONNECTED_WITH_PC = false;

void setup() {
  Serial.begin(9600);
  Serial.setTimeout(100);
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, LOW);
  
  while(!CONNECTED_WITH_PC){  
    while (Serial.available()){
      char inChar = (char)Serial.read();
      if (inChar == '\n'){ commandReady = true; } else { command += inChar; }
    }
    
    if (commandReady){
        command.trim(); command.toUpperCase();
        if(command.equals("*IDN?")){
           if (!dac.begin(0x60)) {} // init DAC
           Serial.print(DEVICE_ID); Serial.print('\n'); Serial.flush(); 
           CONNECTED_WITH_PC = true;
           digitalWrite(LED_BUILTIN, HIGH);
        }
        command = ""; commandReady = false;
    } 
  }
}

void loop() {
  while (Serial.available()){
    char inChar = (char)Serial.read();
    if (inChar == '\n'){ commandReady = true; } else{ command += inChar; }
  }

  if (commandReady){
    command.trim(); command.toUpperCase(); 

    if(command.equals("*IDN?")){
      Serial.println("ALREADY_CONNECTED");
    }
    else if (command.startsWith("DAC:")){
        command.remove(0,4); 
        int dacValue = command.toInt(); 
        if(dacValue < 0) dacValue = 0; if(dacValue > 4095) dacValue = 4095;
        dac.setVoltage(dacValue, false); 
        // Nie odsyłamy nic, albo proste potwierdzenie, jeśli chcesz. 
        // Na razie milczymy, żeby nie psuć konsoli.
    } 
    else if (command.equals("READV?")){
      int sensorValue = analogRead(VOLTAGE_READ_PIN);
      float voltage = sensorValue * (5.0 / 1023.0);
      Serial.println(voltage, 3); // Wysyła TYLKO liczbę i nową linię
    }
    Serial.flush();
    command = ""; commandReady = false;
  }
}
