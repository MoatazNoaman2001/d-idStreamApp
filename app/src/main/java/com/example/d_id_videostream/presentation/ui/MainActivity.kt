package com.example.d_id_videostream.presentation.ui

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.d_id_videostream.commons.network.IDNetwork
import com.example.d_id_videostream.data.remote.RemoteStream
import com.example.d_id_videostream.data.repo.Resource
import com.example.d_id_videostream.databinding.ActivityMainBinding
import com.example.d_id_videostream.domain.model.MySdpObserver
import com.example.d_id_videostream.domain.model.StartSteamRequest
import com.example.d_id_videostream.domain.useCases.GetD_IDPeerConnection
import com.example.d_id_videostream.domain.useCases.PeerConnectionObserver
import com.example.d_id_videostream.presentation.viewModel.ActivityViewModelImpl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SessionDescription
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

private const val TAG = "MainActivity"
@AndroidEntryPoint
class MainActivity : AppCompatActivity(), GetD_IDPeerConnection.SessionListener {
    lateinit var binding: ActivityMainBinding
    private val viewModel: ActivityViewModelImpl by viewModels()

    @Inject
    lateinit var idNetwork: IDNetwork
    lateinit var getdIdpeerconnection: GetD_IDPeerConnection

    lateinit var permisionsRequest : ActivityResultLauncher<Array<String>>
    private var peermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel.currentState = {
            binding.label.text = it
        }

        permisionsRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            if (it.all { it.value }){
                MainScope().launch {
                    viewModel.createNewStream("https://img.freepik.com/free-photo/portrait-white-man-isolated_53876-40306.jpg?size=626&ext=jpg&ga=GA1.1.98259409.1708819200&semt=ais").collectLatest {
                        when (it) {
                            is Resource.Failed -> {
                                viewModel.currentState?.invoke("Failed : ${it.message}")
                            }

                            is Resource.Loading -> {
                                viewModel.currentState?.invoke("Loading...")
                            }

                            is Resource.Success -> {
                                viewModel.currentState?.invoke("id: ${it.data.id}, \n,ice servers: ${it.data.iceServers[0]}")


                                val stunUrl = it.data.iceServers.filter { it.urls is String }[0].urls as String
                                val turnUrls = it.data.iceServers.filter { it.urls is List<*> }.flatMap {
                                    it.urls as List<String>
                                }

                                Log.d(TAG, "onCreate: id: ${it.data.id}")
                                Log.d(TAG, "onCreate: sessionId: ${it.data.sessionId}")
                                Log.d(TAG, "onCreate: stunurl: $stunUrl")
                                Log.d(TAG, "onCreate: turnUrls: $turnUrls")
                                Log.d(TAG, "onCreate: username : ${it.data.iceServers.joinToString { it.username.toString() }}")
                                Log.d(TAG, "onCreate: credential : ${it.data.iceServers.joinToString { it.credential.toString() }}")

                                val iceServers = mutableListOf<PeerConnection.IceServer>()
                                iceServers.add(PeerConnection.IceServer.builder(stunUrl).createIceServer())


                                turnUrls.forEach { url ->
                                    iceServers.add(PeerConnection.IceServer.builder(url).apply {
                                        setUsername(it.data.iceServers[1].username)
                                        setPassword(it.data.iceServers[1].credential)
                                    }.createIceServer())
                                }
                                getdIdpeerconnection = GetD_IDPeerConnection(this@MainActivity , PeerConnection.RTCConfiguration(iceServers), this@MainActivity, it.data)
                                getdIdpeerconnection.setRemoteSession(
                                    SessionDescription(
                                        SessionDescription.Type.OFFER ,
                                        it.data.offer.sdp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        permisionsRequest.launch(peermissions)

    }

    private val eglBaseContext = EglBase.create().eglBaseContext
    fun createPeerConnection(
        offer: SessionDescription,
        iceServers: List<PeerConnection.IceServer>,
        remoteStream: RemoteStream
    ): PeerConnection? {
        try {
            val peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(PeerConnectionFactory.Options())
                .setVideoDecoderFactory(
                    DefaultVideoDecoderFactory(eglBaseContext)
                ).setVideoEncoderFactory(
                    DefaultVideoEncoderFactory(
                        eglBaseContext, true, true
                    )
                ).setOptions(PeerConnectionFactory.Options().apply {
                    disableNetworkMonitor = false
                    disableEncryption = false
                })
                .createPeerConnectionFactory()
            val rtc_connection = PeerConnection.RTCConfiguration(iceServers)
            val peerConnection = peerConnectionFactory.createPeerConnection(rtc_connection, PeerConnectionObserver())?.apply {
                Log.d(TAG, "createPeerConnection: $this")
                setRemoteDescription(RemoteDescSdpObserver(this, viewModel, remoteStream , idNetwork) , offer)
            }
            return peerConnection
        }catch (e:Exception){
            Log.d(TAG, "createPeerConnection: ${e.message}")
            return null
        }

    }

    internal class RemoteDescSdpObserver(
        val peerConnection: PeerConnection,
        val viewModelImpl: ActivityViewModelImpl,
        val remoteStream: RemoteStream,
        val idNetwork: IDNetwork
    ) : MySdpObserver(){

        override fun onSetSuccess() {
            Log.d(TAG, "onSetSuccess: your remote desc is set successfully")
            peerConnection.createAnswer(object : MySdpObserver(){
                override fun onCreateSuccess(p0: SessionDescription?) {
                    viewModelImpl.currentState?.invoke("answer created")
                    Log.d(TAG, "onCreateSuccess: answer: ${p0?.description}")
                    peerConnection.setLocalDescription(object : MySdpObserver() {
                        override fun onSetSuccess() {
                            Log.d(TAG, "onSetSuccess: Local description is set successfully")
                        }
                    }, p0)
                }
            } , MediaConstraints())
        }
    }

    private fun createPeerConnectionInternal(iceServers: List<PeerConnection.IceServer>): PeerConnection? {

       return try{
           val peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory()
           val rtc_connection = PeerConnection.RTCConfiguration(iceServers)
           val peerConnection = peerConnectionFactory.createPeerConnection(rtc_connection, PeerConnectionObserver())
           peerConnection
       }catch (E:Exception){
           Log.d(TAG, "createPeerConnectionInternal: ${E.message}")
           return null
       }
    }

    override fun onComplete(desc: SessionDescription, data:RemoteStream) {
        Log.d(TAG, "onComplete: ${desc.description}")
        idNetwork.startStream(startSteamRequest =
        //just try
            StartSteamRequest(answer =desc, sessionId = data.sessionId)
            , id =data.id).enqueue(object : Callback<String>{
            override fun onResponse(call: Call<String>, response: Response<String>) {
                val errorBody = String(response.errorBody()?.bytes()!!)
                Log.d(TAG, "onResponse: "  +  "code: ${response.code()} \n" +
                        "message: ${response.message()} \n"+
                        "error body : $errorBody \n"+
                        "response body: ${response.body()} \n" +
                        "raw: ${response.raw()}")
                viewModel.currentState?.invoke(
                    "code: ${response.code()} \n" +
                            "message: ${response.message()} \n"+
                            "error body : $errorBody \n"+
                            "response body: ${response.body()} \n" +
                            "raw: ${response.raw()}"
                )
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                viewModel.currentState?.invoke(t.message!! +"\t" + t.cause?.message)
            }
        })
    }
}

