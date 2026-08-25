plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.maximillionsnyder.umafinidad"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.maximillionsnyder.umafinidad"
        minSdk = 24
        targetSdk = 36
        /* El workflow de release sobrescribe ambas vía entorno. */
        versionCode = (System.getenv("VERSION_CODE") ?: "1").toInt()
        versionName = System.getenv("VERSION_NAME") ?: "0.1.0"
    }

    /* Firma de release: el workflow decodifica el keystore desde los secrets
       a la raíz del repo. Sin keystore (dev local), se firma con debug para
       que assembleRelease nunca falle. */
    val hayKeystoreCi = !System.getenv("ANDROID_KEYSTORE_BASE64").isNullOrEmpty()

    signingConfigs {
        if (hayKeystoreCi) {
            create("ci") {
                storeFile = File(rootProject.projectDir, "android.keystore")
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("ANDROID_KEY_ALIAS") ?: "umafinidad"
                keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = if (hayKeystoreCi) {
                signingConfigs.getByName("ci")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    /* Los tests de paridad leen los mismos JSON datamined del app
       (src/main/assets) sin duplicarlos como recursos de test. */
    sourceSets.getByName("test") {
        resources.srcDir("src/main/assets")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")

    val compose = "1.11.4"
    implementation("androidx.compose.ui:ui:$compose")
    implementation("androidx.compose.ui:ui-tooling-preview:$compose")
    implementation("androidx.compose.material3:material3:1.4.0")
    debugImplementation("androidx.compose.ui:ui-tooling:$compose")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")

    testImplementation("junit:junit:4.13.2")
}
