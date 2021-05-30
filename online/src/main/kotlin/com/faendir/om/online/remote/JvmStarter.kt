package com.faendir.om.online.remote

import io.github.classgraph.ClassGraph
import java.io.File


object JvmStarter {
    private const val MAX_TIME = 60
    private const val MAX_MEM = 512
    fun exec(klass: Class<*>, vararg args: String) {
        val javaHome = System.getProperty("java.home")
        val javaBin = javaHome + File.separator.toString() + "bin" + File.separator.toString() + "java"
        val classpath = ClassGraph().classpath
        val command = listOf<String>(
            javaBin,
            "-Xmx${MAX_MEM}m",
            "-Xms${MAX_MEM / 8}m",
            "-cp", classpath,
            "--add-opens", "java.base/java.util=ALL-UNNAMED",
            "-Djava.security.manager",
            "-Djava.security.policy==java.policy",
            klass.name,
            *args
        )
        val builder = ProcessBuilder(command)
        val process = builder.inheritIO().start()
        try {
            val thread = Thread{ process.waitFor() }
            thread.start()
            thread.join(MAX_TIME * 1000L)
        } finally {
            process.destroyForcibly()
        }
    }
}