import java.util.Properties

plugins {
    id("com.android.application")
}

// Release signing comes from env vars on CI and from the shared keystore config
// in ~/android-release for local builds.
val localSigning = Properties().apply {
    val file = File(System.getProperty("user.home"), "android-release/signing.properties")
    if (file.isFile) file.inputStream().use { load(it) }
}

fun signingSetting(env: String, property: String): String? =
    System.getenv(env)?.takeIf { it.isNotBlank() }
        ?: localSigning.getProperty(property)?.takeIf { it.isNotBlank() }

val releaseStoreFile = signingSetting("ANDROID_KEYSTORE", "storeFile")
val releaseStorePassword = signingSetting("ANDROID_KEYSTORE_PASSWORD", "storePassword")
val releaseKeyAlias = signingSetting("ANDROID_KEY_ALIAS", "keyAlias")
val releaseKeyPassword = signingSetting("ANDROID_KEY_PASSWORD", "keyPassword")

android {
    namespace = "com.andunie.notes"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.andunie.notes"
        minSdk = 26
        targetSdk = 36
        // The release workflow derives these from a v<major>.<minor>[.<patch>] tag.
        versionCode = System.getenv("ANDROID_VERSION_CODE")?.toIntOrNull() ?: 1
        versionName = System.getenv("ANDROID_VERSION_NAME") ?: "1.0"
    }

    signingConfigs {
        if (releaseStoreFile != null) {
            create("release") {
                storeFile = file(releaseStoreFile)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            signingConfig = signingConfigs.findByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

tasks.matching { it.name == "assembleRelease" || it.name == "bundleRelease" }.configureEach {
    doFirst {
        check(releaseStoreFile != null) {
            "No release signing config found. Set ANDROID_KEYSTORE, ANDROID_KEYSTORE_PASSWORD, " +
                "ANDROID_KEY_ALIAS and ANDROID_KEY_PASSWORD, or create ~/android-release/signing.properties."
        }
    }
}
