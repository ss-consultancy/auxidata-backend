package com.auxidata.api.service.impl;

import java.util.Set;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auxidata.api.dto.RegisterRequest;
import com.auxidata.api.entity.Role;
import com.auxidata.api.entity.User;
import com.auxidata.api.exception.AuthenticationException;
import com.auxidata.api.repository.RoleRepository;
import com.auxidata.api.repository.UserRepository;
import com.auxidata.api.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public User createUser(RegisterRequest request) {
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new AuthenticationException("Email already registered");
		}

		Role userRole = roleRepository.findByName("ROLE_USER")
				.orElseThrow(() -> new AuthenticationException("Default role not found"));

		User user = User.builder().email(request.getEmail()).password(passwordEncoder.encode(request.getPassword()))
				.firstName(request.getFirstName()).lastName(request.getLastName()).roles(Set.of(userRole)).build();

		return userRepository.save(user);
	}

	@Override
	public User findByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		UserDetails user = findByEmail(email);
		log.info(user.toString());
		return user;
	}
}