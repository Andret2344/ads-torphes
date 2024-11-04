plugins {
    java
    idea
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-jdk14:2.0.6")
    implementation("net.dv8tion:JDA:5.2.0")
    implementation("com.google.code.gson:gson:2.11.0")
}

application {
    mainClass.set("eu.andret.bot.discord.torphes.Torphes")
}

tasks {
    jar {
        duplicatesStrategy = DuplicatesStrategy.WARN

        into("resources") {
            from("resources")
        }

        from({
            configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
        })

        manifest {
            attributes["Main-Class"] = "eu.andret.bot.discord.torphes.Torphes"
        }
    }
}
