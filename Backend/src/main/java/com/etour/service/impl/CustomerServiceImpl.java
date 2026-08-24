package com.etour.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.etour.dto.CustomerRequest;
import com.etour.dto.UpdateProfileRequest;
import com.etour.entity.Customer;
import com.etour.entity.User;
import com.etour.exception.ResourceNotFoundException;
import com.etour.repository.CustomerRepository;
import com.etour.repository.UserRepository;
import com.etour.security.CurrentUserProvider;
import com.etour.service.CustomerService;

@Service
public class CustomerServiceImpl implements CustomerService {

private final CustomerRepository customerRepository;
private final UserRepository userRepository;
private final CurrentUserProvider currentUserProvider;
	
	public CustomerServiceImpl(CustomerRepository customerRepository,
			UserRepository userRepository,
			CurrentUserProvider currentUserProvider) {
		this.customerRepository = customerRepository;
		this.userRepository = userRepository;
		this.currentUserProvider = currentUserProvider;
	}
	
	
	public List<Customer> getAllCustomer(){
		return customerRepository.findAll();
	}
	
	@Override
	@Transactional
	public Customer createCustomer(CustomerRequest request) {

		User user = userRepository.findById(request.getUserId())
				.orElseThrow(() -> new ResourceNotFoundException(
						"User not found with id : " + request.getUserId()));

		// user_id is unique in customer; refuse if this user already has a profile.
		if (customerRepository.existsByUser_UserId(request.getUserId())) {
			throw new com.etour.exception.ResourceConflictException(
					"User already has a customer profile");
		}

		Customer customer = new Customer();
		customer.setUser(user);
		customer.setFullName(request.getFullName());
		customer.setEmail(request.getEmail());
		customer.setPhone(request.getPhone());

		return customerRepository.save(customer);
	}
	
	public Customer getById( long id) {
		Customer customer = customerRepository.findById(id)
				.orElseThrow(()-> new ResourceNotFoundException("Customer Not Found"));

		requireAdminOrOwner(customer);

		return customer;
	}
	
	public Customer updateCustomer( long id, Customer customerDetails) {
		Customer customer = customerRepository.findById(id)
				.orElseThrow(()-> new ResourceNotFoundException("Customer Not Found"));

		requireAdminOrOwner(customer);

		customer.setFullName(customerDetails.getFullName());
	    customer.setEmail(customerDetails.getEmail());
	    customer.setPhone(customerDetails.getPhone());

	    return customerRepository.save(customer);
	}
	
	public void deleteCustomer( long id) {
		customerRepository.deleteById(id);
	}

	@Override
	public Customer getCurrentCustomer() {
		return currentUserProvider.currentCustomer();
	}

	@Override
	@Transactional
	public Customer updateCurrentCustomer(UpdateProfileRequest request) {
		Customer customer = currentUserProvider.currentCustomer();
		customer.setFullName(request.getFullName());
		customer.setPhone(request.getPhone());
		return customerRepository.save(customer);
	}

	private void requireAdminOrOwner(Customer customer) {

		if (currentUserProvider.isAdmin()) {
			return;
		}

		Customer current = currentUserProvider.currentCustomer();

		if (!customer.getCustomerId().equals(current.getCustomerId())) {
			// 404 on purpose - don't reveal the existence of other profiles.
			throw new ResourceNotFoundException("Customer Not Found");
		}
	}
	
}
