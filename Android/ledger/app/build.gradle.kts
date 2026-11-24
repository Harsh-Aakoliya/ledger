import java.util.Properties
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
val env = Properties().apply {
    val envFile = rootProject.file(".env")
    if (envFile.exists()) {
        envFile.inputStream().use { load(it) }
    }
    System.out.println(envFile);
}
android {
    namespace = "com.noviaralabs.ledger"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.noviaralabs.ledger"
        minSdk = 26
        targetSdk = 35
        versionCode = env.getProperty("VERSION_CODE", "1").toInt()
        versionName = env.getProperty("VERSION_NAME", "1.0")

        val baseUrl = env.getProperty(
            "BASE_URL",
            "http://10.0.2.2:8080/"
        )

        buildConfigField(
            "String",
            "BASE_URL",
            "\"$baseUrl\""
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Phone Number Hint API
    implementation("com.google.android.gms:play-services-auth:21.2.0")
}
