package com.example.Spot.infra.auth.security;

import java.io.IOException;
import java.util.List;

import com.example.Spot.infra.auth.security.DevPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.Spot.infra.auth.security.CustomUserDetails;
import com.example.Spot.store.domain.Role;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class DevAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String userIdHeader = request.getHeader("X-User-Id");
        String roleHeader = request.getHeader("X-Role"); // OWNER / MASTER / MANAGER

        if (userIdHeader == null || roleHeader == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Integer userId = Integer.valueOf(userIdHeader);
        String role = roleHeader.trim();

        CustomUserDetails principal = CustomUserDetails.forDev(userId, role);

        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

        var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}
