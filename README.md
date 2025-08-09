# Audioscribe

An Android application that captures system audio and transcribes it using AI, creating a searchable knowledge base from passive audio consumption.

## Features (Planned)
- **System Audio Capture**: Uses MediaProjection API to capture audio from other apps
- **AI Transcription**: Leverages OpenAI Whisper API for accurate speech-to-text
- **Local Storage**: Room database for offline transcript access
- **Cloud Sync**: Firebase Storage for backup and cross-device access
- **Background Processing**: WorkManager for reliable background tasks

## Technical Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM with Repository pattern
- **Database**: Room (SQLite)
- **Networking**: Retrofit + OkHttp
- **Background Tasks**: WorkManager
- **Cloud Storage**: Firebase Storage
- **Dependency Injection**: Manual (initially)

## Requirements
- Android 10+ (API 29) - Required for AudioPlaybackCapture
- Microphone permission
- MediaProjection permission
- Internet access for transcription API

## Setup
1. Clone the repository
2. Open in Android Studio
3. Add your API keys to `local.properties`:
   ```
   OPENAI_API_KEY=your_key_here
   ```
4. Build and run

## Project Status
✅ **Task #1**: Project Setup and Dependency Management - **COMPLETE**
- Android project structure created
- All required dependencies configured
- Manifest permissions set up
- Basic UI foundation with Compose

**Next**: Task #2 - Implement Audio Capture Service

## Development
This project follows the PRD outlined in `.taskmaster/docs/prd.txt` and uses Task Master for project management.

## License
TBD
