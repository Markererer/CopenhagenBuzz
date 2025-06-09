# CopenhagenBuzz App 📱

A mobile application developed as part of the **Mobile App Development (MOAPD)** course at **IT University of Copenhagen**. CopenhagenBuzz is a social event-sharing app where users can create, browse, favorite, and locate events around Copenhagen in real-time.

![Screenshot of CopenhagenBuzz](images/copenahgenbuzz.jpg)


## 🧠 Overview

This project demonstrates the use of modern Android development practices including:
- MVVM architecture
- Firebase Realtime Database & Firebase Storage
- Google Maps integration
- Image handling via camera and gallery
- Sensor interaction (shake gesture)
- Dynamic navigation and UI with Material Design 3

## 📸 Key Features

- 📍 **Google Maps integration** with real-time event markers
- 🖼️ **Event image upload** via camera or gallery
- 💾 **Firebase Realtime Database** for storing and retrieving events
- 🌐 **Firebase Storage** for image handling
- 🤳 **Shake-to-find** gesture: scrolls to the nearest event based on current location
- ⭐ **Favorites system**: tag and browse events you like
- 👤 **Multi-auth login**: supports Google, email/password, and guest login
- 🔄 **Live updates** across Timeline, Favorites, and Map views

## 📂 Project Structure

```
app/
├── activities/
│   ├── LoginActivity.kt
│   └── MainActivity.kt
├── fragments/
│   ├── TimelineFragment.kt
│   ├── FavoritesFragment.kt
│   ├── AddEventFragment.kt
│   └── MapsFragment.kt
├── adapters/
│   └── EventAdapter.kt
├── services/
│   └── LocationService.kt
├── viewmodels/
│   └── EventViewModel.kt
└── models/
    ├── Event.kt
    └── EventLocation.kt
```

## 🧪 Testing

Manual testing was performed using USB-deployed builds on real Android devices. Logcat and GitHub Copilot were essential tools during debugging and feature validation.

## ⚠ Known Issues

- Creating a new event can sometimes cause a crash (reason unknown).
- Event creation occasionally takes ~10 seconds to reflect changes.
- Avatar initials in Favorites tab may not reflect the user’s first name correctly.

## 📁 Report

You can find the full academic report (`.pdf` and `.tex`) in the `/docs` folder or download the final version [here](https://markererer.github.io/assets/docs/CopenhBuzz_report_V11.pdf).

## 👤 Author

**Mark Assejev**  
Data Science Bachelor, Spring 2025  
IT University of Copenhagen  
Course: Mobile App Development (MOAPD)  
