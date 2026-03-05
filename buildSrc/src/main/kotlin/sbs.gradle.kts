plugins {
    id("shared")
}

// We explicitly pin the test JVM to JDK 21 here. The reason for this is that OptionsParsingVMFeaturesProvider
// conditionally adds AotCache and CompressedObjectHeaders VM features only when running on JDK 24+.
// Without this pin, the test JVM defaults to whatever JDK Gradle itself runs on, which may vary across
// environments and cause AxelixMetadataEndpointTest to fail with an unexpected number of vmFeatures.
tasks.withType<Test>().configureEach {
    javaLauncher.set(
            javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(21) })
}
