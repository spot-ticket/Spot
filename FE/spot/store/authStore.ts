import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import Cookies from 'js-cookie';
import type { User, Role } from '@/types';

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  hasHydrated: boolean;
  setUser: (user: User | null) => void;
  setAuthenticated: (value: boolean) => void;
  setLoading: (value: boolean) => void;
  setHasHydrated: (value: boolean) => void;
  logout: () => void;
  hasRole: (roles: Role[]) => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      isAuthenticated: false,
      isLoading: true,
      hasHydrated: false,

      setUser: (user) => {
        console.log('[AuthStore] setUser 호출:', user);
        set({ user, isAuthenticated: !!user });
      },

      setAuthenticated: (value) => set({ isAuthenticated: value }),

      setLoading: (value) => set({ isLoading: value }),

      setHasHydrated: (value) => set({ hasHydrated: value }),

      logout: () => {
        Cookies.remove('accessToken');
        Cookies.remove('refreshToken');
        set({ user: null, isAuthenticated: false });
      },

      hasRole: (roles) => {
        const user = get().user;
        return user ? roles.includes(user.role) : false;
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({ user: state.user, isAuthenticated: state.isAuthenticated }),
      onRehydrateStorage: () => (state, error) => {
        console.log('[AuthStore] onRehydrateStorage 호출:', { state, error });
        if (error) {
          console.error('[AuthStore] Rehydration 에러:', error);
        }
        state?.setHasHydrated(true);
      },
    }
  )
);

export default useAuthStore;
