package com.chaquo.myapplication

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import java.io.File
private fun createLabeledText(label: String, content: String): SpannableString {
    val fullText = "$label $content"
    val spannable = SpannableString(fullText)
    spannable.setSpan(StyleSpan(Typeface.BOLD), 0, label.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    return spannable
}
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
        val bestPathText = intent.getStringExtra("BEST_PATH_TEXT") ?: "Best Path: Not available"
        val distanceBeforeText = intent.getStringExtra("DISTANCE_BEFORE_TEXT") ?: "Distance Before: Not available"
        val bestDistanceText = intent.getStringExtra("BEST_DISTANCE_TEXT") ?: "Best Distance: Not available"


        val tvBestPath = findViewById<TextView>(R.id.tvBestPath)
        val tvDistanceBefore = findViewById<TextView>(R.id.tvDistanceBefore)
        val tvBestDistance = findViewById<TextView>(R.id.tvBestDistance)

        tvBestPath.text = createLabeledText("Best Path:", bestPathText, )
        tvDistanceBefore.text = createLabeledText("Distance Before:", distanceBeforeText)
        tvBestDistance.text = createLabeledText("Best Distance:", bestDistanceText)


        val btnConfirmSelection = findViewById<Button>(R.id.btnConfirmSelection)
        btnConfirmSelection.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}