# Data Layer

This directory contains the data layer components for Audioscribe:

## Structure
- `database/` - Room database entities, DAOs, and database setup
- `repository/` - Repository pattern implementations
- `model/` - Data models and API response classes  
- `network/` - Retrofit API interfaces and network utilities
- `worker/` - WorkManager background task implementations

## To be implemented in upcoming tasks:
- Room database for transcript storage
- Retrofit API client for OpenAI Whisper
- Repository for managing transcription data
- WorkManager workers for background processing
