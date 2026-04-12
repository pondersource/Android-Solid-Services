This project consists of three parts:
- [Android Solid Services app](#android-solid-services-app)
- [Solid Android Client library](#solid-android-client-library)
- [Solid Android API library](#solid-android-api-library)


## Android Solid Services app
This app allows you to do single [Solid](https://solidproject.org/) sign-in in Android ecosystem. Which will be used for other apps to communicate through this app to do access resources requests, resource management and contacts data modules on the already-logged-in users' pod.
You can download the app from [here](https://github.com/pondersource/Android-Solid-Services/releases) at the moment.

### How to use locally
This project built with an enhanced JDK (JBR v17.0.9).
So in case of having any problem during the build process, it is recommended to set your JAVA_HOME variable to your JBR v17.0.9 path or JDK 17 in general.
Gradle is the default build tool used in this project.
In the root directory of the project run command:
```sh
./gradlew assembleDebug
```
You can find the generated ```.apk``` file in the path:
`./app/build/outputs/apk/debug`

You can open it for instance with Android Studio. It will take a while for the emulator to start up but then you'll be presented with a login screen
and and you can log in to your pod there. Here are some screenshots from the application:
| ![Screenshot_20241218_152823](https://github.com/user-attachments/assets/77932c43-7cec-4a7c-96c7-fb1f59e1e92b) | ![Screenshot_20241218_152854](https://github.com/user-attachments/assets/543b2d9e-2f51-481f-b50d-934ece61172f) | ![Screenshot_20241218_152906](https://github.com/user-attachments/assets/854ad425-1db3-4284-bf89-79ba7490303f) | ![Screenshot_20241218_152952](https://github.com/user-attachments/assets/b6df9725-321d-4572-b9fa-07cf28de3e9a) |
|-|-|-|-|


## Solid Android Client library
This android library has the responsibility to check your app already has access grant, request to access the pod resources, resource management requests and access data modules (currently Contacts data module).
you can add this library to your android project by adding this line to your module-level ```build.gradle.kts``` file :
```gradle
dependencies {
    ...
    implementation("com.pondersource.solidandroidclient:solidandroidclient:0.2.0")
}
```
or if you are using another building system, check [here](https://central.sonatype.com/artifact/com.pondersource.solidandroidclient/solidandroidclie).


All the requests handled by Android Inter-processes Communication with Android Solid Services app. Before using any service you check if the service is already connected and then do your requests.
For the authentication you can use this code:
```kotlin
val solidSignInClient = Solid.getSignInClient(context)
solidSignInClient.authServiceConnectionState().collect { hasConnected ->
    if(hasConnected) {
        //Auth service has connected
        
        //This code returens your account if you already have access, null if you don't have access.
        val account = solidSignInClient.getAccount()
        
        if (account == null) {
            solidSignInClient).requestLogin { granted, exception ->
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
Resources must inherited from ```com.pondersource.shared.resource.Resource``` or for better implementation, ```com.pondersource.shared.RDFSource``` or ```com.pondersource.shared.NonRDFSource```.
Depending on your data class, you can choose one. NonRDFSource used for raw files and resources without structure such as .txt, image files and etc. However, RDFSource used for data classes in format of RDF (common in Solid ecosystem).
You can do the main four functions similar to this code:
```kotlin
val resourceClient = Solid.getResourceClient(context)

resourceClient.resourceServiceConnectionState().collect { hasConnected ->
    if(hasConnected) {
        //Service has connected and you can call methods below.
    } else {
        //Service has not been connected.
    }
}

//Read
resourceClient.read(RESOURCE_URL, YOUR_CLASS::class.java, object : SolidResourceCallback<YOUR_CLASS> {
    override fun onResult(result: YOUR_CLASS) {}

    override fun onError(exception: SolidException.SolidResourceException) {}
})

//Create
resourceClient.create(RESOURCE_OBJ, object : SolidResourceCallback<RESOURCE_OBJ_CLASS_TYPE> {
    override fun onResult(result: RESOURCE_OBJ_CLASS_TYPE) {}

    override fun onError(exception: SolidException.SolidResourceException) {}
})

//Update - for already existed resource
resourceClient.create(RESOURCE_OBJ, object : SolidResourceCallback<RESOURCE_OBJ_CLASS_TYPE> {
    override fun onResult(result: RESOURCE_OBJ_CLASS_TYPE) {}

    override fun onError(exception: SolidException.SolidResourceException) {}
})

//Delete - for already existed resource
resourceClient.delete(RESOURCE_OBJ, object : SolidResourceCallback<RESOURCE_OBJ_CLASS_TYPE> {
    override fun onResult(result: RESOURCE_OBJ_CLASS_TYPE) {}

    override fun onError(exception: SolidException.SolidResourceException) {}
})
```

In case your data classes are contacts, you can use contacts data modules functions:
```kotlin
val contactsDataModule = Solid.getContactsDataModule(context)

contactsDataModule.contactsDataModuleServiceConnectionState().collect { hasConnected ->
    if(hasConnected) {
        //Service has connected and you can call methods below.
    } else {
        //Service has not been connected.
    }
}

contactsDataModule.getAddressBooks()
contactsDataModule.getAddressBook(ADDRESSBOOK_URI)
contactsDataModule.getContact(CONTACT_URI)
contactsDataModule.getGroup(GROUP_URI)
//Check the module class for more functions on address books.

```

For seeing some examples, you can refer to [Solid Contacts app](https://github.com/pondersource/Solid-Contacts) which works with Solid Contacts data module based on this library.

## Solid Android API library
This library is used in the Android Solid Services app to interact with Solid. In case you have any problem with installing the app or want to connect to Solid directly you can add it to your android project by adding this line to your module level ```build.gradle.kts``` file.

```gradle
dependencies {
    ...
    implementation("com.pondersource.solidandroidapi:solidandroidapi:0.2.0")
}
```
or if you are using another building system, check [here](https://central.sonatype.com/artifact/com.pondersource.solidandroidapi/solidandroidapi).

For authentication, you can use: ```com.pondersource.solidandroidapi.AuthenticatorImplementation.getInstance(context)```. 
There are couple of steps to authenticate user with OpenId protocol such as register your app to OpenId and then ask for the intent to transfer the user to browser to enter their username/password of the selected IDP. For a better understanding please refer to Android Solid Services app codes. 

After authenticating successfully, you can interact with Solid resources and data modules similar to what have been explained in [Solid Android Client](#solid-android-client-library) section with the difference that need to get the class instance with:
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

