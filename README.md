# BoiWatch 🎬

BoiWatch is a modern, high-performance Android application designed for movie and TV show enthusiasts. It provides a seamless discovery experience, detailed media insights, and an integrated AI assistant named **Cinemawala** to help users find their next favorite watch.

## ✨ Features

- **Dynamic Discovery**: Browse trending, popular, and top-rated movies and TV shows powered by TMDB.
- **Cinemawala AI**: An engaging, talkative AI assistant that provides personalized recommendations, movie trivia, and reviews.
- **Specialized Season Picker**: A beautiful BottomSheet UI for series, allowing users to browse and switch seasons with ease.
- **Detailed Media Insights**: Comprehensive details including cast, crew, trailers, photos, and related recommendations.
- **Continue Watching**: Smart history tracking to resume your favorite shows right where you left off.
- **Integrated Video Player**: Seamless streaming experience for both movies and series.
- **Responsive UI**: Optimized for mobile and large-screen devices (tablets/desktops) with adaptive grid layouts.

## 🛠 Tech Stack

- **Language**: Java / Android SDK
- **Architecture**: Modern Android Component Architecture
- **Networking**: Retrofit 2 with GSON
- **Image Loading**: Picasso
- **UI Components**: Material Design 3, CoordinatorLayout, MotionLayout, ViewPager2
- **Video Playback**: ExoPlayer (Media3)
- **AI Integration**: Groq API (Qwen/Llama models)

## 🔑 Environment Variables & API Keys

The app requires API keys from **TMDB** and **Groq** to function correctly. These keys are managed in the `Constants.java` file.

### Location
File: `app/src/main/java/com/tusher/boiwatch/Constants.java`

### Configuration
Update the following fields with your own credentials:

```java
public class Constants {
    // TMDB API Details
    public static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/";
    public static final String TMDB_ACCESS_TOKEN = "YOUR_TMDB_ACCESS_TOKEN_HERE";
    
    // AI (Groq) API Details
    public static final String GROQ_BASE_URL = "https://api.groq.com/openai/v1/";
    public static final String GROQ_API_KEY = "YOUR_GROQ_API_KEY_HERE";
    
    // Preference Keys
    public static final String PREF_NAME = "BoiWatchPrefs";
    // ...
}
```

> **Note**: For production environments, it is recommended to use local properties or encrypted storage to manage these sensitive keys.

## 🚀 Getting Started

1. Clone this repository.
2. Open the project in **Android Studio**.
3. Obtain your API keys from [TMDB](https://www.themoviedb.org/documentation/api) and [Groq Console](https://console.groq.com/).
4. Replace the placeholders in `Constants.java` with your actual keys.
5. Build and run the app on an emulator or a physical device.

---
Developed with ❤️ by Tusher.
