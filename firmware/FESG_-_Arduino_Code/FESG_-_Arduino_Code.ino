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

// --- DANE FALI ---
#define ILOSC_PROBEK 512

// ... pod tablicą DACLookup_FullSine ...

const PROGMEM uint16_t DACLookup_Triangle[ILOSC_PROBEK] = {
0, 16, 32, 48, 64, 80, 96, 112, 128, 144, 160, 176, 192, 208, 224, 240, 256, 272, 288, 304, 320, 336, 352, 368, 384, 400, 416, 432, 448, 464, 480, 496,
512, 528, 544, 560, 576, 592, 608, 624, 640, 656, 672, 688, 704, 720, 736, 752, 768, 784, 800, 816, 832, 848, 864, 880, 896, 912, 928, 944, 960, 976,
992, 1008, 1024, 1040, 1056, 1072, 1088, 1104, 1120, 1136, 1152, 1168, 1184, 1200, 1216, 1232, 1248, 1264, 1280, 1296, 1312, 1328, 1344, 1360, 1376, 1392, 1408, 1424, 1440, 1456,
1472, 1488, 1504, 1520, 1536, 1552, 1568, 1584, 1600, 1616, 1632, 1648, 1664, 1680, 1696, 1712, 1728, 1744, 1760, 1776, 1792, 1808, 1824, 1840, 1856, 1872, 1888, 1904, 1920, 1936,
1952, 1968, 1984, 2000, 2016, 2032, 2048, 2064, 2080, 2096, 2112, 2128, 2144, 2160, 2176, 2192, 2208, 2224, 2240, 2256, 2272, 2288, 2304, 2320, 2336, 2352, 2368, 2384, 2400, 2416,
2432, 2448, 2464, 2480, 2496, 2512, 2528, 2544, 2560, 2576, 2592, 2608, 2624, 2640, 2656, 2672, 2688, 2704, 2720, 2736, 2752, 2768, 2784, 2800, 2816, 2832, 2848, 2864, 2880, 2896,
2912, 2928, 2944, 2960, 2976, 2992, 3008, 3024, 3040, 3056, 3072, 3088, 3104, 3120, 3136, 3152, 3168, 3184, 3200, 3216, 3232, 3248, 3264, 3280, 3296, 3312, 3328, 3344, 3360, 3376,
3392, 3408, 3424, 3440, 3456, 3472, 3488, 3504, 3520, 3536, 3552, 3568, 3584, 3600, 3616, 3632, 3648, 3664, 3680, 3696, 3712, 3728, 3744, 3760, 3776, 3792, 3808, 3824, 3840, 3856,
3872, 3888, 3904, 3920, 3936, 3952, 3968, 3984, 4000, 4016, 4032, 4048, 4064, 4080, 4095, 4080, 4064, 4048, 4032, 4016, 4000, 3984, 3968, 3952, 3936, 3920, 3904, 3888, 3872, 3856,
3840, 3824, 3808, 3792, 3776, 3760, 3744, 3728, 3712, 3696, 3680, 3664, 3648, 3632, 3616, 3600, 3584, 3568, 3552, 3536, 3520, 3504, 3488, 3472, 3456, 3440, 3424, 3408, 3392, 3376,
3360, 3344, 3328, 3312, 3296, 3280, 3264, 3248, 3232, 3216, 3200, 3184, 3168, 3152, 3136, 3120, 3104, 3088, 3072, 3056, 3040, 3024, 3008, 2992, 2976, 2960, 2944, 2928, 2912, 2896,
2880, 2864, 2848, 2832, 2816, 2800, 2784, 2768, 2752, 2736, 2720, 2704, 2688, 2672, 2656, 2640, 2624, 2608, 2592, 2576, 2560, 2544, 2528, 2512, 2496, 2480, 2464, 2448, 2432, 2416,
2400, 2384, 2368, 2352, 2336, 2320, 2304, 2288, 2272, 2256, 2240, 2224, 2208, 2192, 2176, 2160, 2144, 2128, 2112, 2096, 2080, 2064, 2048, 2032, 2016, 2000, 1984, 1968, 1952, 1936,
1920, 1904, 1888, 1872, 1856, 1840, 1824, 1808, 1792, 1776, 1760, 1744, 1728, 1712, 1696, 1680, 1664, 1648, 1632, 1616, 1600, 1584, 1568, 1552, 1536, 1520, 1504, 1488, 1472, 1456,
1440, 1424, 1408, 1392, 1376, 1360, 1344, 1328, 1312, 1296, 1280, 1264, 1248, 1232, 1216, 1200, 1184, 1168, 1152, 1136, 1120, 1104, 1088, 1072, 1056, 1040, 1024, 1008, 992, 976,
960, 944, 928, 912, 896, 880, 864, 848, 832, 816, 800, 784, 768, 752, 736, 720, 704, 688, 672, 656, 640, 624, 608, 592, 576, 560, 544, 528, 512, 496, 480, 464, 448, 432, 416, 400,
384, 368, 352, 336, 320, 304, 288, 272, 256, 240, 224, 208, 192, 176, 160, 144, 128, 112, 96, 80, 64, 48, 32, 16
};

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
unsigned long delayMicrosecs = 1000; // Domyślne opóźnienie między próbkami
float targetFrequency = 1.0;         // Domyślna częstotliwość (Hz)
unsigned long lastUpdate = 0;
uint16_t waveIndex = 0;
int burstCount = 0;

// Definicja trybów pracy generatora
enum Mode { STOPPED, SINGLE, BURST, CONTINUOUS };
Mode currentMode = STOPPED;

// Typ fali
enum WaveType { WAVE_SINE, WAVE_TRIANGLE };
WaveType currentWave = WAVE_SINE;

// --- PROTOTYPY FUNKCJI ---
void recalculateDelay();
void handleEndOfCycle();

void setup() {
  Serial.begin(9600);
  Serial.setTimeout(100);

  // Przyspieszenie I2C do 400kHz dla płynniejszej fali
  Wire.begin();
  Wire.setClock(400000);

  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, LOW);

  // --- OCZEKIWANIE NA POŁĄCZENIE Z PC (*IDN?) ---
  while(!CONNECTED_WITH_PC){
    while (Serial.available()){
      char inChar = (char)Serial.read();
      if (inChar == '\n'){ commandReady = true; } else { command += inChar; }
    }

    if (commandReady){
        command.trim(); command.toUpperCase();
        if(command.equals("*IDN?")){
           // Inicjalizacja DAC
           if (!dac.begin(0x60)) {
             Serial.println("ERROR: DAC NOT FOUND");
             // Błąd jeśli nie znaleziono MCP4725
           } else {
             Serial.print(DEVICE_ID); Serial.print('\n'); Serial.flush();
             CONNECTED_WITH_PC = true;
             digitalWrite(LED_BUILTIN, HIGH);
             recalculateDelay(); // Przeliczamy czas na start
           }
        }
        command = ""; commandReady = false;
    }
  }
}

void loop() {
  // 1. ODCZYT DANYCH Z PORTU
  while (Serial.available()){
    char inChar = (char)Serial.read();
    if (inChar == '\n'){ commandReady = true; } else{ command += inChar; }
  }

  // 2. OBSŁUGA KOMEND
  if (commandReady){
    command.trim(); command.toUpperCase();

    if(command.equals("*IDN?")){
      Serial.println("ALREADY_CONNECTED");
    }
    else if (command.startsWith("DAC:")){
        // Tryb ręczny: zatrzymaj falę i ustaw stałe napięcie
        currentMode = STOPPED;
        command.remove(0,4);
        int dacValue = command.toInt();
        if(dacValue < 0) dacValue = 0; if(dacValue > 4095) dacValue = 4095;
        dac.setVoltage(dacValue, false);
    }
    else if (command.equals("READV?")){
      int sensorValue = analogRead(VOLTAGE_READ_PIN);
      float voltage = sensorValue * (5.0 / 1023.0); // Zakładamy referencję 5V
      Serial.println(voltage, 3);
    }
    // --- STEROWANIE GENERATOREM ---
    else if (command.equals("START")) {
      currentMode = CONTINUOUS;
      waveIndex = 0; // Reset fazy przy starcie
    }
    else if (command.equals("STOP")) {
      currentMode = STOPPED;
    }
    else if (command.equals("ONCE")) {
      currentMode = SINGLE;
      waveIndex = 0;
    }
    else if (command.startsWith("BURST")) {
      // Format: "BURST 5" (5 cykli)
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
      // Format: "FREQ 10.5" (10.5 Hz)
      int spaceIndex = command.indexOf(' ');
      if (spaceIndex > 0) {
        float newFreq = command.substring(spaceIndex + 1).toFloat();
        if (newFreq > 0) {
          targetFrequency = newFreq;
          recalculateDelay();
        }
      }
    }
    else if (command.startsWith("WAVE")) {
      // Komendy wyboru typu fali: WAVE SIN, WAVE TRI
      int spaceIndex = command.indexOf(' ');
      if (spaceIndex > 0) {
        String type = command.substring(spaceIndex + 1);
        type.trim();
        if (type.equals("SIN")) {
          currentWave = WAVE_SINE;
        } else if (type.equals("TRI")) {
          currentWave = WAVE_TRIANGLE;
        }
      }
    }

    command = ""; commandReady = false;
  }

  // 3. GENERATOR FALI (NON-BLOCKING)
  if (currentMode != STOPPED) {
    unsigned long currentMicros = micros();

    // Czy minął czas na kolejną próbkę?
    if (currentMicros - lastUpdate >= delayMicrosecs) {
      lastUpdate = currentMicros;

      uint16_t value;
      if (currentWave == WAVE_SINE) {
        value = pgm_read_word(&(DACLookup_FullSine[waveIndex]));
      } else {
        value = pgm_read_word(&(DACLookup_Triangle[waveIndex]));
      }

      dac.setVoltage(value, false);

      waveIndex++;
      if (waveIndex >= ILOSC_PROBEK) {
        waveIndex = 0;
        handleEndOfCycle();
      }
    }
  }
}

// --- FUNKCJE POMOCNICZE ---

void handleEndOfCycle() { // Obsługa logiki po zakończeniu jednego pełnego cyklu fali
  if (currentMode == SINGLE) {
    currentMode = STOPPED;
  }
  else if (currentMode == BURST) {
    burstCount--;
    if (burstCount <= 0) {
      currentMode = STOPPED;
    }
  }
  // W trybie CONTINUOUS nic nie jest zmieniane, fala trwa dalej
}

void recalculateDelay() { // Przeliczenie opóźnienia w mikrosekundach na podstawie częstotliwości
  if (targetFrequency <= 0.001) targetFrequency = 0.001; // Zabezpieczenie przed zerem

  // Wzór: 1 sekunda (w us) / (częstotliwość * ilość próbek)
  // float jest uzywany do mianownika, aby zachować precyzję, a potem zmieniany na unsigned long
  float delayFloat = 1000000.0 / (targetFrequency * (float)ILOSC_PROBEK);

  delayMicrosecs = (unsigned long)delayFloat;

  // Zabezpieczenie, żeby nie było 0 (DAC ma swoje limity prędkości I2C)
  if (delayMicrosecs < 1) delayMicrosecs = 1;
}
