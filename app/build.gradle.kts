import java.io.File
import java.net.Inet4Address
import java.net.NetworkInterface

plugins {
    alias(libs.plugins.sizzlesenseAndroidApplication)
    alias(libs.plugins.sizzlesenseAndroidCompose)
    alias(libs.plugins.sizzlesenseAndroidHilt)
    alias(libs.plugins.sizzlesenseKotlinSerialization)
}

fun String.escapeForBuildConfigString(): String =
    replace("\\", "\\\\").replace("\"", "\\\"")

fun readLocalKeyValues(vararg files: File): Map<String, String> {
    val values = mutableMapOf<String, String>()
    files.filter { it.exists() }.forEach { file ->
        file.readLines().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isNotEmpty() && !line.startsWith("#") && '=' in line) {
                val sepIdx = line.indexOf('=')
                val key = line.substring(0, sepIdx).trim()
                val value = line.substring(sepIdx + 1)
                    .trim()
                    .removeSurrounding("\"")
                    .removeSurrounding("'")
                if (key.isNotEmpty() && value.isNotEmpty()) {
                    values[key] = value
                }
            }
        }
    }
    return values
}

fun detectHostLanIp(): String? = runCatching {
    NetworkInterface.getNetworkInterfaces()
        ?.toList()
        ?.asSequence()
        ?.filter { it.isUp && !it.isLoopback && !it.isVirtual }
        ?.flatMap { it.inetAddresses.toList().asSequence() }
        ?.filterIsInstance<Inet4Address>()
        ?.firstOrNull { !it.isLoopbackAddress && it.isSiteLocalAddress }
        ?.hostAddress
}.getOrNull()

val localDevConfig = readLocalKeyValues(
    rootProject.file("local.properties"),
    rootProject.file("ai-backend/.env")
)

android {
    namespace = "com.sizzlesense.ai"

    defaultConfig {
        applicationId = "com.sizzlesense.ai"
        versionCode = 1
        versionName = "1.0.0"

        buildConfigField("String", "DEFAULT_BACKEND_URL", "\"http://10.0.2.2:8000\"")
        buildConfigField("String", "DEFAULT_LAN_BACKEND_URL", "\"\"")
        buildConfigField("String", "DEFAULT_TAPO_IP", "\"192.168.11.16\"")
        buildConfigField("String", "DEFAULT_TAPO_USERNAME", "\"\"")
        buildConfigField("String", "DEFAULT_TAPO_PASSWORD", "\"\"")
    }

    buildTypes {
        debug {
            val tapoIp = localDevConfig["TAPO_CAMERA_IP"] ?: "192.168.11.16"
            val tapoUser = localDevConfig["TAPO_CAMERA_USERNAME"].orEmpty()
            val tapoPass = localDevConfig["TAPO_CAMERA_PASSWORD"].orEmpty()
            val lanBackendUrl = localDevConfig["SIZZLE_BACKEND_URL"]
                ?: detectHostLanIp()?.let { ip -> "http://$ip:8000" }
                ?: ""

            buildConfigField(
                "String",
                "DEFAULT_LAN_BACKEND_URL",
                "\"${lanBackendUrl.escapeForBuildConfigString()}\""
            )
            buildConfigField(
                "String",
                "DEFAULT_TAPO_IP",
                "\"${tapoIp.escapeForBuildConfigString()}\""
            )
            buildConfigField(
                "String",
                "DEFAULT_TAPO_USERNAME",
                "\"${tapoUser.escapeForBuildConfigString()}\""
            )
            buildConfigField(
                "String",
                "DEFAULT_TAPO_PASSWORD",
                "\"${tapoPass.escapeForBuildConfigString()}\""
            )
        }
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
