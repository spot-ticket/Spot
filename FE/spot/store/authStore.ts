import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import Cookies from 'js-cookie';
import type { User, Role } from '@/types';

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  hasHydrated: boolean;
  
  actions: {
    setUser: (user: User | null) => void;
    logout: () => void;
    setHasHydrated: (value: boolean) => void;
  };
  // Selectors (상태 기반 계산 함수)
  hasRole: (roles: Role[]) => boolean;
}

// SSR 환경에서 안전한 스토리지 생성
const getStorage = () => {
  if (typeof window !== 'undefined') {
    return window.localStorage;
  }
  // SSR 환경에서는 빈 스토리지 반환
  return {
    getItem: () => null,
    setItem: () => {},
    removeItem: () => {},
  };
};

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      isAuthenticated: false,
      isLoading: true,
      hasHydrated: false,

      actions: {
        setUser: (user) => {
          console.log('[Debug] Store - setUser 호출됨:', user);
          set({ user, isAuthenticated: !!user, isLoading: false });
        },
        logout: () => {
          console.log('[Debug] Store - logout 호출됨');
          Cookies.remove('accessToken');
          Cookies.remove('refreshToken');
          set({ user: null, isAuthenticated: false });

          // localStorage에서도 완전히 제거
          if (typeof window !== 'undefined') {
            localStorage.removeItem('auth-storage');
          }
        },
        setHasHydrated: (value) => {
          console.log('[Debug] Store - Hydration 상태 변경:', value);
          set({ hasHydrated: value });
        },
      },

      hasRole: (roles) => {
        const user = get().user;
        if (!user) return false;
        return roles.includes(user.role);
      },
    }),
    {
      name: 'auth-storage',
      storage: createJSONStorage(() => getStorage()),
      partialize: (state) => ({
        user: state.user,
        isAuthenticated: state.isAuthenticated
      }),
      onRehydrateStorage: () => {
        return (hydratedState, error) => {
          if (error) {
            console.error('[Debug] Store - Rehydration 에러:', error);
          }
          console.log('[Debug] Store - 저장소에서 복구된 데이터:', hydratedState);
          // hydration 완료 후 상태 업데이트 (setTimeout으로 초기화 완료 후 실행)
          setTimeout(() => {
            useAuthStore.setState({ hasHydrated: true, isLoading: false });
          }, 0);
        };
      },
    }
  )
);

// 편의를 위한 Custom Hook (컴포넌트에서 깔끔하게 사용)
export const useAuth = () => useAuthStore((state) => state);
export const useAuthActions = () => useAuthStore((state) => state.actions);