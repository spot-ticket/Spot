  # Dummy Data Generator for Spot Food Delivery Platform

  이 디렉토리에는 Spot 프로젝트의 더미 데이터를 생성하는 Python 스크립트가 포함되어 있습니다.

  ## 파일 구조

  - `generate_dummy_data.py`: 더미 데이터 생성 스크립트
  - `dummy_data.sql`: 생성된 SQL INSERT 문 (48MB)

  ## 생성된 데이터 통계

  최근 생성된 데이터 (기본 설정 기준):

  | 테이블 | 레코드 수 | 설명 |
  |--------|----------|------|
  | p_category | 20 | 카테고리 (한식, 중식, 일식 등) |
  | p_user | 1,000 | 사용자 (OWNER: 100명, CUSTOMER: 900명) |
  | p_user_auth | 1,000 | 사용자 인증 정보 |
  | p_store | 1,000 | 가게 (APPROVED: 850개, PENDING: 100개, REJECTED: 50개) |
  | p_store_user | 1,000 | 가게-사용자 연결 (점주 할당) |
  | p_store_category | 1,989 | 가게-카테고리 연결 |
  | p_menu | 8,440 | 메뉴 (APPROVED 가게만) |
  | p_menu_option | 21,055 | 메뉴 옵션 |
  | p_origin | 12,734 | 메뉴 원산지 정보 |
  | p_order | 10,000 | 주문 |
  | p_order_item | 30,043 | 주문 항목 |
  | p_order_item_option | 17,670 | 주문 항목 옵션 |
  | p_payment | 10,000 | 결제 정보 |
  | p_payment_history | 10,000 | 결제 이력 |
  | p_payment_key | 9,023 | 결제 키 (완료된 결제만) |
  | p_review | 8,305 | 리뷰 |
  | **총계** | **143,279** | **전체 INSERT 문** |

  ## 주요 특징

  ### 1. 테이블 관계 반영
  - **외래 키 관계**: 모든 테이블 간 외래 키 관계를 정확히 반영
  - **연관 테이블**: 가게-카테고리, 가게-사용자, 주문-메뉴 등 다대다 관계 포함
  - **계층 구조**: 주문 → 주문항목 → 주문항목옵션 계층 구조 유지

  ### 2. 현실적인 데이터
  - **사용자 역할**: OWNER 10%, CUSTOMER 90%
  - **가게 상태**: APPROVED 85%, PENDING 10%, REJECTED 5%
  - **주문 상태 분포**: COMPLETED 50%, 기타 상태들 분산
  - **리뷰 평점**: 4-5점이 85% (현실적인 긍정 편향)
  - **삭제된 데이터**: 일부 가게 5% 삭제 상태 (soft delete)

  ### 3. 비즈니스 로직 준수
  - APPROVED 가게만 메뉴 보유
  - 완료된 주문만 리뷰 작성 가능
  - 주문 상태별 타임스탬프 자동 설정
  - 결제 완료된 주문만 payment_key 보유

  ### 4. 타임스탬프 관리
  - `created_at`: 과거 365일 이내 랜덤 생성
  - `updated_at`: created_at 이후 0-30일 사이
  - 주문 타임스탬프: 상태별 논리적 순서 유지 (결제 → 수락 → 조리 → 완료)

  ## 설정 커스터마이징

  `generate_dummy_data.py` 파일 상단의 설정값을 수정하여 생성할 데이터 양을 조절할 수 있습니다:

  ```python
  # 설정
  NUM_USERS = 1000              # 생성할 사용자 수
  NUM_STORES = 1000             # 생성할 가게 수
  NUM_CATEGORIES = 20           # 카테고리 수
  NUM_MENUS_PER_STORE = (5, 15) # 가게당 메뉴 수 범위
  NUM_OPTIONS_PER_MENU = (0, 5) # 메뉴당 옵션 수 범위
  NUM_ORIGINS_PER_MENU = (0, 3) # 메뉴당 원산지 정보 수
  NUM_ORDERS = 10000            # 생성할 주문 수
  ITEMS_PER_ORDER = (1, 5)      # 주문당 아이템 수
  NUM_REVIEWS_PER_STORE = (0, 20) # 가게당 리뷰 수 범위
  OWNER_RATIO = 0.1             # 전체 사용자 중 OWNER 비율
  ```

  ## 사용 방법

  ### 1. 더미 데이터 생성

  ```bash
  cd /Users/yoonchul/Documents/Spot/data
  python3 generate_dummy_data.py > dummy_data.sql
  ```

  ### 2. 데이터베이스에 적용

  **주의**: 기존 데이터가 모두 삭제됩니다!

  ```bash
  # PostgreSQL에 연결
  psql -h localhost -U your_username -d spot_db

  # 기존 데이터 삭제 (선택사항)
  TRUNCATE TABLE p_review, p_payment_key, p_payment_history, p_payment,
                  p_order_item_option, p_order_item, p_order,
                  p_origin, p_menu_option, p_menu,
                  p_store_category, p_store_user, p_store,
                  p_user_auth, p_user, p_category CASCADE;

  # 더미 데이터 적용
  \i /Users/yoonchul/Documents/Spot/data/dummy_data.sql

  # 또는 bash에서 직접 실행
  psql -h localhost -U your_username -d spot_db < dummy_data.sql
  ```

  ### 3. 비밀번호 검증 테스트 (선택사항)

  생성된 비밀번호 해시가 올바른지 테스트할 수 있습니다:

  ```bash
  # 특정 사용자의 비밀번호 검증
  python3 test_password.py user1
  python3 test_password.py user50

  # 여러 사용자 한번에 테스트
  for user in user1 user5 user10; do python3 test_password.py $user; done
  ```

  출력 예시:
  ```
  Testing password for user: user1
  ------------------------------------------------------------
  ✓ Password verification SUCCESSFUL for 'user1'
    Username: user1
    Email: user1@example.com
    Password: user1
    Hash: $2b$10$d3Rpl5gTRoFYtixA3zkolurdrQ.mlKJFc/aCoG5u1MAdmOzY0ZbN2
  ```

  ### 4. 데이터 확인

  ```sql
  -- 사용자 수 확인
  SELECT role, COUNT(*) FROM p_user GROUP BY role;

  -- 가게 상태별 수 확인
  SELECT status, COUNT(*) FROM p_store GROUP BY status;

  -- 주문 상태별 수 확인
  SELECT order_status, COUNT(*) FROM p_order GROUP BY order_status;

  -- 가게별 평균 평점 확인
  SELECT s.name, AVG(r.rating)::numeric(3,2) as avg_rating, COUNT(r.id) as review_count
  FROM p_store s
  LEFT JOIN p_review r ON s.id = r.store_id AND r.is_deleted = false
  WHERE s.status = 'APPROVED'
  GROUP BY s.id, s.name
  ORDER BY avg_rating DESC
  LIMIT 10;
  ```

  ## 로그인 정보

  생성된 사용자 계정으로 로그인할 수 있습니다:

  ### 고정 테스트 계정

  먼저 생성되는 고정 테스트 계정:

  | ID | 닉네임 | 이메일 | 비밀번호 | 역할 |
  |----|--------|--------|----------|------|
  | 1 | master | master@example.com | master | MASTER |
  | 2 | owner | owner@example.com | owner | OWNER |
  | 3 | chef | chef@example.com | chef | CHEF |
  | 4 | customer | customer@example.com | customer | CUSTOMER |

  **사용 예시:**
  - **관리자 계정**: master@example.com / master
  - **점주 계정**: owner@example.com / owner
  - **주방장 계정**: chef@example.com / chef
  - **고객 계정**: customer@example.com / customer

  ### 일반 테스트 계정

  나머지 사용자는 자동 생성됩니다:

  | 사용자 ID | 이메일 | 비밀번호 | 역할 |
  |----------|--------|---------|------|
  | 5-14 | user5@example.com ~ user14@example.com | user5 ~ user14 | OWNER (10명) |
  | 15-100 | user15@example.com ~ user100@example.com | user15 ~ user100 | CUSTOMER |

  **패턴**: 모든 사용자의 비밀번호는 닉네임과 동일합니다 (user5, user6, user7, ...)

  ## 생성된 데이터 특징

  ### 주소 데이터
  - 모든 가게는 **종로구** 도로명 주소 사용
  - 21개 종로구 주요 도로 (종로, 세종대로, 율곡로 등)

  ### 메뉴 데이터
  - 카테고리별 현실적인 메뉴명 (한식: 김치찌개, 중식: 짜장면 등)
  - 가격: 5,000원 ~ 50,000원 (1,000원 단위)
  - 옵션: 맵기 선택, 사이즈, 추가 토핑, 음료 등

  ### 주문 데이터
  - 주문 번호: ORD00000001 ~ ORD00010000 형식
  - 픽업 시간: 주문 시간 + 30~90분
  - 요청사항: "빨리요", "문앞에 놔주세요", "벨 누르지 마세요" 등

  ### 리뷰 데이터
  - 평점별 내용 자동 생성
  - 평점 5: "정말 맛있어요!", "최고입니다" 등
  - 평점 1: "최악이에요", "너무 실망했어요" 등

  ## 의존성

  ```bash
  pip install faker bcrypt
  ```

  - **faker**: 한국어 더미 데이터 생성
  - **bcrypt**: 비밀번호 해시 생성 (선택사항, 없으면 placeholder 사용)

  ## 주의사항

  1. **대용량 파일**: 생성된 SQL 파일은 약 48MB입니다.
  2. **실행 시간**: 데이터 생성에 약 10-20초, DB 적용에 약 1-2분 소요됩니다.
  3. **메모리**: 충분한 메모리가 필요합니다 (권장: 2GB 이상).
  4. **백업**: 프로덕션 환경에서는 **절대 사용하지 마세요**. 개발/테스트 환경 전용입니다.
  5. **순서**: 테이블 생성 순서가 중요합니다. 외래 키 참조 관계를 고려하여 올바른 순서로 생성됩니다.

  ## 테이블 생성 순서

  스크립트는 다음 순서로 데이터를 생성합니다:

  1. `p_category` (카테고리 - 독립 테이블)
  2. `p_user` + `p_user_auth` (사용자 + 인증)
  3. `p_store` (가게)
    - `p_store_category` (가게-카테고리 연결)
    - `p_store_user` (가게-사용자 연결)
  4. `p_menu` (메뉴)
    - `p_menu_option` (메뉴 옵션)
    - `p_origin` (원산지 정보)
  5. `p_order` (주문)
    - `p_order_item` (주문 항목)
      - `p_order_item_option` (주문 항목 옵션)
    - `p_payment` (결제)
      - `p_payment_history` (결제 이력)
      - `p_payment_key` (결제 키)
  6. `p_review` (리뷰)

  ## 트러블슈팅

  ### 오류: "duplicate key value violates unique constraint"
  - 해결: 기존 데이터를 완전히 삭제한 후 다시 실행하세요.

  ### 오류: "foreign key constraint violation"
  - 해결: 테이블 TRUNCATE 시 CASCADE 옵션을 사용하세요.

  ### 실행 속도가 느림
  - 해결: `NUM_*` 설정값을 줄여서 데이터 양을 감소시키세요.

  ## 라이선스

  이 스크립트는 Spot 프로젝트의 일부이며, 개발 및 테스트 목적으로만 사용됩니다.
