package com.example.cars.security;

public class AuthorizationConstants {

    public static final String
            ADMIN = "hasAuthority('ROLE_ADMIN')",
            USER = "hasAuthority('ROLE_USER')",
            USER_OR_ADMIN = "hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_USER')";
}
