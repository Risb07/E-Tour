package com.etour.service;

import java.util.List;

import com.etour.dto.CustomerRequest;
import com.etour.dto.UpdateProfileRequest;
import com.etour.entity.Customer;

public interface CustomerService {

	public List<Customer> getAllCustomer();
	
	public Customer createCustomer(CustomerRequest request);
	
	public Customer getById( long id);
	
	public Customer updateCustomer( long id, Customer customerDetails);
	
	public void deleteCustomer( long id);

	// Own-profile operations resolved from the authenticated principal.
	public Customer getCurrentCustomer();

	public Customer updateCurrentCustomer(UpdateProfileRequest request);
	
}
