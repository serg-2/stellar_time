package com.example.stellartime;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public abstract class Constants {
    public static final int ageOfGSTSecs = 10;
    public static final int ageOfEOTSecs = 60;
    public static final int updateGpsTimeSeconds = 60;
    public static final int updateClockTimeMillis = 50;

    public static final String dateTimeFormatterString = "HH:mm:ss.S";
    public static final String dateTimeFormatterStringWhole = "HH:mm:ss";

    //public static final double naklonElkiptikiKEkvatoru = 23.439291111;

}
