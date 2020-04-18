package com.paul.outputcalendardemo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.paul.eventreminder.CalendarManager;
import com.paul.eventreminder.ICSManager;
import com.paul.eventreminder.model.CalendarEvent;
import com.paul.eventreminder.utils.CoverUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    CalendarManager calendarManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        calendarManager=new CalendarManager(this,"测试");

    }

    public void inputCalendar(View view) {

        calendarManager.setAlarm(true);
        calendarManager.setAlarmTime(20);
        calendarManager.addCalendarEvent(getTestEvent(), 8, new CalendarManager.OnExportProgressListener() {
            @Override
            public void onProgress(int total, int now) {

            }

            @Override
            public void onError(String msg) {

            }

            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this,"成功",Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void deleteCalendar(View view) {
        calendarManager.deleteCalendarEvent(MainActivity.this, new CalendarManager.OnExportProgressListener() {
            @Override
            public void onProgress(int total, int now) {

            }

            @Override
            public void onError(String msg) {
                Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this,"成功",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void exportCalendar(View view) {
        ICSManager icsManager=new ICSManager(MainActivity.this,"测试");
        icsManager.setFalg_alarm(true);
        icsManager.setAlarm_seconds(20);
        List<CalendarEvent> calendarEvents=new ArrayList<>();
        calendarEvents.add(getTestEvent());
        icsManager.OutPutIcsFile("testICS", true,calendarEvents, 8, new ICSManager.OutPutListener() {
            @Override
            public void onError(String msg) {

            }

            @Override
            public void onProgress(int now, int total) {

            }

            @Override
            public void onSuccess(String filedir) {
                Toast.makeText(MainActivity.this,"成功"+filedir,Toast.LENGTH_SHORT).show();
            }
        });
    }
    public CalendarEvent getTestEvent(){
        CalendarEvent calendarEvent=new CalendarEvent();
        calendarEvent.setContent("我是一个测试消息");
        calendarEvent.setDayOfWeek(6);
        calendarEvent.setStartTime("8:00");
        calendarEvent.setEndTime("10:00");
        calendarEvent.setSummary("测试课程");
        calendarEvent.setLoc("教室");
        Integer lists[]={2,4,6,8,10};
        calendarEvent.setWeekList(CoverUtil.getList(lists));
        return calendarEvent;
    }

}
