package com.blaise.SPTTS.DTOs;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record RatingDto(@Min(1) @Max(5) int score) {}