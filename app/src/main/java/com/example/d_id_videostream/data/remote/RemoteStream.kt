package com.example.d_id_videostream.data.remote

import com.example.d_id_videostream.data.remote.Offer
import com.example.d_id_videostream.data.remote.IceServer
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RemoteStream(
    @SerializedName("ice_servers")
    val iceServers: List<IceServer>,
    val id: String,
    val offer: Offer,
    @SerializedName("session_id")
    val sessionId: String,
): Serializable{
    constructor() : this (emptyList() , "" , Offer("" , "") , "")
}