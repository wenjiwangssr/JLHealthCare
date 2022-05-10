package com.example.healthcare

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Choreographer
import android.view.SurfaceView
import com.google.android.filament.EntityManager
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.MaterialProvider
import com.google.android.filament.gltfio.ResourceLoader
import com.google.android.filament.gltfio.UbershaderLoader
import com.google.android.filament.utils.ModelViewer
import com.google.android.filament.utils.Utils
import java.io.ByteArrayInputStream
import java.nio.Buffer
import java.nio.ByteBuffer

class Demo2Activity : AppCompatActivity() {

    private lateinit var materialProvider: MaterialProvider
    private lateinit var surfaceView : SurfaceView
    private lateinit var choreographer: Choreographer
    private lateinit var modelViewer: ModelViewer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo2)

        Utils.init()
        surfaceView = findViewById(R.id.surface2)
        modelViewer = ModelViewer(surfaceView)
        surfaceView.setOnTouchListener(modelViewer)
        choreographer = Choreographer.getInstance()
        val engine = modelViewer.engine
        materialProvider = UbershaderLoader(engine)
        val assetLoader = AssetLoader(engine, materialProvider, EntityManager.get())

        val filamentAsset = assets.open("models/linyvmeiuntitled.gltf").use { input ->
            val bytes = ByteArray(input.available())
            input.read(bytes)
            assetLoader.createAssetFromJson(ByteBuffer.wrap(bytes))!!
        }
        for (i in filamentAsset.root+1 .. filamentAsset.root+filamentAsset.entities.size ){
            Log.d("MeshName:",decode(filamentAsset.getName(i))?:"")
        }
        val resourceLoader = ResourceLoader(engine)
        for (uri in filamentAsset.resourceUris) {
            val buffer = loadResource(uri)
            resourceLoader.addResourceData(uri, buffer)
        }
        resourceLoader.loadResources(filamentAsset)
        resourceLoader.destroy()
        filamentAsset.releaseSourceData();
        val scene = modelViewer.scene
        scene.addEntities(filamentAsset.entities)

    }

    private fun loadResource(assetName: String): Buffer {
        val input = if (assetName.contains("base64")){
            val code = assetName.split("base64,")[1]
            ByteArrayInputStream(Base64.decode(code, Base64.DEFAULT))
        }else{
            assets.open(assetName)
        }
        val bytes = ByteArray(input.available())
        input.read(bytes)
        return ByteBuffer.wrap(bytes)
    }

    private fun decode(unicodeStr: String?): String? {
        if (unicodeStr == null) {
            return null
        }
        val retBuf = StringBuffer()
        val maxLoop = unicodeStr.length
        var i = 0
        while (i < maxLoop) {
            if (unicodeStr[i] == '\\') {
                if (i < maxLoop - 5 && (unicodeStr[i + 1] == 'u' || unicodeStr[i + 1] == 'U')) try {
                    retBuf.append(unicodeStr.substring(i + 2, i + 6).toInt(16).toChar())
                    i += 5
                } catch (localNumberFormatException: NumberFormatException) {
                    retBuf.append(unicodeStr[i])
                } else retBuf.append(unicodeStr[i])
            } else {
                retBuf.append(unicodeStr[i])
            }
            i++
        }
        return retBuf.toString()
    }

}