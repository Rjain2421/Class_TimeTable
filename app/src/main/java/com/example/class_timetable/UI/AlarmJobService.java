package com.example.class_timetable.UI;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;

import com.example.class_timetable.Activity.DemoApp;
import com.example.class_timetable.Activity.MainActivity;
import com.example.class_timetable.R;

public class AlarmJobService extends JobIntentService {
    public static final String TAG="AlarmJobService";
    public static final int JOB_ID = 100;
//    public static final int NOTIF_ID = 56;
private static final String PRIMARY_CHANNEL_ID =
        "primary_notification_channel";
    public static final String fixClassLink="classLink";
    public static final String fixSubject="subject";
    public static final String fixTeacher="teacher";
    public static final String fixHour="hour";
    public static final String fixMinute="minute";
    public static final String fixAlarm="alarmId";

    private String subject,classLink;
    private String teacher;
    private int hour;
    private int minute;
    private int alarm;


    long timestamp;
    public static void enqueueWork(Context context, Intent work) {

        enqueueWork(context, AlarmJobService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        if(isStopped()){
            return;
        }
        hour=intent.getIntExtra(fixHour,0);
        minute=intent.getIntExtra(fixMinute,0);
        teacher=intent.getStringExtra(fixTeacher);
        subject=intent.getStringExtra(fixSubject);
        alarm=intent.getIntExtra(fixAlarm,0);
        classLink=intent.getStringExtra(fixClassLink);

        updateNotification();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void updateNotification(){

        Intent intent=new Intent(this, MainActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,0);

        Intent urlIntent=new Intent(Intent.ACTION_VIEW, Uri.parse(classLink));
        PendingIntent urlPendingIntent=PendingIntent.getActivity(this,0,urlIntent,0);

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), PRIMARY_CHANNEL_ID)
                .setContentTitle(subject)
                .setContentText("Class by "+teacher+" at- "+hour+":"+minute)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher_classtime)
                .setContentIntent(pendingIntent)
                .setColor(Color.BLUE)
                .addAction(R.mipmap.ic_launcher_classtime,"Go To Link",urlPendingIntent)
                .setAutoCancel(true)
                .build();
        NotificationManager manager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if(manager!=null) manager.notify(123, notification);
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public boolean onStopCurrentWork() {

        return super.onStopCurrentWork();
    }
}
