package com.example.cars.user.service;

import com.example.cars.error.NotFoundException;
import com.example.cars.user.persistence.Role;
import com.example.cars.user.persistence.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Role getRole(Long id){
        return roleRepository.findById(id).orElseThrow(()->new NotFoundException("Role with id " + id + " not found"));
    }

}
