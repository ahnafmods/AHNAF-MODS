plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.ahnaf.mods"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ahnaf.mods"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "9.0.0"
    }
}
