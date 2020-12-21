package com.example.pomato.activities.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pomato.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class DashAdmin extends AppCompatActivity {
    private SharedPreferences sharedPref;
    private ImageView mBtnProfileEdit;
    private TextView mProjects, mTasks, mMessages, mPeople, mCalendar;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        fStore = FirebaseFirestore.getInstance();

        mBtnProfileEdit = findViewById(R.id.btnProfile);
        mProjects = findViewById(R.id.projects);
        mTasks = findViewById(R.id.tasks);
        mMessages = findViewById(R.id.messages);
        mPeople = findViewById(R.id.calendar);

        //redirects user to Profile activity upon clicking
        mBtnProfileEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Profile.class));
            }
        });

    }

    //sign-out method
    public void signOut(View view) {
        FirebaseAuth.getInstance().signOut();
        //redirects user back to sign in page
        startActivity(new Intent(getApplicationContext(), SignIn.class));
        finish();
    }
}