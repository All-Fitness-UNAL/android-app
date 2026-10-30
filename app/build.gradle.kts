import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

android {
    namespace = "com.allfitness.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.allfitness.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"

        // shortcut: URLs legales provisionales; reemplazar por las publicadas antes de salir a Play Store
        buildConfigField("String", "TERMS_URL", "\"https://allfitness.example/terminos\"")
        buildConfigField("String", "PRIVACY_URL", "\"https://allfitness.example/politica-de-datos\"")
        buildConfigField("String", "LEGAL_VERSION", "\"2026-10\"")
    }

    flavorDimensions += "backend"
    productFlavors {
        create("mock") {
            dimension = "backend"
            applicationIdSuffix = ".mock"
            buildConfigField("String", "BASE_URL", "\"https://mock.invalid/\"")
        }
        create("real") {
            dimension = "backend"
            val baseUrl = localProperties.getProperty("allfitness.baseUrl", "https://api.allfitness.invalid/")
            buildConfigField("String", "BASE_URL", "\"${baseUrl.trimEnd('/')}/\"")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        // AGP está fijado a la versión más alta que soporta Android Studio; subirlo cuando el IDE lo permita.
        disable += "AndroidGradlePluginVersion"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.datastore.preferences)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}
