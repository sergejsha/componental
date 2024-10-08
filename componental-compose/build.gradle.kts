import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("module.publication")
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    explicitApi()

    jvm()
    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        androidMain.dependencies {
            implementation(compose.uiTooling)
        }
        commonMain.dependencies {
            api(project(":componental"))
            implementation(compose.foundation)
            implementation(compose.animation)
            implementation(compose.material3)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "de.halfbit.componental.compose"
    compileSdk = libs.versions.android.sdk.compile.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.sdk.min.get().toInt()
    }
}

// https://youtrack.jetbrains.com/issue/KT-61313
tasks.withType<Sign>().configureEach {
    val publicationName = name.removePrefix("sign").removeSuffix("Publication")
    tasks.findByName("linkDebugTest$publicationName")?.let {
        mustRunAfter(it)
    }
    tasks.findByName("compileTestKotlin$publicationName")?.let {
        mustRunAfter(it)
    }
}
