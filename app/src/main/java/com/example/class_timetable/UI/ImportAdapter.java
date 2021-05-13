package com.example.class_timetable.UI;

import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.class_timetable.R;
import com.example.class_timetable.Util.ImportApi;
import com.example.class_timetable.model.ClassDetail;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import java.util.HashMap;

public class ImportAdapter extends FirestoreRecyclerAdapter<ClassDetail,ImportAdapter.ImportHolder> {
    public static final String TAG="importAdapter";
    private HashMap<String,ClassDetail> hashMap;
    private SparseBooleanArray selected;
    public ImportAdapter(@NonNull FirestoreRecyclerOptions<ClassDetail> options) {
        super(options);
        hashMap=new HashMap<>();
        selected=new SparseBooleanArray();
    }

    @Override
    protected void onBindViewHolder(@NonNull final ImportHolder holder, final int position, @NonNull ClassDetail model) {
        holder.subjectName.setText(model.getSubject());
        holder.teacherName.setText(model.getTeacher());
        holder.classTime.setText(model.getClassTime());
        holder.classDay.setText(holder.weekdays[model.getDay()]);
        holder.radioButton.setChecked(selected.get(position,false));
        if(selected.get(position)){
            holder.itemView.setBackgroundResource(R.color.colorButton);
        }
        else {
            holder.itemView.setBackgroundResource(0);
        }
    }

    @NonNull
    @Override
    public ImportHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.class_selector,parent,false);
        return new ImportHolder(view);
    }

    class ImportHolder  extends RecyclerView.ViewHolder {
        private TextView subjectName,teacherName,classTime,classDay;
        private RadioButton radioButton;
        private String[] weekdays;
        public ImportHolder(@NonNull final View itemView) {
            super(itemView);
            weekdays=itemView.getResources().getStringArray(R.array.weekdays);
            subjectName=itemView.findViewById(R.id.select_subjectName);
            teacherName=itemView.findViewById(R.id.select_teacherName);
            classTime=itemView.findViewById(R.id.select_classTime);
            radioButton=itemView.findViewById(R.id.select_radioButton);
            classDay=itemView.findViewById(R.id.select_classDay);
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: "+getAdapterPosition());
                    if(selected.get(getAdapterPosition(),false)){
//                        isSelected=false;
                        selected.delete(getAdapterPosition());
                        radioButton.setChecked(false);
                        itemView.setBackgroundResource(0);
                        hashMap.remove(getSnapshots().getSnapshot(getAdapterPosition()).get("alarmId").toString());
                        ImportApi.getInstance().setImportList(hashMap);
                    }
                    else {
                        selected.put(getAdapterPosition(),true);
//                        isSelected=true;
                        radioButton.setChecked(true);
                        itemView.setBackgroundResource(R.color.colorButton);
                        hashMap.put(getSnapshots().getSnapshot(getAdapterPosition()).get("alarmId").toString(),getSnapshots().getSnapshot(getAdapterPosition()).toObject(ClassDetail.class));
                        ImportApi.getInstance().setImportList(hashMap);
                    }
                }
            });
        }
    }
}
