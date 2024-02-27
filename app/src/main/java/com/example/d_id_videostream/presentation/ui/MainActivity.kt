package com.example.d_id_videostream.presentation.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.example.d_id_videostream.data.repo.Resource
import com.example.d_id_videostream.databinding.ActivityMainBinding
import com.example.d_id_videostream.domain.useCases.PeerConnectionObserver
import com.example.d_id_videostream.presentation.viewModel.ActivityViewModelImpl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import java.lang.IllegalArgumentException

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
                        binding.label.text = it.data.iceServers.filter {
                            it.urls is String
                        }.map {
                            if (it.urls is List<*>) PeerConnection.IceServer.builder(it.urls[0].toString())
                                .createIceServer() else PeerConnection.IceServer.builder(it.urls.toString())
                                .createIceServer()
                        }.toList()[0].toString()
//                        val peerConnection = createPeerConnection(
//                            SessionDescription(SessionDescription.Type.OFFER, it.data.offer.sdp),
//                            it.data.iceServers.filter {
//                                it.urls is String
//                            }.map {
//                                if (it.urls is List<*>) PeerConnection.IceServer.builder(it.urls[0].toString())
//                                    .createIceServer() else PeerConnection.IceServer.builder(it.urls.toString())
//                                    .createIceServer()
//                            }.toList()
//                        )
//                        binding.label.setText("Fetched Successfully: ${peerConnection.localDescription}")


                    }
                }
            }
        }
    }

    suspend fun createPeerConnection(
        offer: SessionDescription,
        iceServers: List<PeerConnection.IceServer>
    ): PeerConnection {
        val peerConnection: PeerConnection = createPeerConnectionInternal(iceServers)
        peerConnection.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {

            }

            override fun onSetSuccess() {
                peerConnection.createAnswer(object : SdpObserver {
                    override fun onCreateSuccess(sessionDescription: SessionDescription) {
                        peerConnection.setLocalDescription(object : SdpObserver {
                            override fun onCreateSuccess(p0: SessionDescription?) {
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

    private fun createPeerConnectionInternal(iceServers: List<PeerConnection.IceServer>): PeerConnection {

        val peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory()
        val peerConnection = peerConnectionFactory.createPeerConnection(iceServers, PeerConnectionObserver())
        if (peerConnection == null)
            throw IllegalArgumentException("peer failed to create: " + iceServers.joinToString())
        return peerConnection
    }
}