package com.malik_isr.vkau

import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.malik_isr.vkau.databinding.AudioItemBinding
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import java.util.Date
import java.text.SimpleDateFormat

class AudioAdapter (private var files: Array<File>): RecyclerView.Adapter<AudioAdapter.AudioHolder>() {

    class AudioHolder (item: View): RecyclerView.ViewHolder(item) {
        private val binding = AudioItemBinding.bind(item)
        private var timeCor: Job? = null
        private val mediaPlayer = MediaPlayer()

        @RequiresApi(Build.VERSION_CODES.O)
        fun setData(file: File)= with(binding){
            val attr = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
            val proportion = proportionPro(file)
            audioName.text = file.name.substringBeforeLast(".")
            audioDate.text = formatDate(Date(attr.creationTime().toMillis()).toString())
            audioDuration.text = duration(file)
            prepareMedia(file)
            var checkPlay = false
            bPlayStop.setOnClickListener {
                if (!checkPlay) {
                    mediaPlayer.start()
                    bPlayStop.setImageResource(R.drawable.ic_pause)
                    currentTime.text = time(mediaPlayer.currentPosition)
                    checkPlay = true
                    currentTime.visibility = View.VISIBLE
                    slash.text = "/"

                    timeCor = CoroutineScope(Dispatchers.Main).launch {
                        while (mediaPlayer.isPlaying) {
                            delay(1000)
                            val med = mediaPlayer.currentPosition / 1000
                            progressTimeBar.progress = (proportion * med).toInt()
                            currentTime.text = time(mediaPlayer.currentPosition)
                        }

                        bPlayStop.setImageResource(R.drawable.ic_play)
                        currentTime.text = ""
                        slash.text = ""
                        progressTimeBar.progress = 0
                        checkPlay = false
                        timeCor?.cancel()
                    }

                } else {
                    mediaPlayer.pause()
                    bPlayStop.setImageResource(R.drawable.ic_play)
                    checkPlay = false
                    timeCor?.cancel()
                }
            }
        }

        private fun prepareMedia(file: File){
            try {
                val fileDescriptor = FileInputStream(file).fd
                mediaPlayer.setDataSource(fileDescriptor)
                mediaPlayer.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun proportionPro(file: File): Float{
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
            val seconds = duration?.let { (it / 1000).toFloat() }
            println(seconds)
            val proportion = seconds?.let { 100.div(it) }
            println(proportion)

            return proportion!!
        }

        private fun duration(file: File): String{
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)

            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
            retriever.release()
            val seconds = duration?.let { (it / 1000).toInt() }
            val minutes = seconds?.div(60)
            val remainingSeconds = seconds?.rem(60)
            return "%d:%02d".format(minutes, remainingSeconds)
        }

        private fun formatDate(dateString: String): String {
            val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("MM.dd.yyyy 'в' HH:mm", Locale.ENGLISH)

            val date = inputFormat.parse(dateString)
            return outputFormat.format(date)
        }

        private fun time(duration: Int):String{
            val seconds = duration.let { (it / 1000).toInt() }
            val minutes = seconds.div(60)
            val remainingSeconds = seconds.rem(60)
            return "%d:%02d".format(minutes, remainingSeconds)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.audio_item, parent, false)
        return AudioHolder(view)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: AudioHolder, position: Int) {
        holder.setData(files[position])
    }

    override fun getItemCount(): Int {
        return files.size
    }

    fun reload (audFiles: Array<File>){
        files = audFiles
        notifyDataSetChanged()
    }

    interface AudioList{
        fun onClickItemStart(file: File)
    }
}