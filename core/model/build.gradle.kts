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
}