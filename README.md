# Android Solid Services

[![Work In Progress](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExeGNuZWhuaTNtbHFnN20xZzY1bDgwN2hsN2N3YjA3dmo5MndxOWVobyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/mQWABflgJsuqmllEiF/giphy.gif)]()


## App
This app allows you to do single [Solid]() sign-in in Android ecosystem. Which will be used for other apps to comunicate through this app to do CRUD functions on the users' pod.

### How to use
This project built with an enhanced JDK (JBR v17.0.9).
So in case of having any problem during the build process, it is recommended to set your JAVA_HOME variable to your JBR v17.0.9 path or JDK 17 in general.
Gradle is the default build tool used in this project.
In the root directory of the project run command: 
```sh
./gradlew assembleDebug
```
You can find the generated ```.apk``` file in the path:
`/app/build/outputs/apk/debug`


## Library
This library has the functionality to do the authentication for your solid app on Android.

### How to use
For now, download the source code and add ```SolidAndroidClient``` module to your app mudules. The development is in proccess, soon it will available through Maven Central.
