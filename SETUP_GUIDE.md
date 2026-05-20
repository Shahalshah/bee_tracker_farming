# Madhu-Siri (Bee-Farmer Harmony App) Setup Guide

## 1. Firebase Setup
1. Go to [Firebase Console](https://console.firebase.google.com/).
2. Create a new project named "Madhu-Siri".
3. Add an Android App with package name `com.example.ben`.
4. Download `google-services.json` and place it in the `app/` directory.
5. Enable **Authentication** (Email/Password).
6. Enable **Realtime Database** (Start in test mode or set rules).
7. Enable **Cloud Messaging** (FCM).

### Firebase Security Rules (Realtime Database)
```json
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null"
  }
}
```

## 2. Google Maps API Setup
1. Go to [Google Cloud Console](https://console.cloud.google.com/).
2. Enable **Maps SDK for Android**.
3. Create an API Key in **Credentials**.
4. Restrict the key to your Android app (optional but recommended).
5. Open `AndroidManifest.xml` and replace the value in `com.google.android.geo.API_KEY` with your key.

## 3. SHA-1 Generation (Required for Google Maps/Auth)
Run the following command in Android Studio Terminal:
```bash
./gradlew signingReport
```
Copy the SHA-1 from the `debug` variant and add it to your Firebase project settings.

## 4. FCM Setup
To test push notifications, use the Firebase Console -> Cloud Messaging -> New Campaign. Target the app by its package name or a specific FCM token.

## 5. Running the Project
1. Clean and Rebuild the project in Android Studio.
2. Run on an Emulator or Physical device with Google Play Services.
3. Grant Location permissions when prompted.
