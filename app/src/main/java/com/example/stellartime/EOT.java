package com.example.stellartime;

import static com.example.stellartime.Constants.maxAgeOfEOTSecs;
import static com.example.stellartime.Helpers.getEOT;

import android.util.Log;

import java.time.LocalDateTime;

public class EOT {
    private double eotSecs;
    private long lastEOTTimestamp;

    public EOT() {
        lastEOTTimestamp = 0;
    }

    public double getSecs(LocalDateTime gtime) {
        // check GST value is too old
        long ageOfEot = System.currentTimeMillis() - lastEOTTimestamp;
        if (Math.abs(ageOfEot / 1000) > maxAgeOfEOTSecs) {
            // Log.e("Stellar time", "Updating EOT.");
            updateEOT(gtime);
        }
        return eotSecs;
    }

    private void updateEOT(LocalDateTime gtime) {
        eotSecs = getEOT(gtime);
        lastEOTTimestamp = System.currentTimeMillis();
        // Log.d("EOT", "Value: " + eotSecs/60);
    }
}
