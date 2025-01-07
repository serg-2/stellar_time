package com.example.stellartime;

import static java.lang.Math.PI;
import static java.lang.Math.acos;
import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import static lombok.AccessLevel.PRIVATE;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.Calendar;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public abstract class AstroUtils {
    public static double getNP(int year) {
        // Смещение времени перигелия от 01 января 00:00
        // https://ru.m.wikipedia.org/wiki/%D0%A3%D1%80%D0%B0%D0%B2%D0%BD%D0%B5%D0%BD%D0%B8%D0%B5_%D0%B2%D1%80%D0%B5%D0%BC%D0%B5%D0%BD%D0%B8
        // Данные из таблицы:
        // http://www.astropixels.com/ephemeris/perap2001.html
        return switch (year) {
            case (2023) -> 4d + 16d / 24d + 17d / (24d * 60d);
            case (2024) -> 3d + 0d / 24d + 39d / (24d * 60d);
            case (2025) -> 4d + 13d / 24d + 28d / (24d * 60d);
            case (2026) -> 3d + 17d / 24d + 16d / (24d * 60d);
            case (2027) -> 3d + 2d / 24d + 33d / (24d * 60d);
            case (2028) -> 5d + 12d / 24d + 28d / (24d * 60d);
            case (2029) -> 2d + 18d / 24d + 13d / (24d * 60d);
            case (2030) -> 3d + 10d / 24d + 12d / (24d * 60d);
            case (2031) -> 4d + 20d / 24d + 48d / (24d * 60d);
            case (2032) -> 3d + 5d / 24d + 11d / (24d * 60d);
            case (2033) -> 4d + 11d / 24d + 51d / (24d * 60d);
            case (2034) -> 4d + 4d / 24d + 47d / (24d * 60d);
            case (2035) -> 3d + 54d / (24d * 60d);
            case (2036) -> 5d + 14d / 24d + 17d / (24d * 60d);
            case (2037) -> 3d + 4d / 24d;
            case (2038) -> 3d + 5d / 24d + 1d / (24d * 60d);
            case (2039) -> 5d + 6d / 24d + 41d / (24d * 60d);
            case (2040) -> 3d + 11d / 24d + 33d / (24d * 60d);
            case (2041) -> 3d + 21d / 24d + 52d / (24d * 60d);
            case (2042) -> 4d + 9d / 24d + 7d / (24d * 60d);
            case (2043) -> 2d + 22d / 24d + 15d / (24d * 60d);
            case (2044) -> 5d + 12d / 24d + 52d / (24d * 60d);
            case (2045) -> 3d + 14d / 24d + 56d / (24d * 60d);
            case (2046) -> 3d + 58d / (24d * 60d);
            case (2047) -> 5d + 11d / 24d + 44d / (24d * 60d);
            case (2048) -> 3d + 18d / 24d + 5d / (24d * 60d);
            case (2049) -> 3d + 10d / 24d + 27d / (24d * 60d);
            case (2050) -> 4d + 19d / 24d + 35d / (24d * 60d);
            default -> 3;
        };
    }

    public static int getMoonNumber(int year) {
        int moonNumberStartYear = 2023;
        int moonNumberStartNumber = 7;
        return (((year - moonNumberStartYear) * 11) + moonNumberStartNumber) % 30;
    }

    public static double getSunInclination(int julianDay) {
        int day = julianDay - 2451545;
        // Log.e("Day", " after 1 Jan 2000: " + day);

        // средняя долгота Солнца - исправлeнная за аберрацию
        double L = (280.472 + 0.9856474 * day);// в градусах
        // Log.e("SUN", "Longitude: " + (280.472 + 0.9856474 * day));

        // средняя аномалия
        double g = (357.528 + 0.9856003 * day);// в градусах
        // Log.e("SUN", "Anomaly: " + (357.528 + 0.9856003 * day));

        // эклиптическая долгота
        double eL = L + 1.915 * sin(g * PI / 180) + 0.02 * sin(2 * g * PI / 180);
        // Log.e("SUN", "Ecliptical longitude: " + eL);

        // наклонность эклиптики
        double nakl = 23.439 - 0.0000004 * day;
        // Wiki 23.4333333
        // My   23.4353448
        // Log.e("SUN", "Наклон эклиптики: " + nakl);

        // синус склонения Солнца
        double sinIncl = sin(nakl * PI / 180) * sin(eL * (PI / 180));

        // Склонение Солнца и к градусам
        return asin(sinIncl) * 180 / PI;
    }

    public static int getJulianDay(Calendar cal) {
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1; // NB: January returns 0
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

    public static SunParameters getSunParameters(MutableLiveData<LatLng> coordinates, int timeZoneRawOffset, double eotSecs) {
        // Sun inclination
        double sunInclination = getSunInclination(getJulianDay(Calendar.getInstance()));
        // Sun Hour Angle
        double hourAngleSun = getSunHourAngle(
                sunInclination,
                coordinates.getValue().latitude
        );
        // get NOON
        LocalDateTime noon = LocalDateTime.parse("2024-01-01T12:00:00");
        // At time zone
        LocalDateTime noonAtTimeZone = noon.plus(timeZoneRawOffset, ChronoField.MILLI_OF_DAY.getBaseUnit());
        // With longitude. 3600 * 1000 / 15 = 240000
        LocalDateTime meanNoon = noonAtTimeZone.minus((long) (coordinates.getValue().longitude * 240000), ChronoField.MILLI_OF_DAY.getBaseUnit());
        // With EOT
        LocalDateTime meanNoonEot = meanNoon.minus((long) (eotSecs * 1000), ChronoField.MILLI_OF_DAY.getBaseUnit());
        LocalDateTime sunrise = meanNoonEot.minus((long) ((hourAngleSun / 15) * 3600 * 1000), ChronoField.MILLI_OF_DAY.getBaseUnit());
        LocalDateTime sunset = meanNoonEot.plus((long) ((hourAngleSun / 15) * 3600 * 1000), ChronoField.MILLI_OF_DAY.getBaseUnit());

        return new SunParameters(
                meanNoonEot,
                sunrise,
                sunset,
                sunInclination,
                hourAngleSun
        );
    }

}
