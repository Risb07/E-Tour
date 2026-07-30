package com.etour.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.etour.entity.Customer;
import com.etour.exception.ResourceNotFoundException;
import com.etour.repository.CustomerRepository;
import com.etour.service.CustomerService;

@Service
public class CustomerServiceImpl implements CustomerService {

private final CustomerRepository customerRepository;
	
	public CustomerServiceImpl(CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}
	
	
	public List<Customer> getAllCustomer(){
		return customerRepository.findAll();
	}
	
	public Customer createCustomer(Customer customer) {
		return customerRepository.save(customer);
	}
	
	public Customer getById( long id) {
		return customerRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Customer Not Found"));
	}
	
	public Customer updateCustomer( long id, Customer customerDetails) {
		Customer customer = customerRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Customer Not Found"));

		customer.setFullName(customerDetails.getFullName());
	    customer.setEmail(customerDetails.getEmail());
	    customer.setPhone(customerDetails.getPhone());

	    return customerRepository.save(customer);
	}
	
	public void deleteCustomer( long id) {
		customerRepository.deleteById(id);
	}
	
}
