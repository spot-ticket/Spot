'use client';

import React, {useEffect, useState} from 'react';
import {useRouter} from 'next/navigation';
import {orderApi} from '@/lib/orders';
import {useAuthStore} from '@/store/authStore';
import {Button} from '@/components/ui/Button';
import type {OrderResponse, OrderStatus} from '@/types';

const statusLabels: Record<OrderStatus, string> = {
  PAYMENT_PENDING: '결제 대기',
  PENDING: '주문 접수',
  ACCEPTED: '주문 수락',
  REJECTED: '주문 거절',
  COOKING: '조리 중',
  READY: '픽업 준비 완료',
  COMPLETED: '픽업 완료',
  CANCELLED: '주문 취소',
  PAYMENT_FAILED: '결제 실패',
};

const statusColors: Record<OrderStatus, string> = {
  PAYMENT_PENDING: 'bg-yellow-100 text-yellow-800',
  PENDING: 'bg-blue-100 text-blue-800',
  ACCEPTED: 'bg-green-100 text-green-800',
  REJECTED: 'bg-red-100 text-red-800',
  COOKING: 'bg-orange-100 text-orange-800',
  READY: 'bg-purple-100 text-purple-800',
  COMPLETED: 'bg-gray-100 text-gray-800',
  CANCELLED: 'bg-red-100 text-red-800',
  PAYMENT_FAILED: 'bg-red-100 text-red-800',
};

export default function OrdersPage() {
  const router = useRouter();
  const { isAuthenticated } = useAuthStore();
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [activeOrders, setActiveOrders] = useState<OrderResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (!isAuthenticated) {
      router.push('/login');
      return;
    }
    loadOrders();
  }, [isAuthenticated, router]);

  const loadOrders = async () => {
    try {
      const [ordersData, activeData] = await Promise.all([
        orderApi.getMyOrders(),
        orderApi.getActiveOrders(),
      ]);
      setOrders(ordersData.content);
      setActiveOrders(activeData);
    } catch (error) {
      console.error('Failed to load orders:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleCancelOrder = async (orderId: string) => {
    const reason = prompt('취소 사유를 입력해주세요:');
    if (!reason) return;

    try {
      await orderApi.cancelOrder(orderId, reason);
      alert('주문이 취소되었습니다.');
      loadOrders();
    } catch (error) {
      console.error('Failed to cancel order:', error);
      alert('주문 취소에 실패했습니다.');
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const canCancel = (status: OrderStatus) => {
    return ['PAYMENT_PENDING', 'PENDING'].includes(status);
  };

  if (isLoading) {
    return (
      <div className="max-w-2xl mx-auto px-4 py-8">
        <div className="animate-pulse space-y-4">
          {[...Array(3)].map((_, i) => (
            <div key={i} className="bg-gray-100 h-32 rounded-xl" />
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">주문내역</h1>

      {/* 진행 중인 주문 */}
      {activeOrders.length > 0 && (
        <div className="mb-8">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">진행 중인 주문</h2>
          <div className="space-y-4">
            {activeOrders.map((order) => (
              <div
                key={order.orderId}
                className="bg-white rounded-xl shadow-md p-4 border-l-4 border-orange-500"
              >
                <div className="flex justify-between items-start mb-3">
                  <div>
                    <h3 className="font-semibold text-gray-900">{order.storeName}</h3>
                    <p className="text-sm text-gray-500">
                      주문번호: {order.orderNumber}
                    </p>
                  </div>
                  <span
                    className={`px-3 py-1 rounded-full text-sm font-medium ${statusColors[order.orderStatus]}`}
                  >
                    {statusLabels[order.orderStatus]}
                  </span>
                </div>

                <div className="text-sm text-gray-600 mb-3">
                  <p>픽업 시간: {formatDate(order.pickupTime)}</p>
                  {order.estimatedTime && (
                    <p>예상 조리시간: {order.estimatedTime}분</p>
                  )}
                </div>

                <div className="text-sm text-gray-600 mb-3">
                  {order.orderItems.map((item) => (
                    <p key={item.itemId}>
                      {item.menuName} x {item.quantity}
                    </p>
                  ))}
                </div>

                <div className="flex justify-between items-center pt-3 border-t">
                  <span className="font-semibold text-orange-500">
                    {Number(order.totalAmount ?? 0).toLocaleString()}원
                  </span>
                  {canCancel(order.orderStatus) && (
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleCancelOrder(order.orderId)}
                    >
                      주문 취소
                    </Button>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* 전체 주문 내역 */}
      <div>
        <h2 className="text-lg font-semibold text-gray-900 mb-4">전체 주문 내역</h2>

        {orders.length === 0 ? (
          <div className="text-center py-12">
            <div className="text-4xl mb-4">📋</div>
            <p className="text-gray-500">주문 내역이 없습니다.</p>
          </div>
        ) : (
          <div className="space-y-4">
            {orders.map((order) => (
              <div key={order.orderId} className="bg-white rounded-xl shadow-md p-4">
                <div className="flex justify-between items-start mb-3">
                  <div>
                    <h3 className="font-semibold text-gray-900">{order.storeName}</h3>
                    <p className="text-sm text-gray-500">{formatDate(order.createdAt)}</p>
                  </div>
                  <span
                    className={`px-3 py-1 rounded-full text-sm font-medium ${statusColors[order.orderStatus]}`}
                  >
                    {statusLabels[order.orderStatus]}
                  </span>
                </div>

                <div className="text-sm text-gray-600 mb-3">
                  {order.orderItems.map((item) => (
                    <p key={item.itemId}>
                      {item.menuName} x {item.quantity}
                    </p>
                  ))}
                </div>

                {order.reason && (
                  <p className="text-sm text-red-500 mb-3">
                    {order.cancelledBy === 'CUSTOMER' ? '취소 사유' : '거절 사유'}:{' '}
                    {order.reason}
                  </p>
                )}

                <div className="flex justify-between items-center pt-3 border-t">
                  <span className="font-semibold text-gray-900">
                    {Number(order.totalAmount ?? 0).toLocaleString()}원
                  </span>
                  {canCancel(order.orderStatus) && (
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleCancelOrder(order.orderId)}
                    >
                      주문 취소
                    </Button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
