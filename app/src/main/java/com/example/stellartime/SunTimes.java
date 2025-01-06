package com.example.stellartime;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SunTimes {
    private final LocalDateTime noon;
    private final LocalDateTime sunrise;
    private final LocalDateTime sunset;
}
