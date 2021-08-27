package com.faendir.om.cli

import com.faendir.om.dsl.DslCompiler
import com.faendir.om.dsl.DslGenerator
import com.faendir.om.parser.solution.SolutionParser
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.optional
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import kotlin.script.experimental.api.ResultWithDiagnostics

fun main(args: Array<String>) {
    val parser = ArgParser("f43dit")
    val input by parser.argument(ArgType.String, description = "Input file")
    val output by parser.argument(ArgType.String, description = "Output file").optional()
    parser.parse(args)
    if (input.endsWith(".solution")) {
        File(output ?: "$input.kts").writeText(DslGenerator.toDsl(SolutionParser.parse(File(input).inputStream().source().buffer())))
    } else if (input.endsWith(".solution.kts")) {
        when (val result = DslCompiler.fromDsl(File(input).readText())) {
            is ResultWithDiagnostics.Success -> SolutionParser.write(result.value, File(output ?: input.removeSuffix(".kts")).outputStream().sink().buffer())
            is ResultWithDiagnostics.Failure -> println(result.reports.joinToString("\n"))
        }

    }
}