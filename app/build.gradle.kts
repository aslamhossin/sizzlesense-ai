plugins {
    alias(libs.plugins.sizzlesenseAndroidApplication)
    alias(libs.plugins.sizzlesenseAndroidCompose)
    alias(libs.plugins.sizzlesenseAndroidHilt)
    alias(libs.plugins.sizzlesenseKotlinSerialization)
}

android {
    namespace = "com.sizzlesense.ai"

    defaultConfig {
        applicationId = "com.sizzlesense.ai"
        versionCode = 1
        versionName = "1.0.0"

        buildConfigField("String", "DEFAULT_BACKEND_URL", "\"http://192.168.11.6:8000\"")
        buildConfigField("String", "DEFAULT_TAPO_IP", "\"192.168.11.16\"")
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

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    implementation(libs.hilt.navigation.compose)
    implementation(libs.navigation3.runtime)
    implementation(libs.navigation3.ui)

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)

    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.rtsp)
    implementation(libs.media3.ui)
}
