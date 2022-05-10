package com.example.healthcare.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.healthcare.R
import com.example.healthcare.bean.CaseBean

class CaseAdapter(layoutResId: Int) : BaseQuickAdapter<CaseBean, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: CaseBean) {
        holder.setText(R.id.patient_name,item.patient_name)
        holder.setText(R.id.date,item.date)
        holder.setText(R.id.age,"[ ${item.age}岁")
        holder.setText(R.id.gender,if (item.gender==0)"男 ]" else "女 ]")
        holder.setText(R.id.dept_name,item.dept_name)
        holder.setText(R.id.detail,"\t ${item.des}")
    }
}