package com.etour.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.etour.entity.User;

public interface UserRepository extends JpaRepository<User, Long>{

	@Query("""
			SELECT u
			FROM User u
			JOIN FETCH u.role
			WHERE u.email = :email
			""")
			Optional<User> findByEmail(@Param("email") String email);

    boolean existsByEmail(String email);

}