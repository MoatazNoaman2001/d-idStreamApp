package com.example.d_id_videostream.domain.useCases

import com.example.d_id_videostream.data.remote.RemoteStream
import com.example.d_id_videostream.data.repo.IdRepo
import com.example.d_id_videostream.data.repo.IdRepoImpl
import com.example.d_id_videostream.data.repo.Resource
import com.example.d_id_videostream.domain.model.Answer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class StartNewStreamUseCase @Inject constructor(val idRepo: IdRepoImpl) {

    operator fun invoke(answer: Answer, id:String) = flow {
        emit(Resource.Loading())
        emit(idRepo.startNewStream(answer, id))
    }
        .flowOn(Dispatchers.IO)
        .catch { emit(Resource.Failed(it.message!!)) }
}