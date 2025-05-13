package com.example.dailymeal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    EditText name,address,phone,email;
    Button update;
    FirebaseFirestore db;
    FirebaseAuth auth;
    ImageView backtoprofile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        name = findViewById(R.id.Name);
        address = findViewById(R.id.Address);
        phone = findViewById(R.id.Phone);
        email = findViewById(R.id.Email);
        update = findViewById(R.id.btnRegister);

        backtoprofile = findViewById(R.id.backtoprofile);
        backtoprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(EditProfile.this,UserProfileActivity.class);
                startActivity(i);
                finish();
            }
        });

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Get passed data
        Intent intent = getIntent();
        name.setText(intent.getStringExtra("name"));
        phone.setText(intent.getStringExtra("phone"));
        email.setText(intent.getStringExtra("email"));
        address.setText(intent.getStringExtra("address"));

        update.setOnClickListener(v -> {
            String uid = auth.getCurrentUser().getUid();

            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("name", name.getText().toString());
            updatedData.put("phone", phone.getText().toString());
            updatedData.put("email", email.getText().toString());
            updatedData.put("address", address.getText().toString());

            db.collection("Users").document(uid).update(updatedData)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Go back to profile page
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Update failed : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
