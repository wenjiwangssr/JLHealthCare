package com.example.healthcare.dicom

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.healthcare.R
import com.example.healthcare.base.BaseSurfaceViewActivity
import com.example.healthcare.bean.ColorBean
import com.example.healthcare.utils.CommonHelper
import com.imebra.*
import com.qmuiteam.qmui.widget.QMUIRadiusImageView
import kotlinx.android.synthetic.main.activity_dicom_viewer.*
import me.rosuh.filepicker.config.FilePickerManager
import org.dcm4che3.android.RasterUtil
import org.dcm4che3.android.imageio.dicom.DicomImageReader
import org.dcm4che3.data.Attributes
import org.dcm4che3.data.Tag
import org.dcm4che3.data.VR
import org.dcm4che3.io.DicomInputStream
import java.io.File
import java.nio.ByteBuffer
import kotlin.math.abs

@SuppressLint("SetTextI18n")
class DicomViewerActivity : BaseSurfaceViewActivity() {
    private lateinit var mImageView:ImageView
    val REQUESTCODE_FROM_ACTIVITY = 666
    private val dicomFiles = ArrayList<File>()
    var currentIndex = 0
    var mFocusX = 0f
    var mFocusY = 0f

    //窗宽及其范围
    var windowCenter = 40f
    val windowCenterMAX = 600f
    val windowCenterMIN = -500f

    //窗位及其范围
    var windowWidth = 400f
    val windowWidthMAX = 2000f
    val windowWidthMIN = 85f

    private val colorBeanList = ArrayList<ColorBean>()

    private val moveListener = object :MoveGestureDetector.SimpleOnMoveGestureListener(){
        override fun onMove(detector: MoveGestureDetector?,event: MotionEvent?): Boolean {

            val d = detector!!.focusDelta
            Log.d("onMove","${d.y}  ${d.x}")
            if (abs(d.y)>= abs(d.x) ){//判断滑动为上下滑动
                Log.d("onMove_UP_DOWN","---------------")

                when(event?.pointerCount){
                    1->{
                        mFocusY -= d.y
                        mFocusY = Math.max(0f,mFocusY)
                        mFocusY = Math.min((dicomFiles.size-1)*15f,mFocusY)

                        val index = mFocusY.toInt()/15
                        if (index!=currentIndex){
                            currentIndex = index
                            loadDicomIntoImageView(dicomFiles[currentIndex],mImageView)
                            tier.text = "当前层/总层数： ${currentIndex+1}/${dicomFiles.size}"
                        }

                        Log.d("onMove", "$mFocusX   $mFocusY")
                        Log.d("onMove dxdy", d.x.toString() + "   " + d.y)
                    }

                    2->{//调整窗宽
                        windowWidth += d.y
                        windowWidth = Math.max(windowWidthMIN,windowWidth)
                        windowWidth = Math.min(windowWidthMAX,windowWidth)
                        window_state.text = "窗宽/窗位： ${windowWidth.toInt()}/${windowCenter.toInt()}"
                        loadDicomIntoImageView(dicomFiles[currentIndex],mImageView)
                    }
                }

            }else{//判断滑动为左右滑动
                Log.d("onMove_LEFT_RIGHT","---------------")

                when(event?.pointerCount){
                    1 -> {
                        //单指屏幕左侧左滑 -> 返回
                        if (d.x>0 && event.x <220 ){
                            finish()
                        }
                    }
                    2 -> {
                        //调整窗位
                        Log.d("onMove_change","---------------")
                        windowCenter += d.x
                        windowCenter = Math.max(windowCenterMIN,windowCenter)
                        windowCenter = Math.min(windowCenterMAX,windowCenter)
                        window_state.text = "窗宽/窗位： ${windowWidth.toInt()}/${windowCenter.toInt()}"
                        loadDicomIntoImageView(dicomFiles[currentIndex],mImageView)
                    }
                }

            }
            return true
        }
    }


    //载入模型
    private fun loadModel() {
        //案例数据填充
        val modelUri = intent.getStringExtra("modelUri")

        val list = intent.getSerializableExtra("colorBeanList") as ArrayList<ColorBean>?
        if (list?.isNotEmpty() == true){
            colorBeanList.addAll(list)
        }

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



    private lateinit var moveGestureDetector: MoveGestureDetector

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        Log.d("onTouchEvent",event.toString())
        moveGestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    @SuppressLint("ClickableViewAccessibility")


    override fun getSurfaceViewId(): Int = R.id.surface
    override fun getContentViewId(): Int =
        R.layout.activity_dicom_viewer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadModel()

        mImageView = findViewById(R.id.mImageView)

        moveGestureDetector = MoveGestureDetector(this,moveListener)

//        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE) , 111);
//        FilePickerManager
//            .from(this)
//            .forResult(REQUESTCODE_FROM_ACTIVITY)

        val files = File("/storage/emulated/0/dicomfile/patient1/")
        if (files.listFiles() == null){
            Toast.makeText(this,"No Dicom Files", Toast.LENGTH_LONG).show()
            return
        }
        dicomFiles.clear()
        dicomFiles.addAll(
            files.listFiles()
                .filter { it.path.endsWith(".DCM",true) }
                .sortedBy { it.name }

        )

        loadDicomIntoImageView(dicomFiles[0],mImageView)
    }

    /**
     * 初始化信息栏
     */
    private fun initMsgView(attrs: Attributes) {
        window_state.text = "窗宽/窗位： ${windowWidth.toInt()}/${windowCenter.toInt()}"
        tier.text = "当前层/总层数： ${currentIndex+1}/${dicomFiles.size}"
        val patientName = attrs.getString(Tag.PatientName, "")
        tv_name.text = "病人姓名：$patientName"

        //生日
        val patientBirthDate = attrs.getString(Tag.PatientBirthDate, "")
//            tv_birthday.setText("生日：$patientBirthDate")

        //机构
        val institution = attrs.getString(Tag.InstitutionName, "")
        tv_institution.text = "医院：$institution"

        //站点
        val station = attrs.getString(Tag.StationName, "")
//            tv_station.setText("站点：$station")

        //制造商
        val manufacturer = attrs.getString(Tag.Manufacturer, "")
        tv_manufacturer.text = "制造商：$manufacturer"

        //制造商模型
        val manufacturerModelName = attrs.getString(Tag.ManufacturerModelName, "")
//            tv_manufacturerModelName.setText("制造商模型：$ManufacturerModelName")


        //描述--心房
        val description = attrs.getString(Tag.StudyDescription, "")
//            tv_StudyDescription.setText("描述--心房：$description")
        //描述--具体
        val seriesDes = attrs.getString(Tag.SeriesDescription, "")
        tv_SeriesDescription.text = if (seriesDes.split(" ").size>=2) "层厚：${seriesDes.split(" ")[1]}" else "层厚：${seriesDes}"

        //描述时间
        val studyData = attrs.getString(Tag.StudyDate, "")
//            tv_StudyDate.setText("描述时间：$studyData")
    }

    /**
     * 加载dicom文件并在ImageView中展示
     * @param file dicom文件
     * @param imageView 用于展示的ImageView
     */
    fun loadDicomIntoImageView(file: File, imageView: ImageView) {
        findViewById<TextView>(R.id.dicom_file_name).text = file.name

        val dr = DicomImageReader()
        try {
            //dcm文件输入流
            val dcmInputStream = DicomInputStream(file)
            //属性对象
            val attrs = dcmInputStream.readDataset(-1, -1)

            //获取行
            val row = attrs.getInt(Tag.Rows, 1)
            //获取列
            val columns = attrs.getInt(Tag.Columns, 1)


            attrs.setFloat(Tag.WindowCenter, VR.FL,windowCenter)
            attrs.setFloat(Tag.WindowWidth, VR.FL,windowWidth)

            //修改默认字符集为GB18030
            attrs.setString(Tag.SpecificCharacterSet, VR.CS, "GB18030") //解决中文乱码问题
            Log.d("TAG", "输出所有属性信息:$attrs")

            if (currentIndex == 0){
                initMsgView(attrs)
            }

            dr.open(file)
            //            Attributes ds = dr.getAttributes();
//            String wc = ds.getString(Tag.WindowCenter);
//            String ww = ds.getString(Tag.WindowWidth);
//            Log.e("TAG", "" + "wc=" + wc + ",ww=" + ww);
            val raster = dr.applyWindowCenter(
                0,
                windowWidth.toInt(), windowCenter.toInt()
            )
            //            Log.e("TAG", "" + "raster.getWidth()=" + raster.getWidth() + ",raster.getHeight()=" + raster.getHeight());
//            Log.e("TAG", "" + "raster.getByteData().length=" + raster.getByteData().length);

//            Bitmap bmp = RasterUtil.gray8ToBitmap(raster.getWidth(), raster.getHeight(), raster.getByteData());
//            Log.e("TAG", "b==raster.getByteData()" + (b == raster.getByteData()));
            val bmp = RasterUtil.gray8ToBitmap(columns, row, raster.byteData)
            imageView.setImageBitmap(bmp) //显示图片
        } catch (e: Exception) {
            Log.d("TAG", "" + e)
        }
    }

    fun loadDICOM(name: String) {
        val dataSet = CodecFactory.load(name)
        val image = dataSet.getImage(0)
        Log.d("height", "" + image.height)
        Log.d("width", "" + image.width)
        val height = image.height
        val width = image.width
        val chain = TransformsChain()

        val draw = DrawBitmap(chain)

// Ask for the size of the buffer (in bytes)
        val requestedBufferSize = width * height * 4
        val buffer =
            ByteArray(requestedBufferSize.toInt()) // Ideally you want to reuse this in subsequent calls to getBitmap()
        val byteBuffer = ByteBuffer.wrap(buffer)

// Now fill the buffer with the image data and create a bitmap from it
        draw.getBitmap(image, drawBitmapType_t.drawBitmapRGBA, 4, buffer)
        val renderBitmap = Bitmap.createBitmap(
            image.width.toInt(),
            image.height.toInt(), Bitmap.Config.ARGB_8888
        )
        renderBitmap.copyPixelsFromBuffer(byteBuffer)
        val imageView = findViewById<QMUIRadiusImageView>(R.id.mImageView)
        imageView.setImageBitmap(renderBitmap)
    }

    private fun loadDicomFile(file: File?) {
        CodecFactory.setMaximumImageSize(8000, 8000)

//        val filePath = Uri.fromFile(file)
//        val stream = contentResolver.openInputStream(filePath)

        val stream = contentResolver.openInputStream(Uri.fromFile(file))

        // The usage of the Pipe allows to use also files on Google Drive or other providers

        // The usage of the Pipe allows to use also files on Google Drive or other providers
        val imebraPipe = PipeStream(32000)

        // Launch a separate thread that read from the InputStream and pushes the data
        // to the Pipe.

        // Launch a separate thread that read from the InputStream and pushes the data
        // to the Pipe.
        val pushThread = Thread(PushToImebraPipe(imebraPipe, stream))
        pushThread.start()

        // The CodecFactory will read from the Pipe which is feed by the thread launched
        // before. We could just pass a file name to it but this would limit what we
        // can read to only local files

        // The CodecFactory will read from the Pipe which is feed by the thread launched
        // before. We could just pass a file name to it but this would limit what we
        // can read to only local files
        val loadDataSet = CodecFactory.load(StreamReader(imebraPipe.streamInput))


        // Get the first frame from the dataset (after the proper modality transforms
        // have been applied).


        // Get the first frame from the dataset (after the proper modality transforms
        // have been applied).
        val dicomImage = loadDataSet.getImageApplyModalityTransform(0)

        // Use a DrawBitmap to build a stream of bytes that can be handled by the
        // Android Bitmap class.

        // Use a DrawBitmap to build a stream of bytes that can be handled by the
        // Android Bitmap class.
        val chain = TransformsChain()

        if (ColorTransformsFactory.isMonochrome(dicomImage.colorSpace)) {
            val voilut =
                VOILUT(VOILUT.getOptimalVOI(dicomImage, 0, 0, dicomImage.width, dicomImage.height))
            chain.addTransform(voilut)
        }
        val drawBitmap = DrawBitmap(chain)
        val memory = drawBitmap.getBitmap(dicomImage, drawBitmapType_t.drawBitmapRGBA, 4)

        // Build the Android Bitmap from the raw bytes returned by DrawBitmap.

        // Build the Android Bitmap from the raw bytes returned by DrawBitmap.
        val renderBitmap = Bitmap.createBitmap(
            dicomImage.width.toInt(),
            dicomImage.height.toInt(), Bitmap.Config.ARGB_8888
        )
        val memoryByte = ByteArray(memory.size().toInt())
        memory.data(memoryByte)
        val byteBuffer = ByteBuffer.wrap(memoryByte)
        renderBitmap.copyPixelsFromBuffer(byteBuffer)

        // Update the image
        mImageView.setImageBitmap(renderBitmap)
//        mImageView.scaleType = ImageView.ScaleType.CENTER_INSIDE

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUESTCODE_FROM_ACTIVITY -> {
                if (resultCode == Activity.RESULT_OK && data != null){
//                    val file = data.getParcelableArrayListExtra<Uri>(FilePickerConst.KEY_SELECTED_MEDIA)
                    val list = FilePickerManager.obtainData()
//                    loadDicomFile(File(list[0]))
                    loadDicomIntoImageView(File(list[0]),mImageView)
                    val sb = StringBuilder()
                }
            }
            111 -> {

            }
        }

    }

}