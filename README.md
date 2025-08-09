# Audioscribe

An Android application that captures system audio and transcribes it using AI, creating a searchable knowledge base from passive audio consumption.

## Features (Planned)
- **System Audio Capture**: Uses MediaProjection API to capture audio from other apps
- **AI Transcription**: Leverages OpenAI Whisper API for accurate speech-to-text
- **Local Storage**: Room database for offline transcript access
- **Local-Only Operation**: All data stored locally on device (no cloud dependencies)
- **Background Processing**: WorkManager for reliable background tasks

## Technical Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM with Repository pattern
- **Database**: Room (SQLite)
- **Networking**: Retrofit + OkHttp
- **Background Tasks**: WorkManager
- **Local Storage**: Device internal/external storage
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

✅ **Task #2**: Implement Audio Capture Service - **COMPLETE**
- MediaProjection integration for system audio access
- AudioPlaybackCapture implementation
- High-quality WAV file recording (44.1kHz stereo)
- Foreground service with notification controls
- Comprehensive permission management

✅ **Task #3**: Develop Basic UI - **COMPLETE**
- Material 3 UI design with Compose
- Permission status indicators and controls
- Start/Stop recording functionality
- Transcription results display area
- Processing state indicators
- Professional user experience

**Next**: Task #4 - Implement Transcription API Integration

## Development
This project follows the PRD outlined in `.taskmaster/docs/prd.txt` and uses Task Master for project management.

## License
TBD
