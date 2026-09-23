package com.auxidata.api.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.auxidata.api.dto.RegisterRequest;
import com.auxidata.api.entity.User;

public interface UserService extends UserDetailsService {
	User createUser(RegisterRequest request);

	User findByEmail(String email);
}
