package com.example.d_id_videostream.data.repo
import android.os.Build
import androidx.annotation.RequiresExtension
import com.example.d_id_videostream.commons.network.IDNetwork
import com.example.d_id_videostream.data.remote.CreateStream
import com.example.d_id_videostream.data.remote.RemoteStream
import com.example.d_id_videostream.domain.model.Answer
import java.io.IOException
import javax.inject.Inject


class IdRepoImpl @Inject constructor(val idNetwork: IDNetwork): IdRepo {
    override suspend fun createStream(source_id: String): Resource<RemoteStream> {
        return try {
            Resource.Success(idNetwork.createStream(createStream = CreateStream(source_id)))
        }catch (e:IOException){
            Resource.Failed(e.message!!)
        }
    }
//
//    override suspend fun startNewStream(): Resource<Any> {
//        TODO("Not yet implemented")
//    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun startNewStream(answer: Answer, id:String): Resource<Any> {
        return Resource.Loading()
    }
}