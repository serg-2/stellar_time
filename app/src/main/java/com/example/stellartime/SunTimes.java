package com.example.stellartime;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;

public class SunTimes {
    private LocalDateTime sunrise;
    private LocalDateTime sunset;
    private LocalDateTime noon;

    public SunTimes(LocalDateTime noon, LocalDateTime sunrise, LocalDateTime sunset) {
        this.noon = noon;
        this.sunrise = sunrise;
        this.sunset = sunset;
    }

    public TemporalAccessor getSunrise() {
        return sunrise;
    }

    public TemporalAccessor getSunset() {
        return sunset;
    }

    public TemporalAccessor getNoon() {
        return noon;
    }
}
