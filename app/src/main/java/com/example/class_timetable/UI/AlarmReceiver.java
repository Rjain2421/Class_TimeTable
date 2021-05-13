package com.example.class_timetable.UI;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;

import com.example.class_timetable.UI.AlarmJobService;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG ="AlarmReceiver" ;
    public static final String fixSubject="subject";
    public static final String fixTeacher="teacher";
    public static final String fixHour="hour";
    public static final String fixMinute="minute";
    public static final String fixAlarm="alarmId";
    public static final String fixClassLink="classLink";
    private Vibrator vibrator;


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: ");
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            AlarmJobService.enqueueWork(context, new Intent());
        }

        Intent newintent=new Intent(context, AlarmJobService.class);
        newintent.putExtra(fixAlarm,intent.getIntExtra(fixAlarm,0));
        newintent.putExtra(fixSubject,intent.getStringExtra(fixSubject));
        newintent.putExtra(fixTeacher,intent.getStringExtra(fixTeacher));
        newintent.putExtra(fixHour,intent.getIntExtra(fixHour,0));
        newintent.putExtra(fixMinute,intent.getIntExtra(fixMinute,0));
        newintent.putExtra(fixClassLink,intent.getStringExtra(fixClassLink));

        AlarmJobService.enqueueWork(context,newintent);


    }

}
