package com.faendir.om.dsl

import kotlinx.coroutines.runBlocking
import java.lang.reflect.InvocationTargetException
import java.security.AccessControlContext
import java.security.AccessController
import java.security.PrivilegedAction
import kotlin.reflect.KClass
import kotlin.script.experimental.api.*

class SecureScriptEvaluator(private val scriptSecurityContext: AccessControlContext, private val classLoader: ClassLoader) : ScriptEvaluator {

    override suspend operator fun invoke(compiledScript: CompiledScript, scriptEvaluationConfiguration: ScriptEvaluationConfiguration): ResultWithDiagnostics<EvaluationResult> {
        return try {
            val saveClassLoader = Thread.currentThread().contextClassLoader
            Thread.currentThread().contextClassLoader = classLoader
            val clazz = try {
                AccessController.doPrivileged(PrivilegedAction {
                    runBlocking { compiledScript.getClass(scriptEvaluationConfiguration) }
                }, scriptSecurityContext)
            } finally {
                Thread.currentThread().contextClassLoader = saveClassLoader
            }
            clazz.onSuccess { scriptClass ->

                compiledScript.otherScripts.mapSuccess {
                    invoke(it, scriptEvaluationConfiguration)
                }.onSuccess { importedScriptsEvalResults ->

                    val refinedEvalConfiguration =
                        scriptEvaluationConfiguration.refineBeforeEvaluation(compiledScript).valueOr {
                            return@invoke ResultWithDiagnostics.Failure(it.reports)
                        }

                    val resultValue = try {
                        val instance =
                            scriptClass.evalWithConfigAndOtherScriptsResults(refinedEvalConfiguration, importedScriptsEvalResults)

                        compiledScript.resultField?.let { (resultFieldName, resultType) ->
                            val resultField = scriptClass.java.getDeclaredField(resultFieldName).apply { isAccessible = true }
                            ResultValue.Value(resultFieldName, resultField.get(instance), resultType.typeName, scriptClass, instance)
                        } ?: ResultValue.Unit(scriptClass, instance)

                    } catch (e: InvocationTargetException) {
                        ResultValue.Error(e.targetException ?: e, e, scriptClass)
                    }

                    EvaluationResult(resultValue, refinedEvalConfiguration).let { ResultWithDiagnostics.Success(it) }
                }
            }
        } catch (e: Throwable) {
            ResultWithDiagnostics.Failure(
                e.asDiagnostics(path = compiledScript.sourceLocationId)
            )
        }
    }

    private fun KClass<*>.evalWithConfigAndOtherScriptsResults(
        refinedEvalConfiguration: ScriptEvaluationConfiguration,
        importedScriptsEvalResults: List<EvaluationResult>
    ): Any {
        val args = ArrayList<Any?>()

        refinedEvalConfiguration[ScriptEvaluationConfiguration.previousSnippets]?.let {
            if (it.isNotEmpty()) {
                args.add(it.toTypedArray())
            }
        }

        refinedEvalConfiguration[ScriptEvaluationConfiguration.constructorArgs]?.let {
            args.addAll(it)
        }

        importedScriptsEvalResults.forEach {
            args.add(it.returnValue.scriptInstance)
        }

        refinedEvalConfiguration[ScriptEvaluationConfiguration.implicitReceivers]?.let {
            args.addAll(it)
        }
        refinedEvalConfiguration[ScriptEvaluationConfiguration.providedProperties]?.forEach {
            args.add(it.value)
        }

        val ctor = java.constructors.single()

        val saveClassLoader = Thread.currentThread().contextClassLoader
        Thread.currentThread().contextClassLoader = classLoader
        return try {
            AccessController.doPrivileged(PrivilegedAction { ctor.newInstance(*args.toArray()) }, scriptSecurityContext)
        } finally {
            Thread.currentThread().contextClassLoader = saveClassLoader
        }
    }
}