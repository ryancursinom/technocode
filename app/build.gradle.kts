import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

val cloudinaryPropertyNames = listOf(
    "CLOUDINARY_CLOUD_NAME",
    "CLOUDINARY_UPLOAD_PRESET"
)

val envFile = rootProject.file(".env")
val localPropertiesFile = rootProject.file("local.properties")

val localProperties = Properties().apply {
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }

    if (envFile.exists()) {
        val envProperties = Properties().apply {
            envFile.inputStream().use(::load)
        }

        cloudinaryPropertyNames.forEach { propertyName ->
            envProperties.getProperty(propertyName)?.let { propertyValue ->
                setProperty(propertyName, propertyValue)
            }
        }
    }
}

fun cloudinaryProperty(name: String): String =
    localProperties.getProperty(name)
        ?: error("A propriedade $name não foi encontrada no arquivo .env")

fun buildConfigString(value: String): String =
    "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

android {
    namespace = "com.aula.tiktoktech"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.aula.tiktoktech"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        buildConfigField(
            "String",
            "CLOUDINARY_CLOUD_NAME",
            buildConfigString(cloudinaryProperty("CLOUDINARY_CLOUD_NAME"))
        )
        buildConfigField(
            "String",
            "CLOUDINARY_UPLOAD_PRESET",
            buildConfigString(cloudinaryProperty("CLOUDINARY_UPLOAD_PRESET"))
        )

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
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.cloudinary:cloudinary-android:3.1.2")
    implementation("com.github.bumptech.glide:glide:4.13.2")
}
