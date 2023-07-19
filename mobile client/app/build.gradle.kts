@file:Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.android") // sic! can not do migration
    id("kotlin-kapt")
    id("project-report")
    id("kotlin-android")
    id("kotlin-android-extensions")
}

android {

    namespace = "ru.home.swap"

    compileSdk = libs.versions.compileSdkVersion.get().toInt()

    defaultConfig {
        applicationId = "io.github.gelassen.swap"
        minSdk = libs.versions.minSdkVersion.get().toInt()
        targetSdk = libs.versions.targetSdkVersion.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "ru.home.swap.MyCustomTestRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
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
//        android:forceQueryable="true"
        execution = "ANDROIDX_TEST_ORCHESTRATOR"

    }
    lint {
        baseline = file("lint-baseline.xml")
    }
}

dependencies {
    //    androidx
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.extensions)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.dynamic.features.fragment)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.espresso.idling.net)
    //    kotlinx
    implementation (libs.kotlinx.coroutines.core)
    implementation (libs.kotlinx.coroutines.android)
    //    retrofit
    implementation (libs.retrofit2.retrofit)
    implementation (libs.retrofit2.converter.gson)
    //    dagger
    implementation (libs.dagger)
    implementation (libs.dagger.android.support)
    implementation(libs.dagger.android)
    //    kapt
    kapt(libs.kapt.dagger.compiler)
    kapt(libs.kapt.dagger.android.processor)
    //    tests
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockito.inline)
    testImplementation (libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    //    android tests
    androidTestImplementation(libs.android.test.junit)
    androidTestImplementation(libs.android.test.espresso.core)
    androidTestImplementation(libs.android.test.espresso.idling.net)
    androidTestImplementation(libs.android.test.core.ktx)
    androidTestImplementation(libs.android.test.runner)
    androidTestUtil(libs.android.test.orchestrator)

    implementation(project(":core"))
    implementation(project(":wallet"))
}