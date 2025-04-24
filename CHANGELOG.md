# Changelog

<!---
https://keepachangelog.com/
### Added
### Changed
### Deprecated
### Removed
### Fixed
### Security
--->

## [v1.9.2] - 2025-03-31

### Changed

- Update build environ:
    - Android Studio Meerkat (Gradle 8.13, AGP 8.9.1)
    - Java 17
    - SnakeYAML 2.4


## [v1.8.0] - 2023-05-26

### Added

- SettingsActivity: SINETStream: Add icons for some items.
- SettingsActivity: SINETStream: Split settings for manual and remote configurations.
- SettingsActivity: SINETStream: Add settings screen for remote configuration.
    - Access token picker
    - Key pair management on local device
    - Public key management on the config-server
    - Pre-download and keep REST-API parameters in the shared preference

### Changed

- MainActivity: If predefined parameters exist, read them without user interventions.
- Update build environ


## [v1.6.1] - 2022-12-12

### Added

- Add option to download SINETStream parameters from specified Config Server.
- Run on Android 12+ devices, along with PahoMqttAndroid-bugfix library

### Changed

- Reorganize Settings layer structures
- Follow guidelines for runtime permissions handling
- Show progress bar if the broker connection has lost and auto-reconnect procedure is running

### Removed

- Embedded SINETStream configuration file has removed.

### Fixed

- Fix some lint warnings, such as rewrite obsoleted API usages

### Security

- Update Android Studio and its tools
- Update all dependency modules


## [v1.6.0] - 2021-12-22

### Added

- SettingsActivity: SINETStream: Add detailed parameters for MQTT and SSL/TLS.

### Changed

- build.gradle: Use MavenCentral instead of jCenter
- build.gradle: Use JDK 11 instead of JDK 8, from Android Studio Arctic Fox.

- SettingsActivity: Rearrange menu hierarchy.
- MainActivity: For SSL/TLS connection, operation will be intercepted by a system dialog to pick up credentials.
- MainActivity: Received message will be shown in timeline, instead of the latest only.
- MainActivity: Use typed Reader/Writer classes in `sinetstream-android` library.

### Removed

- SettingsActivity: Exclude TLSv1 and TLSv1.1 from menu items, and set TLSv1.2 as default.

### Fixed

- MainActivity: Keep some attributes beyond Activity`s lifecycle.


## [v1.5.3] - 2021-05-20

#### Added

- MainActivity: Show receiver fragment contents with history.

#### Changed

- build.gradle: Update build environ for the Android Studio 4.1.2.
- MainActivity: Adjust screen layout.
- SettingsActivity: Use fixed service name.

#### Fixed

- MainActivity: Resolve race conditions between modal dialogs.
- MainActivity: Keep the receiver fragment contents, even if the activity has re-created after suspend/resume.


## [v1.5.0] - 2021-03-18

### Added

- Initial release

