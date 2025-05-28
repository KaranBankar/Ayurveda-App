package com.example.servyapp.survey

import android.content.Context
import android.content.Intent
import android.graphics.Color
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
import com.example.servyapp.profile.ProfileActivity
import com.example.servyapp.registration.LoginActivity
import com.example.servyapp.registration.RegistrationActivity
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
    private val totalSets = 15
    private var totalPoints = 0
    private val pointsMap = mapOf("a" to 30, "b" to 20, "c" to 10)
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private val userAnswers = MutableList(totalSets) { MutableList(3) { "" } } // Stores selected option tags (a, b, c) for each question

    // 15 sets of 3 health-related questions with unique options
    private val questionSets = listOf(
        listOf(
            Question("Q1: Wake up time?", listOf("a) 4 to 6 AM", "b) 6 to 8 AM", "c) 8 to 10 AM")),
            Question("Q2: How do you feel after waking up?", listOf("a) Competely Fresh", "b) Partially Fresh", "c) Dull")),
            Question("Q3: First thing you do after wake up?", listOf("a) Chating a Shloke/Mantra", "b) Go to the Morning chores", "c) Go through/check social media"))
        ),
        listOf(
            Question("Q1: Do you need any external source/substance like tabbaco,tea etc ?", listOf("a) No", "b) Sometimes", "c) Always")),
            Question("Q2: Style of washroom, you use ?", listOf("a) Indian", "b) Sometimes Indian & Western", "c) Western")),
            Question("Q3: Do you apply pressure during defecation process ?", listOf("a) No", "b) Sometimes", "c) Always"))
        ),
        listOf(
            Question("Q1: Kind of Source, you use for dantdhawan ?", listOf("a) Pwigs of plants like Neem", "b) Toothpaste with Natural integradient", "c) Toothpaste with floride content")),
            Question("Q2: How many times, do you clean your brush?", listOf("a) once a day", "b) Sometime once a day", "c) Never Brush")),
            Question("Q3: Do you Brush your teeth of daily basis?", listOf("a) Yes", "b) Sometimes", "c) No"))
        ),
        listOf(
            Question("Q1: What is your Screen Time?", listOf("a) 1 to 3 Hours", "b) 3 to 5 Hours", "c) More than 5 Hohurs")),
            Question("Q2: Do you wear eyeglasses or contact lenses?", listOf("a) No", "b) Sometimes", "c) Yes")),
            Question("Q3: To protect your eyes do you perform Anjana Karma?", listOf("a) Yes Daily", "b) Sometimes on Occusion", "c) None"))
        ),
        listOf(
            Question("Q1: Do you medicated drop in nose?", listOf("a) Yes", "b) Sometimes", "c) No")),
            Question("Q2: Do you often suffer from a blocked or runny nose??", listOf("a) Rarely", "b) Sometimes", "c) Often")),
            Question("Q3: Do you perform Jal Neti dialy?", listOf("a) Always", "b) Sometimes", "c) Rarely"))
        ),
        listOf(
            Question("Q1: Do you gargles daily?", listOf("a) Yes", "b) Sometimes", "c) None")),
            Question("Q2: Type of source do you use per gargles?", listOf("a) Medicated bil/water", "b) Water", "c) Bitadine")),
            Question("Q3: Do you gargle regularly to maintain oral hygiene?", listOf("a) Daily", "b) Weekly", "c) Rarely"))
        ),
        listOf(
            Question("Q1: Do you perform/do oil message daily?", listOf("a) Yes", "b) Sometimes", "c) No")),
            Question("Q2: Nature of oil use?", listOf("a) Medical oil", "b) Simple Coconut oil", "c) None")),
            Question("Q3: Do you perform oil massage on your body daily?", listOf("a) Yes", "b) Sometimes", "c) Daily"))
        ),
        listOf(
            Question("Q1: Do you exercise?", listOf("a) Daily", "b) Occasionally", "c) Never")),
            Question("Q2: Mode of exercise?", listOf("a) Yoga", "b) Gym", "c) Walking")),
            Question("Q3: Durition of Exercise?", listOf("a) 45 Minutes", "b) 30 Minutes", "c) 15 Minutes"))
        ),
        listOf(
            Question("Q1: Do you do Udavartan?", listOf("a) Daily", "b) Sometimes", "c) Not at all")),
            Question("Q2: Have you experienced benefits like improved skin texture or fat reduction from Udvartana?", listOf("a) Yes", "b) Little bit", "c) No")),
            Question("Q3: Do you use Ayurvedic powders or oils during Udvartana massage?", listOf("a) Often", "b) Sometimes", "c) Rarely"))
        ),
        listOf(
            Question("Q1: Do you bath?", listOf("a) Yes, Daily once", "b) Sometimes twice in a day", "c) Once in teo days")),
            Question("Q2: Type of water, you prefer above neck region?", listOf("a) Warm", "b) Cold", "c) Hot")),
            Question("Q3: Type of water, you prefer below neck region?", listOf("a) Warm", "b) Cold", "c) Hot"))
        ),
        listOf(
            Question("Q1: Do you perform meditation?", listOf("a) Daily", "b) Occasionally", "c) Never")),
            Question("Q2: Duration of Medication?", listOf("a) 30 Minutes", "b) 10-20 Minutes", "c) 0-10 Minutes")),
            Question("Q3: Has meditation helped you reduce stress or improve focus?", listOf("a) Yes", "b) Little bit", "c) No"))
        ),
        listOf(
            Question("Q1: Do you take ahar according to Prakrati, rutu (Season)?", listOf("a) Always", "b) Sometimes", "c) Not at all")),
            Question("Q2: What kind of food do you preferred?", listOf("a) Normal Spicy", "b) Intermediate Spicy", "c) Spicy")),
            Question("Q3: Do you take food(ahar) as per hunger?", listOf("a) Yes, as i get hungry I eat", "b) Sometimes", "c) No, as per my daily schedule"))
        ),
        listOf(
            Question("Q1: What kind of food you preferred at night?", listOf("a) Light", "b) Intermediate", "c) heavy")),
            Question("Q2: What time do you eat your dinners?", listOf("a) 6-8 PM", "b) 8-10 PM", "c) After 10 PM")),
            Question("Q3: At what time, do you eat your dinners?", listOf("a) Before 10PM", "b) 10PM - 12AM", "c) After 12AM"))
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySurveyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.Secoundary_Green)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false // if green is dark

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
                    var i=Intent(this, ProfileActivity::class.java)
                    startActivity(i)
                }

                R.id.nav_home->{

                }
                R.id.nav_logout -> {
                    logout()
                }
                R.id.nav_dev->{
                    openWebsite()
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
            clear() // or remove("isLoggedIn") + remove("isRegistered")
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

    private fun displaySet(setNumber: Int) {
        with(binding) {
            // Update set title
            //setTitle.text = "Set $setNumber"
            if(setNumber==1){
                setTitle.text="Bramha Muhurta"
            }else if(setNumber==2){
                setTitle.text="Defecation/Mala Visarjan"
            }else if(setNumber==3){
                setTitle.text="Dhantdhawan"
            }else if(setNumber==4){
                setTitle.text="Anjana Karma"
            }else if(setNumber==5){
                setTitle.text="Nasya"
            }else if(setNumber==6){
                setTitle.text="Gandusha"
            }else if(setNumber==7){
                setTitle.text="Abhyanga"
            }else if(setNumber==8){
                setTitle.text="Exercise"
            }else if(setNumber==9){
                setTitle.text="Udavartana"
            }else if(setNumber==10){
                setTitle.text="Bath(Snana)"
            }else if(setNumber==11){
                setTitle.text="Meditation(Dhanya)"
            }else if(setNumber==12){
                setTitle.text="Aahar"
            }else{
                setTitle.text="Ratri Charya"
            }

            // Show/hide Previous button
            previousButton.visibility = if (setNumber == 1) View.GONE else View.VISIBLE

            // Clear previous questions
            questionContainer.removeAllViews()

            // Display 3 questions
            questionSets[setNumber - 1].forEachIndexed { index, question ->
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

                // Update answers when selection changes
                radioGroup.setOnCheckedChangeListener { _, checkedId ->
                    if (checkedId != -1) {
                        val selectedButton = findViewById<RadioButton>(checkedId)
                        userAnswers[setNumber - 1][index] = selectedButton.tag.toString()
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
            // Iterate through question container to find RadioGroups
            for (i in 0 until questionContainer.childCount) {
                val view = questionContainer.getChildAt(i)
                if (view is RadioGroup) {
                    val checkedId = view.checkedRadioButtonId
                    if (checkedId == -1) {
                        allAnswered = false
                        break
                    }
                    val selectedButton = findViewById<RadioButton>(checkedId)
                    userAnswers[currentSet - 1][i / 2] = selectedButton.tag.toString()
                }
            }

            if (!allAnswered) {
                Toast.makeText(this@SurveyActivity, "Please answer all questions", Toast.LENGTH_SHORT).show()
                return false
            }
            return true
        }
    }

    private fun recalculateTotalPoints() {
        totalPoints = userAnswers.flatten().sumOf { answer ->
            pointsMap[answer] ?: 0
        }
    }
}