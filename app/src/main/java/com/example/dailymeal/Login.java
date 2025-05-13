package com.example.dailymeal;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    EditText email, password;
    Button login;
    TextView registertv,tvForget;
    ProgressDialog progressDialog;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        email = findViewById(R.id.email_input);
        password = findViewById(R.id.password_input);
        tvForget = findViewById(R.id.tvForgotPassword);
        login = findViewById(R.id.login_b);
        registertv = findViewById(R.id.register_link);

        tvForget.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, ForgetPassword.class));
        });
        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);

        registertv.setText(Html.fromHtml("Don't have an account? <u>Register</u>"));
        registertv.setOnClickListener(view -> {
            startActivity(new Intent(Login.this, Register.class));
        });

        login.setOnClickListener(view -> {
            String userEmail = email.getText().toString().trim();
            String userPassword = password.getText().toString().trim();

            if (userEmail.isEmpty() || userPassword.isEmpty()) {
                Toast.makeText(Login.this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show progress dialog
            progressDialog.show();
            login.setEnabled(false);

            firebaseAuth.signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(task -> {
                        // Hide progress dialog
                        progressDialog.dismiss();
                        login.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(Login.this, "Login Successfull....", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Login.this, Home.class)); // Your Home activity
                            finish();
                        } else {
                            Toast.makeText(Login.this, "Login Failed: Incorrect email or password!!!"  , Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
