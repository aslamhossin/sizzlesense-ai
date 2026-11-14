package com.sizzlesense.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.HasConfigurableKotlinCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions

/**
 * Configures shared Android SDK levels, Java 17 compatibility, test defaults, Kotlin JVM target,
 * and standard unit-test dependencies across application and library modules.
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension
) {
    commonExtension.apply {
        compileSdk = libs.findVersion("compileSdk").get().toString().toInt()

        defaultConfig.apply {
            minSdk = libs.findVersion("minSdk").get().toString().toInt()
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compileOptions.apply {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        testOptions.unitTests.isReturnDefaultValues = true
    }

    configureKotlinJvmTarget()

    dependencies {
        add("testImplementation", libs.findLibrary("junit").get())
        add("testImplementation", libs.findLibrary("coroutines-test").get())
    }
}

private fun Project.configureKotlinJvmTarget() {
    val kotlinExtension = extensions.findByName("kotlin") as? HasConfigurableKotlinCompilerOptions<*>
    val jvmOptions = kotlinExtension?.compilerOptions as? KotlinJvmCompilerOptions ?: return
    jvmOptions.jvmTarget.set(JvmTarget.JVM_17)
}
