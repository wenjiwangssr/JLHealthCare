package com.example.healthcare

import android.content.Context
import android.util.Base64
import android.util.Log
import android.view.Choreographer
import android.view.MotionEvent
import android.view.SurfaceView
import com.example.healthcare.utils.ModelViewer
import com.google.android.filament.EntityInstance
import com.google.android.filament.Skybox
import com.google.android.filament.View
import com.google.android.filament.android.UiHelper
import com.google.android.filament.utils.KTXLoader
import com.google.android.filament.utils.Utils
import java.io.ByteArrayInputStream

import java.nio.ByteBuffer

abstract class CustomViewer
{
    companion object
    {
        init {
            Utils.init();
        }
    }

    private lateinit var choreographer: Choreographer
    public lateinit var modelViewer: ModelViewer

    fun loadEntity()
    {
        choreographer = Choreographer.getInstance()
    }

    fun setSurfaceView(mSurfaceView: SurfaceView)
    {
        modelViewer = object :ModelViewer(mSurfaceView){
            override fun onTouch1(event: MotionEvent) {
                touch(event)
            }
        }
        mSurfaceView.setOnTouchListener(modelViewer)
        //Skybox and background color
        //without this part the scene'll appear broken
//        modelViewer.engine.lightManager.setColor(1,0.7f,0.5f,0f)
        modelViewer.makeBackgroundTransparent()
        modelViewer.view.blendMode = View.BlendMode.TRANSLUCENT
//        modelViewer.scene.skybox = null

        modelViewer.scene.skybox = Skybox.Builder().build(modelViewer.engine)
        modelViewer.scene.skybox!!.setColor(1f, 1f, 1f, 1.2f)//White color
    }

    abstract fun touch(event: MotionEvent)

    fun loadGlb(context:Context, name: String)
    {
        val buffer = readAsset(context, "model/${name}.glb")
        modelViewer.loadModelGlb(buffer)
        modelViewer.transformToUnitCube()
    }
    fun loadGlb(context:Context, dirName: String, name: String)
    {
        val buffer = readAsset(context, "model/${dirName}/${name}.glb")
        modelViewer.loadModelGlb(buffer)
        modelViewer.transformToUnitCube()
    }


    @Synchronized
    fun loadGltf(context: Context, name: String)
    {
//        val buffer = context.assets.open(name).use { input ->
//            val bytes = ByteArray(input.available())
//            input.read(bytes)
//            ByteBuffer.wrap(bytes)
//
//        }
        val `in` = if (name.contains("base64")){
            val code = name.split("base64,")[1]
            ByteArrayInputStream(Base64.decode(code, Base64.DEFAULT))
        }else{
            context.assets.open(name)
        }
        val buffer = `in`.use{ input ->
            val bytes = ByteArray(input.available())
            input.read(bytes)
            ByteBuffer.wrap(bytes)
        }


//        modelViewer.loadModelGltf(buffer){ uri -> readAsset(context, "models/$uri") }
        modelViewer.loadModelGltfAsync(buffer){ uri ->
//            Log.d("TimeTag","FinishLoadingASYNC...... Cost time:${System.currentTimeMillis()}")
            readAsset(context, "models/$uri")
        }
        modelViewer.transformToUnitCube()



//        val myInstance =  modelViewer.asset?.materialInstances
//        myInstance?.forEach {
//            Log.d("materialInstances","${it.material}   ${it.name}")
//        }
//        modelViewer.asset?.entities?.forEach { num -> Log.d("entities","$num") }

    }

    fun loadIndirectLight(context: Context, ibl: String)
    {
        // Create the indirect light source and add it to the scene.
        val buffer = readAsset(context, ibl)
        KTXLoader.createIndirectLight(modelViewer.engine, buffer).apply {
            intensity = 30_000f
            modelViewer.scene.indirectLight = this
        }
    }

    fun loadEnvironments(context: Context, ibl: String)
    {
        // Create the sky box and add it to the scene.
        val buffer = readAsset(context, "environments/venetian_crossroads_2k/${ibl}_skybox.ktx")
        KTXLoader.createSkybox(modelViewer.engine, buffer).apply {
            modelViewer.scene.skybox = this
        }
    }

    private fun readAsset(context: Context, assetName: String): ByteBuffer
    {
        Log.d("assetName",assetName)
//        val input = context.assets.open(assetName)
//        val bytes = ByteArray(input.available())
//        input.read(bytes)
//        return ByteBuffer.wrap(bytes)
        val input = if (assetName.contains("base64")){
            val code = assetName.split("base64,")[1]
            ByteArrayInputStream(Base64.decode(code,Base64.DEFAULT))
        }else{
            context.assets.open(assetName)
        }
        val bytes = ByteArray(input.available())
        input.read(bytes)
        return ByteBuffer.wrap(bytes)
    }

    private val frameCallback = object : Choreographer.FrameCallback {
        //        override fun doFrame(currentTime: Long) {
//            choreographer.postFrameCallback(this)
//            modelViewer.render(currentTime)
//        }
        private val startTime = System.nanoTime()
        override fun doFrame(currentTime: Long) {
            val seconds = (currentTime - startTime).toDouble() / 1_000_000_000
            choreographer.postFrameCallback(this)
            modelViewer.animator?.apply {
                if (animationCount > 0) {
                    applyAnimation(0, seconds.toFloat())
                }
                updateBoneMatrices()
            }
            modelViewer.render(currentTime)
        }
    }

    fun onResume() {
        choreographer.postFrameCallback(frameCallback)
    }
    fun onPause() {
        choreographer.removeFrameCallback(frameCallback)
    }

    fun onDestroy() {
        choreographer.removeFrameCallback(frameCallback)
    }
}