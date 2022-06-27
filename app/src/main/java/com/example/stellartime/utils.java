package com.example.stellartime;

import java.time.LocalDateTime;
import java.util.Locale;

public class utils {
    public static double getNP(int year) {
        switch (year) {
            case (2022):
                return 4d + 6d / 24d + 55d / (24d * 60d);
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

    public static double getJD(LocalDateTime utcTime) {
        int month = utcTime.getMonthValue();
        int year = utcTime.getYear();
        double day = utcTime.getDayOfMonth() + utcTime.getHour() / 24d + utcTime.getMinute() / (24d * 60d) + utcTime.getSecond() / (24d * 60d * 60d);

        if (month == 1 || month == 2) {
            year -= 1;
            month += 12;
        }

        int a = (int) Math.floor(year / 100d);
        int b = 2 - a + ((int) Math.floor((double) a / 4d));
        return ((int) Math.floor(365.25 * (year + 4716))) + ((int) Math.floor(30.6001 * (month + 1))) + day + b - 1524.5;
    }

    public static String getClockString(double time) {
        int intHour = (int) Math.floor(time);
        double realMin = (time - (double) intHour) * 60d;
        int intMin = (int) Math.floor(realMin);
        int intSec = (int) Math.round((realMin - (double) intMin) * 60);

        return String.format(Locale.ENGLISH, "%02d:%02d:%02d", intHour, intMin, intSec);
    }

    public static double getGST(double jdate) {
        double T = ((jdate - 2451545.0) / 36525);
        // Mean Sidereal time in degrees at 0 hour UTC this date
        return 280.46061837 + 360.98564736629 * (jdate - 2451545.0) + (0.000387933 * T * T) - (T * T * T / 38710000.0);
    }

    public static double getEOT(LocalDateTime utcTime) {
        // Day of Year
        double N = utcTime.getDayOfYear() + utcTime.getHour() / 24d + utcTime.getMinute() / (24d * 60d) + utcTime.getSecond() / (24d * 3600d);

        // Day correction
        N = N - 1;

        // склонение Земли в радианах
        double lambda = 23.4372d * Math.PI / 180d;
        // Угловая скорость полного оборота
        double omega = 2d * Math.PI / 365.25636;

        // Угол (средний). Число дней + 10, так как Солнечный год начинается 21 декабря.
        double alpha = omega * ((N + 10d) % 365d);

        // Дата перигея по справочнику: http://www.astropixels.com/ephemeris/perap2001.html от 1 января.
        double np = getNP(utcTime.getYear());

        // угол элиптической орбиты от перигея (радианы)
        // под которым Земля движется от точки солнцестояния до угла даты D, поправку первого порядка на эксцентриситет Земли по орбите
        double beta = alpha + 0.03340560188317d * Math.sin(omega * ((N - np + 365d) % 365d));
        // угловая коррекция
        // разница между углами, перемещаемыми со средней скоростью, и со скорректированной скоростью, проецируемой на экваториальную плоскость, и деленными на 180, чтобы получить разницу в «полуоборотах».
        double gamma = (alpha - Math.atan(Math.tan(beta) / Math.cos(lambda))) / Math.PI;

        // EOT in seconds.
        return 43200d * (gamma - Math.round(gamma));
    }

}
