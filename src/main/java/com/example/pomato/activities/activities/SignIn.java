package com.example.pomato.activities.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pomato.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignIn extends AppCompatActivity {
    public static final String TAG = "TAG";
    EditText mEmail, mPassword;
    Button mBtnSignIn;
    TextView mBtnRegister;
    ProgressBar progressBar;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    boolean valid = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mBtnSignIn = findViewById(R.id.btnSignIn);
        mBtnRegister = findViewById(R.id.btnRegister);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        Log.d(TAG, "onClick" + mEmail.getText().toString());

        mBtnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                //error msg displayed if no email entry
                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Please enter your email.");
                    valid = false;
                    return;
                }

                //error msg displayed if no passwords entry
                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Please enter a password.");
                    valid = false;
                    return;
                }

                //error msg displayed if password is less than 6 characters
                if (password.length() < 6) {
                    mPassword.setError("Password must be greater than 6 characters.");
                }

                if ( valid ) {
                    firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            Toast.makeText(SignIn.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                            //checks for user access level upon sign in
                            checkAccessLevel(authResult.getUser().getUid());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SignIn.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });

        //user is redirected to register activity upon click
        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Register.class));
            }
        });

    }

    //checks database for uid data
    private void checkAccessLevel(String uid) {
        DocumentReference df = firestore.collection("users").document(uid);
        //extract data from document
        df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            //documentSnapshot contains data of particular uid value
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d(TAG, "onSuccess: " + documentSnapshot.getData());
                //identify access level
                if ( documentSnapshot.getString("isManager") != null) {
                    //user is admin
                    startActivity(new Intent(getApplicationContext(), DashAdmin.class));
                    finish();
                }
                if ( documentSnapshot.getString("isUser") != null) {
                    //user is normal user
                    startActivity(new Intent(getApplicationContext(), DashUser.class));
                    finish();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //user is automatically directed to the correct activity if logged in
        if ( firebaseAuth.getCurrentUser() != null ){
            DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if ( documentSnapshot.getString("isManager") != null ) {
                        startActivity(new Intent(getApplicationContext(), DashAdmin.class));
                        finish();
                    }
                    if ( documentSnapshot.getString("isUser") != null ) {
                        startActivity(new Intent(getApplicationContext(), DashUser.class));
                        finish();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                //user is redirected to sign-in page if there are any errors with the session
                public void onFailure(@NonNull Exception e) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(), SignIn.class));
                }
            });
        }
    }

}