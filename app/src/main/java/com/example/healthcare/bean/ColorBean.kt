package com.example.healthcare.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

//右侧模型调色栏数据类
@Parcelize
data class ColorBean(
    var mesh_name: String,
    var r:Float,
    var g:Float,
    var b:Float,
    var a:Float,
):Parcelable