package com.example.d_id_videostream.data.repo
import com.example.d_id_videostream.commons.network.IDNetwork
import com.example.d_id_videostream.data.remote.CreateStream
import com.example.d_id_videostream.data.remote.RemoteStream
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
}