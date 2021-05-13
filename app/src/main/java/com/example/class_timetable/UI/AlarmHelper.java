package com.example.class_timetable.UI;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

public class AlarmHelper {
    public static final String TAG="AlarmHelper";
    public static final String fixSubject="subject";
    public static final String fixTeacher="teacher";
    public static final String fixHour="hour";
    public static final String fixMinute="minute";
    public static final String fixAlarm="alarmId";
    public static final String fixClassLink="classLink";


    public static void setAlarm(Context context, int hourSet, int minuteSet, int day, int alarmId, String teacher, String subject,String classLink) {


        Calendar calendar=Calendar.getInstance();
        Calendar setCalendar=Calendar.getInstance();
        setCalendar.set(Calendar.HOUR_OF_DAY,hourSet);
        setCalendar.set(Calendar.MINUTE,minuteSet);
        setCalendar.set(Calendar.SECOND,0);
        setCalendar.set(Calendar.DAY_OF_WEEK,day+1);


        if(setCalendar.before(calendar)){

            setCalendar.add(Calendar.DATE,7);
        }


        Intent notifyIntent = new Intent(context, AlarmReceiver.class);
        notifyIntent.putExtra(fixAlarm,alarmId);
        notifyIntent.putExtra(fixSubject,subject);
        notifyIntent.putExtra(fixTeacher,teacher);
        notifyIntent.putExtra(fixHour,hourSet);
        notifyIntent.putExtra(fixMinute,minuteSet);
        notifyIntent.putExtra(fixClassLink,classLink);


        PendingIntent pendingIntent=PendingIntent.getBroadcast(context,alarmId,notifyIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        boolean alarmUp=(PendingIntent.getBroadcast(context,alarmId,notifyIntent,PendingIntent.FLAG_NO_CREATE)!=null);

        if(!alarmUp){

            return;
        }

        AlarmManager alarmManager= (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {

            //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,setCalendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY*7,pendingIntent);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,setCalendar.getTimeInMillis(),1000,pendingIntent);
        }
    }

    public static void cancelAlarm(Context context,String value) {
        Intent notifyIntent=new Intent(context,AlarmReceiver.class);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(context, Integer.parseInt(value),notifyIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager= (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {

            alarmManager.cancel(pendingIntent);
        }
    }
}
