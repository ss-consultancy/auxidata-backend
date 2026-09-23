package com.auxidata.api.service;

import com.auxidata.api.dto.AuthResponse;
import com.auxidata.api.dto.LoginRequest;
import com.auxidata.api.dto.RegisterRequest;

public interface AuthService {
	AuthResponse register(RegisterRequest request);

	AuthResponse login(LoginRequest request);

	AuthResponse refreshToken(String refreshToken);

	void logout(String refreshToken);
}