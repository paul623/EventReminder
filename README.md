# EventReminder

安卓针对日历事件导出以及ics文件生成、解析封装库

## 碎碎念

为啥要写这个库呢？

* 尝试自己写一个库调用，学习一下这个流程，为以后做准备
* 日历库在网上的资料太少了，而这个功能却又很实用
* 自己做的项目都会涉及到事件导出功能，不想重复写代码

## 食用方法

目前只写了日历导出的方法

不是特别方便的初始化方法：

你需要提供Activity（用来获取权限）以及日历账户配置

```
 	private String CALENDARS_NAME = "课表拍拍";
    private String CALENDARS_ACCOUNT_NAME = "SimpleTools";
    private String CALENDARS_ACCOUNT_TYPE = "com.android.simpletools";
    private String CALENDARS_DISPLAY_NAME = "课表拍拍账户";
```

首先声明一个Manager：

```java
CalendarManager calendarManager=new CalendarManager(this,"测试","测试","com.paul.test","测试账户");
```

调用其中的

```java
calendarManager.init();
```

来进行初始化操作（权限申请）

添加一个事件的时候你需要创建一个CalendarEvent 对象，或者您也可以选择继承自这个类

属性如下：

```java
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
```

这里的weeklist至关重要，因为通过该集合来控制事件的重复

考虑到部分存在[1,2,3,4,8,10]这种不规则的形式，因此采用逐一导入的方法。

开始时间和结束时间的格式为 ：“8:00”

创建好你的事件后调用addCalendarEvent方法即可，这里需要传入一个当前周次来告诉我当前所处时间位置。这个周次一定是在你提供的weekList中的。

```java
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
```

删除事件也很简单，直接调用delete方法即可。

```java
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
```

值得一提的是，删除判断的是事件内容末尾的@+ACCOUNT_NAME，所以请保持该名称在创建和删除时候要相同。

## 关于

该项目为业余项目，写的比较赶，仍在完善中，后期会上传jcenter仓库

@Paul623

Powered By 巴塞罗那的余晖

博客：https://www.cnblogs.com/robotpaul/