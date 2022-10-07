plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 21
        targetSdk = 32

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    packagingOptions {
        resources.pickFirsts.add("org/bouncycastle/x509/CertPathReviewerMessages.properties")
        resources.pickFirsts.add("org/bouncycastle/x509/CertPathReviewerMessages_de.properties")
        resources.pickFirsts.add("org.bouncycastle.LICENSE")
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.activity:activity-ktx:1.6.0")

    implementation("org.web3j:core:4.9.4") {
//        exclude(group : "org.bouncycastle")
    }
//    implementation 'com.madgag.spongycastle:core:1.58.0.0'
//    implementation 'com.madgag.spongycastle:prov:1.58.0.0'
//    implementation 'com.madgag.spongycastle:bcpkix-jdk15on:1.58.0.0'
//    implementation 'com.madgag.spongycastle:pkix:1.54.0.0' // sic!
//    implementation 'com.madgag.spongycastle:pg:1.54.0.0'
//    runtimeOnly 'com.madgag.spongycastle:bcpg-jdk15on:1.58.0.0'
//    implementation 'com.madgag.spongycastle:bctls-jdk15on:1.58.0.0'
//    implementation("com.walletconnect:kotlin-walletconnect-lib:0.9.9") {
//        exclude('org/bouncycastle/x509/CertPathReviewerMessages_de.properties')
//        exclude('org.bouncycastle.LICENSE')
//        exclude(group = 'org.bouncycastle')
//        exclude module: 'bcprov-jdk15on'
//    }

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.1.30")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    implementation(project(":Core"))
}