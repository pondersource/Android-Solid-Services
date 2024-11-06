plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    `maven-publish`
}

android {
    namespace = "com.pondersource.solidandroidclient"
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

    viewBinding {
        enable = true
    }

    buildFeatures {
        aidl = true
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

    //Testing
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    testImplementation(libs.junit)

    implementation(libs.androidx.appcompat)
    implementation(libs.google.android.material)
    implementation(libs.jetbrains.kotlinx.coroutins.android)

    /*implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    implementation("com.squareup.okhttp3:okhttp")
    */

    implementation(libs.openid.appauth)
    implementation(libs.google.code.gson)

    implementation(platform(libs.inrupt.client.bom))
    implementation(libs.inrupt.client.solid)
    implementation(libs.inrupt.client.core)
    implementation(libs.inrupt.client.okhttp)
    implementation(libs.inrupt.client.openid)
    implementation(libs.inrupt.client.vocabulary)
    implementation(libs.titanium.json.ld.jre8)
    implementation(libs.glassfish.jakarta.json)
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.pondersource.solidandroidclient"
            artifactId = "solidandroidclient"
            version = "0.1"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}