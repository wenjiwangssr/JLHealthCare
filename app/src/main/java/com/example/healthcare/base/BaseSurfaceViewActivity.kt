package com.example.healthcare.base

import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.healthcare.CustomViewer
import com.example.healthcare.R

abstract class BaseSurfaceViewActivity:AppCompatActivity() {
    lateinit var surfaceView : SurfaceView
    lateinit var customViewer: CustomViewer

    override fun onCreate(savedInstanceState: Bundle?) {
        System.loadLibrary("imebra_lib")
        super.onCreate(savedInstanceState)
        setContentView(getContentViewId())

        //隐藏底部导航栏
        val decorView = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN)
        decorView.systemUiVisibility = uiOptions

        init()
    }


    fun init(){
        surfaceView = findViewById(R.id.surface)
        customViewer = CustomViewer()
        customViewer.loadEntity()
        customViewer.setSurfaceView(surfaceView)

        //Environments and Lightning (OPTIONAL)
//        customViewer.loadIndirectLight(this, "environments/venetian_crossroads_2k/venetian_crossroads_2k_ibl.ktx")
        customViewer.loadIndirectLight(this, "ibl/default_env_ibl.ktx")
//        customViewer.loadIndirectLight(this, "ibl/pillars_2k_ibl.ktx")
//        val floatArray = customViewer.modelViewer.scene.indirectLight?.getRotation(floatArrayOf(0f,1f,0f,-1f,0f,0f,0f,0f,1f))

        customViewer.modelViewer.camera.setExposure(16.0f, 1.0f / 125.0f, 100.0f)

//        customViewer.loadEnvironments(this, "venetian_crossroads_2k");
        customViewer.modelViewer.view.blendMode = com.google.android.filament.View.BlendMode.TRANSLUCENT
    }

    abstract fun getSurfaceViewId() :Int
    abstract fun getContentViewId() :Int

    override fun onResume() {
        super.onResume()
        customViewer.onResume()
    }

    override fun onPause() {
        super.onPause()
        customViewer.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        customViewer.onDestroy()
    }
}