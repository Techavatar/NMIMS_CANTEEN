# 🍽️ NMIMS Canteen - Streamlined Android App

A lightweight, fully functional Android canteen ordering system optimized for smooth Android Studio performance.

## 📋 Features (Simplified & Optimized)

### 🛒 Customer Features
- **Food Menu** with 19 food items organized by categories
- **Search & Filter** functionality
- **Shopping Cart** with real-time updates
- **Secure Authentication** (Email/Password & Google Sign-In)
- **Order Processing** with status tracking
- **Payment Simulation** (Cash, UPI, Card)
- **Basic Profile Management**

### 👨‍💼 Admin Features (Simplified)
- **Admin Dashboard** with basic metrics
- **Order Management** overview
- **Simple Food Item Management**

### 🎨 UI/UX
- **Material Design** with clean interface
- **Smooth Animations**
- **Responsive Layout**
- **Intuitive Navigation**

## 🍴 Food Items Included

All 19 food items from your list are pre-loaded:

1. **Aloo Paratha** - ₹60
2. **Bread Pakora** - ₹40
3. **Veg Burger** - ₹80
4. **Cheese Sandwich** - ₹50
5. **Chole Bhature** - ₹90
6. **Chole Kulche** - ₹70
7. **Idli Sambar** - ₹60
8. **Khamand** - ₹50
9. **Masala Dosa** - ₹80
10. **Paneer Paratha** - ₹90
11. **Pav Bhaji** - ₹100
12. **Veg Pizza** - ₹150
13. **Special Pizza** - ₹200
14. **Rava Dosa** - ₹70
15. **Red Pasta** - ₹120
16. **Samosa** - ₹20
17. **Vada Pav** - ₹30
18. **Veg Sandwich** - ₹45
19. **White Pasta** - ₹130

## 🚀 Quick Start (5 Minutes)

### Prerequisites
- **Android Studio** (any recent version)
- **Android SDK** (API 21+)
- **Device or Emulator**
- **Internet Connection** (for Firebase)

### Step 1: Open Project
1. Open Android Studio
2. Select **"Open an Existing Project"**
3. Navigate to: `NMIMS_CANTEEN` folder
4. Wait for Gradle sync (2-5 minutes)

### Step 2: Firebase Setup (2 minutes)
1. Go to: https://console.firebase.google.com/
2. Create project: **"NMIMS_Canteen"**
3. Add Android app → Package name: **`com.nmims.canteen`**
4. Download `google-services.json`
5. Place it in: `NMIMS_CANTEEN/app/google-services.json`
6. Enable **Authentication** and **Firestore Database**
7. Start Firestore in **Test mode**

### Step 3: Run the App!
1. Select your device/emulator
2. Click **▶️ Run** button
3. **Sign up** → Food items auto-populate!

## 📁 Project Structure (Streamlined)

```
NMIMS_CANTEEN/
├── 📱 app/
│   ├── 💻 java/com/nmims/canteen/
│   │   ├── 📱 activities/       # Core screens (6 files)
│   │   ├── 🎯 adapters/         # RecyclerView adapters (3 files)
│   │   ├── 📦 models/           # Data classes (6 files)
│   │   ├── 🔥 services/         # Firebase services (2 files)
│   │   └── 🛠️ utils/           # Helper classes (3 files)
│   ├── 🎨 res/
│   │   ├── 📐 layout/           # XML layouts (10 files)
│   │   ├── 🍽️ menu/             # Navigation menus (3 files)
│   │   ├── 🖼️ drawable/         # Icons (15 files)
│   │   ├── 🎨 values/           # Resources (3 files)
│   │   └── 🌈 color/            # Color selectors (4 files)
│   └── ⚙️ build.gradle          # App configuration
├── ⚙️ build.gradle              # Project settings
├── 📋 settings.gradle
└── 📖 README.md                 # This file
```

## 🛠️ Tech Stack (Optimized)

- **Language:** Java 11
- **Min SDK:** API 21 (Android 5.0+) - Better compatibility
- **Target SDK:** API 34 (Android 14)
- **Backend:** Firebase (Firestore + Authentication)
- **UI:** Material Design Components
- **Architecture:** Simple Service-Oriented

## ⚡ Performance Optimizations

✅ **Removed bulky components:**
- Review system (removed entire review functionality)
- Advanced analytics (simplified to basic metrics)
- Complex inventory management (removed)
- Heavy admin features (streamlined)
- Unused adapters and models

✅ **Streamlined codebase:**
- **21 Java files** (down from 33)
- **45 XML files** (down from 49)
- Simplified Firebase structure
- Reduced dependencies
- Faster build times

## 🔧 Firebase Setup

### Simple Rules (for testing)
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

### Required Services
✅ **Authentication** (Email/Password + Google)
✅ **Firestore Database** (Test mode)
❌ **Storage** (not required for basic functionality)
❌ **Analytics** (removed for performance)

## 🎯 App Features (What Works)

### ✅ Working Features
- User registration and login
- Food menu browsing with search
- Add items to cart
- Cart management (add/remove/update quantities)
- Checkout process
- Order status tracking
- Basic admin dashboard
- Profile management

### ❌ Removed Features (for performance)
- Review system
- Advanced analytics
- Inventory management
- Sales reports
- Complex admin tools

## 📱 Testing Checklist

**Core Functionality:**
- [ ] App launches smoothly
- [ ] User registration works
- [ ] Login with Google works
- [ ] All 19 food items load
- [ ] Search and filtering work
- [ ] Add to cart works
- [ ] Cart updates correctly
- [ ] Checkout process works
- [ ] Order tracking works
- [ ] Admin dashboard loads basic metrics

## 🐛 Common Issues & Solutions

**❌ Gradle Sync Slow**
```
✅ This is normal first time
✅ Try File → Invalidate Caches/Restart
✅ Check internet connection
```

**❌ Firebase Connection Error**
```
✅ Verify google-services.json is in app/ folder
✅ Check Firebase project setup
✅ Confirm package name: com.nmims.canteen
✅ Test internet connection
```

**❌ App Crashes**
```
✅ Check LogCat for errors
✅ Verify Firebase rules allow access
✅ Make sure Firebase services are enabled
✅ Try clean rebuild
```

## 📞 Quick Support

If issues occur:
1. **Check LogCat** in Android Studio
2. **Verify Firebase setup**
3. **Test internet connection**
4. **Try clean rebuild**

## 🎉 Ready to Go!

Your streamlined NMIMS Canteen app includes:
- ✅ **All 19 food items** from your list
- ✅ **Complete ordering system**
- ✅ **Basic admin dashboard**
- ✅ **Optimized performance**
- ✅ **Faster build times**
- ✅ **Simpler codebase**

**Perfect for Android Studio - runs smoothly and efficiently!** 🚀

---

*Streamlined for performance • All essential features included*