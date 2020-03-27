package com.faendir.om.online.remote

import java.io.Serializable

sealed class RemoteResult : Serializable {
    class Success(val value: ByteArray) : RemoteResult()
    class Failure(val exception: Throwable) : RemoteResult()
}