import axios from 'axios';
import Cookies from 'js-cookie';
import api from './api';
import type { ApiResponse, LoginRequest, LoginResponse, JoinRequest, User } from '@/types';

export const authApi = {
  // 로그인 - Next.js 프록시를 통해 요청 (CORS 우회)
  login: async (data: LoginRequest): Promise<LoginResponse> => {
    console.log('로그인 요청:', { username: data.username, password: '***' });

    // Next.js rewrite를 통해 프록시됨 (/api/* -> localhost:8080/api/*)
    const response = await axios.post<LoginResponse>(
      '/api/login',
      {
        username: data.username,
        password: data.password,
      },
      {
        headers: {
          'Content-Type': 'application/json',
        },
      }
    );

    // 헤더에서 토큰 추출 (LoginFilter 응답 형식)
    const authHeader = response.headers['authorization'];
    if (authHeader) {
      const accessToken = authHeader.replace('Bearer ', '');
      Cookies.set('accessToken', accessToken, { expires: 1 / 48 }); // 30분
    }

    // 바디에서도 토큰 추출
    if (response.data.accessToken) {
      Cookies.set('accessToken', response.data.accessToken, { expires: 1 / 48 });
    }
    if (response.data.refreshToken) {
      Cookies.set('refreshToken', response.data.refreshToken, { expires: 14 });
    }

    return response.data;
  },

  // 회원가입
  join: async (data: JoinRequest): Promise<ApiResponse<void>> => {
    const response = await api.post<ApiResponse<void>>('/api/join', data);
    return response.data;
  },

  // 로그아웃
  logout: async (): Promise<void> => {
    try {
      await api.post('/api/auth/logout');
    } finally {
      Cookies.remove('accessToken');
      Cookies.remove('refreshToken');
    }
  },

  // 토큰 갱신
  refresh: async (refreshToken: string): Promise<LoginResponse> => {
    const response = await api.post<ApiResponse<LoginResponse>>('/api/auth/refresh', {
      refreshToken,
    });
    return response.data.result;
  },

  // 현재 사용자 정보 조회
  getMe: async (userId: number): Promise<User> => {
    const response = await api.get<ApiResponse<User>>(`/api/users/${userId}`);
    return response.data.result;
  },

  // 토큰에서 사용자 정보 파싱 (JWT 디코딩)
  parseToken: (token: string): { userId: number; role: string } | null => {
    try {
      const payload = token.split('.')[1];
      const decoded = JSON.parse(atob(payload));

      return {
        userId: decoded.sub,
        role: decoded.role,
      };
    } catch {
      return null;
    }
  },

  // 로그인 상태 확인
  isAuthenticated: (): boolean => {
    return !!Cookies.get('accessToken');
  },
};

export default authApi;
