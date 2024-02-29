package com.example.d_id_videostream.presentation.viewModel

import com.example.d_id_videostream.data.remote.RemoteStream
import com.example.d_id_videostream.data.repo.Resource
import com.example.d_id_videostream.domain.model.Answer
import kotlinx.coroutines.flow.Flow

interface ActivityViewModel {
    fun createNewStream(source_id: String): Flow<Resource<RemoteStream>>
    fun startNewStream(answer: Answer, id:String): Flow<Resource<Any>>
}