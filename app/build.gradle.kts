plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}
android {
    namespace = "com.gnbsoftec.dolphinnative"
    compileSdk = 34
    buildToolsVersion = "34.0.0"
    defaultConfig {
        applicationId = "com.gnbsoftec.dolphinnative"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    signingConfigs {
        create("dolphinSigningConfig") {
            storeFile = file("key/dolphin.jks")
            storePassword = "gnb1234"
            keyAlias = "gnb"
            keyPassword = "gnb1234"
        }
    }
    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = true
            isShrinkResources = true
            commonSetting(this,signingConfigs.getByName("dolphinSigningConfig"))
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            commonSetting(this,signingConfigs.getByName("dolphinSigningConfig"))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
        //noinspection DataBindingWithoutKapt
        dataBinding = true
    }
}

// Kotlin DSL 환경에서 공통의 buildConfigField 설정을 적용하기 위한 함수
fun commonSetting(buildType: com.android.build.api.dsl.ApplicationBuildType, signingConfig: com.android.build.gradle.internal.dsl.SigningConfig) {
    buildType.proguardFiles(com.android.build.gradle.ProguardFiles.getDefaultProguardFile("proguard-android-optimize.txt", project.layout.buildDirectory),"proguard-rules.pro")
    buildType.signingConfig = signingConfig
    buildType.buildConfigField("String", "devUrl", "\"http://175.209.155.74:8180/login.frm\"") //개발서버
    buildType.buildConfigField("String", "prodUrl", "\"http://175.209.155.74:8180/login.frm\"") //운영서버
}

//라이브러리
dependencies {
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.databinding:databinding-runtime:8.2.2")
    implementation("androidx.databinding:databinding-common:8.2.2")

    runtimeOnly("androidx.annotation:annotation:1.7.1")
    runtimeOnly("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    implementation("com.jakewharton.timber:timber:4.7.1")
    runtimeOnly("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")

    //JSON <=> Map 처리
    implementation("com.google.code.gson:gson:2.10.1")
    //스냅뷰
    implementation("com.google.android.material:material:1.11.0")

    //카메라 라이브러리
    implementation("androidx.camera:camera-view:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")

    // Gif 이미지 로딩을 위한 라이브러리
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.17")
}