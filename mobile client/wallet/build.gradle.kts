import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 21
        targetSdk = 32

//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunner = "ru.home.government.MyCustomTestRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    testOptions {
        animationsDisabled = true
        unitTests {
            isReturnDefaultValues = true
        }
    }
    packagingOptions {
        resources.pickFirsts.add("org/bouncycastle/x509/CertPathReviewerMessages.properties")
        resources.pickFirsts.add("org/bouncycastle/x509/CertPathReviewerMessages_de.properties")
        resources.pickFirsts.add("org.bouncycastle.LICENSE")
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.activity:activity-ktx:1.6.1")
    implementation("androidx.room:room-runtime:2.5.0")
    implementation("androidx.room:room-ktx:2.5.0")
    implementation("androidx.paging:paging-runtime:3.1.1")

    implementation("com.google.android.material:material:1.6.1")
    implementation("com.google.code.gson:gson:2.10")
    implementation ("com.google.dagger:dagger:2.44.2")
    implementation ("com.google.dagger:dagger-android-support:2.44.2")
    implementation("com.google.dagger:dagger-android:2.44.2")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.6.4")

    implementation("org.web3j:core:4.9.4")
    implementation("androidx.work:work-runtime:2.8.0")
    implementation("androidx.work:work-runtime-ktx:2.8.0")
//    implementation("androidx.test:monitor:1.6.1") -- cause android tests issues in 'app' main module

//    annotationProcessor("androidx.room:room-compiler:2.5.0")

    kapt("androidx.room:room-compiler:2.5.0")
    kapt("com.google.dagger:dagger-compiler:2.44.2")
    kapt("com.google.dagger:dagger-android-processor:2.44.2")

    // To use Kotlin Symbol Processing (KSP)
//    ksp("androidx.room:room-compiler:$room_version")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    testImplementation("androidx.arch.core:core-testing:2.1.0")
    testImplementation("org.mockito:mockito-inline:3.12.4")
    testImplementation("org.mockito:mockito-core:3.12.4")
    testImplementation("app.cash.turbine:turbine:0.12.1")
    testImplementation("com.google.code.gson:gson:2.10")

    androidTestImplementation("androidx.test.ext:junit:1.1.30")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    implementation(project(":core"))
}