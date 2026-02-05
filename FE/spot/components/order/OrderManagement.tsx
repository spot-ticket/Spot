'use client';

import {useEffect, useState} from 'react';
import {ownerOrderApi} from '@/lib/orders';
import type {OrderResponse} from '@/types';
import Button from '@/components/ui/Button';

interface OrderManagementProps {
  storeId: string;
}

const ORDER_STATUS_KR: Record<string, string> = {
  PAYMENT_PENDING: '결제 대기',
  PAYMENT_FAILED: '결제 실패',
  PENDING: '주문 수락 대기',
  ACCEPTED: '주문 수락',
  REJECTED: '주문 거절',
  COOKING: '조리중',
  READY: '픽업 대기',
  COMPLETED: '픽업 완료',
  CANCELLED: '주문 취소',
};

const ORDER_STATUS_COLOR: Record<string, string> = {
  PAYMENT_PENDING: 'bg-gray-100 text-gray-800',
  PAYMENT_FAILED: 'bg-red-100 text-red-800',
  PENDING: 'bg-yellow-100 text-yellow-800',
  ACCEPTED: 'bg-blue-100 text-blue-800',
  REJECTED: 'bg-red-100 text-red-800',
  COOKING: 'bg-purple-100 text-purple-800',
  READY: 'bg-green-100 text-green-800',
  COMPLETED: 'bg-gray-100 text-gray-800',
  CANCELLED: 'bg-gray-100 text-gray-800',
};

export function OrderManagement({ storeId }: OrderManagementProps) {
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [selectedStatus, setSelectedStatus] = useState<string>('all');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [cancelOrderId, setCancelOrderId] = useState<string | null>(null);
  const [cancelReason, setCancelReason] = useState('');

  useEffect(() => {
    loadOrders();
  }, [storeId, selectedStatus, page]);

  const loadOrders = async () => {
    try {
      setIsLoading(true);
      const status = selectedStatus === 'all' ? undefined : selectedStatus;
      const data = await ownerOrderApi.getMyStoreOrders(
        undefined,
        undefined,
        status,
        page,
        10
      );
      setOrders(data.content);
      setTotalPages(data.totalPages);
    } catch (error) {
      console.error('주문 목록 로드 실패:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleCancelOrder = async () => {
    if (!cancelOrderId || !cancelReason.trim()) {
      alert('취소 사유를 입력해주세요.');
      return;
    }

    try {
      await ownerOrderApi.storeCancelOrder(cancelOrderId, cancelReason);
      alert('주문이 취소되었습니다.');
      setCancelOrderId(null);
      setCancelReason('');
      loadOrders();
    } catch (error: any) {
      console.error('주문 취소 실패:', error);
      const errorMsg = error.response?.data?.message || '주문 취소에 실패했습니다.';
      alert(errorMsg);
    }
  };

  const handleAcceptOrder = async (orderId: string) => {
    const estimatedTime = prompt('예상 조리 시간을 입력하세요 (분):');
    if (!estimatedTime) return;

    const time = parseInt(estimatedTime);
    if (isNaN(time) || time <= 0) {
      alert('올바른 시간을 입력해주세요.');
      return;
    }

    try {
      await ownerOrderApi.acceptOrder(orderId, time);
      alert('주문을 수락했습니다.');
      loadOrders();
    } catch (error: any) {
      console.error('주문 수락 실패:', error);
      alert(error.response?.data?.message || '주문 수락에 실패했습니다.');
    }
  };

  const handleRejectOrder = async (orderId: string) => {
    const reason = prompt('거절 사유를 입력하세요:');
    if (!reason) return;

    if (!confirm('정말 이 주문을 거절하시겠습니까?')) return;

    try {
      await ownerOrderApi.rejectOrder(orderId, reason);
      alert('주문을 거절했습니다.');
      loadOrders();
    } catch (error: any) {
      console.error('주문 거절 실패:', error);
      alert(error.response?.data?.message || '주문 거절에 실패했습니다.');
    }
  };

  const handleCompleteOrder = async (orderId: string) => {
    if (!confirm('픽업이 완료되었습니까?')) return;

    try {
      await ownerOrderApi.completeOrder(orderId);
      alert('주문이 완료 처리되었습니다.');
      loadOrders();
    } catch (error: any) {
      console.error('주문 완료 실패:', error);
      alert(error.response?.data?.message || '주문 완료 처리에 실패했습니다.');
    }
  };

  const calculateOrderTotal = (order: OrderResponse): number => {
    return order.totalAmount;
  };

  if (isLoading) {
    return (
      <div className="flex justify-center py-12">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold text-gray-900">주문 관리</h2>
        <Button onClick={loadOrders} variant="outline" size="sm">
          새로고침
        </Button>
      </div>

      {/* 상태 필터 */}
      <div className="flex flex-wrap gap-2">
        {['all', 'PENDING', 'ACCEPTED', 'COOKING', 'READY', 'COMPLETED', 'CANCELLED'].map(
          (status) => (
            <button
              key={status}
              onClick={() => {
                setSelectedStatus(status);
                setPage(0);
              }}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                selectedStatus === status
                  ? 'bg-orange-500 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              {status === 'all' ? '전체' : ORDER_STATUS_KR[status]}
            </button>
          )
        )}
      </div>

      {/* 주문 목록 */}
      <div className="space-y-4">
        {orders.length === 0 ? (
          <div className="text-center py-12 bg-white rounded-lg shadow">
            <p className="text-gray-500">주문이 없습니다.</p>
          </div>
        ) : (
          orders.map((order) => (
            <div key={order.orderId} className="bg-white rounded-lg shadow p-6">
              {/* 주문 헤더 */}
              <div className="flex items-start justify-between mb-4">
                <div>
                  <div className="flex items-center gap-3 mb-2">
                    <h3 className="text-lg font-semibold text-gray-900">
                      #{order.orderNumber}
                    </h3>
                    <span
                      className={`px-3 py-1 rounded-full text-xs font-semibold ${
                        ORDER_STATUS_COLOR[order.orderStatus]
                      }`}
                    >
                      {ORDER_STATUS_KR[order.orderStatus]}
                    </span>
                  </div>
                  <div className="text-sm text-gray-600">
                    주문 시간:{' '}
                    {new Date(order.createdAt).toLocaleString('ko-KR')}
                  </div>
                  <div className="text-sm text-gray-600">
                    픽업 시간:{' '}
                    {new Date(order.pickupTime).toLocaleString('ko-KR')}
                  </div>
                </div>
                <div className="text-right">
                  <div className="text-2xl font-bold text-orange-500">
                    {calculateOrderTotal(order).toLocaleString()}원
                  </div>
                </div>
              </div>

              {/* 주문 항목 */}
              <div className="border-t border-b border-gray-200 py-3 mb-4">
                {order.orderItems.map((item, index) => (
                  <div key={index} className="flex justify-between py-2">
                    <div>
                      <div className="font-medium text-gray-900">
                        {item.menuName} x {item.quantity}
                      </div>
                      {item.orderItemOptions && item.orderItemOptions.length > 0 && (
                        <div className="text-sm text-gray-600 ml-2">
                          {item.orderItemOptions.map((opt, idx) => (
                            <div key={idx}>
                              + {opt.optionName}: {opt.optionValue}
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                    <div className="text-gray-700">
                      {item.subtotal.toLocaleString()}원
                    </div>
                  </div>
                ))}
              </div>

              {/* 요청 사항 */}
              {order.request && (
                <div className="mb-4 p-3 bg-gray-50 rounded">
                  <div className="text-sm font-medium text-gray-700 mb-1">
                    요청 사항
                  </div>
                  <div className="text-sm text-gray-900">{order.request}</div>
                </div>
              )}

              {order.needDisposables && (
                <div className="mb-4 text-sm text-gray-600">
                  🥢 일회용품 필요
                </div>
              )}

              {/* 액션 버튼 */}
              <div className="flex gap-2 flex-wrap">
                {order.orderStatus === 'PENDING' && (
                  <>
                    <Button
                      size="sm"
                      onClick={() => handleAcceptOrder(order.orderId)}
                    >
                      수락
                    </Button>
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => handleRejectOrder(order.orderId)}
                      className="text-red-600 border-red-600 hover:bg-red-50"
                    >
                      거절
                    </Button>
                  </>
                )}

                {(order.orderStatus === 'PENDING' ||
                  order.orderStatus === 'ACCEPTED' ||
                  order.orderStatus === 'COOKING') && (
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => setCancelOrderId(order.orderId)}
                    className="text-red-600 border-red-600 hover:bg-red-50"
                  >
                    주문 취소 (고객 요청)
                  </Button>
                )}

                {order.orderStatus === 'READY' && (
                  <Button
                    size="sm"
                    onClick={() => handleCompleteOrder(order.orderId)}
                    className="bg-green-600 hover:bg-green-700"
                  >
                    픽업 완료
                  </Button>
                )}
              </div>
            </div>
          ))
        )}
      </div>

      {/* 페이지네이션 */}
      {totalPages > 1 && (
        <div className="flex justify-center gap-2">
          <Button
            variant="outline"
            disabled={page === 0}
            onClick={() => setPage(page - 1)}
          >
            이전
          </Button>
          <span className="px-4 py-2 text-sm text-gray-700">
            {page + 1} / {totalPages}
          </span>
          <Button
            variant="outline"
            disabled={page >= totalPages - 1}
            onClick={() => setPage(page + 1)}
          >
            다음
          </Button>
        </div>
      )}

      {/* 취소 확인 모달 */}
      {cancelOrderId && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 max-w-md w-full mx-4">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
              주문 취소
            </h3>
            <p className="text-sm text-gray-600 mb-4">
              고객이 전화로 취소를 요청한 경우입니다. 취소 사유를 입력해주세요.
            </p>
            <textarea
              value={cancelReason}
              onChange={(e) => setCancelReason(e.target.value)}
              placeholder="예: 고객 요청으로 인한 취소"
              className="w-full px-3 py-2 border border-gray-300 rounded-md mb-4"
              rows={3}
            />
            <div className="flex gap-2 justify-end">
              <Button
                variant="outline"
                onClick={() => {
                  setCancelOrderId(null);
                  setCancelReason('');
                }}
              >
                닫기
              </Button>
              <Button
                onClick={handleCancelOrder}
                className="bg-red-600 hover:bg-red-700"
              >
                취소 확인
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
