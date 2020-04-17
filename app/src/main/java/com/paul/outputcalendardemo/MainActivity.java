package com.paul.outputcalendardemo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.paul.eventreminder.CalendarManager;
import com.paul.eventreminder.model.CalendarEvent;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    CalendarManager calendarManager;
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        calendarManager=new CalendarManager(this,"测试","测试","com.paul.test","测试账户");
        textView=findViewById(R.id.tv_main);
    }

    public void inputCalendar(View view) {

        CalendarEvent calendarEvent=new CalendarEvent();
        calendarEvent.setContent("我是一个测试消息");
        calendarEvent.setDayOfWeek(6);
        calendarEvent.setStartTime("8:00");
        calendarEvent.setEndTime("10:00");
        calendarEvent.setSummary("测试课程");
        calendarEvent.setLoc("教室");
        List<Integer> integers=new ArrayList<>();
        for(int i=1;i<14;i++){
            integers.add(i);
        }
        calendarEvent.setWeekList(integers);
        calendarManager.init();
        calendarManager.setAlarm(true);
        calendarManager.setAlarmTime(20);
        calendarManager.addCalendarEvent(calendarEvent, 8, new CalendarManager.OnExportProgressListener() {
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

}
