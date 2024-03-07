package com.example.d_id_videostream.domain.model

import java.io.Serializable

data class Answer(val type :String? = "answer", val sdp: String) :Serializable