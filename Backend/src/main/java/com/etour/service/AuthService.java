package com.etour.service;

import com.etour.dto.LoginRequest;
import com.etour.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

}