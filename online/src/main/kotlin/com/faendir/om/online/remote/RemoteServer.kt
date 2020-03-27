package com.faendir.om.online.remote

import com.faendir.om.sp.solution.Solution
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.RuntimeException
import java.rmi.registry.LocateRegistry
import java.rmi.server.UnicastRemoteObject
import java.util.*
import java.util.concurrent.TimeoutException
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptDiagnostic

object RemoteServer : UnicastRemoteObject(), RemoteInterface {
    private val work = mutableMapOf<UUID, String>()
    private val resultHandlers = mutableMapOf<UUID, (RemoteResult) -> Unit>()

    const val NAME = "//localhost/OmServer"

    init {
        val registry = LocateRegistry.createRegistry(1099)
        registry.rebind(NAME, this)
    }

    fun fromDsl(dsl: String, resultHandler: (RemoteResult) -> Unit) {
        val uuid = UUID.randomUUID()
        work[uuid] = dsl
        resultHandlers[uuid] = resultHandler
        GlobalScope.launch {
            JvmStarter.exec(RemoteClient::class.java, uuid.toString())
            work.remove(uuid)
            if(resultHandlers.containsKey(uuid)) {
                resultHandlers.remove(uuid)?.invoke(RemoteResult.Failure(TimeoutException("Remote jvm died without returning a result.")))
            }
        }
    }

    override fun getWork(id: String): String? {
        return work.remove(UUID.fromString(id))
    }

    override fun returnResult(id: String, result: RemoteResult) {
        val uuid = UUID.fromString(id)
        resultHandlers.remove(uuid)?.invoke(result)
    }
}