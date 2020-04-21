# EventReminder

安卓针对日历事件导出以及ics文件生成、解析封装库

[![](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![](https://img.shields.io/badge/version-0.0.6-yellow.svg)](https://bintray.com/paul623/EventReminder/eventreminder/0.0.3)

## 碎碎念

为啥要写这个库呢？

* 尝试自己写一个库调用，学习一下这个流程，为以后做准备
* 日历库在网上的资料太少了，而这个功能却又很实用
* 自己做的项目都会涉及到事件导出功能，不想重复写代码

## 食用方法

在项目中引用即可

```groovy
implementation 'com.paul.eventreminder:eventreminder:0.0.6'
```

## 使用教程



### CalendarManager

不是特别方便的初始化方法：

你需要提供Activity（用来获取权限）以及日历账户配置名称

首先声明一个Manager：

```java
CalendarManager calendarManager=new CalendarManager(this,"测试");
```

会自动请求日历写入权限，如果拒绝这一块逻辑我没有处理，你可以在自己的代码中去实现该逻辑。



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
 public  void addCalendarEvent(CalendarEvent mySubject, int curWeek,OnExportProgressListener listener)
```

当然你也可以选择不传curWeek这个参数,会自动默认添加从1月1号到当前时间过了几周

在添加事件事前，你也可以为其设置提醒

```java
 calendarManager.setAlarm(true);//开启提醒
 calendarManager.setAlarmTime(15);//15分钟
```



删除事件也很简单，直接调用delete方法即可。

```java
public void deleteCalendarEvent(OnExportProgressListener listener)
```

值得一提的是，删除判断的是事件内容末尾的@+ACCOUNT_NAME，所以请保持该名称在创建和删除时候要相同。

### ICSManager

初始化：

```java
ICSManager icsManager=new ICSManager(Context context,String userName);
```

生成的ics默认保存在私有目录下，如果要开启保存在Download路径下，请使用：

```java
icsManager.setFlag_alarm(true);//默认为开启状态
```

同Calendar一样，你需要创建对应的CalendarEvent并传入

```java
icsManager.OutPutIcsFile(String filename,boolean useRule,List<CalendarEvent> calendarEvents,int curWeek,OutPutListener listener)
```

这里有一个参数为useRule，为bool类型

true代表开启重复规则，false代表关闭

开启重复规则后，根据你提供的weeklist来进行判断

```
形如 [1,2,3,4,5,6]或者[2,4,6,8,10]或者[1,3,5,7,9]都可以支持规则导出
但如果是这种[1,2,3,4,6,8]不规则的，会自动按照重复逐一导出
```

开启提醒的方式：

```java
icsManager.setAlarm_seconds(15);
icsManager.setFlag_alarm(true);
```

在回调函数中，onSuccess方法会传回来一个生成文件路径，由于该文件是保存在包名下的私有目录，因此不需要任何读写权限。

你也可以直接调用File来处理他。

## 第三方依赖

本项目使用了[biweekly](https://github.com/mangstadt/biweekly)

## 关于

该项目为业余项目，写的比较赶，仍在完善中，后期会上传jcenter仓库

@Paul623

Powered By 巴塞罗那的余晖

博客：https://www.cnblogs.com/robotpaul/

## License

```
Copyright 2020 Paul623. https://github.com/paul623

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
 limitations under the License.
```