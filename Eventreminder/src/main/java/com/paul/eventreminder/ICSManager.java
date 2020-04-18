package com.paul.eventreminder;

import android.content.Context;

import com.paul.eventreminder.model.CalendarEvent;
import com.paul.eventreminder.utils.ColorPool;
import com.paul.eventreminder.utils.TimeUtil;

import java.io.File;
import java.io.IOException;
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
import biweekly.property.Trigger;
import biweekly.util.Duration;

public class ICSManager {
    boolean falg_alarm=false;
    int alarm_seconds=15;
    String userName="默认日历";
    Context context;
    OutPutListener listener;

    public ICSManager(Context context,String userName, OutPutListener listener) {
        this.userName = userName;
        this.listener = listener;
        this.context=context;
    }

    public void setFalg_alarm(boolean falg_alarm) {
        this.falg_alarm = falg_alarm;
    }

    public void setAlarm_seconds(int alarm_seconds) {
        this.alarm_seconds = alarm_seconds;
    }

    public  void exportToFile(String filename, List<CalendarEvent>calendarEvents, int curWeek){
        ICalendar ical = new ICalendar();

        final File[] dirs = context.getExternalFilesDirs("Documents");
        File primaryDir = null;
        if (dirs != null && dirs.length > 0) {
            primaryDir = dirs[0];
        }
        if(primaryDir==null){
            return ;
        }
        List<VEvent> data=getDataSource(calendarEvents,curWeek);
        for(VEvent i:data){
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
            e.printStackTrace();
        }
    }
    /**
     * 把数据源转为VEvent
     * */
    private  List<VEvent> getDataSource(List<CalendarEvent> calendarEvents,int curWeek){

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

                String des="第"+i+"周 | "+model.getStartTime()+"-"+model.getEndTime()+" "+model.getContent();
                event.setDescription(des);
                event.setColor(color);
                if(falg_alarm){
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
}
