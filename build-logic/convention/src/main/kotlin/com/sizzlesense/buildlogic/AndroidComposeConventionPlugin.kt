package com.sizzlesense.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType

/**
 * Configures Jetpack Compose compiler plugin, build feature flag, and Compose Material 3 dependencies
 * on any Android application or library module.
 */
class AndroidComposeConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            val commonExtension: CommonExtension = extensions.findByType<ApplicationExtension>()
                ?: extensions.findByType<LibraryExtension>()
                ?: error(
                    "sizzlesense.android.compose requires com.android.application or com.android.library."
                )

            configureAndroidCompose(commonExtension)
        }
    }
}
