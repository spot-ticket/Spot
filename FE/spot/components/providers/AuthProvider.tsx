'use client';

import {useEffect} from 'react';
import {useTokenRefresh} from '@/lib/hooks/useTokenRefresh';
import {useAuthStore} from '@/store/authStore';
import {authApi} from '@/lib/auth';
import Cookies from 'js-cookie';

/**
 * 인증 관련 전역 로직을 처리하는 Provider
 * - 토큰 자동 갱신 (10분마다)
 * - 초기 인증 상태 복원
 */
export function AuthProvider({ children }: { children: React.ReactNode }) {
  const { user, hasHydrated, actions } = useAuthStore();

  // 10분마다 토큰 갱신 체크
  useTokenRefresh(10);

  // 초기 마운트 시 토큰 유효성 검증
  useEffect(() => {
    if (!hasHydrated) return;

    const validateInitialAuth = async () => {
      const accessToken = Cookies.get('accessToken');
      const storedUser = user;

      // 토큰은 있지만 유저 정보가 없는 경우
      if (accessToken && !storedUser) {
        try {
          const tokenInfo = authApi.parseToken(accessToken);
          if (tokenInfo && tokenInfo.userId) {
            const userData = await authApi.getMe(tokenInfo.userId);
            actions.setUser(userData);
          }
        } catch (error) {
          console.error('[AuthProvider] 초기 인증 복원 실패:', error);
          actions.logout();
        }
      }
      // 유저 정보는 있지만 토큰이 없는 경우
      else if (!accessToken && storedUser) {
        console.log('[AuthProvider] 토큰 없음 - 로그아웃 처리');
        actions.logout();
      }
    };

    validateInitialAuth();
  }, [hasHydrated, user, actions]);

  return <>{children}</>;
}
