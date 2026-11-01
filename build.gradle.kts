plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinCompose) apply false
    alias(libs.plugins.kotlinSerialization) apply false
}

buildscript {
    dependencies {
        classpath(libs.androidToolsBuildGradle)
        classpath(libs.kotlinGradlePlugin)
        classpath(libs.kotlinSerializationCompilerPlugin)
        classpath(libs.composeCompilerPlugin)
    }
    repositories {
        google()
        mavenCentral()
    }
}
