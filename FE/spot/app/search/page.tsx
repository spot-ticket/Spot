'use client';

import React, {Suspense, useEffect, useState} from 'react';
import {useSearchParams} from 'next/navigation';
import Link from 'next/link';
import {storeApi} from '@/lib/stores';
import {Input} from '@/components/ui/Input';
import type {Store} from '@/types';

function SearchContent() {
  const searchParams = useSearchParams();
  const query = searchParams.get('q') || '';

  const [searchQuery, setSearchQuery] = useState(query);
  const [stores, setStores] = useState<Store[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (query) {
      handleSearch(query);
    }
  }, [query]);

  const handleSearch = async (keyword: string) => {
    if (!keyword.trim()) return;

    setIsLoading(true);
    try {
      const data = await storeApi.searchStores(keyword);
      setStores(data.content);
    } catch (error) {
      console.error('Search failed:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    handleSearch(searchQuery);
  };

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">가게 검색</h1>

      {/* 검색 폼 */}
      <form onSubmit={handleSubmit} className="mb-8">
        <div className="flex gap-3">
          <Input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="가게명을 검색하세요"
            className="flex-1"
          />
          <button
            type="submit"
            className="px-6 py-2 bg-orange-500 text-white rounded-lg hover:bg-orange-600 transition-colors"
          >
            검색
          </button>
        </div>
      </form>

      {/* 검색 결과 */}
      {isLoading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
          {[...Array(4)].map((_, i) => (
            <div key={i} className="bg-gray-100 rounded-xl h-48 animate-pulse" />
          ))}
        </div>
      ) : stores.length === 0 ? (
        <div className="text-center py-12">
          {query ? (
            <>
              <div className="text-4xl mb-4">🔍</div>
              <p className="text-gray-500">&quot;{query}&quot;에 대한 검색 결과가 없습니다.</p>
            </>
          ) : (
            <>
              <div className="text-4xl mb-4">🔍</div>
              <p className="text-gray-500">검색어를 입력하세요.</p>
            </>
          )}
        </div>
      ) : (
        <>
          <p className="text-gray-600 mb-4">
            &quot;{query}&quot; 검색 결과 {stores.length}개
          </p>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
            {stores.map((store) => (
              <Link
                key={store.id}
                href={`/stores/${store.id}`}
                className="bg-white rounded-xl shadow-md overflow-hidden hover:shadow-lg transition-shadow"
              >
                <div className="bg-gradient-to-br from-orange-100 to-orange-200 h-32 flex items-center justify-center">
                  <span className="text-4xl">🏪</span>
                </div>
                <div className="p-4">
                  <h3 className="font-bold text-lg text-gray-900 mb-1">{store.name}</h3>
                  <p className="text-sm text-gray-500 mb-2">
                    {store.roadAddress} {store.addressDetail}
                  </p>
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-gray-600">
                      {store.openTime} - {store.closeTime}
                    </span>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        </>
      )}
    </div>
  );
}

export default function SearchPage() {
  return (
    <Suspense fallback={<div className="max-w-4xl mx-auto px-4 py-8">로딩중...</div>}>
      <SearchContent />
    </Suspense>
  );
}
