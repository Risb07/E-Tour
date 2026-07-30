package com.etour.service;

import java.util.List;

import com.etour.entity.Customer;

public interface CustomerService {

	public List<Customer> getAllCustomer();
	
	public Customer createCustomer(Customer customer);
	
	public Customer getById( long id);
	
	public Customer updateCustomer( long id, Customer customerDetails);
	
	public void deleteCustomer( long id);
	
}
