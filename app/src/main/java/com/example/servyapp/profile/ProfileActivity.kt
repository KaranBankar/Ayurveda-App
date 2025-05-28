package com.example.servyapp.profile

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.servyapp.R
import com.example.servyapp.databinding.ActivityProfileBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class UserProfile(
    val name: String = "",
    val age: String = "",
    val sex: String = "",
    val occupation: String = "",
    val city: String = "",
    val state: String = "",
    val mobile: String = ""
)

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val database = FirebaseDatabase.getInstance().getReference("Users")
    private val PREF_NAME = "UserPrefs"
    private val KEY_MOBILE = "mobile"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get mobile number from SharedPreferences
        val sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val registeredMobile = sharedPref.getString(KEY_MOBILE, null)

        if (registeredMobile == null) {
            Toast.makeText(this, "No registered mobile number found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        window.statusBarColor = ContextCompat.getColor(this, R.color.Secoundary_Green)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false // if
        // Compare with mobile from Intent (if provided)
        val intentMobile = intent.getStringExtra("MOBILE") ?: registeredMobile

        if (intentMobile != registeredMobile) {
            Toast.makeText(this, "Mobile number does not match registered user", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Fetch and display user profile using registered mobile
        fetchUserProfile(registeredMobile)

        // Save button click listener
        binding.saveButton.setOnClickListener {
            saveUserProfile(registeredMobile)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun fetchUserProfile(mobile: String) {
        database.child(mobile).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(UserProfile::class.java)
                    user?.let {
                        with(binding) {
                            nameEditText.setText(it.name)
                            ageEditText.setText(it.age)
                            sexEditText.setText(it.sex)
                            occupationEditText.setText(it.occupation)
                            cityEditText.setText(it.city)
                            stateEditText.setText(it.state)
                            mobileEditText.setText(it.mobile)
                        }
                    }
                } else {
                    Toast.makeText(this@ProfileActivity, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfileActivity, "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveUserProfile(mobile: String) {
        with(binding) {
            val name = nameEditText.text.toString().trim()
            val age = ageEditText.text.toString().trim()
            val sex = sexEditText.text.toString().trim()
            val occupation = occupationEditText.text.toString().trim()
            val city = cityEditText.text.toString().trim()
            val state = stateEditText.text.toString().trim()
            val mobileInput = mobileEditText.text.toString().trim()

            // Validate inputs
            if (name.isEmpty() || age.isEmpty() || sex.isEmpty() || occupation.isEmpty() ||
                city.isEmpty() || state.isEmpty() || mobileInput.isEmpty()) {
                Toast.makeText(this@ProfileActivity, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return
            }
            if (!mobileInput.matches(Regex("\\d{10}"))) {
                Toast.makeText(this@ProfileActivity, "Invalid mobile number", Toast.LENGTH_SHORT).show()
                return
            }
            if (age.toIntOrNull() == null || age.toInt() < 0 || age.toInt() > 120) {
                Toast.makeText(this@ProfileActivity, "Invalid age", Toast.LENGTH_SHORT).show()
                return
            }

            // Create user profile object
            val userProfile = UserProfile(
                name = name,
                age = age,
                sex = sex,
                occupation = occupation,
                city = city,
                state = state,
                mobile = mobileInput
            )

            // Save to Firebase
            database.child(mobile).setValue(userProfile)
                .addOnSuccessListener {
                    Toast.makeText(this@ProfileActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this@ProfileActivity, "Error updating profile: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}