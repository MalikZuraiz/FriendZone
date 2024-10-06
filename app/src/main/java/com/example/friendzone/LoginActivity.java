package com.example.friendzone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;

    Button login;
    EditText emaill,passl;

    TextView forgetpasswordl;
    ProgressDialog pd;

    SignInButton googlesigninl;

    private FirebaseAuth mAuth;

    private GoogleSignInClient mGoogleSignInClient;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emaill = findViewById(R.id.emaillogin);
        passl = findViewById(R.id.passwordlogin);
        forgetpasswordl = findViewById(R.id.forgetpassword);
        mAuth = FirebaseAuth.getInstance();
        pd = new ProgressDialog(this);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.googlesignin).setOnClickListener(view -> signInWithGoogle());

    }

    public void Back_to_Login(View view) {
        Intent intent = new Intent(LoginActivity.this, Registration.class);
        startActivity(intent);
        finish();
    }

    // Login User with Email & Password

    public void Login(View view) {
        String Emaill = emaill.getText().toString().trim();
        String Passwordl= passl.getText().toString().trim();

        if(!Patterns.EMAIL_ADDRESS.matcher(Emaill).matches())
        {
            emaill.setError("Invalid Email");
            emaill.setFocusable(true);
        }
        else
        {
            loginuser(Emaill,Passwordl);
        }
    }

    private void loginuser(String emaill, String passwordl) {
        pd.setMessage("Logging In");
        pd.show();

        mAuth.signInWithEmailAndPassword(emaill, passwordl)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // User created successfully
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d("TAG", "User created: " + user.getEmail());
                        startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                        finish();
                    } else {
                        // User creation failed
                        pd.dismiss();
                        String errorMessage = task.getException().getMessage();
                        Log.e("TAG", "Error creating user: " + errorMessage);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(LoginActivity.this, "Authenication Failed", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    //Recover Password Method

    public void RecoverPassword(View view) {
        showRecoverPassword();
    }

    private void showRecoverPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");


        LinearLayout linearLayout = new LinearLayout(this);

        EditText emailET = new EditText(this);
        emailET.setHint("Enter Email");
        emailET.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        emailET.setMinEms(16);

        linearLayout.addView(emailET);
        linearLayout.setPadding(10,10,10,10);


        builder.setView(linearLayout);


        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String emailT = emailET.getText().toString().trim();
                beginrecovery(emailT);

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

            }
        });

        builder.create().show();


    }

    private void beginrecovery(String emailT) {
        pd.setMessage("Sending email....");
        pd.show();
        mAuth.sendPasswordResetEmail(emailT).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                pd.dismiss();
                if(task.isSuccessful())
                {
                    Toast.makeText(LoginActivity.this, "Email Sent to given Email address", Toast.LENGTH_SHORT).show();
                    
                }
                else
                {
                    Toast.makeText(LoginActivity.this, "Failed to Sent Email", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(LoginActivity.this, "", Toast.LENGTH_SHORT).show();

            }
        });

    }

    //Google Sign-in Method

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();

                            if(task.getResult().getAdditionalUserInfo().isNewUser())
                            {
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

                            }



                            Log.d("TAG", "signInWithCredential:success");
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }


}