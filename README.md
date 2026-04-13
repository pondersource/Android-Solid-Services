This project consists of three parts:
- [Android Solid Services app](#android-solid-services-app)
- [Solid Android Client library](#solid-android-client-library)
- [Solid Android API library](#solid-android-api-library)

## What's New in v0.3.0

- **Multi-account support**: Log in with multiple Solid accounts and switch between them from the Settings page.
- **Suspend functions**: All resource and contacts data module methods are now Kotlin `suspend` functions instead of callback-based, for cleaner coroutine integration.
- **Unified result types**: Resource operations use `SolidNetworkResponse<T>` (sealed class with `Success`, `Error`, `Exception`) and contacts data module uses `DataModuleResult<T>` for consistent error handling.
- **Structured exceptions**: A new `SolidException` sealed class hierarchy provides typed exceptions (`SolidAppNotFoundException`, `SolidServiceConnectionException`, `SolidNotLoggedInException`, `SolidResourceException`, etc.).
- **DPoP authentication**: Proper DPoP (Demonstration of Proof-of-Possession) token support for authenticated requests.
- **kotlinx.serialization**: Replaced Gson with kotlinx.serialization for better Kotlin compatibility.
- **JVM 17**: Upgraded project JVM target from 11 to 17.
- **Compile SDK 36**: Updated compile SDK from 35 to 36.
- **CI/CD**: Added GitHub Actions workflows for publishing libraries and the application.
- **Internal API encapsulation**: Implementation classes are now `internal`, exposing only the public SDK surface.


## Android Solid Services app
This app allows you to do single [Solid](https://solidproject.org/) sign-in in Android ecosystem. It is used by other apps to communicate through this app to do access resources requests, resource management and contacts data modules on the already-logged-in users' pod.
The app supports multiple Solid accounts — you can log in with different pod providers and switch between them from the Settings page.
You can download the app from [here](https://github.com/pondersource/Android-Solid-Services/releases) at the moment.

### How to use locally
This project requires JDK 17 (JBR v17.0.9 recommended).
In case of having any problem during the build process, set your `JAVA_HOME` variable to your JDK 17 path.
Gradle is the default build tool used in this project.
In the root directory of the project run command:
```sh
./gradlew assembleDebug
```
You can find the generated ```.apk``` file in the path:
`./app/build/outputs/apk/debug`

You can open it for instance with Android Studio. It will take a while for the emulator to start up but then you'll be presented with a login screen
and you can log in to your pod there. Here are some screenshots from the application:
| ![Screenshot_20241218_152823](https://github.com/user-attachments/assets/77932c43-7cec-4a7c-96c7-fb1f59e1e92b) | ![Screenshot_20241218_152854](https://github.com/user-attachments/assets/543b2d9e-2f51-481f-b50d-934ece61172f) | ![Screenshot_20241218_152906](https://github.com/user-attachments/assets/854ad425-1db3-4284-bf89-79ba7490303f) | ![Screenshot_20241218_152952](https://github.com/user-attachments/assets/b6df9725-321d-4572-b9fa-07cf28de3e9a) |
|-|-|-|-|


## Solid Android Client library
This android library has the responsibility to check your app already has access grant, request to access the pod resources, resource management requests and access data modules (currently Contacts data module).
You can add this library to your android project by adding this line to your module-level ```build.gradle.kts``` file:
```gradle
dependencies {
    ...
    implementation("com.pondersource.solidandroidclient:solidandroidclient:0.3.0")
}
```
or if you are using another building system, check [here](https://central.sonatype.com/artifact/com.pondersource.solidandroidclient/solidandroidclient).


All the requests are handled by Android Inter-process Communication with Android Solid Services app. Before using any service you check if the service is already connected and then do your requests.
For the authentication you can use this code:
```kotlin
val solidSignInClient = Solid.getSignInClient(context)
solidSignInClient.authServiceConnectionState().collect { hasConnected ->
    if(hasConnected) {
        //Auth service has connected
        
        //This code returns your account if you already have access, null if you don't have access.
        val account = solidSignInClient.getAccount()
        
        if (account == null) {
            solidSignInClient.requestLogin { granted, exception ->
                if (exception == null) {
                    if (granted == true) {
                        //User gave access grant to your app
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
Resources must inherit from ```com.pondersource.shared.resource.Resource``` or for better implementation, ```com.pondersource.shared.RDFSource``` or ```com.pondersource.shared.NonRDFSource```.
Depending on your data class, you can choose one. NonRDFSource is used for raw files and resources without structure such as .txt, image files, etc. However, RDFSource is used for data classes in RDF format (common in Solid ecosystem).

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
    val resource = resourceClient.read(RESOURCE_URL, YOUR_CLASS::class.java)

    //Create
    val created = resourceClient.create(RESOURCE_OBJ)

    //Update - for already existing resource
    val updated = resourceClient.update(RESOURCE_OBJ)

    //Delete - for already existing resource
    val deleted = resourceClient.delete(RESOURCE_OBJ)
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

val addressBooks = contactsDataModule.getAddressBooks()
val addressBook = contactsDataModule.getAddressBook(ADDRESSBOOK_URI)
val contact = contactsDataModule.getContact(CONTACT_URI)
val group = contactsDataModule.getGroup(GROUP_URI)
//Check the module class for more functions on address books, contacts and groups.

```

For seeing some examples, you can refer to [Solid Contacts app](https://github.com/pondersource/Solid-Contacts) which works with Solid Contacts data module based on this library.

## Solid Android API library
This library is used in the Android Solid Services app to interact with Solid. In case you have any problem with installing the app or want to connect to Solid directly you can add it to your android project by adding this line to your module level ```build.gradle.kts``` file.

```gradle
dependencies {
    ...
    implementation("com.pondersource.solidandroidapi:solidandroidapi:0.3.0")
}
```
or if you are using another building system, check [here](https://central.sonatype.com/artifact/com.pondersource.solidandroidapi/solidandroidapi).

For authentication, you can use: ```com.pondersource.solidandroidapi.AuthenticatorImplementation.getInstance(context)```. 
There are couple of steps to authenticate user with OpenID Connect protocol (with DPoP support) such as register your app to OpenID and then ask for the intent to transfer the user to browser to enter their username/password of the selected IDP. For a better understanding please refer to Android Solid Services app codes. 

After authenticating successfully, you can interact with Solid resources and data modules similar to what have been explained in [Solid Android Client](#solid-android-client-library) section with the difference that you need to get the class instance with:
```kotlin
val resourceManager = com.pondersource.solidandroidapi.SolidResourceManagerImplementation.getInstance(context)
val contactModule = com.pondersource.solidandroidapi.SolidContactsDataModuleImplementation.getInstance(context)
```
---
For a better understanding of the project structure you can refer to this diagram:
![AndroidSolidServices (1)](https://github.com/user-attachments/assets/1b953cc9-3334-4827-9aac-63c29b4f7203)

## Acknowledgments
Thanks to funding from [NLnet](https://nlnet.nl/) <img src="https://nlnet.nl/logo/banner.svg" style="width: 5%; margin: 0 1% 0 1%;">
/ <img src="https://nlnet.nl/image/logos/NGI0Entrust_tag.svg" style="width: 5%; margin: 0 1% 0 1%;">

