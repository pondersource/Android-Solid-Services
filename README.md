[![solidandroidclient](https://img.shields.io/maven-central/v/com.pondersource.solidandroidclient/solidandroidclient.svg?label=solidandroidclient)](https://central.sonatype.com/artifact/com.pondersource.solidandroidclient/solidandroidclient)
[![solidandroidapi](https://img.shields.io/maven-central/v/com.pondersource.solidandroidapi/solidandroidapi.svg?label=solidandroidapi)](https://central.sonatype.com/artifact/com.pondersource.solidandroidapi/solidandroidapi)
[![Docs](https://img.shields.io/badge/docs-site-blueviolet)](https://androidsolidservices.erfangholami.com)
[![License](https://img.shields.io/github/license/pondersource/Android-Solid-Services)](LICENSE)

This project consists of three parts:

- [Android Solid Services app](#android-solid-services-app)
- [Solid Android Client library](#solid-android-client-library)
- [Solid Android API library](#solid-android-api-library)

## What's New in v0.4.0

- **`head()` resource metadata** — fetch ETag, Content-Type, WAC-Allow, and other headers via HTTP HEAD without downloading the resource body.
- **N3 Patch support** — `patch()` and the new `N3Patch` type (DSL builder + diff factory) enable atomic partial updates to RDF resources.
- **Conditional writes** — `update()` now accepts an `ifMatch` ETag for optimistic-concurrency protection.
- **Delete by URI** — `delete(webid, uri)` removes a resource without reading it first.
- **DPoP algorithm negotiation** — the token generator now picks the best algorithm the server supports, improving pod compatibility.
- **Removed Inrupt Java Client** — replaced with a custom `SolidHttpClient`; significantly lighter dependency footprint.
- **Bug fixes** — DPoP nonce race condition, WebID parsing, contacts DC namespace, ETag casing.

## Android Solid Services app

This app allows you to do single [Solid](https://solidproject.org/) sign-in in Android ecosystem. It
is used by other apps to communicate through this app to do access resources requests, resource
management and contacts data modules on the already-logged-in users' pod.
The app supports multiple Solid accounts — you can log in with different pod providers and switch
between them from the Settings page.
You can download the app
from [here](https://github.com/pondersource/Android-Solid-Services/releases) at the moment.

### How to use locally

This project requires JDK 17 (JBR v17.0.9 recommended).
In case of having any problem during the build process, set your `JAVA_HOME` variable to your JDK 17
path.
Gradle is the default build tool used in this project.
In the root directory of the project run command:

```sh
./gradlew assembleDebug
```

You can find the generated ```.apk``` file in the path:
`./app/build/outputs/apk/debug`

You can open it for instance with Android Studio. It will take a while for the emulator to start up, 
but then you'll be presented with a login screen,
and you can log in to your pod there. Here are some screenshots from the application:
|![Screenshot_20260414_00260](https://github.com/user-attachments/assets/9afe2f9d-f4a3-4e05-ae77-ab6e13febf84)|![Screenshot_20241218_152854](https://github.com/user-attachments/assets/543b2d9e-2f51-481f-b50d-934ece61172f)|![Screenshot_20260414_002657](https://github.com/user-attachments/assets/3deabd3a-d907-407a-9fbb-b27e26882206)|![Screenshot_20241218_152952](https://github.com/user-attachments/assets/b6df9725-321d-4572-b9fa-07cf28de3e9a)|
|-|-|-|-|

## Solid Android Client library

This android library has the responsibility to check your app already has access grant, request to
access the pod resources, resource management requests and access data modules (currently Contacts
data module).
You can add this library to your android project by adding this line to your module-level
```build.gradle.kts``` file:

```kotlin
// build.gradle.kts (module level)
android {
    defaultConfig {
        manifestPlaceholders["appAuthRedirectScheme"] = "YOUR_APP_PACKAGE_NAME"
    }
}
dependencies {
    implementation("com.pondersource.solidandroidclient:solidandroidclient:0.4.0")
}
```

or if you are using another building system,
check [here](https://central.sonatype.com/artifact/com.pondersource.solidandroidclient/solidandroidclient).

All the requests are handled by Android Inter-process Communication with Android Solid Services app.
Before using any service you check if the service is already connected and then do your requests.
For the authentication you can use this code:

```kotlin
val solidSignInClient = Solid.getSignInClient(context)
solidSignInClient.authServiceConnectionState().collect { hasConnected ->
    if(hasConnected) {
        //Auth service has connected
        
        //This code returns your account if you already have access, null if you don't have access.
        //You need to pass the webid you want to check
        val account = solidSignInClient.getAccount(SAVED_WEBID ?: "")
        
        if (account == null) {
            solidSignInClient.requestLogin { webid, exception ->
                if (exception == null) {
                    if (!webid.isNullOrEmpty()) {
                        //User gave access grant to your app and you need to save webid locally for future calls
                    } else {
                        //User declined your access grant request
                    }
                } else {
                    //Some error happened during the access request.
                }
            }
        }
    } else {
        //Auth service hasn't been connected and you can show a message to user
    }
}
```

After this step you can request for resources or data modules.
Resources must inherit from ```com.pondersource.shared.domain.resource.SolidResource``` or for better
implementation, ```com.pondersource.shared.domain.SolidRDFResource``` or
```com.pondersource.shared.domain.SolidNonRDFResource```.
Depending on your data class, you can choose one. NonRDFSource is used for raw files and resources
without structure such as .txt, image files, etc. However, RDFSource is used for data classes in RDF
format (common in Solid ecosystem).

All resource operations are `suspend` functions and throw `SolidException` on failure:

```kotlin
val resourceClient = Solid.getResourceClient(context)

resourceClient.resourceServiceConnectionState().collect { hasConnected ->
    if(hasConnected) {
        //Service has connected and you can call methods below.
    } else {
        //Service has not been connected.
    }
}

try {
    //Read
    val resource = resourceClient.read(SAVED_WEBID, RESOURCE_URL, YOUR_CLASS::class.java)

    //Create
    val created = resourceClient.create(SAVED_WEBID, RESOURCE_OBJ)

    //Update - for already existing resource
    val updated = resourceClient.update(SAVED_WEBID, RESOURCE_OBJ)

    //Delete - for already existing resource
    val deleted = resourceClient.delete(SAVED_WEBID, RESOURCE_OBJ)
} catch (e: SolidException) {
    // Handle error
}
```

In case your data classes are contacts, you can use contacts data module functions (also `suspend`):

```kotlin
val contactsDataModule = Solid.getContactsDataModule(context)

contactsDataModule.contactsDataModuleServiceConnectionState().collect { hasConnected ->
    if(hasConnected) {
        //Service has connected and you can call methods below.
    } else {
        //Service has not been connected.
    }
}

val addressBooks = contactsDataModule.getAddressBooks(SAVED_WEBID)
val addressBook = contactsDataModule.getAddressBook(SAVED_WEBID, ADDRESSBOOK_URI)
val contact = contactsDataModule.getContact(SAVED_WEBID, CONTACT_URI)
val group = contactsDataModule.getGroup(SAVED_WEBID, GROUP_URI)
//Check the module class for more functions on address books, contacts and groups.

```

For seeing some examples, you can refer
to [Solid Contacts app](https://github.com/pondersource/Solid-Contacts) which works with Solid
Contacts data module based on this library.

## Solid Android API library

This library is used in the Android Solid Services app to interact with Solid. In case you have any
problem with installing the app or want to connect to Solid directly you can add it to your android
project by adding this line to your module level ```build.gradle.kts``` file.

```kotlin
// build.gradle.kts (module level)
android {
    defaultConfig {
        manifestPlaceholders["appAuthRedirectScheme"] = "YOUR_APP_PACKAGE_NAME"
    }
}
dependencies {
    implementation("com.pondersource.solidandroidapi:solidandroidapi:0.4.0")
}
```

or if you are using another building system,
check [here](https://central.sonatype.com/artifact/com.pondersource.solidandroidapi/solidandroidapi).

For authentication, you can use:
```com.pondersource.solidandroidapi.auth.Authenticator.getInstance(context)```.
There are couples of steps to authenticate user with OpenID Connect protocol (with DPoP support) such
as register your app to OpenID and then ask for the intent to transfer the user to browser to enter
their username/password of the selected IDP. For a better understanding please refer to Android
Solid Services app codes.

After authenticating successfully, you can interact with Solid resources and data modules similar to
what have been explained in [Solid Android Client](#solid-android-client-library) section with the
difference that you need to get the class instance with:

```kotlin
val resourceManager = com.pondersource.solidandroidapi.resource.SolidResourceManager.getInstance(authenticator)
val contactModule = com.pondersource.solidandroidapi.datamodule.contacts.SolidContactsDataModule.getInstance(resourceManager)
```

---
For a better understanding of the project structure you can refer to this diagram:
![AndroidSolidServices (1)](https://github.com/user-attachments/assets/1b953cc9-3334-4827-9aac-63c29b4f7203)

## Acknowledgments

Thanks to funding
from [NLnet](https://nlnet.nl/) <img src="https://nlnet.nl/logo/banner.svg" style="width: 5%; margin: 0 1% 0 1%;">
/ <img src="https://nlnet.nl/image/logos/NGI0Entrust_tag.svg" style="width: 5%; margin: 0 1% 0 1%;">

