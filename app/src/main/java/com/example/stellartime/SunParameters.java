package com.example.stellartime;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SunParameters {
    private final LocalDateTime noon;
    private final LocalDateTime sunrise;
    private final LocalDateTime sunset;
    private final double sunInclination;
    private final double hourAngleSun;
}
