package com.example.d_id_videostream.commons.network

import com.example.d_id_videostream.commons.Constants
import com.example.d_id_videostream.data.remote.CreateStream
import com.example.d_id_videostream.data.remote.RemoteStream
import com.example.d_id_videostream.domain.model.StartSteamRequest
import com.example.d_id_videostream.domain.model.CreateTalkStream
import com.example.d_id_videostream.domain.model.StartTalkStreamResponse
import com.example.d_id_videostream.domain.model.SubmitStreamRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Path

interface IDNetwork {

    companion object {
        val BaseUrl = "https://api.d-id.com/talks/"
    }

    @POST("streams/")
    suspend fun createStream(
        @HeaderMap header: Map<String, String> = Constants.header,
        @Body createStream: CreateStream
    ): RemoteStream

    @POST("streams/{id}/sdp")
    fun startStream(
        @HeaderMap header: Map<String, String> = Constants.header,
        @Body startSteamRequest: StartSteamRequest,
        @Path("id") id: String
    ): Call<String>


    @POST("streams/{id}/ice")
    fun submitNetworkInfo(
        @HeaderMap header: Map<String , String> = Constants.header,
        @Path("id") id:String,
        @Body submitStreamRequest: SubmitStreamRequest
    ) : String

    @POST("streams/{id}")
    fun startTalkStream(
        @HeaderMap header: Map<String , String> = Constants.header,
        @Path("id") id:String,
        @Body createStream: CreateTalkStream
    ) : StartTalkStreamResponse

    @DELETE("streams/{id}")
    fun deleteTalkStream(
        @HeaderMap header: Map<String , String> = Constants.header,
        @Path("id") id:String
    ) : String
}