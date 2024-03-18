# Android Solid Services

## App
This app allows you to do single [Solid]() sign-in in Android ecosystem. Which will be used for other apps to comunicate through this app to do CRUD functions on the users' pod.

### How to use
This project built with an enhanced JDK (JBR v17.0.9).
So in case of having any problem during the build process, it is recommended to set your JAVA_HOME variable to your JBR v17.0.9 path or JDK 17 in general.
Gradle is the default build tool used in this project. You need to install gradle wrapper with the help of this [link](https://docs.gradle.org/current/userguide/installation.html).

After this change your directory to the root of the project location. 
Run command:
```sh
gradlew assembleDebug
```
You can find the generated .apk file in the path:
`/app/build/outputs/apk/debug`


## Library
This library has the functionality to do the authentication for your solid app on Android.

### How to use
For now, download the source code and add ```SolidAndroidClient``` module to your app mudules. The development is in proccess, soon it will available through Maven Central.