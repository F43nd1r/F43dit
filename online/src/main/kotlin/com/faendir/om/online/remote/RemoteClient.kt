package com.faendir.om.online.remote

import com.faendir.om.dsl.DslCompiler
import com.faendir.om.online.SyntaxException
import com.faendir.om.sp.SolutionParser
import kotlinx.io.streams.asOutput
import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.rmi.registry.LocateRegistry
import java.util.*
import kotlin.script.experimental.api.ResultWithDiagnostics

class RemoteClient {
    companion object {
        @JvmStatic
        fun main(args: Array<String>?) {
            if (args != null && args.isNotEmpty()) {
                val id = args[0]
                println("handling work $id")
                val server = LocateRegistry.getRegistry("localhost", 1099).lookup(RemoteServer.NAME) as RemoteInterface
                val dsl = server.getWork(id)
                if (dsl != null) {
                    val remoteResult = when (val result = DslCompiler.fromDsl(dsl)) {
                        is ResultWithDiagnostics.Success -> {
                            val out = ByteArrayOutputStream()
                            SolutionParser.write(result.value, out.asOutput())
                            RemoteResult.Success(out.toByteArray())
                        }
                        is ResultWithDiagnostics.Failure -> {
                            println(result.reports.joinToString("\n") {
                                    diagnostic -> diagnostic.exception?.let{
                                val writer = StringWriter()
                                it.printStackTrace(PrintWriter(writer))
                                writer.toString()
                            } ?: diagnostic.message
                            })
                            RemoteResult.Failure(result.reports.map { it.exception }.firstOrNull { it != null } ?: result.reports.last().run {
                                SyntaxException(message, location?.start?.line?.let { it - 1 } ?: -1)
                            })
                        }
                    }
                    server.returnResult(id, remoteResult)
                }
            }
        }
    }
}