package com.example.healthcare.bean
//底部案例选择栏数据类
data class CaseBean(
    var age: Int,
    var id: Int,
    var gender: Int, //0 男性； 1女性
    var patient_name: String,
    var dept_name: String,
    var date: String,
    var des:String,

)