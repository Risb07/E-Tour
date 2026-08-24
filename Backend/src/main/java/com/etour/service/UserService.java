package com.etour.service;

import com.etour.dto.RegisterRequest;
import com.etour.dto.RegisterResponse;

public interface UserService {

    RegisterResponse register(RegisterRequest request);

}