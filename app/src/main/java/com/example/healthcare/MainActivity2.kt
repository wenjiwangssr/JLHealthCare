package com.example.healthcare

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.healthcare.ui.UIActivity
import com.example.healthcare.utils.AssetUtils
import java.io.IOException
import java.security.Permission
import java.util.jar.Manifest

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

    }

    override fun onResume() {
        super.onResume()

        val builder = AlertDialog.Builder(this)
        builder.setTitle("选择测试入口")
        builder.setMessage(null)
        builder.setNegativeButton("Cancel") { dialog: DialogInterface?, which: Int ->
            finish()
        }
        val titleList = arrayOf("底部栏样式1","底部栏样式2")
        builder.setItems(titleList,object :DialogInterface.OnClickListener{
            override fun onClick(p0: DialogInterface?, p1: Int) {
                when(p1){
                    0 -> {
                        val intent = Intent(this@MainActivity2, UIActivity::class.java)
                        intent.putExtra("menuType",0)
                        startActivity(intent)
                    }
                    1 -> {
                        val intent = Intent(this@MainActivity2, UIActivity::class.java)
                        intent.putExtra("menuType",1)
                        startActivity(intent)

//                        AssetUtils.createChooserDialog(context = this@MainActivity2,title = "选择模型",message = null,folder = "models",fileRegex = "(?i).*\\.(gltf|glb)"
//                        ) { asset ->
////            val intent = Intent(this@MainActivity2,DemoActivity::class.java)
//                            val intent = Intent(this@MainActivity2, MainActivity::class.java)
//                            intent.putExtra("Uri", asset)
//                            startActivity(intent)
//                        }
                    }
                }
            }

        })
        builder.create().show()



//        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.BLUETOOTH,android.Manifest.permission.BLUETOOTH_ADMIN),999)
    }
}