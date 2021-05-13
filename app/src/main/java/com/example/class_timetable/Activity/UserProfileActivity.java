package com.example.class_timetable.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.class_timetable.R;
import com.example.class_timetable.Util.ClassApi;
import com.example.class_timetable.Util.TableApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int GALLERY_CODE = 1453;
    private ImageView userImage;
    private ImageButton editImage,deleteImage;
    private TextView usernameText,emailText;
    private Button resetPassButton,deleteAccButton;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;
    private String imageLink=null,imageUrl;
    private CircleImageView circleImageView;
    private Uri imageUri;
    public static final String TAG="userprofileactivity";
    private static final String IMAGEURL = "imageUrl";
    private ConstraintLayout container;
    private TextView progressText;
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference collectionReference=db.collection("Users");
    
    private CollectionReference imageCollection= FirebaseFirestore.getInstance().collection("Users");
    private StorageReference storageReference,storagedelete;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar=findViewById(R.id.toolbarUserProfile);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView toolbarText=findViewById(R.id.toolbarText);
        toolbarText.setText(ClassApi.getInstance().getUsername());
        imageLink=null;

        container=findViewById(R.id.progressContainer);
        progressBar=findViewById(R.id.userProgressBar);
        progressText=findViewById(R.id.progressText);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();
        storageReference= FirebaseStorage.getInstance().getReference();
        storagedelete=storageReference.child("profile_picture");
        circleImageView=findViewById(R.id.action_profile_image);

        userImage=findViewById(R.id.userProfilePhoto);
        editImage=findViewById(R.id.editUserProfilePicture);
        deleteImage=findViewById(R.id.deleteUserProfilePicture);
        usernameText=findViewById(R.id.user_username);
        emailText=findViewById(R.id.user_email);
        resetPassButton=findViewById(R.id.user_resetPassword_button);
        deleteAccButton=findViewById(R.id.user_delete_button);

        emailText.setText(firebaseUser.getEmail().toString().trim());
        usernameText.setText(ClassApi.getInstance().getUsername());

        editImage.setOnClickListener(this);
        resetPassButton.setOnClickListener(this);
        deleteAccButton.setOnClickListener(this);
        deleteImage.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.editUserProfilePicture:
                Intent usereditIntent=new Intent(Intent.ACTION_GET_CONTENT);
                usereditIntent.setType("image/*");
                startActivityForResult(usereditIntent,GALLERY_CODE);
                break;
            case R.id.user_resetPassword_button:
                firebaseAuth.sendPasswordResetEmail(firebaseAuth.getCurrentUser().getEmail().toString())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                firebaseAuth.signOut();
                                Toast.makeText(UserProfileActivity.this,"Check your email!!",Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(UserProfileActivity.this,"Error!!",Toast.LENGTH_LONG).show();
                            }
                        });
                break;
            case R.id.user_delete_button:
                Intent deleteIntent=new Intent(UserProfileActivity.this, DeleteAccountActivity.class);
                deleteIntent.putExtra(IMAGEURL,imageLink);
                startActivity(deleteIntent);
                break;
            case R.id.deleteUserProfilePicture:
                String deleteImage="profile_"+ClassApi.getInstance().getUserId();
                imageUrl=null;
                storagedelete.child(deleteImage).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                final Map<String, Object> userObj = new HashMap<>();
                                userObj.put("userId", ClassApi.getInstance().getUserId());
                                userObj.put("username", ClassApi.getInstance().getUsername());
                                userObj.put("imageUrl", imageUrl);

                                collectionReference.whereEqualTo("userId",ClassApi.getInstance().getUserId())
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                                for(QueryDocumentSnapshot snapshot:queryDocumentSnapshots){
                                                    snapshot.getReference().update(userObj)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    container.setVisibility(View.INVISIBLE);
                                                                    Toast.makeText(UserProfileActivity.this,"Profile Picture Deleted",Toast.LENGTH_LONG).show();

                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(UserProfileActivity.this,"Error!!!",Toast.LENGTH_LONG).show();

                                                                    container.setVisibility(View.INVISIBLE);
                                                                }
                                                            });
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                container.setVisibility(View.INVISIBLE);
                                            }
                                        });

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_CODE && resultCode==RESULT_OK){
            if(data!=null){
                container.setVisibility(View.VISIBLE);
                imageUri=data.getData();
                userImage.setImageURI(imageUri);
                final StorageReference filepath = storageReference
                        .child("profile_picture")
                        .child("profile_" + ClassApi.getInstance().getUserId());
                filepath.putFile(imageUri)
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

                                int progress= (int) (100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                                progressBar.setProgress(progress);
                                progressText.setText(MessageFormat.format("{0}%", progress));
                            }
                        })

                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                imageUrl = uri.toString();

                                final Map<String, Object> userObj = new HashMap<>();
                                userObj.put("userId", ClassApi.getInstance().getUserId());
                                userObj.put("username", ClassApi.getInstance().getUsername());
                                userObj.put("imageUrl", imageUrl);

                                collectionReference.whereEqualTo("userId",ClassApi.getInstance().getUserId())
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                                for(QueryDocumentSnapshot snapshot:queryDocumentSnapshots){
                                                    snapshot.getReference().update(userObj)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    container.setVisibility(View.INVISIBLE);
                                                                    Toast.makeText(UserProfileActivity.this,"Profile Picture Updated",Toast.LENGTH_LONG).show();

                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {

                                                                    Toast.makeText(UserProfileActivity.this,"Error!!!",Toast.LENGTH_LONG).show();
                                                                    container.setVisibility(View.INVISIBLE);
                                                                }
                                                            });
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                container.setVisibility(View.INVISIBLE);
                                            }
                                        });


                            }
                        });
                    }
                })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            container.setVisibility(View.INVISIBLE);
                        }
                    });
            }
            else {
                container.setVisibility(View.INVISIBLE);
            }
        }
        else {
            container.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        progressText.setText("0%");
        progressBar.setProgress(0);
        imageCollection.whereEqualTo("userId",ClassApi.getInstance().getUserId())
                .addSnapshotListener(UserProfileActivity.this,new EventListener<QuerySnapshot>() {
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
                                        .into(userImage);
                                Glide.with(getApplicationContext())
                                        .asBitmap()
                                        .load(imageLink)
                                        .into(circleImageView);

                            }
                            else{
                                circleImageView.setImageResource(R.drawable.ic_baseline_person_24);
                                userImage.setImageResource(R.drawable.ic_baseline_person_24);
                            }
                        }
                    }
                });
    }
}