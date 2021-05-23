package com.faendir.om.sekt

import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*

@KotlinScript(fileExtension = "solution.kts", compilationConfiguration = ScriptConfiguration::class)
abstract class OmScript


object ScriptConfiguration : ScriptCompilationConfiguration(
    {
        defaultImports(
            "com.faendir.om.dsl.api.*",
            "com.faendir.om.parser.solution.model.*",
            "com.faendir.om.parser.solution.model.part.*",
            "com.faendir.om.parser.solution.model.part.ArmType.*",
            "com.faendir.om.parser.solution.model.part.GlyphType.*",
            "com.faendir.om.parser.solution.model.part.IOType.*"
        )
        ide {
            acceptedLocations(ScriptAcceptedLocation.Everywhere)
        }
    })