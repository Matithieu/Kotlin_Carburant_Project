plugins {
    // Plugin Kotlin pour les projets JVM
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.8.0"
    id("org.openjfx.javafxplugin") version "0.0.13"

    application
}

repositories {
    // Utilisation de Maven Central pour les dépendances
    mavenCentral()
}

dependencies {
    // Intégration de Kotlin avec JUnit 5 pour les tests
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.0")

    // Intégration avec JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")

    // Lanceur JUnit Platform pour les tests
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.0")

    // Dépendances Fuel pour les requêtes HTTP
    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
    implementation("com.github.kittinunf.fuel:fuel-gson:2.3.1")

    // Gson pour la désérialisation JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // Exemple d'une autre dépendance, Guava (si nécessaire dans le projet)
    implementation("com.google.guava:guava:32.1.2-jre")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    implementation("org.apache.logging.log4j:log4j-core:2.17.2")
    implementation("org.apache.logging.log4j:log4j-api:2.17.2")

    implementation("org.controlsfx:controlsfx:11.2.1")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

javafx {
    version = "17.0.6"
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    // Définition de la classe principale
    mainClass.set("org.isen.carburants.AppKt")
}

tasks.named<Test>("test") {
    // Utilisation de JUnit Platform pour les tests
    useJUnitPlatform()
}
