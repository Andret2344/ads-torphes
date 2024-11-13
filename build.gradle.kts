plugins {
    java
    idea
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.slf4j)
    implementation(libs.jda)
    implementation(libs.gson)
}

application {
    mainClass.set("eu.andret.bot.discord.torphes.Torphes")
}

tasks {
    jar {
        duplicatesStrategy = DuplicatesStrategy.WARN

        from({
            configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
        })

        manifest {
            attributes["Main-Class"] = "eu.andret.bot.discord.torphes.Torphes"
        }
    }
}
