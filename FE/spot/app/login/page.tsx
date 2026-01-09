'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { authApi } from '@/lib/auth';
import { useAuthStore } from '@/store/authStore';

export default function LoginPage() {
  const { setUser } = useAuthStore();
  const [formData, setFormData] = useState({
    username: '',
    password: '',
  });
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    setError('');
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    try {
      const response = await authApi.login(formData);

      console.log('로그인 응답:', response);
      // 토큰에서 사용자 정보 파싱
      const tokenInfo = authApi.parseToken(response.accessToken);
      console.log('토큰 정보:', tokenInfo);

      if (tokenInfo) {
        try {
          const user = await authApi.getMe(tokenInfo.userId);
          console.log('사용자 정보:', user);
          setUser(user);
        } catch (userError) {
          console.error('사용자 정보 조회 실패:', userError);
          // getMe 실패해도 기본 사용자 정보로 인증 상태 유지
          setUser({
            id: tokenInfo.userId,
            username: formData.username,
            role: tokenInfo.role as 'CUSTOMER' | 'OWNER' | 'CHEF' | 'MANAGER' | 'MASTER',
            nickname: formData.username,
            email: '',
            roadAddress: '',
            addressDetail: '',
            age: 0,
            male: true,
          });
        }
      }

      // 상태가 localStorage에 저장된 후 페이지 이동 (새로고침으로 hydration 보장)
      window.location.href = '/';
    } catch (err) {
      console.error('Login error:', err);
      setError('아이디 또는 비밀번호가 올바르지 않습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-[80vh] flex items-center justify-center px-4">
      <div className="w-full max-w-md">
        <div className="bg-white rounded-2xl shadow-lg p-8">
          <div className="text-center mb-8">
            <h1 className="text-3xl font-bold text-gray-900">로그인</h1>
            <p className="mt-2 text-gray-600">HERE에 오신 것을 환영합니다</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            <Input
              label="아이디"
              name="username"
              type="text"
              placeholder="아이디를 입력하세요"
              value={formData.username}
              onChange={handleChange}
              required
            />

            <Input
              label="비밀번호"
              name="password"
              type="password"
              placeholder="비밀번호를 입력하세요"
              value={formData.password}
              onChange={handleChange}
              required
            />

            {error && (
              <div className="bg-red-50 text-red-500 text-sm p-3 rounded-lg">{error}</div>
            )}

            <Button type="submit" className="w-full" size="lg" isLoading={isLoading}>
              로그인
            </Button>
          </form>

          <div className="mt-6 text-center">
            <p className="text-gray-600">
              아직 계정이 없으신가요?{' '}
              <Link href="/join" className="text-orange-500 font-medium hover:underline">
                회원가입
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
