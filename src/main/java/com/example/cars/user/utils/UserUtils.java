package com.example.cars.user.utils;

import com.example.cars.error.AccessDeniedException;
import com.example.cars.error.NotFoundException;
import com.example.cars.user.persistence.AppUser;
import com.example.cars.user.persistence.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserUtils {
    private final AppUserRepository appUserRepository;

    public AppUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }

        String username = authentication.getName();

        return appUserRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("User with username '" + username + "' not found"));
    }
}
