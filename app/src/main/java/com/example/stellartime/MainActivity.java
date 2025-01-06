package com.example.stellartime;

import static com.example.stellartime.AstroUtils.getJulianDay;
import static com.example.stellartime.AstroUtils.getMoonNumber;
import static com.example.stellartime.AstroUtils.getSunHourAngle;
import static com.example.stellartime.AstroUtils.getSunInclination;
import static com.example.stellartime.AstroUtils.getSunriseSunset;
import static com.example.stellartime.Constants.dateTimeFormatterString;
import static com.example.stellartime.Constants.dateTimeFormatterStringWhole;
import static com.example.stellartime.Constants.updateClockTimeMillis;
import static com.example.stellartime.Constants.updateGpsTimeSeconds;
import static com.example.stellartime.Helpers.getClockString;
import static com.example.stellartime.Helpers.getEOT;
import static com.example.stellartime.Helpers.getMinSecFromSec;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private TextView localtime;
    private TextView gmttime;
    private TextView coordinates;
    private TextView msolartime;
    private TextView tsolartime;
    private TextView lstime;
    private TextView gstime;
    private TextView eottime;
    private TextView tile9;

    // Cachers
    private GstTime gst;
    private EOT eot;

    private final MutableLiveData<LatLng> latLng = new MutableLiveData<>(new LatLng(55.5, 36.63));
    private final MutableLiveData<Boolean> locationAvailable = new MutableLiveData<>(false);

    private Timer timer;
    private boolean timerState = false;

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern(dateTimeFormatterString, Locale.ENGLISH);
    DateTimeFormatter dtfWhole = DateTimeFormatter.ofPattern(dateTimeFormatterStringWhole, Locale.ENGLISH);

    // Location
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationClient;
    private Long lastKnownLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Turn off dark theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.activity_main);

        localtime = findViewById(R.id.localtime);
        gmttime = findViewById(R.id.gmttime);
        coordinates = findViewById(R.id.coordinates);
        msolartime = findViewById(R.id.msolartime);
        tsolartime = findViewById(R.id.tsolartime);
        lstime = findViewById(R.id.lstime);
        gstime = findViewById(R.id.gstime);
        eottime = findViewById(R.id.eot);
        tile9 = findViewById(R.id.tile9);

        // GST constructor
        gst = new GstTime();
        eot = new EOT();

        // Location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        /* DEPRECATED
        // 15 second
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(updateGpsTimeSeconds * 1000);
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
         */

        locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                updateGpsTimeSeconds * 1000
        ).build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                List<Location> locationList = locationResult.getLocations();
                if (!locationList.isEmpty()) {
                    //The last location in the list is the newest
                    Location location = locationList.get(locationList.size() - 1);

                    Log.d("Stellar Time", "Location: " + location.getLatitude() + " " + location.getLongitude());
                    latLng.postValue(new LatLng(location.getLatitude(), location.getLongitude()));
                    locationAvailable.postValue(true);
                    lastKnownLocation = System.currentTimeMillis();
                }
            }
        };
        // Request permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (timerState) {
            timer.cancel();
            timerState = false;
        }

        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }

        // Timer
        if (!timerState) {
            timer = new Timer();

            /* DEPRECATED
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    onTimeChanged();
                }
            }, 0, updateClockTimeMillis);//put here time 1000 milliseconds=1 second
             */

            timer.schedule(new TimerTask() {
                               @Override
                               public void run() {
                                   onTimeChanged();
                               }
                           },
                    0,
                    updateClockTimeMillis //put here time 1000 milliseconds=1 second
            );
            timerState = true;
        }
    }

    private void onTimeChanged() {
        // OffsetDateTime odt = OffsetDateTime.of(LocalDate.of(2013, 1, 4), LocalTime.of(15, 51, 45),
        //        ZoneOffset.ofHoursMinutes(5, 30));
        // gmttime.setText(new StringBuilder().append("GMT:\n").append(dtf.format(odt)).toString());

        // TIME 1. Local --------------------------------------------------
        LocalDateTime time = LocalDateTime.now();
        // Calculating .beats time
        LocalDateTime beatztime = time.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.ofHoursMinutes(1, 0)).toLocalDateTime();
        double beats = (beatztime.getHour() * 3600 + beatztime.getMinute() * 60 + beatztime.getSecond() + beatztime.getNano() / 1000000000d) / 86.4;

        // TIME 2. GMT time -----------------------------------------------
        LocalDateTime gtime = time.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

        // TIME 3. Mean Solar time ----------------------------------------
        // 3600 * 1000 / 15 = 240000
        LocalDateTime ntime = gtime.plus((long) (latLng.getValue().longitude * 240000), ChronoField.MILLI_OF_DAY.getBaseUnit());

        // TIME 4. True Solar time ----------------------------------------
        // Equation of Time. Cast to long for plusSeconds function
        // True solar time
        LocalDateTime ttime = ntime.plus((long) (eot.get(gtime) * 1000), ChronoField.MILLI_OF_DAY.getBaseUnit());

        // Timezone
        TimeZone timeZone = TimeZone.getDefault();
        String dst = timeZone.useDaylightTime() ? "yes" : "no";

        // Sun Hour Angle
        double sunInclination = getSunInclination(getJulianDay(Calendar.getInstance()));
        double hourAngleSun = getSunHourAngle(
                sunInclination,
                latLng.getValue().latitude
        );

        // Sunrise, sunset, noon
        SunTimes sunTimes = getSunriseSunset(latLng.getValue().longitude, hourAngleSun, timeZone.getRawOffset(), eot.get(gtime));

        String localSiderealTime = getClockString((gst.get(gtime) + latLng.getValue().longitude) % 360 / 15);

        // Location
        String locString = Boolean.TRUE.equals(locationAvailable.getValue()) ?
                String.format(
                        Locale.ENGLISH,
                        "Время с последнего определения координат: %d",
                        Math.round(System.currentTimeMillis() - lastKnownLocation) / 1000) :
                "Позиционирование недоступно";

        // PRE-OUTPUT ------------------------------------------------------
        String localTimeText = String.format(
                Locale.ENGLISH,
                "LocalTime: %s\nTimeZone:\n%+d\nDaylight Savings time:\n%s\nDay of Year:\n%d\nАстрополдень:\nКульминация Солнца:\n%s\n",
                dtf.format(time),
                timeZone.getRawOffset() / 3600000,
                dst,
                time.getDayOfYear(),
                dtfWhole.format(sunTimes.getNoon())
        );

        String tile9Text = String.format(
                Locale.ENGLISH,
                "Лунное число: %d\nЛунный день~: %d\nСклонение солнца~: %02.2f\u00B0\nПродолжительность дня~: %02d:%02d\n",
                getMoonNumber(time.getYear()),
                (getMoonNumber(time.getYear()) + time.getDayOfMonth() + time.getMonthValue()) % 30,
                sunInclination,
                (int) Math.floor(hourAngleSun / 15 * 2),
                Math.round((hourAngleSun / 15 * 2) % 1 * 60)
        );

        // OUTPUT ----------------------------------------------------------
        runOnUiThread(() -> {
            localtime.setText(localTimeText);
            gmttime.setText(String.format(
                    Locale.ENGLISH,
                    "UTC:\n%s\n\n.beat time:\n@%03.3f\n",
                    dtf.format(gtime),
                    beats
            ));
            msolartime.setText(String.format(
                    Locale.ENGLISH,
                    "Среднее солнечное время:\nMean Solar time:\nHour angle of the mean Sun(+12 hours):\n%s",
                    dtf.format(ntime)
            ));
            tsolartime.setText(String.format(
                    Locale.ENGLISH,
                    "Истинное солнечное время:\nTrue Solar time:\nApparent solar time:\nSundial time:\n%s",
                    dtf.format(ttime)
            ));
            eottime.setText(String.format(
                    Locale.ENGLISH,
                    "EOT (NYSS): \n%s",
                    getMinSecFromSec(getEOT(gtime))
            ));
            lstime.setText(String.format(
                    Locale.ENGLISH,
                    "Местное звёздное время:\nПрямое восхождение кульминирующего светила:\nLocal (mean) Sidereal Time:\n%s",
                    localSiderealTime
            ));
            // TIME 5. Greenwich Sidereal time --------------------------------
            gstime.setText(String.format(
                    Locale.ENGLISH,
                    "Гринвичское звёздное время:\nЧасовой угол точки овна:\nGreenwich (mean) Sidereal Time:\n%s",
                    getClockString(gst.get(gtime) % 360 / 15)
            ));
            tile9.setText(tile9Text);
            coordinates.setText(String.format(
                    Locale.ENGLISH,
                    "Lat: %3.7f\nLong: %3.7f\n%s\nВосход~: %s\nЗакат~: %s\n",
                    latLng.getValue().latitude,
                    latLng.getValue().longitude,
                    locString,
                    dtfWhole.format(sunTimes.getSunrise()),
                    dtfWhole.format(sunTimes.getSunset())
            ));
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 101: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                        Log.d("Stellar Time", "PERMISSION ACQUIRED!");
                    }
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }
}
