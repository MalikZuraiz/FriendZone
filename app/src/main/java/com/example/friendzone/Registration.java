package com.example.friendzone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Registration extends AppCompatActivity {

    Button signup;
    TextView back_to_login;
    EditText UN,email,pass,repass;

    ProgressDialog progressDialog;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        signup = findViewById(R.id.signup);
        back_to_login = findViewById(R.id.backtologin);
        UN = findViewById(R.id.Username);
        email = findViewById(R.id.email);
        pass = findViewById(R.id.password);
        repass = findViewById(R.id.retypepassword);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering User....");
    }

    public void SignUp(View view) {
        String Email = email.getText().toString().trim();
        String UserName= UN.getText().toString().trim();
        String Password= pass.getText().toString().trim();
        String RePassword= repass.getText().toString().trim();

        if(!Patterns.EMAIL_ADDRESS.matcher(Email).matches())
        {
            email.setError("Invalid Email");
            email.setFocusable(true);
        }
        else if(Password.length()<6)
        {
            pass.setError("Password Should be atleast 6 characters");
            email.setFocusable(true);
        }
        else if(Password == RePassword)
        {
            pass.setError("Password Not Matched..!");
            email.setFocusable(true);
        }
        else
        {
            registeruser(Email,Password);
        }

    }

    private void registeruser(String email, String password) {

        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // User created successfully
                        FirebaseUser user = mAuth.getCurrentUser();

                        //Database Work
                        String emaildb = user.getEmail();
                        String uid = user.getUid();

                        //registering store user into db using hashmap

                        HashMap<Object,String> hashMap = new HashMap<>();
                        //put info into hashmap

                        hashMap.put("email",emaildb);
                        hashMap.put("uid",uid);
                        hashMap.put("phone","");
                        hashMap.put("image","");
                        hashMap.put("name","");
                        hashMap.put("cover","");


                        //firebase db instance

                        FirebaseDatabase database = FirebaseDatabase.getInstance();

                        //Storing in path called "Users"

                        DatabaseReference reference = database.getReference("Users");

                        // put data within hashmap in db

                        reference.child(uid).setValue(hashMap);



                        Log.d("TAG", "User created: " + user.getEmail());
                        startActivity(new Intent(Registration.this, DashboardActivity.class));
                        finish();
                    } else {
                        // User creation failed
                        progressDialog.dismiss();
                        String errorMessage = task.getException().getMessage();
                        Log.e("TAG", "Error creating user: " + errorMessage);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(Registration.this, "User Already Exist", Toast.LENGTH_SHORT).show();
                    }
                });

    }


    public void backlogin(View view) {
        Intent intent = new Intent(Registration.this,LoginActivity.class);
        startActivity(intent);
    }
}