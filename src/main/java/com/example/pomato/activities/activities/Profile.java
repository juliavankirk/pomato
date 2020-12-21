package com.example.pomato.activities.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pomato.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class Profile extends AppCompatActivity {
    TextView mName, mEmail, mPhone, mCompany, mRole, mBday;
    ImageView mAvatar;
    Button mBtnEdit;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    StorageReference storageReference;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAvatar = findViewById(R.id.vImage);
        mName = findViewById(R.id.vName);
        mRole = findViewById(R.id.vPosition);
        mEmail = findViewById(R.id.vEmail);
        mPhone = findViewById(R.id.vPhone);
        mCompany = findViewById(R.id.vCompany);
        mBday = findViewById(R.id.vBday);
        mBtnEdit = findViewById(R.id.btnEditProfile);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        userId = fAuth.getCurrentUser().getUid();

        StorageReference profileRef = storageReference.child("users/" + userId + "/profile.jpg");
        //loads onto screen existing Avatar in database
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(mAvatar);
            }
        });

        userId = fAuth.getCurrentUser().getUid();

        //fetches data from users collection if Firebase
        DocumentReference df = fStore.collection("users").document(userId);
        df.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                //sets variables to whatever value was stored into database
                mName.setText(value.getString("fullName"));
                mRole.setText(value.getString("role"));
                mEmail.setText(value.getString("email"));
                mPhone.setText(value.getString("phone"));
                mCompany.setText(value.getString("company"));
                mBday.setText(value.getString("bday"));
            }
        });

        //user is redirected to Edit Profile activity upon click
        mBtnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), EditProfile.class);
                //stored user data is passed onto next activity
                i.putExtra("fullName", mName.getText().toString());
                i.putExtra("role", mRole.getText().toString());
                i.putExtra("email", mEmail.getText().toString());
                i.putExtra("phone", mPhone.getText().toString());
                i.putExtra("company", mCompany.getText().toString());
                i.putExtra("bday", mBday.getText().toString());
                startActivity(i);
            }
        });

    }
}