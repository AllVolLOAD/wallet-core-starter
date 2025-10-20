plugins {
    id("kotlin-jvm")
}

dependencies {
    implementation(libs.coroutines.core)
    implementation(libs.tink)
    implementation(libs.web3j)
    implementation(libs.bitcoinj)
    testImplementation(libs.junit.jupiter)
}

tasks.test {
    useJUnitPlatform()
}
