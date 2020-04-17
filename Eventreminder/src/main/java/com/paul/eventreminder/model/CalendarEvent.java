package com.paul.eventreminder.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Bean类
 * 事件处理
 * */
public class CalendarEvent implements Serializable {
    //总结
    String summary;
    //内容
    String content;
    //地点
    String loc;
    //周次
    List<Integer> weekList;
    //周几
    int dayOfWeek;
    //开始时间
    String startTime;
    //结束时间
    String endTime;

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public List<Integer> getWeekList() {
        return weekList;
    }

    public void setWeekList(List<Integer> weekList) {
        this.weekList = weekList;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalendarEvent that = (CalendarEvent) o;
        return dayOfWeek == that.dayOfWeek &&
                Objects.equals(summary, that.summary) &&
                Objects.equals(content, that.content) &&
                Objects.equals(loc, that.loc) &&
                Objects.equals(weekList, that.weekList) &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(endTime, that.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(summary, content, loc, weekList, dayOfWeek, startTime, endTime);
    }
}
