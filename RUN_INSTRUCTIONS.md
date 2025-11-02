# How to Run NMIMS Canteen App in Android Studio

## Prerequisites

1. **Android Studio** (latest version recommended)
2. **Java Development Kit (JDK)** 11 or higher
3. **Android SDK** with API level 24 or higher
4. **Physical Android device** or **Emulator** with Android 7.0+ (API 24+)

## Step-by-Step Instructions

### 1. Open the Project in Android Studio

1. Launch **Android Studio**
2. Click **"Open an Existing Project"** (or "Open" if you already have Android Studio open)
3. Navigate to the project folder: `/workspace/cmhi7latq04r6ocilpv2rcq9o/NMIMS_CANTEEN`
4. Select the folder and click **"OK"**

### 2. Wait for Gradle Sync

1. Android Studio will automatically start **Gradle sync**
2. This may take **5-15 minutes** depending on your internet speed and computer
3. If you see any errors during sync, wait for it to complete
4. If sync fails, try these solutions:
   - Click **"Try Again"**
   - Check your internet connection
   - Restart Android Studio

### 3. Set Up Firebase (Required)

Since the app uses Firebase, you need to set up Firebase:

1. **Create Firebase Project:**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Click **"Add project"**
   - Enter project name: **"NMIMS_Canteen"**
   - Follow the setup steps

2. **Connect App to Firebase:**
   - In Firebase Console, click **"Add app"**
   - Select **"Android"**
   - Enter package name: **`com.nmims.canteen`**
   - Download `google-services.json`
   - Place it in: `NMIMS_CANTEEN/app/google-services.json`

3. **Enable Firebase Services:**
   - Enable **Authentication** (Email/Password and Google Sign-In)
   - Enable **Firestore Database**
   - Create Firestore database in **Test mode** for now
   - Enable **Storage** (optional, for images)

### 4. Configure Emulator or Physical Device

**Option A: Using Android Emulator**
1. Click **Tools → AVD Manager** (or the phone icon in toolbar)
2. Click **"Create Virtual Device"**
3. Select a phone (e.g., Pixel 6)
4. Select a system image (Android 10+ recommended)
5. Click **"Download"** if not available
6. After download, click **"Next"** and **"Finish"**
7. Click the **▶️** button to launch the emulator

**Option B: Using Physical Device**
1. Enable **Developer Options** on your Android device:
   - Go to **Settings → About Phone**
   - Tap **"Build number"** 7 times
2. Enable **USB Debugging**:
   - Go to **Settings → Developer Options**
   - Enable **"USB debugging"**
3. Connect your device via USB
4. Allow USB debugging when prompted

### 5. Run the App

1. Select your target device from the dropdown menu at the top:
   - Choose your emulator or physical device
2. Click the **▶️ Run** button (green triangle) or press **Shift + F10**
3. Wait for build and installation
4. The app will launch automatically

### 6. Initial App Setup

**First Run:**
1. The app will start with the **Login screen**
2. Click **"Sign Up"** to create a new account
3. Enter email, password, name, and phone number
4. Click **"Create Account"**
5. The app will automatically populate the database with food items

**Admin Access:**
1. After creating your account, you'll be taken to the **Main Menu**
2. The **Firebase DataInitializer** will automatically add all 19 food items
3. You can test the app features immediately

### 7. Troubleshooting Common Issues

**Issue 1: Gradle Sync Fails**
```
Solution:
1. Check internet connection
2. Open build.gradle files and ensure all dependencies are correct
3. Try "File → Invalidate Caches / Restart"
4. Delete .gradle folder and restart Android Studio
```

**Issue 2: Firebase Connection Error**
```
Solution:
1. Ensure google-services.json is in app folder
2. Check that Firebase project is created correctly
3. Verify package name matches (com.nmims.canteen)
4. Check internet connection
```

**Issue 3: Build Errors**
```
Solution:
1. Clean Project: Build → Clean Project
2. Rebuild: Build → Rebuild Project
3. Check for missing imports in Java files
4. Verify all resource files exist
```

**Issue 4: App Crashes on Launch**
```
Solution:
1. Check LogCat in Android Studio
2. Ensure Firebase is properly configured
3. Verify all permissions in AndroidManifest.xml
4. Check if all resource files are available
```

**Issue 5: Food Items Not Loading**
```
Solution:
1. Check internet connection (Firebase needs it)
2. Verify Firestore database rules allow access
3. Check LogCat for Firebase errors
4. Try restarting the app
```

### 8. Quick Test Checklist

**Basic Functionality:**
- [ ] App launches successfully
- [ ] Can create new account
- [ ] Food items appear in main menu (should see 19 items)
- [ ] Can search and filter food items
- [ ] Can add items to cart
- [ ] Cart shows correct total
- [ ] Can proceed to checkout
- [ ] Can view order status

**Admin Features (if needed):**
- [ ] Admin dashboard loads
- [ ] Can view analytics
- [ ] Can manage inventory
- [ ] Notifications work

### 9. Firebase Database Rules (Important)

For testing, set these Firestore rules in Firebase Console:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true; // For testing only
    }
  }
}
```

**⚠️ Important:** Change to proper rules before production!

### 10. Project Structure Overview

```
NMIMS_CANTEEN/
├── app/
│   ├── src/main/
│   │   ├── java/com/nmims/canteen/
│   │   │   ├── activities/     # All Activity classes
│   │   │   ├── adapters/       # RecyclerView adapters
│   │   │   ├── models/         # Data models
│   │   │   ├── services/       # Firebase services
│   │   │   └── utils/          # Utility classes
│   │   ├── res/
│   │   │   ├── layout/         # XML layouts
│   │   │   ├── menu/           # Menu files
│   │   │   ├── drawable/       # Icons and backgrounds
│   │   │   ├── values/         # Colors, strings, styles
│   │   │   └── color/          # Color selectors
│   │   └── AndroidManifest.xml
│   ├── build.gradle            # App-level build config
│   └── google-services.json    # Firebase config (add this)
├── build.gradle                # Project-level build config
└── settings.gradle             # Project settings
```

### Need Help?

If you encounter any issues:

1. **Check LogCat** in Android Studio for error messages
2. **Verify Firebase setup** is complete
3. **Ensure all dependencies** are downloaded
4. **Check internet connection** (Firebase requires it)
5. **Try with a different emulator** or physical device

The app is designed to work out-of-the-box once Firebase is properly configured. Enjoy using your NMIMS Canteen app! 🍽️