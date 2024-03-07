package com.example.d_id_videostream.domain.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CreateTalkStream(
    @SerializedName("session_id")
    val sessionID:String,
    val status:String
) :Serializable