package com.etour.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
	
	@GetMapping
	public List<Customer> getAllCustomer(){
		return customerService.getAllCustomer();
	}
	
	@PostMapping
	public Customer createCustomer(@Valid @RequestBody Customer customer) {
		return customerService.createCustomer(customer);
	}
	
	@GetMapping("/{id}")
	public Customer getById(@PathVariable("id") long id) {
		return customerService.getById(id);
	}
	
	@PutMapping("/{id}")
	public Customer updateCustomer(@PathVariable("id") long id,@Valid @RequestBody Customer customerDetails) {
		return customerService.updateCustomer(id, customerDetails);
	}
	
	@DeleteMapping("/{id}")
	public void deleteCustomer(@PathVariable("id") long id) {
		customerService.deleteCustomer(id);
	}
	
}
