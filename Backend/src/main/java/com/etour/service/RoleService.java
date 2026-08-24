package com.etour.service;

import java.util.List;

import com.etour.entity.Role;

public interface RoleService {

    Role save(Role role);

    List<Role> getAllRoles();

    Role getRoleById(Long id);
}