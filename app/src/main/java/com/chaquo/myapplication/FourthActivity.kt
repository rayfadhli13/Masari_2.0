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

    private fun overlayImages(background: Bitmap, overlay: Bitmap, scale: Float = 1.0f): Bitmap {
        // Create a new image with the same size as the background
        val combinedImage = Bitmap.createBitmap(background.width, background.height, background.config)
        val canvas = Canvas(combinedImage)
        canvas.drawBitmap(background, 0f, 0f, null)

        // Calculate the scaling factor to maintain the overlay's aspect ratio
        val scaleFactor = Math.min(
            background.width.toFloat() / overlay.width,
            background.height.toFloat() / overlay.height
        )* scale
        // Calculate the new size of the overlay
        val newOverlayWidth = (overlay.width * scaleFactor).toInt()
        val newOverlayHeight = (overlay.height * scaleFactor).toInt()

        // Create a scaled version of the overlay
        val scaledOverlay = Bitmap.createScaledBitmap(overlay, newOverlayWidth, newOverlayHeight, true)

        // Calculate the position of the overlay to center it on the background
        val overlayX = (background.width - scaledOverlay.width) / 2f
        val overlayY = (background.height - scaledOverlay.height) / 2f

        // Draw the scaled overlay on the canvas
        canvas.drawBitmap(scaledOverlay, overlayX, overlayY, null)

        return combinedImage
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fourth)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val selectedStateName = intent.getStringExtra("SELECTED_STATE_NAME") ?: "DefaultState"
        Log.d("FourthActivity", "Selected state name: $selectedStateName")

        val stateImageResId = when (selectedStateName) {
            "Mubarak Al-Kabeer" -> R.drawable.mubark
            "Al-Ahmadi" -> R.drawable.ahmadi
            "Al-Jahra" -> R.drawable.algahra
            else -> R.drawable.bg_logo
        }

        val imageView = findViewById<ImageView>(R.id.imageView)
        val stateImageBitmap = BitmapFactory.decodeResource(resources, stateImageResId)
        val imagePath = intent.getStringExtra("IMAGE_PATH")

        imagePath?.let {
            val imgFile = File(it)
            if (imgFile.exists()) {
                val plotBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                val scaleFactor = 1.23f // Adjust this to make the overlay plot larger or smaller
                val combinedImage = overlayImages(stateImageBitmap, plotBitmap, scaleFactor)
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