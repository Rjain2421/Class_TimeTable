package com.example.class_timetable.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.class_timetable.R;
import com.example.class_timetable.UI.AlarmHelper;
import com.example.class_timetable.Util.ClassApi;
import com.example.class_timetable.Util.TableApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class DeleteAccountActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;
    private static final String IMAGEURL = "imageUrl";
    public static final String TAG="deleteaccountactivity";

    private ImageView imageView;
    private TextInputLayout passwordWrapper;
    private EditText passwordText;
    private TextView emailText;
    private Button deleteButton;

    private SharedPreferences sharedPreferences;
    private String sharedPrefFile="com.example.android.classtimetable";

    private String imageLink=null;
    private CircleImageView circleImageView;
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private ProgressBar progressBar;
    private CollectionReference collectionReference=db.collection("TableInfo");
    private StorageReference storageReference,storagedelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);
        Toolbar toolbar=findViewById(R.id.toolbarDelete);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        storageReference= FirebaseStorage.getInstance().getReference();
        storagedelete=storageReference.child("profile_picture");

        imageLink=getIntent().getStringExtra(IMAGEURL);
        TextView toolbarText=findViewById(R.id.toolbarText);
        toolbarText.setText(ClassApi.getInstance().getUsername());
        sharedPreferences=getSharedPreferences(sharedPrefFile,MODE_PRIVATE);
        progressBar=findViewById(R.id.deleteProgressBar);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();
        circleImageView=findViewById(R.id.action_profile_image);
        imageView=findViewById(R.id.deleteProfilePhoto);
        passwordWrapper=findViewById(R.id.deletePasswordWrapper);
        emailText=findViewById(R.id.delete_email);
        emailText.setText(firebaseUser.getEmail().toString().trim());
        passwordText=findViewById(R.id.delete_password);
        deleteButton=findViewById(R.id.delete_button);


        passwordWrapper.setHint("Password");
        if(imageLink!=null){
            Glide.with(getApplicationContext())
                    .asBitmap()
                    .load(imageLink)
                    .into(circleImageView);
            Glide.with(getApplicationContext())
                    .load(imageLink)
                    .into(imageView);
        }

        passwordText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!validatePassword(s.toString())){
                    passwordWrapper.setError("Not a valid password!");
                }
                else {
                    passwordWrapper.setError(null);
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(passwordText.getText().toString().trim())) {
                    String passwordText = passwordWrapper.getEditText().getText().toString();
                    if (!validatePassword(passwordText)) {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                    else {
                        progressBar.setVisibility(View.VISIBLE);
                        deleteButton.setEnabled(false);
                        firebaseAuth.signInWithEmailAndPassword(firebaseUser.getEmail().toString(),passwordText)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        String deleteImage="profile_"+ClassApi.getInstance().getUserId();
                                        storagedelete.child(deleteImage).delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {

                                                    }
                                                });

                                        collectionReference.whereEqualTo("userId", TableApi.getInstance().getUserId())
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                        if(!queryDocumentSnapshots.isEmpty()){
                                                            for (QueryDocumentSnapshot snapshot:queryDocumentSnapshots){
                                                                AlarmHelper.cancelAlarm(getApplicationContext(),snapshot.getString(PostDayActivity.fixAlarm));
                                                            }
                                                        }
                                                        else {
                                                            progressBar.setVisibility(View.INVISIBLE);

                                                        }

                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        progressBar.setVisibility(View.INVISIBLE);

                                                    }
                                                });
                                        db.collection("TableInfo").whereEqualTo("userId",ClassApi.getInstance().getUserId())
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        if(task.isSuccessful()){
                                                            QuerySnapshot documentSnapshot=task.getResult();
                                                            if(!documentSnapshot.isEmpty()){
                                                                for (QueryDocumentSnapshot snapshot:documentSnapshot){
                                                                    snapshot.getReference().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                        }
                                                                    })
                                                                        .addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                progressBar.setVisibility(View.INVISIBLE);

                                                                            }
                                                                        });
                                                                }
                                                            }

                                                        }
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {

                                                    }
                                                });

                                        db.collection("Users").whereEqualTo("userId",ClassApi.getInstance().getUserId())
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        if(task.isSuccessful()){
                                                            QuerySnapshot documentSnapshot=task.getResult();
                                                            if(!documentSnapshot.isEmpty()){
                                                                for (QueryDocumentSnapshot snapshot:documentSnapshot){
                                                                    snapshot.getReference().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                        }
                                                                    })
                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    progressBar.setVisibility(View.INVISIBLE);

                                                                                }
                                                                            });
                                                                }
                                                            }

                                                        }
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        progressBar.setVisibility(View.INVISIBLE);
                                                    }
                                                });
                                        clearSharedPref();
                                        FirebaseAuth.getInstance().signOut();
                                        firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(DeleteAccountActivity.this,"Account Successfully Deleted",Toast.LENGTH_LONG).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressBar.setVisibility(View.INVISIBLE);
                                                Toast.makeText(DeleteAccountActivity.this,"Error!!!",Toast.LENGTH_LONG).show();

                                            }
                                        });
                                        Intent intent=new Intent(DeleteAccountActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        progressBar.setVisibility(View.INVISIBLE);
                                        Toast.makeText(DeleteAccountActivity.this,"Incorrect Password",Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                }
            }
        });
    }
    
    public boolean validatePassword(String password) {
        return password.length() > 5;
    }
    
    private void clearSharedPref() {
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
        deleteButton.setEnabled(true);
    }
}