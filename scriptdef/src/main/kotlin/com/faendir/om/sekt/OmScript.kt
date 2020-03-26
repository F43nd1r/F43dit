package com.faendir.om.sekt

import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*

@KotlinScript(fileExtension = "solution.kts", compilationConfiguration = ScriptConfiguration::class)
abstract class OmScript


object ScriptConfiguration : ScriptCompilationConfiguration(
    {
        defaultImports("com.faendir.om.sp.part.*", "com.faendir.om.dsl.api.*")
        ide {
            acceptedLocations(ScriptAcceptedLocation.Everywhere)
        }
    })