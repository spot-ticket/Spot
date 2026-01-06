package com.example.Spot.infra.auth.jwt;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.Spot.infra.auth.security.CustomUserDetails;
import com.example.Spot.user.domain.Role;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    // 검증을 담당하는 부분 = authentication manager
    private final AuthenticationManager authenticationManager;

    // JWTUtil 주입
    private final JWTUtil jwtUtil;


    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        // 클라이언트 요청에서 username, password 추출
        String username = obtainUsername(request);
        String password = obtainPassword(request);

        // 스프링 시큐리티에서 username과 password를 검증하기 위해서는 token에 담아야 함
        // null - 추후에 role 등으로
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);

        //token에 담은 검증을 위한 AuthenticationManager로 전달
        return authenticationManager.authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authentication) {

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        Integer userId = principal.getUserId();

        String authority = authentication.getAuthorities().iterator().next().getAuthority();
        Role role = Role.valueOf(authority.replace("ROLE_", ""));

        // ✅ access (짧게)
        long accessExpMs = 1000L * 60 * 30; // 30분 예시
        String accessToken = jwtUtil.createJwt(userId, role, accessExpMs);

        // ✅ refresh (길게) - DB 저장 없음
        long refreshExpMs = 1000L * 60 * 60 * 24 * 14; // 14일 예시
        String refreshToken = jwtUtil.createRefreshToken(userId, refreshExpMs);

        response.setHeader("Authorization", "Bearer " + accessToken);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");

        String body = """
        {"accessToken":"%s","refreshToken":"%s"}
        """.formatted(accessToken, refreshToken);

        try {
            response.getWriter().write(body);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
