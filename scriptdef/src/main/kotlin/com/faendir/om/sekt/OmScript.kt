package com.faendir.om.sekt

import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*

@KotlinScript(fileExtension = "solution.kts", compilationConfiguration = ScriptConfiguration::class)
abstract class OmScript


object ScriptConfiguration : ScriptCompilationConfiguration(
    {
        defaultImports(
            "com.faendir.om.sp.part.*",
            "com.faendir.om.dsl.api.*",
            "com.faendir.om.sp.part.ArmType.*",
            "com.faendir.om.sp.part.GlyphType.*",
            "com.faendir.om.sp.part.IOType.*"
        )
        ide {
            acceptedLocations(ScriptAcceptedLocation.Everywhere)
        }
    })