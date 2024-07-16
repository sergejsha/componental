[![Maven Central](http://img.shields.io/maven-central/v/de.halfbit/componental.svg)](https://central.sonatype.com/artifact/de.halfbit/componental)
[![maintenance-status](https://img.shields.io/badge/maintenance-experimental-blue.svg)](https://gist.github.com/taiki-e/ad73eaea17e2e0372efb76ef6b38f17b)

# 🍱 Componental

Kotlin Multiplatform library for componentization of Compose UI. The library is based on concepts proposed and implemented
by awesome [Decompose](https://github.com/arkivanov/Decompose). The differences are:

- Kotlin's coroutine first
- No dependencies on Android types
- Slightly modified API

Use it only if you know what you do.

# Dependencies

In `gradle/libs.versions.toml`

```toml
[versions]
kotlin = "2.0.0"
componental = "0.1"

[libraries]
componental = { module = "de.halfbit:componental", version.ref = "componental" }
componental-compose = { module = "de.halfbit:componental-compose", version.ref = "componental" }

[plugins]
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
```

Shared code in `shared/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.componental)
        }
    }
}
```

Android code in `appAndroid/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

dependencies {
    api(libs.componental.compose)
}
```

# Release

1. Bump version in `root.publication.gradle.kts` of the root project
2. `./gradlew clean build publishAllPublicationsToCentralRepository`

# License

```
Copyright 2024 Sergej Shafarenka, www.halfbit.de

You are FREE to use, copy, redistribute, remix, transform, and build 
upon the material or its derivative FOR ANY LEGAL PURPOSES.

Any distributed derivative work containing this material or parts 
of it must have this copyright attribution notices.

The material is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES 
OR CONDITIONS OF ANY KIND, either express or implied.
```
