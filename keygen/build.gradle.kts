plugins {
    application
}

application {
    mainClass.set("dev.muzahid.keygen.Keygen")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
