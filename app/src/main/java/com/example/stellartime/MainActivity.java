package com.example.stellartime;

import static com.example.stellartime.AstroUtils.getJulianDay;
import static com.example.stellartime.AstroUtils.getSunParameters;
import static com.example.stellartime.Constants.MOON_PERIOD;
import static com.example.stellartime.Constants.MSG_KEY;
import static com.example.stellartime.Constants.updateClockTimeMillis;
import static com.example.stellartime.Constants.updateGpsTimeSeconds;
import static com.example.stellartime.Helpers.getClockString;
import static com.example.stellartime.Helpers.getMoonZodiac;
import static com.example.stellartime.Tiles.getTile1LocalTime;
import static com.example.stellartime.Tiles.getTile2MeanSolarTime;
import static com.example.stellartime.Tiles.getTile3LocalSiderealTime;
import static com.example.stellartime.Tiles.getTile4GMTAndBeatsTime;
import static com.example.stellartime.Tiles.getTile5TrueSolarTime;
import static com.example.stellartime.Tiles.getTile6GreenwichSiderealTime;
import static com.example.stellartime.Tiles.getTile7Location;
import static com.example.stellartime.Tiles.getTile8Solar;
import static com.example.stellartime.Tiles.getTile9EOT;
import static com.example.stellartime.Tiles.getTile10Moon;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    // Cachers
    private GstTime gst;
    private EOT eot;

    private final MutableLiveData<LatLng> location = new MutableLiveData<>(new LatLng(55.5, 36.63));
    private final MutableLiveData<Boolean> isLocationAvailable = new MutableLiveData<>(false);

    private Timer timer;
    private boolean isTimerStarted = false;

    // Location
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationClient;
    private Long lastKnownLocation;

    // Handler
    private Handler h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Turn off dark theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.activity_main);

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
                    MainActivity.this.location.postValue(new LatLng(location.getLatitude(), location.getLongitude()));
                    isLocationAvailable.postValue(true);
                    lastKnownLocation = System.currentTimeMillis();
                }
            }
        };
        // Request permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }

        // New Handler
        h = new Handler(Looper.getMainLooper()) {
            public void handleMessage(@NonNull Message msg) {
                Bundle bundle = msg.getData();
                String tileContents = bundle.getString(MSG_KEY);
                final TextView myTextView = findViewById(msg.what);
                myTextView.setText(tileContents);
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isTimerStarted) {
            timer.cancel();
            isTimerStarted = false;
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
        if (!isTimerStarted) {
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
            isTimerStarted = true;
        }
    }

    private void onTimeChanged() {
        LocalDateTime time = LocalDateTime.now();
        // UTC time -----------------------------------------------
        LocalDateTime utc = time.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

        // Sunrise, sunset, noon, inclination, sun hour angle
        SunParameters sunParameters = getSunParameters(location, TimeZone.getDefault().getRawOffset(), eot.getSecs(utc));
        // Mean Solar time ----------------------------------------
        // 3600 * 1000 / 15 = 240000
        LocalDateTime meanSolarTime = utc.plus((long) (location.getValue().longitude * 240000), ChronoField.MILLI_OF_DAY.getBaseUnit());
        // Local SiderealTime
        String localSiderealTime = getClockString((gst.get(utc) + location.getValue().longitude) % 360 / 15);
        // True Solar time ----------------------------------------
        // Equation of Time. Cast to long for plusSeconds function
        LocalDateTime trueSolarTime = meanSolarTime.plus((long) (eot.getSecs(utc) * 1000), ChronoField.MILLI_OF_DAY.getBaseUnit());

        // MOON
        double moonFullPhase = (getJulianDay() - 2451550.1) / 29.530588853;
        double moonPhase = (moonFullPhase % 1) * MOON_PERIOD;
        // Moon Zodiac. moon's ecliptic longitude
        double RP = ((getJulianDay() - 2451555.8d) / 27.321582241d) % 1;
        double DP = 2 * Math.PI * (((getJulianDay() - 2451562.2 ) / 27.55454988) % 1);
        double IP = 2 * Math.PI * (moonFullPhase % 1);
        double L = 360 * RP + 6.3 * Math.sin( DP ) + 1.3 * Math.sin(2*IP - DP) + 0.7 * Math.sin(2*IP);
        String zodiac = getMoonZodiac(L);

        // OUTPUT ----------------------------------------------------------
        sendMessage(R.id.localtime, getTile1LocalTime(time));
        sendMessage(R.id.msolartime, getTile2MeanSolarTime(meanSolarTime));
        sendMessage(R.id.lstime, getTile3LocalSiderealTime(localSiderealTime));
        sendMessage(R.id.gmttime, getTile4GMTAndBeatsTime(time, utc));
        sendMessage(R.id.tsolartime, getTile5TrueSolarTime(trueSolarTime));
        sendMessage(R.id.gstime, getTile6GreenwichSiderealTime(gst, utc));
        sendMessage(R.id.coordinates, getTile7Location(location, getLocationString()));
        sendMessage(R.id.tsolaradd, getTile8Solar(sunParameters));
        sendMessage(R.id.eot, getTile9EOT(utc, sunParameters.getSunInclination()));
        sendMessage(R.id.moon, getTile10Moon(time, moonPhase, zodiac));
    }

    private String getLocationString() {
        return Boolean.TRUE.equals(isLocationAvailable.getValue()) ?
                String.format(
                        Locale.ENGLISH,
                        "Время с последнего определения координат: %d",
                        Math.round(System.currentTimeMillis() - lastKnownLocation) / 1000) :
                "Позиционирование недоступно";
    }

    private void sendMessage(int viewId, String localTimeText) {
        Message msg = h.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString(MSG_KEY, localTimeText);
        msg.what = viewId;
        msg.setData(bundle);
        h.sendMessage(msg);
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
