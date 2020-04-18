package com.paul.eventreminder.utils;

import com.paul.eventreminder.model.CalendarEvent;

import java.util.List;

import biweekly.component.VEvent;
import biweekly.property.RecurrenceRule;
import biweekly.util.Frequency;
import biweekly.util.Recurrence;

//该类主要是为了产生重复规则
public class ICSRulesHelper {
    /**
     * 判断所属类别
     * 1 未知类型（按照集合一个一个添加）
     * 2 偶数 直接取重复规则即可
     * 3 奇数 直接取重复规则即可
     * 4 普通版本 取头取尾
     * */
    private static int getRRuleID(List<Integer> integers){
        if(integers.size()==0){
            return 1;
        }
        //判断是否为奇数
        boolean flag_single=true;
        boolean flag_even=true;
        for (Integer i:integers){
            if(i%2==0){
                flag_single=false;
                break;
            }
        }
        if(flag_single){
            return 3;
        }
        for (Integer i:integers){
            if(i%2!=0){
                flag_even=false;
                break;
            }
        }
        if(flag_even){
            return 2;
        }
        int range=integers.get(0)+integers.size()-1;
        if(range!=integers.get(integers.size()-1)){
            return 1;
        }else {
            return 4;
        }
    }

    public static RecurrenceRule getRRule(CalendarEvent calendarEvent){
        int ruleID=getRRuleID(calendarEvent.getWeekList());
        switch (ruleID){
            //未知类型直接返回null
            case 2:
            case 3:
                Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY).interval(2).count(calendarEvent.getWeekList().size()).build();
                return new RecurrenceRule(recur);
            case 4:
                Recurrence recurrence = new Recurrence.Builder(Frequency.WEEKLY).interval(1).count(calendarEvent.getWeekList().size()).build();
                return new RecurrenceRule(recurrence);
            case 1:
            default:
                return null;
        }
    }
}
