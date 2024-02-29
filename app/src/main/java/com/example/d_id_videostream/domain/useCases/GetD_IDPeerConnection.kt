package com.example.d_id_videostream.domain.useCases

import android.content.Context
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection.IceServer
import org.webrtc.PeerConnection.RTCConfiguration
import org.webrtc.PeerConnectionFactory
import javax.inject.Inject

class GetD_IDPeerConnection @Inject constructor(
    val context: Context,
    val rtcConfiguration: RTCConfiguration
) {
    private val peerConnectionFactory by lazy { createPeerConnectionFactory() }
    private val peerConnection by lazy { peerConnectionFactory.createPeerConnection(rtcConfiguration , CustomPeerConnectionServer()) }

    private val mediaConstraint = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo","true"))
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio","true"))
    }


    private val eglBaseContext = EglBase.create().eglBaseContext
    init {
        initPeerConnectionFactory()
    }





    private fun createPeerConnectionFactory() : PeerConnectionFactory{
        return PeerConnectionFactory.builder()
            .setVideoDecoderFactory(
                DefaultVideoDecoderFactory(eglBaseContext)
            ).setVideoEncoderFactory(
                DefaultVideoEncoderFactory(
                    eglBaseContext, true, true
                )
            ).setOptions(PeerConnectionFactory.Options().apply {
                disableNetworkMonitor = false
                disableEncryption = false
            }).createPeerConnectionFactory()
    }
    private fun initPeerConnectionFactory(){
        val init = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .setFieldTrials("WebRTC-H264HighProfile/Enable/")
            .createInitializationOptions()
        PeerConnectionFactory.initialize(init)
    }
}