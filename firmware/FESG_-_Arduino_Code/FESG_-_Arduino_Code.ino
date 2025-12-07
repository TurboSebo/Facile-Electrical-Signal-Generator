#include <Wire.h>
#include <Adafruit_MCP4725.h>

Adafruit_MCP4725 dac;

// --- DANE KONFIGURACYJNE URZĄDZENIA ---
const String DEVICE_ID = "MY_FESG_ARDUINO_V1.1_WAVE"; 
const int VOLTAGE_READ_PIN = A0;
boolean CONNECTED_WITH_PC = false;

// --- ZMIENNE DO KOMUNIKACJI SERIAL ---
String command = ""; 
bool commandReady = false; 

// --- DANE FALI (Tablica 512 próbek) ---
#define ILOSC_PROBEK 512 
const PROGMEM uint16_t DACLookup_FullSine[ILOSC_PROBEK] = { 
2047, 2072, 2097, 2122, 2147, 2173, 2198, 2223, 2248, 2273, 2298, 2323, 2347, 2372,
2397, 2422, 2446, 2471, 2496, 2520, 2545, 2569, 2593, 2617, 2641, 2665, 2689, 2713, 2737, 2760, 2784, 2807, 2831, 2854, 2877, 2900, 2922, 2945,
2968, 2990, 3012, 3034, 3056, 3078, 3100, 3121, 3142, 3164, 3185, 3205, 3226, 3246, 3267, 3287, 3307, 3326, 3346, 3365, 3384, 3403,3422, 3441,
3459, 3477, 3495, 3512, 3530, 3547, 3564, 3581, 3597, 3614, 3630, 3646, 3661, 3676, 3692, 3706, 3721, 3735,3749, 3763, 3777, 3790, 3803, 3816, 
3829, 3841, 3853, 3864, 3876, 3887, 3898, 3909, 3919, 3929, 3939, 3948, 3957, 3966,3975, 3983, 3991, 3999, 4006, 4013, 4020, 4027, 4033, 4039, 
4045, 4050, 4055, 4060, 4064, 4068, 4072, 4076, 4079, 4082,4085, 4087, 4089, 4091, 4092, 4093, 4094, 4094, 4095, 4094, 4094, 4093, 4092, 4091, 
4089, 4087, 4085, 4082, 4079, 4076,4072, 4068, 4064, 4060, 4055, 4050, 4045, 4039, 4033, 4027, 4020, 4013, 4006, 3999, 3991, 3983, 3975, 3966, 3957,
3948,3939, 3929, 3919, 3909, 3898, 3887, 3876, 3864, 3853, 3841, 3829, 3816, 3803, 3790, 3777, 3763, 3749, 3735, 3721, 3706,3692, 3676, 3661, 3646,
3630, 3614, 3597, 3581, 3564, 3547, 3530, 3512, 3495, 3477, 3459, 3441, 3422, 3403, 3384, 3365,3346, 3326, 3307, 3287, 3267, 3246, 3226, 3205, 3185, 
3164, 3142, 3121, 3100, 3078, 3056, 3034, 3012, 2990, 2968, 2945, 2922, 2900, 2877, 2854, 2831, 2807, 2784, 2760, 2737, 2713, 2689, 2665, 2641, 2617, 
2593, 2569, 2545, 2520, 2496, 2471,2446, 2422, 2397, 2372, 2347, 2323, 2298, 2273, 2248, 2223, 2198, 2173, 2147, 2122, 2097, 2072, 2047, 2022, 1997, 1972,
1947, 1921, 1896, 1871, 1846, 1821, 1796, 1771, 1747, 1722, 1697, 1672, 1648, 1623, 1598, 1574, 1549, 1525, 1501, 1477,1453, 1429, 1405, 1381, 1357, 1334, 
1310, 1287, 1263, 1240, 1217, 1194, 1172, 1149, 1126, 1104, 1082, 1060, 1038, 1016,994, 973, 952, 930, 909, 889, 868, 848, 827, 807, 787, 768, 748, 729, 710, 
691, 672, 653, 635, 617, 599, 582, 564, 547,530, 513, 497, 480, 464, 448, 433, 418, 402, 388, 373, 359, 345, 331, 317, 304, 291, 278, 265, 253, 241, 230, 218, 
207,196, 185, 175, 165, 155, 146, 137, 128, 119, 111, 103, 95, 88, 81, 74, 67, 61, 55, 49, 44, 39, 34, 30, 26, 22, 18, 15, 12, 9, 7, 5, 3, 2, 1, 0, 0, 0, 0, 0, 
1, 2, 3, 5, 7, 9, 12, 15, 18, 22, 26, 30, 34, 39, 44, 49, 55, 61, 67, 74, 81, 88, 95, 103, 111, 119, 128, 137, 146, 155, 165, 175, 185, 196, 207, 218, 230, 241, 253, 
265, 278, 291, 304, 317, 331, 345, 359, 373, 388, 402, 418, 433, 448, 464, 480, 497, 513, 530, 547, 564, 582, 599, 617, 635, 653, 672, 691, 710, 729, 748, 768, 787, 807, 
827, 848, 868, 889, 909, 930, 952, 973, 994, 1016, 1038, 1060, 1082, 1104, 1126, 1149, 1172, 1194, 1217, 1240, 1263, 1287, 1310, 1334, 1357, 1381, 1405,
1429, 1453, 1477, 1501, 1525, 1549, 1574, 1598, 1623, 1648, 1672, 1697, 1722, 1747, 1771, 1796, 1821, 1846, 1871, 1896, 1921, 1947, 1972, 1997, 2022 };

// --- ZMIENNE GENERATORA FALI ---
unsigned long delayMicrosecs = 1000;
float targetFrequency = 1.0; 
unsigned long lastUpdate = 0;
uint16_t waveIndex = 0;
int burstCount = 0;

// Definicja trybów pracy generatora
enum Mode { STOPPED, SINGLE, BURST, CONTINUOUS };
Mode currentMode = STOPPED;

void setup() {
  Serial.begin(9600); // Zostawiamy Twoje 9600
  Serial.setTimeout(100);
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, LOW);
  
  // Twoja pętla oczekiwania na połączenie
  while(!CONNECTED_WITH_PC){  
    while (Serial.available()){
      char inChar = (char)Serial.read();
      if (inChar == '\n'){ commandReady = true; } else { command += inChar; }
    }
    
    if (commandReady){
        command.trim(); command.toUpperCase();
        if(command.equals("*IDN?")){
           if (!dac.begin(0x60)) {
             // Ewentualna obsługa błędu DAC
           } 
           Serial.print(DEVICE_ID); Serial.print('\n'); Serial.flush(); 
           CONNECTED_WITH_PC = true;
           digitalWrite(LED_BUILTIN, HIGH);
           recalculateDelay(); // Przeliczamy czas na start
        }
        command = ""; commandReady = false;
    } 
  }
}

void loop() {
  // 1. ODCZYT DANYCH Z PORTU (Bez zmian, Twoja metoda)
  while (Serial.available()){
    char inChar = (char)Serial.read();
    if (inChar == '\n'){ commandReady = true; } else{ command += inChar; }
  }

  // 2. OBSŁUGA KOMEND (Rozszerzona o nowe funkcje)
  if (commandReady){
    command.trim(); command.toUpperCase(); 

    if(command.equals("*IDN?")){
      Serial.println("ALREADY_CONNECTED");
    }
    else if (command.startsWith("DAC:")){
        // Jeśli użytkownik ustawia napięcie ręcznie, ZATRZYMUJEMY falę
        currentMode = STOPPED; 
        
        command.remove(0,4); 
        int dacValue = command.toInt(); 
        if(dacValue < 0) dacValue = 0; if(dacValue > 4095) dacValue = 4095;
        dac.setVoltage(dacValue, false); 
    } 
    else if (command.equals("READV?")){
      int sensorValue = analogRead(VOLTAGE_READ_PIN);
      float voltage = sensorValue * (5.0 / 1023.0);
      Serial.println(voltage, 3); 
    }
    // --- NOWE KOMENDY ---
    else if (command.equals("START")) {
      currentMode = CONTINUOUS;
      waveIndex = 0;
    }
    else if (command.equals("STOP")) {
      currentMode = STOPPED;
    }
    else if (command.equals("ONCE")) {
      currentMode = SINGLE;
      waveIndex = 0;
    }
    else if (command.startsWith("BURST")) {
      // Oczekujemy formatu "BURST 5"
      int spaceIndex = command.indexOf(' ');
      if (spaceIndex > 0) {
        burstCount = command.substring(spaceIndex + 1).toInt();
        if (burstCount > 0) {
          currentMode = BURST;
          waveIndex = 0;
        }
      }
    }
    else if (command.startsWith("FREQ")) {
      // Oczekujemy formatu "FREQ 2.5"
      int spaceIndex = command.indexOf(' ');
      if (spaceIndex > 0) {
        float newFreq = command.substring(spaceIndex + 1).toFloat();
        if (newFreq > 0) {
          targetFrequency = newFreq;
          recalculateDelay();
        }
      }
    }
    
    command = ""; commandReady = false;
  }

  // 3. GENERATOR FALI (Działa w tle, nie blokuje pętli)
  if (currentMode != STOPPED) {
    unsigned long currentMicros = micros();

    // Sprawdź czy minął czas na kolejną próbkę
    if (currentMicros - lastUpdate >= delayMicrosecs) {
      lastUpdate = currentMicros;

      uint16_t value = pgm_read_word(&(DACLookup_FullSine[waveIndex]));
      dac.setVoltage(value, false);

      waveIndex++;

      // Jeśli koniec tablicy...
      if (waveIndex >= ILOSC_PROBEK) {
        waveIndex = 0; // Pętla od nowa
        handleEndOfCycle();
      }
    }
  }
}

// --- FUNKCJE POMOCNICZE ---

void handleEndOfCycle() {
  if (currentMode == SINGLE) {
    currentMode = STOPPED;
    // Opcjonalnie: można tu wysłać komunikat do PC, że skończono
  } 
  else if (currentMode == BURST) {
    burstCount--;
    if (burstCount <= 0) {
      currentMode = STOPPED;
    }
  }
}

void recalculateDelay() {
  if (targetFrequency <= 0) targetFrequency = 1.0;
  delayMicrosecs = 1000000UL / (targetFrequency * ILOSC_PROBEK);
}