import axios, {AxiosError, InternalAxiosRequestConfig} from 'axios';
import Cookies from 'js-cookie';

// Next.js rewrite 프록시를 사용하므로 baseURL 없이 상대 경로 사용
export const api = axios.create({
  headers: {
    'Content-Type': 'application/json',
  },
});

// 요청 인터셉터 - 토큰 추가 (토큰이 있을 때만)
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = Cookies.get('accessToken');
    // 토큰이 있으면 헤더에 추가, 없어도 요청은 진행
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 401 에러 시 로그인 리다이렉트를 하지 않을 경로들
const publicPaths = ['/api/stores', '/api/categories', '/api/menus'];

const isPublicPath = (url: string | undefined): boolean => {
  if (!url) return false;
  return publicPaths.some((path) => url.startsWith(path));
};

// 응답 인터셉터 - 토큰 만료 처리
api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    // 공개 경로는 401 에러가 발생해도 로그인 리다이렉트 하지 않음
    if (error.response?.status === 401 && isPublicPath(originalRequest.url)) {
      console.log('[API] 공개 경로 401 에러 - 로그인 없이 진행');
      return Promise.reject(error);
    }

    // 401 에러이고 재시도하지 않았을 때만 처리
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      const refreshToken = Cookies.get('refreshToken');
      if (refreshToken) {
        try {
          console.log('[API] 토큰 갱신 시도...');
          const response = await axios.post('/api/auth/refresh', {
            refreshToken,
          });

          const { accessToken, refreshToken: newRefreshToken } = response.data.result;

          Cookies.set('accessToken', accessToken, { expires: 1 / 48 }); // 30분
          Cookies.set('refreshToken', newRefreshToken, { expires: 14 }); // 14일
          localStorage.setItem('accessToken', accessToken);
          localStorage.setItem('refreshToken', newRefreshToken);

          if (originalRequest.headers) {
            originalRequest.headers.Authorization = `Bearer ${accessToken}`;
          }

          console.log('[API] 토큰 갱신 성공 - 요청 재시도');
          return api(originalRequest);
        } catch (refreshError) {
          // 리프레시 토큰도 만료된 경우
          console.log('[API] 토큰 갱신 실패 - 로그인 페이지로 이동');
          Cookies.remove('accessToken');
          Cookies.remove('refreshToken');
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          localStorage.removeItem('auth-storage');

          // 현재 페이지가 로그인 페이지가 아닐 때만 리다이렉트
          if (typeof window !== 'undefined' && !window.location.pathname.includes('/login')) {
            window.location.href = '/login';
          }
        }
      } else {
        // refreshToken이 없는 경우
        console.log('[API] Refresh 토큰 없음');
        // 공개 경로가 아니고 로그인 페이지가 아닐 때만 리다이렉트
        if (!isPublicPath(originalRequest.url) && typeof window !== 'undefined' && !window.location.pathname.includes('/login')) {
          window.location.href = '/login';
        }
      }
    }

    return Promise.reject(error);
  }
);

export default api;
