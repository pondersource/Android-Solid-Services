# Getting Started

=== "Use the Libraries"

    ## Add to Your Android Project

    There are two libraries depending on your use case:

    **Option A — Solid Android Client** (recommended for most apps)

    Your app talks to the Android Solid Services host app via IPC. Users log in once in ASS; your app reuses their session.

    ```kotlin
    // build.gradle.kts (module level)
    dependencies {
        implementation("com.pondersource.solidandroidclient:solidandroidclient:0.3.1")
    }
    ```

    **Option B — Solid Android API** (direct Solid access, no host app required)

    Your app communicates with the Solid pod server directly, managing its own auth.

    ```kotlin
    // build.gradle.kts (module level)
    dependencies {
        implementation("com.pondersource.solidandroidapi:solidandroidapi:0.3.1")
    }
    ```

    For other build systems (Maven, etc.) see the Maven Central pages:
    [solidandroidclient](https://central.sonatype.com/artifact/com.pondersource.solidandroidclient/solidandroidclient) ·
    [solidandroidapi](https://central.sonatype.com/artifact/com.pondersource.solidandroidapi/solidandroidapi)

    Once added, head to the [Solid Android Client](client-library.md) or [Solid Android API](api-library.md) docs for usage.

=== "Download the App"

    ## Install Android Solid Services

    Download the latest pre-built APK from the [GitHub Releases page](https://github.com/pondersource/Android-Solid-Services/releases).

    1. On your Android device, enable **Install from unknown sources** in Settings if needed.
    2. Open the downloaded `.apk` file and tap **Install**.
    3. Launch **Android Solid Services** and log in with your Solid pod credentials.

    Once you're logged in, any app using the `SolidAndroidClient` library can request access to your pod through ASS.

=== "Clone & Run Locally"

    ## Prerequisites

    - **JDK 17** (JBR v17.0.9 recommended). If builds fail, set `JAVA_HOME` to your JDK 17 path.
    - **Android Studio** (latest stable) or Android command-line tools.

    ## Clone

    ```sh
    git clone https://github.com/pondersource/Android-Solid-Services.git
    cd Android-Solid-Services
    ```

    ## Build

    ```sh
    # Debug APK — fastest for local development
    ./gradlew assembleDebug
    # Output: ./app/build/outputs/apk/debug/

    # Release APK
    ./gradlew assembleRelease
    ```

    Open the generated `.apk` in Android Studio or sideload it onto a device/emulator.

    ## Run Tests

    ```sh
    # Unit tests (all modules)
    ./gradlew test

    # Unit tests for a specific module
    ./gradlew :app:testDebugUnitTest
    ./gradlew :Shared:testDebugUnitTest

    # Instrumented tests (requires a connected device or running emulator)
    ./gradlew connectedAndroidTest
    ```

    ## Publish Libraries Locally

    Useful when testing library changes against a local consumer app before publishing:

    ```sh
    ./gradlew publishToMavenLocal
    ```

    Then reference them in your consumer app with `mavenLocal()` in your repositories block.

---

!!! question "Something not working?"
    Please [open an issue on GitHub](https://github.com/pondersource/Android-Solid-Services/issues). Include your Android/JDK version and the error output — it helps us fix things faster.
