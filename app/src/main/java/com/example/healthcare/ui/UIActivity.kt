package com.example.healthcare.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ccy.focuslayoutmanager.FocusLayoutManager
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.example.healthcare.CustomViewer
import com.example.healthcare.R
import com.example.healthcare.bean.CaseBean
import com.example.healthcare.bean.ColorBean
import com.example.healthcare.dicom.DicomViewerActivity
import com.example.healthcare.dicom.MoveGestureDetector
import com.example.healthcare.ui.adapter.CaseAdapter
import com.example.healthcare.ui.adapter.ColorAdapter
import com.example.healthcare.ui.view.PDFViewActivity
import com.example.healthcare.utils.CommonHelper
import com.example.healthcare.utils.CommonHelper.decode
import com.google.android.filament.*
import com.ldoublem.loadingviewlib.view.LVCircularRing
import com.qmuiteam.qmui.kotlin.onClick
import com.yarolegovich.discretescrollview.DiscreteScrollView
import com.yarolegovich.discretescrollview.transform.Pivot
import com.yarolegovich.discretescrollview.transform.ScaleTransformer
import kotlinx.android.synthetic.main.activity_dicom_viewer.*
import kotlinx.android.synthetic.main.activity_uiactivity.*
import me.rosuh.filepicker.config.FilePickerManager
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs


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

    private lateinit var moveGestureDetector: MoveGestureDetector

    private lateinit var loadingView: LVCircularRing

    private lateinit var timer:Timer
    private lateinit var timerTask:TimerTask


    private fun initTimerTask() {
        timer = Timer()
        timerTask = object :TimerTask(){
            override fun run() {
                runOnUiThread {
                    loadingView.stopAnim()
                    loadingView.visibility = View.GONE
                }
            }
        }
    }

    val TO_COLOR_MODIFY = 211

    var lastClickTime = 0L

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

        setLoadingView()

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

        moveGestureDetector = MoveGestureDetector(this,moveListener)

        findViewById<View>(R.id.back).onClick {
            finish()
        }
    }

    /**
     * 设置LoadingView
     */
    private fun setLoadingView() {
        loadingView = findViewById(R.id.loading_view)
        loadingView.setViewColor(R.color.main_light)
    }

    //初始化右侧色块选择栏
    private fun setColorView() {

//        val layoutManager = CarouselLayoutManager(CarouselLayoutManager.VERTICAL,true)
//        layoutManager.setPostLayoutListener(CarouselZoomPostLayoutListener(0.1f))
//        colorRecyclerView = findViewById(R.id.rv_color_picker)
//        colorRecyclerView.layoutManager = layoutManager
//        colorRecyclerView.setHasFixedSize(true)
//        colorAdapter = ColorAdapter()
//        colorRecyclerView.adapter = colorAdapter
//        colorRecyclerView.addOnScrollListener(CenterScrollListener())

        colorRecyclerView = findViewById(R.id.rv_color_picker)
        colorRecyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        colorRecyclerView.overScrollMode = View.OVER_SCROLL_NEVER
//        colorRecyclerView.setHasFixedSize(true)

        colorAdapter = ColorAdapter()
        colorRecyclerView.adapter = colorAdapter

        colorAdapter.setOnItemClickListener { adapter, view, position ->
            if (isFastClick())return@setOnItemClickListener
            val intent = Intent(this@UIActivity, ColorModifyActivity::class.java)
            intent.putExtra("modelUri", modelUriList[discreteScrollView.currentItem])
            intent.putExtra("colorBeanList", colorBeanList[discreteScrollView.currentItem])
            startActivityForResult(intent,TO_COLOR_MODIFY)
        }

    }

    fun isFastClick():Boolean{
        val temp = lastClickTime
        lastClickTime = System.currentTimeMillis()
        return System.currentTimeMillis() - temp <= 500
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
//        dicom_img_1.onClick {
//            val intent = Intent(this,DicomViewerActivity::class.java)
//            intent.putExtra("modelUri",modelUriList[discreteScrollView.currentItem])
//            intent.putExtra("colorBeanList",colorBeanList[discreteScrollView.currentItem])
//            startActivity(intent)
//        }
//        dicom_img_2.onClick {
//            val intent = Intent(this,DicomViewerActivity::class.java)
//            intent.putExtra("modelUri",modelUriList[discreteScrollView.currentItem])
//            intent.putExtra("colorBeanList",colorBeanList[discreteScrollView.currentItem])
//            startActivity(intent)
//        }
//        dicom_img_3.onClick {
//            val intent = Intent(this,DicomViewerActivity::class.java)
//            intent.putExtra("modelUri",modelUriList[discreteScrollView.currentItem])
//            intent.putExtra("colorBeanList",colorBeanList[discreteScrollView.currentItem])
//            startActivity(intent)
//        }
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
//        customViewer.loadIndirectLight(this, "environments/venetian_crossroads_2k/venetian_crossroads_2k_ibl.ktx")
        customViewer.loadIndirectLight(this, "ibl/default_env_ibl.ktx")
//        customViewer.loadIndirectLight(this, "ibl/pillars_2k_ibl.ktx")
//        val floatArray = customViewer.modelViewer.scene.indirectLight?.getRotation(floatArrayOf(0f,1f,0f,-1f,0f,0f,0f,0f,1f))

//        customViewer.modelViewer.camera.setExposure(16.0f, 1.0f / 125.0f, 100.0f)

//        customViewer.loadEnvironments(this, "venetian_crossroads_2k");
        customViewer.modelViewer.view.blendMode = com.google.android.filament.View.BlendMode.TRANSLUCENT


    }

    private fun fillModelUrlList() {
//        modelUriList.add("models/谢子东untitled.gltf")
        modelUriList.add("models/untitled_1.gltf")
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
        discreteScrollView.setItemTransitionTimeMillis(130)

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

        discreteScrollView.addScrollListener { scrollPosition, currentPosition, newPosition, currentHolder, newCurrent ->

        }
        discreteScrollView.addScrollStateChangeListener(object :DiscreteScrollView.ScrollStateChangeListener<RecyclerView.ViewHolder>{
            override fun onScrollStart(
                currentItemHolder: RecyclerView.ViewHolder,
                adapterPosition: Int
            ) {
                Log.d("onScroll","onScrollStart...")
                loadingView.visibility = View.VISIBLE
                loadingView.startAnim()
            }

            override fun onScrollEnd(
                currentItemHolder: RecyclerView.ViewHolder,
                adapterPosition: Int
            ) {
                Log.d("onScroll","onScrollEnd...")
            }

            override fun onScroll(
                scrollPosition: Float,
                currentPosition: Int,
                newPosition: Int,
                currentHolder: RecyclerView.ViewHolder?,
                newCurrent: RecyclerView.ViewHolder?
            ) {
            }

        })
        emptyView = findViewById(R.id.empty_view)
        discreteScrollView.addOnItemChangedListener { viewHolder, adapterPosition ->
//            if (firstSelect){
//                firstSelect = false
//                return@addOnItemChangedListener
//            }
            val time = System.currentTimeMillis()
            Log.d("TimeTag","StartLoadingModel...... $time")
            customViewer.loadGltf(this,modelUriList[adapterPosition])
            Log.d("TimeTag","FinishLoadingModel...... Cost time:${System.currentTimeMillis() - time}")
            if (colorBeanList[adapterPosition].isEmpty()){
                setColor()
            }else{
                refreshColor()
            }

            initTimerTask()
            timer.schedule(timerTask,400)
//            loadingView.stop()
//            loadingView.visibility = View.GONE
//            Log.d("TimeTag","SetColor...... Cost time:${System.currentTimeMillis() - time}")

            showColorChangeView()
        }

//        caseAdapter.notifyDataSetChanged()

    }
    var firstSelect = true

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


        for (count in 0..entities.size-1){
            Log.d("MeshBlendingMode:",customViewer.modelViewer.asset!!.materialInstances[count].material.blendingMode.toString())
            val meshName = decode(customViewer.modelViewer.asset!!.getName(entities[count]))
            Log.d("MeshBlendingMode: mesh_name",meshName)

            val color = Color.parseColor(CommonHelper.colorMap.getOrDefault(meshName,"#FFCA95"))
            Log.d("mesh_color","${Color.red(color)}  ${Color.green(color)}  ${Color.blue(color)}")
            colorBeanList[discreteScrollView.currentItem].add(ColorBean(mesh_name = meshName,r = Color.red(color)/256f,g = Color.green(color)/256f,b = Color.blue(color)/256f,a = 0.7f))
            myInstance[count].setParameter("baseColorFactor",
                colorBeanList[discreteScrollView.currentItem][count].r,
                colorBeanList[discreteScrollView.currentItem][count].g,
                colorBeanList[discreteScrollView.currentItem][count].b,
                colorBeanList[discreteScrollView.currentItem][count].a
            )
        }
    }

    private val moveListener = object : MoveGestureDetector.SimpleOnMoveGestureListener(){
        override fun onMove(detector: MoveGestureDetector?, event: MotionEvent?): Boolean {

            val d = detector!!.focusDelta
            Log.d("onMove","${d.y}  ${d.x}")
            if (abs(d.y) >= abs(d.x) ){//判断滑动为上下滑动
                Log.d("onMove_UP_DOWN","---------------")

                when(event?.pointerCount){
                    1->{
                        if (d.y>0  && !stateJumping ){
                            stateJumping = true
                            val intent = Intent(this@UIActivity,PDFViewActivity::class.java)
                            startActivity(intent)
                            Log.d("go","3333333333333")
                        }
                    }
                    2->{//调整窗宽

                    }
                }

            }else{//判断滑动为左右滑动
                Log.d("onMove_LEFT_RIGHT","---------------")

                when(event?.pointerCount){
                    2 -> {
                        //单指屏幕左侧左滑 -> 进入阅片页
                        if (d.x>0 && event.x <400 && !stateJumping ){
                            stateJumping = true
                            val intent = Intent(this@UIActivity,DicomViewerActivity::class.java)
                            intent.putExtra("modelUri",modelUriList[discreteScrollView.currentItem])
                            intent.putExtra("colorBeanList",colorBeanList[discreteScrollView.currentItem])
                            startActivity(intent)
                            Log.d("go","1111111111111")
                        }
                        else if (d.x<0 && event.x >1500 && !stateJumping){
                            stateJumping = true
                            val intent = Intent(this@UIActivity,ColorModifyActivity::class.java)
                            intent.putExtra("modelUri",modelUriList[discreteScrollView.currentItem])
                            intent.putExtra("colorBeanList",colorBeanList[discreteScrollView.currentItem])
                            startActivity(intent)
                            Log.d("go","2222222222222")
                        }
                    }
                    1 -> {

                    }
                }

            }
            return true
        }
    }

    private var stateJumping = false

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        Log.d("onTouchEvent",event.toString())
        moveGestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(resultCode){
            TO_COLOR_MODIFY -> {
                if ( data != null){
                    colorBeanList[discreteScrollView.currentItem].clear()
                    colorBeanList[discreteScrollView.currentItem].addAll(data.getSerializableExtra ("ColorBeanList") as ArrayList<ColorBean>)
                    colorAdapter.notifyDataSetChanged()
                    refreshColor()
                }
            }
        }
    }

    private fun refreshColor() {
        val myInstance =  customViewer.modelViewer.asset?.materialInstances!!

        val entities = customViewer.modelViewer.asset!!.entities

        for (count in entities.indices){
            myInstance[count].setParameter("baseColorFactor",
                colorBeanList[discreteScrollView.currentItem][count].r,
                colorBeanList[discreteScrollView.currentItem][count].g,
                colorBeanList[discreteScrollView.currentItem][count].b,
                colorBeanList[discreteScrollView.currentItem][count].a
            )
        }
    }

    override fun onResume() {
        super.onResume()
        stateJumping = false
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