# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Android Solid Services (ASS) is an Android app and SDK for integrating with [Solid](https://solidproject.org/) pods. It provides single sign-in for the Solid ecosystem on Android, allowing third-party apps to authenticate and access Solid pod resources through inter-process communication (IPC via AIDL).

## Build Commands

```sh
# Build debug APK
./gradlew assembleDebug
# Output: ./app/build/outputs/apk/debug/

# Build release APK
./gradlew assembleRelease

# Run unit tests (all modules)
./gradlew test

# Run unit tests for a single module
./gradlew :app:testDebugUnitTest
./gradlew :Shared:testDebugUnitTest

# Run Android instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest

# Clean build
./gradlew clean

# Publish libraries to Maven Local (for local testing)
./gradlew publishToMavenLocal
```

Requires JBR v17.0.9 or JDK 17. Set `JAVA_HOME` accordingly if builds fail.

## Module Architecture

Four Gradle modules with a layered dependency chain:

```
app (Android application)
 ├── SolidAndroidApi   (direct Solid server interaction)
 ├── SolidAndroidClient (IPC client library for third-party apps)
 │    └── Shared
 └── SolidAndroidApi
      └── Shared        (common types, AIDL definitions, RDF/resource models)
```

### Shared (`com.pondersource.shared`)
Common data types and AIDL interface definitions shared across all modules:
- **Resource model**: `Resource` -> `RDFSource`/`NonRDFSource` -> `SolidRDFSource`/`SolidNonRDFSource`/`SolidContainer`
- **Data modules**: Contact data models (`AddressBook`, `Contact`, `Group`) with RDF serialization classes
- **Vocabulary constants**: RDF vocabulary classes in `shared/vocab/` (LDP, VCARD, ACL, OWL, etc.)
- **AIDL parcelable definitions** for cross-process data transfer

### SolidAndroidApi (`com.pondersource.solidandroidapi`)
Library for direct Solid server communication. Published to Maven Central as `com.pondersource.solidandroidapi:solidandroidapi`.
- `Authenticator`/`AuthenticatorImplementation` - OpenID Connect auth with DPoP support
- `SolidResourceManager`/`SolidResourceManagerImplementation` - CRUD operations on Solid resources
- `SolidContactsDataModule`/`SolidContactsDataModuleImplementation` - Contacts data module
- `DPoPGenerator`, `DPopClientSecretBasic` - DPoP proof generation for authenticated requests
- Uses Inrupt Java Client libraries for Solid protocol interaction

### SolidAndroidClient (`com.pondersource.solidandroidclient`)
IPC client library for third-party apps. Published to Maven Central as `com.pondersource.solidandroidclient:solidandroidclient`.
- `Solid` - Entry point: `Solid.getSignInClient()`, `Solid.getResourceClient()`, `Solid.getContactsDataModule()`
- AIDL interfaces (`IASSAuthenticatorService`, `IASSResourceService`, `IASSContactsModuleInterface`) define the IPC contract
- Communicates with the main app via Android bound services

### app (`com.pondersource.androidsolidservices`)
The Android Solid Services application itself:
- **UI**: Jetpack Compose with MVVM pattern, Hilt DI, Navigation Compose
- **Services**: `ASSAuthenticatorService`, `ASSResourceService`, `SolidDataModulesService` - bound services that third-party apps connect to via AIDL
- **Repository layer**: `AccessGrantRepository`, `ResourcePermissionRepository` for managing app permissions
- **DataStore + Protobuf** for local persistence of access grants and user profiles

## Key Technical Details

- **IPC mechanism**: Android AIDL (Android Interface Definition Language) for cross-app communication. AIDL files in `Shared/src/main/aidl/` and `SolidAndroidClient/src/main/aidl/` define the service contracts.
- **Auth flow**: OpenID Connect with DPoP (Demonstration of Proof-of-Possession) tokens. Uses `net.openid:appauth` library.
- **RDF handling**: Inrupt Java Client SDK (`com.inrupt.client.*`) for Solid protocol operations and RDF parsing.
- **DI**: Hilt (app module only). Modules in `app/.../di/`.
- **Min SDK**: 26, Target SDK: 33, Compile SDK: 35
- **Library publishing**: Vanniktech Maven Publish plugin, targeting Sonatype Central Portal.
