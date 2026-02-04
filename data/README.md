# Dummy Data Generator for Spot Food Delivery Platform

이 디렉토리에는 Spot 프로젝트의 더미 데이터를 생성하는 Python 스크립트가 포함되어 있습니다.

## 파일 구조

```
data/
├── main.py                    # 메인 실행 스크립트
├── generators/                # 도메인별 데이터 생성기
│   ├── __init__.py
│   ├── base_generator.py      # 공통 기능
│   ├── user_generator.py      # 사용자 생성
│   ├── category_generator.py  # 카테고리 생성
│   ├── store_generator.py     # 가게 생성
│   ├── menu_generator.py      # 메뉴 생성
│   ├── order_generator.py     # 주문/결제 생성
│   └── review_generator.py    # 리뷰 생성
└── README.md                  # 이 문서
```

## 주요 특징

### 1. 도메인 기반 구조화
- **분리된 책임**: 각 도메인별로 독립적인 생성기
- **유지보수성**: 도메인별 로직 수정이 용이
- **확장성**: 새로운 도메인 추가가 간편

### 2. 현실적인 데이터 분포

#### Customer별 주문 분포 (파레토 분포)
- **활성 고객** (20%): 30-50개 주문 - 자주 주문하는 단골
- **일반 고객** (50%): 10-30개 주문 - 가끔 주문
- **비활성 고객** (30%): 0-5개 주문 - 거의 주문하지 않음

#### 가게 상태 분포
- **APPROVED**: 85% (운영 중인 가게)
- **PENDING**: 10% (승인 대기)
- **REJECTED**: 5% (반려된 가게)

#### 주문 상태 분포
- **COMPLETED**: 50% (완료된 주문)
- **ACCEPTED/COOKING/READY**: 30% (진행 중)
- **CANCELLED/REJECTED**: 5% (취소/거절)
- **PAYMENT_PENDING**: 5% (결제 대기)
- **PENDING**: 10% (접수 대기)

### 3. Payment History 구조 개선
결제 상태 변화를 시계열로 기록:
- **완료된 주문**: `READY → IN_PROGRESS → DONE`
- **취소된 주문**: `READY → CANCELLED`
- **결제 대기**: `READY` (단일 상태)

### 4. 시간 순서 보장
주문 상태 전환이 논리적 순서를 따름:
```
created_at
  → payment_completed_at
    → accepted_at
      → cooking_started_at
        → cooking_completed_at
          → picked_up_at
```

### 5. 제외된 테이블
- **retry 테이블**: 재시도 로직 관련 (생성 안함)
- **outbox 테이블**: 아웃박스 패턴 관련 (생성 안함)

## 사용 방법

### 1. 의존성 설치

```bash
pip install faker bcrypt psycopg2-binary
```

### 2. 데이터 생성 옵션

#### 옵션 A: 데이터베이스에 직접 삽입 (권장)

```bash
cd /home/yoonchul/Documents/Goorm/Spot/data
python3 main.py --direct
```

**옵션:**
- `--direct, -d`: DB에 직접 삽입
- `--host`: DB 호스트 (기본: localhost)
- `--port`: DB 포트 (기본: 5432)
- `--database`: DB 이름 (기본: myapp_db)
- `--user`: DB 사용자 (기본: admin)
- `--password`: DB 비밀번호 (기본: secret)

**예시:**
```bash
# 로컬 DB에 삽입
python3 main.py --direct

# 원격 DB에 삽입
python3 main.py --direct --host 192.168.1.100 --user myuser --password mypass

# 환경 변수 사용
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=myapp_db
export DB_USER=admin
export DB_PASSWORD=secret
python3 main.py --direct
```

#### 옵션 B: SQL 파일 생성

```bash
python3 main.py > dummy_data.sql
```

SQL 파일을 생성한 후 수동으로 적용:
```bash
psql -U admin -d myapp_db < dummy_data.sql
```

### 3. 데이터베이스 초기화 (선택사항)

기존 데이터를 삭제하고 싶다면:

```bash
docker exec local-postgres_db psql -U admin -d myapp_db -c "
TRUNCATE TABLE
  p_review,
  p_payment_key, p_payment_history, p_payment,
  p_order_item_option, p_order_item, p_order,
  p_origin, p_menu_option, p_menu,
  p_store_category, p_store_user, p_store,
  p_user_auth, p_user,
  p_category
CASCADE;
"
```

### 4. 데이터 확인

```sql
-- 사용자 수 확인
SELECT role, COUNT(*) FROM p_user GROUP BY role;

-- 가게 상태별 확인
SELECT status, COUNT(*) FROM p_store GROUP BY status;

-- 주문 상태별 확인
SELECT order_status, COUNT(*) FROM p_order GROUP BY order_status;

-- Customer별 주문 수 (상위 10명)
SELECT u.username, COUNT(o.id) as order_count
FROM p_user u
LEFT JOIN p_order o ON u.id = o.user_id
WHERE u.role = 'CUSTOMER'
GROUP BY u.id, u.username
ORDER BY order_count DESC
LIMIT 10;

-- 결제 상태 변화 이력 확인
SELECT p.id, ph.payment_status, ph.created_at
FROM p_payment p
JOIN p_payment_history ph ON p.id = ph.payment_id
WHERE p.id = 'some-payment-id'
ORDER BY ph.created_at;
```

## 설정 커스터마이징

`main.py` 파일의 `CONFIG` 딕셔너리를 수정하여 생성할 데이터 양을 조절:

```python
CONFIG = {
    'NUM_USERS': 500,              # 사용자 수
    'NUM_STORES': 1000,            # 가게 수
    'NUM_CATEGORIES': 20,          # 카테고리 수
    'MENUS_PER_STORE': (5, 15),    # 가게당 메뉴 수 (최소, 최대)
    'OPTIONS_PER_MENU': (0, 5),    # 메뉴당 옵션 수
    'ORIGINS_PER_MENU': (0, 3),    # 메뉴당 원산지 정보 수
    'ITEMS_PER_ORDER': (1, 5),     # 주문당 아이템 수
    'REVIEWS_PER_STORE': (0, 20),  # 가게당 리뷰 수
    'OWNER_RATIO': 0.1,            # OWNER 비율 (10%)
}
```

## 로그인 정보

### 고정 테스트 계정

| ID | Username | Email | Password | Role |
|----|----------|-------|----------|------|
| 1 | master | master@example.com | master | MASTER |
| 2 | owner | owner@example.com | owner | OWNER |
| 3 | chef | chef@example.com | chef | CHEF |
| 4 | customer | customer@example.com | customer | CUSTOMER |

**사용 예시:**
- **k6 테스트**: customer / customer
- **관리자 기능 테스트**: master / master
- **점주 기능 테스트**: owner / owner

### 일반 사용자

일반 사용자는 자동 생성되며, 패턴은 다음과 같습니다:
- **Username**: user5, user6, user7, ...
- **Email**: user5@example.com, user6@example.com, ...
- **Password**: username과 동일 (user5, user6, ...)

## 생성 데이터 통계 (기본 설정 기준)

| 테이블 | 예상 레코드 수 | 설명 |
|--------|---------------|------|
| p_category | 20 | 카테고리 |
| p_user | 500 | 사용자 (OWNER: 50, CUSTOMER: 450) |
| p_user_auth | 500 | 인증 정보 |
| p_store | 1,000 | 가게 |
| p_store_category | ~2,000 | 가게-카테고리 연결 |
| p_store_user | 1,000 | 가게-점주 연결 |
| p_menu | ~8,500 | 메뉴 (APPROVED 가게만) |
| p_menu_option | ~20,000 | 메뉴 옵션 |
| p_origin | ~12,000 | 원산지 정보 |
| p_order | ~5,000-7,000 | 주문 (파레토 분포) |
| p_order_item | ~15,000-20,000 | 주문 아이템 |
| p_order_item_option | ~10,000-15,000 | 주문 아이템 옵션 |
| p_payment | ~5,000-7,000 | 결제 |
| p_payment_history | ~15,000-20,000 | 결제 이력 (상태 변화) |
| p_payment_key | ~4,000-5,000 | 결제 키 (완료만) |
| p_review | ~8,000 | 리뷰 |

## 데이터 생성 순서

1. **Users** → 사용자 및 인증 정보
2. **Categories** → 카테고리
3. **Stores** → 가게, 가게-카테고리, 가게-사용자
4. **Menus** → 메뉴, 메뉴 옵션, 원산지
5. **Orders** → 주문, 주문 아이템, 결제, 결제 이력
6. **Reviews** → 리뷰 (완료된 주문 기반)

## 개발자 가이드

### 새로운 도메인 추가

1. `generators/` 폴더에 새 generator 파일 생성:
```python
# generators/new_domain_generator.py
from .base_generator import BaseGenerator

class NewDomainGenerator(BaseGenerator):
    def generate(self, **kwargs):
        # 생성 로직
        pass
```

2. `generators/__init__.py`에 추가:
```python
from .new_domain_generator import NewDomainGenerator
__all__ = [..., 'NewDomainGenerator']
```

3. `main.py`의 `generate_all()` 메서드에 추가:
```python
new_gen = NewDomainGenerator(self.conn, self.cursor, self.direct_insert, ...)
new_gen.generate()
```

### BaseGenerator 사용

모든 generator는 `BaseGenerator`를 상속받아 공통 기능 사용:
- `batch_insert()`: 배치 삽입을 위한 데이터 수집
- `flush_batch()`: 수집된 데이터를 DB에 삽입
- `generate_uuid()`: UUID 생성
- `random_updated_at()`: 랜덤 updated_at 생성

## 트러블슈팅

### "ModuleNotFoundError: No module named 'faker'"
```bash
pip install faker bcrypt psycopg2-binary
```

### "duplicate key value violates unique constraint"
기존 데이터를 완전히 삭제한 후 재실행:
```bash
# 데이터베이스 초기화 (위 3번 참조)
python3 main.py --direct
```

### 메모리 부족
설정에서 데이터 양을 줄이기:
```python
CONFIG = {
    'NUM_USERS': 100,
    'NUM_STORES': 200,
    # ...
}
```

## 주의사항

1. **개발/테스트 전용**: 프로덕션 환경에서 절대 사용하지 마세요
2. **백업 필수**: 기존 데이터가 있다면 반드시 백업하세요
3. **실행 시간**: 직접 삽입 시 1-3분 소요 (데이터 양에 따라 다름)
4. **메모리**: 최소 2GB 이상 권장

## 라이선스

이 스크립트는 Spot 프로젝트의 일부이며, 개발 및 테스트 목적으로만 사용됩니다.
