package com.cleevio.vexl.common.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class AuthenticationHolder extends UsernamePasswordAuthenticationToken {

    public AuthenticationHolder(Object principal) {
        super(principal, null, null);
    }
}
