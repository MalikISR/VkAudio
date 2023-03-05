package com.malik_isr.vkau

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.malik_isr.vkau.databinding.ActivityMainBinding
import java.io.File
import android.text.InputFilter
import android.view.View
import android.view.animation.*

class MainActivity : AppCompatActivity(), AudioAdapter.AudioList {

    private lateinit var binding: ActivityMainBinding
    private val recordController = AudioRecord(this)
    private var countDownTimer: CountDownTimer? = null
    private lateinit var adapter: AudioAdapter
    lateinit var filter:InputFilter
    private var initAdapter = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Ваши аудиозаписи"
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            11
        )
        init()
    }

    private fun init() = with(binding){
        initAdapt()

        startStopBlue.setOnClickListener { onStartStopClick() }

        val unwantedChars = arrayOf('.', ',', '/', '\\' , ':', '*', '?', '"', '<', '>', '|', ' ' )
        filter = InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                if (unwantedChars.contains(source[i])) {
                    return@InputFilter ""
                }
            }
            null
        }

        edTextReName.filters = arrayOf(filter)

        bSave.setOnClickListener {
            if (edTextReName.text.toString() != "") {
                var bool = false
                val audioFilesArray = audioFiles()
                if (audioFilesArray.isNotEmpty()) {
                    for (name in audioFilesArray){
                        if ("${edTextReName.text}.wav" == name.name) {
                            bool = true
                            break
                        }
                    }
                }
                if (!bool) {
                    val endFile = audioFiles()[audioFiles().size.minus(1) ?: 0]
                    val oldFile = File(endFile.absolutePath)
                    val parentDir = oldFile.parent
                    val newFile = File(parentDir, "${edTextReName.text}.wav")
                    oldFile.renameTo(newFile)
                    animRenameClose()
                    initAdapt()
                } else {
                    edTextReName.error = "Аудиозапись с таким название уже существует!"
                }
            } else {
                edTextReName.error = "Поле не запелнено!"
            }
        }
        bCancel.setOnClickListener {
            animRenameClose()
            initAdapt()
        }
    }

    private fun initAdapt()= with(binding){
        if (!initAdapter) {
            if (audioFiles().isNotEmpty()) {
                adapter = AudioAdapter(audioFiles())
                rcView.layoutManager = LinearLayoutManager(this@MainActivity)
                rcView.adapter = adapter
                initAdapter = true
            }
        } else {
            audioFiles().let { adapter.reload(it) }
        }
    }

    private fun audioFiles(): Array<File> {
        val context = this
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)

        return directory?.listFiles() ?: arrayOf()
    }

    private fun onStartStopClick(){
        if (recordController.isRecording()){
            recordController.stop()
            binding.startStopRed.visibility = View.INVISIBLE
            countDownTimer?.cancel()
            countDownTimer = null
            animRenameOpen()
        } else {
            recordController.start()
            binding.startStopRed.visibility = View.VISIBLE

            countDownTimer = object : CountDownTimer(60_000, 100){
                override fun onTick(p0: Long) {
                    val volume = recordController.getVolume()
                    handleVolume(volume)
                }
                override fun onFinish() {
                }
            }
            countDownTimer?.start()
        }
    }

    private fun animRenameOpen(){
        val centerX = (binding.rcView.parent as View).width / 2.0f
        val centerY = (binding.rcView.parent as View).height / 2.0f
        val scaleAnimation = ScaleAnimation(
            0.0f, 1.0f,
            0.0f, 1.0f,
            centerX, centerY
        )
        scaleAnimation.duration = 1000

        val translateAnimation = TranslateAnimation(
            -230f, 0f,
            -830f, 0f,
        )
        translateAnimation.duration = 1000

        val animationSet = AnimationSet(true)
        animationSet.addAnimation(scaleAnimation)
        animationSet.addAnimation(translateAnimation)
        binding.reName.startAnimation(animationSet)
        binding.reName.visibility = View.VISIBLE
    }

    private fun animRenameClose(){
        val centerX = (binding.rcView.parent as View).width / 2.0f
        val centerY = (binding.rcView.parent as View).height / 2.0f
        val scaleAnimation = ScaleAnimation(
            1.0f, 0.0f,
            1.0f, 0.0f,
            centerX, centerY
        )
        scaleAnimation.duration = 1000

        val translateAnimation = TranslateAnimation(
            0f,-230f,
            0f,-830f,
        )
        translateAnimation.duration = 1000

        val animationSet = AnimationSet(true)
        animationSet.addAnimation(scaleAnimation)
        animationSet.addAnimation(translateAnimation)
        binding.reName.startAnimation(animationSet)
        binding.reName.visibility = View.GONE
        binding.edTextReName.setText("")
    }


    private fun handleVolume(volume: Int){
        val scale = volume / MAX_RECORD_AMPLITUDE.toFloat() + 1.0f
        Math.min(scale, 4.0f)

        binding.startStopRed.animate()
            .scaleX(scale)
            .scaleY(scale)
            .setInterpolator(OvershootInterpolator()).duration = 100
    }

    private companion object {
        const val MAX_RECORD_AMPLITUDE = 32768
    }

    override fun onClickItemStart(file: File) {}

    override fun onDestroy() {
        super.onDestroy()
        if (recordController.isRecording()){
            recordController.stop()
            countDownTimer?.cancel()

        }
    }
}