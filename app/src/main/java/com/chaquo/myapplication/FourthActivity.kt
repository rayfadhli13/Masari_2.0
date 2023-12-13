package com.chaquo.myapplication

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import java.io.File

class FourthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fourth)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        val imageView = findViewById<ImageView>(R.id.imageView)
        val imagePath = intent.getStringExtra("IMAGE_PATH")
        if (imagePath != null) {
            val imgFile = File(imagePath)
            if (imgFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                imageView.setImageBitmap(bitmap)
            }
        }
        val tvResultText = findViewById<TextView>(R.id.tvResultText)
        val resultText = intent.getStringExtra("RESULT_TEXT") ?: "No data available"
        tvResultText.text = resultText
        val btnConfirmSelection = findViewById<Button>(R.id.btnConfirmSelection)
        btnConfirmSelection.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}