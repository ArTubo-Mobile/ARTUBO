# ASUCARE 🌱

ASUCARE is an Android application designed to assist sugarcane farmers in monitoring crop health using AI and IoT. It features real-time leaf disease detection through object detection and integrates with Arduino-based sensors to gather vital environmental and plant-specific data. The app also includes user authentication for personalized crop management.

## ✨ Features

- 📷 **Leaf Disease Detection**
  - Uses object detection (e.g., TensorFlow Lite) to identify sugarcane leaf diseases via the camera.
  - Offers visual feedback and suggestions for treatment.

- 📡 **Real-time Sensor Data**
  - Fetches data from Arduino sensors (e.g., temperature, soil moisture, humidity).
  - Displays live graphs and trends for informed decision-making.

- 👤 **User Authentication**
  - Account creation and login using Firebase Authentication.
  - Secure and persistent user data.

- 📊 **Dashboard**
  - Overview of plant health metrics and detected diseases.
  - Historical data visualization for disease progression and environmental changes.

## 🛠️ Tech Stack

- **Frontend**: Android (Java/Kotlin)
- **ML**: TensorFlow Lite for object detection
- **Backend**: Firebase (Auth, Firestore, Realtime Database)
- **Hardware**: Arduino + DHT11/Soil Moisture Sensor/Other IoT Modules
- **Communication**: Bluetooth / WiFi / MQTT (for Arduino-to-App data transfer)

## 🔧 Setup Instructions

### Android App

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/asucare.git
    ```

2. Open in Android Studio.
3. Add your Firebase configuration file (`google-services.json`) to `/app`.
4. Sync project and build the APK.

### Arduino Sensor

1. Connect sensors to Arduino (DHT11, Soil Moisture Sensor, etc.)
2. Upload the sensor sketch (`arduino/sketch.ino`) provided in the repo.
3. Ensure Bluetooth/Wi-Fi module is paired with the Android device.
4. Stream sensor data using a defined protocol (e.g., JSON over serial/Bluetooth).

## 📷 Leaf Disease Detection

* Trained a custom object detection model on sugarcane leaf images.
* Model exported as `.tflite` and integrated into the app using TensorFlow Lite Android API.
* Detects common diseases such as red rot, leaf scald, smut, etc.

## 🔐 Authentication & User Flow

* Firebase Email/Password authentication
* User profile and historical sensor data stored in Firestore
* Secure access to personalized dashboard and disease history

## 📎 Future Improvements

* Integration with weather APIs for predictive analytics
* Notification alerts for disease detection or abnormal readings
* Multilingual support for local farmers

## 📸 Screenshots

*(Include screenshots of disease detection, sensor data dashboard, and login page here)*

## 📄 License

MIT License. See [LICENSE](LICENSE) for more information.

## 🤝 Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you'd like to change.

---

Made with 💚 for sustainable farming.
