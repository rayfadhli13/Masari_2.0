package com.chaquo.myapplication

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
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

    private fun overlayImages(background: Bitmap, overlay: Bitmap): Bitmap {
        val combinedImage = Bitmap.createBitmap(background.width, background.height, background.config)
        val canvas = Canvas(combinedImage)
        canvas.drawBitmap(background, 0f, 0f, null)
        val overlayX = (background.width - overlay.width) / 2f // Center horizontally
        val overlayY = (background.height - overlay.height) / 2f // Center vertically
        canvas.drawBitmap(overlay, overlayX, overlayY, null)
        return combinedImage
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fourth)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val selectedStateName = intent.getStringExtra("SELECTED_STATE_NAME") ?: "DefaultState"
        Log.d("FourthActivity", "Selected state name4: $selectedStateName")

        val stateImageResId = when (selectedStateName) {
            "Mubarak Al-Kabeer" -> R.drawable.mubark // Replace with actual drawable resources
            "Al-Ahmadi" -> R.drawable.ahmadi
            "Al-Jahra" -> R.drawable.algahra
            // Add other states as needed
            else -> R.drawable.bg_logo // Default image or error state
        }

        val imageView = findViewById<ImageView>(R.id.imageView)
        val stateImageBitmap = BitmapFactory.decodeResource(resources, stateImageResId)

        val imagePath = intent.getStringExtra("IMAGE_PATH")
        if (imagePath != null) {
            val imgFile = File(imagePath)
            if (imgFile.exists()) {
                val plotBitmapOriginal = BitmapFactory.decodeFile(imgFile.absolutePath)

                val scaleFactor = 3.5f // Adjust this factor to scale up or down
                val plotBitmapScaled = Bitmap.createScaledBitmap(
                    plotBitmapOriginal,
                    (plotBitmapOriginal.width * scaleFactor).toInt(),
                    (plotBitmapOriginal.height * scaleFactor).toInt(),
                    true
                )

                // Now overlay the plot image on the state image
                val combinedImage = overlayImages(stateImageBitmap, plotBitmapScaled)

                // And set the combined image on the ImageView
                imageView.setImageBitmap(combinedImage)
            }
            val bestPathText = intent.getStringExtra("BEST_PATH_TEXT") ?: "Best Path: Not available"
            val distanceBeforeText =
                intent.getStringExtra("DISTANCE_BEFORE_TEXT") ?: "Distance Before: Not available"
            val bestDistanceText =
                intent.getStringExtra("BEST_DISTANCE_TEXT") ?: "Best Distance: Not available"


            val tvBestPath = findViewById<TextView>(R.id.tvBestPath)
            val tvDistanceBefore = findViewById<TextView>(R.id.tvDistanceBefore)
            val tvBestDistance = findViewById<TextView>(R.id.tvBestDistance)

            tvBestPath.text = createLabeledText("Best Path:", bestPathText,)
            tvDistanceBefore.text = createLabeledText("Distance Before:", distanceBeforeText)
            tvBestDistance.text = createLabeledText("Best Distance:", bestDistanceText)


            val btnConfirmSelection = findViewById<Button>(R.id.btnConfirmSelection)
            btnConfirmSelection.setOnClickListener {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }
}