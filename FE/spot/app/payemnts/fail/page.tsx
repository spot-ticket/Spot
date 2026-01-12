'use client';

import { Suspense } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { Button } from '@/components/ui/Button';

function PaymentFailContent() {
  const router = useRouter();
  const searchParams = useSearchParams();

  // 에러 정보
  const errorCode = searchParams.get('code');
  const errorMessage = searchParams.get('message');

  // 빌링키 관련 에러인지 확인
  const isBillingKeyError = errorCode?.includes('BILLING') || errorCode?.includes('BILL_KEY');

  return (
    <div className="min-h-[80vh] flex items-center justify-center px-4">
      <div className="w-full max-w-md text-center">
        {/* 실패 아이콘 */}
        <div className="mb-6 flex justify-center">
          <div className="rounded-full bg-red-100 p-4">
            <svg className="w-12 h-12 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </div>
        </div>

        <h1 className="text-2xl font-bold text-gray-900 mb-2">
          {isBillingKeyError ? '결제 수단 오류' : '결제에 실패했습니다'}
        </h1>
        <p className="text-gray-600 mb-8">
          {isBillingKeyError
            ? '등록된 결제 수단에 문제가 있습니다. 결제 수단을 다시 등록해주세요.'
            : errorMessage || '알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요.'}
        </p>

        {/* 상세 정보 */}
        {errorCode && (
          <div className="bg-gray-50 rounded-xl p-4 mb-8 text-left text-sm text-gray-500">
            <div className="flex justify-between">
              <span>에러 코드</span>
              <span className="font-mono text-gray-700">{errorCode}</span>
            </div>
          </div>
        )}

        {/* 액션 버튼 */}
        <div className="space-y-3">
          {isBillingKeyError && (
            <Button
              className="w-full"
              size="lg"
              onClick={() => router.push('/mypage/billing')}
            >
              결제 수단 재등록하기
            </Button>
          )}
          <Button
            variant={isBillingKeyError ? 'outline' : 'primary'}
            className="w-full"
            size="lg"
            onClick={() => router.push('/cart')}
          >
            장바구니로 돌아가기
          </Button>
          <Button
            variant="outline"
            className="w-full"
            size="lg"
            onClick={() => router.push('/')}
          >
            홈으로 이동
          </Button>
        </div>

        <p className="mt-8 text-sm text-gray-400">
          지속적으로 결제 실패 시 고객센터로 문의 바랍니다.
        </p>
      </div>
    </div>
  );
}

export default function PaymentFailPage() {
  return (
    <Suspense
      fallback={
        <div className="min-h-[80vh] flex items-center justify-center px-4">
          <div className="w-full max-w-md text-center">
            <div className="mb-6 flex justify-center">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-orange-500"></div>
            </div>
            <p className="text-gray-600">로딩 중...</p>
          </div>
        </div>
      }
    >
      <PaymentFailContent />
    </Suspense>
  );
}