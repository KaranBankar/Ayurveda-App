package com.example.servyapp.survey

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.servyapp.R
import com.example.servyapp.databinding.ActivitySurveyBinding
import com.example.servyapp.profile.AboutUsActivity
import com.example.servyapp.profile.ProfileActivity
import com.example.servyapp.registration.LoginActivity
import com.example.servyapp.survey.CalculationActivity
import com.google.android.material.navigation.NavigationView
import com.google.android.material.radiobutton.MaterialRadioButton

data class Question(
    val text: String,
    val options: List<String>
)

class SurveyActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySurveyBinding
    private var currentSet = 1
    private val totalSets = 13
    private var totalPoints = 0
    private val pointsMap = mapOf("a" to 30, "b" to 20, "c" to 10)
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private val userAnswers = MutableList(totalSets) { MutableList(5) { "" } } // Max 5 questions per set (Set 12)

    // 13 sets of health-related questions with unique options
    private val questionSets = listOf(
        listOf(
            Question("Q1: What time do you Wake Up?", listOf("a) Between 4 to 6 AM", "b) Between 6 to 8 AM", "c) Between 8 to 10 AM")),
            Question("Q2: How do you feel after waking up?", listOf("a) Completely Fresh", "b) Somewhat Fresh", "c) Dull or Tired")),
            Question("Q3: What is the first thing you do after waking up?", listOf("a) Offer Prayer/Chanting a Mantra", "b) Start Morning Chores", "c) Check Your Phone"))
        ),
        listOf(
            Question("Q1: What Type of washroom do you use during defecation?", listOf("a) Indian", "b) Sometimes Indian & Western", "c) Western")),
            Question("Q2: Do you required any external stimulants like tobacco, tea etc. before defecation?", listOf("a) No", "b) Sometimes", "c) Always")),
            Question("Q3: Do you apply pressure during defecation ?", listOf("a) No", "b) Sometimes", "c) Always"))
        ),
        listOf(
            Question("Q1: Which type of material do you use for Tooth Cleaning(dantadhavan)?", listOf("a) Datun (Herbal Stick like Neem,babool etc)", "b) Toothpaste made with natural ingredients", "c) Toothpaste containing fluoride")),
            Question("Q2: How often do you brush your teeth?", listOf("a) Twice a day", "b) once a day", "c) Never brush"))
        ),
        listOf(
            Question("Q1: Do you apply Anjana, To protect your eyes?", listOf("a) Yes, daily", "b) Sometimes", "c) Never")),
            Question("Q2: Do you wear spectacle or contact lenses?", listOf("a) No", "b) Sometimes", "c) Yes, daily")),
            Question("Q3: What is your daily screen time?", listOf("a) Less than 2 hours", "b) 2 to 6 hours", "c) More than 6 hours"))
        ),
        listOf(
            Question("Q1: Do you use medicated nasal drops(nasya)?", listOf("a) Yes", "b) Sometimes", "c) No")),
            Question("Q2: Do you practice Jal Neti daily?", listOf("a) Yes", "b) Occasionally", "c) No"))
        ),
        listOf(
            Question("Q1: Do you gargle everyday?", listOf("a) Yes", "b) Occasionally", "c) No")),
            Question("Q2: What type of liquid do you use?", listOf("a) Cold/room temperature water", "b) Lukewarm water", "c) Medicated oil/kwath"))
        ),
        listOf(
            Question("Q1: Do you perform oil massage daily?", listOf("a) Yes", "b) Occasionally", "c) No")),
            Question("Q2: Which type of oil do you use?", listOf("a) Tila Taila", "b) Narikel Taila", "c) Other oil"))
        ),
        listOf(
            Question("Q1: Do you exercise?", listOf("a) Yes, daily", "b) Occasionally", "c) Never")),
            Question("Q2: What type of exercise do you practice?", listOf("a) Yoga", "b) Gym", "c) Walking")),
            Question("Q3: When do you usually exercise?", listOf("a) Morning", "b) Afternoon", "c) Evening")),
            Question("Q4: What is the duration of your exercise?", listOf("a) More than 45 minutes", "b) 15 to 45 minutes", "c) less than 15 minutes"))
        ),
        listOf(
            Question("Q1: Do you perform Udvartana?", listOf("a) Yes, daily", "b) Occasionally", "c) No")),
            Question("Q2: Do you experience benefits like improved skin texture or fat reduction?", listOf("a) Yes", "b) Little bit", "c) No"))
        ),
        listOf(
            Question("Q1: Do you take a bath?", listOf("a) Yes, daily without fail", "b) Most days, but I skip occasionally", "c) No, I don't bathe daily")),
            Question("Q2: What type of water do you usually use for bathing above the neck region?", listOf("a) Normal (Room Temperature)", "b) Lukewarm", "c) Hot")),
            Question("Q3: What type of water do you usually use for bathing below the neck region?", listOf("a) Warm", "b) Lukewarm", "c) Normal (Room Temperature)"))
        ),
        listOf(
            Question("Q1: Do you meditate?", listOf("a) Yes, daily", "b) Occasionally", "c) Never")),
            Question("Q2: What is the duration of your meditation?", listOf("a) More than 45 minutes", "b) 15 to 45 minutes", "c) Less than 15 minutes")),
            Question("Q3: When do you meditate usually?", listOf("a) Early morning", "b) In the evening", "c) Before sleep"))
        ),
        listOf(
            Question("Q1: Do you eat breakfast?", listOf("a) Yes, daily", "b) Occasionally, when I feel hungry", "c) No, Not at all")),
            Question("Q2: What type of food do you usually have for breakfast?", listOf("a) Fruits", "b) Fermented food", "c) Poha/Upma/Egg, etc.")),
            Question("Q3: Do you eat only when you feel hungry?", listOf("a) Yes, I always", "b) Sometimes", "c) Even when I'm not hungry")),
            Question("Q4: What type of food do you usually eat for lunch?", listOf("a) Vegetarian", "b) Mixed", "c) Non-vegetarian")),
            Question("Q5: How much food do you usually eat in one meal?", listOf("a) Less than needed", "b) Just enough (I feel satisfied, not overly full)", "c) More than needed"))
        ),
        listOf(
            Question("Q1: What time do you usually have dinner?", listOf("a) Before sunset", "b) Between 8 to 9 PM", "c) After 9 PM")),
            Question("Q2: What type of food do you usually eat for dinner?", listOf("a) Light", "b) Sometimes light, sometimes heavy", "c) Heavy")),
            Question("Q3: What time do you go to sleep?", listOf("a) Before 10 PM", "b) Between 10 PM - 12 AM", "c) After 12 AM"))
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySurveyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.Secoundary_Green)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Restore state if available
        savedInstanceState?.let {
            currentSet = it.getInt("currentSet", 1)
            totalPoints = it.getInt("totalPoints", 0)
            val savedAnswers = it.getSerializable("userAnswers") as? Array<Array<String>>
            savedAnswers?.forEachIndexed { setIndex, answers ->
                answers.forEachIndexed { qIndex, answer ->
                    userAnswers[setIndex][qIndex] = answer
                }
            }
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_home -> {
                    // Handle home navigation if needed
                }
                R.id.nav_logout -> {
                    logout()
                }
                R.id.nav_dev -> {
                    openWebsite()
                }
                R.id.nav_settings->{
                    var i=Intent(this, AboutUsActivity::class.java)
                    startActivity(i)
                    finish()
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        // Display first set
        displaySet(currentSet)

        // Next button
        binding.nextButton.setOnClickListener {
            if (saveAndCalculateSetPoints()) {
                if (currentSet < totalSets) {
                    currentSet++
                    // Clear answers for subsequent questions in sets with conditional display
                    if (setOf(6, 7, 8, 9, 11, 12).contains(currentSet)) {
                        val q1Answer = userAnswers[currentSet - 1][0]
                        val maxQuestions = getMaxQuestionsForSet(currentSet, q1Answer)
                        for (i in maxQuestions until userAnswers[currentSet - 1].size) {
                            userAnswers[currentSet - 1][i] = ""
                        }
                    }
                    displaySet(currentSet)
                } else {
                    // Recalculate total points for all sets before navigating
                    recalculateTotalPoints()
                    val intent = Intent(this, CalculationActivity::class.java)
                    intent.putExtra("TOTAL_POINTS", totalPoints)
                    intent.putExtra("USER_ANSWERS", userAnswers.map { it.toTypedArray() }.toTypedArray())
                    startActivity(intent)
                    finish()
                }
            }
        }

        // Previous button
        binding.previousButton.setOnClickListener {
            if (saveAndCalculateSetPoints() && currentSet > 1) {
                currentSet--
                // Clear answers for subsequent questions in sets with conditional display
                if (setOf(6, 7, 8, 9, 11, 12).contains(currentSet)) {
                    val q1Answer = userAnswers[currentSet - 1][0]
                    val maxQuestions = getMaxQuestionsForSet(currentSet, q1Answer)
                    for (i in maxQuestions until userAnswers[currentSet - 1].size) {
                        userAnswers[currentSet - 1][i] = ""
                    }
                }
                displaySet(currentSet)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun openWebsite() {
        val companyUrl = "https://nextserve.in/"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(companyUrl))
        try {
            startActivity(Intent.createChooser(intent, "Open with"))
        } catch (e: Exception) {
            Toast.makeText(this, "No browser found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logout() {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("currentSet", currentSet)
        outState.putInt("totalPoints", totalPoints)
        outState.putSerializable("userAnswers", userAnswers.toTypedArray())
    }

    private fun getMaxQuestionsForSet(setNumber: Int, q1Answer: String): Int {
        return when (setNumber) {
            6, 7, 9 -> if (q1Answer in listOf("a", "b")) 2 else 1
            8 -> if (q1Answer in listOf("a", "b")) 4 else 1
            11 -> if (q1Answer in listOf("a", "b")) 3 else 1
            12 -> if (q1Answer in listOf("a", "b")) 5 else 3 // Q3, Q4, Q5 always shown
            else -> questionSets[setNumber - 1].size
        }
    }

    private fun displaySet(setNumber: Int) {
        with(binding) {
            // Update set title
            setTitle.text = when (setNumber) {
                1 -> "Brahma Muhurta"
                2 -> "Defecation/Mala Visarjan"
                3 -> "Dantdhawan (Tooth Cleaning)"
                4 -> "Anjana Karma"
                5 -> "Nasya"
                6 -> "Gandusha"
                7 -> "Abhyanga"
                8 -> "Exercise"
                9 -> "Udvartana"
                10 -> "Bath (Snana)"
                11 -> "Meditation (Dhyana)"
                12 -> "Aahar"
                else -> "Ratri Charya"
            }

            // Show/hide Previous button
            previousButton.visibility = if (setNumber == 1) View.GONE else View.VISIBLE

            // Clear previous questions
            questionContainer.removeAllViews()

            // Determine number of questions to display based on Q1 answer
            val q1Answer = userAnswers[setNumber - 1][0]
            val questionsToDisplay = when (setNumber) {
                6, 7, 9 -> if (q1Answer in listOf("a", "b")) questionSets[setNumber - 1] else listOf(questionSets[setNumber - 1][0])
                8 -> if (q1Answer in listOf("a", "b")) questionSets[setNumber - 1] else listOf(questionSets[setNumber - 1][0])
                11 -> if (q1Answer in listOf("a", "b")) questionSets[setNumber - 1] else listOf(questionSets[setNumber - 1][0])
                12 -> if (q1Answer in listOf("a", "b")) questionSets[setNumber - 1] else questionSets[setNumber - 1].subList(0, 1) + questionSets[setNumber - 1].subList(2, 5)
                else -> questionSets[setNumber - 1]
            }

            // Display questions
            questionsToDisplay.forEachIndexed { index, question ->
                // Question Text
                val questionText = com.google.android.material.textview.MaterialTextView(this@SurveyActivity).apply {
                    text = question.text
                    textSize = 18f
                    setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
                    setPadding(0, 16, 0, 8)
                    setTypeface(typeface, Typeface.BOLD)
                }
                questionContainer.addView(questionText)

                // RadioGroup for options
                val radioGroup = RadioGroup(this@SurveyActivity).apply {
                    id = View.generateViewId()
                    orientation = RadioGroup.VERTICAL
                }

                // Add radio buttons
                question.options.forEachIndexed { optionIndex, option ->
                    val radioButton = MaterialRadioButton(this@SurveyActivity).apply {
                        id = View.generateViewId()
                        text = option
                        tag = when (optionIndex) {
                            0 -> "a"
                            1 -> "b"
                            2 -> "c"
                            else -> ""
                        }
                        // Restore previous selection if available
                        if (userAnswers[setNumber - 1][index] == tag) {
                            isChecked = true
                        }
                    }
                    radioGroup.addView(radioButton)
                }

                // Update answers and handle Q1 selection changes
                radioGroup.setOnCheckedChangeListener { _, checkedId ->
                    if (checkedId != -1) {
                        val selectedButton = findViewById<RadioButton>(checkedId)
                        userAnswers[setNumber - 1][index] = selectedButton.tag.toString()
                        // If this is Q1 and the set has conditional questions, redraw the set
                        if (index == 0 && setNumber in listOf(6, 7, 8, 9, 11, 12)) {
                            // Clear answers for subsequent questions
                            for (i in 1 until userAnswers[setNumber - 1].size) {
                                userAnswers[setNumber - 1][i] = ""
                            }
                            displaySet(setNumber)
                        }
                    }
                }

                questionContainer.addView(radioGroup)
            }

            // Update button text for last set
            nextButton.text = if (setNumber == totalSets) "Finish" else "Next"
        }
    }

    private fun saveAndCalculateSetPoints(): Boolean {
        with(binding) {
            var allAnswered = true
            val q1Answer = userAnswers[currentSet - 1][0]
            val questionsToCheck = getMaxQuestionsForSet(currentSet, q1Answer)

            // Iterate through displayed questions
            for (i in 0 until questionContainer.childCount step 2) { // Step 2 to skip question texts
                val view = questionContainer.getChildAt(i + 1)
                if (view is RadioGroup) {
                    val questionIndex = i / 2
                    if (questionIndex < questionsToCheck) {
                        val checkedId = view.checkedRadioButtonId
                        if (checkedId == -1) {
                            allAnswered = false
                            break
                        }
                        val selectedButton = findViewById<RadioButton>(checkedId)
                        userAnswers[currentSet - 1][questionIndex] = selectedButton.tag.toString()
                    }
                }
            }

            if (!allAnswered) {
                Toast.makeText(this@SurveyActivity, "Please answer all displayed questions", Toast.LENGTH_SHORT).show()
                return false
            }
            return true
        }
    }

    private fun recalculateTotalPoints() {
        totalPoints = userAnswers.mapIndexed { setIndex, answers ->
            val q1Answer = answers[0]
            val maxQuestions = getMaxQuestionsForSet(setIndex + 1, q1Answer)
            answers.take(maxQuestions).sumOf { answer ->
                pointsMap[answer] ?: 0
            }
        }.sum()
    }
}