package com.paul.eventreminder.utils;

import java.util.Calendar;
import java.util.Date;

public class TimeUtil {
    public static Date getTargetDate(int curWeek, int targetWeek, int curDay, int targetDay) {
        Calendar resultCalender=Calendar.getInstance();
        resultCalender.setTime(new Date());
        resultCalender.add(Calendar.DATE,(targetWeek-curWeek)*7+targetDay-curDay);
        return resultCalender.getTime();
    }
    //获取今天是星期几
    public static int getTodayWeekNumber()
    {
        Date today=new Date();
        Calendar c=Calendar.getInstance();
        c.setTime(today);
        int weekday=c.get(Calendar.DAY_OF_WEEK);
        int curday;
        if(weekday==1){
            curday=7;
        }else{
            curday=weekday-1;
        }
        return curday;
    }
    public static int getCurWeek(){
        Calendar cal=Calendar.getInstance();
        cal.setTime(new Date());
        return cal.get(Calendar.WEEK_OF_YEAR);
    }
}
