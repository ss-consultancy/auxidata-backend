package com.auxidata.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.auxidata.api.entity.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
	Optional<RefreshToken> findByIdAndRevokedFalse(String id);

	void deleteByUserId(Long userId);
}
