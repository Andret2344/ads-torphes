plugins {
    java
    idea
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.jda)
    implementation(libs.gson)
    // logging
    implementation(libs.slf4j.api)
    implementation(libs.log4j.slf4j.impl)
    implementation(libs.log4j.core)
}

application {
    mainClass.set("eu.andret.bot.discord.torphes.Torphes")
}

tasks {
    compileJava {
        sourceCompatibility = JavaVersion.VERSION_21.toString()
        targetCompatibility = JavaVersion.VERSION_21.toString()
    }

    jar {
        duplicatesStrategy = DuplicatesStrategy.WARN

        from({
            configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
        })

        manifest {
            attributes["Main-Class"] = application.mainClass.get()
        }
    }
}
