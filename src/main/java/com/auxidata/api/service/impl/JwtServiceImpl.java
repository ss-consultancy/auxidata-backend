package com.auxidata.api.service.impl;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.auxidata.api.service.JwtService;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Value("${jwt.access-expiration}")
	private long accessExpiration;

	@Value("${jwt.refresh-expiration}")
	private long refreshExpiration;

	private SecretKey key;

	@PostConstruct
	public void init() {
		this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public String generateAccessToken(UserDetails user) {
		return generateToken(new HashMap<>(), user, accessExpiration);
	}

	@Override
	public String generateRefreshToken(UserDetails user) {
		return generateToken(new HashMap<>(), user, refreshExpiration);
	}

	private String generateToken(Map<String, Object> claims, UserDetails user, long expiration) {
		return Jwts.builder().claims(claims).subject(user.getUsername()).issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + expiration)).signWith(key, SignatureAlgorithm.HS256)
				.compact();
	}

	@Override
	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public <T> T extractClaim(String token, Function<Claims, T> resolver) {
		Claims claims = extractAllClaims(token);
		return resolver.apply(claims);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
	}

	@Override
	public boolean isTokenValid(String token, UserDetails user) {
		final String username = extractUsername(token);
		return (username.equals(user.getUsername())) && !isTokenExpired(token);
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	@Override
	public long getAccessTokenExpirationMillis() {
		return accessExpiration;
	}

	@Override
	public long getRefreshTokenExpirationMillis() {
		return refreshExpiration;
	}
}