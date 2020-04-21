package com.paul.eventreminder;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.paul.eventreminder.model.CalendarEvent;
import com.paul.eventreminder.utils.ColorPool;
import com.paul.eventreminder.utils.ICSRulesHelper;
import com.paul.eventreminder.utils.TimeUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VAlarm;
import biweekly.component.VEvent;
import biweekly.parameter.Related;
import biweekly.property.Color;
import biweekly.property.RecurrenceRule;
import biweekly.property.Trigger;
import biweekly.util.Duration;

public class ICSManager {
    boolean flag_alarm=false;
    boolean flag_download=true;
    int alarm_seconds=15;
    String userName;
    Context context;
    Activity activity;

    public boolean isFlag_download() {
        return flag_download;
    }

    public void setFlag_download(boolean flag_download) {
        this.flag_download = flag_download;
    }

    public boolean isFlag_alarm() {
        return flag_alarm;
    }

    public ICSManager(Activity activity, String userName) {
        this.userName = userName;
        this.context=activity.getApplicationContext();
        this.activity=activity;
        init();
    }
    private void init(){
        //android.permission.WRITE_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }
    public void setFlag_alarm(boolean falg_alarm) {
        this.flag_alarm = falg_alarm;
    }

    public void setAlarm_seconds(int alarm_seconds) {
        this.alarm_seconds = alarm_seconds;
    }

    private void exportToFile(String filename, List<VEvent> vEvents,OutPutListener listener){
        ICalendar ical = new ICalendar();

        final File[] dirs = context.getExternalFilesDirs("Documents");
        File primaryDir = null;
        if (dirs != null && dirs.length > 0) {
            primaryDir = dirs[0];
        }
        if(primaryDir==null){
            return ;
        }
        for(VEvent i:vEvents){
            ical.addEvent(i);
        }
        File file = new File(primaryDir.getAbsolutePath(), filename + ".ics");
        if(file.exists()){
            file.delete();
        }
        try {
            Biweekly.write(ical).go(file);
            listener.onSuccess(file.getAbsolutePath());
        } catch (IOException e) {
            listener.onError(e+"");
            e.printStackTrace();
        }
        if(flag_download){
            insertToDownloadDir(file,filename);
        }


    }
    /**
     * 把数据源转为VEvent
     * */
    private  List<VEvent> getDataSource(List<CalendarEvent> calendarEvents,int curWeek,OutPutListener listener){
        List<VEvent> vEvents=new ArrayList<>();
        int count=0;
        for(CalendarEvent lesson:calendarEvents){
            count++;
            listener.onProgress(count,calendarEvents.size());
            String startTime=lesson.getStartTime();
            String endTime=lesson.getEndTime();
            vEvents.addAll(addToEventList(lesson,startTime,endTime,curWeek,listener, ColorPool.getColor()));
        }
        return vEvents;
    }
    /**
     * 把数据源转为VEvent
     * */
    private  List<VEvent> getDataSource(CalendarEvent calendarEvent,int curWeek,OutPutListener listener){

        List<VEvent> vEvents=new ArrayList<>();

        String startTime=calendarEvent.getStartTime();
        String endTime=calendarEvent.getEndTime();
        vEvents.addAll(addToEventList(calendarEvent,startTime,endTime,curWeek,listener, ColorPool.getColor()));

        return vEvents;
    }
    private  List<VEvent> addToEventList(CalendarEvent model,String startTime,String endTime,int curWeek,OutPutListener listener,String color){
        List<VEvent> vEventList=new ArrayList<>();
        Set<Integer> weekSet= new HashSet<>();
        weekSet.addAll(model.getWeekList());
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf2=new SimpleDateFormat("yyyy-MM-dd HH:mm");
        int thisDay=TimeUtil.getTodayWeekNumber();
        for(Integer i:weekSet){
            Date date= TimeUtil.getTargetDate(curWeek,i,thisDay,model.getDayOfWeek());
            String dateString=sdf.format(date);
            try {
                Date realStartDate=sdf2.parse(dateString+" "+startTime);
                Date realEndDate=sdf2.parse(dateString+" "+endTime);
                VEvent event=new VEvent();
                event.setSummary(model.getSummary());
                event.setDateStart(realStartDate);
                event.setDateEnd(realEndDate);
                event.setLocation(model.getLoc());
                event.setTransparency(true);
                String des="第"+i+"周 | "+model.getStartTime()+"-"+model.getEndTime()+" "+model.getContent();
                event.setDescription(des);
                event.setColor(color);
                if(flag_alarm){
                    Duration duration = Duration.builder().prior(true).minutes(alarm_seconds).build();
                    Trigger trigger = new Trigger(duration, Related.START);
                    VAlarm alarm = VAlarm.display(trigger, model.getSummary()+"于"+alarm_seconds+"分钟后开始");
                    event.addAlarm(alarm);
                }
                vEventList.add(event);
            } catch (ParseException e) {
                listener.onError("出错了:"+e);
                e.printStackTrace();
            }
        }
        return vEventList;
    }

    public interface OutPutListener{
        public void onError(String msg);
        public void onProgress(int now,int total);
        public void onSuccess(String filedir);
    }
    /**
     * 输出
     * @param filename 生成的文件名称
     * @param useRule true使用 false不使用 重复规则
     * */
    public void OutPutIcsFile(String filename,boolean useRule,List<CalendarEvent> calendarEvents,int curWeek,OutPutListener listener){
        //exportToFile(String filename, List<CalendarEvent>calendarEvents, int curWeek,OutPutListener listener)
        if(useRule){
            //使用重复规则
            exportToFile(filename,getEventByRule(curWeek,calendarEvents,listener),listener);
        }else {
            exportToFile(filename,getDataSource(calendarEvents,curWeek,listener),listener);
        }
    }
    private List<VEvent> getEventByRule(int curWeek, List<CalendarEvent> calendarEvents, OutPutListener listener){
        List<VEvent> events=new ArrayList<>();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf2=new SimpleDateFormat("yyyy-MM-dd HH:mm");
        int thisDay=TimeUtil.getTodayWeekNumber();
        int count=0;
        for(CalendarEvent model:calendarEvents){
            count++;
            listener.onProgress(count,calendarEvents.size());
            Date date= TimeUtil.getTargetDate(curWeek,model.getWeekList().get(0),thisDay,model.getDayOfWeek());
            String dateString=sdf.format(date);
            Date realStartDate= null;
            try {
                RecurrenceRule rRule = ICSRulesHelper.getRRule(model);
                if(rRule==null){
                    events.addAll(getDataSource(model,curWeek,listener));
                }else {
                    realStartDate = sdf2.parse(dateString+" "+model.getStartTime());
                    Date realEndDate=sdf2.parse(dateString+" "+model.getEndTime());
                    VEvent event=new VEvent();
                    event.setSummary(model.getSummary());
                    event.setDateStart(realStartDate);
                    event.setDateEnd(realEndDate);
                    event.setLocation(model.getLoc());
                    event.setTransparency(true);
                    String des=model.getStartTime()+"-"+model.getEndTime()+" "+model.getContent();
                    event.setDescription(des);
                    event.setColor(ColorPool.getColor());
                    if(flag_alarm){
                        Duration duration = Duration.builder().prior(true).minutes(alarm_seconds).build();
                        Trigger trigger = new Trigger(duration, Related.START);
                        VAlarm alarm = VAlarm.display(trigger, model.getSummary()+"于"+alarm_seconds+"分钟后开始");
                        event.addAlarm(alarm);
                    }
                    event.setRecurrenceRule(rRule);
                    events.add(event);
                }
            } catch (ParseException e) {
                listener.onError(e+"");
                e.printStackTrace();
            }

        }
        return events;
    }


    public void insertToDownloadDir(File file,String fileName){

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            ContentValues values=new ContentValues();
            ContentResolver contentResolver=context.getContentResolver();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME,fileName+".ics");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
            Uri uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try {
                    FileInputStream fin=new FileInputStream(file);//输入流
                    OutputStream outputStream = contentResolver.openOutputStream(uri);
                    if (outputStream != null) {
                        byte[]b=new byte[1024];
                        while((fin.read(b))!=-1) {//读取到末尾 返回-1 否则返回读取的字节个数
                            outputStream.write(b);
                        }
                        fin.close();
                        outputStream.close();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }else {
            String path=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            try {
                FileInputStream fin=new FileInputStream(file);//输入流
                File newfile=new File(path,fileName+".ics");
                FileOutputStream fout=new FileOutputStream(newfile,true);//输出流
                byte[]b=new byte[1024];
                while((fin.read(b))!=-1) {//读取到末尾 返回-1 否则返回读取的字节个数
                    fout.write(b);
                }
                fin.close();
                fout.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
