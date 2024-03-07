package com.example.d_id_videostream.presentation.viewModel

import androidx.lifecycle.ViewModel
import com.example.d_id_videostream.data.remote.RemoteStream
import com.example.d_id_videostream.data.repo.Resource
import com.example.d_id_videostream.domain.model.Answer
import com.example.d_id_videostream.domain.useCases.CreateStreamUseCase
import com.example.d_id_videostream.domain.useCases.GetD_IDPeerConnection
import com.example.d_id_videostream.domain.useCases.StartNewStreamUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ActivityViewModelImpl @Inject constructor(
    val createStreamUseCase: CreateStreamUseCase,
    val startNewStreamUseCase: StartNewStreamUseCase
): ActivityViewModel , ViewModel(){

    var currentState : ((String)->Unit)?=null
    override fun createNewStream(source_id: String): Flow<Resource<RemoteStream>> = createStreamUseCase(source_id)
    override fun startNewStream(answer: Answer, id:String): Flow<Resource<Any>> = startNewStreamUseCase(answer , id)
}