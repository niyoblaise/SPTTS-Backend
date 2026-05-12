package com.blaise.SPTTS.DTOs;

public record LoginResponse(String accessToken, String refreshToken, UserDto user) {}