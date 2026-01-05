# 주문 API 테스트 가이드

## 사전 준비

### 1. 서버 실행
```bash
./gradlew bootRun
```

### 2. 데이터베이스 확인
docker-compose로 PostgreSQL이 실행 중인지 확인

## 테스트 순서

### Step 1: 로그인 및 토큰 발급

각 역할별로 로그인하여 JWT 토큰을 발급받아야 합니다.

**로그인 요청**
```
POST http://localhost:8080/login
Content-Type: application/json

{
  "username": "customer1",
  "password": "password123"
}
```

**응답 예시**
```json
{
  "access": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

> ⚠️ **중요**: 응답에서 받은 `access` 토큰을 이후 모든 요청의 `Authorization` 헤더에 포함시켜야 합니다.
> 
> 헤더 형식: `Authorization: Bearer {access_token}`

---

## Customer (고객) API 테스트

### 1. 주문 생성
```
POST http://localhost:8080/api/orders
Authorization: Bearer {CUSTOMER_TOKEN}
Content-Type: application/json

{
  "storeId": "550e8400-e29b-41d4-a716-446655440000",
  "orderType": "DINE_IN",
  "tableNumber": 5,
  "items": [
    {
      "menuId": "550e8400-e29b-41d4-a716-446655440001",
      "quantity": 2,
      "options": [
        {
          "optionId": "550e8400-e29b-41d4-a716-446655440002",
          "quantity": 1
        }
      ]
    }
  ]
}
```

### 2. 내 주문 조회
```
GET http://localhost:8080/api/orders/my
Authorization: Bearer {CUSTOMER_TOKEN}
```

**필터링 옵션**
- `?storeId={storeId}` - 특정 가게의 주문만
- `?date=2026-01-05` - 특정 날짜의 주문만
- `?status=PENDING` - 특정 상태의 주문만 (PENDING, ACCEPTED, COOKING, READY, COMPLETED, CANCELLED, REJECTED)

```
GET http://localhost:8080/api/orders/my?status=PENDING&date=2026-01-05
Authorization: Bearer {CUSTOMER_TOKEN}
```

### 3. 활성 주문 조회
```
GET http://localhost:8080/api/orders/my/active
Authorization: Bearer {CUSTOMER_TOKEN}
```

### 4. 주문 취소
```
PATCH http://localhost:8080/api/orders/{orderId}/customer-cancel
Authorization: Bearer {CUSTOMER_TOKEN}
Content-Type: application/json

{
  "reason": "주문을 잘못했습니다"
}
```

---

## Owner (점주) API 테스트

### 1. 내 가게 주문 조회
```
GET http://localhost:8080/api/orders/my-store
Authorization: Bearer {OWNER_TOKEN}
```

**필터링 옵션**
- `?customerId={customerId}` - 특정 고객의 주문만
- `?date=2026-01-05` - 특정 날짜의 주문만
- `?status=PENDING` - 특정 상태의 주문만

### 2. 내 가게 활성 주문 조회
```
GET http://localhost:8080/api/orders/my-store/active
Authorization: Bearer {OWNER_TOKEN}
```

### 3. 주문 수락
```
PATCH http://localhost:8080/api/orders/{orderId}/accept
Authorization: Bearer {OWNER_TOKEN}
Content-Type: application/json

{
  "estimatedTime": 20
}
```

### 4. 주문 거절
```
PATCH http://localhost:8080/api/orders/{orderId}/reject
Authorization: Bearer {OWNER_TOKEN}
Content-Type: application/json

{
  "reason": "재료 소진"
}
```

### 5. 주문 완료
```
PATCH http://localhost:8080/api/orders/{orderId}/complete
Authorization: Bearer {OWNER_TOKEN}
```

### 6. 가게 측 주문 취소
```
PATCH http://localhost:8080/api/orders/{orderId}/store-cancel
Authorization: Bearer {OWNER_TOKEN}
Content-Type: application/json

{
  "reason": "긴급 상황으로 영업 종료"
}
```

---

## Chef (주방장) API 테스트

### 1. 오늘의 주문 조회
```
GET http://localhost:8080/api/orders/chef/today
Authorization: Bearer {CHEF_TOKEN}
```

### 2. 조리 시작
```
PATCH http://localhost:8080/api/orders/{orderId}/start-cooking
Authorization: Bearer {CHEF_TOKEN}
```

### 3. 픽업 준비 완료
```
PATCH http://localhost:8080/api/orders/{orderId}/ready
Authorization: Bearer {CHEF_TOKEN}
```

---

## 주문 상태 흐름

```
PENDING (주문 접수)
   ↓
ACCEPTED (점주 수락) → estimatedTime 설정
   ↓
COOKING (조리 중) → 주방장이 시작
   ↓
READY (픽업 준비 완료) → 주방장이 완료
   ↓
COMPLETED (주문 완료) → 점주가 최종 완료
```

**취소/거절 흐름**
- PENDING → CANCELLED (고객 취소)
- PENDING → REJECTED (점주 거절)
- 모든 상태 → CANCELLED (점주/고객 취소)

---

## 데이터베이스 확인 방법

### psql로 직접 확인
```bash
# PostgreSQL 컨테이너 접속
docker exec -it spot-postgres psql -U spotuser -d spotdb

# 주문 테이블 조회
SELECT * FROM orders;

# 주문 아이템 조회
SELECT * FROM order_items;

# 주문 아이템 옵션 조회
SELECT * FROM order_item_options;

# 특정 주문의 전체 정보 조회 (JOIN)
SELECT 
    o.order_id,
    o.order_status,
    o.total_price,
    oi.item_name,
    oi.quantity,
    oio.option_name
FROM orders o
LEFT JOIN order_items oi ON o.order_id = oi.order_id
LEFT JOIN order_item_options oio ON oi.order_item_id = oio.order_item_id
WHERE o.order_id = 'your-order-id-here';
```

### DBeaver나 DataGrip 같은 DB 클라이언트 사용
- Host: localhost
- Port: 5432
- Database: spotdb
- Username: spotuser
- Password: spotpassword

---

## 테스트 시나리오 예시

### 시나리오 1: 일반 주문 플로우
1. **Customer**: 주문 생성
2. **Customer**: 내 주문 조회 → PENDING 상태 확인
3. **Owner**: 내 가게 주문 조회 → 새 주문 확인
4. **Owner**: 주문 수락 (예상 시간 15분)
5. **Chef**: 오늘의 주문 조회
6. **Chef**: 조리 시작
7. **Chef**: 픽업 준비 완료
8. **Owner**: 주문 완료
9. **DB 확인**: order_status가 COMPLETED인지 확인

### 시나리오 2: 주문 취소 플로우
1. **Customer**: 주문 생성
2. **Customer**: 주문 취소 (이유 입력)
3. **DB 확인**: order_status가 CANCELLED인지, cancel_reason이 저장되었는지 확인

### 시나리오 3: 주문 거절 플로우
1. **Customer**: 주문 생성
2. **Owner**: 주문 거절 (이유: 재료 소진)
3. **Customer**: 내 주문 조회 → REJECTED 상태 확인
4. **DB 확인**: reject_reason이 저장되었는지 확인

---

## 문제 해결

### 401 Unauthorized 에러
- 토큰이 만료되었거나 잘못된 토큰입니다.
- `/login`으로 다시 로그인하여 새 토큰을 발급받으세요.

### 403 Forbidden 에러
- 해당 API를 호출할 권한이 없습니다.
- 역할(CUSTOMER, OWNER, CHEF)을 확인하세요.

### 404 Not Found 에러
- orderId가 잘못되었거나 존재하지 않는 주문입니다.
- UUID 형식이 맞는지 확인하세요.

### 500 Internal Server Error
- 서버 로그를 확인하여 자세한 에러 내용을 파악하세요.
- 데이터베이스 연결 상태를 확인하세요.

---

## Postman Collection 사용 (선택사항)

위 API들을 Postman Collection으로 만들어서 사용하면 더 편리합니다.

1. Postman에서 새 Collection 생성
2. 각 API 요청을 추가
3. Collection 변수 설정:
   - `base_url`: http://localhost:8080
   - `customer_token`: 로그인 후 받은 토큰
   - `owner_token`: 로그인 후 받은 토큰
   - `chef_token`: 로그인 후 받은 토큰
4. 요청 헤더에 `Authorization: Bearer {{customer_token}}` 형식으로 사용

