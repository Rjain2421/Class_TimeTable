package com.example.class_timetable.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.class_timetable.R;
import com.example.class_timetable.UI.ClassAdapter;
import com.example.class_timetable.UI.ImportAdapter;
import com.example.class_timetable.Util.ClassApi;
import com.example.class_timetable.Util.ImportApi;
import com.example.class_timetable.Util.TableApi;
import com.example.class_timetable.model.ClassDetail;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ImportDataActivity extends AppCompatActivity {
    public static final String TAG="importactivity";
    private TextView noText;
    private RecyclerView recyclerView;
    private CircleImageView circleImageView;
    private String imageLink;
    private Button goButton;
    private EditText enteredId;
    private ImportAdapter importAdapter;
    private ClassAdapter classAdapter;
    private FirebaseAuth firebaseAuth;
    private int x;
    private ImageButton importButton;

    private CollectionReference imageCollection= FirebaseFirestore.getInstance().collection("Users");
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference collectionReference=db.collection("Users");
    private CollectionReference collectionReference1=db.collection("TableInfo");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_data);
        x=0;
        Toolbar toolbar=findViewById(R.id.toolbarImport);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView toolbarText=findViewById(R.id.toolbarText);
        toolbarText.setText(ClassApi.getInstance().getUsername());
        firebaseAuth=FirebaseAuth.getInstance();
        imageLink=null;
        circleImageView=findViewById(R.id.action_profile_image);
        importButton=findViewById(R.id.importTableButton);
        importButton.setVisibility(View.INVISIBLE);
        noText=findViewById(R.id.import_noText);
        noText.setVisibility(View.VISIBLE);
        enteredId=findViewById(R.id.import_classId);
        goButton=findViewById(R.id.importButton);
        noText.setText("Enter Access Code and click on the Go Button");
        recyclerView=findViewById(R.id.import_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Query query=collectionReference1.whereEqualTo("userId","null");
        FirestoreRecyclerOptions<ClassDetail> options= new  FirestoreRecyclerOptions.Builder<ClassDetail>()
                .setQuery(query,ClassDetail.class)
                .build();
        importAdapter=new ImportAdapter(options);
        recyclerView.setAdapter(importAdapter);

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                x=0;
                if(!TextUtils.isEmpty(enteredId.getText().toString().trim())){
                    final String textId=enteredId.getText().toString().trim();
                    if(textId.equals(ClassApi.getInstance().getUserId())){
                        Toast.makeText(ImportDataActivity.this,"You cannot import your own data",Toast.LENGTH_LONG).show();
                    }
                    else {
                        collectionReference.whereEqualTo("userId",textId)
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        if(queryDocumentSnapshots.isEmpty()){
                                            Toast.makeText(ImportDataActivity.this,"Enter a valid Access Code",Toast.LENGTH_LONG).show();
                                        }
                                        else {
                                            collectionReference1.whereEqualTo("userId",textId)
                                                    .addSnapshotListener(ImportDataActivity.this, new EventListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                            if(error!=null){
                                                                return;
                                                            }
                                                            if(!value.isEmpty()){
                                                                noText.setVisibility(View.INVISIBLE);
                                                            }
                                                            else {
                                                                noText.setText("Empty Database");
                                                                noText.setVisibility(View.VISIBLE);
                                                            }
                                                        }
                                                    });
                                            Query query=collectionReference1.whereEqualTo("userId",textId).orderBy("day", Query.Direction.ASCENDING);
                                            FirestoreRecyclerOptions<ClassDetail> options= new  FirestoreRecyclerOptions.Builder<ClassDetail>()
                                                    .setQuery(query,ClassDetail.class)
                                                    .build();
                                            importAdapter=new ImportAdapter(options);
                                            recyclerView.setAdapter(importAdapter);
                                            importAdapter.startListening();
                                            importButton.setVisibility(View.VISIBLE);

                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onSuccess: 4");
                                    }
                                });
                    }

                }
                else{
                    Log.d(TAG, "onSuccess: 5");
                    Toast.makeText(ImportDataActivity.this,"Access code cannot be empty",Toast.LENGTH_LONG).show();
                }
            }
        });
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                x=0;
                final HashMap<String,ClassDetail> hashMap= ImportApi.getInstance().getImportList();
                if(hashMap==null){
                    Toast.makeText(ImportDataActivity.this,"0 classes imported",Toast.LENGTH_LONG).show();
                    HashMap<String,ClassDetail> hash=null;
                    ImportApi.getInstance().setImportList(hash);
                    Intent intent=new Intent(ImportDataActivity.this,TableActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                else{

                    for(final ClassDetail classDetail:hashMap.values()){
                        classDetail.setUsername(ClassApi.getInstance().getUsername());
                        classDetail.setUserId(ClassApi.getInstance().getUserId());
                        collectionReference1.whereEqualTo("userId",ClassApi.getInstance().getUserId())
                                .whereEqualTo("alarmId",classDetail.getAlarmId())
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        if(!queryDocumentSnapshots.isEmpty()){
                                            x++;
                                            Log.d(TAG, "onSuccess: exists "+classDetail.getSubject());
                                        }
                                        else {
                                            x++;
//                                            Log.d(TAG, "onSuccess: "+classDetail.getSubject());
                                            collectionReference1.add(classDetail).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    HashMap<String,ClassDetail> hash=null;
                                                    ImportApi.getInstance().setImportList(hash);
                                                }
                                            })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(ImportDataActivity.this,"Error!!!",Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                        }
                                    }
                                })
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if(x==0){
                                            Toast.makeText(ImportDataActivity.this,hashMap.size()+" classes imported",Toast.LENGTH_LONG).show();
                                        }
                                        else{
                                            Toast.makeText(ImportDataActivity.this,hashMap.size()-x+" classes imported, "+x+" duplicates found",Toast.LENGTH_LONG).show();
                                        }

                                    }
                                });
                    }
                    Log.d(TAG, "onClick:xxxx "+x);
//                    Toast.makeText(ImportDataActivity.this,hashMap.size()-x+" classes imported",Toast.LENGTH_LONG).show();
                    Toast.makeText(ImportDataActivity.this,"Import Successfull",Toast.LENGTH_LONG).show();
//                        Log.d(TAG, "onClick: "+classDetail.getSubject());
                    Intent intent=new Intent(ImportDataActivity.this,TableActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        HashMap<String,ClassDetail> hash=null;
        ImportApi.getInstance().setImportList(hash);
    }

    @Override
    protected void onStart() {
        super.onStart();
        HashMap<String,ClassDetail> hash=null;
        ImportApi.getInstance().setImportList(hash);
        importAdapter.startListening();
        imageCollection.whereEqualTo("userId",ClassApi.getInstance().getUserId())
                .addSnapshotListener(ImportDataActivity.this,new EventListener<QuerySnapshot>() {
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
        importAdapter.stopListening();
    }
}