package com.example.healthcare.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import com.example.healthcare.R
import com.example.healthcare.base.BaseSurfaceViewActivity
import com.example.healthcare.bean.ColorBean
import com.example.healthcare.ui.view.TestAdapter
import com.example.healthcare.ui.view.WheelView
import com.example.healthcare.utils.CommonHelper
import com.yarolegovich.discretescrollview.DiscreteScrollView
import com.yarolegovich.discretescrollview.InfiniteScrollAdapter


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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wheelView = findViewById(R.id.wheel_view)
        loadModel()

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
        //无限滚动
//        val infiniteScrollAdapter = InfiniteScrollAdapter.wrap(Whee)
//        discreteScrollView.adapter = infiniteScrollAdapter
        testAdapter = TestAdapter(colorBeanList)
        wheelView.setAdapter(testAdapter)

        wheelView.setOnItemSelectedListener {
            currentIndex = it
        }

        wheelView.mRecyclerView.smoothScrollToPosition(5)

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


}