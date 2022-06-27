package com.example.stellartime;

import static com.example.stellartime.utils.getClockString;
import static com.example.stellartime.utils.getEOT;
import static com.example.stellartime.utils.getGST;
import static com.example.stellartime.utils.getJD;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.TextView;

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
    private TextView eot;
    private TextView tile9;

    private final MutableLiveData<LatLng> latLng = new MutableLiveData<>(new LatLng(55.5, 36.63));
    private final MutableLiveData<Boolean> locationAvailable = new MutableLiveData<>(false);

    private Timer timer;
    private boolean timerState = false;

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH);

    // Location
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationClient;
    private Long lastKnownLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        localtime = findViewById(R.id.localtime);
        gmttime = findViewById(R.id.gmttime);
        coordinates = findViewById(R.id.coordinates);
        msolartime = findViewById(R.id.msolartime);
        tsolartime = findViewById(R.id.tsolartime);
        lstime = findViewById(R.id.lstime);
        gstime = findViewById(R.id.gstime);
        eot = findViewById(R.id.eot);
        tile9 = findViewById(R.id.tile9);

        // Location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();

        // 15 second
        locationRequest.setInterval(15 * 1000);
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                List<Location> locationList = locationResult.getLocations();
                if (locationList.size() > 0) {
                    //The last location in the list is the newest
                    Location location = locationList.get(locationList.size() - 1);

                    // Log.i("Stellar Time", "Location: " + location.getLatitude() + " " + location.getLongitude());
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
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    onTimeChanged();
                }
            }, 0, 200);//put here time 1000 milliseconds=1 second
            timerState = true;
        }
    }

    private void onTimeChanged() {
        // OffsetDateTime odt = OffsetDateTime.of(LocalDate.of(2013, 1, 4), LocalTime.of(15, 51, 45),
        //        ZoneOffset.ofHoursMinutes(5, 30));
        // gmttime.setText(new StringBuilder().append("GMT:\n").append(dtf.format(odt)).toString());

        // TIME 1. Local --------------------------------------------------
        LocalDateTime time = LocalDateTime.now();

        // TIME 2. GMT time -----------------------------------------------
        LocalDateTime gtime = time.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

        // TIME 3. Mean Solar time ----------------------------------------
        LocalDateTime ntime = gtime.plusSeconds((long) ((latLng.getValue().longitude / 15) * 3600));

        // TIME 4. True Solar time ----------------------------------------
        // Equation of Time
        int equationValue = (int) Math.round(getEOT(gtime));
        // True solar time
        LocalDateTime ttime = ntime.plusSeconds(equationValue);

        // TIME 5. Greenwich Sidereal time --------------------------------
        // get Julian Date (with current seconds)
        double jdate = getJD(gtime);
        // System.out.println("Julian Date = " + jdate);
        // get Greenwich Sidereal time in degrees
        double gst = getGST(jdate);

        // GST string
        String gstString = getClockString(gst % 360 / 15);

        // TIME 6. Local Sidereal time -----------------------------------
        String lstString = getClockString((gst + latLng.getValue().longitude) % 360 / 15);

        // OUTPUT ----------------------------------------------------------
        localtime.setText(new StringBuilder().append("LocalTime:\n").append(dtf.format(time)).toString());
        gmttime.setText(new StringBuilder().append("UTC:\n").append(dtf.format(gtime)).toString());

        msolartime.setText(new StringBuilder().append("Среднее солнечное время\nMean Solar time:\nClock time:\n").append(dtf.format(ntime)).toString());
        tsolartime.setText(new StringBuilder().append("Истинное солнечное время\nTrue Solar time:\nApparent solar time:\nSundial time:\n").append(dtf.format(ttime)).toString());

        eot.setText(new StringBuilder().append("EOT (NYSS): \n").append(equationValue / 60).append("m ").append(Math.abs(equationValue) % 60).append("s").toString());

        lstime.setText(new StringBuilder().append("Местное звёздное время\nLocal Sidereal Time:\n").append(lstString));
        gstime.setText(new StringBuilder().append("Гринвичское звёздное время\nGreenwich Sidereal Time:\n").append(gstString));

        // Timezone
        TimeZone timeZone = TimeZone.getDefault();
        String dst = timeZone.useDaylightTime() ? "yes" : "no";
        tile9.setText(String.format(Locale.ENGLISH, "TimeZone:\n%+d\nDaylight Savings time:\n%s", timeZone.getRawOffset() / 3600000, dst));

        // Location
        String locString = locationAvailable.getValue() ? String.format(Locale.ENGLISH, "Время с последнего определения координат: %d", Math.round(System.currentTimeMillis() - lastKnownLocation) / 1000) : "Позиционирование недоступно";
        coordinates.setText(new StringBuilder().append("Lat: ").append(latLng.getValue().latitude).append("\n").append("Long: ").append(latLng.getValue().longitude).append("\n").append(locString).toString());

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
                        // Log.i("Stellar Time", "PERMISSION ACQUIRED!");
                    }
                } else {
                    // Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

}