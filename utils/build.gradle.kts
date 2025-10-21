import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
    kotlin("jvm")     // без версии — версия уже в корне
}

// НЕ добавляем repositories здесь

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("com.google.crypto.tink:tink:1.13.0")
    implementation("org.web3j:core:4.9.7")
    implementation("org.bitcoinj:bitcoinj-core:0.16.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
    testRuntimeOnly("org.slf4j:slf4j-simple:2.0.13")
}

tasks.test { useJUnitPlatform() }

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}
java {
    // привести Java-таски к тем же таргетам
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}