# How It Works

This page walks through how Android Solid Services works at runtime — from a user logging in to a third-party app reading a pod resource. Understanding this helps you build apps that integrate correctly and handle edge cases gracefully.

---

## The Three-Layer Model

```mermaid
graph TD
    subgraph "Your device"
        A["Third-party app<br/>(uses client)"]
        B["Android Solid Services<br/>(host app)"]
    end
    C["Solid Pod Server<br/>(CSS, ESS, etc.)"]
    D["OpenID Provider<br/>(identity server)"]

    A -- "AIDL IPC\n(resource / contacts calls)" --> B
    B -- "HTTPS + DPoP\n(authenticated requests)" --> C
    B -- "OIDC auth flow\n(login / token refresh)" --> D
```

Your app **never talks directly to the pod**. It calls the ASS host app over Android IPC (AIDL), which holds the tokens and makes all authenticated HTTP requests on its behalf.

This design has two benefits:

- **Single sign-in** — the user logs in once; every app on the device reuses the same session.
- **Credential isolation** — access tokens never leave the ASS process; third-party apps cannot exfiltrate them.

---

## Authentication Flow

The login flow runs once per Solid account. ASS orchestrates the full OpenID Connect exchange with DPoP:

```mermaid
sequenceDiagram
    actor User
    participant App as Your App
    participant ASS as Android Solid Services
    participant Browser
    participant IDP as OpenID Provider
    participant Pod as Solid Pod

    App->>ASS: requestLogin(callback)
    ASS->>User: Show permission dialog
    User->>ASS: Approve
    ASS->>IDP: Fetch OIDC discovery doc<br/>(from WebID → issuer)
    ASS->>Browser: Open authorization URL
    Browser->>User: Show IDP login page
    User->>Browser: Enter credentials
    Browser->>ASS: Redirect with auth code
    ASS->>IDP: Exchange code → access + refresh tokens
    IDP-->>ASS: Tokens (bound to DPoP key)
    ASS->>Pod: First pod request (HEAD /profile)
    Pod-->>ASS: 200 OK
    ASS-->>App: callback(granted=true)
```

After login, ASS stores the tokens (access + refresh) in DataStore with Protobuf. Tokens are tied to the DPoP key pair that ASS holds; a stolen token is useless without the private key.

---

## DPoP: Why Tokens Are Bound to the App

Solid servers require [DPoP (Demonstration of Proof-of-Possession)](https://datatracker.ietf.org/doc/html/rfc9449). Every HTTP request carries two headers:

| Header | Content |
|--------|---------|
| `Authorization: DPoP <token>` | The access token issued by the IDP |
| `DPoP: <proof>` | A short-lived JWT, signed with a private key ASS generated at first launch, binding the token to this specific request (method + URI + timestamp) |

If the server returns a `DPoP-Nonce` header, ASS incorporates it into the next proof — preventing replay attacks. This negotiation happens automatically; your app doesn't need to know about it.

---

## IPC: How Your App Calls ASS

The `client` library binds to three Android services inside the ASS app:

```mermaid
sequenceDiagram
    participant App as Your App
    participant Client as client
    participant Binder as ASS AIDL Service
    participant RM as SolidResourceManager
    participant Pod as Solid Pod

    App->>Client: Solid.getResourceClient(context)
    Client->>Binder: bindService(ASSResourceService)
    Binder-->>Client: onServiceConnected
    Client-->>App: resourceServiceConnectionState emits true

    App->>Client: resourceClient.read(url, MyNote::class.java)
    Client->>Binder: AIDL call: read(url, className)
    Binder->>RM: resourceManager.read(webid, uri, clazz)
    RM->>Pod: GET /data/note.ttl<br/>Authorization: DPoP …<br/>DPoP: <proof>
    Pod-->>RM: 200 OK  (Turtle body)
    RM-->>Binder: SolidNetworkResponse.Success(note)
    Binder-->>Client: AIDL callback: onResult(note)
    Client-->>App: returns MyNote
```

The `Flow<Boolean>` connection state is essential: AIDL binding is asynchronous. Always collect it before calling methods — or you'll get a `SolidServiceConnectionException`.

---

## Multi-Account Routing

Since v0.3.0, ASS manages multiple logged-in Solid accounts. Since v0.4.0, the client library passes the target WebID on every call so ASS can route the request to the correct token set.

```mermaid
sequenceDiagram
    participant App as Your App
    participant ASS
    participant Pod1 as pod.example.org
    participant Pod2 as another.pod.net

    App->>ASS: read(webId="alice@pod.example.org", url)
    ASS->>Pod1: GET /data/file.ttl<br/>(token for alice)

    App->>ASS: read(webId="bob@another.pod.net", url)
    ASS->>Pod2: GET /data/other.ttl<br/>(token for bob)
```

Persist the WebID after login: `signInClient.getAccount()?.webId`. Pass it on every subsequent call.

---

## Resource Operations: What Happens Under the Hood

When your app calls `resourceClient.read(url, clazz)`, ASS:

1. Looks up the access token for the given WebID.
2. Refreshes it if expired (using the stored refresh token + a fresh DPoP proof).
3. Issues a `GET` with `Authorization: DPoP` and `DPoP` headers.
4. Parses the response body (Turtle, JSON-LD, or raw bytes) into your data class.
5. Returns `SolidNetworkResponse.Success(data)` or an error variant — never throws.

For `update()` and `patch()`, passing an `ifMatch` ETag from a prior `head()` or `read()` adds conditional write protection: the server rejects the write with `412 Precondition Failed` if someone else changed the resource since you last read it.

---

## Direct API Mode (no host app)

If you use `api` directly (no ASS host app), the flow is the same — but your app owns the auth state:

```mermaid
graph LR
    A["Your App"] -- "direct HTTPS + DPoP" --> B["Solid Pod"]
    A -- "OIDC" --> C["OpenID Provider"]
```

You call `Authenticator.getInstance(context)` and manage the token lifecycle yourself. Use this when you want a fully self-contained app or when ASS is unavailable.

---

## Access Grant Flow

Before a third-party app can read any resource, ASS requires an explicit grant from the user:

```mermaid
sequenceDiagram
    participant App as Third-party App
    participant ASS
    actor User

    App->>ASS: requestLogin(callback)
    ASS->>User: "App X wants access to your Solid pod"
    alt User approves
        User->>ASS: Tap "Allow"
        ASS->>ASS: Persist grant in DataStore
        ASS-->>App: callback(granted=true, null)
    else User denies
        User->>ASS: Tap "Deny"
        ASS-->>App: callback(granted=false, null)
    end
```

Grants are stored per-app in DataStore and shown in the ASS Settings page. The user can revoke them at any time. Your app can also revoke its own grant by calling `disconnectFromSolid()`.
