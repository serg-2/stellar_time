package com.example.stellartime;

import static com.example.stellartime.Zodiac.getSiderealSign;
import static com.example.stellartime.Zodiac.getTropicalSign1;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ZodiacTest {

    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH);

    @Test
    public void siderealZodiacTest() throws ParseException {
        assertEquals(
                "Pisces",
                getSiderealSign(getDay("Apr 13 2025")));
        assertEquals(
                "Aries",
                getSiderealSign(getDay("Apr 14 2025")));
        assertEquals(
                "Sagittarius",
                getSiderealSign(getDay("Dec 15 2025")));
        assertEquals(
                "Sagittarius",
                getSiderealSign(getDay("Dec 31 2025")));
        assertEquals(
                "Sagittarius",
                getSiderealSign(getDay("Jan 01 2025")));
        assertEquals(
                "Sagittarius",
                getSiderealSign(getDay("Jan 13 2025")));
        assertEquals(
                "Capricorn",
                getSiderealSign(getDay("Jan 14 2025")));
    }

    @Test
    public void tropical1ZodiacTest() throws ParseException {
        assertEquals(
                "Pisces",
                getTropicalSign1(getDay("Mar 20 2025")));
        assertEquals(
                "Aries",
                getTropicalSign1(getDay("Mar 21 2025")));
        assertEquals(
                "Capricorn",
                getTropicalSign1(getDay("Dec 22 2025")));
        assertEquals(
                "Capricorn",
                getTropicalSign1(getDay("Dec 31 2025")));
        assertEquals(
                "Capricorn",
                getTropicalSign1(getDay("Jan 01 2025")));
        assertEquals(
                "Capricorn",
                getTropicalSign1(getDay("Jan 19 2025")));
        assertEquals(
                "Aquarius",
                getTropicalSign1(getDay("Jan 20 2025")));
    }

    public int getDay(String day) throws ParseException {
        calendar.setTime(sdf.parse(day));
        return calendar.get(Calendar.DAY_OF_YEAR);
    }
}