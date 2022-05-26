package com.example.healthcare.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import com.example.healthcare.R
import com.example.healthcare.base.BaseSurfaceViewActivity
import com.example.healthcare.bean.ColorBean
import com.example.healthcare.dicom.MoveGestureDetector
import com.example.healthcare.ui.view.TestAdapter
import com.example.healthcare.ui.view.WheelView
import com.example.healthcare.utils.CommonHelper
import com.qmuiteam.qmui.kotlin.onClick
import com.yarolegovich.discretescrollview.DiscreteScrollView
import kotlinx.android.synthetic.main.activity_color_modify.*
import kotlin.math.abs
import kotlin.math.min


/**
 * 模型色块透明度调整页面
 */
class ColorModifyActivity:BaseSurfaceViewActivity() {
    override fun getSurfaceViewId(): Int {
        return R.id.surface
    }

    override fun getContentViewId(): Int {
        return R.layout.activity_color_modify
    }

    private lateinit var wheelView: WheelView
    private lateinit var testAdapter: TestAdapter
    private val colorBeanList = ArrayList<ColorBean>()
    private var currentIndex = 0
    private lateinit var discreteScrollView: DiscreteScrollView

    private lateinit var moveGestureDetector: MoveGestureDetector

    private val moveListener = object : MoveGestureDetector.SimpleOnMoveGestureListener(){
        override fun onMove(detector: MoveGestureDetector?, event: MotionEvent?): Boolean {

            val d = detector!!.focusDelta
            Log.d("onMove","${d.y}  ${d.x}")
            if (abs(d.y) >= abs(d.x) ){//判断滑动为上下滑动
                Log.d("onMove_UP_DOWN","ColorModifyActivity")
                when(event?.pointerCount){
                    1->{

                    }
                    2->{//调整窗宽

                    }
                }

            }else{//判断滑动为左右滑动
                Log.d("onMove_LEFT_RIGHT","ColorModifyActivity")
                when(event?.pointerCount){
                    2 -> {
                        //单指屏幕左侧左滑 -> 进入阅片页
                        if (d.x>0 && event.x <400 ){
                        }
                        else if (d.x<0 && event.x >1500 ){
                        }
                    }
                    1 -> {
                        val colorBean = colorBeanList[currentIndex]
                        var alpha = colorBean.a
                        alpha += d.x/100f
                        if (alpha <0f) alpha = 0f
                        if (alpha >1f) alpha = 1f
                        colorBean.a = alpha


                        customViewer.modelViewer.asset?.materialInstances!![currentIndex].setParameter("baseColorFactor",
                            colorBeanList[currentIndex].r,
                            colorBeanList[currentIndex].g,
                            colorBeanList[currentIndex].b,
                            colorBeanList[currentIndex].a
                        )

                        testAdapter.notifyDataSetChanged()
                    }
                }
            }
            return true
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wheelView = findViewById(R.id.wheel_view)
        moveGestureDetector = MoveGestureDetector(this,moveListener)
        loadModel()
        back.onClick { back() }
    }

     fun back() {
        val intent = Intent()
        intent.putExtra("ColorBeanList",colorBeanList)
        setResult(211,intent)
        finish()
    }

    private fun loadModel() {
        //案例数据填充
        val modelUri = intent.getStringExtra("modelUri")

        val list = intent.getSerializableExtra("colorBeanList") as ArrayList<ColorBean>?
        if (list?.isNotEmpty() == true){
            colorBeanList.addAll(list)
        }else{
            return
        }


//        discreteScrollView = findViewById(R.id.scrollView)
//        discreteScrollView.setOffscreenItems(0)
//        discreteScrollView.setOverScrollEnabled(true)
//        discreteScrollView.setSlideOnFling(true)
////        discreteScrollView.setItemTransitionTimeMillis(100)
//        discreteScrollView.setOrientation(DSVOrientation.VERTICAL)
//        val colorAdapter = ColorAdapter()
//        colorAdapter.data = colorBeanList
//        //无限滚动
//        val infiniteScrollAdapter = InfiniteScrollAdapter.wrap(colorAdapter)
//        discreteScrollView.adapter = infiniteScrollAdapter


//        discreteScrollView.setItemTransformer(
//            ScaleTransformer.Builder()
//            .setMaxScale(1.0f)
//            .setMinScale(0.68f)
//            .setPivotX(Pivot.X.CENTER)
//            .setPivotY(Pivot.Y.CENTER)
//            .build())
//        discreteScrollView.addOnItemChangedListener { viewHolder, adapterPosition ->
//
//        }



        testAdapter = TestAdapter(colorBeanList)
        wheelView.setAdapter(testAdapter)

        wheelView.setOnItemSelectedListener {
            Log.d("Index",it.toString())
            currentIndex = it
        }

        wheelView.setOnTouchMsgListener(WheelView.OnTouchMsgListener {
//            moveGestureDetector.onTouchEvent(it)
        })

        wheelView.mRecyclerView.layoutManager.scrollToPosition(min(5,colorBeanList.size-1))
        currentIndex = min(5,colorBeanList.size-1)

        customViewer.loadGltf(this,modelUri)
        setColor()
    }

    private fun setColor() {
        val myInstance =  customViewer.modelViewer.asset?.materialInstances!!

        val entities = customViewer.modelViewer.asset!!.entities

        for (count in entities.indices){
            Log.d("MeshBlendingMode:",customViewer.modelViewer.asset!!.materialInstances[count].material.blendingMode.toString())
            val meshName =
                CommonHelper.decode(customViewer.modelViewer.asset!!.getName(entities[count]))
            Log.d("MeshBlendingMode: mesh_name",meshName)

            val color = Color.parseColor(CommonHelper.colorMap.getOrDefault(meshName,"#FFCA95"))
            Log.d("mesh_color","${Color.red(color)}  ${Color.green(color)}  ${Color.blue(color)}")
            myInstance[count].setParameter("baseColorFactor",
                colorBeanList[count].r,
                colorBeanList[count].g,
                colorBeanList[count].b,
                colorBeanList[count].a
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        Log.d("onTouchEvent",event.toString())
        moveGestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    override fun onDestroy() {

        super.onDestroy()
    }
}