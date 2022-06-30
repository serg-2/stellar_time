package com.example.stellartime;

import static com.example.stellartime.consts.ageOfEOTSecs;
import static com.example.stellartime.utils.getEOT;

import java.time.LocalDateTime;

public class EOT {
    private double eot;
    private long ts;

    public EOT() {
        ts = 0;
    }

    // Result in Degrees
    public double get(LocalDateTime gtime) {
        // check GST value too old
        long ageOfEot = System.currentTimeMillis() - ts;
        if (Math.abs(ageOfEot / 1000) > ageOfEOTSecs) {
            // Log.e("Stellar time", "Updating EOT.");
            updateEOT(gtime);
        }
        return eot;
    }

    private void updateEOT(LocalDateTime gtime) {
        eot = getEOT(gtime);
        ts = System.currentTimeMillis();
    }
}
