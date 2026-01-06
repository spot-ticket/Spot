package com.example.Spot.infra.auth.jwt;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.Spot.infra.auth.security.CustomUserDetails;
import com.example.Spot.user.domain.Role;
import com.example.Spot.user.domain.entity.UserEntity;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JWTFilter extends OncePerRequestFilter {
    // jwtUtil을 주입받음
    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //request에서 Authorization 헤더를 찾음
        String authorization = request.getHeader("Authorization");

        //Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {

            System.out.println("token null");
            filterChain.doFilter(request, response);

            //조건이 해당되면 메소드 종료 (필수)
            return;
        }
        System.out.println("authorization now");



        String token = authorization.substring(7);

        //유효한 토큰만 통과
        try {
            String username = jwtUtil.getUsername(token);
            Role role = jwtUtil.getRole(token);

            UserEntity userEntity = UserEntity.forAuthentication(username, role);
            CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);

            Authentication authToken =
                    new UsernamePasswordAuthenticationToken(
                            customUserDetails,
                            null,
                            customUserDetails.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // 만료된 토큰 → 401
            System.out.println("JWT expired");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;

        } catch (Exception e) {
            // 위조 / 파싱 실패 → 401
            System.out.println("JWT invalid: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
