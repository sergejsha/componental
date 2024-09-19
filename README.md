[![Maven Central](http://img.shields.io/maven-central/v/de.halfbit/componental.svg)](https://central.sonatype.com/artifact/de.halfbit/componental)
[![maintenance-status](https://img.shields.io/badge/maintenance-experimental-blue.svg)](https://gist.github.com/taiki-e/ad73eaea17e2e0372efb76ef6b38f17b)

# 🍱 Componental

Kotlin Multiplatform library for componentization of Compose UI. The library is based on concepts proposed and
implemented
by awesome [Decompose](https://github.com/arkivanov/Decompose). The differences are:

- Kotlin's coroutine first
- No dependencies on Android types
- simplified (subjectively) API

Use it only if you know what you do.

# Dependencies

In `gradle/libs.versions.toml`

```toml
[versions]
kotlin = "2.0.20"
componental = "0.5"

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

# Publishing

1. Bump version in `root.publication.gradle.kts`
2. `./gradlew clean build publishAllPublicationsToCentralRepository`

## Local maven

1. Set `X.X-SNAPSHOT` version in `root.publication.gradle.kts`
2. `./gradlew clean build publishToMavenLocal`

# Release Notes

* 0.6 Better handling of closed channels
* 0.5 Improve Toast API and UI, bump to Kotlin 2.0.20
* 0.4 Refine Stack and Slot API and their implementations
* 0.3 Add BackNavigation
* 0.2 Module `componental` is exposed as API from `componental.compose`
* 0.1 Initial release

# License

```
Copyright 2024 Sergej Shafarenka, www.halfbit.de

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
