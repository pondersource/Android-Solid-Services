plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.pondersource.shared"
    compileSdk = 34

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        manifestPlaceholders["appAuthRedirectScheme"] = namespace.toString()
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
        aidl = true
    }
}

dependencies {

    implementation(libs.androidx.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.android.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)

    api(libs.openid.appauth)
    api(libs.google.code.gson)

    api(platform(libs.inrupt.client.bom))
    api(libs.inrupt.client.solid)
    api(libs.inrupt.client.core)
    api(libs.inrupt.client.okhttp)
    api(libs.inrupt.client.openid)
    api(libs.inrupt.client.vocabulary)
    api(libs.titanium.json.ld.jre8)
    api(libs.glassfish.jakarta.json)
}