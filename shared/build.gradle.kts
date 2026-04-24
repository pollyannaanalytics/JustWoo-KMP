import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kotlin.parcelize)
}

kotlin {
    jvmToolchain(21)

    androidTarget { publishLibraryVariants("release") }
    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    // shared/build.gradle.kts

    kotlin {
        sourceSets {
            commonMain.dependencies {
                api(libs.decompose)
                implementation(projects.core.model)
                implementation(libs.decompose.extensions.compose)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.kotlinx.datetime)
                implementation(libs.koin.core)

                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.serialization)
                implementation(libs.ktor.serialization.json)
                implementation(libs.ktor.client.logging)

            implementation(libs.settings)
            implementation(libs.settings.coroutines)
            implementation(libs.settings.serialization)
            implementation(libs.settings.observable)
        }

            iosMain.dependencies {
            }
            commonTest.dependencies {
                implementation(kotlin("test"))
            }
        }
    }

    //https://kotlinlang.org/docs/native-objc-interop.html#export-of-kdoc-comments-to-generated-objective-c-headers
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        compilations["main"].compileTaskProvider.configure {
            compilerOptions {
                freeCompilerArgs.add("-Xexport-kdoc")
            }
        }
    }

    android {
        namespace = "com.pollyannawu.justwoo.android"
        compileSdk = 36

        defaultConfig {
            minSdk = 26
            //noinspection EditedTargetSdkVersion
            targetSdk = 36
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        packaging {
            resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}
