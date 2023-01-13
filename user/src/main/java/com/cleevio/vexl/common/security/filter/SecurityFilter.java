package com.cleevio.vexl.common.security.filter;

import com.cleevio.vexl.common.dto.ErrorResponse;
import com.cleevio.vexl.common.security.AuthenticationHolder;
import com.cleevio.vexl.common.util.NumberUtils;
import com.cleevio.vexl.module.user.service.SignatureService;
import com.cleevio.vexl.module.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

public class SecurityFilter extends OncePerRequestFilter {

    public static final String HEADER_PUBLIC_KEY = "public-key";
    public static final String HEADER_HASH = "hash";
    public static final String HEADER_SIGNATURE = "signature";
    public static final String HEADER_CRYPTO_VERSION = "crypto-version";

    private final SignatureService signatureService;
    private final UserService userService;

    public SecurityFilter(SignatureService signatureService,
                          UserService userService) {
        this.signatureService = signatureService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String requestURI = request.getRequestURI();

        final String publicKey = request.getHeader(HEADER_PUBLIC_KEY);
        final String phoneHash = request.getHeader(HEADER_HASH);
        final String signature = request.getHeader(HEADER_SIGNATURE);
        final int cryptoVersion = NumberUtils.parseIntOrFallback(request.getHeader(HEADER_CRYPTO_VERSION), 1);

        if (signature == null || publicKey == null || phoneHash == null || !requestURI.contains("/api/v1")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (signatureService.isSignatureValid(publicKey, phoneHash, signature, cryptoVersion)) {
                AuthenticationHolder authentication = userService
                        .findByPublicKey(publicKey)
                        .map(user -> {
                            AuthenticationHolder authenticationHolder = new AuthenticationHolder(user);
                            authenticationHolder.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                            return authenticationHolder;
                        })
                        .orElse(null);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                SecurityContextHolder.clearContext();
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            handleError(response, "Signature verification failed: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    protected void handleError(ServletResponse response, String s) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setStatus(500);

        ErrorResponse error = new ErrorResponse(Collections.singleton(s), "0");
        OutputStream out = response.getOutputStream();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(out, error);
        out.flush();

        throw new RuntimeException();
    }
}
