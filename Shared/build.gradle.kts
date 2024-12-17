import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.vanniktech.maven.publish)
    id("kotlin-parcelize")
    `maven-publish`
}

android {
    namespace = "com.pondersource.shared"
    compileSdk = 35

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
    buildFeatures {
        aidl = true
    }
}

dependencies {

    implementation(libs.androidx.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.android.material)
    implementation(libs.jetbrains.kotlinx.serialization.json)
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

mavenPublishing {
    configure(AndroidSingleVariantLibrary(
        variant = "release",
        sourcesJar = true,
        publishJavadocJar = true,
    ))
    coordinates("com.pondersource.shared", "shared", "0.2.0")

    pom {
        name.set("SolidAndroidShared")
        description.set("A set of classes needed for SolidAndroidServices and it's dependencies.")
        inceptionYear.set("2024")
        url.set("https://github.com/pondersource/Android-Solid-Services/")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/license/mit")
                distribution.set("https://opensource.org/license/mit")
            }
        }
        developers {
            developer {
                id.set("erfangholami")
                name.set("Erfan Gholami")
                url.set("https://github.com/erfangholami/")
            }
        }
        scm {
            url.set("https://github.com/pondersource/Android-Solid-Services/")
            connection.set("scm:git:git://github.com/pondersource/Android-Solid-Services.git")
            developerConnection.set("scm:git:ssh://git@github.com/pondersource/Android-Solid-Services.git")
        }
    }

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
}

/*
publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.pondersource.shared"
            artifactId = "shared"
            version = "0.2.0"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}*/
