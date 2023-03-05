package com.malik_isr.vkau

import android.content.Context
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log

class AudioRecord(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null

    fun start(){
            Log.d("My", "start")

            val mediaRecorder = MediaRecorder()
            stop()
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mediaRecorder.setOutputFile(getPath())
            mediaRecorder.prepare()
            mediaRecorder.start()

            this.mediaRecorder = mediaRecorder
    }

    fun isRecording():Boolean{
        return mediaRecorder != null
    }

    private fun getPath(): String{
        Log.d("My", "${context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)}/${System.currentTimeMillis()}.wav")
        return "${context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)}/${System.currentTimeMillis()}.wav"
    }

    fun getVolume(): Int{
        return mediaRecorder?.maxAmplitude ?: 0
    }

    fun stop(){
        if(mediaRecorder == null) return
        Log.d("My", "stop")
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()

        } catch (_: java.lang.Exception) {}
        mediaRecorder = null
    }
}