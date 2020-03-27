package com.faendir.om.online.remote

import java.rmi.Remote
import java.rmi.RemoteException

interface RemoteInterface : Remote {

    @Throws(RemoteException::class)
    fun getWork(id: String): String?

    @Throws(RemoteException::class)
    fun returnResult(id: String, result: RemoteResult)
}