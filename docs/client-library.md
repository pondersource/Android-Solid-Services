# Solid Android Client Library

The **Solid Android Client** library lets third-party Android apps plug into the Solid ecosystem by talking to the Android Solid Services host app over IPC. Your app never handles tokens or pod credentials — ASS does that for you.

**Use this when:**

- You're building an app that works alongside the Android Solid Services host app
- You want the user's Solid session managed centrally (single sign-in)

**Otherwise, see the [Solid Android API](api-library.md)** if you need direct Solid server access.

## Installation

```kotlin
// build.gradle.kts (module level)
android {
    defaultConfig {
        manifestPlaceholders["appAuthRedirectScheme"] = "YOUR_APP_PACKAGE_NAME"
    }
}
dependencies {
    implementation("com.pondersource.solidandroidclient:solidandroidclient:0.3.1")
}
```

[View on Maven Central](https://central.sonatype.com/artifact/com.pondersource.solidandroidclient/solidandroidclient)

---

## Entry Point: `Solid`

All three clients are obtained from the `Solid` companion object. Each is a process-scoped singleton.

```kotlin
val signInClient      = Solid.getSignInClient(context)
val resourceClient    = Solid.getResourceClient(context)
val contactsModule    = Solid.getContactsDataModule(context)
```

---

## SolidSignInClient

Manages authorization between your app and ASS. Obtain via `Solid.getSignInClient(context)`.

### Connection state

All IPC services expose a `Flow<Boolean>` that emits `true` once the bound service connects and `false` if it drops. Always wait for `true` before calling any methods.

```kotlin
signInClient.authServiceConnectionState().collect { connected ->
    if (connected) {
        // safe to call sign-in methods
    }
}
```

### Methods

```kotlin
// Emits connection state of the auth IPC service
fun authServiceConnectionState(): Flow<Boolean>

// Returns a SolidSignInAccount if this app is already authorized, null if not.
// Throws SolidException if ASS is not installed, not connected, or no user is logged in.
fun getAccount(): SolidSignInAccount?

// Prompts the user (inside ASS) to grant or deny this app access.
// callBack: (true, null) = granted | (false, null) = denied | (null, error) = error
fun requestLogin(callBack: (Boolean?, SolidException?) -> Unit)

// Revokes this app's access grant.
fun disconnectFromSolid(callBack: (Boolean) -> Unit)
```

### Full auth example

```kotlin
val signInClient = Solid.getSignInClient(context)

signInClient.authServiceConnectionState().collect { connected ->
    if (connected) {
        val account = signInClient.getAccount()
        if (account == null) {
            signInClient.requestLogin { granted, error ->
                when {
                    error != null  -> showError(error)
                    granted == true -> proceedToApp()
                    else           -> showDeniedMessage()
                }
            }
        } else {
            proceedToApp()
        }
    }
}
```

---

## SolidResourceClient

Reads, creates, updates, and deletes resources on the user's Solid pod via IPC. Obtain via `Solid.getResourceClient(context)`.

All methods are `suspend` functions. They throw a subclass of `SolidResourceException` on failure.

```kotlin
// Emits connection state of the resource IPC service
fun resourceServiceConnectionState(): Flow<Boolean>

// Fetch the authenticated user's WebID document
suspend fun getWebId(): WebId

// Read a resource. clazz must extend RDFSource or NonRDFSource.
suspend fun <T : Resource> read(resourceUrl: String, clazz: Class<T>): T

// Create a new resource on the pod
suspend fun <T : Resource> create(resource: T): T

// Replace an existing resource
suspend fun <T : Resource> update(resource: T): T

// Delete a resource
suspend fun <T : Resource> delete(resource: T): T
```

### Resource types

| Base class     | Use for                                     |
|----------------|---------------------------------------------|
| `RDFSource`    | Structured RDF data (Turtle, JSON-LD, etc.) |
| `NonRDFSource` | Raw files — images, text, binary            |

### Example

```kotlin
val resourceClient = Solid.getResourceClient(context)

resourceClient.resourceServiceConnectionState().collect { connected ->
    if (connected) {
        try {
            val note = resourceClient.read(
                "https://yourpod.example/notes/hello.ttl",
                MyNote::class.java
            )
            val updated = resourceClient.update(note.copy(body = "updated text"))
        } catch (e: SolidException) {
            handleException(e)
        }
    }
}
```

---

## SolidContactsDataModule

Manages address books, contacts, and groups on the pod via IPC. Obtain via `Solid.getContactsDataModule(context)`.

All methods are `suspend` functions.

```kotlin
// Emits connection state of the contacts IPC service
fun contactsDataModuleServiceConnectionState(): Flow<Boolean>
```

### Address Books

```kotlin
suspend fun getAddressBooks(): AddressBookList?

suspend fun createAddressBook(
    title: String,
    isPrivate: Boolean = true,
    storage: String? = null,
    ownerWebId: String? = null,
    container: String? = null,
): AddressBook?

suspend fun getAddressBook(uri: String): AddressBook?

suspend fun deleteAddressBook(addressBookUri: String): AddressBook?
```

### Contacts

```kotlin
suspend fun createNewContact(
    addressBookUri: String,
    newContact: NewContact,
    groupUris: List<String> = emptyList(),
): FullContact?

suspend fun getContact(contactUri: String): FullContact?

suspend fun renameContact(contactUri: String, newName: String): FullContact?

suspend fun addNewPhoneNumber(contactUri: String, newPhoneNumber: String): FullContact?

suspend fun addNewEmailAddress(contactUri: String, newEmailAddress: String): FullContact?

suspend fun removePhoneNumber(contactUri: String, phoneNumber: String): FullContact?

suspend fun removeEmailAddress(contactUri: String, emailAddress: String): FullContact?

suspend fun deleteContact(addressBookUri: String, contactUri: String): FullContact?
```

### Groups

```kotlin
suspend fun createNewGroup(
    addressBookUri: String,
    title: String,
    contactUris: List<String> = emptyList(),
): FullGroup?

suspend fun getGroup(groupUri: String): FullGroup?

suspend fun deleteGroup(addressBookUri: String, groupUri: String): FullGroup?

suspend fun addContactToGroup(contactUri: String, groupUri: String): FullGroup?

suspend fun removeContactFromGroup(contactUri: String, groupUri: String): FullGroup?
```

---

## Exception Hierarchy

All client methods throw a subclass of `SolidException`:

```
SolidException
├── SolidAppNotFoundException           — ASS app is not installed
├── SolidServiceConnectionException     — IPC connection to ASS failed
├── SolidNotLoggedInException           — No user is logged in inside ASS
├── SolidServicesDrawPermissionDeniedException — ASS lacks overlay draw permission
└── SolidResourceException
    ├── NotSupportedClassException      — resource class doesn't extend RDFSource/NonRDFSource
    ├── NotPermissionException          — app not authorized to access this resource
    ├── NullWebIdException              — no WebID available for the request
    └── UnknownException                — unexpected server or protocol error
```

---

## Example App

[Solid Contacts](https://github.com/pondersource/Solid-Contacts) is a full open-source app built on this library, demonstrating multi-account contacts management on a Solid pod.
