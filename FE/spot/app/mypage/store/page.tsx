'use client';

import React, {useEffect, useState} from 'react';
import {useRouter} from 'next/navigation';
import {useAuthStore} from '@/store/authStore';
import {Button} from '@/components/ui/Button';
import {Input} from '@/components/ui/Input';
import axios from 'axios';

interface Store {
  id: string;
  name: string;
  description: string;
  phoneNumber: string;
  roadAddress: string;
  addressDetail: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
}

export default function OwnerStorePage() {
  const router = useRouter();
  const { user, hasHydrated } = useAuthStore();
  const [stores, setStores] = useState<Store[]>([]);
  const [categories, setCategories] = useState<string[]>([
    '한식', '중식', '일식', '양식', '분식', '카페', '치킨', '피자', '족발/보쌈', '찜/탕'
  ]);
  const [newCategory, setNewCategory] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    phoneNumber: '',
    roadAddress: '서울특별시 종로구 ',  // 서비스 가능 지역으로 자동 설정
    addressDetail: '',
    openTime: '09:00',
    closeTime: '22:00',
    categoryNames: [] as string[],
    ownerId: user?.id || 0,
    chefId: user?.id || 0,
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (hasHydrated && user?.role !== 'OWNER') {
      router.push('/mypage');
    }
  }, [hasHydrated, user, router]);

  useEffect(() => {
    if (hasHydrated && user?.role === 'OWNER') {
      loadStores();
      // user ID로 formData 업데이트
      setFormData((prev) => ({
        ...prev,
        ownerId: user.id,
        chefId: user.id,
      }));
    }
  }, [hasHydrated, user]);

  const loadStores = async () => {
    try {
      const token = localStorage.getItem('accessToken');
      console.log('=== 내 가게 조회 요청 ===');
      console.log('Token:', token ? '있음' : '없음');

      const response = await axios.get('/api/stores/my', {
        headers: { Authorization: `Bearer ${token}` },
      });

      console.log('응답 전체:', response);
      console.log('응답 데이터:', response.data);

      // API 응답이 ApiResponse로 래핑되어 있는지, 직접 배열인지 확인
      const storesData = response.data.result || response.data;
      console.log('가게 데이터:', storesData);
      console.log('가게 개수:', storesData?.length || 0);

      setStores(Array.isArray(storesData) ? storesData : []);
    } catch (error) {
      console.error('가게 목록 로드 실패:', error);
      if (axios.isAxiosError(error)) {
        console.error('에러 응답:', error.response?.data);
        console.error('에러 상태:', error.response?.status);
      }
    }
  };

  const handleCancelStore = async (storeId: string, storeName: string) => {
    if (!confirm(`"${storeName}" 가게 등록을 취소하시겠습니까?`)) {
      return;
    }

    try {
      const token = localStorage.getItem('accessToken');
      await axios.delete(`/api/stores/${storeId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      alert('가게 등록이 취소되었습니다.');
      loadStores();
    } catch (error: any) {
      console.error('가게 취소 실패:', error);
      const errorMsg = error.response?.data?.message || '가게 취소에 실패했습니다.';
      alert(`가게 취소 실패: ${errorMsg}`);
    }
  };

  const handleDeleteStore = async (storeId: string, storeName: string) => {
    if (!confirm(`"${storeName}" 가게를 삭제하시겠습니까?\n삭제된 가게는 복구할 수 없습니다.`)) {
      return;
    }

    try {
      const token = localStorage.getItem('accessToken');
      await axios.delete(`/api/stores/${storeId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      alert('가게가 삭제되었습니다.');
      loadStores();
    } catch (error: any) {
      console.error('가게 삭제 실패:', error);
      const errorMsg = error.response?.data?.message || '가게 삭제에 실패했습니다.';
      alert(`가게 삭제 실패: ${errorMsg}`);
    }
  };

  const handleAddCategory = () => {
    if (newCategory.trim() && !categories.includes(newCategory.trim())) {
      setCategories([...categories, newCategory.trim()]);
      setNewCategory('');
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    setErrors((prev) => ({ ...prev, [name]: '' }));
  };

  const validate = () => {
    const newErrors: Record<string, string> = {};
    if (!formData.name) newErrors.name = '가게 이름을 입력하세요';
    if (!formData.phoneNumber) newErrors.phoneNumber = '전화번호를 입력하세요';
    if (!formData.roadAddress) newErrors.roadAddress = '주소를 입력하세요';
    if (!formData.openTime) newErrors.openTime = '오픈 시간을 입력하세요';
    if (!formData.closeTime) newErrors.closeTime = '마감 시간을 입력하세요';
    if (formData.categoryNames.length === 0) newErrors.categoryNames = '최소 1개의 카테고리를 선택하세요';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;

    setIsLoading(true);
    try {
      const token = localStorage.getItem('accessToken');

      // 1. 먼저 기존 카테고리 목록을 조회
      const existingCategoriesResponse = await axios.get('/api/categories', {
        headers: { Authorization: `Bearer ${token}` },
      });
      const existingCategoryNames = existingCategoriesResponse.data.map((cat: any) => cat.name);

      // 2. 선택된 카테고리 중 존재하지 않는 카테고리를 찾아서 생성
      for (const categoryName of formData.categoryNames) {
        if (!existingCategoryNames.includes(categoryName)) {
          await axios.post(
            '/api/categories',
            { name: categoryName },
            { headers: { Authorization: `Bearer ${token}` } }
          );
        }
      }

      // 3. 모든 카테고리가 준비되면 스토어 등록
      await axios.post('/api/stores', formData, {
        headers: { Authorization: `Bearer ${token}` },
      });
      alert('가게가 등록되었습니다. 관리자 승인 후 운영이 가능합니다.');
      setShowForm(false);
      setFormData({
        name: '',
        phoneNumber: '',
        roadAddress: '서울특별시 종로구 ',  // 서비스 가능 지역으로 자동 설정
        addressDetail: '',
        openTime: '09:00',
        closeTime: '22:00',
        categoryNames: [],
        ownerId: user?.id || 0,
        chefId: user?.id || 0,
      });
      loadStores();
    } catch (error: any) {
      console.error('가게 등록 실패:', error);
      console.error('Error response:', error.response);
      console.error('Request data:', formData);
      const errorMsg = error.response?.data?.message || error.message || '가게 등록에 실패했습니다.';
      alert(`가게 등록 실패: ${errorMsg}\nStatus: ${error.response?.status}`);
    } finally {
      setIsLoading(false);
    }
  };

  if (!hasHydrated || !user) {
    return null;
  }

  const statusLabels = {
    PENDING: '승인 대기',
    APPROVED: '승인됨',
    REJECTED: '거부됨',
  };

  const statusColors = {
    PENDING: 'bg-yellow-100 text-yellow-800',
    APPROVED: 'bg-green-100 text-green-800',
    REJECTED: 'bg-red-100 text-red-800',
  };

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">내 가게 관리</h1>
        {!showForm && (
          <Button onClick={() => setShowForm(true)}>
            가게 등록하기
          </Button>
        )}
      </div>

      {showForm ? (
        <div className="bg-white rounded-xl shadow-md p-6 mb-6">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xl font-semibold">새 가게 등록</h2>
            <button
              onClick={() => setShowForm(false)}
              className="text-gray-500 hover:text-gray-700"
            >
              ✕
            </button>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <Input
              label="가게 이름"
              name="name"
              value={formData.name}
              onChange={handleChange}
              error={errors.name}
              required
            />

            <Input
              label="전화번호"
              name="phoneNumber"
              value={formData.phoneNumber}
              onChange={handleChange}
              error={errors.phoneNumber}
              placeholder="010-1234-5678"
              required
            />

            <div>
              <Input
                label="도로명 주소"
                name="roadAddress"
                value={formData.roadAddress}
                onChange={handleChange}
                error={errors.roadAddress}
                required
              />
              <p className="mt-1 text-xs text-gray-500">
                현재 서비스 지역: 종로구 (주소에 "종로구"가 포함되어야 합니다)
              </p>
            </div>

            <Input
              label="상세 주소"
              name="addressDetail"
              value={formData.addressDetail}
              onChange={handleChange}
            />

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  오픈 시간 <span className="text-red-500">*</span>
                </label>
                <input
                  type="time"
                  name="openTime"
                  value={formData.openTime}
                  onChange={handleChange}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500"
                  required
                />
                {errors.openTime && (
                  <p className="mt-1 text-sm text-red-500">{errors.openTime}</p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  마감 시간 <span className="text-red-500">*</span>
                </label>
                <input
                  type="time"
                  name="closeTime"
                  value={formData.closeTime}
                  onChange={handleChange}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500"
                  required
                />
                {errors.closeTime && (
                  <p className="mt-1 text-sm text-red-500">{errors.closeTime}</p>
                )}
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                카테고리 <span className="text-red-500">*</span>
              </label>

              {/* 카테고리 추가 입력란 */}
              <div className="flex gap-2 mb-3">
                <input
                  type="text"
                  value={newCategory}
                  onChange={(e) => setNewCategory(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') {
                      e.preventDefault();
                      handleAddCategory();
                    }
                  }}
                  placeholder="새 카테고리 추가"
                  className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500"
                />
                <button
                  type="button"
                  onClick={handleAddCategory}
                  className="px-4 py-2 bg-orange-500 text-white rounded-lg hover:bg-orange-600 transition-colors text-sm font-medium"
                >
                  추가
                </button>
              </div>

              {/* 카테고리 선택 버튼 */}
              <div className="flex flex-wrap gap-2">
                {categories.map((category) => (
                  <button
                    key={category}
                    type="button"
                    onClick={() => {
                      const newCategories = formData.categoryNames.includes(category)
                        ? formData.categoryNames.filter((c) => c !== category)
                        : [...formData.categoryNames, category];
                      setFormData((prev) => ({ ...prev, categoryNames: newCategories }));
                      setErrors((prev) => ({ ...prev, categoryNames: '' }));
                    }}
                    className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                      formData.categoryNames.includes(category)
                        ? 'bg-orange-500 text-white'
                        : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                    }`}
                  >
                    {category}
                  </button>
                ))}
              </div>
              {errors.categoryNames && (
                <p className="mt-1 text-sm text-red-500">{errors.categoryNames}</p>
              )}
            </div>

            <div className="flex gap-2">
              <Button type="submit" isLoading={isLoading} className="flex-1">
                등록하기
              </Button>
              <Button
                type="button"
                variant="outline"
                onClick={() => setShowForm(false)}
              >
                취소
              </Button>
            </div>
          </form>
        </div>
      ) : null}

      {/* 가게 목록 */}
      <div className="space-y-4">
        {stores.length === 0 ? (
          <div className="bg-white rounded-xl shadow-md p-8 text-center">
            <p className="text-gray-500">등록된 가게가 없습니다.</p>
            <p className="text-sm text-gray-400 mt-2">
              가게를 등록하고 운영을 시작하세요.
            </p>
          </div>
        ) : (
          stores.map((store) => (
            <div key={store.id} className="bg-white rounded-xl shadow-md p-6">
              <div className="flex items-start justify-between mb-4">
                <div>
                  <h3 className="text-xl font-semibold text-gray-900">{store.name}</h3>
                  <p className="text-gray-600 mt-1">{store.description}</p>
                </div>
                <span
                  className={`px-3 py-1 rounded-full text-sm font-medium ${
                    statusColors[store.status]
                  }`}
                >
                  {statusLabels[store.status]}
                </span>
              </div>

              <div className="space-y-2 text-sm text-gray-600">
                <div className="flex items-center gap-2">
                  <span>📞</span>
                  <span>{store.phoneNumber}</span>
                </div>
                <div className="flex items-center gap-2">
                  <span>📍</span>
                  <span>
                    {store.roadAddress}
                    {store.addressDetail && `, ${store.addressDetail}`}
                  </span>
                </div>
              </div>

              <div className="mt-4 pt-4 border-t flex gap-2">
                {store.status === 'APPROVED' && (
                  <>
                    <Button
                      size="sm"
                      onClick={() => router.push(`/mypage/store/${store.id}`)}
                    >
                      관리하기
                    </Button>
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => handleDeleteStore(store.id, store.name)}
                      className="text-red-600 border-red-600 hover:bg-red-50"
                    >
                      삭제
                    </Button>
                  </>
                )}
                {store.status === 'PENDING' && (
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => handleCancelStore(store.id, store.name)}
                    className="text-gray-600 hover:bg-gray-50"
                  >
                    등록 취소
                  </Button>
                )}
                {store.status === 'REJECTED' && (
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => handleDeleteStore(store.id, store.name)}
                    className="text-red-600 border-red-600 hover:bg-red-50"
                  >
                    삭제
                  </Button>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
