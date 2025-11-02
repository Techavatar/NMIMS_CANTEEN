# Firebase Quick Setup Guide for NMIMS Canteen

## 🔥 Firebase Setup in 5 Minutes

### Step 1: Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **"Add project"**
3. Project name: **NMIMS_Canteen**
4. Click **"Create project"** → **"Continue"**

### Step 2: Add Android App
1. In Firebase Console, click **"Add app"** → **"Android"**
2. **Package name:** `com.nmims.canteen`
3. **App nickname:** `NMIMS Canteen` (optional)
4. **Debug signing certificate:** Leave blank for now
5. Click **"Register app"**

### Step 3: Download Config File
1. Click **"Download google-services.json"**
2. **Place this file** in: `NMIMS_CANTEEN/app/google-services.json`

### Step 4: Enable Required Services
1. **Authentication:**
   - Go to **Build → Authentication**
   - Click **"Get started"**
   - Enable **Email/Password** and **Google** sign-in

2. **Firestore Database:**
   - Go to **Build → Firestore Database**
   - Click **"Create database"**
   - Choose **"Start in test mode"** (for now)
   - Select a location (choose closest to your users)

3. **Storage (Optional):**
   - Go to **Build → Storage**
   - Click **"Get started"**
   - Start in test mode

### Step 5: Test Rules (For Development Only)

**Firestore Rules:**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true; // Testing only!
    }
  }
}
```

**Storage Rules:**
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if true; // Testing only!
    }
  }
}
```

### Step 6: Run the App
1. Open the project in Android Studio
2. Wait for Gradle sync
3. Run on emulator or device
4. Create account → Food items will auto-populate!

## ✅ You're Ready!

Your NMIMS Canteen app is now connected to Firebase and ready to use! The app will automatically populate with all 19 food items on first run.

**Remember:** Change the Firebase security rules before going to production!