'use client';

import React, {useEffect} from 'react';
import {useRouter} from 'next/navigation';
import Link from 'next/link';
import {useAuthStore} from '@/store/authStore';

export default function MyPage() {
  const router = useRouter();
  const { user, isAuthenticated, hasHydrated, actions } = useAuthStore();

  useEffect(() => {
    if (hasHydrated && !isAuthenticated) {
      router.push('/login');
    }
  }, [hasHydrated, isAuthenticated, router]);

  const handleLogout = () => {
    actions.logout();
    router.push('/');
  };

  if (!hasHydrated || !user) {
    return null;
  }

  const roleLabels: Record<string, string> = {
    CUSTOMER: '일반 회원',
    OWNER: '가게 사장',
    CHEF: '요리사',
    MANAGER: '관리자',
    MASTER: '마스터',
  };

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">마이페이지</h1>

      {/* 프로필 카드 */}
      <div className="bg-white rounded-xl shadow-md p-6 mb-6">
        <div className="flex items-center gap-4 mb-6">
          <div className="w-16 h-16 bg-orange-100 rounded-full flex items-center justify-center">
            <span className="text-2xl">👤</span>
          </div>
          <div>
            <h2 className="text-xl font-bold text-gray-900">{user.nickname}</h2>
            <p className="text-gray-500">{roleLabels[user.role]}</p>
          </div>
        </div>

        <div className="space-y-4">
          <div className="flex justify-between py-3 border-b">
            <span className="text-gray-600">아이디</span>
            <span className="text-gray-900">{user.username}</span>
          </div>
          <div className="flex justify-between py-3 border-b">
            <span className="text-gray-600">이메일</span>
            <span className="text-gray-900">{user.email}</span>
          </div>
          <div className="flex justify-between py-3 border-b">
            <span className="text-gray-600">성별</span>
            <span className="text-gray-900">{user.male ? '남성' : '여성'}</span>
          </div>
          <div className="flex justify-between py-3 border-b">
            <span className="text-gray-600">나이</span>
            <span className="text-gray-900">{user.age}세</span>
          </div>
          {user.roadAddress && (
            <div className="flex justify-between py-3 border-b">
              <span className="text-gray-600">주소</span>
              <span className="text-gray-900 text-right">
                {user.roadAddress}
                {user.addressDetail && <br />}
                {user.addressDetail}
              </span>
            </div>
          )}
        </div>
      </div>

      {/* 메뉴 */}
      <div className="bg-white rounded-xl shadow-md divide-y">
        {/* CUSTOMER 전용 메뉴 */}
        {user.role === 'CUSTOMER' && (
          <>
            <Link
              href="/orders"
              className="flex items-center justify-between p-4 hover:bg-gray-50"
            >
              <div className="flex items-center gap-3">
                <span className="text-xl">📋</span>
                <span className="text-gray-900">주문 내역</span>
              </div>
              <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
            </Link>

            <Link
              href="/cart"
              className="flex items-center justify-between p-4 hover:bg-gray-50"
            >
              <div className="flex items-center gap-3">
                <span className="text-xl">🛒</span>
                <span className="text-gray-900">장바구니</span>
              </div>
              <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
            </Link>
          </>
        )}

        {/* OWNER 전용 메뉴 */}
        {user.role === 'OWNER' && (
          <Link
            href="/mypage/store"
            className="flex items-center justify-between p-4 hover:bg-gray-50"
          >
            <div className="flex items-center gap-3">
              <span className="text-xl">🏪</span>
              <span className="text-gray-900">내 가게 관리</span>
            </div>
            <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
            </svg>
          </Link>
        )}

        {/* CHEF 전용 메뉴 */}
        {user.role === 'CHEF' && (
          <Link
            href="/mypage/chef"
            className="flex items-center justify-between p-4 hover:bg-gray-50"
          >
            <div className="flex items-center gap-3">
              <span className="text-xl">👨‍🍳</span>
              <span className="text-gray-900">소속 가게 등록</span>
            </div>
            <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
            </svg>
          </Link>
        )}

        <button
          onClick={handleLogout}
          className="flex items-center justify-between p-4 hover:bg-gray-50 w-full text-left"
        >
          <div className="flex items-center gap-3">
            <span className="text-xl">🚪</span>
            <span className="text-red-500">로그아웃</span>
          </div>
        </button>
      </div>

      {/* 회원 탈퇴 */}
      <div className="mt-8 text-center">
        <button className="text-sm text-gray-400 hover:text-gray-600">
          회원 탈퇴
        </button>
      </div>
    </div>
  );
}
