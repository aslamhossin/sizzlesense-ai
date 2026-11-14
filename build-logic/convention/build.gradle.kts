import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.sizzlesense.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.androidToolsBuildGradle)
    compileOnly(libs.kotlinGradlePlugin)
    compileOnly(libs.composeCompilerPlugin)
    compileOnly(libs.kspGradlePlugin)
    compileOnly(libs.daggerHiltAndroidGradlePlugin)
    compileOnly(libs.kotlinSerializationCompilerPlugin)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "sizzlesense.android.application"
            implementationClass = "com.sizzlesense.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "sizzlesense.android.library"
            implementationClass = "com.sizzlesense.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "sizzlesense.android.compose"
            implementationClass = "com.sizzlesense.buildlogic.AndroidComposeConventionPlugin"
        }
        register("androidHilt") {
            id = "sizzlesense.android.hilt"
            implementationClass = "com.sizzlesense.buildlogic.AndroidHiltConventionPlugin"
        }
        register("kotlinSerialization") {
            id = "sizzlesense.kotlin.serialization"
            implementationClass = "com.sizzlesense.buildlogic.KotlinSerializationConventionPlugin"
        }
    }
}
