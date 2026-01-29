import axios from 'axios';
import Cookies from 'js-cookie';
import api from './api';
import type {ApiResponse, JoinRequest, LoginRequest, LoginResponse, User} from '@/types';

export type Role = 'CUSTOMER' | 'OWNER' | 'CHEF' | 'MANAGER' | 'MASTER';

export const authApi = {
  // 로그인 - Next.js 프록시를 통해 요청 (CORS 우회)
  login: async (data: LoginRequest): Promise<LoginResponse> => {
    console.log('로그인 요청:', { username: data.username, password: '***' });

    try {
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
        localStorage.setItem('accessToken', accessToken); // localStorage에도 저장
      }

      // 바디에서도 토큰 추출
      if (response.data.accessToken) {
        Cookies.set('accessToken', response.data.accessToken, { expires: 1 / 48 });
        localStorage.setItem('accessToken', response.data.accessToken); // localStorage에도 저장
      }
      if (response.data.refreshToken) {
        Cookies.set('refreshToken', response.data.refreshToken, { expires: 14 });
        localStorage.setItem('refreshToken', response.data.refreshToken); // localStorage에도 저장
      }

      return response.data;
    } catch (error: any) {
      // 401 Unauthorized - 로그인 실패
      if (error.response?.status === 401) {
        const errorMessage = error.response?.data?.message || '아이디 또는 비밀번호가 올바르지 않습니다.';
        throw new Error(errorMessage);
      }

      // 기타 네트워크 에러
      if (error.code === 'ECONNREFUSED' || error.message.includes('Network Error')) {
        throw new Error('서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.');
      }

      // 기타 에러
      throw new Error(error.response?.data?.message || '로그인 중 오류가 발생했습니다.');
    }
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
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
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
    const response = await api.get<User>(`/api/users/${userId}`);
    return response.data;
  },

  // 토큰에서 사용자 정보 파싱 (JWT 디코딩)
  // lib/auth.ts 내 parseToken 수정
  parseToken: (token: string): { userId: number; role: Role } | null => {
    try {
      const payload = token.split('.')[1];
      // UTF-8 디코딩 대응 및 JSON 파싱
      const decoded = JSON.parse(decodeURIComponent(escape(window.atob(payload))));

      console.log('[authApi] 디코딩된 토큰:', decoded);

      return {
        // sub가 문자열로 올 수 있으므로 숫자로 변환
        userId: Number(decoded.userId || decoded.sub), 
        role: decoded.role as Role,
      };
    } catch (error) {
      console.error('[authApi] 토큰 파싱 실패:', error);
      return null;
    }
  },

  // 로그인 상태 확인
  isAuthenticated: (): boolean => {
    return !!Cookies.get('accessToken');
  },

  // 토큰 만료 확인 (만료 5분 전이면 true 반환)
  isTokenExpiringSoon: (token: string, thresholdMinutes: number = 5): boolean => {
    try {
      const payload = token.split('.')[1];
      const decoded = JSON.parse(decodeURIComponent(escape(window.atob(payload))));
      const exp = decoded.exp;

      if (!exp) return true;

      const now = Math.floor(Date.now() / 1000);
      const timeUntilExpiry = exp - now;
      const thresholdSeconds = thresholdMinutes * 60;

      return timeUntilExpiry <= thresholdSeconds;
    } catch (error) {
      console.error('[authApi] 토큰 만료 확인 실패:', error);
      return true;
    }
  },

  // 토큰 자동 갱신 (만료가 임박하면 refresh)
  refreshIfNeeded: async (): Promise<boolean> => {
    const accessToken = Cookies.get('accessToken');
    const refreshToken = Cookies.get('refreshToken');

    if (!accessToken || !refreshToken) {
      return false;
    }

    // 만료 5분 전이면 갱신
    if (authApi.isTokenExpiringSoon(accessToken, 5)) {
      try {
        console.log('[authApi] 토큰 갱신 시작...');
        const response = await authApi.refresh(refreshToken);

        if (response.accessToken) {
          Cookies.set('accessToken', response.accessToken, { expires: 1 / 48 });
          localStorage.setItem('accessToken', response.accessToken);
        }
        if (response.refreshToken) {
          Cookies.set('refreshToken', response.refreshToken, { expires: 14 });
          localStorage.setItem('refreshToken', response.refreshToken);
        }

        console.log('[authApi] 토큰 갱신 완료');
        return true;
      } catch (error) {
        console.error('[authApi] 토큰 갱신 실패:', error);
        // 갱신 실패 시 토큰 삭제
        Cookies.remove('accessToken');
        Cookies.remove('refreshToken');
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        return false;
      }
    }

    return true;
  },
};

export default authApi;
