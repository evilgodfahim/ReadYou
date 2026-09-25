<div align="center">
  <img width="160" height="160" style="display: block; border-radius: 36px; box-shadow: 0 8px 24px rgba(0,0,0,0.12);" src="https://raw.githubusercontent.com/ReadYouApp/ReadYou/main/fastlane/metadata/android/en-US/images/icon.png" alt="Read You Icon">

  # Read You

  ### *Material You RSS Reader &bull; AI-Enhanced Edition*

  <p align="center">
    <strong>A modern, distraction-free RSS reader for Android crafted in Material Design 3, engineered for ocular comfort and powered by intelligent AI assistance.</strong>
  </p>

  <p align="center">
    <a href="https://android.com"><img alt="Platform" src="https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white"></a>
    <a href="https://kotlinlang.org"><img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.0+-7F52FF?style=flat-square&logo=kotlin&logoColor=white"></a>
    <a href="https://developer.android.com/jetpack/compose"><img alt="Jetpack Compose" src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white"></a>
    <a href="https://m3.material.io/"><img alt="Material Design 3" src="https://img.shields.io/badge/Design-Material%203-c3e7ff?style=flat-square"></a>
    <a href="https://ai.google.dev/"><img alt="Gemini AI" src="https://img.shields.io/badge/AI-Gemini%20Enabled-8E75FF?style=flat-square&logo=googlebard&logoColor=white"></a>
    <a href="LICENSE"><img alt="License" src="https://img.shields.io/badge/License-GPL%20v3.0-blue?style=flat-square"></a>
  </p>

  <p align="center">
    <a href="#key-highlights">Highlights</a> &bull;
    <a href="#ocular-comfort--reading-flow">Design & Eye Care</a> &bull;
    <a href="#ai-features--intelligence">AI Features</a> &bull;
    <a href="#sync--cloud-integrations">Cloud Sync</a> &bull;
    <a href="#tech-stack">Tech Stack</a> &bull;
    <a href="#building-from-source">Building</a> &bull;
    <a href="#license">License</a>
  </p>

  <br>

  <img src="https://raw.githubusercontent.com/ReadYouApp/ReadYou/main/fastlane/metadata/android/en-US/images/phoneScreenshots/startup.png" width="19%" alt="Startup Screen" />
  <img src="https://raw.githubusercontent.com/ReadYouApp/ReadYou/main/fastlane/metadata/android/en-US/images/phoneScreenshots/feeds.png" width="19%" alt="Feeds Screen" />
  <img src="https://raw.githubusercontent.com/ReadYouApp/ReadYou/main/fastlane/metadata/android/en-US/images/phoneScreenshots/flow.png" width="19%" alt="Flow Screen" />
  <img src="https://raw.githubusercontent.com/ReadYouApp/ReadYou/main/fastlane/metadata/android/en-US/images/phoneScreenshots/read.png" width="19%" alt="Reader Screen" />
  <img src="https://raw.githubusercontent.com/ReadYouApp/ReadYou/main/fastlane/metadata/android/en-US/images/phoneScreenshots/settings.png" width="19%" alt="Settings Screen" />

  <br>
  <br>
</div>

---

## Overview

**Read You (AI-Enhanced Edition)** is a modern Android RSS reader built from the ground up with **Jetpack Compose** and **Material Design 3**. It combines the dynamic aesthetics of Material You with deeply considered reading ergonomics: Kindle-inspired low-contrast typography for pitch-black AMOLED screens, clean list hierarchies, background AI processing, and hands-free commute listening.

Whether you follow technical blogs, news outlets, or creative journals, Read You delivers a tranquil, focused reading experience free of algorithmic noise, tracking, or clutter.

---

## Key Highlights

- 📖 **Kindle-Grade Ocular Comfort**: Thoughtfully calibrated warm cream/bone white headlines on pitch-black dark mode to eliminate night-time glare and visual strain.
- 🎨 **Adaptive Material You Theming**: Dynamic Monet color extraction harmonized across light and dark themes.
- 🤖 **On-Demand & Background AI**: Intelligent article summarization, accurate multi-lingual translation, and deep contextual chat backed by Google Gemini.
- 🎧 **Commute Audio Briefing (TTS)**: Automatic text-to-speech audio playlist compiled directly from your unread article summaries.
- 🔄 **Resilient Sync Engine**: Reliable background synchronization via `WorkManager`, persistent task retry queues, and extended 90-second timeouts for AI model workloads.
- 🌐 **Universal RSS & Cloud Support**: Native local feeds with OPML import/export, plus full two-way sync with Google Reader APIs (FreshRSS, Inoreader, Miniflux, etc.) and Fever.

---

## Ocular Comfort & Reading Flow

### 🌙 Soothing Low-Contrast Typography
Traditional dark modes often place pure `#FFFFFF` text against `#000000` backgrounds, causing harsh halos and retinal fatigue during prolonged reading. This edition introduces a warm, paper-like reading palette:
- **Dominant Cream Headlines**: Styled in soft warm cream white (`#C8C2B6`) with semi-bold weighting and comfortable line height (`23.sp`), ensuring articles stand out immediately without searing the eyes.
- **Harmonized Lower Layers**: Article preview snippets are tuned to warm slate (`#6E6A63`) and metadata to subtle charcoal (`#504D47`), creating a natural visual hierarchy.
- **AMOLED-Optimized Dark Canvas**: True deep blacks maximize battery life on OLED panels while keeping content easy to scan.

### 📰 Curated Timeline Layout
- **Left-Aligned Media**: Clean thumbnail placement on the leading side for quick identification.
- **Subtle Visual Accent Bars**: Integrated vertical markers beside article cards to indicate unread status and structure list flow cleanly.
- **Smart Description Truncation**: Concise previews prevent wall-of-text fatigue while preserving the context of each story.
- **Full Article Readability**: Integrated content extraction strips away cookie notices, advertisements, and navigation bars for distraction-free reading.

---

## AI Features & Intelligence

Read You integrates modern LLMs (including Google Gemini and OpenAI-compatible APIs) to transform how you consume knowledge:

| Feature | Description |
| :--- | :--- |
| **Instant Summarization** | Generate concise, actionable summaries for long-form essays, research papers, and news dispatches with one tap. |
| **Background Precompute Queue** | As feeds synchronize, a dedicated SQLite-backed background worker (`AiSummaryPrecomputeWorker`) can pre-generate summaries so they are immediately available offline. |
| **Contextual Translation** | Translate international articles into your preferred language while faithfully preserving Markdown formatting, headers, and code blocks. |
| **Interactive AI Chat** | Ask follow-up questions, explore nuances, or request clarifications on any article with integrated web search grounding. |
| **Commute Audio Brief** | Generates a sequential Text-to-Speech playlist of article summaries for hands-free listening while walking, driving, or commuting. |
| **Extended Model Resilience** | Network pipelines support up to 90-second timeout thresholds and exponential retry policies, preventing timeouts during heavy generative model responses. |

---

## Sync & Cloud Integrations

Read You acts as both a standalone offline RSS reader and a client for popular self-hosted and cloud sync services:

- **Local Storage**: Completely private standalone reader with local SQLite database.
- **OPML Management**: One-click import and export of your subscription lists.
- **Google Reader API**: Seamless synchronization with:
  - FreshRSS
  - Inoreader
  - Miniflux
  - BazQux Reader
  - The Old Reader
- **Fever API**: Compatible with self-hosted Fever backends.
- **Multi-Account**: Switch smoothly between independent accounts and sources.

---

## Tech Stack & Architecture

Built following modern Android best practices and Clean Architecture:

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3 (M3).
- **Language & Concurrency**: [Kotlin 2.0+](https://kotlinlang.org/) with Coroutines, `StateFlow`, and `SharedFlow`.
- **Architecture**: MVVM / MVI pattern with unidirectional data flow and modular service layers.
- **Local Persistence**: [Room Database](https://developer.android.com/training/data-storage/room) with type converters and robust schema migrations.
- **Dependency Injection**: [Hilt](https://dagger.dev/hilt/) for clean component lifecycle management.
- **Networking**: [OkHttp 4](https://square.github.io/okhttp/) & [Retrofit](https://square.github.io/retrofit/) with custom interceptors and resilient retry handlers.
- **Background Processing**: [Android WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) for power-efficient scheduled syncing and AI precomputation.
- **Article Extraction**: [Readability4J](https://github.com/dankito/Readability4J) & [Rome Tools](https://github.com/rometools/rome) for robust feed ingestion.
- **Audio & Speech**: Android Native Text-to-Speech (`TextToSpeech`) with segment duration calculation and queue management.

---

## Building from Source

### Prerequisites
1. **Android Studio** (Ladybug | 2024.2.1 or newer recommended)
2. **JDK 17** or **JDK 21**
3. **Android SDK** (API Level 35 compile target, minimum API 26)

### Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/ReadYouApp/ReadYou.git
   cd ReadYou
   ```

2. Open the project in Android Studio:
   - Select **Open an Existing Project** and choose the cloned directory.
   - Allow Gradle to sync dependencies.

3. Build and run:
   - Select the `app` run configuration.
   - Choose a physical device or Android Virtual Device (AVD).
   - Click **Run ▶** (or build release APK via `Build > Build Bundle(s) / APK(s) > Build APK(s)`).

### AI Configuration
To enable AI features (Summarization, Translation, Contextual Q&A):
1. Open the app and navigate to **Settings &gt; AI Services**.
2. Select your AI Provider (e.g., Google Gemini or custom OpenAI-compatible endpoint).
3. Enter your API key securely.
4. Customize your preferred prompt tone, summary length, and background precomputation preferences.

---

## Credits & Acknowledgements

Read You stands on the shoulders of remarkable open-source projects and community contributors:

- **Upstream Project**: Thanks to the original creator **Ash** and all contributors to [Read You](https://github.com/ReadYouApp/ReadYou).
- **Design Inspiration**: **@Kyant0** for the Monet engine implementation and original aesthetic inspiration.
- **Open Source Libraries**:
  - [MusicYou](https://github.com/Kyant0/MusicYou) &bull; [ParseRSS](https://github.com/muhrifqii/ParseRSS) &bull; [Readability4J](https://github.com/dankito/Readability4J)
  - [opml-parser](https://github.com/mdewilde/opml-parser) &bull; [Rome](https://github.com/rometools/rome) &bull; [Seal](https://github.com/JunkFood02/Seal)
  - [besticon](https://github.com/mat/besticon) &bull; [Jiffy Reader](https://github.com/ansh/jiffyreader.com)
- **Supporting Platforms**:
  - [JetBrains](https://www.jetbrains.com/) for open-source development tooling.
  - [Weblate](https://hosted.weblate.org/) for crowd-sourced international localization.

---

## License

Read You is free software licensed under the **GNU General Public License v3.0 (GPL-3.0)**.  
You are free to use, modify, and redistribute this application in accordance with the terms of the license.

See the [LICENSE](LICENSE) file for complete details.
