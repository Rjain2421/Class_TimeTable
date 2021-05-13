package com.example.class_timetable.model;

public class ClassDetail {
    private String subject,teacher,classLink,classTime,username,userId,alarmId;
    int day,hour,minute;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public String getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(String alarmId) {
        this.alarmId = alarmId;
    }

    public ClassDetail(String subject, String teacher, String classLink, String classTime, String username, String userId, String alarmId, int day, int hour, int minute) {
        this.subject = subject;
        this.teacher = teacher;
        this.classLink = classLink;
        this.classTime = classTime;
        this.username = username;
        this.userId = userId;
        this.alarmId = alarmId;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getClassLink() {
        return classLink;
    }

    public void setClassLink(String classLink) {
        this.classLink = classLink;
    }

    public String getClassTime() {
        return classTime;
    }

    public void setClassTime(String classTime) {
        this.classTime = classTime;
    }

    public ClassDetail() {
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }
}
