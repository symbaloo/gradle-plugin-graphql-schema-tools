plugins {
    java
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.12.0"
    kotlin("jvm") version "1.3.72"
    id("com.diffplug.gradle.spotless") version "4.5.0"
}

group = "com.symbaloo.graphql"
version = "1.0.1"

val isReleaseVersion = !version.toString().endsWith("SNAPSHOT")
val repoUrl = "https://github.com/arian/graphql-kotlin-test-dsl"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.graphql-java:graphql-java:15.0")
    implementation("com.apollographql.apollo:apollo-compiler:2.2.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

spotless {
    kotlin {
        ktlint("0.37.2")
    }
    kotlinGradle {
        ktlint("0.37.2")
    }
}

pluginBundle {
    website = "https://github.com/symbaloo/gradle-plugin-graphql-schema-introspection"
    vcsUrl = "https://github.com/symbaloo/gradle-plugin-graphql-schema-introspection.git"
    tags = listOf("GraphQL", "Introspection", "JSON", "GraphQL Schema")
}

gradlePlugin {
    plugins {
        create("graphQLSchemaIntrospectionPlugin") {
            id = "com.symbaloo.graphql.graphql-introspection"
            displayName = "graphQLSchemaIntrospectionPlugin"
            description = "graphQLSchemaIntrospectionPlugin"
            implementationClass = "com.symbaloo.graphql.introspection.SchemaIntrospectionPlugin"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("jar") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            val repoUrl: String = properties["mavenRepoUrl"] as String
            val releasesRepoUrl = uri("$repoUrl/maven-releases/")
            val snapshotsRepoUrl = uri("$repoUrl/maven-snapshots/")

            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl

            // these can be set through gradle project properties
            if (properties.containsKey("mavenRepoUser")) {
                credentials {
                    username = properties["mavenRepoUser"] as String?
                    password = properties["mavenRepoPass"] as String?
                }
            }
        }
    }
}
