plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "ru.home.swap.wallet"

    compileSdk = libs.versions.compileSdkVersion.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdkVersion.get().toInt()
        targetSdk = libs.versions.targetSdkVersion.get().toInt()

        testInstrumentationRunner = "ru.home.swap.MyCustomTestRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.material)
    implementation(libs.gson)
    implementation(libs.dagger)
    implementation(libs.dagger.android.support)
    implementation(libs.dagger.android)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.reactive)

    implementation(libs.web3j.core)

//    implementation("androidx.test:monitor:1.6.1") -- cause android tests issues in 'app' main module

//    annotationProcessor("androidx.room:room-compiler:2.5.0")

    kapt("androidx.room:room-compiler:2.5.0")
    kapt(libs.kapt.dagger.compiler)
    kapt(libs.kapt.dagger.android.processor)

    // To use Kotlin Symbol Processing (KSP)
//    ksp("androidx.room:room-compiler:$room_version")

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.turbine)
    testImplementation(libs.gson)
    testImplementation(libs.json)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockk)

    androidTestImplementation(libs.android.test.junit)
    androidTestImplementation(libs.android.test.espresso.core)
/*    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    androidTestImplementation("androidx.arch.core:core-testing:2.1.0")
    androidTestImplementation("org.mockito:mockito-inline:3.12.4")
    androidTestImplementation("org.mockito:mockito-core:3.12.4")
    androidTestImplementation("app.cash.turbine:turbine:0.12.1")
    androidTestImplementation("com.google.code.gson:gson:2.10")
    androidTestImplementation("androidx.work:work-testing:2.8.1")
    androidTestImplementation("junit:junit:4.13.2")*/


    implementation(project(":core"))
}