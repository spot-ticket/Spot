-- Spot 프로젝트 더미 데이터 확인 쿼리 예제
-- 더미 데이터 적용 후 이 쿼리들로 데이터를 확인할 수 있습니다.

-- ============================================
-- 1. 기본 데이터 통계
-- ============================================

-- 사용자 역할별 통계
SELECT
    role,
    COUNT(*) as count,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (), 2) as percentage
FROM p_user
WHERE is_deleted = false
GROUP BY role
ORDER BY count DESC;

-- 가게 상태별 통계
SELECT
    status,
    COUNT(*) as count,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (), 2) as percentage
FROM p_store
WHERE is_deleted = false
GROUP BY status
ORDER BY count DESC;

-- 주문 상태별 통계
SELECT
    order_status,
    COUNT(*) as count,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (), 2) as percentage
FROM p_order
GROUP BY order_status
ORDER BY count DESC;

-- 결제 상태별 통계
SELECT
    ph.payment_status,
    COUNT(*) as count
FROM p_payment_history ph
JOIN (
    SELECT payment_id, MAX(created_at) as max_created_at
    FROM p_payment_history
    GROUP BY payment_id
) latest ON ph.payment_id = latest.payment_id AND ph.created_at = latest.max_created_at
GROUP BY ph.payment_status
ORDER BY count DESC;

-- ============================================
-- 2. 리뷰 관련 통계
-- ============================================

-- 평점 분포
SELECT
    rating,
    COUNT(*) as count,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (), 2) as percentage
FROM p_review
WHERE is_deleted = false
GROUP BY rating
ORDER BY rating DESC;

-- 평균 평점 TOP 10 가게
SELECT
    s.name as store_name,
    s.road_address,
    COUNT(r.id) as review_count,
    AVG(r.rating)::numeric(3,2) as avg_rating
FROM p_store s
JOIN p_review r ON s.id = r.store_id AND r.is_deleted = false
WHERE s.status = 'APPROVED' AND s.is_deleted = false
GROUP BY s.id, s.name, s.road_address
HAVING COUNT(r.id) >= 5  -- 최소 5개 이상 리뷰
ORDER BY avg_rating DESC, review_count DESC
LIMIT 10;

-- 리뷰가 많은 가게 TOP 10
SELECT
    s.name as store_name,
    COUNT(r.id) as review_count,
    AVG(r.rating)::numeric(3,2) as avg_rating
FROM p_store s
JOIN p_review r ON s.id = r.store_id AND r.is_deleted = false
WHERE s.status = 'APPROVED' AND s.is_deleted = false
GROUP BY s.id, s.name
ORDER BY review_count DESC
LIMIT 10;

-- ============================================
-- 3. 매출 관련 통계
-- ============================================

-- 가게별 총 매출 TOP 10 (완료된 주문만)
SELECT
    s.name as store_name,
    COUNT(o.id) as completed_orders,
    SUM(p.payment_amount) as total_revenue
FROM p_store s
JOIN p_order o ON s.id = o.store_id AND o.order_status = 'COMPLETED'
JOIN p_payment p ON o.id = p.order_id
GROUP BY s.id, s.name
ORDER BY total_revenue DESC
LIMIT 10;

-- 일별 매출 추이 (최근 30일)
SELECT
    DATE(o.created_at) as order_date,
    COUNT(o.id) as order_count,
    SUM(p.payment_amount) as daily_revenue
FROM p_order o
JOIN p_payment p ON o.id = p.order_id
WHERE o.order_status = 'COMPLETED'
  AND o.created_at >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY DATE(o.created_at)
ORDER BY order_date DESC;

-- 인기 메뉴 TOP 20
SELECT
    m.name as menu_name,
    m.category,
    COUNT(oi.id) as order_count,
    SUM(oi.menu_price * oi.quantity) as total_revenue
FROM p_menu m
JOIN p_order_item oi ON m.menu_id = oi.menu_id
JOIN p_order o ON oi.order_id = o.id
WHERE o.order_status = 'COMPLETED'
GROUP BY m.menu_id, m.name, m.category
ORDER BY order_count DESC
LIMIT 20;

-- ============================================
-- 4. 사용자 활동 통계
-- ============================================

-- 주문을 많이 한 고객 TOP 10
SELECT
    u.name as customer_name,
    u.email,
    COUNT(o.id) as order_count,
    SUM(p.payment_amount) as total_spent
FROM p_user u
JOIN p_order o ON u.id = o.user_id
JOIN p_payment p ON o.id = p.order_id
WHERE u.role = 'CUSTOMER'
GROUP BY u.id, u.name, u.email
ORDER BY order_count DESC
LIMIT 10;

-- OWNER별 가게 수
SELECT
    u.name as owner_name,
    u.email,
    COUNT(DISTINCT su.store_id) as store_count
FROM p_user u
JOIN p_store_user su ON u.id = su.user_id
WHERE u.role = 'OWNER' AND su.is_deleted = false
GROUP BY u.id, u.name, u.email
ORDER BY store_count DESC;

-- ============================================
-- 5. 카테고리 통계
-- ============================================

-- 카테고리별 가게 수
SELECT
    c.name as category_name,
    COUNT(DISTINCT sc.store_id) as store_count
FROM p_category c
JOIN p_store_category sc ON c.id = sc.category_id
JOIN p_store s ON sc.store_id = s.id
WHERE c.is_deleted = false
  AND sc.is_deleted = false
  AND s.is_deleted = false
  AND s.status = 'APPROVED'
GROUP BY c.id, c.name
ORDER BY store_count DESC;

-- 카테고리별 주문 수
SELECT
    c.name as category_name,
    COUNT(DISTINCT o.id) as order_count,
    SUM(p.payment_amount) as total_revenue
FROM p_category c
JOIN p_store_category sc ON c.id = sc.category_id
JOIN p_store s ON sc.store_id = s.id
JOIN p_order o ON s.id = o.store_id AND o.order_status = 'COMPLETED'
JOIN p_payment p ON o.id = p.order_id
WHERE c.is_deleted = false
GROUP BY c.id, c.name
ORDER BY order_count DESC;

-- ============================================
-- 6. 메뉴 옵션 통계
-- ============================================

-- 가장 많이 선택된 옵션 TOP 20
SELECT
    mo.name as option_name,
    mo.detail as option_detail,
    COUNT(oio.id) as selection_count
FROM p_menu_option mo
JOIN p_order_item_option oio ON mo.option_id = oio.menu_option_id
JOIN p_order_item oi ON oio.order_item_id = oi.id
JOIN p_order o ON oi.order_id = o.id
WHERE o.order_status = 'COMPLETED'
GROUP BY mo.option_id, mo.name, mo.detail
ORDER BY selection_count DESC
LIMIT 20;

-- ============================================
-- 7. 시간대별 통계
-- ============================================

-- 요일별 주문 수
SELECT
    TO_CHAR(created_at, 'Day') as day_of_week,
    COUNT(*) as order_count
FROM p_order
WHERE order_status = 'COMPLETED'
GROUP BY TO_CHAR(created_at, 'Day'), EXTRACT(DOW FROM created_at)
ORDER BY EXTRACT(DOW FROM created_at);

-- 시간대별 주문 수
SELECT
    EXTRACT(HOUR FROM created_at) as hour,
    COUNT(*) as order_count
FROM p_order
WHERE order_status = 'COMPLETED'
GROUP BY EXTRACT(HOUR FROM created_at)
ORDER BY hour;

-- ============================================
-- 8. 주문 취소 통계
-- ============================================

-- 취소 사유별 통계
SELECT
    cancelled_by,
    COUNT(*) as cancel_count,
    ROUND(AVG(EXTRACT(EPOCH FROM (cancelled_at - created_at))/60), 2) as avg_cancel_time_minutes
FROM p_order
WHERE order_status = 'CANCELLED'
  AND cancelled_by IS NOT NULL
GROUP BY cancelled_by;

-- 취소율이 높은 가게 TOP 10
SELECT
    s.name as store_name,
    COUNT(o.id) as total_orders,
    COUNT(CASE WHEN o.order_status = 'CANCELLED' THEN 1 END) as cancelled_orders,
    ROUND(COUNT(CASE WHEN o.order_status = 'CANCELLED' THEN 1 END) * 100.0 / COUNT(o.id), 2) as cancel_rate
FROM p_store s
JOIN p_order o ON s.id = o.store_id
GROUP BY s.id, s.name
HAVING COUNT(o.id) >= 10  -- 최소 10개 이상 주문
ORDER BY cancel_rate DESC
LIMIT 10;

-- ============================================
-- 9. 결제 방법 통계
-- ============================================

-- 결제 방법별 통계
SELECT
    payment_method,
    COUNT(*) as payment_count,
    SUM(payment_amount) as total_amount,
    ROUND(AVG(payment_amount), 2) as avg_amount
FROM p_payment
GROUP BY payment_method;

-- ============================================
-- 10. 종합 대시보드 쿼리
-- ============================================

-- 전체 플랫폼 통계
SELECT
    (SELECT COUNT(*) FROM p_user WHERE role = 'CUSTOMER' AND is_deleted = false) as total_customers,
    (SELECT COUNT(*) FROM p_user WHERE role = 'OWNER' AND is_deleted = false) as total_owners,
    (SELECT COUNT(*) FROM p_store WHERE status = 'APPROVED' AND is_deleted = false) as approved_stores,
    (SELECT COUNT(*) FROM p_menu WHERE is_deleted = false AND is_available = true) as available_menus,
    (SELECT COUNT(*) FROM p_order) as total_orders,
    (SELECT COUNT(*) FROM p_order WHERE order_status = 'COMPLETED') as completed_orders,
    (SELECT SUM(payment_amount) FROM p_payment p JOIN p_order o ON p.order_id = o.id WHERE o.order_status = 'COMPLETED') as total_revenue,
    (SELECT COUNT(*) FROM p_review WHERE is_deleted = false) as total_reviews,
    (SELECT AVG(rating)::numeric(3,2) FROM p_review WHERE is_deleted = false) as avg_rating;
