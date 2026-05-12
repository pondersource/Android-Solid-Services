# Changelog

All notable changes to this project are documented here.

## [0.4.0] — May 2026

### New API — `SolidResourceManager`

- `head(webid, uri)` — HTTP HEAD returns `SolidMetadata` (ETag, Content-Type, Content-Length, WAC-Allow, ACL link, Accept-Patch/Post/Put, Last-Modified, and more) without transferring the resource body. Useful for caching checks and permission discovery before a full read.
- `patch(webid, uri, patch)` / `patchRaw(webid, uri, n3Body)` — N3 Patch support for atomic partial updates to RDF resources. The typed overload accepts an `N3Patch` value; the raw overload accepts a pre-serialised `text/n3` string.
- `update()` now accepts `ifMatch` — pass the ETag from a prior `head` or `read` call for optimistic-concurrency protection (server returns 412 on version mismatch).
- `delete(webid, resourceUri: URI)` — delete a resource by URI directly, without reading it first.

### New Type — `N3Patch` (`com.erfangholami.androidsolidservices.shared.domain.crud`)

Type-safe DSL and diff-based factory for building Solid N3 Patch documents:

```kotlin
val patch = N3Patch.build {
    where(contactUri, VCARD.FN, variable = "oldName")
    deleteVar(contactUri, VCARD.FN, variable = "oldName")
    insertLiteral(contactUri, VCARD.FN, "Alice")
}

val patch = N3Patch.fromDiff(originalResource, modifiedResource)
```

### Authentication

- DPoP algorithm negotiation: the generator reads the server's `WWW-Authenticate` header and selects the best supported algorithm, improving compatibility with different pod servers.
- New `IdTokenVerifier` validates claims in received ID tokens.
- Fixed a race condition where token refresh conflicted with an in-flight DPoP nonce update.
- Fixed incorrect WebID extraction from the token response.
- Removed several crash points from the token exchange and session handling paths.

### Multi-account in `client`

Third-party apps must now pass the target WebID on resource and contacts calls. This enables correct IPC routing when the user has multiple Solid accounts active.

### Resource Operations

- ETag headers are now sent on PUT requests for optimistic concurrency.
- Resource URIs containing spaces or other special characters are now percent-encoded correctly.
- `delete` and `deleteContainer` paths unified; redundant network round-trips removed.

### Architecture

- Removed the Inrupt Java Client library; replaced with a custom `SolidHttpClient` (OkHttp-based). Significantly leaner dependency footprint.
- API Validator plugin added to all modules. The public API surface is tracked via `.api` files to prevent accidental binary-incompatible changes.
- All AIDL parcelable definitions consolidated in the `Shared` module.
- Release builds of the ASS app are now minified with ProGuard.
- Extended RDF vocabulary constants in the `Shared` module (`Solid`, `DC`, and others).

### Bug Fixes

- Fixed wrong Dublin Core namespace in the Contacts data module; old data is still readable.
- Fixed `ETag` header name casing.
- Fixed granted apps not persisting across process restarts.

### UI

- ASS now shows a dialog prompting users to grant the overlay draw permission when it is missing.
- Updated "Sign in with Solid" login screen.
- UI strings moved to Android string resources.

### Dependencies

- Updated several library versions across all modules.

---

## [0.3.1] — April 2026

- Fix saving accounts bug.

---

## [0.3.0] — April 2026

- Multi-account support — log in with multiple Solid accounts and switch between them from the Settings page.
- All resource and contacts data module methods are now Kotlin `suspend` functions.
- Resource operations return `SolidNetworkResponse<T>` (sealed: `Success`, `Error`, `Exception`); contacts operations return `DataModuleResult<T>`.
- New `SolidException` sealed class hierarchy with typed subclasses.
- Proper DPoP token support for all authenticated pod requests.
- Replaced Gson with `kotlinx.serialization`.
- Upgraded project JVM target from 11 to 17.
- Updated compile SDK from 35 to 36.
- GitHub Actions workflows for publishing libraries and the application.
- Implementation classes are now `internal`, exposing only the public SDK surface.

---

## [0.2.1] — December 2024

- Remove SolidCommunity.net from the login provider list.
- Add app screenshots to documentation.

---

## [0.2.0] — December 2024

- Full contacts management over IPC: create, read, rename, and delete address books, contacts, and groups stored on the pod.
- `Flow<Boolean>` connection state for all IPC services.
- Recursive deletion of LDP containers.
- Support for Solid type index registration (private and public).
- Apps can programmatically revoke their own access grant.
- Extracted common types into a standalone `Shared` library.
- Bug fixes: token saving on network error, app relogin flow, authentication edge cases.

---

## [0.1] — March 2024

Initial public release.

- OpenID Connect login with DPoP via AppAuth.
- Resource CRUD on a Solid pod over IPC (AIDL).
- `SolidContainer` support for listing and navigating containers.
- `getWebId()` exposed as an IPC service.
- Permission dialog and access grant management in the ASS app.
- First version of the `client` library.
