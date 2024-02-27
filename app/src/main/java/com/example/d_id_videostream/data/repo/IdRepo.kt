package com.example.d_id_videostream.data.repo

import com.example.d_id_videostream.data.remote.RemoteStream

interface IdRepo {
    suspend fun createStream() : Resource<RemoteStream>
}

sealed class Resource<T>(data:T?= null , message:String? = null){
    data class Success<T>(val data: T) : Resource<T>(data = data)
    data class Failed<T>(val message: String) : Resource<T>(message = message)
    class Loading<T>() : Resource<T>()
}