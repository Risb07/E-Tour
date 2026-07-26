package com.etour.service;

import com.etour.dto.RegisterRequest;
import com.etour.entity.User;

public interface UserService {

    User register(RegisterRequest request);

}