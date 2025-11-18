// File: app/build.gradle.kts

val nav_version = "2.7.7"
val room_version = "2.6.1"
val lifecycle_version = "2.8.3"

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services) // <-- 1. BARIS BARU DITAMBAHKAN
}

android {
    namespace = "com.example.wangku"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.wangku"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Circle ImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Gunakan variabel dalam string dengan ${}
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")

    // Dependensi untuk testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    // --- DEPENDENSI ROOM ---
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    // ----------------------------------------

    // --- DEPENDENSI VIEWMODEL & GRAFIK ---
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    // ---------------------------------------------

    // --- DEPENDENSI FIREBASE BARU ---
    // Import Bill of Materials (BOM)
    implementation(platform(libs.firebase.bom)) // <-- 2. BARIS BARU DITAMBAHKAN
    // Tambahkan dependensi Firebase Authentication
    implementation(libs.firebase.auth)          // <-- 3. BARIS BARU DITAMBAHKAN
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation(libs.firebase.firestore)
    // ---------------------------------

    // --- DEPENDENSI GLIDE ---
    implementation("com.github.bumptech.glide:glide:4.16.0")
    ksp("com.github.bumptech.glide:compiler:4.16.0")
    // ---------------------------------
}