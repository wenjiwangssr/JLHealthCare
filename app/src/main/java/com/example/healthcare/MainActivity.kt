package com.example.healthcare

import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.filament.Material
import com.google.android.filament.View
import com.google.android.filament.filamat.MaterialBuilder
import com.qmuiteam.qmui.kotlin.onClick
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private lateinit var surfaceView : SurfaceView
    private lateinit var customViewer: CustomViewer
    private lateinit var diyMaterial: Material

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        surfaceView = findViewById(R.id.surface_container)
        customViewer = CustomViewer()
        customViewer.loadEntity()
        customViewer.setSurfaceView(surfaceView)

        val uri = intent.getStringExtra("Uri")

//        customViewer.loadGltf(this,"models/DamagedHelmet.gltf")

        //Environments and Lightning (OPTIONAL)
        customViewer.loadIndirectLight(this, "venetian_crossroads_2k")
//        customViewer.loadEnvironments(this, "venetian_crossroads_2k");
        customViewer.modelViewer.view.blendMode = View.BlendMode.TRANSLUCENT
        if (uri != null) {
            customViewer.loadGltf(this,uri)
        }else{
            customViewer.loadGltf(this,"models/谢子东untitled.gltf")
        }
//        customViewer.modelViewer.scene.skybox = null
//        customViewer.modelViewer.renderer.clearOptions.clear = true
//        buildMaterial() //自定义material

        findViewById<Button>(R.id.button).onClick {
            for (i in customViewer.modelViewer.asset?.materialInstances?.indices!!){
                customViewer.modelViewer.asset?.materialInstances?.set(i, diyMaterial.createInstance())
            }
//            customViewer.modelViewer.SurfaceCallback()
            val myInstance =  customViewer.modelViewer.asset?.materialInstances!!
            for (i in myInstance.indices){
                myInstance[i] = diyMaterial.createInstance()
                myInstance[i].setColorWrite(true)
                myInstance[i].setMaskThreshold(0.8f)
                Log.d("MeshName:",decode(customViewer.modelViewer.asset?.getName(i))?:"")
                myInstance[i].setParameter("baseColorFactor",0.5f,0.1f,0.3f,0.1f)
                Log.d("MeshBlendingMode:",myInstance[i].material.blendingMode.toString())
//                myInstance[i].setParameter("baseColor",1f,1f,1f)
            }

//            myInstance?.forEachIndexed { index, materialInstance ->
//                if (materialInstance.material.parameters[3].type == Material.Parameter.Type.FLOAT4 && materialInstance.material.parameters[3].name == "baseColorFactor"){
//                    materialInstance.setParameter("baseColorFactor",1f,0f,1f,1f)
////                    materialInstance.setParameter("baseColorFactor",f,f,f,f)
//                }
//                Log.d("materialInstances","${materialInstance.material}   ${decode(materialInstance.name)}")
//            }

        }

        var firstClick = true
        var count = 0;

        findViewById<Button>(R.id.button2).onClick {

            val myInstance =  customViewer.modelViewer.asset?.materialInstances!!

            if (firstClick){
                count = customViewer.modelViewer.asset!!.root+1
                firstClick = false
            }

//            for (i in filamentAsset.root+1 .. filamentAsset.root+filamentAsset.entities.size ){
//                Log.d("MeshName:",decode(filamentAsset.getName(i))?:"")
//            }
//            val materialInstance =  diyMaterial.createInstance()
//            materialInstance.setParameter("baseColor", Colors.RgbType.SRGB, 0.71f, 0.0f, 0.0f)
//            filamentAsset.materialInstances[0] = materialInstance
            Log.d("MeshBlendingMode:",customViewer.modelViewer.asset!!.materialInstances[count - customViewer.modelViewer.asset!!.root-1].material.blendingMode.toString())
            myInstance[count - customViewer.modelViewer.asset?.root!!-1].setParameter("baseColorFactor",0f,0.6f,0.3f,0.1f)
            Log.d("MeshName:",
                "ColorChange---"+ decode(customViewer.modelViewer.asset!!.getName(count))
            )
            count++
            if (count>customViewer.modelViewer.asset!!.root+customViewer.modelViewer.asset!!.entities.size-1){
                finish()
            }

//            myInstance?.forEachIndexed { index, materialInstance ->
//                if (materialInstance.material.parameters[3].type == Material.Parameter.Type.FLOAT4 && materialInstance.material.parameters[3].name == "baseColorFactor"){
//                    materialInstance.setParameter("baseColorFactor",1f,0f,1f,1f)
////                    materialInstance.setParameter("baseColorFactor",f,f,f,f)
//                }
//                Log.d("materialInstances","${materialInstance.material}   ${decode(materialInstance.name)}")
//            }

        }
    }
    private fun buildMaterial() {
        // MaterialBuilder.init() must be called before any MaterialBuilder methods can be used.
        // It only needs to be called once per process.
        // When your app is done building materials, call MaterialBuilder.shutdown() to free
        // internal MaterialBuilder resources.
        MaterialBuilder.init()

        val matPackage = MaterialBuilder()
            // By default, materials are generated only for DESKTOP. Since we're an Android
            // app, we set the platform to MOBILE.
            .platform(MaterialBuilder.Platform.MOBILE)

            // Set the name of the Material for debugging purposes.
            .name("Organ")
            // Defaults to LIT. We could change the shading model here if we desired.
            .shading(MaterialBuilder.Shading.LIT)
            .refractionMode(MaterialBuilder.RefractionMode.SCREEN_SPACE)
            .postLightingBlending(MaterialBuilder.BlendingMode.TRANSPARENT)
            .blending(MaterialBuilder.BlendingMode.TRANSPARENT)
            .transparencyMode(MaterialBuilder.TransparencyMode.DEFAULT)
            .refractionMode(MaterialBuilder.RefractionMode.NONE)
            // Add a parameter to the material that can be set via the setParameter method once
            // we have a material instance.
            .uniformParameter(MaterialBuilder.UniformType.FLOAT3, "baseColor")
            .uniformParameter(MaterialBuilder.UniformType.FLOAT4, "baseColorFactor")

            // Fragment block- see the material readme (docs/Materials.md.html) for the full
            // specification.
            .material("void material(inout MaterialInputs material) {\n" +
                    "    prepareMaterial(material);\n" +
                    "    material.baseColor.rgb = materialParams.baseColor;\n" +
                    "    material.roughness = 0.65;\n" +
                    "    material.metallic = 1.0;\n" +
                    "    material.clearCoat = 1.0;\n" +
                    "}\n")

            // Turn off shader code optimization so this sample is compatible with the "lite"
            // variant of the filamat library.
            .optimization(MaterialBuilder.Optimization.NONE)

            // When compiling more than one material variant, it is more efficient to pass an Engine
            // instance to reuse the Engine's job system
            .build(customViewer.modelViewer.engine)

        if (matPackage.isValid) {
            val buffer = matPackage.buffer
            diyMaterial = Material.Builder().payload(buffer, buffer.remaining()).build(customViewer.modelViewer.engine)
        }

        // We're done building materials, so we call shutdown here to free resources. If we wanted
        // to build more materials, we could call MaterialBuilder.init() again (with a slight
        // performance hit).
        MaterialBuilder.shutdown()


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

    fun random() = Random(1).nextFloat()

}