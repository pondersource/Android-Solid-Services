plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
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

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation("com.google.code.gson:gson:2.10.1")

    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.apicatalog:titanium-json-ld-jre8:1.4.1")
    implementation("org.glassfish:jakarta.json:2.0.1")

    //Solid Java Client Library
    implementation(platform("com.inrupt.client:inrupt-client-bom:1.1.0"))
    //implementation("com.inrupt.client:inrupt-client-api")
    implementation("com.inrupt.client:inrupt-client-solid")
    implementation("com.inrupt.client:inrupt-client-core")
    implementation("com.inrupt.client:inrupt-client-okhttp")
    implementation("com.inrupt.client:inrupt-client-openid")
    implementation("com.inrupt.client:inrupt-client-vocabulary")

    implementation("net.openid:appauth:0.11.1")
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