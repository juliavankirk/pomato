package com.example.pomato.activities.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pomato.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    public static final String TAG = "TAG";
    EditText mName, mRole, mEmail, mPhone, mCompany, mBday;
    ImageView mAvatar;
    TextView mDone, mCancel;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    StorageReference storageReference;
    FirebaseUser user;

    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Intent data = getIntent();
        //receives data from Profile activity
        String fullName = data.getStringExtra("fullName");
        String role = data.getStringExtra("role");
        String email = data.getStringExtra("email");
        String phone = data.getStringExtra("phone");
        String company = data.getStringExtra("company");
        String bday = data.getStringExtra("bday");

        mAvatar = findViewById(R.id.eImage);
        mName = findViewById(R.id.fName);
        mRole = findViewById(R.id.fPosition);
        mEmail = findViewById(R.id.fEmail);
        mPhone = findViewById(R.id.fPhone);
        mCompany = findViewById(R.id.fCompany);
        mBday = findViewById(R.id.fBday);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        //sets values as what was passed on from Profile
        mName.setText(fullName);
        mRole.setText(role);
        mEmail.setText(email);
        mPhone.setText(phone);
        mCompany.setText(company);
        mBday.setText(bday);


        Log.d(TAG, "onCreate: " + fullName + " " + role + " " + email + " " + phone + " " + company + " " + "bday");


        StorageReference profileRef = storageReference.child("users/" + userId + "/profile.jpg");
        //loads onto screen existing Avatar in database
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(mAvatar);
            }
        });

        user = fAuth.getCurrentUser();
        userId = fAuth.getCurrentUser().getUid();



        DocumentReference df = fStore.collection("users").document(userId);
        df.addSnapshotListener(this, (documentSnapshot, e) -> {
            if( documentSnapshot.exists() ) {
                mName.setText(documentSnapshot.getString("fullName"));
                mEmail.setText(documentSnapshot.getString("email"));
                mRole.setText(documentSnapshot.getString("role"));
                mEmail.setText(documentSnapshot.getString("email"));
                mPhone.setText(documentSnapshot.getString("phone"));
                mCompany.setText(documentSnapshot.getString("company"));
                mBday.setText(documentSnapshot.getString("bday"));

            } else {
                Log.d("TAG", "onEvent: Document does not exist");
            }
        });

        mAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //opens gallery and returns selected image URI
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent, 1000);
            }
        });

        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( mName.getText().toString().isEmpty() ||
                     mRole.getText().toString().isEmpty() ||
                     mEmail.getText().toString().isEmpty() ||
                     mPhone.getText().toString().isEmpty() ||
                     mCompany.getText().toString().isEmpty() ||
                     mBday.getText().toString().isEmpty() ) {
                    Toast.makeText(EditProfile.this, "Please enter your changes.", Toast.LENGTH_SHORT).show();
                }

                String email = mEmail.getText().toString();
                user.updateEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DocumentReference df = fStore.collection("users").document(user.getUid());
                        Map<String, Object> edited = new HashMap<>();
                        //updates the hashmap of containing user values
                        edited.put("email", email);
                        edited.put("fullName", mName.getText().toString());
                        edited.put("role", mRole.getText().toString());
                        edited.put("email", mEmail.getText().toString());
                        edited.put("phone", mPhone.getText().toString());
                        edited.put("company", mCompany.getText().toString());
                        edited.put("bday", mBday.getText().toString());
                        //adds hashmap values onto database
                        df.update(edited);
                        Toast.makeText(EditProfile.this, "Email successfully changed!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( requestCode == 1000 ) {
            if ( resultCode == Activity.RESULT_OK) {
                Uri imgUri = data.getData();
                //mAvatar.setImageURI(imgUri);

                uploadImgToFirebase(imgUri);
            }
        }
    }

    private void uploadImgToFirebase(Uri imgUri) {
        //uploads image to Firebase
        StorageReference fileRef = storageReference.child("users/" + userId + "/profile.jpg");
        fileRef.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(mAvatar);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditProfile.this, "Upload Failed", Toast.LENGTH_SHORT).show();
            }
        })
        ;
    }

}