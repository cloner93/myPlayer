package com.milad.myplayer;

import androidx.databinding.InverseMethod;

public class Utilities {

    @InverseMethod("milli_to_time")
    public static String milli_to_time(int millisecond) {
        String hours = String.valueOf((int) (millisecond / (60 * 60 * 1000)));
        String minutes = String.valueOf(((int) (millisecond % (60 * 60 * 1000) / (60 * 1000))));
        String seconds = String.valueOf(((int) ((millisecond % (60 * 60 * 1000)) % (60 * 1000) / 1000)));

        if (Integer.parseInt(hours) < 10)
            hours = "0" + hours;

        if (Integer.parseInt(minutes) < 10)
            minutes = "0" + minutes;

        if (Integer.parseInt(seconds) < 10)
            seconds = "0" + seconds;

        if (Integer.parseInt(hours) > 0)
            return hours + ":" + minutes + ":" + seconds;
        else
            return minutes + ":" + seconds;
    }

}
