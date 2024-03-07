package com.example.d_id_videostream.domain.model

import com.google.gson.annotations.SerializedName
import org.webrtc.SessionDescription
import java.io.Serializable

data class StartSteamRequest(
    @SerializedName("answer")
    val answer: SessionDescription,
    @SerializedName("session_id")
    val sessionId: String
) :Serializable