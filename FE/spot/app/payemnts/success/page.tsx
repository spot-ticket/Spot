'use client';

import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/Button';

export default function PaymentSuccessPage() {
  const router = useRouter();

  return (
    <div className="min-h-[80vh] flex items-center justify-center px-4">
      <div className="w-full max-w-md text-center">
        {/* 성공 아이콘 */}
        <div className="mb-6 flex justify-center">
          <div className="rounded-full bg-green-100 p-4">
            <svg className="w-12 h-12 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
            </svg>
          </div>
        </div>

        <h1 className="text-2xl font-bold text-gray-900 mb-2">결제가 완료되었습니다</h1>
        <p className="text-gray-600 mb-8">
          빌링키 자동결제가 사용되지 않는 경우에만 이 페이지가 표시됩니다.
        </p>

        {/* 액션 버튼 */}
        <div className="space-y-3">
          <Button
            className="w-full"
            size="lg"
            onClick={() => router.push('/orders')}
          >
            주문 내역 확인
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
      </div>
    </div>
  );
}