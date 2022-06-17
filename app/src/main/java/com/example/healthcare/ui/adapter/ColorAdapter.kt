package com.example.healthcare.ui.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.healthcare.R
import com.example.healthcare.bean.ColorBean
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButtonDrawable
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundFrameLayout
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundRelativeLayout

/**
 * 主页右侧色块选择栏数据适配器
 */
class ColorAdapter:BaseQuickAdapter<ColorBean,BaseViewHolder>(R.layout.item_color_picker3) {
    override fun convert(holder: BaseViewHolder, item: ColorBean) {
        val text = holder.getView<View>(R.id.text)
        if (text is QMUIRoundButton){
            text.text = item.mesh_name
            text.setBackgroundColor(Color.argb(item.a,item.r,item.g,item.b))
//        if (item.r*0.299 + item.g*0.587 + item.b*0.144 <= 0.753 || item.a <= 0.4){
            if (item.r * 0.299 + item.g * 0.587 + item.b * 0.144 < 0.753 && item.a > 0.69) {
                text.setTextColor(Color.WHITE)
            }else{
                text.setTextColor(Color.BLACK)
            }
        }else if (text is TextView){
            text.text = item.mesh_name
//            text.textSize = if (text.lineCount == 1) 18f else 14f
            val container = holder.getView<QMUIRoundRelativeLayout>(R.id.container)
            (container.background as QMUIRoundButtonDrawable).setBgData(ColorStateList.valueOf(Color.argb(item.a,item.r,item.g,item.b)))
//            container.set = Color.argb(item.a,item.r,item.g,item.b)

            val tagImg = holder.getView<ImageView>(R.id.tag_img)
            tagImg.setImageResource(if (item.a<=0.05) R.mipmap.invisible else R.mipmap.visible)
//        if (item.r*0.299 + item.g*0.587 + item.b*0.144 <= 0.753 || item.a <= 0.4){
            if (item.r * 0.299 + item.g * 0.587 + item.b * 0.144 < 0.753 && item.a > 0.69) {
                text.setTextColor(Color.WHITE)
            }else{
                text.setTextColor(Color.BLACK)
            }
        }
    }
}