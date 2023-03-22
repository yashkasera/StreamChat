package com.yashkasera.streamchat.data.model

import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.yashkasera.streamchat.AppObjectController
import java.io.File
import java.io.IOException


class AudioRecorder {
    private val file = File.createTempFile("recording_", ".3gp")
    private var onAudioRecordListener: OnAudioRecordListener? = null
    private var mStartingTimeMillis: Long = 0
    fun setOnAudioRecordListener(onAudioRecordListener: OnAudioRecordListener?) {
        this.onAudioRecordListener = onAudioRecordListener
    }

    var mediaRecorder: MediaRecorder? = null
    fun startRecording() {
        mStartingTimeMillis = System.currentTimeMillis()
        try {
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                MediaRecorder(AppObjectController.streamChatApplication)
            else MediaRecorder()
            mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mediaRecorder!!.setOutputFile(file.absolutePath)
            mediaRecorder!!.prepare()
            mediaRecorder!!.start()
            onAudioRecordListener?.onRecordingStarted()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "prepare() failed")
        }
    }


    fun stopRecording(cancel: Boolean) {
        mediaRecorder?.stop()
        mediaRecorder?.release();
        mediaRecorder = null
        if (file.length() == 0L) {
            onAudioRecordListener?.onError(Exception("file is null !"))
            return
        }
        val mElapsedMillis = System.currentTimeMillis() - mStartingTimeMillis
        val recordingItem = RecordingItem(name = file.name, filePath = file.absolutePath, file = file)
        recordingItem.length = mElapsedMillis.toInt()
        recordingItem.time = System.currentTimeMillis()
        if (cancel) {
            deleteFile()
            return
        }
        onAudioRecordListener?.onRecordFinished(recordingItem) ?: throw IOException("Unable to save file!")
    }

    private fun deleteFile() {
        if (file.exists()) Log.d(TAG, String.format("deleting file success %b ", file.delete()))
    }

    companion object {
        private const val TAG = "AudioRecording"
    }
}

interface OnAudioRecordListener {
    fun onRecordFinished(recordingItem: RecordingItem) {}
    fun onError(exception: Exception) {}
    fun onRecordingStarted() {}
}

data class RecordingItem(
    var name: String? = null,
    var filePath: String,
    var file: File? = null,
    var id: Int = 0,
    var length: Int = 0,
    var time: Long = 0,
)