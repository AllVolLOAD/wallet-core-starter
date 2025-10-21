import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")           // версия объявлена в корне
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

// репозитории не указываем — они в settings.gradle.kts

dependencies {
    implementation(project(":utils"))

    // runtime-логгер, чтобы не было NOP в рантайме
    runtimeOnly("org.slf4j:slf4j-simple:2.0.13")
}

application {
    // если файл App.kt в корне пакета: main() в AppKt
    mainClass.set("AppKt")
}

// Опционально: настроим имя файла и манифест
tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("wallet-cli")
    archiveClassifier.set("")        // без "-all"
    archiveVersion.set("")           // без версии в имени
    // mergeServiceFiles() // раскомментируй, если когда-нибудь добавим gRPC/vertx и т.п.
}

kotlin {
    // Компилируем под JVM 1.8 (чтобы запускалось на Java 8)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

java {
    // привести Java-таски к тем же таргетам
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
