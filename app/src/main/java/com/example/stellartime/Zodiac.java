package com.example.stellartime;

import java.util.Arrays;

public abstract class Zodiac {

    public static String getTropicalSign1(int day) {
        // Западная астрология (вариант I) wiki
        int[] borders = {19, 49, 79, 109, 140, 171, 203, 234, 265, 295, 325, 355, 366};

        String[] zodiacStrings = {"Capricorn", "Aquarius", "Pisces", "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo", "Libra", "Scorpio", "Sagittarius", "Capricorn"};

        int index = Arrays.binarySearch(borders, day);
        if (index < 0) index = -index - 1;

        return zodiacStrings[index];
    }

    public static String getTropicalSign2(int day) {
        // Западная астрология (вариант II) wiki
        int[] borders = {20, 50, 79, 110, 141, 172, 203, 233, 266, 296, 326, 356, 366};

        String[] zodiacStrings = {"Capricorn", "Aquarius", "Pisces", "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo", "Libra", "Scorpio", "Sagittarius", "Capricorn"};

        int index = Arrays.binarySearch(borders, day);
        if (index < 0) index = -index - 1;

        return zodiacStrings[index];
    }

    public static String getAstronomicalSign(int day) {
        int[] borders = {18, 46, 70, 108, 133, 170, 201, 221, 258, 303, 326, 333, 351, 366};

        String[] zodiacStrings = {"Sagittarius", "Capricorn", "Aquarius", "Pisces", "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo", "Libra", "Scorpio", "Ophiuchus", "Sagittarius"};

        int index = Arrays.binarySearch(borders, day);
        if (index < 0) index = -index - 1;

        return zodiacStrings[index];
    }

    public static String getSiderealSign(int day) {
        int[] borders = {13, 42, 71, 103, 134, 165, 195, 226, 258, 288, 318, 348, 366};
        String[] zodiacStrings = {"Sagittarius", "Capricorn", "Aquarius", "Pisces", "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo", "Libra", "Scorpio", "Sagittarius"};

        int index = Arrays.binarySearch(borders, day);
        if (index < 0) index = -index - 1;

        return zodiacStrings[index];
    }

    public static String getMoonZodiac(double moonEclipticLongitude) {
        if (moonEclipticLongitude < 33.18) {
            return "Pisces";
        } else if (moonEclipticLongitude < 51.16) {
            return "Aries";
        } else if (moonEclipticLongitude < 93.44) {
            return "Taurus";
        } else if (moonEclipticLongitude < 119.48) {
            return "Gemini";
        } else if (moonEclipticLongitude < 135.30) {
            return "Cancer";
        } else if (moonEclipticLongitude < 173.34) {
            return "Leo";
        } else if (moonEclipticLongitude < 224.17) {
            return "Virgo";
        } else if (moonEclipticLongitude < 242.57) {
            return "Libra";
        } else if (moonEclipticLongitude < 271.26) {
            return "Scorpio";
        } else if (moonEclipticLongitude < 302.49) {
            return "Sagittarius";
        } else if (moonEclipticLongitude < 311.72) {
            return "Capricorn";
        } else if (moonEclipticLongitude < 348.58) {
            return "Aquarius";
        }
        return "Pisces";
    }
}
