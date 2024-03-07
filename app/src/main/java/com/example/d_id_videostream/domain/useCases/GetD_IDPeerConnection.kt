package com.example.d_id_videostream.domain.useCases

import android.content.Context
import com.example.d_id_videostream.data.remote.RemoteStream
import com.example.d_id_videostream.domain.model.MySdpObserver
import org.webrtc.AudioTrack
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection.RTCConfiguration
import org.webrtc.PeerConnectionFactory
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer

class GetD_IDPeerConnection(
    val context: Context,
    val rtcConfiguration: RTCConfiguration,
    val sessionListener: SessionListener,
    val data: RemoteStream
) {
    private val peerConnectionFactory by lazy { createPeerConnectionFactory() }
    private val peerConnection by lazy {
        peerConnectionFactory.createPeerConnection(
            rtcConfiguration,
            CustomPeerConnectionServer()
        )
    }
    private val localAudioSource by lazy { peerConnectionFactory.createAudioSource(MediaConstraints()) }

    private lateinit var localSurfaceView: SurfaceViewRenderer
    private lateinit var remoteSurfaceView: SurfaceViewRenderer
    private var localStream: MediaStream? = null
    private var localTrackId = ""
    private var localStreamId = ""
    private var localAudioTrack: AudioTrack? = null

    private val mediaConstraint = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
    }


    private val eglBaseContext = EglBase.create().eglBaseContext

    init {
        initPeerConnectionFactory()
    }

    private fun createPeerConnectionFactory(): PeerConnectionFactory {
        return PeerConnectionFactory.builder()
            .setVideoDecoderFactory(
                DefaultVideoDecoderFactory(eglBaseContext)
            )
            .setVideoEncoderFactory(
                DefaultVideoEncoderFactory(eglBaseContext, true, true)
            )
            .setOptions(PeerConnectionFactory.Options().apply {
                disableNetworkMonitor = false
                disableEncryption = false
            })
            .createPeerConnectionFactory()
    }

    private fun initPeerConnectionFactory() {
        val init = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .setFieldTrials("WebRTC-H264HighProfile/Enable/")
            .createInitializationOptions()
        PeerConnectionFactory.initialize(init)
    }

    fun answer() {
        peerConnection?.createAnswer(object : MySdpObserver() {
            override fun onCreateSuccess(desc: SessionDescription?) {
                super.onCreateSuccess(desc)
                peerConnection?.setLocalDescription(object : MySdpObserver() {
                    override fun onSetSuccess() {
                        super.onSetSuccess()
                        sessionListener.onComplete(desc!!, remoteStream = data)
                    }
                }, desc)
            }
        }, mediaConstraint)
    }

    fun setRemoteSession(sessionDescription: SessionDescription) {
        peerConnection?.setRemoteDescription(object : MySdpObserver() {
            override fun onSetSuccess() {
                super.onSetSuccess()
                answer()
            }
        }, sessionDescription)
    }

    private fun initSurfaceView(view: SurfaceViewRenderer) {
        view.run {
            setMirror(false)
            setEnableHardwareScaler(true)
            init(eglBaseContext, null)
        }
    }


    interface SessionListener {
        fun onComplete(sessionDescription: SessionDescription, remoteStream: RemoteStream)
    }
}