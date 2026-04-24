@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    applyDefaultHierarchyTemplate()

    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvmToolchain(21)
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.serialization.core)

            implementation(libs.kotlinx.datetime)


        }
    }

}

