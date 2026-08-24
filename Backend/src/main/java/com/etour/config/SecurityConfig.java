package com.etour.config;

import org.springframework.beans.factory.annotation.Value;
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

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Value("${app.cors.allowed-origins}")
	private String allowedOrigins;

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
				.cors(Customizer.withDefaults()) // <-- ADD THIS
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth

						// Public: no token required
						.requestMatchers("/api/auth/**").permitAll()
						.requestMatchers("/api/users/register").permitAll()
						.requestMatchers("/uploads/**").permitAll()
						// Joining/leaving a mailing list doesn't need an account.
						// Everything else under /api/newsletter (the subscriber list)
						// is admin-only via @PreAuthorize and the rule further down.
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/newsletter/subscribe").permitAll()
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/newsletter/unsubscribe").permitAll()
						// Writing in via the Contact page doesn't need an account.
						// Reading the enquiry inbox does - that rule is further down,
						// alongside the other admin-only modules.
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/contact").permitAll()

						// Admin-only
						.requestMatchers("/api/roles/**").hasRole("ADMIN")
						.requestMatchers("/api/excel-upload/**").hasRole("ADMIN")
						.requestMatchers("/api/admin/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/categories/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/categories/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/tours/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/tours/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/tours/**").hasRole("ADMIN")

						// New BRD modules - writes are admin-only
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/ad-banners/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/ad-banners/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/ad-banners/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/crawling-text/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/crawling-text/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/crawling-text/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/nav-menu/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/nav-menu/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/nav-menu/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/content/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/content/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/content/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/tour-costs/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/tour-costs/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/tour-costs/**").hasRole("ADMIN")

						// Room-sharing charges - public to read (needed on the
						// booking page), admin-only to change, like all pricing.
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/room-charges/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/room-charges/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/room-charges/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/tour-schedules/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/tour-schedules/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/tour-schedules/**").hasRole("ADMIN")

						// Tour schedules: seat inventory and pricing live here, so writes
						// are ADMIN-only. Previously these fell through to
						// anyRequest().authenticated(), which let ANY logged-in customer
						// create/modify/delete schedules (and therefore seat counts
						// and prices).
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/tour-schedules/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/tour-schedules/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/tour-schedules/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/tour-schedules/**").hasRole("ADMIN")

						// Reviews: customers act on their own review only, via
						// /api/reviews/me/** (identity taken from the JWT). The generic
						// write endpoints are moderation tools and are ADMIN-only -
						// previously any authenticated customer could edit or delete
						// ANY review by id.
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/reviews/me/**").hasAnyRole("ADMIN", "CUSTOMER")
						.requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/reviews/me/**").hasAnyRole("ADMIN", "CUSTOMER")
						.requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/reviews/me/**").hasAnyRole("ADMIN", "CUSTOMER")
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/reviews/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/reviews/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/reviews/**").hasRole("ADMIN")

						// Locations: reference data - public to read, admin to change.
						.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/locations/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/locations/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/locations/**").hasRole("ADMIN")

						// Subscriber list / delete - admin only.
						.requestMatchers("/api/newsletter/**").hasRole("ADMIN")

						// Contact enquiry inbox (list, read, close, delete) - admin
						// only. The public POST above is matched first and so is
						// unaffected by this.
						.requestMatchers("/api/contact/**").hasRole("ADMIN")

						// Wishlist is customer-owned; identity always from the JWT.
						.requestMatchers("/api/wishlist/**").hasAnyRole("ADMIN", "CUSTOMER")

						// Authenticated users (either role)
						.requestMatchers("/api/bookings/**").hasAnyRole("ADMIN", "CUSTOMER")
						.requestMatchers("/api/customer/**").hasAnyRole("ADMIN", "CUSTOMER")
						.requestMatchers("/api/cart/**").hasAnyRole("ADMIN", "CUSTOMER")
						.requestMatchers("/api/passengers/**").hasAnyRole("ADMIN", "CUSTOMER")
						.requestMatchers("/api/payments/**").hasAnyRole("ADMIN", "CUSTOMER")
						.requestMatchers("/api/invoices/**").hasAnyRole("ADMIN", "CUSTOMER")
						.requestMatchers("/api/reviews/me/**").hasAnyRole("ADMIN", "CUSTOMER")

						// Public reads (browsing tours/categories doesn't need login)
						.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/categories/**").permitAll()
						.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/tours/**").permitAll()
						.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/ad-banners/**").permitAll()
						.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/crawling-text/**").permitAll()
						.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/nav-menu/**").permitAll()
						.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/content/**").permitAll()
						.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/tour-costs/**").permitAll()
						.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/room-charges/**").permitAll()
						// An anonymous visitor browsing a tour needs departure dates,
						// reviews and location names. These previously fell through to
						// anyRequest().authenticated() and 401'd for logged-out users.
						.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/tour-schedules/**").permitAll()
						.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/locations/**").permitAll()
						// ...but one customer's whole review history is not public
						// browsing data, so it stays admin-only. This MUST precede the
						// generic /api/reviews/** GET rule below to take effect.
						.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/reviews/customer/**").hasRole("ADMIN")
						.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/reviews/**").permitAll()

						.anyRequest()
						.authenticated())

				.addFilterBefore(
						jwtAuthenticationFilter,
						UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}


	@Bean
	CorsConfigurationSource corsConfigurationSource() {

		CorsConfiguration configuration = new CorsConfiguration();

		configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

		source.registerCorsConfiguration("/**", configuration);

		return source;
	}

}