plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.pondersource.androidsolidservices"
    compileSdk = 33
    defaultConfig {
        applicationId = "com.pondersource.androidsolidservices"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
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
    buildFeatures {
        viewBinding = true
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
                    "META-INF/ASL2.0"
                )
            )
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("com.google.code.gson:gson:2.10.1")

    //Solid Client Library
    implementation(platform("com.inrupt.client:inrupt-client-bom:1.2.0-SNAPSHOT"))
    implementation("com.inrupt.client:inrupt-client-api")
    implementation("com.inrupt.client:inrupt-client-solid")
    implementation("com.inrupt.client:inrupt-client-core")
    implementation("com.inrupt.client:inrupt-client-okhttp")
    implementation("com.inrupt.client:inrupt-client-jackson")
    implementation("com.inrupt.client:inrupt-client-jena")
    implementation("com.inrupt.client:inrupt-client-accessgrant")
    implementation("com.inrupt.client:inrupt-client-openid")
    implementation("com.inrupt.client:inrupt-client-uma")
    implementation("com.inrupt.client:inrupt-client-vocabulary")
    implementation("com.inrupt.client:inrupt-client-webid")
}