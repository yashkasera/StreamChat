package com.yashkasera.streamchat.ui.dialogs

import android.content.DialogInterface
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yashkasera.streamchat.R
import com.yashkasera.streamchat.data.model.AudioRecorder
import com.yashkasera.streamchat.data.model.OnAudioRecordListener
import com.yashkasera.streamchat.data.model.RecordingItem
import com.yashkasera.streamchat.databinding.BottomsheetRecordAudioBinding
import com.yashkasera.streamchat.util.showToast

class DialogRecordAudio : BottomSheetDialogFragment(), OnAudioRecordListener {
    private lateinit var binding: BottomsheetRecordAudioBinding
    private val audioRecorder = AudioRecorder()
    private val mediaPlayer: MediaPlayer = MediaPlayer()
    private var isRecording = false
    private var isPlaying = false
    var setOnAudioRecordListener: ((RecordingItem) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomsheetRecordAudioBinding.inflate(inflater, container, false)
        isCancelable = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnRecord.setOnClickListener {
            isRecording = !isRecording
            if (isRecording) {
                audioRecorder.startRecording()
                binding.chronometer.start()
            } else {
                binding.chronometer.stop()
                audioRecorder.stopRecording(false)
            }
        }
        audioRecorder.setOnAudioRecordListener(this)
        binding.btnCancel.setOnClickListener { dismiss() }
    }

    override fun onDismiss(dialog: DialogInterface) {
        audioRecorder.stopRecording(true)
        super.onDismiss(dialog)
    }

    override fun onRecordFinished(recordingItem: RecordingItem) {
        super.onRecordFinished(recordingItem)
        binding.btnRecord.setImageResource(R.drawable.ic_play)
        binding.btnRecord.isEnabled = true
        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                mediaPlayer.pause()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.progress?.let { mediaPlayer.seekTo(it) }
                mediaPlayer.start()
            }
        })
        binding.btnRecord.setOnClickListener {
            isPlaying = !isPlaying
            try {
                if (isPlaying) {
                    mediaPlayer.setDataSource(recordingItem.filePath)
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                    mediaPlayer.setScreenOnWhilePlaying(true)
                    mediaPlayer.setOnTimedTextListener { _, text ->
                        binding.chronometer.text = text.toString()
                    }
                    binding.seekbar.max = mediaPlayer.duration
                    binding.chronometer.visibility = View.INVISIBLE
                    binding.chronometer.setOnChronometerTickListener {
                        binding.seekbar.progress = mediaPlayer.currentPosition
                    }
                } else {
                    mediaPlayer.stop()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
            }
        }
        binding.seekbar.visibility = View.VISIBLE
        binding.chronometer.visibility = View.VISIBLE
        binding.btnDone.setOnClickListener {
            setOnAudioRecordListener?.invoke(recordingItem)
        }
    }

    override fun onError(exception: Exception) {
        super.onError(exception)
        showToast(exception.message ?: "An error occurred!")
    }

    override fun onRecordingStarted() {
        super.onRecordingStarted()
        binding.btnRecord.setImageResource(R.drawable.ic_stop)
    }

    companion object {
        fun newInstance(): DialogRecordAudio {
            return DialogRecordAudio()
        }
    }
}
