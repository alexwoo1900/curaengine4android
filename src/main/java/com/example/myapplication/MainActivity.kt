package com.example.myapplication

import java.util.*
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.example.myapplication.databinding.ActivityMainBinding
import java.lang.ref.WeakReference

enum class EngineState(val value: Int) {
    IDLE(0),
    INTERRUPT(0xfff0)
}

enum class SliceStage(val value: Int) {
    PREPARING(1),
    INSETS(0b0010),
    SKINSINFILL(0b0100),
    GCODEWRITER(0b1000),
    FINISHED(0b1111)
}



class MainActivity : AppCompatActivity() {

    // C++ functions for running curaengine
    external fun runCuraEngine(str: String): Int
    external fun monitorCuraEngine(): Int
    external fun getSliceStageFromCuraEngine(): Int
    external fun getSliceProgressFromCuraEngine(): Int

    private lateinit var binding: ActivityMainBinding
    private var sliceBroadcaster: Timer? = null

    private var evHandler: EventHandler? = null

    private var sliceCubeBtn : Button? = null
    private var sliceTrooperBtn : Button? = null
    private var insetsPB : ProgressBar? = null
    private var skinsInfillPB : ProgressBar? = null
    private var gcodeWriterPB : ProgressBar? = null
    private var insetsPTV : TextView? = null
    private var skinsInfillPTV : TextView? = null
    private var gcodeWriterPTV : TextView? = null


    private class UIHandler(val wrActivity: WeakReference<MainActivity>) : Handler() {

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            wrActivity.get()?.run {
                when (msg.what) {
                    EngineState.IDLE.value -> {
                        // Log.d("UIHandler", "IDLE")
                        sliceCubeBtn?.isEnabled = true
                        sliceCubeBtn?.isClickable = true
                        sliceTrooperBtn?.isEnabled = true
                        sliceTrooperBtn?.isClickable = true
                    }
                    SliceStage.PREPARING.value -> {
                        // Log.d("UIHandler", "PREPARING")
                        insetsPB?.progress = 0
                        skinsInfillPB?.progress = 0
                        gcodeWriterPB?.progress = 0

                        sliceCubeBtn?.isEnabled = false
                        sliceCubeBtn?.isClickable = false
                        sliceTrooperBtn?.isEnabled = false
                        sliceTrooperBtn?.isClickable = false
                    }
                    SliceStage.INSETS.value -> {
                        val progress = getSliceProgressFromCuraEngine()
                        insetsPB?.progress = progress
                        skinsInfillPB?.progress = 0
                        gcodeWriterPB?.progress = 0
                        insetsPTV?.text = ("$progress%")
                    }
                    SliceStage.SKINSINFILL.value -> {
                        val progress = getSliceProgressFromCuraEngine()
                        insetsPB?.progress = 100
                        insetsPTV?.text = "Completed"
                        skinsInfillPB?.progress = progress
                        skinsInfillPTV?.text = ("$progress%")
                        gcodeWriterPB?.progress = 0
                    }
                    SliceStage.GCODEWRITER.value -> {
                        val progress = getSliceProgressFromCuraEngine()
                        insetsPB?.progress = 100
                        skinsInfillPB?.progress = 100
                        skinsInfillPTV?.text = "Completed"
                        gcodeWriterPB?.progress = progress
                        gcodeWriterPTV?.text = ("$progress%")
                    }
                    SliceStage.FINISHED.value -> {
                        Log.d("EVHandler", "FINISHED")
                        insetsPB?.progress = 100
                        skinsInfillPB?.progress = 100
                        gcodeWriterPB?.progress = 100
                        gcodeWriterPTV?.text = "Completed"
                    }
                }
            }
        }
    }


    private class EventHandler(val wrActivity: WeakReference<MainActivity>) : Handler() {
        private val uiHandler = UIHandler(wrActivity)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            class BroadcastTask() : TimerTask() {
                override fun run() {
                    uiHandler.sendEmptyMessage(wrActivity.get()?.getSliceStageFromCuraEngine()!!)
                }
            }

            wrActivity.get()?.run {
                when (msg.what) {
                    EngineState.IDLE.value -> {
                        // Log.d("EVHandler", "IDLE")
                        uiHandler.sendEmptyMessage(EngineState.IDLE.value)
                    }
                    SliceStage.PREPARING.value -> {
                        // Log.d("EVHandler", "PREPARING")
                        val broadcast = BroadcastTask()
                        sliceBroadcaster = sliceBroadcaster ?: Timer()
                        sliceBroadcaster?.schedule(broadcast, Date(), 200)
                        // Log.d("SliceBroadcaster", "Started")

                        Thread {
                            // Log.d("CuraEngine Monitor", "Started")
                            monitorCuraEngine()
                            // Log.d("CuraEngine Monitor", "Finished")
                        }.start()

                        uiHandler.sendEmptyMessage(SliceStage.PREPARING.value)

                        val modelName = msg.obj.toString()
                        Thread {
                            // Log.d("CuraEngine", "Started")
                            runCuraEngine(modelName)
                            // Log.d("CuraEngine", "Finished")
                            evHandler?.sendEmptyMessage(SliceStage.FINISHED.value)
                            evHandler?.sendEmptyMessage(EngineState.IDLE.value)
                        }.start()
                    }
                    SliceStage.FINISHED.value -> {
                        uiHandler.sendEmptyMessage(SliceStage.FINISHED.value)

                        // Log.d("EVHandler", "FINISHED")
                        sliceBroadcaster?.cancel()
                        sliceBroadcaster = null
                        // Log.d("SliceBroadcaster", "Cancelled")
                    }
                    else -> {

                    }
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        verifyStoragePermissions(this)
        setContentView(binding.root)

        sliceCubeBtn = findViewById<Button>(R.id.sliceCubeBtn)
        sliceTrooperBtn = findViewById<Button>(R.id.sliceTrooperBtn)
        insetsPB = findViewById<ProgressBar>(R.id.insetsPB)
        skinsInfillPB = findViewById<ProgressBar>(R.id.skinsInfillPB)
        gcodeWriterPB = findViewById<ProgressBar>(R.id.gcodeWriterPB)
        insetsPTV = findViewById<TextView>(R.id.insetsPTV)
        skinsInfillPTV = findViewById<TextView>(R.id.skinsInfillPTV)
        gcodeWriterPTV = findViewById<TextView>(R.id.gcodeWriterPTV)

        evHandler = EventHandler(WeakReference(this))
        sliceCubeBtn!!.setOnClickListener{
            val msg = Message.obtain()
            msg.what = SliceStage.PREPARING.value
            msg.obj = "cube"
            evHandler!!.sendMessage(msg)
        }

        sliceTrooperBtn!!.setOnClickListener{
            val msg = Message.obtain()
            msg.what = SliceStage.PREPARING.value
            msg.obj = "trooper"
            evHandler!!.sendMessage(msg)
        }
    }

    companion object {
        init {
            System.loadLibrary("myapplication")
        }

        private const val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        fun verifyStoragePermissions(activity: Activity?) {
            val permission = ActivityCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
                )
            }
        }
    }
}