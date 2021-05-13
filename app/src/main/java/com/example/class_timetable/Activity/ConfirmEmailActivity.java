package com.example.class_timetable.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.class_timetable.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfirmEmailActivity extends AppCompatActivity {
    private TextInputLayout emailWrapper;
    private EditText emailText;
    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
    private Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    private Matcher matcher;
    private Button resetButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_email);
        emailWrapper=findViewById(R.id.confirmEmailWrapper);
        emailText=findViewById(R.id.confirmEmailText);
        resetButton=findViewById(R.id.confirmResetButton);
        emailWrapper.setHint("Email");

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailgetText=emailText.getText().toString().trim();
                if(!validateEmail(emailgetText) || !TextUtils.isEmpty(emailgetText)){
                    FirebaseAuth.getInstance().sendPasswordResetEmail(emailgetText)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(ConfirmEmailActivity.this,"Check your email!!",Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ConfirmEmailActivity.this,"Error!!",Toast.LENGTH_LONG).show();
                                }
                            });
                }
                else {
                    Toast.makeText(ConfirmEmailActivity.this,"Empty fields not allowed!!",Toast.LENGTH_LONG).show();
                }
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
                    emailWrapper.setError("Enter valid Email");
                }
                else {
                    emailWrapper.setError(null);
                }
            }
        });
    }
    public boolean validateEmail(String email) {
        matcher = pattern.matcher(email);
        return matcher.matches();
    }
}