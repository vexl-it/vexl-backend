package com.cleevio.vexl.common.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class AuthenticationHolder extends UsernamePasswordAuthenticationToken {

    public AuthenticationHolder(Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(principal, null, authorities);
    }
}
