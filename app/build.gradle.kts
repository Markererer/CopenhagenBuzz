plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
}

android {
    namespace = "dk.itu.moapd.copenhagenbuzz.maass"
    compileSdk = 35

    defaultConfig {
        applicationId = "dk.itu.moapd.copenhagenbuzz.maass"
        minSdk = 27
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0") // Consider adding to libs.versions.toml
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0") // Consider adding to libs.versions.toml
    implementation("androidx.activity:activity-ktx:1.8.2") // Consider adding to libs.versions.toml

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.play.services.auth)
    implementation(libs.firebase.ui.auth)
    implementation("com.google.firebase:firebase-auth") // Keep this when using BOM

    // Credential Manager (if needed)
    implementation("androidx.credentials:credentials:1.3.0") // Consider adding to libs.versions.toml
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0") // Consider adding to libs.versions.toml
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1") // Consider adding to libs.versions.toml
}