package com.example.stellartime;

import static java.lang.Math.PI;
import static java.lang.Math.acos;
import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import android.util.Log;
import android.util.Pair;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.Calendar;

public class AstroUtils {
    public static double getNP(int year) {
        switch (year) {
            case (2023):
                return 4d + 16d / 24d + 17d / (24d * 60d);
            case (2024):
                return 3d + 0d / 24d + 39d / (24d * 60d);
            case (2025):
                return 4d + 13d / 24d + 28d / (24d * 60d);
            case (2026):
                return 3d + 17d / 24d + 16d / (24d * 60d);
            case (2027):
                return 3d + 2d / 24d + 33d / (24d * 60d);
            case (2028):
                return 5d + 12d / 24d + 28d / (24d * 60d);
            case (2029):
                return 2d + 18d / 24d + 13d / (24d * 60d);
            case (2030):
                return 3d + 10d / 24d + 12d / (24d * 60d);
            case (2031):
                return 4d + 20d / 24d + 48d / (24d * 60d);
            default:
                return 3;
        }
    }

    public static int getMoonNumber(int year) {
        int moonNumberStartYear = 2023;
        int moonNumberStartNumber = 7;
        return (((year - moonNumberStartYear) * 11) + moonNumberStartNumber) % 30;
    }

    public static double getSunInclination(int julianDay) {
        int day = julianDay - 2451545;
        // Log.e("Day", " after 1 Jan 2000: " + day);

        // средняя долгота Солнца - исправлённая за аберрацию
        double L = (280.472 + 0.9856474 * day);// * (PI / 180) ;
        // Log.e("SUN", "Longtitude: " + (280.472 - 0.9856474 * day));

        // средняя аномалия
        double g = (357.528 + 0.9856003 * day);// * (PI / 180);
        // Log.e("SUN", "Anomaly: " + (357.528 + 0.9856003 * day));

        // эклиптическая долгота
        double eL = L + 1.915 * sin(g) + 0.02 * sin(2 * g);
        // Log.e("SUN", "Ecliptical longtitude: " + eL);

        // наклонность эклиптики
        double nakl = 23.439 - 0.0000004 * day;
        // Log.e("SUN", "Naklon: " + nakl);

        // синус склонения Солнца
        double sinIncl = sin(nakl * PI / 180) * sin(eL * (PI / 180));

        // Склонение Солнца и к градусам
        return 180 / PI * asin(sinIncl);
    }

    public static int getJulianDay(Calendar cal) {
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1; //Note January returns 0
        int date = cal.get(Calendar.DATE);
        return (1461 * (year + 4800 + (month - 14) / 12)) / 4
                + (367 * (month - 2 - 12 * ((month - 14) / 12))) / 12
                - (3 * ((year + 4900 + (month - 14) / 12) / 100)) / 4 + date - 32075;
    }

    public static double getSunHourAngle(double sunIncl, double latitude) {
        // cosinus sun hour angle
        double cosW = (sin(-0.0144) - sin(latitude * (PI / 180)) * sin(sunIncl * (PI / 180))) / (cos(latitude * (PI / 180)) * cos(sunIncl * (PI / 180)));

        return 180 / PI * acos(cosW);
    }

    public static SunTimes getSunriseSunset(double longitude, double hourAngleSun, int timeZoneRawOffset, double EOTOffset) {
        LocalDateTime noon = LocalDateTime.parse("2023-01-01T12:00:00");
        LocalDateTime noonAtTimeZone = noon.plus(timeZoneRawOffset, ChronoField.MILLI_OF_DAY.getBaseUnit());
        LocalDateTime meanNoon = noonAtTimeZone.minus((long) (longitude * 240000), ChronoField.MILLI_OF_DAY.getBaseUnit());
        LocalDateTime meanNoonEot = meanNoon.minus((long) (EOTOffset * 1000), ChronoField.MILLI_OF_DAY.getBaseUnit());

        LocalDateTime sunrise = meanNoonEot.minus((long) ((hourAngleSun / 15) * 3600 * 1000), ChronoField.MILLI_OF_DAY.getBaseUnit());
        LocalDateTime sunset = meanNoonEot.plus((long) ((hourAngleSun / 15) * 3600 * 1000), ChronoField.MILLI_OF_DAY.getBaseUnit());

        return new SunTimes(meanNoonEot, sunrise, sunset);
    }

}
