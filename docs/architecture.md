# Architecture

## Module Dependency Graph

```
app  (com.erfangholami.androidsolidservices)
 ├── api   (com.erfangholami.androidsolidservices.api)
 │    └── Shared       (com.erfangholami.androidsolidservices.shared)
 └── client (com.erfangholami.androidsolidservices.client)
      └── Shared

Third-party apps
 └── client
      └── Shared
```

![Architecture diagram](https://github.com/user-attachments/assets/1b953cc9-3334-4827-9aac-63c29b4f7203)

---

## IPC: How Apps Communicate

Third-party apps **never talk directly to the Solid pod**. They bind to one of three AIDL services in the ASS app:

```
Third-party app                Android Solid Services app
─────────────────              ──────────────────────────────────
Solid.getSignInClient()   ──►  ASSAuthenticatorService   (login, grants)
Solid.getResourceClient() ──►  ASSResourceService        (CRUD on pod resources)
Solid.getContactsDataModule()  SolidDataModulesService   (contacts, address books)
```

Each service is an Android **bound service**. The client libraries expose `Flow<Boolean>` connection state so apps can react to connect/disconnect events in real time.

AIDL interface definitions (both parcelable types and service contracts) live in `Shared/src/main/aidl/`.

---

## Authentication: OpenID Connect + DPoP

The auth flow uses the **AppAuth** library (`net.openid:appauth`) and adds DPoP (Demonstration of Proof-of-Possession) token binding:

1. User enters their WebID or selects an OpenID provider.
2. ASS resolves the OIDC issuer from the WebID document.
3. A browser intent opens the provider's login page.
4. The provider redirects back to ASS with an authorization code.
5. ASS exchanges the code for access + refresh tokens.
6. Every subsequent pod request includes an `Authorization: DPoP <token>` header and a signed DPoP proof — preventing token replay attacks.

Multi-account state (profiles, tokens) is persisted with **DataStore + Protocol Buffers**.

---

## Module Breakdown

### Shared (`com.erfangholami.androidsolidservices.shared`)

Common types shared across all modules. Published implicitly as a transitive dependency.

| Area              | Contents                                                                                              |
|-------------------|-------------------------------------------------------------------------------------------------------|
| Resource model    | `Resource` → `RDFSource` / `NonRDFSource` → `SolidRDFSource` / `SolidNonRDFSource` / `SolidContainer` |
| Response type     | `SolidNetworkResponse<T>` — sealed class: `Success`, `Error`, `Exception`                             |
| Data module types | `AddressBook`, `AddressBookList`, `Contact`, `FullContact`, `NewContact`, `FullGroup`                 |
| Patch type        | `N3Patch` — type-safe DSL and diff factory for [Solid N3 Patch](https://solidproject.org/TR/protocol#n3-patch) documents |
| Vocabulary        | `LDP`, `VCARD`, `ACL`, `OWL`, `DC`, `RDFS`, `Solid` constants                                         |
| AIDL parcelables  | Parcelable wrappers for cross-process data transfer (all definitions consolidated here)               |

### api (`com.erfangholami.androidsolidservices.api`)

Direct Solid server communication. Used internally by the ASS app and available as a standalone library.

| Class                                                               | Role                                       |
|---------------------------------------------------------------------|--------------------------------------------|
| `Authenticator` / `AuthenticatorImplementation`                     | OIDC + DPoP auth, multi-account            |
| `SolidResourceManager` / `SolidResourceManagerImplementation`       | CRUD on pod resources                      |
| `SolidContactsDataModule` / `SolidContactsDataModuleImplementation` | Contacts data module                       |
| `DPoPGenerator`                                                     | Signs DPoP proof JWTs                      |
| `ProfileManager`                                                    | Persists and retrieves user profiles       |
| `WebIdResolver`                                                     | Resolves OIDC issuer from a WebID document |

### client (`com.erfangholami.androidsolidservices.client`)

IPC client library. No direct pod access — all calls are proxied through the ASS app.

| Class                      | Role                                                                               |
|----------------------------|------------------------------------------------------------------------------------|
| `Solid`                    | Entry point: `getSignInClient()`, `getResourceClient()`, `getContactsDataModule()` |
| `SolidSignInClient`        | Auth IPC client                                                                    |
| `SolidResourceClient`      | Resource CRUD IPC client                                                           |
| `SolidContactsDataModule`  | Contacts IPC client                                                                |
| `SolidException` hierarchy | Typed exceptions for all failure modes                                             |

### app (`com.erfangholami.androidsolidservices`)

The host application. Users interact with this; third-party apps bind to its services.

| Area           | Technology                                                                 |
|----------------|----------------------------------------------------------------------------|
| UI             | Jetpack Compose, Navigation Compose                                        |
| Architecture   | MVVM, ViewModel, Kotlin StateFlow                                          |
| DI             | Hilt                                                                       |
| Services       | `ASSAuthenticatorService`, `ASSResourceService`, `SolidDataModulesService` |
| Persistence    | DataStore + Protocol Buffers                                               |
| Auth           | `net.openid:appauth` + DPoP                                                |
| Solid protocol | Custom `SolidHttpClient` (OkHttp-based; Inrupt Java Client removed)        |

---

## Technology Summary

| Technology                     | Version / Notes                              |
|--------------------------------|----------------------------------------------|
| Kotlin                         | Coroutines, Flow, serialization              |
| Jetpack Compose                | UI — no XML views                            |
| Hilt                           | Dependency injection (app module only)       |
| AIDL                           | Cross-process communication                  |
| AppAuth (`net.openid:appauth`) | OpenID Connect                               |
| `SolidHttpClient`              | Custom OkHttp-based Solid HTTP client (replaces Inrupt Java Client SDK) |
| Titanium JSON-LD               | RDF, JSON-LD parsing                         |
| DataStore + Protobuf           | Local persistence                            |
| kotlinx.serialization          | JSON serialization (replaced Gson in v0.3.0) |
| API Validator plugin           | Binary API compatibility enforcement via `.api` files |
| Min SDK                        | 26 (Android 8.0)                             |
| Target / Compile SDK           | 33 / 35                                      |
| JVM target                     | 17                                           |
