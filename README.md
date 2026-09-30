# SlimTrack build repository

This repository is used to build the SlimTrack Android APK automatically with GitHub Actions.

The current source bundle is stored as base64 chunks under `build_source/`. The workflow reconstructs the ZIP, extracts the Android project, runs tests/lint, builds a debug APK, and uploads it as a downloadable artifact.
