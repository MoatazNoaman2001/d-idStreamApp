package com.example.d_id_videostream.domain.useCases

import android.app.Application
import android.content.Context
import com.example.d_id_videostream.domain.useCases.PeerConnectionObserver
import org.webrtc.Camera2Enumerator
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoCapturer
import java.lang.IllegalArgumentException
import javax.inject.Inject

class GetPeerConnectionUseCase @Inject constructor(val application: Application){
    private val eglContext = EglBase.create()
    private val peerConnectionFactory by lazy { createPeerConnectionFactory() }
    private val iceServer = listOf(
        PeerConnection.IceServer.builder("stun:iphone-stun.strato-iphone.de:3478").createIceServer()
    )
    private val peerConnection by lazy { createPeerConnection(PeerConnectionObserver()) }
    private val videoLocalSource by lazy{peerConnectionFactory.createVideoSource(false)}
    private val audioLocalSource by lazy{peerConnectionFactory.createAudioSource(MediaConstraints())}


    init {
        establishAnConnection()
    }
    private fun establishAnConnection(){
        val initOpt = PeerConnectionFactory.InitializationOptions.builder(application.applicationContext)
            .setEnableInternalTracer(true)
            .setFieldTrials("WebRTC-H264HighProfile/Enable/")
            .createInitializationOptions()
        PeerConnectionFactory.initialize(initOpt)
    }

    private fun createPeerConnectionFactory() : PeerConnectionFactory {
        return PeerConnectionFactory.builder()
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglContext.eglBaseContext , true , true))
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglContext.eglBaseContext))
            .setOptions(PeerConnectionFactory.Options().apply { disableEncryption = true; disableNetworkMonitor = true })
            .createPeerConnectionFactory()
    }

    private fun createPeerConnection(observer: PeerConnection.Observer): PeerConnection?{
        return peerConnectionFactory.createPeerConnection(iceServer , observer)
    }

    private fun initializeSurfaceView(surface: SurfaceViewRenderer){
        surface.run {
            setEnableHardwareScaler(true)
            setMirror(true)
            init(eglContext.eglBaseContext, null)
        }
    }

    private fun startLocalVideo(surface: SurfaceViewRenderer){
        val surfaceTexture = SurfaceTextureHelper.create(Thread.currentThread().name , eglContext.eglBaseContext)
        val videoCapture = getVideoCapture(application)
        videoCapture.initialize(surfaceTexture , surface.context , videoLocalSource.capturerObserver)
        videoCapture.startCapture(320 , 240 , 30)
        val localVideoTracker = peerConnectionFactory.createVideoTrack("local_video" , videoLocalSource)
        localVideoTracker.addSink(surface)
        val localAudioTracker = peerConnectionFactory.createAudioTrack("local_audio" , audioLocalSource)
        val localStream = peerConnectionFactory.createLocalMediaStream("local_stream")
        localStream.addTrack(localAudioTracker)
        localStream.addTrack(localVideoTracker)

        peerConnection?.addStream(localStream)
    }
    fun getVideoCapture(application: Application): VideoCapturer {
        return Camera2Enumerator(application.applicationContext).run {
            deviceNames.find {
                isFrontFacing(it)
            }?.let {
                createCapturer(it, null)
            }?: throw IllegalArgumentException()
        }
    }
    operator fun invoke(name:String, surface: SurfaceViewRenderer): PeerConnection?{
        initializeSurfaceView(surface)
        startLocalVideo(surface)
        return peerConnection
    }
}