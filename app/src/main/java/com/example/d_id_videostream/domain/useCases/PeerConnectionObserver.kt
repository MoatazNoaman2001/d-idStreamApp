package com.example.d_id_videostream.domain.useCases

import android.util.Log
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.RtpReceiver

private const val TAG = "PeerConnectionObserver"
class PeerConnectionObserver: PeerConnection.Observer {
    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
        Log.d(TAG, "onSignalingChange: ${p0?.toString()}")
    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
        Log.d(TAG, "onIceConnectionChange: ${p0?.name}")
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
        Log.d(TAG, "onIceConnectionReceivingChange: ${p0}")
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
        Log.d(TAG, "onIceGatheringChange: ${p0?.name}")
    }

    override fun onIceCandidate(p0: IceCandidate?) {
        Log.d(TAG, "onIceCandidate: ${p0.toString()}")
    }

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
        Log.d(TAG, "onIceCandidatesRemoved: ${p0?.joinToString()}")
    }

    override fun onAddStream(p0: MediaStream?) {
        Log.d(TAG, "onAddStream: ${p0.toString()}")
    }

    override fun onRemoveStream(p0: MediaStream?) {
    }

    override fun onDataChannel(p0: DataChannel?) {
        Log.d(TAG, "onDataChannel: ${p0.toString()}")
    }

    override fun onRenegotiationNeeded() {
    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
    }
}