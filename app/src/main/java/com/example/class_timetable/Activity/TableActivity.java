package com.example.class_timetable.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.class_timetable.R;
import com.example.class_timetable.UI.AlarmHelper;
import com.example.class_timetable.Util.ClassApi;
import com.example.class_timetable.Util.ImportApi;
import com.example.class_timetable.Util.TableApi;
import com.example.class_timetable.model.ClassDetail;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class TableActivity extends AppCompatActivity {
    public static final String TAG="TableActivity";
    private static final String PRIMARY_CHANNEL_ID =
            "primary_notification_channel";
    public static final String fixChannelName="helloFirebase";
    private static final String PROFILEIMAGE ="profileImage" ;
    private static final String IMAGEURL = "imageUrl";
    private String currentUserId;
    private int flag=1;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;
    private List<String> alarmList;
    private static final String TABLEID = "tableId";
    private static final String ALARMLIST = "alarmList";
    private String imageLink=null;
    private CircleImageView circleImageView;

    private SharedPreferences sharedPreferences;
    private String sharedPrefFile="com.example.android.classtimetable";

    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference collectionReference=db.collection("TableInfo");
    private CollectionReference imageCollection=FirebaseFirestore.getInstance().collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);
        Toolbar toolbar=findViewById(R.id.toolbarTable);
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
        sharedPreferences=getSharedPreferences(sharedPrefFile,MODE_PRIVATE);
        circleImageView=findViewById(R.id.action_profile_image);
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(TableActivity.this,UserProfileActivity.class);
                intent.putExtra(IMAGEURL,imageLink);
                startActivity(intent);
            }
        });
        firebaseAuth=FirebaseAuth.getInstance();
        currentUserId=TableApi.getInstance().getUserId();
        BottomNavigationView bottomNavigationView=findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new DailyFragment()).commit();
        authStateListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser=firebaseAuth.getCurrentUser();
                if(firebaseUser!=null){

                }
                else{

                }
            }
        };
    }

    private void setAllAlarms() {
        alarmList=new ArrayList<>();
        if(sharedPreferences.getString(ALARMLIST,null)!=null){
            Gson gson=new Gson();
            String json=sharedPreferences.getString(ALARMLIST,null);
            Type type=new TypeToken<ArrayList<String>>(){}.getType();
            alarmList=gson.fromJson(json,type);
            for (String s:alarmList){
                AlarmHelper.cancelAlarm(getApplicationContext(),s);
            }
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.clear();
            editor.apply();
        }
        alarmList.clear();

        collectionReference.whereEqualTo("userId",TableApi.getInstance().getUserId())
                .addSnapshotListener(TableActivity.this,new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error!=null){
                            return;
                        }
                        if(!value.isEmpty()){
                            List<DocumentSnapshot> documentSnapshot=value.getDocuments();
                            for(DocumentSnapshot doc:documentSnapshot){
                                ClassDetail classDetail=doc.toObject(ClassDetail.class);
                                alarmList.add(classDetail.getAlarmId());
                                if(!sharedPreferences.contains(classDetail.getAlarmId())){
                                    AlarmHelper.setAlarm(getApplicationContext(),classDetail.getHour(),classDetail.getMinute(),classDetail.getDay(), Integer.parseInt(classDetail.getAlarmId()),classDetail.getTeacher(),classDetail.getSubject(),classDetail.getClassLink());
                                    SharedPreferences.Editor editor=sharedPreferences.edit();
                                    editor.putBoolean(classDetail.getAlarmId(),true);
                                    editor.apply();
                                }
                            }
                            SharedPreferences.Editor editor=sharedPreferences.edit();
                            Gson gson=new Gson();
                            String json=gson.toJson(alarmList);
                            editor.putString(ALARMLIST,json);
                            editor.apply();
                        }
                    }
                });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sign,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_signout:
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

                                }

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                TableApi tableApi=TableApi.getInstance();
                tableApi.setUserId(ClassApi.getInstance().getUserId());
                clearSharedPref();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(TableActivity.this,MainActivity.class));
                finish();
                break;
            case  R.id.accessTable:
                startActivity(new Intent(TableActivity.this, NewTimeTableActivity.class));
                finish();
                break;
            case R.id.importTable:
                if(TableApi.getInstance().getUserId()!=ClassApi.getInstance().getUserId()){
                    Toast.makeText(TableActivity.this,"You cannot edit another user's table",Toast.LENGTH_LONG).show();
                }
                else{
                    startActivity(new Intent(TableActivity.this,ImportDataActivity.class));
                }
                break;

            case R.id.shareButton:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, MessageFormat.format("This is my table ID: {0}", currentUserId));
                sendIntent.setType("text/plain");
                Intent shareIntent = Intent.createChooser(sendIntent, null);
                startActivity(shareIntent);
                break;
            case R.id.resetMenuButton:
                clearAlarms();
                sharedPreferences=getSharedPreferences(sharedPrefFile,MODE_PRIVATE);
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putString(TABLEID,ClassApi.getInstance().getUserId());
                editor.apply();
                TableApi.getInstance().setUserId(ClassApi.getInstance().getUserId());
                startActivity(new Intent(TableActivity.this,TableActivity.class));
                finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void clearAlarms() {
        collectionReference.whereEqualTo("userId",TableApi.getInstance().getUserId())
                .addSnapshotListener(TableActivity.this,new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error!=null){
                            return;
                        }
                        if(!value.isEmpty()){
                            List<DocumentSnapshot> documentSnapshot=value.getDocuments();
                            for(DocumentSnapshot doc:documentSnapshot){
                                ClassDetail classDetail=doc.toObject(ClassDetail.class);
                                AlarmHelper.cancelAlarm(getApplicationContext(),classDetail.getAlarmId());
//                                AlarmHelper.setAlarm(getApplicationContext(),classDetail.getHour(),classDetail.getMinute(),classDetail.getDay(), Integer.parseInt(classDetail.getAlarmId()),classDetail.getTeacher(),classDetail.getSubject(),classDetail.getClassLink());
                                SharedPreferences.Editor editor=sharedPreferences.edit();
                                editor.remove(classDetail.getAlarmId());
                                editor.apply();
                            }
                        }
                    }
                });
    }

    private void clearSharedPref() {
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener=new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment;
            switch (item.getItemId()){
                case R.id.today:
                    if(flag==0){
                        selectedFragment=new DailyFragment();
                        getSupportFragmentManager().beginTransaction()
                                .setCustomAnimations(R.anim.slide_in_left,R.anim.slide_out_left)
                                .add(R.id.fragment_container,selectedFragment).disallowAddToBackStack()
                                .replace(R.id.fragment_container,selectedFragment).commit();
                        flag=1;
                    }
                    break;
                case R.id.weekly:

                    if(flag==1){
                        selectedFragment=new WeeklyFragment();
                        getSupportFragmentManager().beginTransaction()
                                .setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_right)
                                .disallowAddToBackStack()
                                .add(R.id.fragment_container,selectedFragment)
                                .replace(R.id.fragment_container,selectedFragment).commit();
                        flag=0;
                    }
                    break;
            }

            return true;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        setAllAlarms();
        firebaseUser=firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
        imageCollection.whereEqualTo("userId",ClassApi.getInstance().getUserId())
                .addSnapshotListener(TableActivity.this,new EventListener<QuerySnapshot>() {
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
                            else {
                                circleImageView.setImageResource(R.drawable.ic_baseline_person_24);
                            }
                        }
                        else{
                            circleImageView.setImageResource(R.drawable.ic_baseline_person_24);
                        }
                    }
                });

        if(firebaseUser==null){
            Toast.makeText(TableActivity.this,"Please renter your credentials",Toast.LENGTH_LONG).show();
            startActivity(new Intent(TableActivity.this,MainActivity.class));
            finish();
        }
    }



    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseAuth!=null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}