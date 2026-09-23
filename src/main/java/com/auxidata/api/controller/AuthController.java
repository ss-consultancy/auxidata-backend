package com.auxidata.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auxidata.api.dto.AuthResponse;
import com.auxidata.api.dto.LoginRequest;
import com.auxidata.api.dto.RefreshRequest;
import com.auxidata.api.dto.RegisterRequest;
import com.auxidata.api.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

	@GetMapping("/test")
	public String test() {
		return "ok";
	}

	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
		log.info("Register request for email: {}", request.getEmail());
		return ResponseEntity.ok(authService.register(request));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		log.info("Login request for email: {}", request.getEmail());
		return ResponseEntity.ok(authService.login(request));
	}

	@PostMapping("/refresh")
	public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
		log.info("Token refresh request");
		return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@RequestBody RefreshRequest request) {
		log.info("Logout request");
		authService.logout(request.getRefreshToken());
		return ResponseEntity.ok().build();
	}

}
