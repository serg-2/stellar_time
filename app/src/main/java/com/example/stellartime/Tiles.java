package com.example.stellartime;

import static com.example.stellartime.AstroUtils.getMoonNumber;
import static com.example.stellartime.Constants.dtf;
import static com.example.stellartime.Constants.dtfWhole;
import static com.example.stellartime.Helpers.getClockString;
import static com.example.stellartime.Helpers.getEOT;
import static com.example.stellartime.Helpers.getMinSecFromSec;
import static lombok.AccessLevel.PRIVATE;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.TimeZone;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public abstract class Tiles {

    public static String getTile1LocalTime(LocalDateTime time, LocalDateTime noon) {
        return String.format(
                Locale.ENGLISH,
                """
                        Local Time: %s
                        TimeZone: %+d
                        Daylight Savings Time: %s
                        Day of Year: %d
                        Астрополдень:
                        Кульминация Солнца:
                        %s""",
                dtf.format(time),
                TimeZone.getDefault().getRawOffset() / 3600000,
                TimeZone.getDefault().useDaylightTime() ? "yes" : "no",
                time.getDayOfYear(),
                dtfWhole.format(noon)
        );
    }

    public static String getTile2MeanSolarTime(LocalDateTime ntime) {
        return String.format(
                Locale.ENGLISH,
                """
                Среднее солнечное время:
                Mean Solar time:
                Hour angle of the mean Sun(+12 hours):
                %s""",
                dtf.format(ntime)
        );
    }

    public static String getTile3LocalSiderealTime(String localSiderealTime) {
        return String.format(
                Locale.ENGLISH,
                """
                Местное звёздное время:
                Прямое восхождение кульминирующего светила:
                Local (mean) Sidereal Time:
                %s""",
                localSiderealTime
        );
    }

    public static String getTile4GMTAndBeatsTime(LocalDateTime time, LocalDateTime gtime) {
        // Calculating .beats time
        LocalDateTime beatztime = time.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.ofHoursMinutes(1, 0)).toLocalDateTime();
        double beats = (beatztime.getHour() * 3600 + beatztime.getMinute() * 60 + beatztime.getSecond() + beatztime.getNano() / 1000000000d) / 86.4;

        return String.format(
                Locale.ENGLISH,
                """
                        UTC:
                        %s
                        
                        .beat time:
                        @%03.3f""",
                dtf.format(gtime),
                beats
        );
    }

    public static String getTile5TrueSolarTime(LocalDateTime trueSolarTime) {
        return String.format(
                Locale.ENGLISH,
                """
                Истинное солнечное время:
                Астрономическое время:
                True Solar time:
                Apparent solar time:
                Sundial time:
                %s""",
                dtf.format(trueSolarTime)
        );
    }

    public static String getTile6GreenwichSiderealTime(GstTime gst, LocalDateTime gmt) {
        return String.format(
                Locale.ENGLISH,
                """
                        Гринвичское звёздное время:
                        Часовой угол точки овна:
                        Greenwich (mean) Sidereal Time:
                        %s""",
                getClockString(gst.get(gmt) % 360 / 15)
        );
    }

    public static String getTile7LocationAndSun(MutableLiveData<LatLng> location, SunParameters sunTimes, String locationString) {
        return String.format(
                Locale.ENGLISH,
                """
                Lat: %3.7f
                Long: %3.7f
                %s
                Восход~: %s
                Закат~: %s""",
                location.getValue().latitude,
                location.getValue().longitude,
                locationString,
                dtfWhole.format(sunTimes.getSunrise()),
                dtfWhole.format(sunTimes.getSunset())
        );
    }

    public static String getTile8EOT(LocalDateTime gmt) {
        return String.format(
                Locale.ENGLISH,
                """
                        EOT (NYSS):
                        %s""",
                getMinSecFromSec(getEOT(gmt))
        );
    }

    public static String getTile9Text(LocalDateTime time, SunParameters sunParameters) {
        return String.format(
                Locale.ENGLISH,
                """
                        Лунное число: %d
                        Лунный день~: %d
                        Склонение солнца~: %02.2f\u00B0
                        Продолжительность дня~: %02d:%02d""",
                getMoonNumber(time.getYear()),
                (getMoonNumber(time.getYear()) + time.getDayOfMonth() + time.getMonthValue()) % 30,
                sunParameters.getSunInclination(),
                (int) Math.floor(sunParameters.getHourAngleSun() / 15 * 2),
                Math.round((sunParameters.getHourAngleSun() / 15 * 2) % 1 * 60)
        );
    }

}
