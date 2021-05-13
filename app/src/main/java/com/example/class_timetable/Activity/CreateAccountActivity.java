package com.example.class_timetable.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.class_timetable.R;
import com.example.class_timetable.Util.ClassApi;
import com.example.class_timetable.Util.TableApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateAccountActivity extends AppCompatActivity {
    public static final String TAG="createaccount";
    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
    private static final int GALLERY_CODE = 1234 ;
    private Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    private Matcher matcher;
    private TextInputLayout emailWrapper,passwordWrapper,usernameWrapper;
    private Uri imageUri;
    private EditText usernameText,emailText,passwordText;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;
    private Button createButton;
    private ImageView profilePictureImage;
    private ImageButton profilePictureEditButton;
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference collectionReference=db.collection("Users");
    private StorageReference storageReference;
    private String imageUrl;
    private String usernameTextLay,passwordTextLay,emailIdTextLay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        getSupportActionBar().setElevation(0);

        usernameWrapper=findViewById(R.id.createUsernameWrapper);
        emailWrapper=findViewById(R.id.createEmailWrapper);
        passwordWrapper=findViewById(R.id.createPasswordWrapper);

        usernameWrapper.setHint("Username");
        emailWrapper.setHint("Email Id");
        passwordWrapper.setHint("Password");

        profilePictureImage=findViewById(R.id.createProfilePhoto);
        profilePictureEditButton=findViewById(R.id.editProfilePicture);
        usernameText=findViewById(R.id.create_username);
        progressBar=findViewById(R.id.createProgressBar);
        emailText=findViewById(R.id.create_email);
        passwordText=findViewById(R.id.create_password);
        createButton=findViewById(R.id.create_account_button);
        createButton.setEnabled(true);
        firebaseAuth=FirebaseAuth.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference();
        authStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser=firebaseAuth.getCurrentUser();
                if(firebaseUser!=null){

                }
                else{

                }
            }
        };

        profilePictureEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,GALLERY_CODE);
            }
        });
        emailText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!validateEmail(s.toString())){
                    emailWrapper.setError("Not a valid email address");
                }
                else {
                    emailWrapper.setError(null);
                }
            }
        });
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
        usernameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!validateUsername(s.toString())){
                    usernameWrapper.setError("Not a valid Username!");
                }
                else {
                    usernameWrapper.setError(null);
                }
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(emailText.getText().toString().trim()) && !TextUtils.isEmpty(passwordText.getText().toString().trim()) && !TextUtils.isEmpty(usernameText.getText().toString().trim())){
                    usernameTextLay=usernameWrapper.getEditText().getText().toString();
                    emailIdTextLay=emailWrapper.getEditText().getText().toString();
                    passwordTextLay=passwordWrapper.getEditText().getText().toString();
                    if(!validateEmail(emailIdTextLay) || !validatePassword(passwordTextLay) || !validateUsername(usernameTextLay)){
                        progressBar.setVisibility(View.INVISIBLE);
                        createButton.setEnabled(true);
                    }
                    else {
                        createButton.setEnabled(false);
                        progressBar.setVisibility(View.VISIBLE);
                        String email = emailText.getText().toString().trim();
                        String password = passwordText.getText().toString().trim();
                        final String username = usernameText.getText().toString().trim();
                        imageUrl=null;
                        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(CreateAccountActivity.this, "Successfully Registered", Toast.LENGTH_SHORT).show();
                                    firebaseUser = firebaseAuth.getCurrentUser();
                                    final String currentuserId;
                                    imageUrl=null;
                                    if (firebaseUser != null) {
                                        currentuserId = firebaseUser.getUid();
                                        if (imageUri != null) {

                                            final StorageReference filepath = storageReference
                                                    .child("profile_picture")
                                                    .child("profile_" + currentuserId);
                                            filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                            imageUrl = uri.toString();

                                                            Map<String, String> userObj = new HashMap<>();
                                                            userObj.put("userId", currentuserId);
                                                            userObj.put("username", username);
                                                            userObj.put("imageUrl", imageUrl);

                                                            collectionReference.add(userObj)
                                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                        @Override
                                                                        public void onSuccess(DocumentReference documentReference) {
                                                                            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                    if (task.getResult().exists()) {
                                                                                        String name = task.getResult().getString("username");
                                                                                        ClassApi classApi = ClassApi.getInstance();
                                                                                        TableApi tableApi = TableApi.getInstance();
                                                                                        tableApi.setUserId(currentuserId);
                                                                                        classApi.setUserId(currentuserId);
                                                                                        classApi.setUsername(name);
                                                                                        Intent intent = new Intent(CreateAccountActivity.this, TableActivity.class);
                                                                                        intent.putExtra("username", name);
                                                                                        intent.putExtra("userId", currentuserId);
                                                                                        intent.putExtra("imageUrl", imageUrl);
                                                                                        progressBar.setVisibility(View.INVISIBLE);
                                                                                        startActivity(intent);
                                                                                        finish();
                                                                                    }
                                                                                }
                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    progressBar.setVisibility(View.INVISIBLE);
                                                                                }
                                                                            });
                                                                        }
                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            progressBar.setVisibility(View.INVISIBLE);
                                                                        }
                                                                    });
                                                        }
                                                    });
                                                }
                                            })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {

                                                        }
                                                    });
                                        }
                                        else{
                                            Map<String, String> userObj = new HashMap<>();
                                            userObj.put("userId", currentuserId);
                                            userObj.put("username", username);
                                            userObj.put("imageUrl", null);
                                            collectionReference.add(userObj)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                    if (task.getResult().exists()) {
                                                                        String name = task.getResult().getString("username");
                                                                        ClassApi classApi = ClassApi.getInstance();
                                                                        TableApi tableApi = TableApi.getInstance();
                                                                        tableApi.setUserId(currentuserId);
                                                                        classApi.setUserId(currentuserId);
                                                                        classApi.setUsername(name);
                                                                        Intent intent = new Intent(CreateAccountActivity.this, TableActivity.class);
                                                                        intent.putExtra("username", name);
                                                                        intent.putExtra("userId", currentuserId);
                                                                        intent.putExtra("imageUrl", imageUrl);
                                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                        progressBar.setVisibility(View.INVISIBLE);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    progressBar.setVisibility(View.INVISIBLE);
                                                                }
                                                            });
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
                                    else {
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }

                                } else {
                                    createButton.setEnabled(true);
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(CreateAccountActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
                else{
                    createButton.setEnabled(true);
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(CreateAccountActivity.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_CODE && resultCode==RESULT_OK){
            if(data!=null){
                imageUri=data.getData();
                profilePictureImage.setImageURI(imageUri);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
//        imageUrl=null;
        createButton.setEnabled(true);
        firebaseUser=firebaseAuth.getCurrentUser();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }
    public boolean validateEmail(String email) {
        matcher = pattern.matcher(email);
        return matcher.matches();
    }
    public boolean validatePassword(String password) {
        return password.length() > 5;
    }
    public boolean validateUsername(String name)
    {
        String regex = "^[aA-zZ]\\w{5,29}$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(name);
        return m.matches();
    }
}