import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)

}

android {
    namespace = "com.example.gestionmovilsolicitudes"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.gestionmovilsolicitudes"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // URLs de los microservicios. 10.0.2.2 es el "localhost" del computador visto desde el emulador.
        // En un teléfono físico, reemplazar por la IP del computador en la red Wi-Fi (por ejemplo 192.168.1.20).
        buildConfigField("String", "USUARIOS_URL", "\"http://10.0.2.2:8081/\"")
        buildConfigField("String", "SOLICITUDES_URL", "\"http://10.0.2.2:8082/\"")
        // API externa pública (indicadores económicos de Chile)
        buildConfigField("String", "INDICADORES_URL", "\"https://mindicador.cl/\"")
    }

    // Firma del APK release. La ruta del .jks y las claves se leen de keystore.properties (en la raíz)
    // para no escribir contraseñas en este archivo. La ruta es relativa a la raíz del proyecto.
    val keystoreProps = Properties().apply {
        val archivo = rootProject.file("keystore.properties")
        if (archivo.exists()) archivo.inputStream().use { load(it) }
    }

    signingConfigs {
        if (keystoreProps.containsKey("storeFile")) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
            // Si no existe keystore.properties el release sale sin firmar (el proyecto igual compila)
            signingConfigs.findByName("release")?.let { signingConfig = it }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.coil.compose)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.android)
    // Retrofit: microservicios Spring Boot y API externa
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}