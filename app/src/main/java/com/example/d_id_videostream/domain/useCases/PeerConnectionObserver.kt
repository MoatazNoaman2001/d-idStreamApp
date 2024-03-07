package com.example.d_id_videostream.domain.useCases

import android.util.Log
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnection.IceConnectionState
import org.webrtc.RtpReceiver

private const val TAG = "PeerConnectionObserver"
class PeerConnectionObserver: PeerConnection.Observer {
    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
        Log.d(TAG, "onSignalingChange: ${p0?.toString()}")
    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
        Log.d(TAG, "onIceConnectionChange: ${p0}")
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
        Log.d(TAG, "onIceConnectionReceivingChange: ${p0}")
    }

    override fun onIceGatheringChange(iceConnectionState: PeerConnection.IceGatheringState?) {
        Log.d(TAG, "onIceGatheringChange: ${iceConnectionState?.name}")
        if(iceConnectionState != null){
            if(iceConnectionState.ordinal == PeerConnection.IceConnectionState.CONNECTED.ordinal){
                Log.d(TAG, "onIceGatheringChange: connected")
            }
            else if(iceConnectionState.ordinal == PeerConnection.IceConnectionState.CLOSED.ordinal){
                Log.d(TAG, "onIceGatheringChange: closed")

            }
            else if(iceConnectionState.ordinal == PeerConnection.IceConnectionState.FAILED.ordinal){
                Log.d(TAG, "onIceGatheringChange: failed")

            }
        }
    }

    override fun onIceCandidate(p0: IceCandidate?) {
        Log.d(TAG, "onIceCandidate: ${p0.toString()}")
        p0?.let {
            val candidate = it.sdp
            val sdpMid = it.sdpMid
            val sdpMLineIndex = it.sdpMLineIndex

        }
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
        Log.d(TAG, "onAddTrack: p0 : ${p0.toString()} , p1: ${p1?.joinToString()}")
    }
}