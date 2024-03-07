package com.example.d_id_videostream.domain.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SubmitStreamRequest(
    val candidate : String,
    val sdpMid : String,
    @SerializedName("sdpMLineIndex")
    val sdpMLineIndex: Int,
    @SerializedName("session_id")
    val sessionID : String
) :Serializable