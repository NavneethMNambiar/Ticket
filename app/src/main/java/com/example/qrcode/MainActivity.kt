package com.example.qrcode

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat

import androidx.core.view.setPadding
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var qrtext : EditText
    private lateinit var qrbutton : Button
    private lateinit var qrcode : ImageView
    private lateinit var view : View
    private lateinit var text1 : TextView
    private lateinit var text2 : TextView
    private lateinit var text3 : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        qrbutton=findViewById(R.id.qrbutton)
        qrtext=findViewById(R.id.qrtext)
        qrcode=findViewById(R.id.qrcode)
        text1=findViewById(R.id.textView1)
        text2=findViewById(R.id.textView2)
        text3=findViewById(R.id.textView3)
        view=findViewById(R.id.view1)
        val captureButton = findViewById<Button>(R.id.btn_capture)
        qrbutton.setOnClickListener{
            val data = qrtext.text.toString().trim()

            if(data.isEmpty()){
                Toast.makeText(this ,"Enter a text!",Toast.LENGTH_SHORT).show()

            }else{
                qrcode.alpha = 1.0f
                val writer=QRCodeWriter()
                try{

                    val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512)
                    val width= bitMatrix.width
                    val height= bitMatrix.height
                    val bmp= Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                    for (x in 0 until width){
                        for (y in 0 until height){
                            bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                        }
                    }
                    qrcode.setImageBitmap(bmp)
                    captureButton.alpha= 1.0f
                }catch (e:WriterException){
                    e.printStackTrace()
                }
                qrtext.alpha=0.0f
                qrbutton.alpha=0.0f
                qrtext.isEnabled=false
                qrbutton.isEnabled=false
                view.alpha=0.0f
                text1.setText("TRANSACTION ID : " + abs((123456789..999999999).random()).toString())
                text1.alpha=1.0f
                text2.alpha=1.0f
                text3.alpha=1.0f
            }
        }
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)

        val imageView = findViewById<View>(R.id.layoutid)
        captureButton.setOnClickListener {
            captureButton.isEnabled=false
            captureButton.alpha=0.0f
            val bitmap = getScreenShotFromView(imageView)
            if (bitmap != null) {
                saveMediaToStorage(bitmap)
            }
        }

    }

    private fun getScreenShotFromView(v: View): Bitmap? {

        var screenshot: Bitmap? = null
        try {

            screenshot = Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)

            val canvas = Canvas(screenshot)
            v.draw(canvas)
        } catch (e: Exception) {
            Log.e("GFG", "Failed to capture screenshot because:" + e.message)
        }

        return screenshot
    }



    private fun saveMediaToStorage(bitmap: Bitmap) {

        val filename = "${System.currentTimeMillis()}.jpg"


        var fos: OutputStream? = null


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            this.contentResolver?.also { resolver ->


                val contentValues = ContentValues().apply {


                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }


                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)


                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {

            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        fos?.use {

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this , "Saved to Gallery!" , Toast.LENGTH_SHORT).show()
        }


    }
}