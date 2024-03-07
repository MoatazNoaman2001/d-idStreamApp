package com.example.d_id_videostream.domain.useCases

import com.example.d_id_videostream.data.repo.IdRepoImpl
import com.example.d_id_videostream.data.repo.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class CreateStreamUseCase @Inject constructor(val idRepo: IdRepoImpl) {

    operator fun invoke(source_id: String) = flow {
        emit(Resource.Loading())
        emit(idRepo.createStream(source_id))
    }
        .flowOn(Dispatchers.IO)
        .catch { emit(Resource.Failed(it.message!!)) }
}