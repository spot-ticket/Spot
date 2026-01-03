package com.example.Spot.infra.auth.jwt;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.Spot.infra.auth.security.CustomUserDetails;
import com.example.Spot.user.application.service.TokenService;
import com.example.Spot.user.domain.Role;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    // 검증을 담당하는 부분 = authentication manager
    private final AuthenticationManager authenticationManager;

    // JWTUtil 주입
    private final JWTUtil jwtUtil;

    // TokenService 주입
    private final TokenService tokenService;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
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

    // 로그인 성공시 실행하는 메소드 (여기서 JWT를 발급)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authentication) {
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        String username = principal.getUsername();

        String authority = authentication.getAuthorities().iterator().next().getAuthority();
        String roleName = authority.replace("ROLE_", "");
        Role role = Role.valueOf(roleName);

        // access 발급 (JWTUtil)
        String accessToken = jwtUtil.createJwt(username, role, 60 * 30L); // 만료 시간은 네 기준으로 조절

        // refresh 발급 (DB 저장)
        TokenService.RefreshIssueResult r = tokenService.issueRefresh(username);


        // JSON 응답
        response.setHeader("Authorization", "Bearer " + accessToken);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");

        String body = """
        {"accessToken":"%s","refreshToken":"%s"}
        """.formatted(accessToken, r.refreshToken());

        try {
            response.getWriter().write(body);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // 로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {

        // 로그인 실패 시 401 응답코드 반환
        response.setStatus(401);
    }

}
