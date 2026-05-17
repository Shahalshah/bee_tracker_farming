# Madhu-Siri (Agriculture) – Bee-Farmer Harmony App Setup Guide

## 1. Firebase Integration
1. Go to [Firebase Console](https://console.firebase.google.com/).
2. Create a new project named "Madhu-Siri".
3. Add an Android App:
   - Package name: `com.example.ben`
4. Download `google-services.json` and place it in the `app/` folder.
5. Enable **Authentication**:
   - Go to Build > Authentication > Get Started.
   - Enable **Email/Password** sign-in method.
6. Enable **Realtime Database**:
   - Go to Build > Realtime Database > Create Database.
   - Choose a location and start in **test mode** (or set rules for authenticated users).
7. Enable **Cloud Messaging (FCM)** for notifications (automatic setup with Firebase).

## 2. Google Maps API Integration
1. Go to [Google Cloud Console](https://console.cloud.google.com/).
2. Create a project and enable "Maps SDK for Android".
3. Create an API Key in APIs & Services > Credentials.
4. Open `AndroidManifest.xml` in Android Studio.
5. Replace `YOUR_GOOGLE_MAPS_API_KEY_HERE` with your actual API key in the `<meta-data>` tag.

## 3. Running the App
1. Sync project with Gradle files.
2. Connect an Android device or use an Emulator.
3. Click "Run" in Android Studio.

## 4. App Features
- **Splash Screen**: Professional intro.
- **Login/Signup**: Role selection (Farmer/Beekeeper).
- **Dashboard**: Role-based access to features.
- **Hive Map**: Beekeepers long-press to add hives; Farmers view all hives.
- **Spray Alerts**: Farmers send alerts; everyone views the alert history.
- **Health Tracker**: Beekeepers log honey production and hive health.
- **Bee Tips**: Educational content for bee protection.

## 5. Project Architecture
- **Language**: Kotlin
- **UI**: XML Layouts with ViewBinding
- **Database**: Firebase Realtime Database
- **Auth**: Firebase Authentication
- **Theme**: Nature-inspired (Green, Yellow, Honey tones)
