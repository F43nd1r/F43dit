package com.faendir.om.dsl

import com.faendir.om.parser.solution.model.Solution
import com.faendir.om.sekt.OmScript
import kotlinx.coroutines.runBlocking
import java.security.*
import java.security.cert.Certificate
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.onSuccess
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.*
import kotlin.script.experimental.jvm.util.classpathFromClass
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.createJvmEvaluationConfigurationFromTemplate

object DslCompiler {
    private val context = AccessControlContext(arrayOf(ProtectionDomain(CodeSource(null, null as Array<Certificate>?), Permissions().apply {
        //kotlin
        add(ClassLoadingPermission("kotlin.jvm.internal.*"))
        add(ClassLoadingPermission("kotlin.jvm.functions.*"))
        add(ClassLoadingPermission("kotlin.Unit"))
        add(ClassLoadingPermission("kotlin.collections.*"))
        add(ClassLoadingPermission("kotlin.ranges.*"))
        //java
        add(ClassLoadingPermission("java.util.*"))
        //api
        add(ClassLoadingPermission("com.faendir.om.parser.solution.model.*"))
        add(ClassLoadingPermission("com.faendir.om.parser.solution.model.part.*"))
        add(ClassLoadingPermission("com.faendir.om.dsl.api.*"))
        //script
        add(ClassLoadingPermission("com.faendir.om.sekt.OmScript"))
        add(ClassLoadingPermission("com.faendir.om.script.generated.*"))
        //compiler
        add(RuntimePermission("createClassLoader"))
    })))
    private val classLoader by lazy { AccessController.doPrivileged(PrivilegedAction { SecureClassLoader(DslCompiler::class.java.classLoader) }) }

    private val compilationConfiguration by lazy {
        AccessController.doPrivileged(PrivilegedAction {
            createJvmCompilationConfigurationFromTemplate<OmScript> {
                jvm {
                    dependenciesFromCurrentContext(wholeClasspath = true, unpackJarCollections = true)
                    updateClasspath(classpathFromClass(OmScript::class))
                }
            }
        })

    }

    private val evaluationConfiguration by lazy {
        createJvmEvaluationConfigurationFromTemplate<OmScript> {
            jvm {
                baseClassLoader(classLoader)
                loadDependencies(false)
            }
        }
    }

    fun fromDsl(dsl: String): ResultWithDiagnostics<Solution> {
        val host = BasicJvmScriptingHost(evaluator = SecureScriptEvaluator(context, classLoader))
        val compiled = AccessController.doPrivileged(PrivilegedAction {
            runBlocking {
                host.compiler.invoke(("package com.faendir.om.script.generated\n$dsl").toScriptSource(), compilationConfiguration)
            }
        })
        val result = compiled.onSuccess { runBlocking { host.evaluator.invoke(it, evaluationConfiguration) } }
        return result.onSuccess {
            when(val returnValue = it.returnValue) {
                is ResultValue.Value -> ResultWithDiagnostics.Success(returnValue.value as Solution)
                is ResultValue.Error -> ResultWithDiagnostics.Failure(listOf(ScriptDiagnostic(1000, "Evaluation failed", severity = ScriptDiagnostic.Severity.ERROR, exception = returnValue.error)))
                else -> ResultWithDiagnostics.Failure(listOf(ScriptDiagnostic(2000, "Unknown eval result", severity = ScriptDiagnostic.Severity.ERROR)))
            }
        }
    }
}