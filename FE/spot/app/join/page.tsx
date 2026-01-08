'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { authApi } from '@/lib/auth';
import type { Role } from '@/types';

export default function JoinPage() {
  const router = useRouter();
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    passwordConfirm: '',
    nickname: '',
    email: '',
    male: true,
    age: '',
    roadAddress: '',
    addressDetail: '',
    role: 'CUSTOMER' as Role,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value, type } = e.target;

    if (type === 'checkbox') {
      const checked = (e.target as HTMLInputElement).checked;
      setFormData((prev) => ({ ...prev, [name]: checked }));
    } else {
      setFormData((prev) => ({ ...prev, [name]: value }));
    }

    setErrors((prev) => ({ ...prev, [name]: '' }));
  };

  const validate = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.username) newErrors.username = '아이디를 입력하세요';
    if (formData.username.length < 4)
      newErrors.username = '아이디는 4자 이상이어야 합니다';

    if (!formData.password) newErrors.password = '비밀번호를 입력하세요';
    if (formData.password.length < 8)
      newErrors.password = '비밀번호는 8자 이상이어야 합니다';

    if (formData.password !== formData.passwordConfirm)
      newErrors.passwordConfirm = '비밀번호가 일치하지 않습니다';

    if (!formData.nickname) newErrors.nickname = '닉네임을 입력하세요';

    if (!formData.email) newErrors.email = '이메일을 입력하세요';
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email))
      newErrors.email = '올바른 이메일 형식이 아닙니다';

    if (!formData.age) newErrors.age = '나이를 입력하세요';

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validate()) return;

    setIsLoading(true);

    try {
      await authApi.join({
        username: formData.username,
        password: formData.password,
        nickname: formData.nickname,
        email: formData.email,
        male: formData.male,
        age: parseInt(formData.age),
        roadAddress: formData.roadAddress,
        addressDetail: formData.addressDetail,
        role: formData.role,
      });

      alert('회원가입이 완료되었습니다. 로그인해주세요.');
      router.push('/login');
    } catch (err) {
      console.error('Join error:', err);
      setErrors({ submit: '회원가입에 실패했습니다. 다시 시도해주세요.' });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-[80vh] flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-lg">
        <div className="bg-white rounded-2xl shadow-lg p-8">
          <div className="text-center mb-8">
            <h1 className="text-3xl font-bold text-gray-900">회원가입</h1>
            <p className="mt-2 text-gray-600">HERE와 함께 맛있는 음식을 주문하세요</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-5">
            <Input
              label="아이디"
              name="username"
              type="text"
              placeholder="아이디를 입력하세요"
              value={formData.username}
              onChange={handleChange}
              error={errors.username}
              required
            />

            <Input
              label="비밀번호"
              name="password"
              type="password"
              placeholder="비밀번호를 입력하세요"
              value={formData.password}
              onChange={handleChange}
              error={errors.password}
              required
            />

            <Input
              label="비밀번호 확인"
              name="passwordConfirm"
              type="password"
              placeholder="비밀번호를 다시 입력하세요"
              value={formData.passwordConfirm}
              onChange={handleChange}
              error={errors.passwordConfirm}
              required
            />

            <Input
              label="닉네임"
              name="nickname"
              type="text"
              placeholder="닉네임을 입력하세요"
              value={formData.nickname}
              onChange={handleChange}
              error={errors.nickname}
              required
            />

            <Input
              label="이메일"
              name="email"
              type="email"
              placeholder="이메일을 입력하세요"
              value={formData.email}
              onChange={handleChange}
              error={errors.email}
              required
            />

            <div className="grid grid-cols-2 gap-4">
              <Input
                label="나이"
                name="age"
                type="number"
                placeholder="나이"
                value={formData.age}
                onChange={handleChange}
                error={errors.age}
                required
              />

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  성별
                </label>
                <select
                  name="male"
                  value={formData.male ? 'true' : 'false'}
                  onChange={(e) =>
                    setFormData((prev) => ({
                      ...prev,
                      male: e.target.value === 'true',
                    }))
                  }
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500"
                >
                  <option value="true">남성</option>
                  <option value="false">여성</option>
                </select>
              </div>
            </div>

            <Input
              label="도로명 주소"
              name="roadAddress"
              type="text"
              placeholder="도로명 주소를 입력하세요"
              value={formData.roadAddress}
              onChange={handleChange}
            />

            <Input
              label="상세 주소"
              name="addressDetail"
              type="text"
              placeholder="상세 주소를 입력하세요"
              value={formData.addressDetail}
              onChange={handleChange}
            />

            {errors.submit && (
              <div className="bg-red-50 text-red-500 text-sm p-3 rounded-lg">
                {errors.submit}
              </div>
            )}

            <Button type="submit" className="w-full" size="lg" isLoading={isLoading}>
              회원가입
            </Button>
          </form>

          <div className="mt-6 text-center">
            <p className="text-gray-600">
              이미 계정이 있으신가요?{' '}
              <Link href="/login" className="text-orange-500 font-medium hover:underline">
                로그인
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
