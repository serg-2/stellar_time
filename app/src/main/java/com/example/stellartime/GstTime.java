package com.example.stellartime;

import static com.example.stellartime.Constants.maxAgeOfGSTSecs;
import static com.example.stellartime.Helpers.getGSTfromJD;
import static com.example.stellartime.Helpers.getJD;

import java.time.LocalDateTime;

public class GstTime {
    private double gst;
    private long lastGSTTimestamp;

    public GstTime() {
        lastGSTTimestamp = 0;
    }

    // Result in Degrees
    public double get(LocalDateTime gtime) {
        // check GST value too old
        long ageOfGst = System.currentTimeMillis() - lastGSTTimestamp;
        if (Math.abs(ageOfGst / 1000) > maxAgeOfGSTSecs) {
            // Log.e("Stellar time", "Updating Gst time.");
            updateGst(gtime);
            return gst;
        } else {
            // 1 second = 1.00273791552838 sidereal second
            // 1.00273791552838 * 15 / 3600 = 0.004178074648034917
            // 0.004178074648034917 / 1000 = 0.000004178074648035
            return gst + ((double) ageOfGst ) * 0.000004178074648035;
        }
    }

    private void updateGst(LocalDateTime gtime) {
        // get Julian Date (with current seconds)
        double jdate = getJD(gtime);
        // System.out.println("Julian Date = " + jdate);
        // get Greenwich Sidereal time in degrees
        gst = getGSTfromJD(jdate);
        lastGSTTimestamp = System.currentTimeMillis();
    }

}
