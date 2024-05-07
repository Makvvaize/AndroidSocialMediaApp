package com.example.socialmedia

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class RegistrationActivity : AppCompatActivity() {

    private lateinit var editTextFullName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonRegister: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()



        editTextFullName = findViewById(R.id.editTextFullName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonRegister = findViewById(R.id.buttonRegister)

        buttonRegister.setOnClickListener {
            val fullName = editTextFullName.text.toString().trim()
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (fullName.isEmpty()) {
                editTextFullName.error = "Full name is required!"
                editTextFullName.requestFocus()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                editTextEmail.error = "Email is required!"
                editTextEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                editTextPassword.error = "Password is required!"
                editTextPassword.requestFocus()
                return@setOnClickListener
            }

            // Register user with Firebase
            registerUser(email, password , fullName)


        }
    }

    private fun registerUser(email: String, password: String , fullName: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user: FirebaseUser? = auth.currentUser
                    Toast.makeText(
                        this, "Registration successful. Welcome ${user?.email}!",
                        Toast.LENGTH_SHORT
                    ).show()
                    saveUserDataToDatabase(
                        auth.currentUser!!.uid,
                        email,
                        fullName
                    )

                    // Forward to login activity after successful registration
                    val mainIntent = Intent(this, MainActivity::class.java)
                    startActivity(mainIntent)
                    finish() // Optional: Finish the current activity to prevent going back to it when pressing back button on login activity
                } else {
                    // If registration fails, display a message to the user.
                    Toast.makeText(
                        baseContext, "Registration failed. Please try again later.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }



    }
    fun saveUserDataToDatabase(userId: String, email: String, displayName: String) {
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")

        val userData = hashMapOf(
            "userId" to userId,
            "email" to email,
            "displayName" to displayName,

            )

        usersRef.child(userId).setValue(userData)
            .addOnSuccessListener {
                // User data saved successfully
            }
            .addOnFailureListener { e ->
                // Failed to save user data
                // Handle the error
            }
    }

}
