package com.example.d_id_videostream.data.repo
import android.net.http.HttpException
import android.os.Build
import androidx.annotation.RequiresExtension
import com.example.d_id_videostream.commons.network.IDNetwork
import com.example.d_id_videostream.data.remote.CreateStream
import com.example.d_id_videostream.data.remote.RemoteStream
import com.example.d_id_videostream.domain.model.Answer
import java.io.IOException
import javax.inject.Inject


class IdRepoImpl @Inject constructor(val idNetwork: IDNetwork): IdRepo {
    override suspend fun createStream() : Resource<RemoteStream> {
        return try {
            Resource.Success(idNetwork.createStream(createStream = CreateStream( "https://img.freepik.com/free-photo/portrait-white-man-isolated_53876-40306.jpg?size=626&ext=jpg&ga=GA1.1.98259409.1708819200&semt=ais")))
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
        return try {
            Resource.Success(idNetwork.startStream(answer = answer, id = id))
        }catch (e:IOException){
            Resource.Failed(e.message!!)
        }catch (e:HttpException){
            Resource.Failed(e.message!!)
        }
    }
}