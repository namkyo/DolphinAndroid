plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.gnbsoftec.dolphinnative"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.gnbsoftec.dolphinnative"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    signingConfigs {
    }
    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = true
            isShrinkResources = true
//            signingConfig = signingConfigs.getByName("config")
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
//            signingConfig = signingConfigs.getByName("config")
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

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.databinding:databinding-runtime:8.2.2")
    implementation("androidx.databinding:databinding-common:8.2.2")
    implementation("com.google.android.material:material:1.11.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.jakewharton.timber:timber:4.7.1")
    runtimeOnly("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")

    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))
    implementation("com.google.firebase:firebase-analytics:21.5.0")
    implementation("com.google.firebase:firebase-messaging:23.4.0")
    implementation("com.google.firebase:firebase-crashlytics:18.6.1")

    //JSON <=> Map 처리
    implementation("com.google.code.gson:gson:2.10.1")


    //카메라 라이브러리
    implementation("androidx.camera:camera-view:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    //Imageview 확장
    implementation("de.hdodenhof:circleimageview:3.1.0")//Circle ImageView
}