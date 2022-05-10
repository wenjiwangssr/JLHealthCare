package com.example.healthcare.ui

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ccy.focuslayoutmanager.FocusLayoutManager
import com.example.healthcare.CustomViewer
import com.example.healthcare.R
import com.example.healthcare.bean.CaseBean
import com.example.healthcare.bean.ColorBean
import com.example.healthcare.dicom.DicomViewerActivity
import com.example.healthcare.ui.adapter.CaseAdapter
import com.example.healthcare.ui.adapter.ColorAdapter
import com.example.healthcare.utils.CommonHelper
import com.example.healthcare.utils.CommonHelper.decode
import com.google.android.filament.*
import com.mig35.carousellayoutmanager.CarouselLayoutManager
import com.mig35.carousellayoutmanager.CarouselZoomPostLayoutListener
import com.mig35.carousellayoutmanager.CenterScrollListener
import com.qmuiteam.qmui.kotlin.onClick
import com.yarolegovich.discretescrollview.DiscreteScrollView
import com.yarolegovich.discretescrollview.transform.Pivot
import com.yarolegovich.discretescrollview.transform.ScaleTransformer
import kotlinx.android.synthetic.main.activity_uiactivity.*
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import java.io.File
import java.util.*


class UIActivity : AppCompatActivity(),EasyPermissions.PermissionCallbacks {

    private lateinit var caseAdapter: CaseAdapter
    private lateinit var focusLayoutManager: FocusLayoutManager
    private lateinit var discreteScrollView: DiscreteScrollView
    private lateinit var emptyView:TextView
    private lateinit var rv: RecyclerView
    private val caseList = ArrayList<CaseBean>()
    private val modelUriList = ArrayList<String>()
    private val colorBeanList = ArrayList<ArrayList<ColorBean>>()

    private lateinit var surfaceView : SurfaceView
    private lateinit var customViewer: CustomViewer
    private lateinit var diyMaterial: Material

    private lateinit var colorRecyclerView: RecyclerView
    private lateinit var colorAdapter: ColorAdapter

    @Entity private var light = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        // First thing: load the Imebra library
        System.loadLibrary("imebra_lib")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_uiactivity)

        //隐藏底部导航栏
        val decorView = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN)
        decorView.systemUiVisibility = uiOptions

        //模拟数据
        caseList.add(CaseBean(age = 36,id = 20220330,gender = 1,patient_name = "杨*明",dept_name = "漳州市医院\n胸外科",date = "2022年3月22日",des = "重建上腔静脉血管，主动脉血管，肺动脉血管，肺静脉血管，淋巴结，心脏，支气管，肺，结节，疑似结节等模型。"))
        caseList.add(CaseBean(age = 59,id = 20220407,gender = 1,patient_name = "吴*英",dept_name = "漳州市医院\n胸外科",date = "2022年3月4日",des = "重建肺动脉血管，肺静脉血管，支气管，肺，心脏，结节，磨玻璃结节，安全切缘等模型。"))
        caseList.add(CaseBean(age = 56,id = 20220411,gender = 0,patient_name = "谢*东",dept_name = "福建省人民医院\n结直肠外科",date = "2022年4月18日",des = "重建下腔静脉，门静脉血管，动脉血管，肝脏，脾脏，胰腺，胆总管,肾脏，胃肠等模型。"))

        //模拟数据
        caseList.add(CaseBean(age = 36,id = 20220330,gender = 1,patient_name = "杨*明",dept_name = "漳州市医院\n胸外科",date = "2022年3月22日",des = "重建上腔静脉血管，主动脉血管，肺动脉血管，肺静脉血管，淋巴结，心脏，支气管，肺，结节，疑似结节等模型。"))
        caseList.add(CaseBean(age = 59,id = 20220407,gender = 1,patient_name = "吴*英",dept_name = "漳州市医院\n胸外科",date = "2022年3月4日",des = "重建肺动脉血管，肺静脉血管，支气管，肺，心脏，结节，磨玻璃结节，安全切缘等模型。"))
        caseList.add(CaseBean(age = 56,id = 20220411,gender = 0,patient_name = "谢*东",dept_name = "福建省人民医院\n结直肠外科",date = "2022年4月18日",des = "重建下腔静脉，门静脉血管，动脉血管，肝脏，脾脏，胰腺，胆总管,肾脏，胃肠等模型。"))

        setCaseMenu2()

        setSurfaceView()

        setColorView()

        val perms =
            arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (EasyPermissions.hasPermissions(this,perms[0],perms[1])) {
            setDicomView()
        }else{
            EasyPermissions.requestPermissions(PermissionRequest.Builder(this,666,perms[0],perms[1]).build())
        }



        findViewById<View>(R.id.back).onClick {
            finish()
        }
    }

    //初始化右侧色块选择栏
    private fun setColorView() {

        val layoutManager = CarouselLayoutManager(CarouselLayoutManager.VERTICAL,true)
        layoutManager.setPostLayoutListener(CarouselZoomPostLayoutListener(0.1f))
        colorRecyclerView = findViewById(R.id.rv_color_picker)
        colorRecyclerView.layoutManager = layoutManager
        colorRecyclerView.setHasFixedSize(true)
        colorAdapter = ColorAdapter()
        colorRecyclerView.adapter = colorAdapter
        colorRecyclerView.addOnScrollListener(CenterScrollListener())


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        setDicomView()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        EasyPermissions.requestPermissions(PermissionRequest.Builder(this,666,perms[0],perms[1]).build())
    }


    /**
     * 设置左侧CT栏
     */
    private fun setDicomView() {
        val files = File("/storage/emulated/0/dicomfile/patient1/")
        if (files.listFiles() == null){
            Toast.makeText(this,"No Dicom Files",Toast.LENGTH_LONG).show()
            return
        }
        CommonHelper.loadDicomIntoImageView(files.listFiles()[0],dicom_img_1)
        CommonHelper.loadDicomIntoImageView(files.listFiles()[0],dicom_img_2)
        CommonHelper.loadDicomIntoImageView(files.listFiles()[0],dicom_img_3)
        dicom_img_1.onClick {
            startActivity(Intent(this,DicomViewerActivity::class.java))
        }
        dicom_img_2.onClick {
            startActivity(Intent(this,DicomViewerActivity::class.java))
        }
        dicom_img_3.onClick {
            startActivity(Intent(this,DicomViewerActivity::class.java))
        }
    }

    //设置中心模式展示view
    private fun setSurfaceView() {
        surfaceView = findViewById(R.id.surface)
        customViewer = CustomViewer()
        customViewer.loadEntity()
        customViewer.setSurfaceView(surfaceView)

        //案例数据填充
        fillModelUrlList()


        //Environments and Lightning (OPTIONAL)


        light = EntityManager.get().create()
        val (r, g, b) = Colors.cct(5_500.0f)
        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(r,g,b)
            .intensity(110_000.0f)
            .direction(0.0f, 2f, 1.0f)
            .castShadows(false)
            .build(customViewer.modelViewer.engine,light)
        customViewer.modelViewer.scene.addEntity(light)
        customViewer.loadIndirectLight(this, "venetian_crossroads_2k")
        val floatArray = customViewer.modelViewer.scene.indirectLight?.getRotation(floatArrayOf(0f,1f,0f,-1f,0f,0f,0f,0f,1f))
        customViewer.modelViewer.scene.indirectLight?.setRotation(floatArray!!)
//        val light2 = EntityManager.get().create()
//        LightManager.Builder(LightManager.Type.POINT)
//            .color(r,g,b)
//            .intensity(110_000.0f)ty6ggggg

//            .direction(-1f, 0f, -1.0f)
//            .castShadows(false)
//            .build(customViewer.modelViewer.engine,light2)
//        customViewer.modelViewer.scene.addEntity(light2)

        customViewer.modelViewer.camera.setExposure(16.0f, 1.0f / 125.0f, 100.0f)

//        customViewer.loadEnvironments(this, "venetian_crossroads_2k");
        customViewer.modelViewer.view.blendMode = com.google.android.filament.View.BlendMode.TRANSLUCENT

//        customViewer.modelViewer.scene.skybox = null
//        customViewer.modelViewer.renderer.clearOptions.clear = true
//        buildMaterial() //自定义material

    }

    private fun fillModelUrlList() {
        modelUriList.add("models/谢子东untitled.gltf")
        modelUriList.add("models/untitled_1.gltf")
        modelUriList.add("models/xzd.glb")
        modelUriList.add("models/ylm.glb")
        modelUriList.add("models/wby.glb")
        modelUriList.add("models/xzd.glb")

        colorBeanList.add(ArrayList<ColorBean>())
        colorBeanList.add(ArrayList<ColorBean>())
        colorBeanList.add(ArrayList<ColorBean>())
        colorBeanList.add(ArrayList<ColorBean>())
        colorBeanList.add(ArrayList<ColorBean>())
        colorBeanList.add(ArrayList<ColorBean>())
    }

    private fun setCaseMenu2() {
        caseAdapter = CaseAdapter(if (intent.getIntExtra("menuType",0) == 0) R.layout.item_case else R.layout.item_color_picker)
        caseAdapter.data = caseList
        caseAdapter.setOnItemClickListener { adapter, view, position ->
            if (position!= discreteScrollView.currentItem){
                discreteScrollView.smoothScrollToPosition(position)
            }
        }
        discreteScrollView = findViewById(R.id.scrollView)
        discreteScrollView.setOffscreenItems(2)
        discreteScrollView.setOverScrollEnabled(false)
        discreteScrollView.setSlideOnFling(true)
        discreteScrollView.setItemTransitionTimeMillis(100)

        discreteScrollView.adapter = caseAdapter
        //无限滚动
//        val infiniteScrollAdapter = InfiniteScrollAdapter.wrap(caseAdapter)
//        discreteScrollView.adapter = infiniteScrollAdapter
        discreteScrollView.setItemTransformer(ScaleTransformer.Builder()
            .setMaxScale(0.9f)
            .setMinScale(0.55f)
            .setPivotX(Pivot.X.CENTER)
            .setPivotY(Pivot.Y.CENTER)
            .build())

        emptyView = findViewById(R.id.empty_view)
        discreteScrollView.addOnItemChangedListener { viewHolder, adapterPosition ->
            customViewer.loadGltf(this,modelUriList[adapterPosition])
            setColor()
            showColorChangeView()
        }


        caseAdapter.notifyDataSetChanged()

    }

    /**
     * 展示模型 调色区
     */
    private fun showColorChangeView() {
        colorAdapter.data = colorBeanList[discreteScrollView.currentItem]
        colorAdapter.notifyDataSetChanged()
    }

    private fun setColor() {
        val myInstance =  customViewer.modelViewer.asset?.materialInstances!!

        val entities = customViewer.modelViewer.asset!!.entities

        for (count in entities.indices){
            Log.d("MeshBlendingMode:",customViewer.modelViewer.asset!!.materialInstances[count].material.blendingMode.toString())
            val meshName = decode(customViewer.modelViewer.asset!!.getName(entities[count]))
            Log.d("MeshBlendingMode: mesh_name",meshName)

            val color = Color.parseColor(CommonHelper.colorMap.getOrDefault(meshName,"#FFCA95"))
            Log.d("mesh_color","${Color.red(color)}  ${Color.green(color)}  ${Color.blue(color)}")
            colorBeanList[discreteScrollView.currentItem].add(ColorBean(mesh_name = meshName,r = Color.red(color)/256f,g = Color.green(color)/256f,b = Color.blue(color)/256f,a = 0.5f))
            myInstance[count].setParameter("baseColorFactor",
                colorBeanList[discreteScrollView.currentItem][count].r,
                colorBeanList[discreteScrollView.currentItem][count].g,
                colorBeanList[discreteScrollView.currentItem][count].b,
                colorBeanList[discreteScrollView.currentItem][count].a
            )
        }
    }

    var startShow = false
    val timer = Timer()
    val timerTask = object :TimerTask(){
        override fun run() {
            if (currentFocusPosition == waitFocusPosition){

            }
        }
    }

    private var currentFocusPosition = -1
    private var waitFocusPosition = 2




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