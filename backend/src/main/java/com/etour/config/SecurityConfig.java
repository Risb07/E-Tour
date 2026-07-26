package com.etour.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.etour.security.JwtAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(
			AuthenticationConfiguration configuration) throws Exception {

		return configuration.getAuthenticationManager();
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth

						// Public APIs
						.requestMatchers(
								"/api/auth/**",
								"/api/users/register")
						.permitAll()

						// Admin only
						.requestMatchers(
								"/api/roles/**",
								"/api/categories/**",
								"/api/tours/**",
								"/api/tour-schedule/**",
								"/api/tour-cost/**",
								"/api/itinerary/**")
						.hasRole("ADMIN")

						// Customer & Admin
						.requestMatchers(
								"/api/bookings/**",
								"/api/passengers/**",
								"/api/payments/**",
								"/api/reviews/**",
								"/api/invoices/**")
						.hasAnyRole("ADMIN", "CUSTOMER")

						.anyRequest()
						.authenticated())
				.addFilterBefore(
						jwtAuthenticationFilter,
						UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

}