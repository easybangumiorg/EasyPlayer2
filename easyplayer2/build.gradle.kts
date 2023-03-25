@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "loli.ball.easyplayer2"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf("-Xjvm-default=all", "-opt-in=kotlin.RequiresOptIn")
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.2"
    }
}

afterEvaluate {
    publishing {
        publications {
            create("maven_public", MavenPublication::class) {
                groupId = "loli.ball"
                artifactId = "easyplayer2"
                version = "1.0.0"
                from(components.getByName("release"))
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "11"
    kotlinOptions.freeCompilerArgs += "-Xcontext-receivers"
}

dependencies {

    implementation("com.google.android.exoplayer:exoplayer:2.18.5")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.10")

    val composeMaterialVersion = "1.4.0"
    implementation("androidx.compose.material:material:$composeMaterialVersion")
    implementation("androidx.compose.material:material-icons-core:$composeMaterialVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeMaterialVersion")

    implementation("com.google.accompanist:accompanist-systemuicontroller:0.30.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.activity:activity-compose:1.7.0")
    implementation("androidx.compose.material3:material3:1.1.0-beta01")

}