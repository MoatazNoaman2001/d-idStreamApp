package com.example.d_id_videostream.commons.network

import com.example.d_id_videostream.commons.Constants
import com.example.d_id_videostream.data.remote.CreateStream
import com.example.d_id_videostream.data.remote.RemoteStream
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.HeaderMap
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Part

interface IDNetwork {

    companion object{
        val BaseUrl = "https://api.d-id.com/talks/"
    }
    @POST("streams/")
    suspend fun createStream(@HeaderMap header: Map<String , String> = Constants.header,@Body createStream: CreateStream) : RemoteStream
}