package com.example.healthcare.utils

import android.app.AlertDialog
import android.content.Context
import com.example.healthcare.utils.AssetUtils
import android.content.DialogInterface
import android.widget.Toast
import java.io.File
import java.io.IOException
import java.util.ArrayList

/**
 * Created by coco on 6/7/15.
 */
object AssetUtils {
    fun createChooserDialog(
        context: Context, title: String?, message: CharSequence?, folder: String,
        fileRegex: String, callback: Callback
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setNegativeButton("Cancel") { dialog: DialogInterface?, which: Int ->
            callback.onClick(
                null
            )
        }
        try {
            val fileList = listFiles(context, folder, fileRegex)
            builder.setItems(fileList) { dialog: DialogInterface?, which: Int ->
                val selectedFile = fileList[which]
                callback.onClick("$folder/$selectedFile")
//                dialog?.dismiss()
            }
            builder.create().show()
        } catch (ex: IOException) {
            Toast.makeText(context, "Error listing assets from $folder", Toast.LENGTH_LONG).show()
        }

    }

    @Throws(IOException::class)
    private fun listFiles(context: Context, folder: String, fileRegex: String): Array<String> {
        val ret: MutableList<String> = ArrayList()
        val list = context.assets.list(folder)
        for (asset in list!!) {
            if (asset.matches(Regex(fileRegex))) {
                ret.add(asset)
            }
        }
        return ret.toTypedArray()
    }

    private fun getFilenames(upLevelFile: File, files: List<File?>): Array<String?> {
        val filenames = arrayOfNulls<String>(files.size)
        for (i in files.indices) {
            if (files[i] == null || upLevelFile === files[i]) {
                filenames[i] = ".."
            } else {
                filenames[i] = files[i]!!.name
            }
        }
        return filenames
    }

    fun interface Callback {
        fun onClick(asset: String?)
    }
}