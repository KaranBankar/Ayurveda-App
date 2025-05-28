package com.example.servyapp.registration

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowCompat
import com.example.servyapp.R
import com.example.servyapp.databinding.ActivityRegistrationBinding
import com.example.servyapp.survey.SurveyActivity
import com.google.firebase.database.*

data class User(
    val name: String = "",
    val mobile: String = "",
    val age: String = "",
    val sex: String = "",
    val occupation: String = "",
    val city: String = "",
    val state: String = ""
)

class RegistrationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var database: DatabaseReference

    private val indianStates = arrayOf(
        "Select State", "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
        "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand",
        "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur",
        "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab",
        "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura",
        "Uttar Pradesh", "Uttarakhand", "West Bengal",
        "Andaman and Nicobar Islands", "Chandigarh", "Dadra and Nagar Haveli and Daman and Diu",
        "Delhi", "Jammu and Kashmir", "Ladakh", "Lakshadweep", "Puducherry"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().getReference("Users")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, indianStates)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.stateSpinner.adapter = adapter
        binding.stateSpinner.setSelection(0)

        binding.submitButton.setOnClickListener {
            if (validateForm()) {
                binding.progressBar.visibility = View.VISIBLE
                checkMobileNumberAndSave()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.loginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        window.statusBarColor = ContextCompat.getColor(this, R.color.Secoundary_Green)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
    }

    private fun validateForm(): Boolean {
        with(binding) {
            if (nameEditText.text.toString().trim().isEmpty()) {
                nameEditText.error = "Name is required"
                return false
            }
            if (mobileEditText.text.toString().trim().length != 10) {
                mobileEditText.error = "Enter valid 10-digit mobile number"
                return false
            }
            if (ageEditText.text.toString().trim().isEmpty()) {
                ageEditText.error = "Age is required"
                return false
            }
            if (sexRadioGroup.checkedRadioButtonId == -1) {
                Toast.makeText(this@RegistrationActivity, "Please select gender", Toast.LENGTH_SHORT).show()
                return false
            }
            if (occupationEditText.text.toString().trim().isEmpty()) {
                occupationEditText.error = "Occupation is required"
                return false
            }
            if (cityEditText.text.toString().trim().isEmpty()) {
                cityEditText.error = "City is required"
                return false
            }
            if (stateSpinner.selectedItem.toString() == "Select State") {
                Toast.makeText(this@RegistrationActivity, "Please select a state", Toast.LENGTH_SHORT).show()
                return false
            }
            if (!termsCheckbox.isChecked) {
                Toast.makeText(this@RegistrationActivity, "Please accept terms and conditions", Toast.LENGTH_SHORT).show()
                return false
            }
            return true
        }
    }

    private fun checkMobileNumberAndSave() {
        val mobile = binding.mobileEditText.text.toString().trim()

        database.orderByChild("mobile").equalTo(mobile)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        binding.mobileEditText.error = "Mobile number already registered"
                        binding.progressBar.visibility = View.GONE
                    } else {
                        saveUserData()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@RegistrationActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                }
            })
    }

    private fun saveUserData() {
        with(binding) {
            val user = User(
                name = nameEditText.text.toString().trim(),
                mobile = mobileEditText.text.toString().trim(),
                age = ageEditText.text.toString().trim(),
                sex = when (sexRadioGroup.checkedRadioButtonId) {
                    R.id.radio_male -> "Male"
                    R.id.radio_female -> "Female"
                    else -> ""
                },
                occupation = occupationEditText.text.toString().trim(),
                city = cityEditText.text.toString().trim(),
                state = stateSpinner.selectedItem.toString()
            )

            database.child(user.mobile).setValue(user)
                .addOnSuccessListener {
                    Toast.makeText(this@RegistrationActivity, "Registration successful", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                    startActivity(Intent(this@RegistrationActivity, LoginActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this@RegistrationActivity, "Registration failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                }
        }
    }
}