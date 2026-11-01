plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.ksp) apply false
}

buildscript {
    dependencies {
        classpath(libs.androidToolsBuildGradle)
        classpath(libs.kotlinGradlePlugin)
        classpath(libs.kspGradlePlugin)
        classpath(libs.daggerHiltAndroidGradlePlugin)
        classpath(libs.kotlinSerializationCompilerPlugin)
        classpath(libs.composeCompilerPlugin)
    }
    repositories {
        google()
        mavenCentral()
    }
}
