'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { authApi } from '@/lib/auth';
import { useAuthStore } from '@/store/authStore';

export type Role = 'CUSTOMER' | 'OWNER' | 'CHEF' | 'MANAGER' | 'MASTER';

export interface User {
  id: number;
  username: string;
  role: Role;
  nickname: string;
  email: string;
  roadAddress: string;
  addressDetail: string;
  age: number;
  male: boolean;
}

export default function LoginPage() {
  const { setUser } = useAuthStore((state) => state.actions);
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
      const loginData = await authApi.login(formData);
      const token = loginData.accessToken;
      if (!token) throw new Error('토큰을 찾을 수 없습니다.');
      
      const tokenInfo = authApi.parseToken(token);
      console.log('토큰 정보:', tokenInfo);
      

      // ======> 이전까지는 잘 동작함.
      if (tokenInfo && tokenInfo.userId) {
        let userToSave: User;
        
        try {
          // 4. 상세 정보 조회
          const response = await authApi.getMe(tokenInfo.userId);
          console.log('서버에서 가져온 유저:', response);
          userToSave = response;

        } catch (userError) {
          console.error('getMe 실패, 기본 정보 생성');
          userToSave = {
            id: tokenInfo.userId,
            username: formData.username,
            role: (tokenInfo.role as Role) || 'CUSTOMER',
            nickname: formData.username,
            email: '',
            roadAddress: '',
            addressDetail: '',
            age: 0,
            male: true,
          };
        }

        // 5. 스토어 저장
        setUser(userToSave);
        
        // localStorage에 직접 저장 (persist가 비동기일 수 있으므로 동기적으로 저장)
        const authStorage = {
          state: {
            user: userToSave,
            isAuthenticated: true,
          },
          version: 0,
        };
        localStorage.setItem('auth-storage', JSON.stringify(authStorage));
        console.log('localStorage에 저장 완료:', authStorage);
      }

      // 페이지 이동
      window.location.replace('/'); 
      
    } catch (err: any) {
      console.error('Login Error:', err);
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
