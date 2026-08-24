package com.etour.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.etour.dto.CustomerRequest;
import com.etour.dto.UpdateProfileRequest;
import com.etour.entity.Customer;
import com.etour.service.CustomerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

private final CustomerService customerService;
	
	public CustomerController(CustomerService customerService) {
		this.customerService = customerService;
	}
	
	// Admin only: customers are normally created via /api/users/register,
	// which also creates the linked Customer row. This exists for admin
	// back-office data entry.
	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public List<Customer> getAllCustomer(){
		return customerService.getAllCustomer();
	}
	
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Customer> createCustomer(@Valid @RequestBody CustomerRequest request) {
		return new ResponseEntity<>(customerService.createCustomer(request), HttpStatus.CREATED);
	}
	
	// Accessible to the profile's own customer or an admin (ownership is
	// enforced inside the service).
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
	public Customer getById(@PathVariable("id") long id) {
		return customerService.getById(id);
	}
	
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
	public Customer updateCustomer(@PathVariable("id") long id,@Valid @RequestBody Customer customerDetails) {
		return customerService.updateCustomer(id, customerDetails);
	}
	
	// Own-profile endpoints resolved from the JWT principal, not a path id.
	@GetMapping("/me")
	@PreAuthorize("hasRole('CUSTOMER')")
	public Customer getMyProfile() {
		return customerService.getCurrentCustomer();
	}
	
	@PutMapping("/me")
	@PreAuthorize("hasRole('CUSTOMER')")
	public Customer updateMyProfile(@Valid @RequestBody UpdateProfileRequest request) {
		return customerService.updateCurrentCustomer(request);
	}
	
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public void deleteCustomer(@PathVariable("id") long id) {
		customerService.deleteCustomer(id);
	}
	
}
