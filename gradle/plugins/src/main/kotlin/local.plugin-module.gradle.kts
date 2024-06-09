import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    id("org.jetbrains.intellij.platform.module")
    id("local.jbr-guidance")
}

val extension = extensions.create<PluginModuleExtension>("pluginModule")
abstract class PluginModuleExtension {
    abstract val platform: Property<IntelliJPlatformType>
}

// Call `.get()` because `group` and `version` do not (yet) seem to support Providers and just call `toString()`.
group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create(extension.platform, providers.gradleProperty("platformVersion"))
        testFramework(TestFrameworkType.Platform)
        //testFramework(TestFrameworkType.JUnit5)
        instrumentationTools()
    }
}

intellijPlatform {
    val suffix = if (project == rootProject) "" else "-${project.name}"
    projectName = providers.gradleProperty("pluginName").map { "$it$suffix" }
}

java {
    // Don't use Gradle's toolchain feature as it prevents building the project with more recent JDKs. Related issues:
    // https://github.com/gradle/gradle/issues/16256 - Ability to set a min language version for a toolchain
    // https://github.com/gradle/gradle/issues/17444 - Toolchains feature does not appear to treat Java as backwards compatible
    // https://github.com/gradle/gradle/issues/18894 - More flexibility in querying Java toolchains
    sourceCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    withType<Javadoc> {
        options.encoding = "UTF-8"
    }

    withType<Test> {
        systemProperty("idea.test.execution.policy", "org.nixos.idea.NixTestExecutionPolicy")
        systemProperty("plugin.testDataPath", projectDir.resolve("src/test/testData").path)
    }
}
