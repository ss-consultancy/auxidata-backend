package com.auxidata.api.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
	String generateAccessToken(UserDetails user);

	String generateRefreshToken(UserDetails user);

	String extractUsername(String token);

	boolean isTokenValid(String token, UserDetails user);

	long getAccessTokenExpirationMillis();

	long getRefreshTokenExpirationMillis();
}