package com.paul.eventreminder.utils;

import java.util.Date;
import java.util.Random;

public class ColorPool {
    public static final String[] LIGHT_COLOUR={"#FFFFCC","#CCFFFF","#FF9966",
            "#FF6666","#FFCCCC","#FFCC99","#CCFF99","#CCFFCC","#CCCC99","#0099FF",
            "#F5F5F5","#FF9933","#FF99CC","#FF6600"};
    public static String getColor(){
        Date date=new Date();
        Random random=new Random(date.getTime());
        String colorString=LIGHT_COLOUR[(random.nextInt(LIGHT_COLOUR.length))];
        return colorString;
    }
}
