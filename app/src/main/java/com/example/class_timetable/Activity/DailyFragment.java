package com.example.class_timetable.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.class_timetable.R;
import com.example.class_timetable.UI.ClassAdapter;
import com.example.class_timetable.Util.ClassApi;
import com.example.class_timetable.Util.TableApi;
import com.example.class_timetable.model.ClassDetail;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.text.MessageFormat;
import java.util.Calendar;


public class DailyFragment extends Fragment {
    public static final String TAG="DailyFragment";
    public static final String fixSubject="subject";
    public static final String fixTeacher="teacher";
    public static final String fixHour="hour";
    public static final String fixMinute="minute";
    public static final String alarmId="alarmId";
    public static TextView dailyText;
    private ClassAdapter classAdapter;

    public String currentUserId,currentUserName;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;

    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference collectionReference=db.collection("TableInfo");
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view= inflater.inflate(R.layout.fragment_daily,container,false);
        final Calendar calendar=Calendar.getInstance();
        final int day=calendar.get(Calendar.DAY_OF_WEEK)-1;
        dailyText=view.findViewById(R.id.dailyText);
        String[] weekdays=getResources().getStringArray(R.array.weekdays);
        dailyText.setText(MessageFormat.format("No classes on {0}", weekdays[day]));
        firebaseAuth=FirebaseAuth.getInstance();
        Query query=collectionReference.whereEqualTo("day",day).whereEqualTo("userId", TableApi.getInstance().getUserId());
        FirestoreRecyclerOptions<ClassDetail> options= new  FirestoreRecyclerOptions.Builder<ClassDetail>()
                .setQuery(query,ClassDetail.class)
                .build();
        classAdapter=new ClassAdapter(options);
        RecyclerView recyclerView=view.findViewById(R.id.recyclerView2);

//        YoYo.with(Techniques.ZoomInUp)
//                .delay(500)
//                .duration(700)
//                .repeat(1)
//                .playOn(recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(classAdapter);

//        classAdapter.notifyDataSetChanged();

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
        classAdapter.onDataChanged();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        classAdapter.startListening();
        firebaseUser=firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);

    }


    @Override
    public void onStop() {
        super.onStop();
        classAdapter.stopListening();
        if(firebaseAuth!=null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}
