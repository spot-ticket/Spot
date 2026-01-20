'use client';

import React, {useState} from 'react';
import {useRouter} from 'next/navigation';
import Link from 'next/link';
import {Button} from '@/components/ui/Button';
import {Input} from '@/components/ui/Input';
import {authApi} from '@/lib/auth';
import type {Role} from '@/types';

export default function JoinPage() {
  const router = useRouter();
  const [selectedRole, setSelectedRole] = useState<Role | null>(null);
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

  const handleRoleSelect = (role: Role) => {
    setSelectedRole(role);
    setFormData((prev) => ({ ...prev, role }));
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
            <p className="mt-2 text-gray-600">SPOT과 함께 맛있는 음식을 주문하세요</p>
          </div>

          {!selectedRole ? (
            <div className="space-y-6">
              <h2 className="text-xl font-semibold text-gray-900 text-center">
                가입 유형을 선택하세요
              </h2>

              <div className="grid grid-cols-1 gap-4">
                <button
                  type="button"
                  onClick={() => handleRoleSelect('CUSTOMER')}
                  className="p-6 border-2 border-gray-200 rounded-xl hover:border-orange-500 hover:bg-orange-50 transition-all group"
                >
                  <div className="flex items-center space-x-4">
                    <div className="w-12 h-12 bg-orange-100 rounded-full flex items-center justify-center group-hover:bg-orange-200">
                      <svg className="w-6 h-6 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      </svg>
                    </div>
                    <div className="text-left">
                      <h3 className="font-semibold text-lg text-gray-900">고객</h3>
                      <p className="text-sm text-gray-600">음식을 주문하고 픽업합니다</p>
                    </div>
                  </div>
                </button>

                <button
                  type="button"
                  onClick={() => handleRoleSelect('OWNER')}
                  className="p-6 border-2 border-gray-200 rounded-xl hover:border-orange-500 hover:bg-orange-50 transition-all group"
                >
                  <div className="flex items-center space-x-4">
                    <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center group-hover:bg-blue-200">
                      <svg className="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                      </svg>
                    </div>
                    <div className="text-left">
                      <h3 className="font-semibold text-lg text-gray-900">점주</h3>
                      <p className="text-sm text-gray-600">가게를 등록하고 주문을 관리합니다</p>
                    </div>
                  </div>
                </button>

                <button
                  type="button"
                  onClick={() => handleRoleSelect('CHEF')}
                  className="p-6 border-2 border-gray-200 rounded-xl hover:border-orange-500 hover:bg-orange-50 transition-all group"
                >
                  <div className="flex items-center space-x-4">
                    <div className="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center group-hover:bg-green-200">
                      <svg className="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
                      </svg>
                    </div>
                    <div className="text-left">
                      <h3 className="font-semibold text-lg text-gray-900">셰프</h3>
                      <p className="text-sm text-gray-600">주방에서 조리를 담당합니다</p>
                    </div>
                  </div>
                </button>
              </div>

              <div className="mt-6 text-center">
                <p className="text-gray-600">
                  이미 계정이 있으신가요?{' '}
                  <Link href="/login" className="text-orange-500 font-medium hover:underline">
                    로그인
                  </Link>
                </p>
              </div>
            </div>
          ) : (
            <>
              <div className="mb-6 flex items-center justify-between">
                <button
                  type="button"
                  onClick={() => setSelectedRole(null)}
                  className="text-gray-600 hover:text-gray-900 flex items-center"
                >
                  <svg className="w-5 h-5 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                  </svg>
                  뒤로
                </button>
                <span className="text-sm font-medium text-orange-600">
                  {selectedRole === 'CUSTOMER' ? '고객' : selectedRole === 'OWNER' ? '점주' : '셰프'} 가입
                </span>
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
            </>
          )}
        </div>
      </div>
    </div>
  );
}
