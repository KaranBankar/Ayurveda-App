package com.example.servyapp.registration

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.servyapp.R
import com.example.servyapp.survey.SurveyActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.karan.kbutton.KButton

class LoginActivity : AppCompatActivity() {

    private lateinit var mobileEditText: EditText
    private lateinit var loginButton: KButton
    private lateinit var databaseReference: DatabaseReference
    private lateinit var tex: TextView

    private val PREF_NAME = "UserPrefs" // Consistent name
    private val KEY_IS_LOGGED_IN = "isLoggedIn"
    private val KEY_MOBILE = "mobile"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)) {
            startActivity(Intent(this, SurveyActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        databaseReference = FirebaseDatabase.getInstance().getReference("Users")

        mobileEditText = findViewById(R.id.mobile)
        loginButton = findViewById(R.id.kbutton)
        tex=findViewById(R.id.register_text)

        loginButton.setOnClickListener {
            val enteredMobile = mobileEditText.text.toString().trim()

            if (enteredMobile.isEmpty()) {
                Toast.makeText(this, "Please enter mobile number", Toast.LENGTH_SHORT).show()
            } else {
                checkMobileNumber(enteredMobile, sharedPreferences)
            }
        }

        tex.setOnClickListener {
            var i=Intent(this, RegistrationActivity::class.java)
            startActivity(i)
        }
    }

    private fun checkMobileNumber(mobile: String, sharedPreferences: android.content.SharedPreferences) {
        databaseReference.child(mobile).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Save login status in SharedPreferences
                sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, true).apply()

                // Go to ServyActivity
                val intent = Intent(this, SurveyActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please Register First", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to connect to database", Toast.LENGTH_SHORT).show()
        }
    }
}

