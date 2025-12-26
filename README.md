# Facile Electrical Signal Generator (FESG)

![License](https://img.shields.io/badge/License-GPLv3-blue.svg)
![Java](https://img.shields.io/badge/Java-Swing-orange)
![Platform](https://img.shields.io/badge/Platform-Arduino-teal)

**FESG** is a digital Arbitrary Waveform Generator (AWG) system based on Master-Slave architecture. This project was developed as an engineering Bachelor's thesis.

The system consists of a PC control application (Java Swing) and an executive module based on an Arduino microcontroller and a Digital-to-Analog Converter (DAC). It allows for precise voltage control, generation of periodic waveforms, and reproduction of custom sequences from external files.

## 🚀 Key Features

1.  **Manual Control:**
    * Direct control of the DAC output voltage (12-bit resolution: 0-4095).
    * Feedback voltage reading via ADC (pin A0).
2.  **Function Generator:**
    * Standard waveforms generation: **Sine, Triangle**.
    * Operation modes: **Continuous, Single (One-shot), Burst**.
    * Frequency regulation (software delay control).
3.  **Arbitrary Waveform Player:**
    * Importing voltage sequences from `.txt` / `.csv` files.
    * Loop playback capability.
    * Real-time progress visualization via UI.

## 🛠️ Technologies & Hardware

### Hardware
* **Microcontroller:** Arduino Uno (ATmega328P).
* **DAC:** MCP4725 (12-bit, I2C interface).
* **Communication:** UART (USB-Serial) @ 9600 baud.

### Software
* **PC:** Java 17+, Swing (GUI), `jSerialComm` library for serial communication.
* **Firmware:** C++ (Arduino), `Adafruit_MCP4725` library.
* **Optimization:** Utilizes **I2C Fast Mode (400 kHz)** and PROGMEM Lookup Tables to ensure signal fluidity and minimize latency.

## 🔌 Wiring Diagram

(to be done)

> **Note:** For correct operation at higher sampling rates, I2C connections must be stable and short.

## 📥 Installation & Setup

### 1. Firmware (Arduino)
1.  Open `Arduino/Main.ino` in Arduino IDE.
2.  Install the `Adafruit MCP4725` library via the Library Manager.
3.  Upload the code to your board.
    * *The code uses `Wire.setClock(400000)` by default. If you encounter connection issues, check your wiring or comment out this line in `setup()`.*

### 2. PC Application (Java)
Requires Java Runtime Environment (JRE) version 17 or higher.

```bash
# Clone the repository
git clone [https://github.com/YourUsername/FESG.git](https://github.com/YourUsername/FESG.git)

# Run via IDE or built JAR file
java -jar FESG.jar
