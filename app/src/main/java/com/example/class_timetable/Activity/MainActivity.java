package com.example.class_timetable.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.class_timetable.R;
import com.example.class_timetable.Util.ClassApi;
import com.example.class_timetable.Util.TableApi;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {
    private static final String TABLEID = "tableId";
    private Boolean flag=false;

    private Button jumpButton;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference collectionReference=db.collection("Users");

    private SharedPreferences sharedPreferences;
    private String sharedPrefFile="com.example.android.classtimetable";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        flag=false;
        sharedPreferences=getSharedPreferences(sharedPrefFile,MODE_PRIVATE);
        if(sharedPreferences.contains(TABLEID)){
            flag=true;
        }
        jumpButton=findViewById(R.id.jumpButton);
        jumpButton.setEnabled(true);
        jumpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpButton.setEnabled(false);
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                finish();
            }
        });
        firebaseAuth=FirebaseAuth.getInstance();
        authStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser=firebaseAuth.getCurrentUser();
                if(firebaseUser!=null){
                    String currentuserId=firebaseUser.getUid();
                    collectionReference.whereEqualTo("userId",currentuserId)
                            .addSnapshotListener(MainActivity.this,new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                    if(error!=null){
                                        return;
                                    }
                                    if(!value.isEmpty()){
                                        for(QueryDocumentSnapshot snapshot:value){
                                            ClassApi classApi=ClassApi.getInstance();
                                            classApi.setUserId(snapshot.getString("userId"));
                                            classApi.setUsername(snapshot.getString("username"));
                                            TableApi tableApi=TableApi.getInstance();
                                            if(flag){
                                                tableApi.setUserId(sharedPreferences.getString(TABLEID,"hello"));
                                            }
                                            else {
                                                tableApi.setUserId(snapshot.getString("userId"));
                                                SharedPreferences.Editor editor=sharedPreferences.edit();
                                                editor.putString(TABLEID,TableApi.getInstance().getUserId());
                                                editor.apply();
                                            }
                                            startActivity(new Intent(MainActivity.this,TableActivity.class));
                                            finish();
                                        }
                                    }
                                }
                            });
                }
            }
        };
    }
    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser=firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(firebaseAuth!=null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}