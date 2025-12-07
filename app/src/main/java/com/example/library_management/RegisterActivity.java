package com.example.library_management;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import androidx.cardview.widget.CardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText emailInput, passwordInput;
    private MaterialButton registerBtn, backBtn;
    private DatabaseHelper db;
    private LinearLayout headerLayout;
    private CardView registerCard;
    private TextView titleText, subtitleText;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper(this);
        initializeViews();
        startEntranceAnimation();
        setupClickListeners();
    }

    private void initializeViews() {
        emailInput = findViewById(R.id.regEmail);
        passwordInput = findViewById(R.id.regPassword);
        registerBtn = findViewById(R.id.btnRegister);
        backBtn = findViewById(R.id.btnBack);

        // Initialize views for animations
        headerLayout = findViewById(R.id.headerLayout);
        registerCard = findViewById(R.id.registerCard);
        titleText = findViewById(R.id.titleText);
        subtitleText = findViewById(R.id.subtitleText);
    }

    private void setupClickListeners() {
        registerBtn.setOnClickListener(v -> {
            animateButtonClick(v);
            new Handler(Looper.getMainLooper()).postDelayed(this::registerUser, 300);
        });

        backBtn.setOnClickListener(v -> {
            animateButtonClick(v);
            new Handler(Looper.getMainLooper()).postDelayed(this::navigateToLogin, 300);
        });
    }

    private void animateButtonClick(View view) {
        // Create a sophisticated button press animation
        ScaleAnimation scaleDown = new ScaleAnimation(
            1.0f, 0.95f, 1.0f, 0.95f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleDown.setDuration(100);

        ScaleAnimation scaleUp = new ScaleAnimation(
            0.95f, 1.02f, 0.95f, 1.02f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleUp.setDuration(150);
        scaleUp.setStartOffset(100);

        ScaleAnimation scaleNormal = new ScaleAnimation(
            1.02f, 1.0f, 1.02f, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleNormal.setDuration(100);
        scaleNormal.setStartOffset(250);

        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(scaleDown);
        animationSet.addAnimation(scaleUp);
        animationSet.addAnimation(scaleNormal);

        view.startAnimation(animationSet);
    }

    private void startEntranceAnimation() {
        Handler handler = new Handler(Looper.getMainLooper());

        // Start with elements invisible
        if (headerLayout != null) {
            headerLayout.setAlpha(0f);
            headerLayout.setTranslationY(-100f);
        }
        if (registerCard != null) {
            registerCard.setAlpha(0f);
            registerCard.setTranslationY(100f);
        }
        if (titleText != null) {
            titleText.setAlpha(0f);
        }
        if (subtitleText != null) {
            subtitleText.setAlpha(0f);
        }

        // Animate title text
        handler.postDelayed(() -> {
            if (titleText != null) {
                titleText.animate()
                    .alpha(1f)
                    .setDuration(600)
                    .setInterpolator(AnimationUtils.loadInterpolator(this, android.R.interpolator.accelerate_decelerate))
                    .start();
            }
        }, 200);

        // Animate subtitle text
        handler.postDelayed(() -> {
            if (subtitleText != null) {
                subtitleText.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .setInterpolator(AnimationUtils.loadInterpolator(this, android.R.interpolator.accelerate_decelerate))
                    .start();
            }
        }, 500);

        // Animate header layout
        handler.postDelayed(() -> {
            if (headerLayout != null) {
                headerLayout.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(700)
                    .setInterpolator(AnimationUtils.loadInterpolator(this, android.R.interpolator.accelerate_decelerate))
                    .start();
            }
        }, 300);

        // Animate register card
        handler.postDelayed(() -> {
            if (registerCard != null) {
                registerCard.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(800)
                    .setInterpolator(AnimationUtils.loadInterpolator(this, android.R.interpolator.accelerate_decelerate))
                    .start();
            }
        }, 800);
    }

    private void registerUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validation
        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }

        if (!isValidEmail(email)) {
            emailInput.setError("Please enter a valid email address");
            emailInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            passwordInput.requestFocus();
            return;
        }

        // Register user
        if (db.registerUser(email, password)) {
            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_LONG).show();
            navigateToLogin();
        } else {
            Toast.makeText(this, "Registration failed. Email may already exist.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
