'use client';

import {useEffect} from 'react';
import {useRouter} from 'next/navigation';
import {useAuth} from '@/store/authStore';

export default function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const router = useRouter();
  const { user, isAuthenticated, hasHydrated, hasRole } = useAuth();

  useEffect(() => {
    if (!hasHydrated) return;

    if (!isAuthenticated) {
      alert('로그인이 필요합니다.');
      router.push('/login');
      return;
    }

    if (!hasRole(['MASTER', 'MANAGER'])) {
      alert('관리자 권한이 없습니다.');
      router.push('/');
      return;
    }
  }, [isAuthenticated, hasHydrated, hasRole, router, user]);

  if (!hasHydrated || !isAuthenticated || !hasRole(['MASTER', 'MANAGER'])) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">로딩 중...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {children}
      </div>
    </div>
  );
}
