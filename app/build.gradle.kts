plugins {
    application
    // применяем наш конвеншн-плагин из buildSrc
    id("kotlin-jvm")
}

dependencies {
    implementation(project(":utils"))
    implementation(libs.tink)
    implementation(libs.web3j)
    implementation(libs.bitcoinj)
    implementation(libs.slf4j.simple)
}

application {
    // имя главного класса
    mainClass.set("AppKt")
}
