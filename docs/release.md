# Releases

All releases are published on the [GitHub Releases page](https://github.com/pondersource/Android-Solid-Services/releases).

Library versions are published to Maven Central:

- [`solidandroidclient`](https://central.sonatype.com/artifact/com.pondersource.solidandroidclient/solidandroidclient)
- [`solidandroidapi`](https://central.sonatype.com/artifact/com.pondersource.solidandroidapi/solidandroidapi)

---

## v0.4.0 — May 2026

### New API — `SolidResourceManager`

- **`head(webid, uri)`** — HTTP HEAD returns a `SolidMetadata` object (ETag, Content-Type, Content-Length, WAC-Allow, ACL link, Accept-Patch/Post, Last-Modified, and more) without transferring the resource body. Ideal for caching checks and permission discovery before a full read.
- **`patch(webid, uri, patch)` / `patchRaw(webid, uri, n3Body)`** — N3 Patch support for atomic partial updates to RDF resources. The typed overload accepts an `N3Patch` value; the raw overload accepts a pre-serialised `text/n3` string.
- **`update()` now accepts `ifMatch`** — pass the ETag from a prior `head` or `read` call for optimistic-concurrency protection (server returns 412 on version mismatch).
- **`delete(webid, resourceUri: URI)`** — delete a resource by URI directly, without reading it first.

### New Type — `N3Patch`

Type-safe DSL and diff-based factory for building [Solid N3 Patch](https://solidproject.org/TR/protocol#n3-patch) documents:

```kotlin
// DSL builder
val patch = N3Patch.build {
    where(contactUri, VCARD.FN, variable = "oldName")
    deleteVar(contactUri, VCARD.FN, variable = "oldName")
    insertLiteral(contactUri, VCARD.FN, "Alice")
}

// Auto-diff from two resource states
val patch = N3Patch.fromDiff(originalResource, modifiedResource)
```

### Authentication

- **DPoP algorithm negotiation** — the DPoP generator now reads the `WWW-Authenticate` response header and selects the best algorithm the server supports; improves compatibility with different pod implementations.
- **ID token verification** — new `IdTokenVerifier` validates claims in the received ID token.
- **DPoP nonce conflict fix** — token refresh no longer races with an in-flight nonce update.
- **WebID parsing fix** — correctly extracts the WebID string from the token response.
- **Crash fixes** — multiple crash points removed from the token exchange and session handling paths.

### Multi-account in `SolidAndroidClient`

Third-party apps must now pass the target WebID on each resource and contacts call. This enables per-account IPC routing when the user has multiple Solid accounts logged in.

### Resource Operations

- **ETag on writes** — PUT requests now include `ETag` headers for optimistic concurrency.
- **Special characters in URIs** — resource URIs containing spaces and other characters are now percent-encoded correctly.
- **Unified delete** — `delete` and `deleteContainer` paths merged; redundant network round-trips removed.

### Architecture

- **Removed Inrupt Java Client library** — replaced with a custom `SolidHttpClient`; significantly leaner dependency footprint.
- **API binary compatibility enforcement** — API Validator plugin added to all modules; the public surface is tracked via `.api` files to prevent accidental breakage.
- **AIDL consolidated in `Shared`** — all parcelable definitions moved to the `Shared` module; no more duplication across modules.
- **ProGuard + minification** — release builds of the ASS app are now minified.
- **Extended vocabulary** — new RDF vocabulary constants added to the `Shared` module for broader developer use.

### Bug Fixes

- Fixed wrong Dublin Core namespace in the Contacts data module; old data using the incorrect namespace is still readable.
- Fixed `ETag` header name casing.
- Fixed granted apps not persisting across process restarts.

### UI

- ASS now shows a dialog prompting users to grant the overlay draw permission when it is missing.
- Updated "Sign in with Solid" login screen.
- UI strings moved to Android string resources.

### Dependencies

- Updated several library versions across all modules.

---

## v0.3.1 — April 2026

- Fix saving accounts bug.

---

## v0.3.0 — April 2026

- **Multi-account support** — log in with multiple Solid accounts and switch between them from the Settings page.
- **Suspend functions** — all resource and contacts data module methods are now Kotlin `suspend` functions instead of callback-based, for cleaner coroutine integration.
- **Unified result types** — resource operations return `SolidNetworkResponse<T>` (sealed: `Success`, `Error`, `Exception`); contacts operations return `DataModuleResult<T>`.
- **Structured exceptions** — new `SolidException` sealed class hierarchy with typed subclasses (`SolidAppNotFoundException`, `SolidServiceConnectionException`, `SolidNotLoggedInException`, `SolidResourceException`, etc.).
- **DPoP authentication** — proper DPoP (Demonstration of Proof-of-Possession) token support for all authenticated pod requests.
- **kotlinx.serialization** — replaced Gson for better Kotlin compatibility.
- **JVM 17** — upgraded project JVM target from 11 to 17.
- **Compile SDK 36** — updated compile SDK from 35 to 36.
- **CI/CD** — GitHub Actions workflows for publishing libraries and the application.
- **Internal API encapsulation** — implementation classes are now `internal`, exposing only the public SDK surface.

---

---

## v0.2.1 — December 2024

- Remove SolidCommunity.net from the login provider list (simplify provider options).
- Add app screenshots to documentation.

---

## v0.2.0 — December 2024

- **Contacts data module** — full contacts management over IPC: create, read, rename, and delete address books, contacts, and groups stored on the pod.
- **Suspend functions for contacts** — all contacts data module methods converted from callbacks to `suspend` functions.
- **Service connection flow** — added `Flow<Boolean>` connection state for all IPC services so apps can react to connect/disconnect events.
- **Container deletion** — recursive deletion of LDP containers and their contents.
- **Private and public type indexes** — support for Solid type index registration.
- **Disconnect from Solid** — apps can now programmatically revoke their own access grant.
- **Shared module** — extracted common types (resource model, AIDL parcelables, contacts types) into a standalone `Shared` library.
- **Bug fixes** — token saving on network error, app relogin flow, authentication edge cases, reading RDF resources as NonRDF.

---

## v0.1 — March 2024

Initial public release.

- **Authentication** — OpenID Connect login with Inrupt and SolidCommunity.net identity providers; browser-based auth flow via AppAuth.
- **DPoP** — DPoP authentication headers on all pod requests.
- **Resource CRUD** — read, create, update, and delete resources on a Solid pod over IPC (AIDL).
- **SolidContainer** — support for listing and navigating pod containers (directories).
- **WebID** — `getWebId()` exposed as an IPC service.
- **Access grants** — permission dialog in ASS when a third-party app requests access; grants tracked in the Settings page.
- **ASS app** — initial Jetpack-based UI with login, settings, and access grant management.
- **Client library** — first version of `SolidAndroidClient` connecting to ASS over AIDL.

---

!!! question "Found a bug or want to request a feature?"
    Please [open an issue on GitHub](https://github.com/pondersource/Android-Solid-Services/issues). Include your device/emulator Android version, library version, and any relevant error output.
