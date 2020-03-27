package com.faendir.om.online.remote

import java.io.File


object JvmStarter {
    private const val MAX_TIME = 60
    private const val MAX_MEM = 512
    fun exec(klass: Class<*>, vararg args: String) {
        val javaHome = System.getProperty("java.home")
        val javaBin = javaHome +
                File.separator.toString() + "bin" +
                File.separator.toString() + "java"
        val classpath = System.getProperty("java.class.path")
        val className = klass.name
        val command = listOf<String>(javaBin,
            "-Xmx${MAX_MEM}m",
            "-Xms${MAX_MEM /8}m",
            "-cp", classpath,
            "-Djava.security.manager",
            "-Djava.security.policy==java.policy",
            className,
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