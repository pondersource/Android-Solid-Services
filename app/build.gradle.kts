plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.parcelize)
    alias(libs.plugins.jetbrains.kotlin.compose.compiler)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.google.hilt.android)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.google.protobuf)
}

android {
    namespace = "com.pondersource.androidsolidservices"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.pondersource.androidsolidservices"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["appAuthRedirectScheme"] = "com.pondersource.androidsolidservices"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    packaging {
        resources {
            excludes.addAll(
                arrayListOf(
                    "META-INF/LICENSE",
                    "META-INF/NOTICE",
                    "META-INF/DEPENDENCIES",
                    "META-INF/LICENSE.txt",
                    "META-INF/NOTICE.txt",
                    "META-INF/NOTICE.md",
                    "META-INF/ASL2.0"
                )
            )
        }
    }
}


dependencies {

    implementation(libs.jetbrains.kotlin.stdlib)
    implementation(libs.jetbrains.kotlin.stdlib.jdk8)
    implementation(libs.jetbrains.kotlin.reflect)
    implementation(libs.jetbrains.kotlinx.coroutins.core)
    implementation(libs.jetbrains.kotlinx.coroutins.android)
    implementation(libs.jetbrains.kotlinx.serialization.json)

    implementation(libs.androidx.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.androidx.annotation)

    implementation(libs.androidx.constraintLayout)
    implementation(libs.androidx.legacy.support)
    implementation(libs.androidx.exifInterface)


    //DI - Hilt
    implementation(libs.google.hilt.android)
    ksp(libs.google.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.fragment)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    //Compose
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.tooling)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.compose.ui.google.fonts)

    //Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.lifecycle.runtime)

    //Local DataBase - Datasource
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.google.protobuf.javalite)
    implementation(libs.google.protobuf.kotlinlite)

    //Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.compose)

    //Worker Manager (work in the background)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.work.rxJava2)
    implementation(libs.androidx.work.gcm)
    androidTestImplementation(libs.androidx.work.testing)
    implementation(libs.androidx.work.multiProcess)

    //Testing
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    testImplementation(libs.junit)

    implementation(project(":SolidAndroidApi"))
    implementation(project(":SolidAndroidClient"))

    implementation("androidx.appcompat:appcompat:1.7.0")

}