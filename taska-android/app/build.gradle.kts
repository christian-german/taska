plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.taska.android"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.taska.android"
        minSdk = 24
        targetSdk = 36
        versionCode = (project.findProperty("versionCode") as String?)?.toInt() ?: 1
        versionName = project.findProperty("versionName") as String? ?: "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("prodRelease") {
            val keystorePath = System.getenv("KEYSTORE_PATH")
            val keystorePassword = System.getenv("KEYSTORE_PASSWORD")
            val keyAliasEnv = System.getenv("KEY_ALIAS")
            val keyPasswordEnv = System.getenv("KEY_PASSWORD")
            if (keystorePath != null && keystorePassword != null && keyAliasEnv != null && keyPasswordEnv != null) {
                storeFile = file(keystorePath)
                storePassword = keystorePassword
                keyAlias = keyAliasEnv
                keyPassword = keyPasswordEnv
            }
        }
    }

    flavorDimensions += "env"

    productFlavors {
        create("dev") {
            dimension = "env"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("String", "API_URL", "\"http://192.168.1.14:8080/\"")
            // Fill in real values before building
            buildConfigField("String", "OIDC_ISSUER_URL", "\"http://192.168.1.14:8000/application/o/taska/\"")
            buildConfigField("String", "OIDC_CLIENT_ID", "\"taska-client\"")
            buildConfigField("String", "OIDC_REDIRECT_URI", "\"com.taska.android:/oauth2callback\"")
            buildConfigField("String", "ACCOUNT_TYPE", "\"com.taska.account.dev\"")
            manifestPlaceholders["redirectUriScheme"] = "com.taska.android"
        }
        create("prod") {
            dimension = "env"
            buildConfigField("String", "API_URL", "\"https://api-taska.atlascore.dev/\"")
            // Fill in real values before building
            buildConfigField("String", "OIDC_ISSUER_URL", "\"https://authentik.atlascore.dev/application/o/taska/\"")
            buildConfigField("String", "OIDC_CLIENT_ID", "\"mE2vXI67I43D8fmclgsjHKwt42W4dkDpXJQUOQEJ\"")
            buildConfigField("String", "OIDC_REDIRECT_URI", "\"com.taska.android:/oauth2callback\"")
            buildConfigField("String", "ACCOUNT_TYPE", "\"com.taska.account\"")
            manifestPlaceholders["redirectUriScheme"] = "com.taska.android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (System.getenv("KEYSTORE_PATH") != null) {
                signingConfig = signingConfigs.getByName("prodRelease")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.appauth)
    implementation(libs.kotlinx.coroutines.android)
    implementation(platform("com.google.firebase:firebase-bom:34.13.0"))
    implementation("com.google.firebase:firebase-messaging:25.0.2")
    implementation("com.auth0.android:jwtdecode:2.0.2")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}