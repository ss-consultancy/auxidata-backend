package com.auxidata.api.service.impl;

import java.time.Instant;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auxidata.api.dto.AuthResponse;
import com.auxidata.api.dto.LoginRequest;
import com.auxidata.api.dto.RegisterRequest;
import com.auxidata.api.entity.RefreshToken;
import com.auxidata.api.entity.User;
import com.auxidata.api.exception.AuthenticationException;
import com.auxidata.api.repository.RefreshTokenRepository;
import com.auxidata.api.service.AuthService;
import com.auxidata.api.service.JwtService;
import com.auxidata.api.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final AuthenticationManager authenticationManager;
	private final UserService userService;
	private final JwtService jwtService;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public AuthResponse register(RegisterRequest request) {
		User user = userService.createUser(request);
		String accessToken = jwtService.generateAccessToken(user);
		String refreshToken = jwtService.generateRefreshToken(user);

		saveRefreshToken(user, refreshToken);

		return AuthResponse.builder().accessToken(accessToken).refreshToken(refreshToken)
				.expiresIn(jwtService.getAccessTokenExpirationMillis() / 1000).build();
	}

	@Override
	@Transactional
	public AuthResponse login(LoginRequest request) {
		try {
			temp(request);
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

			User user = userService.findByEmail(request.getEmail());
			String accessToken = jwtService.generateAccessToken(user);
			String refreshToken = jwtService.generateRefreshToken(user);

			saveRefreshToken(user, refreshToken);

			return AuthResponse.builder().accessToken(accessToken).refreshToken(refreshToken)
					.expiresIn(jwtService.getAccessTokenExpirationMillis() / 1000).build();

		} catch (AuthenticationException e) {
			throw new AuthenticationException("Invalid email or password");
		}
	}
	
	private void temp(LoginRequest loginRequest) {
		String rawPassword = loginRequest.getPassword();

		UserDetails userDetails = userService.loadUserByUsername(
		        loginRequest.getEmail()
		);

		boolean matches = passwordEncoder.matches(
		        rawPassword,
		        userDetails.getPassword()
		);

		System.out.println("Password matches = " + matches);
		System.out.println("DB password = " + userDetails.getPassword());

	}

	@Override
	@Transactional
	public AuthResponse refreshToken(String refreshToken) {
		if (!jwtService.isTokenValid(refreshToken, null)) {
			throw new AuthenticationException("Invalid refresh token");
		}

		String email = jwtService.extractUsername(refreshToken);
		User user = userService.findByEmail(email);

		RefreshToken storedToken = refreshTokenRepository.findByIdAndRevokedFalse(refreshToken)
				.orElseThrow(() -> new AuthenticationException("Refresh token not found or revoked"));

		if (storedToken.getExpiresAt().isBefore(Instant.now())) {
			revokeRefreshToken(refreshToken);
			throw new AuthenticationException("Refresh token expired");
		}

		revokeRefreshToken(refreshToken);

		String newAccessToken = jwtService.generateAccessToken(user);
		String newRefreshToken = jwtService.generateRefreshToken(user);

		saveRefreshToken(user, newRefreshToken);

		return AuthResponse.builder().accessToken(newAccessToken).refreshToken(newRefreshToken)
				.expiresIn(jwtService.getAccessTokenExpirationMillis() / 1000).build();
	}

	@Override
	@Transactional
	public void logout(String refreshToken) {
		revokeRefreshToken(refreshToken);
	}

	private void saveRefreshToken(User user, String token) {
		RefreshToken refreshToken = RefreshToken.builder().id(token).user(user)
				.expiresAt(Instant.now().plusMillis(jwtService.getRefreshTokenExpirationMillis())).revoked(false)
				.build();

		refreshTokenRepository.save(refreshToken);
	}

	private void revokeRefreshToken(String token) {
		refreshTokenRepository.findById(token).ifPresent(t -> {
			t.setRevoked(true);
			refreshTokenRepository.save(t);
		});
	}
}