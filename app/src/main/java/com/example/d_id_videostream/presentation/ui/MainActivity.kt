package com.example.d_id_videostream.presentation.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.example.d_id_videostream.data.repo.Resource
import com.example.d_id_videostream.databinding.ActivityMainBinding
import com.example.d_id_videostream.domain.useCases.PeerConnectionObserver
import com.example.d_id_videostream.presentation.viewModel.ActivityViewModelImpl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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
                        Log.d(TAG, "onCreate: stunurl: $stunUrl")
                        Log.d(TAG, "onCreate: turnUrls: $turnUrls")

                        val iceServers = mutableListOf<PeerConnection.IceServer>()

                        // Create ICE server objects
                        iceServers.add(PeerConnection.IceServer.builder(stunUrl).createIceServer())
                        turnUrls.forEach { url ->
                            iceServers.add(PeerConnection.IceServer.builder(url).createIceServer())
                        }


                        CoroutineScope(Dispatchers.Unconfined).launch {
                            val peerConnection = createPeerConnection(SessionDescription(SessionDescription.Type.OFFER, it.data.offer.sdp), iceServers)
                            Log.d(TAG, "onCreate: ${peerConnection?.localDescription?.description}")
                        }
                    }
                }
            }
        }
    }

    suspend fun createPeerConnection(
        offer: SessionDescription,
        iceServers: List<PeerConnection.IceServer>
    ): PeerConnection? {
        val peerConnection: PeerConnection = createPeerConnectionInternal(iceServers) ?: return null
        peerConnection.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {
                Log.d(TAG, "onCreateSuccess: remote desc is created")
            }

            override fun onSetSuccess() {
                Log.d(TAG, "onCreateSuccess: remote desc is set successfully")
                peerConnection.createAnswer(object : SdpObserver {
                    override fun onCreateSuccess(sessionDescription: SessionDescription) {
                        peerConnection.setLocalDescription(object : SdpObserver {
                            override fun onCreateSuccess(p0: SessionDescription?) {
                                Log.d(TAG, "onCreateSuccess: ${p0?.description}")
                            }

                            override fun onSetSuccess() {

                            }

                            override fun onCreateFailure(p0: String?) {
                            }

                            override fun onSetFailure(p0: String?) {
                            }
                        }, sessionDescription)
                    }

                    override fun onSetSuccess() {

                    }

                    override fun onCreateFailure(p0: String?) {
                    }

                    override fun onSetFailure(p0: String?) {
                    }
                }, MediaConstraints())
            }

            override fun onCreateFailure(p0: String?) {

            }

            override fun onSetFailure(p0: String?) {

            }
        }, offer)
        return peerConnection
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