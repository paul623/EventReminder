package com.paul.eventreminder;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.paul.eventreminder.model.CalendarEvent;
import com.paul.eventreminder.utils.TimeUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class CalendarManager {

    Context context;
    Activity activity;
    private String CALENDER_URL = "content://com.android.calendar/calendars";
    private String CALENDER_EVENT_URL = "content://com.android.calendar/events";
    private String CALENDER_REMINDER_URL = "content://com.android.calendar/reminders";

    private String CALENDARS_NAME = "课表拍拍";
    private String CALENDARS_ACCOUNT_NAME = "SimpleTools";
    private String CALENDARS_ACCOUNT_TYPE = "com.android.simpletools";
    private String CALENDARS_DISPLAY_NAME = "课表拍拍账户";

    private boolean is_alarm = false;
    private int alarm_seconds = 15;

    public CalendarManager(Activity activity, String CALENDAR_NAME) {
        this.activity = activity;
        this.CALENDARS_NAME = CALENDARS_NAME;
        this.CALENDARS_ACCOUNT_NAME = CALENDAR_NAME;
        this.CALENDARS_ACCOUNT_TYPE = activity.getPackageName();
        this.CALENDARS_DISPLAY_NAME = CALENDAR_NAME+"账户";
        context = activity.getApplicationContext();

    }

    public void init() {
        //申请权限
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_CALENDAR,
                            Manifest.permission.READ_CALENDAR}, 1);
        }
    }

    /**
     * 检查是否已经添加了日历账户，如果没有添加先添加一个日历账户再查询
     * 获取账户成功返回账户id，否则返回-1
     */
    private int checkAndAddCalendarAccount(Context context) {
        try {
            int oldId = checkCalendarAccount(context);
            if (oldId >= 0) {
                return oldId;
            } else {
                long addId = addCalendarAccount(context);
                if (addId >= 0) {
                    return checkCalendarAccount(context);
                } else {
                    return -1;
                }
            }
        } catch (Exception e) {
            //ToastTools.show(context,"请务必授予日历权限，错误信息:"+e.getMessage());
        }
        return -1;
    }

    /**
     * 检查是否存在现有账户，存在则返回账户id，否则返回-1
     */
    private int checkCalendarAccount(Context context) {
        Cursor userCursor = context.getContentResolver().query(Uri.parse(CALENDER_URL), null, null, null, null);
        try {
            if (userCursor == null) { //查询返回空值
                return -1;
            }
            int count = userCursor.getCount();
            if (count > 0) { //存在现有账户，取第一个账户的id返回
                userCursor.moveToFirst();
                return userCursor.getInt(userCursor.getColumnIndex(CalendarContract.Calendars._ID));
            } else {
                return -1;
            }
        } finally {
            if (userCursor != null) {
                userCursor.close();
            }
        }
    }

    /**
     * 返回所有的日历账户
     */
    private List<Map<String, String>> listCalendarAccount(Context context) {
        List<Map<String, String>> result = new ArrayList<>();
        Cursor userCursor = context.getContentResolver().query(Uri.parse(CALENDER_URL), null, null, null, null);
        try {
            if (userCursor != null && userCursor.getCount() > 0) {
                for (userCursor.moveToFirst(); !userCursor.isAfterLast(); userCursor.moveToNext()) {
                    String account = userCursor.getString(userCursor.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME));
                    String name = userCursor.getString(userCursor.getColumnIndex(CalendarContract.Calendars.DEFAULT_SORT_ORDER));
                    int calId = userCursor.getInt(userCursor.getColumnIndex(CalendarContract.Calendars._ID));
                    Map<String, String> map = new HashMap<>();
                    map.put("name", name);
                    map.put("account", account);
                    map.put("calId", String.valueOf(calId));
                    result.add(map);
                }
            }
        } finally {
            if (userCursor != null) {
                userCursor.close();
            }
        }
        if (result.isEmpty()) {
            long addId = addCalendarAccount(context);
            if (addId >= 0) {
                Map<String, String> map = new HashMap<>();
                map.put("name", "新增账户");
                map.put("account", "新增账户");
                map.put("calId", String.valueOf(addId));
                result.add(map);
            }
        }
        return result;
    }

    /**
     * 添加日历账户，账户创建成功则返回账户id，否则返回-1
     */
    private long addCalendarAccount(Context context) {
        TimeZone timeZone = TimeZone.getDefault();
        ContentValues value = new ContentValues();
        value.put(CalendarContract.Calendars.NAME, CALENDARS_NAME);
        value.put(CalendarContract.Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME);
        value.put(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE);
        value.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CALENDARS_DISPLAY_NAME);
        value.put(CalendarContract.Calendars.VISIBLE, 1);
        value.put(CalendarContract.Calendars.CALENDAR_COLOR, Color.BLUE);
        value.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        value.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        value.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, timeZone.getID());
        value.put(CalendarContract.Calendars.OWNER_ACCOUNT, CALENDARS_ACCOUNT_NAME);
        value.put(CalendarContract.Calendars.CAN_ORGANIZER_RESPOND, 0);

        Uri calendarUri = Uri.parse(CALENDER_URL);
        calendarUri = calendarUri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, CALENDARS_ACCOUNT_NAME)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE)
                .build();

        Uri result = context.getContentResolver().insert(calendarUri, value);
        // 获取事件ID
        long id = result == null ? -1 : ContentUris.parseId(result);
        return id;
    }

    public void setAlarm(boolean flag) {
        this.is_alarm = flag;
    }

    public void setAlarmTime(int seconds) {
        this.alarm_seconds = seconds;
    }

    private void addScheduleToCalender(Context context, int calId, CalendarEvent model, int curWeek, OnExportProgressListener listener) {
        Set<Integer> weekSet = new HashSet<>();
        weekSet.addAll(model.getWeekList());
        String startTime = model.getStartTime();
        String endTime = model.getEndTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        int thisDay = TimeUtil.getTodayWeekNumber();
        int nowIndex = 0;
        String prefix = "";

        for (Integer i : weekSet) {
            nowIndex++;
            Date date = TimeUtil.getTargetDate(curWeek, i, thisDay, model.getDayOfWeek());
            String dateString = sdf.format(date);
            try {
                Date realStartDate = sdf2.parse(dateString + " " + startTime);
                Date realEndDate = sdf2.parse(dateString + " " + endTime);
                addCalendarEvent(context, calId,
                        model.getSummary(),
                        model.getContent() + "@" + CALENDARS_ACCOUNT_NAME,
                        model.getLoc(),
                        realStartDate, realEndDate, listener);
                if (listener != null) {
                    listener.onProgress(weekSet.size(), nowIndex);
                }
            } catch (ParseException e) {
                if (listener != null) {
                    listener.onError(e.getMessage());
                }
                e.printStackTrace();
            }
        }
    }

    ;

    /**
     * 添加日历事件
     */
    private void addCalendarEvent(Context context, int calId, String title, String description, String location, Date startDate, Date endDate, OnExportProgressListener listener) {
        if (context == null) {
            if (listener != null) {
                listener.onError("context is null");
            }
            return;
        }
        if (calId < 0) { //获取账户id失败直接返回，添加日历事件失败
            //ToastTools.show(context,"添加日历账户失败，可能没有授予日历权限或者没有日历账户");
            if (listener != null) {
                listener.onError("添加日历账户失败，可能没有授予日历权限或者没有日历账户");
            }
            return;
        }

        ContentValues event = new ContentValues();
        event.put("title", title);
        event.put("description", description);
        event.put("calendar_id", calId); //插入账户的id
        event.put(CalendarContract.Events.EVENT_LOCATION, location);
        event.put(CalendarContract.Events.DTSTART, startDate.getTime());
        event.put(CalendarContract.Events.DTEND, endDate.getTime());
        event.put(CalendarContract.Events.HAS_ALARM, 0);//设置有闹钟提醒,1：有提醒
        event.put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Shanghai");//这个是时区，必须有
        Uri newEvent = context.getContentResolver().insert(Uri.parse(CALENDER_EVENT_URL), event); //添加事件
        long id = newEvent == null ? -1 : ContentUris.parseId(newEvent);
        if (id != -1&&is_alarm) {
            // 开始组装事件提醒数据
            ContentValues reminders = new ContentValues();
            // 此提醒所对应的事件ID
            reminders.put(CalendarContract.Reminders.EVENT_ID, id);
            // 设置提醒提前的时间(0：准时  -1：使用系统默认)
            reminders.put(CalendarContract.Reminders.MINUTES, 15);
            // 设置事件提醒方式为通知警报
            reminders.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                listener.onError("没有权限");
                return;
            }
            Uri uri = context.getContentResolver().insert(CalendarContract.Reminders.CONTENT_URI, reminders);
        }
        if (newEvent == null) { //添加日历事件失败直接返回
            if(listener!=null){
                listener.onError("添加日历日程失败");
            }
        }else {
            listener.onSuccess();
        }
    }

    /**
     * 删除日历事件
     */
    private void deleteCalendarEvent(Context context, String title) {
        if (context == null) {
            return;
        }
        Cursor eventCursor = context.getContentResolver().query(Uri.parse(CALENDER_EVENT_URL), null, null, null, null);
        try {
            if (eventCursor == null) { //查询返回空值
                return;
            }
            if (eventCursor.getCount() > 0) {
                //遍历所有事件，找到title跟需要查询的title一样的项
                for (eventCursor.moveToFirst(); !eventCursor.isAfterLast(); eventCursor.moveToNext()) {
                    String eventTitle = eventCursor.getString(eventCursor.getColumnIndex("title"));
                    if (!TextUtils.isEmpty(title) && title.equals(eventTitle)) {
                        int id = eventCursor.getInt(eventCursor.getColumnIndex(CalendarContract.Calendars._ID));//取得id
                        Uri deleteUri = ContentUris.withAppendedId(Uri.parse(CALENDER_EVENT_URL), id);
                        int rows = context.getContentResolver().delete(deleteUri, null, null);
                        if (rows == -1) { //事件删除失败
                            return;
                        }
                    }
                }
            }
        } finally {
            if (eventCursor != null) {
                eventCursor.close();
            }
        }
    }

    /**
     * 删除日历事件
     */
    private  void deleteCalendarSchedule(final Context context,OnExportProgressListener listener) {
        if (context == null) {
            return;
        }
        Cursor eventCursor = context.getContentResolver().query(Uri.parse(CALENDER_EVENT_URL), null, null, null, null);
        try {
            if (eventCursor == null) { //查询返回空值
                listener.onError("找不到任何事件");
                return;
            }
            if (eventCursor.getCount() > 0) {
                //遍历所有事件，找到title跟需要查询的title一样的项
                int i=0;
                for (eventCursor.moveToFirst(); !eventCursor.isAfterLast(); eventCursor.moveToNext()) {
                    i++;
                    String description = eventCursor.getString(eventCursor.getColumnIndex("description"));
                    if (!TextUtils.isEmpty(description) && description.endsWith("@"+CALENDARS_ACCOUNT_NAME)) {
                        int id = eventCursor.getInt(eventCursor.getColumnIndex(CalendarContract.Calendars._ID));//取得id
                        Uri deleteUri = ContentUris.withAppendedId(Uri.parse(CALENDER_EVENT_URL), id);
                        int rows = context.getContentResolver().delete(deleteUri, null, null);
                        if(listener!=null){
                            listener.onProgress(eventCursor.getCount(),i);
                        }
                        if (rows == -1) { //事件删除失败
                            if(listener!=null){
                                listener.onError("事件删除失败");
                            }
                            return;
                        }
                    }
                }
            }
        } finally {
            if (eventCursor != null) {
                eventCursor.close();
            }
        }
        if (listener != null) {
            listener.onSuccess();
        }
    }
    public interface OnExportProgressListener{
        void onProgress(int total, int now);
        void onError(String msg);
        void onSuccess();
    }
    /**
     * 添加日历事件
     * @param mySubject 课表
     * @param listener 监听器，实现回调
     * */
    public  void addCalendarEvent(CalendarEvent mySubject, int curWeek,OnExportProgressListener listener){
        addScheduleToCalender(context,checkAndAddCalendarAccount(context),mySubject,
                curWeek,listener);
    }
    /**
     * 添加日历事件
     * @param mySubject 课表
     * @param listener 监听器，实现回调
     * */
    public  void addCalendarEvent(CalendarEvent mySubject,OnExportProgressListener listener){
        addScheduleToCalender(context,checkAndAddCalendarAccount(context),mySubject,
                TimeUtil.getCurWeek(),listener);
    }

    /**
     * 删除日历事件
     * @param context 上下文
     * @param listener 监听器，实现回调
     * */
    public  void deleteCalendarEvent(Context context,OnExportProgressListener listener){
        deleteCalendarSchedule(context,listener);
    }
}
