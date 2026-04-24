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

    sourceSets {
        androidMain.dependencies {
            implementation(projects.shared)

            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activityCompose)
            implementation(libs.androidx.lifecycle.runtime.ktx)
            implementation(libs.androidx.lifecycle.viewmodel.compose)

            implementation(libs.androidx.compose.ui)
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
}

android {
    namespace = "com.pollyannawu.justwoo.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pollyannawu.justwoo.android"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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
}
