package com.example.stellartime;

import static com.example.stellartime.AstroUtils.getNP;

import static lombok.AccessLevel.PRIVATE;

import java.time.LocalDateTime;
import java.util.Locale;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public abstract class Helpers {

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

    // Time in hours.
    public static String getClockString(double time) {
        int intHour = (int) Math.floor(time);
        double realMin = (time - (double) intHour) * 60d;
        int intMin = (int) Math.floor(realMin);
        double realSec = (realMin - (double) intMin) * 60d;
        int intSec = (int) Math.floor(realSec);
        double dSec = (realSec - (double) intSec) * 10d;

        return String.format(Locale.ENGLISH, "%02d:%02d:%02d.%d", intHour, intMin, intSec, (int) Math.floor(dSec));
    }

    public static double getGSTfromJD(double jdate) {
        double T = ((jdate - 2451545.0) / 36525);
        // Mean Sidereal time in degrees at 0 hour UTC this date
        return 280.46061837 + 360.98564736629 * (jdate - 2451545.0) + (0.000387933 * T * T) - (T * T * T / 38710000.0);
    }

    public static String getMinSecFromSec(double equationValue) {
        int mins = (int) equationValue / 60;
        return String.format(Locale.ENGLISH, "%+dm %04.1fs", mins, (float) Math.abs(equationValue) - ((Math.abs(mins) * 60)));
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
