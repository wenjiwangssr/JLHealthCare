package com.example.healthcare.ui.adapter

import android.graphics.Color
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.healthcare.R
import com.example.healthcare.bean.ColorBean
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton

/**
 * 主页右侧色块选择栏数据适配器
 */
class ColorAdapter:BaseQuickAdapter<ColorBean,BaseViewHolder>(R.layout.item_color_picker) {
    override fun convert(holder: BaseViewHolder, item: ColorBean) {
        val text = holder.getView<QMUIRoundButton>(R.id.text)
        text.text = item.mesh_name
        text.setBackgroundColor(Color.argb(item.a,item.r,item.g,item.b))
//        if (item.r*0.299 + item.g*0.587 + item.b*0.144 <= 0.753 || item.a <= 0.4){
        if (item.r * 0.299 + item.g * 0.587 + item.b * 0.144 < 0.753 && item.a > 0.69) {
            text.setTextColor(Color.WHITE)
        }else{
            text.setTextColor(Color.BLACK)
        }
    }
}