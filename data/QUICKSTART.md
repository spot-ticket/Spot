# Quick Start Guide

## 빠른 시작

### 1. 기본 사용법 (권장)
```bash
# 스크립트 실행 권한 부여
chmod +x run_generator.sh

# 더미 데이터 생성 (기본 1만 건 주문)
./run_generator.sh

# 결과: dummy_data.sql 파일 생성됨
```

### 2. 데이터베이스에 적용
```bash
# PostgreSQL
psql -U postgres -d food_delivery -f dummy_data.sql

# MySQL
mysql -u root -p food_delivery < dummy_data.sql
```

## 데이터량 조절

### 소규모 테스트 (빠른 테스트용)
`generate_dummy_data.py` 파일 수정:
```python
NUM_USERS = 100
NUM_STORES = 10
NUM_ORDERS = 200
```
예상 레코드: 약 2,000건, 생성 시간: 5초

### 중규모 개발용 (권장)
```python
NUM_USERS = 1000
NUM_STORES = 100
NUM_ORDERS = 10000
```
예상 레코드: 약 50,000건, 생성 시간: 1-2분

### 대규모 성능 테스트용
```python
NUM_USERS = 10000
NUM_STORES = 1000
NUM_ORDERS = 100000
```
예상 레코드: 약 500,000건, 생성 시간: 10-20분

## 실제 사용 예시

### 시나리오 1: 로컬 개발 환경 세팅
```bash
# 1. 소규모 데이터로 먼저 테스트
python3 generate_dummy_data.py > test_data.sql

# 2. DB에 적용
psql -U dev -d food_dev -f test_data.sql

# 3. 데이터 확인
psql -U dev -d food_dev -c "SELECT COUNT(*) FROM p_order;"
```

### 시나리오 2: 성능 테스트용 대량 데이터
```bash
# 1. config 수정 후 대량 생성
./run_generator.sh large_data.sql

# 2. 백그라운드로 DB 적용
nohup psql -U test -d perf_test -f large_data.sql > import.log 2>&1 &

# 3. 진행 상황 확인
tail -f import.log
```

### 시나리오 3: 특정 기간 데이터만 생성
스크립트 내부 `random_timestamp()` 함수의 범위를 수정:
```python
# 최근 30일 데이터만 생성
def random_timestamp(start_days_ago=30, end_days_ago=0):
    ...
```

## 생성되는 데이터 구조

### 사용자 (p_user)
- 1,000명의 가상 사용자
- 한국식 이름 (Faker 라이브러리 사용)
- 이메일: user{id}@example.com
- 나이: 18-65세 랜덤

### 가게 (p_store)
- 100개의 음식점
- 카테고리: 한식, 중식, 일식, 양식 등 20종
- 각 가게는 1-3개 카테고리 보유
- 영업시간: 09:00-22:00

### 메뉴 (p_menu)
- 가게당 5-15개 메뉴
- 카테고리별 특화 메뉴 (예: 한식→김치찌개, 중식→짜장면)
- 가격: 5,000원-50,000원
- 각 메뉴당 0-5개 옵션 (맵기, 사이즈 등)

### 주문 (p_order)
- 10,000건의 주문
- 주문 상태: PENDING, ACCEPTED, COOKING, READY, COMPLETED, CANCELLED
- 주문당 1-5개 메뉴 아이템
- 자동으로 총 결제 금액 계산

### 결제 (p_payment)
- 모든 주문에 대한 결제 정보
- 결제 수단: CREDIT_CARD
- 결제 상태 히스토리 추적
- 성공한 결제는 payment_key 보유

## 데이터 검증

생성된 데이터가 올바른지 확인:

```sql
-- 테이블별 레코드 수 확인
SELECT 'users' as table_name, COUNT(*) FROM p_user
UNION ALL
SELECT 'stores', COUNT(*) FROM p_store
UNION ALL
SELECT 'menus', COUNT(*) FROM p_menu
UNION ALL
SELECT 'orders', COUNT(*) FROM p_order
UNION ALL
SELECT 'payments', COUNT(*) FROM p_payment;

-- 주문 상태 분포 확인
SELECT order_status, COUNT(*) as count
FROM p_order
GROUP BY order_status
ORDER BY count DESC;

-- 가게별 주문 수 확인 (상위 10개)
SELECT s.name, COUNT(o.id) as order_count
FROM p_store s
LEFT JOIN p_order o ON s.id = o.store_id
GROUP BY s.id, s.name
ORDER BY order_count DESC
LIMIT 10;

-- 날짜별 주문 수 확인
SELECT DATE(created_at) as order_date, COUNT(*) as count
FROM p_order
GROUP BY DATE(created_at)
ORDER BY order_date DESC
LIMIT 30;
```

## 트러블슈팅

### 문제: SQL 파일이 너무 크다
**해결**: 배치로 나눠서 적용
```bash
split -l 10000 dummy_data.sql part_
for file in part_*; do
    psql -U user -d db -f $file
done
```

### 문제: 외래키 제약 위반
**해결**: 외래키 체크를 일시적으로 비활성화
```sql
-- PostgreSQL
SET session_replication_role = 'replica';
-- SQL 실행
SET session_replication_role = 'origin';
```

### 문제: 메모리 부족
**해결**: 데이터를 작은 단위로 나눠서 생성
```bash
# 2000건씩 5번 생성
for i in {1..5}; do
    # NUM_ORDERS를 2000으로 수정 후
    python3 generate_dummy_data.py > batch_${i}.sql
done
```

### 문제: 한글 깨짐
**해결**: DB 인코딩 확인 및 설정
```sql
-- PostgreSQL
CREATE DATABASE food_delivery 
WITH ENCODING 'UTF8' 
LC_COLLATE='ko_KR.UTF-8' 
LC_CTYPE='ko_KR.UTF-8';
```

## 고급 활용

### 1. 특정 시나리오 데이터 생성
스크립트를 수정하여 특정 비즈니스 시나리오 구현:
- VIP 고객 생성 (주문 횟수 많음)
- 인기 가게 설정 (주문 집중)
- 특정 시간대 주문 몰림 시뮬레이션

### 2. 데이터 익스포트/임포트
```bash
# 생성된 데이터를 CSV로 변환
python3 generate_csv_data.py

# 다른 환경으로 데이터 복사
scp dummy_data.sql user@server:/tmp/
```

### 3. CI/CD 파이프라인 통합
```yaml
# .gitlab-ci.yml 예시
test:
  script:
    - python3 generate_dummy_data.py > test_data.sql
    - psql -U test -d test_db -f test_data.sql
    - pytest tests/
```

## 성능 팁

1. **인덱스는 데이터 삽입 후 생성**: 대량 INSERT 전에는 인덱스를 삭제하고 후에 생성
2. **배치 INSERT 사용**: 가능하면 INSERT INTO ... VALUES (...), (...), (...) 형식 사용
3. **트랜잭션 사용**: BEGIN/COMMIT으로 묶어서 실행
4. **COPY 명령 활용**: PostgreSQL의 경우 COPY 명령이 INSERT보다 훨씬 빠름

## 추가 리소스

- PostgreSQL 벌크 로딩: https://www.postgresql.org/docs/current/populate.html
- MySQL 대량 데이터 임포트: https://dev.mysql.com/doc/refman/8.0/en/insert-optimization.html
- Python Faker 문서: https://faker.readthedocs.io/
