package com.example.class_timetable.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.class_timetable.R;
import com.example.class_timetable.Util.ClassApi;
import com.example.class_timetable.Util.TableApi;
import com.example.class_timetable.model.ClassDetail;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Objects;

public class WeeklyFragment extends Fragment implements View.OnClickListener {
    private Button[] buttons=new Button[7];
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference collectionReference=db.collection("TableInfo");
    public static final String TAG="weeklyFragment";
    int x;
    int[] daycount;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view=inflater.inflate(R.layout.fragment_weekly,container,false);
        final String[] weekdays=getResources().getStringArray(R.array.weekdays);
        Calendar calendar=Calendar.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();
        final int day=calendar.get(Calendar.DAY_OF_WEEK)-1;
//        for(int j=0;j<7;j++){
//            daycount[j]=0;
//        }
        for(int i=0;i<7;i++){
            String id="button"+i;
            int resId=getResources().getIdentifier(id,"id", Objects.requireNonNull(getActivity()).getPackageName());
            buttons[i]= view.findViewById(resId);
            buttons[i].setOnClickListener(this);
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        daycount=new int[7];
        final String[] weekdays=getResources().getStringArray(R.array.weekdays);
        final int day=Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1;
        collectionReference.whereEqualTo("userId",TableApi.getInstance().getUserId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot snapshot:queryDocumentSnapshots){
                            ClassDetail classDetail=snapshot.toObject(ClassDetail.class);
                            int y= classDetail.getDay();
                            daycount[y]++;
                            Log.d(TAG, "onSuccess: "+daycount[y]);
                        }
                        for(int i=0;i<7;i++){
                            String id="button"+i;
                            int resId=getResources().getIdentifier(id,"id", Objects.requireNonNull(getActivity()).getPackageName());
                            if(i==0){
                                x=resId;
                                x=x%10;
                            }
//                            buttons[i].setText(weekdays[i]);
                            buttons[i].setText(MessageFormat.format("{0}{1}({2})", weekdays[i], System.getProperty("line.separator"), daycount[i]));
                        }
                        buttons[day].setBackgroundResource(R.color.colorDay);
                    }
                });

    }

    @Override
    public void onClick(View v) {
        int resId=v.getId();
        Intent intent=new Intent(getActivity(),PostDayActivity.class);
        intent.putExtra("day",resId-x);
        startActivity(intent);
    }
}
