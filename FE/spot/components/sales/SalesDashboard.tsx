'use client';

import {useEffect, useState} from 'react';
import {type DailySales, type PopularMenu, salesApi, type SalesSummary} from '@/lib/sales';
import Button from '@/components/ui/Button';

interface SalesDashboardProps {
  storeId: string;
}

export function SalesDashboard({ storeId }: SalesDashboardProps) {
  const [summary, setSummary] = useState<SalesSummary | null>(null);
  const [dailySales, setDailySales] = useState<DailySales[]>([]);
  const [popularMenus, setPopularMenus] = useState<PopularMenu[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [period, setPeriod] = useState<'7' | '30' | '90'>('30');

  useEffect(() => {
    loadSalesData();
  }, [storeId, period]);

  const loadSalesData = async () => {
    try {
      setIsLoading(true);
      const days = parseInt(period);
      const endDate = new Date().toISOString().split('T')[0];
      const startDate = new Date(Date.now() - days * 24 * 60 * 60 * 1000)
        .toISOString()
        .split('T')[0];

      const [summaryData, dailyData, menuData] = await Promise.all([
        salesApi.getSalesSummary(storeId, startDate, endDate),
        salesApi.getDailySales(storeId, startDate, endDate),
        salesApi.getPopularMenus(storeId, startDate, endDate, 5),
      ]);

      setSummary(summaryData);
      setDailySales(dailyData);
      setPopularMenus(menuData);
    } catch (error) {
      console.error('매출 데이터 로드 실패:', error);
    } finally {
      setIsLoading(false);
    }
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
      {/* 기간 선택 */}
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold text-gray-900">매출 현황</h2>
        <div className="flex gap-2">
          <Button
            size="sm"
            variant={period === '7' ? 'primary' : 'outline'}
            onClick={() => setPeriod('7')}
          >
            최근 7일
          </Button>
          <Button
            size="sm"
            variant={period === '30' ? 'primary' : 'outline'}
            onClick={() => setPeriod('30')}
          >
            최근 30일
          </Button>
          <Button
            size="sm"
            variant={period === '90' ? 'primary' : 'outline'}
            onClick={() => setPeriod('90')}
          >
            최근 90일
          </Button>
        </div>
      </div>

      {/* 매출 요약 */}
      {summary && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="bg-white rounded-lg shadow p-6">
            <div className="text-sm font-medium text-gray-500">총 매출</div>
            <div className="mt-2 text-3xl font-bold text-blue-600">
              {summary.totalRevenue.toLocaleString()}원
            </div>
          </div>
          <div className="bg-white rounded-lg shadow p-6">
            <div className="text-sm font-medium text-gray-500">총 주문 수</div>
            <div className="mt-2 text-3xl font-bold text-gray-900">
              {summary.totalOrders.toLocaleString()}건
            </div>
          </div>
          <div className="bg-white rounded-lg shadow p-6">
            <div className="text-sm font-medium text-gray-500">완료된 주문</div>
            <div className="mt-2 text-3xl font-bold text-green-600">
              {summary.completedOrders.toLocaleString()}건
            </div>
          </div>
          <div className="bg-white rounded-lg shadow p-6">
            <div className="text-sm font-medium text-gray-500">평균 주문 금액</div>
            <div className="mt-2 text-3xl font-bold text-purple-600">
              {Math.round(summary.averageOrderAmount).toLocaleString()}원
            </div>
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* 일별 매출 그래프 (간단한 바 차트) */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">일별 매출</h3>
          <div className="space-y-2">
            {dailySales.slice(-14).map((day) => {
              const maxRevenue = Math.max(...dailySales.map((d) => d.revenue));
              const barWidth = maxRevenue > 0 ? (day.revenue / maxRevenue) * 100 : 0;

              return (
                <div key={day.date} className="flex items-center gap-2">
                  <div className="text-xs text-gray-600 w-20">
                    {new Date(day.date).toLocaleDateString('ko-KR', {
                      month: 'short',
                      day: 'numeric',
                    })}
                  </div>
                  <div className="flex-1">
                    <div className="bg-gray-100 rounded-full h-6 overflow-hidden">
                      <div
                        className="bg-blue-500 h-full flex items-center justify-end pr-2"
                        style={{ width: `${barWidth}%` }}
                      >
                        {day.revenue > 0 && (
                          <span className="text-xs text-white font-medium">
                            {day.revenue.toLocaleString()}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                  <div className="text-xs text-gray-600 w-12 text-right">
                    {day.orderCount}건
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* 인기 메뉴 */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">인기 메뉴 TOP 5</h3>
          <div className="space-y-3">
            {popularMenus.length === 0 ? (
              <div className="text-center py-8 text-gray-500">
                아직 주문된 메뉴가 없습니다.
              </div>
            ) : (
              popularMenus.map((menu, index) => (
                <div
                  key={menu.menuId}
                  className="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
                >
                  <div className="flex items-center gap-3">
                    <div className="flex items-center justify-center w-8 h-8 bg-blue-100 text-blue-600 rounded-full font-bold text-sm">
                      {index + 1}
                    </div>
                    <div>
                      <div className="font-medium text-gray-900">{menu.menuName}</div>
                      <div className="text-sm text-gray-500">
                        {menu.orderCount}회 주문
                      </div>
                    </div>
                  </div>
                  <div className="text-right">
                    <div className="font-semibold text-gray-900">
                      {menu.totalRevenue.toLocaleString()}원
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      {/* 주문 현황 */}
      {summary && (
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">주문 현황</h3>
          <div className="grid grid-cols-3 gap-4">
            <div className="text-center">
              <div className="text-sm text-gray-500">완료율</div>
              <div className="mt-2 text-2xl font-bold text-green-600">
                {summary.totalOrders > 0
                  ? Math.round((summary.completedOrders / summary.totalOrders) * 100)
                  : 0}
                %
              </div>
            </div>
            <div className="text-center">
              <div className="text-sm text-gray-500">취소율</div>
              <div className="mt-2 text-2xl font-bold text-red-600">
                {summary.totalOrders > 0
                  ? Math.round((summary.cancelledOrders / summary.totalOrders) * 100)
                  : 0}
                %
              </div>
            </div>
            <div className="text-center">
              <div className="text-sm text-gray-500">하루 평균 매출</div>
              <div className="mt-2 text-2xl font-bold text-blue-600">
                {dailySales.length > 0
                  ? Math.round(
                      dailySales.reduce((sum, day) => sum + day.revenue, 0) /
                        dailySales.length
                    ).toLocaleString()
                  : 0}
                원
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
