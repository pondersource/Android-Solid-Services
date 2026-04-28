# Solid Android API Library

The **Solid Android API** library lets your app communicate with a Solid pod server directly — no Android Solid Services host app required. It handles OpenID Connect authentication with DPoP and exposes interfaces for resource management and the Contacts data module.

**Use this when:**

- You want to embed full Solid support inside your own app
- The Android Solid Services host app is unavailable or not applicable
- You need fine-grained control over auth flows and token management

**Otherwise, prefer the [Solid Android Client](client-library.md)** — it offloads auth and IPC to the host app.

## Installation

```kotlin
// build.gradle.kts (module level)
android {
    defaultConfig {
        manifestPlaceholders["appAuthRedirectScheme"] = "YOUR_APP_PACKAGE_NAME"
    }
}
dependencies {
    implementation("com.pondersource.solidandroidapi:solidandroidapi:0.3.1")
}
```

[View on Maven Central](https://central.sonatype.com/artifact/com.pondersource.solidandroidapi/solidandroidapi)

---

## Authenticator

Manages OpenID Connect sessions with DPoP (Demonstration of Proof-of-Possession) support, including multi-account handling.

```kotlin
val authenticator = Authenticator.getInstance(context)
```

### State flows

| Property               | Type                       | Description                           |
|------------------------|----------------------------|---------------------------------------|
| `activeProfileFlow`    | `StateFlow<Profile?>`      | The currently active user profile     |
| `loggedInProfilesFlow` | `StateFlow<List<Profile>>` | All signed-in profiles                |
| `isAuthorizedFlow`     | `StateFlow<Boolean>`       | Whether the active user is authorized |
| `activeWebIdFlow`      | `StateFlow<String?>`       | The active user's WebID               |

### Authentication flow

```kotlin
// 1. Build the intent that opens the browser for login
val (intent, error) = authenticator.createAuthenticationIntent(
    webId = "https://yourpod.example/profile/card#me",  // optional
    oidcIssuer = null,         // optional; derived from webId if omitted
    appName = "My App",
    redirectUri = "myapp://callback"
)
startActivity(intent)

// 2. In your Activity/Fragment, handle the redirect result
val result = authenticator.submitAuthorizationResponse(authResponse, authException)
// result is the WebID string on success, or null on failure
```

### All methods

```kotlin
// Create intent for browser-based login
suspend fun createAuthenticationIntent(
    webId: String? = null,
    oidcIssuer: String? = null,
    appName: String,
    redirectUri: String,
): Pair<Intent?, String?>

// Handle the redirect from the browser after login
suspend fun submitAuthorizationResponse(
    authResponse: AuthorizationResponse?,
    authException: AuthorizationException?,
): String?                          // returns WebID on success, null on failure

// Build logout intent for browser-based session termination
suspend fun getTerminationSessionIntent(
    webId: String,
    logoutRedirectUrl: String,
): Pair<Intent?, String?>

// Get (and optionally refresh) the current token
suspend fun getLastTokenResponse(
    webId: String,
    forceRefresh: Boolean = false,
): TokenResponse?

// Get auth headers (Authorization + DPoP) for a manual HTTP request
suspend fun getAuthHeaders(
    webId: String,
    httpMethod: String,
    uri: String,
): Map<String, String>

// Update the DPoP nonce (call this when the server returns a new nonce)
fun updateDPoPNonce(webId: String, nonce: String)

// Synchronous profile accessors
fun isUserAuthorized(): Boolean
fun getAllLoggedInProfiles(): List<Profile>
fun getProfile(webId: String): Profile
fun getActiveProfile(): Profile

// Account management (suspend)
suspend fun getActiveWebId(): String?
suspend fun setActiveWebId(webId: String)
suspend fun removeProfile(webId: String)
suspend fun removeAllProfiles()
```

---

## SolidResourceManager

Performs authenticated CRUD operations on Solid pod resources. All methods are `suspend` functions and return `SolidNetworkResponse<T>`.

```kotlin
val resourceManager = SolidResourceManager.getInstance(context)
```

### SolidNetworkResponse

```kotlin
sealed class SolidNetworkResponse<T> {
    data class Success<T>(val data: T)                              // operation succeeded
    data class Error<T>(val errorCode: Int, val errorMessage: String) // HTTP / server error
    data class Exception<T>(val exception: Throwable)               // unexpected exception
}
```

Convenience methods: `getOrThrow()`, `getOrNull()`, `getOrDefault(default)`.

### Methods

```kotlin
// Read a resource from the pod
suspend fun <T : Resource> read(
    webid: String,    // WebID of the authenticated user
    resource: URI,    // full URI of the resource
    clazz: Class<T>,  // expected type (must extend RDFSource or NonRDFSource)
): SolidNetworkResponse<T>

// Create a new resource on the pod
suspend fun <T : Resource> create(
    webid: String,
    resource: T,      // identifier on the resource determines the target URI
): SolidNetworkResponse<T>

// Replace an existing resource
suspend fun <T : Resource> update(
    webid: String,
    newResource: T,
): SolidNetworkResponse<T>

// Delete a resource
suspend fun <T : Resource> delete(
    webid: String,
    resource: T,
): SolidNetworkResponse<T>

// Recursively delete an LDP container and all its contents
suspend fun deleteContainer(
    webid: String,
    containerUri: URI,
): SolidNetworkResponse<Boolean>
```

### Resource types

Your data classes must extend one of:

| Class            | Use for                                     |
|------------------|---------------------------------------------|
| `RDFSource`      | Structured RDF data (Turtle, JSON-LD, etc.) |
| `NonRDFSource`   | Raw files — images, text, binary            |
| `SolidContainer` | LDP containers (directories on the pod)     |

### Example

```kotlin
val response = resourceManager.read(
    webid = "https://yourpod.example/profile/card#me",
    resource = URI("https://yourpod.example/data/note.ttl"),
    clazz = MyNote::class.java
)

when (response) {
    is SolidNetworkResponse.Success   -> display(response.data)
    is SolidNetworkResponse.Error     -> showError(response.errorCode, response.errorMessage)
    is SolidNetworkResponse.Exception -> handleException(response.exception)
}
```

---

## SolidContactsDataModule

Manages address books, contacts, and groups on a Solid pod using the [Solid Contacts spec](https://www.w3.org/TR/vcard-rdf/). All methods are `suspend` and return `DataModuleResult<T>`.

```kotlin
val contactsModule = SolidContactsDataModule.getInstance(context)
```

### Address Books

```kotlin
suspend fun getAddressBooks(ownerWebId: String): DataModuleResult<AddressBookList>

suspend fun createAddressBook(
    ownerWebId: String,
    title: String,
    isPrivate: Boolean = true,
    storage: String,
    container: String? = null,
): DataModuleResult<AddressBook>

suspend fun getAddressBook(ownerWebId: String, addressBookUri: String): DataModuleResult<AddressBook>

suspend fun renameAddressBook(
    ownerWebId: String,
    addressBookUri: String,
    newName: String,
): DataModuleResult<AddressBook>

suspend fun deleteAddressBook(ownerWebId: String, addressBookUri: String): DataModuleResult<AddressBook>
```

### Contacts

```kotlin
suspend fun createNewContact(
    ownerWebId: String,
    addressBookString: String,
    newContact: NewContact,
    groupStrings: List<String> = emptyList(),
): DataModuleResult<FullContact>

suspend fun getContact(ownerWebId: String, contactString: String): DataModuleResult<FullContact>

suspend fun renameContact(ownerWebId: String, contactString: String, newName: String): DataModuleResult<FullContact>

suspend fun addNewPhoneNumber(ownerWebId: String, contactString: String, newPhoneNumber: String): DataModuleResult<FullContact>

suspend fun addNewEmailAddress(ownerWebId: String, contactString: String, newEmailAddress: String): DataModuleResult<FullContact>

suspend fun removePhoneNumber(ownerWebId: String, contactString: String, phoneNumber: String): DataModuleResult<FullContact>

suspend fun removeEmailAddress(ownerWebId: String, contactString: String, emailAddress: String): DataModuleResult<FullContact>

suspend fun deleteContact(ownerWebId: String, addressBookUri: String, contactUri: String): DataModuleResult<FullContact>
```

### Groups

```kotlin
suspend fun createNewGroup(
    ownerWebId: String,
    addressBookString: String,
    title: String,
    contactUris: List<String> = emptyList(),
): DataModuleResult<FullGroup>

suspend fun getGroup(ownerWebId: String, groupString: String): DataModuleResult<FullGroup>

suspend fun deleteGroup(ownerWebId: String, addressBookString: String, groupString: String): DataModuleResult<FullGroup>

suspend fun addContactToGroup(ownerWebId: String, contactString: String, groupString: String): DataModuleResult<FullGroup>

suspend fun removeContactFromGroup(ownerWebId: String, contactString: String, groupString: String): DataModuleResult<FullGroup>
```
