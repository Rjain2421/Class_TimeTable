package com.example.class_timetable.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewTimeTableActivity extends AppCompatActivity {
    private EditText enterUserId;
    private Button goButton,clearButton;
    public static final String fixAlarm="alarmId";

    private static final String TABLEID = "tableId";

    private String imageLink=null;
    private CircleImageView circleImageView;

    private SharedPreferences sharedPreferences;
    private String sharedPrefFile="com.example.android.classtimetable";

    String textId;

    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference collectionReference=db.collection("Users");
    private CollectionReference collectionReference1=db.collection("TableInfo");
    public static final String TAG="NewTimetable";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_time_table);
        Toolbar toolbar=findViewById(R.id.toolbarNewTimeTable);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView toolbarText=findViewById(R.id.toolbarText);
        if(ClassApi.getInstance().getUserId().equals(TableApi.getInstance().getUserId())){
            toolbarText.setText(ClassApi.getInstance().getUsername());
        }
        else{
            String x= String.valueOf(HtmlCompat.fromHtml("<sup>*</sup",HtmlCompat.FROM_HTML_MODE_LEGACY));
            toolbarText.setText(MessageFormat.format("{0}{1}", ClassApi.getInstance().getUsername(), x));
        }
        circleImageView=findViewById(R.id.action_profile_image);

        enterUserId=findViewById(R.id.newEnteredId);
        clearButton=findViewById(R.id.newClearButton);

        enterUserId.setText(TableApi.getInstance().getUserId());
        goButton=findViewById(R.id.newGoButton);

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterUserId.setText("");
            }
        });

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(enterUserId.getText().toString())){
                    Toast.makeText(getApplicationContext(),"UserId field cannot be empty",Toast.LENGTH_SHORT).show();
                }
                else{
                    textId=enterUserId.getText().toString().trim();
                    collectionReference.whereEqualTo("userId",textId)
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    if(queryDocumentSnapshots.isEmpty()){
                                        Toast.makeText(getApplicationContext(),"Enter a valid UserId",Toast.LENGTH_SHORT).show();
                                    }
                                    else{

                                        collectionReference1.whereEqualTo("userId", TableApi.getInstance().getUserId())
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots1) {
                                                        if(!queryDocumentSnapshots1.isEmpty()){
                                                            for (QueryDocumentSnapshot documentSnapshot:queryDocumentSnapshots1){
//                                                                Log.d(TAG, "onSuccess: "+documentSnapshot.getString("subject"));
                                                                SharedPreferences.Editor editor=sharedPreferences.edit();
                                                                editor.remove(documentSnapshot.getString(fixAlarm));
                                                                editor.apply();
                                                                if(documentSnapshot.getString(fixAlarm)!=null){
                                                                    AlarmHelper.cancelAlarm(getApplicationContext(),documentSnapshot.getString(fixAlarm));
                                                                }
                                                            }
                                                        }
                                                        else {

                                                        }

                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {

                                                    }
                                                });
                                        TableApi.getInstance().setUserId(textId);
                                        sharedPreferences=getSharedPreferences(sharedPrefFile,MODE_PRIVATE);
                                        SharedPreferences.Editor editor=sharedPreferences.edit();
                                        editor.putString(TABLEID,textId);
                                        editor.apply();
                                        Intent intent=new Intent(NewTimeTableActivity.this,TableActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                }
                            });
                }

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        collectionReference.whereEqualTo("userId",ClassApi.getInstance().getUserId())
                .addSnapshotListener(NewTimeTableActivity.this,new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error!=null){
                            return;
                        }
                        if(!value.isEmpty()){
                            List<DocumentSnapshot> documentSnapshot=value.getDocuments();

                            if(documentSnapshot.get(0).get("imageUrl")!=null){
                                imageLink=documentSnapshot.get(0).get("imageUrl").toString();
                                Glide.with(getApplicationContext())
                                        .asBitmap()
                                        .load(imageLink)
                                        .into(circleImageView);

                            }
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(NewTimeTableActivity.this,TableActivity.class));
        finish();
    }
}