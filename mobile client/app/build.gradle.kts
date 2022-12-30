plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("project-report")
}

android {

    namespace = "ru.home.swap"

    compileSdk = 33

    defaultConfig {
        applicationId = "ru.home.swap"
        minSdk = 21
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    flavorDimensions.add("default")
    productFlavors {
        create("prod") {
            dimension = "default"
            applicationIdSuffix = ".prod"
            versionNameSuffix = "-prod"
        }
        create("experiment") {
            dimension = "default"
            applicationIdSuffix = ".experiment"
            versionNameSuffix = "-experiment"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    kapt {
        correctErrorTypes = false
    }
    testOptions {
        animationsDisabled = true
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {

//    implementation "androidx.core:core-ktx:1.9.0"
//    implementation "androidx.appcompat:appcompat:1.4.1"
//    implementation "com.google.android.material:material:1.6.0"
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.activity:activity-ktx:1.6.0")
    implementation("androidx.fragment:fragment-ktx:1.4.1")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.4.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.4.2")
    implementation("androidx.navigation:navigation-dynamic-features-fragment:2.4.2")
    implementation("androidx.paging:paging-runtime:3.1.1")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
//    implementation "com.squareup.okhttp3:logging-interceptor:3.11.0"
    implementation ("com.google.dagger:dagger:2.44.2")
    implementation ("com.google.dagger:dagger-android-support:2.44.2")
    implementation("com.google.dagger:dagger-android:2.44.2")

    kapt("com.google.dagger:dagger-compiler:2.44.2")
    kapt("com.google.dagger:dagger-android-processor:2.44.2")

    testImplementation("org.mockito:mockito-core:3.12.4")
    testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
    testImplementation("org.mockito:mockito-inline:3.12.4")

    testImplementation ("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")

    androidTestImplementation ("androidx.test.ext:junit:1.1.3")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.4.0")

    implementation(project(":core"))
    implementation(project(":wallet"))
}