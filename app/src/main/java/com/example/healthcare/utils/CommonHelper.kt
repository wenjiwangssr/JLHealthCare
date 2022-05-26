package com.example.healthcare.utils

import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.widget.ImageView
import org.dcm4che3.android.RasterUtil
import org.dcm4che3.android.imageio.dicom.DicomImageReader
import org.dcm4che3.data.Tag
import org.dcm4che3.data.VR
import org.dcm4che3.io.DicomInputStream
import java.io.File

object CommonHelper {

    val colorMap:Map<String,String> = mapOf(
        //脏器
        "肝脏" to "#8E8E8E",
        "脾脏" to "#8080C0",
        "肾脏" to "#800080",
        "胰腺" to "#FFCA95",
        "胆囊" to "#00AA00",
        "淋巴结" to "#FFAAFF",
        "十二指肠" to "#FF8080",
        "肠道" to "#FF8080",

        //血管
        "动脉血管" to "#FF0000",
        "下腔静脉血管" to "#0000FF",
        "肝静脉血管" to "#0000FF",
        "门静脉血管" to "#8080FF",
        "胰管" to "#004000",
        "胆总管" to "#004000",
        "未知血管" to "#00FF00",

        //病灶
        "占位" to "#FFFF00",
        "血管瘤" to "#FFAA00",
        "结石" to "#646464",
        "扩张胆管" to "#004000",
        "脾脏结节" to "#FFAA00",
        "脾脏小结节" to "#FFAA00",
        "胆囊壁增厚" to "#FFAA00",
        "异常强化灶" to "#FF7F00",
        "肾囊肿并出血可能" to "#804000",
        "肝囊肿" to "#804000",
        "肾囊肿" to "#804000",
        "小囊肿" to "#804000",
        "憩室" to "#400D0D",
        "异常信号影" to "#550000",
        "积气" to "#000000",
        "水肿" to "#550000",

        //肝分段
        "S1" to "#8C8E8E",
        "尾状叶" to "#8C8E8E",
        "肝Ⅰ段" to "#8C8E8E",
        "S2" to "#FF80FF",
        "肝左外叶上段" to "#FF80FF",
        "肝Ⅱ段" to "#FF80FF",
        "S3" to "#800000",
        "肝左外叶下段" to "#800000",
        "肝Ⅲ段" to "#800000",
        "S4" to "#00FFFF",
        "肝左内叶" to "#00FFFF",
        "肝Ⅳ段" to "#00FFFF",
        "S5" to "#8080FF",
        "肝右前叶下段" to "#8080FF",
        "肝Ⅴ段" to "#8080FF",
        "S6" to "#FFFF00",
        "肝右后叶下段" to "#FFFF00",
        "肝Ⅵ段" to "#FFFF00",
        "S7" to "#0080C0",
        "肝右后叶上段" to "#0080C0",
        "肝Ⅶ段" to "#0080C0",
        "S8" to "#FF007F",
        "肝右前叶上段" to "#FF007F",
        "肝Ⅷ段" to "#FF007F",

        //可能/不确定的病变
        "片状低密度" to "#FFAA00",
        "高信号" to "#FFAA00",
        "低信号影" to "#804000",
        "异常信号" to "#FF8040",
        "高亮" to "#FFFF00",
        "肝脏低密度" to "#804000",
        "低密度影" to "#360D0D",
        "胰腺低密度" to "#804000",
        "梭形低密度灶" to "#360D0D",
        "稍高密度影" to "#FFFF80",
        "皮下异常强化" to "#FFFF80",
        "血管影(动脉期）" to "#FF0000",
        "血管影" to "#55FF00",
        "异常密度影" to "#FF5500",
        "异常灌注可能（动脉期）" to "#AAAA00",
        "癌栓可能" to "#360D0D",
        "副脾" to "#8080CO",

        //医疗器械/药物
        "引流管" to "#FFFFFF",
        "导管" to "#FFFFFF",
        "碘油" to "#AAAA00",
        "粒子" to "#FFFF80",

        //手术方案
        "预留肝脏" to "#FF8080",
        "预切肝脏" to "#00FF00",
        "左半肝" to "#FF8080",
        "右半肝" to "#00FF00",
        "右前叶" to "#FF0080",
        "右后叶" to "#F49FF8",
        "左外叶" to "#A7E19F",
        "左后叶" to "#D7B3A2",

        //脏器
        "心脏" to "#FF8080",
//        "淋巴结" to "#FFAAFF",
        "食道" to "#FF8080",

        //血管
        "肺动脉血管" to "#0000FF",
        "肺动脉" to "#0000FF",
        "肺静脉血管" to "#FF0000",
        "肺静脉" to "#FF0000",
        "主动脉血管" to "#FF0000",
        "主动脉" to "#FF0000",
        "上腔静脉血管" to "#0000FF",
        "上腔静脉" to "#0000FF",
        "支气管" to "#FFFFFF",
//        "未知血管" to "#00FF00",

        //病灶
        "磨玻璃结节" to "#FFFF00",
        "实性结节" to "#FFFF00",
        "粟粒结节" to "#FFFF00",
        "疑似结节" to "#FFFF80",
        "占位" to "#FFFF00",
        "小结节" to "#FFAA00",

        //肺叶
        "左肺" to "#E8FF9A",
        "右肺" to "#E8FF9A",
        "左肺上叶" to "#FF557F",
        "左肺下叶" to "#004000",
        "右肺上叶" to "#FF0000",
        "右肺中叶" to "#FFFF00",
        "右肺下叶" to "#55FF00",

        //左肺叶分段
        "左肺上叶尖后段" to "#FF0000",
        "左肺S1+2" to "#FF0000",
        "左肺上叶前段" to "#00FFFF",
        "左肺S3" to "#00FFFF",
        "左肺上叶上舌段" to "#FFFF82",
        "左肺S4" to "#FFFF82",
        "左肺上叶下舌段" to "#FF6345",
        "左肺S5" to "#FF6345",
        "左肺下叶背段" to "#FF7F7F",
        "左肺S6" to "#FF7F7F",
        "左肺下叶前内基底段" to "#7FFF00",
        "左肺S7+8" to "#7FFF00",
        "左肺下叶外基底段" to "#7F4000",
        "左肺S9" to "#7F4000",
        "左肺下叶后基底段" to "#54007E",
        "左肺S10" to "#54007E",

        //右肺叶分段
        "右肺上叶尖段" to "#FF0000",
        "右肺S1" to "#FF0000",
        "右肺上叶后段" to "#AAFFAA",
        "右肺S2" to "#AAFFAA",
        "右肺上叶前段" to "#00FFFF",
        "右肺S3" to "#00FFFF",
        "右肺中叶外段" to "#FFFF82",
        "右肺S4" to "#FFFF82",
        "右肺中叶内段" to "#FF6345",
        "右肺S5" to "#FF6345",
        "右肺下叶背段" to "#FF7F7F",
        "右肺S6" to "#FF7F7F",
        "右肺下叶内基底段" to "#7FFF00",
        "右肺S7" to "#7FFF00",
        "右肺下叶前基底段" to "#FFFF00",
        "右肺S8" to "#FFFF00",
        "右肺下叶外基底段" to "#7F4000",
        "右肺S9" to "#7F4000",
        "右肺下叶后基底段" to "#54007E",
        "右肺S10" to "#54007E",

        //左肺叶分段再分
        "左肺S1+2a" to "#8C8E8E",
        "左肺S1+2b" to "#FF80FF",
        "左肺S1+2c" to "#800000",
        "左肺S3+2a" to "#00FFFF",
        "左肺S3+2b" to "#8080FF",
        "左肺S3+2c" to "#FFFF00",
        "左肺S3a" to "#AAFFAA",
        "左肺S3b" to "#FF7F7F",
        "左肺S3c" to "#0080CO",

        //手术方案
        "2cm安全切缘" to "#FFFFFF",
//        "" to "",
    )
    fun dp2px(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp,
            context.resources.displayMetrics
        )
    }
    fun decode(unicodeStr: String?): String {
        if (unicodeStr == null) {
            return ""
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


    /**
     * 加载dicom文件并在ImageView中展示
     * @param file dicom文件
     * @param imageView 用于展示的ImageView
     */
    fun loadDicomIntoImageView(file: File, imageView: ImageView) {
        val dr = DicomImageReader()
        try {
            //dcm文件输入流
            val dcmInputStream = DicomInputStream(file)
            //属性对象
            val attrs = dcmInputStream.readDataset(-1, -1)
            //输出所有属性信息
            Log.d("TAG", "输出所有属性信息1:$attrs")

            //获取行
            val row = attrs.getInt(Tag.Rows, 1)
            //获取列
            val columns = attrs.getInt(Tag.Columns, 1)


            attrs.setFloat(Tag.WindowCenter, VR.FL,40f)
            attrs.setFloat(Tag.WindowWidth, VR.FL,400f)
            //窗宽窗位
            val win_center = attrs.getFloat(Tag.WindowCenter, 1f)
            val win_width = attrs.getFloat(Tag.WindowWidth, 1f)

            Log.d("TAG", "" + "row=" + row + ",columns=" + row + "row*columns = " + row * columns)
            Log.d("TAG", "win_center=$win_center,win_width=$win_width")
            //获取像素数据 ，这个像素数据不知道怎么用！！！，得到的是图片像素的两倍的长度
            //后面那个 raster.getByteData()是图片的像素数据
            val b = attrs.getSafeBytes(Tag.PixelData)
            if (b != null) {
                Log.d("TAG", "" + "b.length=" + b.size)
            } else {
                Log.d("TAG", "" + "b==null")
            }

            //修改默认字符集为GB18030
            attrs.setString(Tag.SpecificCharacterSet, VR.CS, "GB18030") //解决中文乱码问题
            Log.d("TAG", "输出所有属性信息2:$attrs")
            val patientName = attrs.getString(Tag.PatientName, "")
//            tv_name.setText("姓名：$patientName")
            val text = attrs.getString(Tag.PerformedSeriesSequence,"")
            //生日
            val patientBirthDate = attrs.getString(Tag.PatientBirthDate, "")
//            tv_birthday.setText("生日：$patientBirthDate")

            //机构
            val institution = attrs.getString(Tag.InstitutionName, "")
//            tv_institution.setText("机构：$institution")

            //站点
            val station = attrs.getString(Tag.StationName, "")
//            tv_station.setText("站点：$station")

            //制造商
            val Manufacturer = attrs.getString(Tag.Manufacturer, "")
//            tv_manufacturer.setText("制造商：$Manufacturer")

            //制造商模型
            val ManufacturerModelName = attrs.getString(Tag.ManufacturerModelName, "")
//            tv_manufacturerModelName.setText("制造商模型：$ManufacturerModelName")


            //描述--心房
            val description = attrs.getString(Tag.StudyDescription, "")
//            tv_StudyDescription.setText("描述--心房：$description")
            //描述--具体
            val SeriesDescription = attrs.getString(Tag.SeriesDescription, "")
//            tv_SeriesDescription.setText("描述--具体：$SeriesDescription")

            //描述时间
            val studyData = attrs.getString(Tag.StudyDate, "")
//            tv_StudyDate.setText("描述时间：$studyData")
            dr.open(file)
            //            Attributes ds = dr.getAttributes();
//            String wc = ds.getString(Tag.WindowCenter);
//            String ww = ds.getString(Tag.WindowWidth);
//            Log.e("TAG", "" + "wc=" + wc + ",ww=" + ww);
            val raster = dr.applyWindowCenter(
                0,
                win_width.toInt(), win_center.toInt()
            )
            //            Log.e("TAG", "" + "raster.getWidth()=" + raster.getWidth() + ",raster.getHeight()=" + raster.getHeight());
//            Log.e("TAG", "" + "raster.getByteData().length=" + raster.getByteData().length);

//            Bitmap bmp = RasterUtil.gray8ToBitmap(raster.getWidth(), raster.getHeight(), raster.getByteData());
//            Log.e("TAG", "b==raster.getByteData()" + (b == raster.getByteData()));
            val bmp = RasterUtil.gray8ToBitmap(columns, row, raster.byteData)
            imageView.setImageBitmap(bmp) //显示图片
        } catch (e: Exception) {
            Log.e("TAG", "" + e)
        }
    }

    var lastClickTime = 0L
    fun isFastClick():Boolean{
        val temp = lastClickTime
        lastClickTime = System.currentTimeMillis()
        return System.currentTimeMillis() - temp <= 500
    }
//    var lastClickTime = 0L
//    fun isDoubleClick():Boolean{
//    }


}