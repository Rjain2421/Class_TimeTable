package com.example.class_timetable.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.class_timetable.R;
import com.example.class_timetable.Util.ClassApi;
import com.example.class_timetable.Util.TableApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity{
    private ProgressBar progressBar;
    public static final String TAG="loginactivity";
    private Button loginButton,createAccButton;
    private EditText loginEmail,loginPassword;
    private TextView forgotPasswordText;
    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
    private Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    private Matcher matcher;

    private TextInputLayout emailWrapper,passwordWrapper;
    private String emailIdText,passwordText;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference collectionReference=db.collection("Users");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setElevation(0);
        progressBar=findViewById(R.id.loginProgressBar);
        forgotPasswordText=findViewById(R.id.login_forgotPassword);
        createAccButton=findViewById(R.id.create_button);
        createAccButton.setEnabled(true);
        loginButton=findViewById(R.id.login_button);
        loginButton.setEnabled(true);
        loginEmail=findViewById(R.id.login_email);

        emailWrapper=findViewById(R.id.loginEmailWrapper);
        passwordWrapper=findViewById(R.id.loginPasswordWrapper);

        emailWrapper.setHint("Email Id");
        passwordWrapper.setHint("Password");

        firebaseAuth=FirebaseAuth.getInstance();
        loginPassword=findViewById(R.id.login_password);
        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ConfirmEmailActivity.class));
            }
        });

        loginEmail.addTextChangedListener(new TextWatcher() {
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
        loginPassword.addTextChangedListener(new TextWatcher() {
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


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                if(!TextUtils.isEmpty(loginEmail.getText().toString().trim()) && !TextUtils.isEmpty(loginPassword.getText().toString().trim())){
                    emailIdText=emailWrapper.getEditText().getText().toString();
                    passwordText=passwordWrapper.getEditText().getText().toString();
                    if(!validateEmail(emailIdText) || !validatePassword(passwordText)){
                        progressBar.setVisibility(View.INVISIBLE);
                        loginButton.setEnabled(true);
                    }
                    else{
                        loginButton.setEnabled(false);
                        progressBar.setVisibility(View.VISIBLE);
                        String email=loginEmail.getText().toString().trim();
                        String password=loginPassword.getText().toString().trim();
                        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                FirebaseUser user=firebaseAuth.getCurrentUser();
                                String currentuserId= null;
                                if (user != null) {
                                    currentuserId = user.getUid();
                                    collectionReference.whereEqualTo("userId",currentuserId)
                                            .addSnapshotListener(LoginActivity.this,new EventListener<QuerySnapshot>() {
                                                @Override
                                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                    if(error!=null){

                                                    }
                                                    if(!value.isEmpty()){
                                                        for (QueryDocumentSnapshot snapshot:value){
                                                            ClassApi classApi=ClassApi.getInstance();
                                                            TableApi tableApi=TableApi.getInstance();
                                                            tableApi.setUserId(snapshot.getString("userId"));
                                                            classApi.setUsername(snapshot.getString("username"));
                                                            classApi.setUserId(snapshot.getString("userId"));
                                                            Intent intent=new Intent(LoginActivity.this,TableActivity.class);
                                                            intent.putExtra("imageUrl",snapshot.getString("imageUrl"));
                                                            progressBar.setVisibility(View.INVISIBLE);
                                                            startActivity(intent);
                                                            finish();
                                                        }

                                                    }
                                                    else{

                                                    }
                                                }
                                            });
                                }
                                else {
                                    loginButton.setEnabled(true);
                                    Toast.makeText(LoginActivity.this, "Credentials don't match", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.INVISIBLE);
                                }

                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        loginButton.setEnabled(true);
                                        Toast.makeText(LoginActivity.this, "Credentials don't match", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                });
                    }

                }
                else{
                    loginButton.setEnabled(true);
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(LoginActivity.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
        createAccButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccButton.setEnabled(false);
                startActivity(new Intent(LoginActivity.this,CreateAccountActivity.class));
            }
        });
    }

    public boolean validateEmail(String email) {
        matcher = pattern.matcher(email);
        return matcher.matches();
    }
    public boolean validatePassword(String password) {
        return password.length() > 5;
    }

    @Override
    protected void onStart() {
        super.onStart();
        loginButton.setEnabled(true);
        createAccButton.setEnabled(true);
    }
}