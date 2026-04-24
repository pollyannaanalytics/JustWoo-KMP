plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    jvmToolchain(21)

    androidTarget()
}

android {
    namespace = "com.pollyannawu.justwoo.android"
    compileSdk = 36

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/main/AndroidManifest.xml")
        }
    }

    defaultConfig {
        applicationId = "com.pollyannawu.justwoo.android"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures {
        compose = true
    }


    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
        getByName("debug") {
            isMinifyEnabled = false
        }
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }

    dependencies {
        implementation(projects.shared)
        implementation(projects.core.model)

        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.activityCompose)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.lifecycle.viewmodel.compose)

        implementation(libs.androidx.compose.ui)
        implementation(libs.androidx.compose.runtime)
        implementation(libs.androidx.compose.ui.tooling.preview)
        implementation(libs.androidx.compose.material3)
        implementation(libs.androidx.compose.material.icons.extended)
        implementation(libs.androidx.compose.foundation)

        implementation(libs.koin.android)
        implementation(libs.koin.androidx.compose)
        implementation(libs.koin.compose.viewmodel.navigation)

        implementation(libs.kotlinx.coroutines.android)
        implementation(libs.kotlinx.datetime)
        implementation(libs.kotlinx.serialization.json)
    }
}
