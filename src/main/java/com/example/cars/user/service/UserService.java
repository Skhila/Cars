package com.example.cars.user.service;

import com.example.cars.error.NotFoundException;
import com.example.cars.user.model.UserRequest;
import com.example.cars.user.persistence.AppUser;
import com.example.cars.user.persistence.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    public void createUser(UserRequest userRequest) {
        AppUser appUser = new AppUser();
        appUser.setUsername(userRequest.getUsername());
        appUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        appUser.setRoles(userRequest.getRoleIds().stream()
                .map(roleService::getRole)
                .collect(Collectors.toSet()));
    }

    public AppUser getUser(String username) {
        return appUserRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("User with username '" + username + "' not found"));
    }
}
