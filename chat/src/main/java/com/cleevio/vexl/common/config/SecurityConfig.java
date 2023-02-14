package com.cleevio.vexl.common.config;

import com.cleevio.vexl.common.security.filter.SecurityFilter;
import com.cleevio.vexl.common.service.SignatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;

@EnableWebSecurity
@EnableGlobalMethodSecurity(
securedEnabled = true,
prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final SignatureService signatureService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling().authenticationEntryPoint((req, rsp, e) -> rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                .and()
                .addFilterAfter(new SecurityFilter(signatureService), UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers("/api-docs/**").permitAll()
                .antMatchers("/actuator/prometheus").permitAll()
				.antMatchers("/actuator/health").permitAll()
                .antMatchers("/internal/**").permitAll()
                .anyRequest().authenticated();
    }
}
