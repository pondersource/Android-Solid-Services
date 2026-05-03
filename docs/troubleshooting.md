# Troubleshooting

Common errors and how to fix them. If your issue isn't listed here, [open an issue on GitHub](https://github.com/pondersource/Android-Solid-Services/issues) — include your Android version, library version, and the full exception message.

---

## Installation & Connection

### `SolidAppNotFoundException`

**Cause:** The Android Solid Services host app is not installed on the device.

**Fix:** Have the user install ASS from the [GitHub Releases page](https://github.com/pondersource/Android-Solid-Services/releases) before your app makes any IPC call.

```kotlin
try {
    signInClient.requestLogin { granted, error -> ... }
} catch (e: SolidAppNotFoundException) {
    // redirect user to the ASS install page
}
```

---

### `SolidServiceConnectionException`

**Cause:** The IPC service bound successfully at the OS level but then disconnected unexpectedly, or you called a method before the `Flow<Boolean>` connection state emitted `true`.

**Fix:** Always gate calls behind the connection state flow:

```kotlin
resourceClient.resourceServiceConnectionState().collect { connected ->
    if (connected) {
        // safe to call resource methods here
    }
}
```

Do not call methods immediately after obtaining the client object — binding is asynchronous.

---

### `SolidServicesDrawPermissionDeniedException`

**Cause:** ASS needs the `SYSTEM_ALERT_WINDOW` (overlay draw) permission to show its permission dialog over your app. On Android 6+, this is a runtime permission that must be granted manually.

**Fix:** ASS will automatically show a dialog prompting the user to grant this permission when it's missing. If the user dismissed it, direct them to:
**Settings → Apps → Android Solid Services → Display over other apps → Allow**.

---

## Authentication

### Login browser opens but redirect never returns to ASS

**Cause:** The `appAuthRedirectScheme` manifest placeholder is missing or doesn't match your package name.

**Fix:** Add it to your module-level `build.gradle.kts`:

```kotlin
android {
    defaultConfig {
        manifestPlaceholders["appAuthRedirectScheme"] = "YOUR_APP_PACKAGE_NAME"
    }
}
```

The value must exactly match your application ID (e.g. `com.example.myapp`).

---

### `SolidNotLoggedInException`

**Cause:** No user is logged in to ASS, or the stored session has been fully invalidated (refresh token expired or revoked by the pod server).

**Fix:** In your app, check `signInClient.getAccount()` — if it returns `null`, call `requestLogin()` again to start a new auth flow.

---

### Token refresh fails silently / requests return 401

**Cause:** The DPoP nonce returned by the server was not updated before a retry, or the refresh token was revoked.

**Fix:** The client handles nonce rotation automatically. If you're calling `SolidAndroidApi` directly and see `401 Unauthorized`:

1. Check for a `DPoP-Nonce` header in the 401 response and call `authenticator.updateDPoPNonce(webId, nonce)`.
2. Then retry the request — ASS does this automatically for you when using `SolidAndroidClient`.

If the token is fully revoked, call `getLastTokenResponse(webId, forceRefresh = true)` or re-authenticate.

---

### "unsupported_algorithm" error during login

**Cause:** The pod server only supports certain DPoP signing algorithms and the client proposed one it doesn't accept.

**Fix:** This is handled automatically since v0.4.0 — the DPoP generator reads the server's `WWW-Authenticate` response and negotiates the best supported algorithm. Upgrade to `0.4.0` or later if you see this on an older version.

---

## Resource Operations

### `412 Precondition Failed`

**Cause:** You passed an `ifMatch` ETag to `update()` or `patch()`, but the resource was modified by someone else since you last read it — a lost-update was correctly prevented.

**Fix:** Re-read the resource to get the latest ETag and version, merge your changes, and retry:

```kotlin
val latest = resourceManager.read(webid, uri, MyNote::class.java)
    .getOrThrow()
val merged = mergeChanges(latest, myChanges)
resourceManager.update(webid, merged, ifMatch = latest.etag)
```

---

### `409 Conflict` on `create()`

**Cause:** A resource already exists at the target URI. `create()` uses a conditional `PUT` with `If-None-Match: *`, which the server rejects if the URI is taken.

**Fix:** Use `update()` to overwrite, or choose a different URI. If you're generating URIs dynamically (e.g. using a UUID), the collision probability is negligible.

---

### `403 Forbidden` on resource access

**Cause:** The authenticated user does not have the required WAC/ACP permission on the requested resource.

**Fix:** Use `head()` to inspect the `WAC-Allow` header before attempting a write:

```kotlin
val meta = resourceManager.head(webid, uri).getOrNull()
val allowed = meta?.wacAllow  // contains read/write/append/control booleans
```

If access should be granted, check the ACL/ACP policy on the pod server side.

---

### Resource URI with spaces returns 404 or 400

**Cause:** On versions before `0.4.0`, resource URIs were not percent-encoded, causing requests for URIs with spaces or special characters to fail.

**Fix:** Upgrade to `0.4.0` or later. URIs are now automatically percent-encoded. If you stored broken URIs in your app, re-derive them with `URI(rawUri).toASCIIString()`.

---

## Contacts Data Module

### Contact names appear empty after upgrade from an older version

**Cause:** Versions before `0.4.0` used the wrong Dublin Core (`dc:`) namespace for some contact fields. Old data used the incorrect IRI.

**Fix:** The `0.4.0` data module reads both the correct and legacy namespace, so existing data is still visible without migration.

---

### `getAddressBooks()` returns an empty list despite having address books on the pod

**Cause:** The type index on the pod may not have been updated when address books were created by another client.

**Fix:** Ensure the pod's type index is populated. Some Solid servers require the creating client to register resources in the type index. Check the pod's type index resource manually if needed.

---

## Build & Gradle

### `NullPointerException` during Gradle configuration for `SolidAndroidApi`

**Cause:** The `key_generator_alias` property is missing from `gradle.properties`.

**Fix:** Add it to your project's `gradle.properties`:

```properties
key_generator_alias="SolidAndroidApiKeyGenAlias"
```

---

### `DuplicateClassException` at runtime involving `okhttp` or `kotlin-stdlib`

**Cause:** Multiple versions of OkHttp or the Kotlin stdlib are on the classpath.

**Fix:** Add an explicit resolution strategy to your root `build.gradle.kts`:

```kotlin
configurations.all {
    resolutionStrategy {
        force("com.squareup.okhttp3:okhttp:5.3.2")
        force("org.jetbrains.kotlin:kotlin-stdlib:2.x.x")
    }
}
```

Run `./gradlew dependencies` to inspect the full dependency tree.

---

!!! question "Still stuck?"
    [Open an issue](https://github.com/pondersource/Android-Solid-Services/issues) with your Android version, library version (`0.x.x`), the full stack trace, and steps to reproduce. The more detail you include, the faster we can help.
