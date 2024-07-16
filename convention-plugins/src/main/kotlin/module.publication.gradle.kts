plugins {
    id("maven-publish")
    id("signing")
}

private val rootProjectName = rootProject.name
private val nexusUser = project.getPropertyOrEmptyString("publishing.nexus.user")
private val nexusPassword = project.getPropertyOrEmptyString("publishing.nexus.password")

publishing {

    publications.withType<MavenPublication> {
        repositories {
            maven {
                name = "local"
                url = uri("${layout.buildDirectory}/repository")
            }
            maven {
                name = "central"
                url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = nexusUser
                    password = nexusPassword
                }
            }
            maven {
                name = "snapshot"
                url = uri("https://oss.sonatype.org/content/repositories/snapshots")
                credentials {
                    username = nexusUser
                    password = nexusPassword
                }
            }
        }

        artifact(
            tasks.register("${name}JavadocJar", Jar::class) {
                archiveClassifier.set("javadoc")
                archiveAppendix.set(name)
            }
        )

        pom {
            name.set(this@withType.name)
            url.set("https://github.com/sergejsha/$rootProjectName")

            licenses {
                license {
                    name.set("Apache-2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("halfbit")
                    name.set("Sergej Shafarenka")
                    organization.set("Halfbit GmbH")
                    organizationUrl.set("https://www.halfbit.de")
                }
            }
            scm {
                connection.set("scm:git:git@github.com:sergejsha/$rootProjectName.git")
                developerConnection.set("scm:git:ssh://github.com:sergejsha/$rootProjectName.git")
                url.set("https://github.com/sergejsha/$rootProjectName")
            }
        }
    }
}

private fun Project.getPropertyOrEmptyString(name: String): String =
    if (hasProperty(name)) property(name) as String? ?: "" else ""

val canSign = project.hasProperty("signing.keyId")
if (canSign) {
    publishing {
        publications.forEach { publication ->
            signing.sign(publication)
        }
    }
    signing {
        sign(publishing.publications)
    }
}

// fix for: https://github.com/gradle/gradle/issues/26091
//          https://youtrack.jetbrains.com/issue/KT-46466 is fixed
tasks.withType<AbstractPublishToMaven>().configureEach {
    dependsOn(project.tasks.withType(Sign::class.java))
}
