package com.cleevio.vexl._support.servlet;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class MockSpringSecurityFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(
            ServletRequest req,
            ServletResponse res,
            FilterChain chain
    ) throws IOException, ServletException {
        SecurityContextHolder.getContext()
                .setAuthentication((Authentication) ((HttpServletRequest) req).getUserPrincipal());

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
        SecurityContextHolder.clearContext();
    }
}
