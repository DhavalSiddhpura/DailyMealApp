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

import java.util.HashMap;

public class Register extends AppCompatActivity {

    Button register;
    EditText name, email, password, phone, address;
    TextView logintv;
    ProgressDialog progressDialog;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        name = findViewById(R.id.Name);
        email = findViewById(R.id.Email);
        password = findViewById(R.id.Password);
        phone = findViewById(R.id.Phone);
        address = findViewById(R.id.Address);
        logintv = findViewById(R.id.login_link);
        register = findViewById(R.id.btnRegister);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering user...");
        progressDialog.setCancelable(false);

        logintv.setText(Html.fromHtml("Already you have registered? <u>Login</u>"));

        logintv.setOnClickListener(view -> {
            startActivity(new Intent(Register.this, Login.class));
        });

        register.setOnClickListener(view -> {

            String userName = name.getText().toString().trim();
            String userEmail = email.getText().toString().trim();
            String userPassword = password.getText().toString().trim();
            String userPhone = phone.getText().toString().trim();
            String userAddress = address.getText().toString().trim();

            if (userName.isEmpty() || userEmail.isEmpty() || userPassword.isEmpty() ||
                    userPhone.isEmpty() || userAddress.isEmpty()) {
                Toast.makeText(Register.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show the loading dialog
            progressDialog.show();
            register.setEnabled(false);

            firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String uid = firebaseAuth.getCurrentUser().getUid();

                            HashMap<String, Object> userMap = new HashMap<>();
                            userMap.put("uid", uid);
                            userMap.put("name", userName);
                            userMap.put("email", userEmail);
                            userMap.put("phone", userPhone);
                            userMap.put("address", userAddress);

                            firestore.collection("Users").document(uid)
                                    .set(userMap)
                                    .addOnSuccessListener(aVoid -> {
                                        progressDialog.dismiss();
                                        register.setEnabled(true);
                                        Toast.makeText(Register.this, "Registration Successfull...", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(Register.this, Login.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        progressDialog.dismiss();
                                        register.setEnabled(true);
                                        Toast.makeText(Register.this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });

                        } else {
                            progressDialog.dismiss();
                            register.setEnabled(true);
                            Toast.makeText(Register.this, "Auth Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
