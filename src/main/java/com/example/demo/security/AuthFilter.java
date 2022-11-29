package com.example.demo.security;

import io.github.censodev.jauthlibcore.CanAuth;
import io.github.censodev.jauthlibcore.TokenProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class AuthFilter<T extends CanAuth> implements Filter {
    private final TokenProvider tokenProvider;
    private final Class<T> canAuthConcreteClass;

    public AuthFilter(TokenProvider tokenProvider, Class<T> canAuthConcreteClass) {
        this.tokenProvider = tokenProvider;
        this.canAuthConcreteClass = canAuthConcreteClass;
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String header = ((HttpServletRequest)request).getHeader(this.tokenProvider.getHeader());
        if (header != null && header.startsWith(this.tokenProvider.getPrefix())) {
            String token = header.replace(this.tokenProvider.getPrefix(), "");

            try {
                this.tokenProvider.validateToken(token);
                T canAuthConcrete = this.tokenProvider.getCredential(token, this.canAuthConcreteClass);
                List<SimpleGrantedAuthority> authorities = canAuthConcrete.authorities().stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
                Object principle = canAuthConcrete.principle();
                Authentication auth = new UsernamePasswordAuthenticationToken(principle, canAuthConcrete, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception var10) {
                SecurityContextHolder.clearContext();
            }

            chain.doFilter(request, response);
        } else {
            chain.doFilter(request, response);
        }
    }
}
