package com.example.class_timetable.UI;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.class_timetable.Activity.DailyFragment;
import com.example.class_timetable.Activity.PostDayActivity;
import com.example.class_timetable.R;
import com.example.class_timetable.Util.ClassApi;
import com.example.class_timetable.Util.TableApi;
import com.example.class_timetable.model.ClassDetail;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ClassAdapter extends FirestoreRecyclerAdapter<ClassDetail, ClassAdapter.ClassHolder> {
    public static final String TAG="ClassAdapter";
    public static final String fixLink="classLink";
    public static final String fixTime="classTime";
    public static final String fixSubject="subject";
    public static final String fixTeacher="teacher";
    public static final String fixHour="hour";
    public static final String fixMinute="minute";
    public static final String alarmId="alarmId";
    private SharedPreferences sharedPreferences;
    private String sharedPrefFile="com.example.android.classtimetable";
    public Boolean isEmpty=false;

    public ClassAdapter(@NonNull FirestoreRecyclerOptions<ClassDetail> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ClassHolder holder, int position, @NonNull ClassDetail model) {
//        Animation animation=AnimationUtils.loadAnimation(holder.context,R.anim.fade_in);
//        animation.setDuration(2500);
//        holder.container.setAnimation(animation);
//        YoYo.with(Techniques.Tada)
//                .duration(700)
//                .repeat(1)
//                .playOn(holder.container);
        holder.teacher.setText(model.getTeacher());
        holder.subject.setText(model.getSubject());
        holder.time.setText(model.getClassTime());
        holder.link.setText(model.getClassLink());

        DocumentSnapshot snap=getSnapshots().getSnapshot(position);
        String val= (String) snap.get(alarmId);

        if(!sharedPreferences.getBoolean(val, true)){

            holder.notificationButton.setTag("0");
            holder.notificationButton.setImageResource(R.drawable.ic_baseline_notifications_active_24);
        }
        else if(holder.notificationButton.getTag()==null){

            holder.notificationButton.setTag("1");
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putBoolean(val,true);
            editor.apply();
        }

    }

    @Override
    public void onDataChanged() {
        if(getItemCount()==0){
            Log.d(TAG, "onDataChanged:hello ");
            isEmpty=true;
            DailyFragment.dailyText.setVisibility(View.VISIBLE);
        }
        else{
            DailyFragment.dailyText.setVisibility(View.INVISIBLE);
        }
    }

    @NonNull
    @Override
    public ClassHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.class_info,parent,false);
        return new ClassHolder(view);
    }
    public void deleteItem(Context context, int position){
        DocumentSnapshot snapshot=getSnapshots().getSnapshot(position);
        String value=snapshot.getString(alarmId);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.remove(value);
        editor.apply();
        AlarmHelper.cancelAlarm(context,value);
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    class ClassHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Context context;
        private TextInputLayout subjectLayout,teacherLayout,linkLayout;
        private TextView teacher,subject,time,link,headline;
        private ImageButton editButton,deleteButton,linkButton,notificationButton;
        private EditText classLink;
        private TextView classTime;
        private Calendar c;
        private EditText teacherName,subjectName;
        private Button setTimeButton,saveButton;
       private ClassDetail classDetail;
        private AlertDialog.Builder builder;
        private AlertDialog dialog;
        private LayoutInflater inflater;
        private RelativeLayout container;
        private int hourSet,minuteSet;
        DocumentSnapshot snapshot;
        private String alarm;

        public ClassHolder(@NonNull View itemView) {
            super(itemView);

            container=itemView.findViewById(R.id.containerClass);
            teacher=itemView.findViewById(R.id.teacherName);
            subject=itemView.findViewById(R.id.subjectName);
            time=itemView.findViewById(R.id.classTime);
            link=itemView.findViewById(R.id.classLink);
            linkButton=itemView.findViewById(R.id.goToLink);
            editButton=itemView.findViewById(R.id.editButton);
            deleteButton=itemView.findViewById(R.id.deleteButton);
            notificationButton=itemView.findViewById(R.id.notificationButton);
            linkButton.setOnClickListener(this);
            editButton.setOnClickListener(this);
            deleteButton.setOnClickListener(this);
            notificationButton.setOnClickListener(this);
            c=Calendar.getInstance();
            context=itemView.getContext();


            sharedPreferences=context.getSharedPreferences(sharedPrefFile,Context.MODE_PRIVATE);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DocumentSnapshot snapshot=getSnapshots().getSnapshot(getAdapterPosition());
                    classDetail=snapshot.toObject(ClassDetail.class);
                }
            });
        }

        @Override
        public void onClick(final View v) {
            final int position=getAdapterPosition();
            snapshot=getSnapshots().getSnapshot(getAdapterPosition());
            classDetail=snapshot.toObject(ClassDetail.class);
            switch (v.getId()){
                case R.id.notificationButton:
                    if(notificationButton.getTag()==null){
                        notificationButton.setTag("1");
                        notificationButton.setImageResource(R.drawable.ic_baseline_notifications_active_24);
                        AlarmHelper.cancelAlarm(context,classDetail.getAlarmId());
                        SharedPreferences.Editor editor=sharedPreferences.edit();
                        editor.putBoolean(classDetail.getAlarmId(),false);
                        editor.apply();
                        Toast.makeText(context,"Alarm Cancelled",Toast.LENGTH_LONG).show();
                    }
                    else if(notificationButton.getTag()=="0"){
                        notificationButton.setTag("1");
                        notificationButton.setImageResource(R.drawable.ic_baseline_notifications_active_red_24);
                        AlarmHelper.setAlarm(context,classDetail.getHour(),classDetail.getMinute(),classDetail.getDay(), Integer.parseInt(classDetail.getAlarmId()),classDetail.getTeacher(),classDetail.getSubject(),classDetail.getClassLink());
                        SharedPreferences.Editor editor=sharedPreferences.edit();
                        editor.putBoolean(classDetail.getAlarmId(),true);
                        editor.apply();
                        Toast.makeText(context,"Alarm Set",Toast.LENGTH_LONG).show();
                    }
                    else if(notificationButton.getTag()=="1"){
                        notificationButton.setTag("0");
                        notificationButton.setImageResource(R.drawable.ic_baseline_notifications_active_24);
                        AlarmHelper.cancelAlarm(context,classDetail.getAlarmId());
                        SharedPreferences.Editor editor=sharedPreferences.edit();
                        editor.putBoolean(classDetail.getAlarmId(),false);
                        editor.apply();
                        Toast.makeText(context,"Alarm Cancelled",Toast.LENGTH_LONG).show();
                    }
                    break;
                case R.id.goToLink:
                    Intent intent=new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.setData(Uri.parse(classDetail.getClassLink()));
                    v.getContext().startActivity(intent);
                    break;
                case R.id.editButton:
                    if(ClassApi.getInstance().getUserId().equals(TableApi.getInstance().getUserId())){
                        createPopupDialog(v.getContext());
                    }
                    else{
                        Toast.makeText(context,"No permission",Toast.LENGTH_LONG).show();
                    }
                    break;
                case  R.id.deleteButton:
                    if(TableApi.getInstance().getUserId().equals(ClassApi.getInstance().getUserId())){
                        AlertDialog.Builder dialogBuilder=new AlertDialog.Builder(context);
                        dialogBuilder.setMessage("Are you sure you want to delete this class?");
                        dialogBuilder.setPositiveButton(R.string.yesText, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteItem(v.getContext(),position);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.cancelText, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).show();
                    }
                    else{
                        Toast.makeText(context,"No permission",Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
        private void createPopupDialog(final Context context) {

            builder=new AlertDialog.Builder(editButton.getContext());
            inflater=LayoutInflater.from(editButton.getContext());
            View view=inflater.inflate(R.layout.pop_up_row,null);
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
            headline=view.findViewById(R.id.headlineText);
            headline.setText("Update Class Details");
            subjectLayout.setHint("Subject");
            teacherLayout.setHint("Teacher");
            linkLayout.setHint("Class Link");

            saveButton.setText("Update");
            classLink.setText(classDetail.getClassLink());
            classTime.setText(classDetail.getClassTime());
            teacherName.setText(classDetail.getTeacher());
            subjectName.setText(classDetail.getSubject());
            hourSet=classDetail.getHour();
            alarm=classDetail.getAlarmId();
            minuteSet=classDetail.getMinute();
            builder.setView(view);
            dialog=builder.create();
            dialog.getWindow().getAttributes().windowAnimations=R.style.DialogCustomAnimation;
            dialog.show();

            setTimeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Calendar c=Calendar.getInstance();
                    int hour=c.get(Calendar.HOUR_OF_DAY);
                    int minute=c.get(Calendar.MINUTE);
                    TimePickerDialog timePickerDialog=new TimePickerDialog(editButton.getContext(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            String timetext=hourOfDay+":"+minute;
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
                            && !subjectName.getText().toString().isEmpty()){
                        saveButton.setEnabled(true);
                        AlarmHelper.cancelAlarm(context,alarm);
                        AlarmHelper.setAlarm(context,hourSet,minuteSet,classDetail.getDay(), Integer.parseInt(alarm),teacherName.getText().toString().trim(),subjectName.getText().toString().trim(),classLink.getText().toString().trim());
                        Map<String,Object> map=new HashMap<>();
                        map.put(fixLink,classLink.getText().toString().trim());
                        map.put(fixSubject,subjectName.getText().toString().trim());
                        map.put(fixTeacher,teacherName.getText().toString().trim());
                        map.put(fixTime,classTime.getText().toString().trim());
                        map.put(fixHour,hourSet);
                        map.put(fixMinute,minuteSet);
                        snapshot.getReference().update(map)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        dialog.dismiss();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(editButton.getContext(),"Unsuccessfull",Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                    else{
                        Snackbar.make(v,"Empty Fields not allowed",Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
