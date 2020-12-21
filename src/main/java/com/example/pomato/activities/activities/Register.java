package com.example.pomato.activities.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pomato.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    public static final String TAG = "TAG";
    public static final String TAG1 = "TAG";
    EditText mName, mEmail, mPassword;
    CheckBox mIsManager;
    Button mBtnRegister;
    TextView mBtnSignIn;
    FirebaseAuth firebaseAuth;
    ProgressBar progressBar;
    FirebaseFirestore firestore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mName = findViewById(R.id.name);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mBtnRegister = findViewById(R.id.btnRegister);
        mBtnSignIn = findViewById(R.id.btnSignIn);
        mIsManager = findViewById(R.id.isManager);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        //progressBar = findViewById(R.id.progressBar);

        //user is automatically directed to Main Activity if logged in
        if ( firebaseAuth.getCurrentUser() != null ){
            startActivity(new Intent(getApplicationContext(), DashUser.class));
            finish();
        }

        //Activates when Register button is clicked
        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mName.getText().toString();
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                //error msg displayed if no email entry
                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Please enter your email.");
                    return;
                }

                //error msg displayed if no passwords entry
                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Please enter a password.");
                    return;
                }

                //error msg displayed if password is less than 6 characters
                if (password.length() < 6) {
                    mPassword.setError("Password must be greater than 6 characters.");
                }

                //register user in firebase
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    //New task if added if required values are present and valid
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //success msg displays if account creation successful
                            Toast.makeText(Register.this, "Account created!", Toast.LENGTH_SHORT).show();
                            //retrieves Uid of current user
                            userID = firebaseAuth.getCurrentUser().getUid();
                            //assigns userID into document reference
                            DocumentReference documentReference = firestore.collection("users").document(userID);
                            //creates new instances of Users
                            Map<String, Object> user = new HashMap<>();
                            //puts the following data into database
                            user.put("fullName", name);
                            user.put("email", email);
                            user.put("password", password);
                            //specify if user is admin
                            if ( mIsManager.isChecked() ) {
                                user.put("isManager", "1");
                                //redirected to admin activity upon manager account creation
                                startActivity(new Intent(getApplicationContext(), DashAdmin.class));
                                finish();
                            } else {
                                user.put("isUser", "1");
                                //redirected to main activity upon regular user acct creation
                               startActivity(new Intent(getApplicationContext(), DashUser.class));
                                finish();
                            }

                            //message displayed upon successful data transfer
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onSuccess: User Profile created for " + userID);
                                }
                                //message displayed upon data transfer failure
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                   Log.d(TAG1, "onFailure: " + e.toString());
                                }
                            });

                            //user is redirected to Main Activity upon sign-up
                            startActivity(new Intent(getApplicationContext(), DashUser.class));
                        } else {
                            //error msg with exception displays upon registration failure
                            Toast.makeText(Register.this, "Error, "
                                    + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            //progress bar shows while loading
                            //progressBar.setVisibility(View.VISIBLE);
                        }

                    }
                });
            }
        });

        //user is redirected to sign-in activity upon click
        mBtnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignIn.class));
            }
        });
    }
}