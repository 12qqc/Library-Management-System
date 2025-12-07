package com.example.library_management;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SecondActivity extends AppCompatActivity {

    EditText email, password;
    Button loginBtn, registerBtn;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Login screen layout
        setContentView(R.layout.activity_main);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.btnLogin);
        registerBtn = findViewById(R.id.btnRegister);

        db = new DatabaseHelper(this);

        loginBtn.setOnClickListener(v -> {

            String emailTxt = email.getText().toString().trim();
            String passTxt = password.getText().toString().trim();

            if (emailTxt.isEmpty() || passTxt.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // FIX: correct function name = loginUser()
            if (db.loginUser(emailTxt, passTxt)) {
                Intent intent = new Intent(SecondActivity.this, HomeActivity.class);
                intent.putExtra("email", emailTxt);
                startActivity(intent);
            } else {
                Toast.makeText(SecondActivity.this, "Invalid Login", Toast.LENGTH_SHORT).show();
            }
        });

        registerBtn.setOnClickListener(v ->
                startActivity(new Intent(SecondActivity.this, RegisterActivity.class))
        );
    }
}
