package com.example.class_timetable.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.class_timetable.R;
import com.example.class_timetable.UI.AlarmHelper;
import com.example.class_timetable.UI.ClassAdapter;
import com.example.class_timetable.Util.ClassApi;
import com.example.class_timetable.Util.ImportApi;
import com.example.class_timetable.Util.TableApi;
import com.example.class_timetable.model.ClassDetail;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostDayActivity extends AppCompatActivity {


    public static final String fixSubject="subject";
    public static final String fixTeacher="teacher";
    public static final String fixHour="hour";
    public static final String fixMinute="minute";
    public static final String fixAlarm="alarmId";
    public static final int fixToken=1111;

    private static final String TABLEID = "tableId";
    private static final String IMAGEURL = "imageUrl";
    private TextInputLayout subjectLayout,teacherLayout,linkLayout;
    private SharedPreferences sharedPreferences;
    private String sharedPrefFile="com.example.android.classtimetable";

    private static final String PRIMARY_CHANNEL_ID =
            "primary_notification_channel";
    private static final String TAG ="notifyPost" ;


    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private TextView classTime;
    public  static TextView noText;

    private ClassAdapter classAdapter;

    private EditText classLink;
    private EditText teacherName,subjectName;
    private Button setTimeButton,saveButton;
    private String timetext;
    private String[] weekdays;
    public String currentUserId,currentUserName;
    private int day,hourSet,minuteSet;

    private String imageLink=null;
    private CircleImageView circleImageView;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;

    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference collectionReference=db.collection("TableInfo");
    private CollectionReference imageCollection=FirebaseFirestore.getInstance().collection("Users");


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_day);
        Toolbar toolbar=findViewById(R.id.toolbarPost);
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
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(PostDayActivity.this,UserProfileActivity.class);
                intent.putExtra(IMAGEURL,imageLink);
                startActivity(intent);
            }
        });
        sharedPreferences=getSharedPreferences(sharedPrefFile,MODE_PRIVATE);

        FloatingActionButton fab = findViewById(R.id.fab);
        noText=findViewById(R.id.noText);

        weekdays=getResources().getStringArray(R.array.weekdays);
        firebaseAuth=FirebaseAuth.getInstance();
        day=getIntent().getIntExtra("day",0);
        day=day%10;
        noText.setText(MessageFormat.format("No classes on {0}", weekdays[day]));

        collectionReference.whereEqualTo("userId", TableApi.getInstance().getUserId())
                .whereEqualTo("day",day)
                .addSnapshotListener(PostDayActivity.this,new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error!=null){
                            return;
                        }
                        if(!value.isEmpty()){
                            noText.setVisibility(View.INVISIBLE);
                        }
                        else{
                            noText.setVisibility(View.VISIBLE);
                        }
                    }
                });

        setUpRecyclerView();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TableApi.getInstance().getUserId().equals(ClassApi.getInstance().getUserId())){
                    Toast.makeText(PostDayActivity.this,"You cannot add to another Timetable, If you still want to add reset the TimeTable",Toast.LENGTH_LONG).show();
                    return;
                }
                createPopupDialog();
            }
        });
        if(ClassApi.getInstance()!=null){
            if(TableApi.getInstance()!=null){
                currentUserId=TableApi.getInstance().getUserId();
            }
            else{
                currentUserId=ClassApi.getInstance().getUserId();
            }

            currentUserName=ClassApi.getInstance().getUsername();
        }
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

                                            AlarmHelper.cancelAlarm(getApplicationContext(),snapshot.getString(fixAlarm));
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

                startActivity(new Intent(PostDayActivity.this,MainActivity.class));
                    finish();
                break;
            case R.id.importTable:
                if(TableApi.getInstance().getUserId()!=ClassApi.getInstance().getUserId()){
                    Toast.makeText(PostDayActivity.this,"You cannot edit another user's table",Toast.LENGTH_LONG).show();
                }
                else {
                    startActivity(new Intent(PostDayActivity.this,ImportDataActivity.class));
                }

                break;
            case  R.id.accessTable:
                clearAlarms();
                startActivity(new Intent(PostDayActivity.this, NewTimeTableActivity.class));
                //finish();
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
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putString(TABLEID,ClassApi.getInstance().getUserId());
                editor.apply();
                TableApi.getInstance().setUserId(ClassApi.getInstance().getUserId());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpRecyclerView() {
        Query query=collectionReference.whereEqualTo("day",day).whereEqualTo("userId",TableApi.getInstance().getUserId());
        FirestoreRecyclerOptions<ClassDetail> options= new  FirestoreRecyclerOptions.Builder<ClassDetail>()
                .setQuery(query,ClassDetail.class)
                .build();
        classAdapter=new ClassAdapter(options);
        RecyclerView recyclerView=findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(classAdapter);

    }
    private void clearAlarms() {
        collectionReference.whereEqualTo("userId",TableApi.getInstance().getUserId())
                .addSnapshotListener(PostDayActivity.this,new EventListener<QuerySnapshot>() {
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
                                SharedPreferences.Editor editor=sharedPreferences.edit();
                                editor.remove(classDetail.getAlarmId());
                                editor.apply();
                            }
                        }
                    }
                });
    }

    private void createPopupDialog() {
        builder=new AlertDialog.Builder(this);
        View view=getLayoutInflater().inflate(R.layout.pop_up_row,null);
        subjectLayout=view.findViewById(R.id.popSubjectWrapper);
        teacherLayout=view.findViewById(R.id.popTeacherWrapper);
        linkLayout=view.findViewById(R.id.popLinkWrapper);

        classLink=view.findViewById(R.id.pop_classLink);
        classTime=view.findViewById(R.id.pop_classTime);
        teacherName=view.findViewById(R.id.pop_teacherName);
        subjectName=view.findViewById(R.id.pop_subjectName);
        setTimeButton=view.findViewById(R.id.pop_setTimeButton);
        saveButton=view.findViewById(R.id.pop_saveButton);
        saveButton.setEnabled(true);
        setTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c=Calendar.getInstance();

                int hour=c.get(Calendar.HOUR_OF_DAY);
                int minute=c.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog=new TimePickerDialog(PostDayActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        timetext=hourOfDay+":"+minute;
                        hourSet=hourOfDay;
                        minuteSet=minute;

                        classTime.setText(timetext);
                    }
                },hour,minute,false);
                timePickerDialog.show();

            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!classLink.getText().toString().isEmpty()
                        && !classTime.getText().toString().isEmpty()
                        && !teacherName.getText().toString().isEmpty()
                        && !subjectName.getText().toString().isEmpty()
                        && URLUtil.isValidUrl(classLink.getText().toString().trim())){

                    SaveItem(v);
                }
                else{
                    Snackbar.make(v,"Check for empty Fields or Invalid Url",Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        builder.setView(view);
        dialog=builder.create();
        dialog.getWindow().getAttributes().windowAnimations=R.style.DialogCustomAnimation;
        dialog.show();

    }


    private void SaveItem(final View view){
        saveButton.setEnabled(false);
        int alarmId = new Random().nextInt(Integer.MAX_VALUE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean(String.valueOf(alarmId),true);
        String newLink=classLink.getText().toString().trim();
        String newTeacher=teacherName.getText().toString().trim();
        String newSubject=subjectName.getText().toString().trim();
        String newTime=classTime.getText().toString().trim();

        final ClassDetail classDetail=new ClassDetail(newSubject,newTeacher,newLink,newTime,ClassApi.getInstance().getUsername(),ClassApi.getInstance().getUserId(),Integer.toString(alarmId),day,hourSet,minuteSet);

        AlarmHelper.setAlarm(getApplicationContext(),hourSet,minuteSet,day,alarmId,classDetail.getTeacher(),classDetail.getSubject(),classDetail.getClassLink());

        collectionReference.add(classDetail).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        Intent intent=new Intent(PostDayActivity.this,PostDayActivity.class);
                        intent.putExtra("day",day);
                        startActivity(intent);
                        overridePendingTransition(0,0);
                        finish();
                    }
                },50);

            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    private void clearSharedPref() {
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
        classAdapter.startListening();
        firebaseUser=firebaseAuth.getCurrentUser();
        if(firebaseUser==null){
            Toast.makeText(PostDayActivity.this,"Please renter your credentials",Toast.LENGTH_LONG).show();
            startActivity(new Intent(PostDayActivity.this,MainActivity.class));
        }
        firebaseAuth.addAuthStateListener(authStateListener);
        imageCollection.whereEqualTo("userId",ClassApi.getInstance().getUserId())
                .addSnapshotListener(PostDayActivity.this,new EventListener<QuerySnapshot>() {
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
                            else{
                                circleImageView.setImageResource(R.drawable.ic_baseline_person_24);
                            }
                        }
                        else{
                            circleImageView.setImageResource(R.drawable.ic_baseline_person_24);
                        }
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        classAdapter.stopListening();
        if(firebaseAuth!=null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}