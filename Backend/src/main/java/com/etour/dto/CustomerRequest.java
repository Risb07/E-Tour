package com.etour.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class CustomerRequest {

	@NotNull(message = "userId is required")
	private Long userId;

	@NotBlank(message = "fullName is required")
	private String fullName;

	@NotBlank(message = "Email is required")
	@Email(message = "Email not upto standards")
	private String email;

	@NotNull(message = "Phone number is required")
	@Pattern(regexp = "^[0-9]{10}$", message = "Phone number must contain exactly 10 digits")
	private String phone;

	public CustomerRequest() {
	}

	public CustomerRequest(Long userId, String fullName, String email, String phone) {
		this.userId = userId;
		this.fullName = fullName;
		this.email = email;
		this.phone = phone;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
}
