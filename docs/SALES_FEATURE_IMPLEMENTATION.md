# Sales (매출) Feature Implementation Summary

## Overview
가게 사장님(OWNER)이 자신의 가게의 매출을 확인할 수 있는 기능이 구현되었습니다. 완료된 주문(COMPLETED)을 기반으로 매출을 계산하며, 일별 매출, 인기 메뉴, 주문 통계 등을 제공합니다.

## Backend Implementation

### 1. DTOs (Data Transfer Objects)

**SalesSummaryResponse** - 매출 요약
- 위치: `/src/main/java/com/example/Spot/sales/presentation/dto/response/SalesSummaryResponse.java`
- 필드:
  - `totalRevenue`: 총 매출 (완료된 주문 기준)
  - `totalOrders`: 총 주문 수
  - `completedOrders`: 완료된 주문 수
  - `cancelledOrders`: 취소된 주문 수
  - `averageOrderAmount`: 평균 주문 금액
  - `periodStart`, `periodEnd`: 조회 기간

**DailySalesResponse** - 일별 매출
- 필드: `date`, `revenue`, `orderCount`

**PopularMenuResponse** - 인기 메뉴
- 필드: `menuId`, `menuName`, `orderCount`, `totalRevenue`

### 2. Service Layer

**SalesService.java** - 매출 계산 비즈니스 로직
- 위치: `/src/main/java/com/example/Spot/sales/application/service/SalesService.java`

**주요 메서드:**

1. **`getSalesSummary()`** - 매출 요약 조회
   - 특정 기간의 총 매출, 주문 수, 완료율 등 계산
   - 완료된 주문(`OrderStatus.COMPLETED`)만 집계
   - PaymentEntity에서 실제 결제 금액 조회

2. **`getDailySales()`** - 일별 매출 조회
   - 기간 내 모든 날짜에 대해 일별 매출 생성
   - 주문이 없는 날도 0원으로 표시

3. **`getPopularMenus()`** - 인기 메뉴 TOP N 조회
   - 주문 수 기준으로 정렬
   - 메뉴별 총 매출도 함께 제공

4. **`validateStoreAccess()`** - 권한 검증
   - OWNER/CHEF: 본인 가게만 조회 가능
   - MANAGER/MASTER: 모든 가게 조회 가능

**매출 계산 로직:**
```java
// 완료된 주문의 결제 금액 합산
Long totalRevenue = completedOrders.stream()
    .mapToLong(order -> {
        PaymentEntity payment = paymentRepository
            .findActivePaymentByOrderId(order.getId())
            .orElse(null);
        return payment != null ? payment.getTotalAmount() : 0L;
    })
    .sum();
```

### 3. Controller Layer

**SalesController.java** - REST API 엔드포인트
- 위치: `/src/main/java/com/example/Spot/sales/presentation/controller/SalesController.java`
- Base Path: `/api/stores/{storeId}/sales`
- 권한: `@PreAuthorize("hasAnyRole('OWNER', 'CHEF', 'MANAGER', 'MASTER')")`

**API 엔드포인트:**

1. **GET `/summary`** - 매출 요약 조회
   - Query Parameters: `startDate`, `endDate` (선택, 기본값: 최근 30일)
   - Response: `SalesSummaryResponse`

2. **GET `/daily`** - 일별 매출 조회
   - Query Parameters: `startDate`, `endDate`
   - Response: `List<DailySalesResponse>`

3. **GET `/popular-menus`** - 인기 메뉴 조회
   - Query Parameters: `startDate`, `endDate`, `limit` (기본 10개)
   - Response: `List<PopularMenuResponse>`

### 4. Security & Authorization

- **OWNER/CHEF**: `StoreRepository.findByIdWithDetailsForOwner()`로 본인 가게 확인
- **MANAGER/MASTER**: 모든 가게 접근 가능
- CUSTOMER는 접근 불가 (PreAuthorize로 차단)

## Frontend Implementation

### 1. API Client

**lib/sales.ts** - 매출 API 클라이언트
- 위치: `/FE/spot/lib/sales.ts`
- 인터페이스:
  - `SalesSummary`, `DailySales`, `PopularMenu`
- API 함수:
  - `getSalesSummary()`, `getDailySales()`, `getPopularMenus()`

### 2. Components

**SalesDashboard.tsx** - 매출 대시보드 컴포넌트
- 위치: `/FE/spot/components/sales/SalesDashboard.tsx`

**기능:**

1. **기간 선택**
   - 최근 7일, 30일, 90일 버튼

2. **매출 요약 카드**
   - 총 매출 (파란색)
   - 총 주문 수 (회색)
   - 완료된 주문 (초록색)
   - 평균 주문 금액 (보라색)

3. **일별 매출 차트**
   - 최근 14일 매출을 바 차트로 시각화
   - 각 날짜별 매출과 주문 수 표시

4. **인기 메뉴 TOP 5**
   - 순위, 메뉴명, 주문 횟수, 총 매출
   - 시각적 랭킹 표시 (1, 2, 3... 뱃지)

5. **주문 현황**
   - 완료율, 취소율, 하루 평균 매출

### 3. Integration

**Store Management Page** 수정
- 위치: `/FE/spot/app/mypage/store/[storeId]/page.tsx`
- 탭 추가: "메뉴 관리", **"매출 현황"**, "가게 정보"
- `activeTab` state에 `'sales'` 옵션 추가
- 매출 현황 탭에 `<SalesDashboard />` 렌더링

## Features

### Owner Features

1. **매출 요약 확인**
   - 선택한 기간의 총 매출
   - 주문 통계 (총/완료/취소)
   - 평균 주문 금액

2. **일별 매출 추이**
   - 최근 14일간 매출 그래프
   - 각 날짜별 주문 수

3. **인기 메뉴 분석**
   - 가장 많이 팔린 메뉴 TOP 5
   - 메뉴별 매출 기여도

4. **주문 완료율 확인**
   - 완료율 = (완료된 주문 / 총 주문) * 100
   - 취소율 파악

5. **기간별 분석**
   - 7일, 30일, 90일 단위 조회
   - 커스텀 기간 선택 가능 (API 지원)

### Admin Features
- 관리자(MANAGER/MASTER)는 모든 가게의 매출 조회 가능

## Data Flow

```
사용자 선택 (기간)
    ↓
Frontend: SalesDashboard
    ↓
API Call: /api/stores/{storeId}/sales/summary
    ↓
Backend: SalesController → SalesService
    ↓
OrderRepository: 기간 내 주문 조회
    ↓
PaymentRepository: 결제 정보 조회
    ↓
계산: 완료된 주문만 집계
    ↓
Response: SalesSummaryResponse
    ↓
Frontend: 차트 및 통계 렌더링
```

## Security Considerations

1. **권한 검증**
   - OWNER는 본인 가게만 조회
   - 타인의 가게 매출 조회 시 `AccessDeniedException` 발생

2. **데이터 필터링**
   - 완료된 주문(`COMPLETED`)만 매출 집계
   - 취소/거절된 주문은 별도 통계로 분리

3. **인증 필요**
   - 모든 엔드포인트에 `@PreAuthorize` 적용
   - 로그인하지 않은 사용자는 접근 불가

## API Examples

### 매출 요약 조회
```bash
GET /api/stores/{storeId}/sales/summary?startDate=2025-01-01&endDate=2025-01-31
Authorization: Bearer {token}

Response:
{
  "result": {
    "totalRevenue": 5420000,
    "totalOrders": 128,
    "completedOrders": 115,
    "cancelledOrders": 13,
    "averageOrderAmount": 47130.43,
    "periodStart": "2025-01-01T00:00:00",
    "periodEnd": "2025-01-31T23:59:59"
  }
}
```

### 일별 매출 조회
```bash
GET /api/stores/{storeId}/sales/daily?startDate=2025-01-01&endDate=2025-01-07
Authorization: Bearer {token}

Response:
{
  "result": [
    {"date": "2025-01-01", "revenue": 180000, "orderCount": 5},
    {"date": "2025-01-02", "revenue": 0, "orderCount": 0},
    {"date": "2025-01-03", "revenue": 245000, "orderCount": 7},
    ...
  ]
}
```

### 인기 메뉴 조회
```bash
GET /api/stores/{storeId}/sales/popular-menus?limit=5
Authorization: Bearer {token}

Response:
{
  "result": [
    {
      "menuId": "uuid-1",
      "menuName": "김치찌개",
      "orderCount": 45,
      "totalRevenue": 360000
    },
    {
      "menuId": "uuid-2",
      "menuName": "된장찌개",
      "orderCount": 38,
      "totalRevenue": 304000
    },
    ...
  ]
}
```

## UI Screenshots (Description)

### 매출 현황 탭
1. **상단 기간 선택 버튼**: 최근 7일 / 30일 / 90일
2. **매출 요약 카드 (4개)**:
   - 총 매출: 5,420,000원 (파란색)
   - 총 주문 수: 128건
   - 완료된 주문: 115건 (초록색)
   - 평균 주문 금액: 47,130원 (보라색)
3. **일별 매출 그래프**: 최근 14일 막대 그래프
4. **인기 메뉴 TOP 5**: 순위별 메뉴명, 주문 수, 매출
5. **주문 현황**: 완료율 89%, 취소율 10%, 하루 평균 매출 180,667원

## Testing

Backend 서버 재시작 후 다음 사항 테스트:
1. OWNER 계정으로 로그인
2. 내 가게 관리 페이지 접속 (`/mypage/store/{storeId}`)
3. "매출 현황" 탭 클릭
4. 기간 선택 (7일/30일/90일)
5. 매출 요약, 일별 매출, 인기 메뉴 확인
6. 타인의 가게 매출 조회 시 권한 오류 확인

## Next Steps (Optional Enhancements)

1. **엑셀 다운로드**: 매출 데이터 CSV/Excel 내보내기
2. **매출 비교**: 전월 대비, 전년 대비 증감률
3. **시간대별 분석**: 피크 타임 분석
4. **결제 수단별 통계**: 카드/계좌이체 비율
5. **환불 관리**: 환불 금액 및 사유 통계
6. **고객 분석**: 재방문율, 신규/재방문 고객 비율

## Technical Notes

- **날짜 처리**: LocalDate/LocalDateTime 사용
- **기본 조회 기간**: 최근 30일
- **매출 집계 기준**: `OrderStatus.COMPLETED`만 포함
- **금액 단위**: Long (원 단위)
- **페이징**: 현재 미지원 (향후 추가 가능)
