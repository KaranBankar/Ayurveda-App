package com.example.servyapp.survey

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.servyapp.R
import com.example.servyapp.databinding.ActivityCalculationBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class CalculationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCalculationBinding
    private val pointsMap = mapOf("a" to 30, "b" to 20, "c" to 10)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCalculationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.Secoundary_Green)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        // Get total points and user answers from intent
        val totalPoints = intent.getIntExtra("TOTAL_POINTS", 0)
        val userAnswers = intent.getSerializableExtra("USER_ANSWERS") as? Array<Array<String>> ?: Array(15) { Array(3) { "" } }

        // Determine health category
        val (category, categoryColor) = when {
            totalPoints >= 1050 -> "Good" to Color.GREEN
            totalPoints >= 750 -> "Average" to Color.YELLOW
            else -> "Very Bad" to Color.RED
        }

        // Update UI with score and category
        binding.resultText.text = "Your Health Score: $totalPoints\nCategory: $category"

        // Setup pie chart
        setupPieChart(totalPoints, category, categoryColor)

        // Setup line chart
        setupLineChart(userAnswers)

        // Display certificate based on category
        displayCertificate(category)

        // Set up download button
        binding.downloadButton.setOnClickListener {
            saveCertificateToGallery()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupPieChart(score: Int, category: String, categoryColor: Int) {
        val maxScore = 1350f
        val scorePercentage = (score / maxScore) * 100f
        val remainingPercentage = 100f - scorePercentage

        val entries = listOf(
            PieEntry(scorePercentage, category),
            PieEntry(remainingPercentage, "Remaining")
        )

        val dataSet = PieDataSet(entries, "Health Status").apply {
            colors = listOf(categoryColor, Color.GRAY)
            valueTextSize = 14f
            valueTextColor = Color.BLACK
        }

        val pieData = PieData(dataSet)

        with(binding.pieChart) {
            data = pieData
            description.isEnabled = false
            isRotationEnabled = false
            setUsePercentValues(true)
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(14f)
            animateY(1000)
            invalidate()
        }
    }

    private fun setupLineChart(userAnswers: Array<Array<String>>) {
        val setScores = userAnswers.map { setAnswers ->
            setAnswers.sumOf { answer -> pointsMap[answer] ?: 0 }.toFloat()
        }

        val entries = setScores.mapIndexed { index, score -> Entry(index.toFloat(), score) }

        val dataSet = LineDataSet(entries, "Health Score per Set").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            valueTextSize = 12f
            lineWidth = 2f
            setCircleColor(Color.BLUE)
            circleRadius = 4f
            setDrawCircleHole(false)
        }

        val lineData = LineData(dataSet)

        with(binding.lineChart) {
            data = lineData
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter((1..15).map { "Set $it" })
                granularity = 1f
                labelCount = 15
                textSize = 10f
            }

            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 100f
                granularity = 10f
                textSize = 10f
            }
            axisRight.isEnabled = false

            animateX(1000)
            invalidate()
        }
    }

    private fun displayCertificate(category: String) {
        // Retrieve user's name from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("user_name", "User") ?: "User"

        // Set the appropriate certificate based on category
        val certificateRes = when (category) {
            "Good" -> R.drawable.one // Assuming R.drawable.one is your provided certificate image
            "Average" -> R.drawable.two
            else -> R.drawable.three
        }

        // Get the drawable for the certificate
        val certificateDrawable = ContextCompat.getDrawable(this, certificateRes)

        // Ensure the drawable is not null
        certificateDrawable?.let { drawable ->
            // Create a mutable bitmap to draw on
            // Use the intrinsic width and height of the drawable for the bitmap size
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Draw the original certificate image onto the canvas
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            // Draw the user's name on the bitmap
            val paint = Paint().apply {
                color = Color.BLACK // Set text color (e.g., black)
                textSize = 90f // Adjust text size based on your certificate design. This might need tweaking.
                textAlign = Paint.Align.CENTER // Center the text horizontally
                isFakeBoldText = true // Make text bold
                isAntiAlias = true // For smoother text edges
            }

            // Calculate text position
            val xPos = (canvas.width / 2).toFloat() // Center horizontally
            val yPos = (canvas.height * 0.50).toFloat() // Adjusted Y position

            canvas.drawText(userName, xPos, yPos, paint)

            // Set the combined bitmap to the ImageView
            binding.certificateImageView.setImageBitmap(bitmap)
        }
    }

    private fun saveCertificateToGallery() {
        // 1. Retrieve user's name and determine certificate resource
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userName = sharedPreferences.getString("user_name", "User") ?: "User"

        // Recalculate category to get the correct certificate drawable
        val totalPoints = intent.getIntExtra("TOTAL_POINTS", 0)
        val category = when {
            totalPoints >= 1050 -> "Good"
            totalPoints >= 750 -> "Average"
            else -> "Very Bad"
        }
        val certificateRes = when (category) {
            "Good" -> R.drawable.one
            "Average" -> R.drawable.two
            else -> R.drawable.three
        }

        val certificateDrawable = ContextCompat.getDrawable(this, certificateRes)

        if (certificateDrawable == null) {
            Toast.makeText(this, "Certificate image not found. Cannot save.", Toast.LENGTH_SHORT).show()
            return
        }

        // --- Create a high-resolution bitmap for saving ---
        // Use the intrinsic (original) dimensions of the drawable to ensure high resolution
        val highResBitmap = Bitmap.createBitmap(
            certificateDrawable.intrinsicWidth,
            certificateDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(highResBitmap)

        // Draw the original certificate image onto the high-res canvas
        certificateDrawable.setBounds(0, 0, canvas.width, canvas.height)
        certificateDrawable.draw(canvas)

        // Draw the user's name onto the high-res canvas
        val paint = Paint().apply {
            color = Color.BLACK
            // Adjust textSize for the high-resolution bitmap.
            // This should ideally be the same as in displayCertificate for consistency.
            textSize = 90f // Make sure this textSize is appropriate for your certificate's resolution
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            isAntiAlias = true // For smoother text edges
        }

        val xPos = (canvas.width / 2).toFloat()
        // Ensure yPos is correctly calculated for the high-res bitmap
        // Use the same proportional calculation as in displayCertificate
        val yPos = (canvas.height * 0.50).toFloat() // Match this with displayCertificate's yPos

        canvas.drawText(userName, xPos, yPos, paint)
        // --- End high-resolution bitmap creation ---


        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Certificate_$userName$timeStamp.png"

        var fos: FileOutputStream? = null
        var imageUri: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10 (API 29) and above: Use MediaStore
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    // Specify the public Pictures directory and a subfolder for your app
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "ServyAppCertificates")
                    put(MediaStore.MediaColumns.IS_PENDING, 1) // Mark as pending until fully written
                }

                val resolver = contentResolver
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                if (imageUri == null) {
                    Toast.makeText(this, "Failed to create new MediaStore record.", Toast.LENGTH_LONG).show()
                    return
                }

                fos = resolver.openOutputStream(imageUri) as? FileOutputStream
                if (fos == null) {
                    Toast.makeText(this, "Failed to open output stream.", Toast.LENGTH_LONG).show()
                    resolver.delete(imageUri, null, null) // Clean up if stream fails
                    return
                }

                highResBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos) // Use highResBitmap here
                fos.flush()
                fos.close()

                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0) // Mark as not pending
                resolver.update(imageUri, contentValues, null, null)

                Toast.makeText(this, "Certificate saved to Gallery!", Toast.LENGTH_LONG).show()

            } else {
                // For Android 9 (API 28) and below: Use direct file path
                // Ensure WRITE_EXTERNAL_STORAGE permission is granted (handled via manifest maxSdkVersion)
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + File.separator + "ServyAppCertificates"
                val dir = File(imagesDir)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val file = File(dir, fileName)
                fos = FileOutputStream(file)
                highResBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos) // Use highResBitmap here
                fos.flush()
                fos.close()

                // Inform the media scanner about the new file so it appears in Gallery
                val mediaScanIntent = android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                mediaScanIntent.data = Uri.fromFile(file)
                sendBroadcast(mediaScanIntent)

                Toast.makeText(this, "Certificate saved to Gallery!", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            // Clean up if an error occurred during saving with MediaStore (for Q+)
            imageUri?.let { uri ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentResolver.delete(uri, null, null)
                }
            }
            Toast.makeText(this, "Failed to save certificate: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            e.printStackTrace() // Log the detailed error for debugging
        } finally {
            // Ensure FileOutputStream is closed
            try {
                fos?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            // Recycle the bitmap to free up memory
            highResBitmap.recycle()
        }
    }
}