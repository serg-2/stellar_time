package com.example.stellartime;

import static lombok.AccessLevel.PRIVATE;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public abstract class Constants {
    public static final int maxAgeOfGSTSecs = 10;
    public static final int maxAgeOfEOTSecs = 60;
    public static final int updateGpsTimeSeconds = 60;
    public static final int updateClockTimeMillis = 50;
    //public static final double naklonElkiptikiKEkvatoru = 23.439291111;
    public static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss.S", Locale.ENGLISH);
    public static final DateTimeFormatter dtfWhole = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH);

    public static final String MSG_KEY = "message_key";
}
