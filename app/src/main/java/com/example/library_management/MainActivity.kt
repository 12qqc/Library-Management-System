package com.example.library_management

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.example.library_management.HomeActivity
import com.example.library_management.DatabaseHelper
import com.example.library_management.LoadingDialog


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var btnRegister: MaterialButton
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var headerLayout: LinearLayout
    private lateinit var loginCard: androidx.cardview.widget.CardView
    private lateinit var logoImage: ImageView
    private lateinit var welcomeText: TextView
    private lateinit var subtitleText: TextView
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set window background for smooth transitions
        window.setBackgroundDrawableResource(android.R.color.transparent)

        loadingDialog = LoadingDialog(this)
            db = DatabaseHelper(this)
        initializeViews()
        startEntranceAnimation()
        setupClickListeners()
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, "App initialization error: " + e.message, android.widget.Toast.LENGTH_LONG).show()
            e.printStackTrace()
            finish()
        }
    }

    private fun initializeViews() {
        try {
        emailInput = findViewById(R.id.email)
        passwordInput = findViewById(R.id.password)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)

        // Initialize additional views for animations
        headerLayout = findViewById(R.id.headerLayout)
        loginCard = findViewById(R.id.loginCard)
        logoImage = findViewById(R.id.logoImage)
        welcomeText = findViewById(R.id.welcomeText)
        subtitleText = findViewById(R.id.subtitleText)
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, "View initialization error: " + e.message, android.widget.Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }


    private fun setupClickListeners() {
        btnRegister.setOnClickListener {
            animateButtonClick(it)
            Handler(Looper.getMainLooper()).postDelayed({
                // Navigate to Register Activity using helper
                com.example.library_management.NavigationHelper.navigateToRegister(this@MainActivity)
            }, 300)
        }

        btnLogin.setOnClickListener {
            animateButtonClick(it)
            Handler(Looper.getMainLooper()).postDelayed({
                performLogin()
            }, 300)
        }
    }

    private fun animateButtonClick(view: View) {
        // Create a sophisticated button press animation
        val scaleDown = ScaleAnimation(
            1.0f, 0.95f, 1.0f, 0.95f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        scaleDown.duration = 100

        val scaleUp = ScaleAnimation(
            0.95f, 1.02f, 0.95f, 1.02f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        scaleUp.duration = 150
        scaleUp.startOffset = 100

        val scaleNormal = ScaleAnimation(
            1.02f, 1.0f, 1.02f, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        scaleNormal.duration = 100
        scaleNormal.startOffset = 250

        val animationSet = AnimationSet(true)
        animationSet.addAnimation(scaleDown)
        animationSet.addAnimation(scaleUp)
        animationSet.addAnimation(scaleNormal)

        view.startAnimation(animationSet)
    }

    private fun performLogin() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        when {
            email.isEmpty() -> {
                emailInput.error = "Email is required"
                emailInput.requestFocus()
                return
            }
            password.isEmpty() -> {
                passwordInput.error = "Password is required"
                passwordInput.requestFocus()
                return
            }
            !isValidEmail(email) -> {
                emailInput.error = "Please enter a valid email"
                emailInput.requestFocus()
                return
            }
            password.length < 6 -> {
                passwordInput.error = "Password must be at least 6 characters"
                passwordInput.requestFocus()
                return
            }
            else -> {
                // Validate user credentials against database
                if (db.validateLogin(email, password)) {
                    // Login successful - user exists in database
                    Toast.makeText(this, "Login Successful! Welcome to Library Management", Toast.LENGTH_SHORT).show()

                    // Navigate to HomeActivity with user email and start with books fragment
                    Handler(Looper.getMainLooper()).postDelayed({
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.putExtra("email", email)
                        intent.putExtra("startFragment", "books")
                        startActivity(intent)
                        finish() // Close login activity
                    }, 1000)
                } else {
                    // User account not found
                    Toast.makeText(this, "Invalid email or password. Please check your credentials or create an account.", Toast.LENGTH_LONG).show()
                    emailInput.error = "Invalid credentials"
                    passwordInput.error = "Invalid credentials"
                    emailInput.requestFocus()
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    private fun navigateToActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivityWithAnimation(intent)
    }

    private fun startEntranceAnimation() {
        // Beautiful dynamic entrance animations
        val handler = Handler(Looper.getMainLooper())

        // Start with all elements invisible
        headerLayout.alpha = 0f
        headerLayout.translationY = -100f
        loginCard.alpha = 0f
        loginCard.translationY = 100f
        logoImage.alpha = 0f
        logoImage.scaleX = 0.5f
        logoImage.scaleY = 0.5f
        welcomeText.alpha = 0f
        subtitleText.alpha = 0f

        // Animate logo with bounce effect
        handler.postDelayed({
            logoImage.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .setInterpolator(AnimationUtils.loadInterpolator(this@MainActivity, android.R.interpolator.bounce))
                .start()
        }, 200)

        // Animate welcome text with slide down
        handler.postDelayed({
            welcomeText.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setInterpolator(AnimationUtils.loadInterpolator(this@MainActivity, android.R.interpolator.accelerate_decelerate))
                .start()
        }, 600)

        // Animate subtitle text
        handler.postDelayed({
            subtitleText.animate()
                .alpha(1f)
                .setDuration(500)
                .setInterpolator(AnimationUtils.loadInterpolator(this@MainActivity, android.R.interpolator.accelerate_decelerate))
                .start()
        }, 900)

        // Animate entire header layout
        handler.postDelayed({
            headerLayout.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(700)
                .setInterpolator(AnimationUtils.loadInterpolator(this@MainActivity, android.R.interpolator.accelerate_decelerate))
                .start()
        }, 300)

        // Animate login card with slide up
        handler.postDelayed({
            loginCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setInterpolator(AnimationUtils.loadInterpolator(this@MainActivity, android.R.interpolator.accelerate_decelerate))
                .start()
        }, 1000)

        // Add continuous subtle floating animation to logo
        handler.postDelayed({
            startFloatingAnimation()
        }, 1500)
    }

    private fun startFloatingAnimation() {
        val floatingAnimation = android.view.animation.TranslateAnimation(
            0f, 0f, 0f, -10f
        )
        floatingAnimation.duration = 2000
        floatingAnimation.repeatCount = android.view.animation.Animation.INFINITE
        floatingAnimation.repeatMode = android.view.animation.Animation.REVERSE
        floatingAnimation.interpolator = AnimationUtils.loadInterpolator(this@MainActivity, android.R.interpolator.accelerate_decelerate)

        logoImage.startAnimation(floatingAnimation)
    }

    private fun startActivityWithAnimation(intent: Intent) {
        startActivity(intent)
        // Beautiful slide transition
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}