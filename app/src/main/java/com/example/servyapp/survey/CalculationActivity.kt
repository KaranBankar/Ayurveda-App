package com.example.servyapp.survey

import android.graphics.Color
import android.os.Bundle
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

class CalculationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCalculationBinding
    private val pointsMap = mapOf("a" to 30, "b" to 20, "c" to 10)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCalculationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.Secoundary_Green)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false // if
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

        // Prepare pie chart entries
        val entries = listOf(
            PieEntry(scorePercentage, category),
            PieEntry(remainingPercentage, "Remaining")
        )

        // Create pie data set
        val dataSet = PieDataSet(entries, "Health Status").apply {
            colors = listOf(categoryColor, Color.GRAY)
            valueTextSize = 14f
            valueTextColor = Color.BLACK
        }

        // Create pie data
        val pieData = PieData(dataSet)

        // Configure pie chart
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
        // Calculate scores for each set
        val setScores = userAnswers.map { setAnswers ->
            setAnswers.sumOf { answer ->
                pointsMap[answer] ?: 0
            }.toFloat()
        }

        // Prepare line chart entries
        val entries = setScores.mapIndexed { index, score ->
            Entry(index.toFloat(), score)
        }

        // Create line data set
        val dataSet = LineDataSet(entries, "Health Score per Set").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            valueTextSize = 12f
            lineWidth = 2f
            setCircleColor(Color.BLUE)
            circleRadius = 4f
            setDrawCircleHole(false)
        }

        // Create line data
        val lineData = LineData(dataSet)

        // Configure line chart
        with(binding.lineChart) {
            data = lineData
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)

            // X-axis configuration
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter((1..15).map { "Set $it" })
                granularity = 1f
                labelCount = 15
                textSize = 10f
            }

            // Y-axis configuration
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
}