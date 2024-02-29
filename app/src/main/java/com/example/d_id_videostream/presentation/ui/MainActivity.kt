package com.example.d_id_videostream.presentation.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.viewModelScope
import com.example.d_id_videostream.data.remote.RemoteStream
import com.example.d_id_videostream.data.repo.Resource
import com.example.d_id_videostream.databinding.ActivityMainBinding
import com.example.d_id_videostream.domain.model.Answer
import com.example.d_id_videostream.domain.useCases.PeerConnectionObserver
import com.example.d_id_videostream.presentation.viewModel.ActivityViewModelImpl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import java.lang.IllegalArgumentException

private const val TAG = "MainActivity"
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val viewModel: ActivityViewModelImpl by viewModels()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val init = PeerConnectionFactory.InitializationOptions.builder(applicationContext)
            .setEnableInternalTracer(true)
            .setFieldTrials("WebRTC-H264HighProfile/Enable/")
            .createInitializationOptions()
        PeerConnectionFactory.initialize(init)

        MainScope().launch {
            viewModel.createNewStream("").collectLatest {
                when (it) {
                    is Resource.Failed -> {
                        binding.label.setText("Failed : ${it.message}")
                    }

                    is Resource.Loading -> {
                        binding.label.setText("Loading...")
                    }

                    is Resource.Success -> {
                        binding.label.text = it.data.id + "\n" + it.data.iceServers.joinToString()
                        val stunUrl = it.data.iceServers.filter { it.urls is String }[0].urls as String
                        val turnUrls = it.data.iceServers.filter { it.urls is List<*> }.flatMap {
                            it.urls as List<String>
                        }

                        Log.d(TAG, "onCreate: id: ${it.data.id}")
                        Log.d(TAG, "onCreate: sessionId:س ${it.data.sessionId}")
                        Log.d(TAG, "onCreate: stunurl: $stunUrl")
                        Log.d(TAG, "onCreate: turnUrls: $turnUrls")

                        val iceServers = mutableListOf<PeerConnection.IceServer>()
//                        iceServers.add(PeerConnection.IceServer.builder(stunUrl).createIceServer())

                        Log.d(TAG, "onCreate: username : ${it.data.iceServers.joinToString { it.username.toString() }}")
                        Log.d(TAG, "onCreate: credential : ${it.data.iceServers.joinToString { it.credential.toString() }}")

                        turnUrls.forEach { url ->
                            iceServers.add(PeerConnection.IceServer.builder(url).apply {
                                setUsername(it.data.iceServers[1].username)
                                setPassword(it.data.iceServers[1].credential)
                            }.createIceServer())
                        }
                        val peerConnection = createPeerConnection(SessionDescription(SessionDescription.Type.OFFER, it.data.offer.sdp), iceServers, it.data)
                        Log.d(TAG, "onCreate: peer connection ${peerConnection?.localDescription?.description}")

                    }
                }
            }
        }
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
                setRemoteDescription(RemoteDescSdpObserver(this, viewModel, remoteStream) , offer)
            }
            return peerConnection
        }catch (e:Exception){
            Log.d(TAG, "createPeerConnection: ${e.message}")
            return null
        }

    }

    internal class RemoteDescSdpObserver(val peerConnection: PeerConnection , val viewModelImpl: ActivityViewModelImpl , val remoteStream: RemoteStream) : SdpObserver{
        override fun onCreateSuccess(p0: SessionDescription?) {
            Log.d(TAG, "onCreateSuccess: p0: $p0")
        }

        override fun onSetSuccess() {
            Log.d(TAG, "onSetSuccess: your remote desc is set successfully")
            peerConnection.createAnswer(object : SdpObserver{
                override fun onCreateSuccess(p0: SessionDescription?) {
                    Log.d(TAG, "onCreateSuccess: answer: ${p0?.description}")
                    peerConnection.setLocalDescription(object : SdpObserver {
                        override fun onCreateSuccess(localSessionDescription: SessionDescription?) {
                            Log.d(TAG, "onCreateSuccess: Local description is created")

                            // Do something with localSessionDescription if needed
                        }

                        override fun onSetSuccess() {
                            Log.d(TAG, "onSetSuccess: Local description is set successfully")
                            p0?.description?.let {
                                Answer(SessionDescription.Type.ANSWER.name ,
                                    it
                                )
                            }?.let {
                                viewModelImpl.viewModelScope.launch {
                                    viewModelImpl.startNewStream(it , remoteStream.id).collectLatest {
                                        when (it) {
                                            is Resource.Failed -> {
                                                Log.d(TAG, "onCreateSuccess: starting session failed: ${it.message}")
                                            }
                                            is Resource.Loading -> {
                                                Log.d(TAG, "onCreateSuccess: start session loading")
                                            }
                                            is Resource.Success -> {
                                                Log.d(TAG, "onCreateSuccess: start session succeeded")
                                                Log.d(TAG, "onCreateSuccess: ${it.data.toString()}")
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        override fun onCreateFailure(p0: String?) {
                            Log.e(TAG, "onCreateFailure: Failed to create local description")
                        }

                        override fun onSetFailure(p0: String?) {
                            Log.e(TAG, "onSetFailure: Failed to set local description")
                        }
                    }, p0)
                }

                override fun onSetSuccess() {

                }

                override fun onCreateFailure(p0: String?) {

                }

                override fun onSetFailure(p0: String?) {

                }

            } , MediaConstraints())
        }

        override fun onCreateFailure(p0: String?) {

        }

        override fun onSetFailure(p0: String?) {

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
}