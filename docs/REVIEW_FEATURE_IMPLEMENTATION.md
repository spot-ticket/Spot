# Review Feature Implementation Summary

## Overview
스토어별 리뷰 기능이 구현되었습니다. 사용자는 가게에 대한 리뷰를 작성하고, 수정하고, 삭제할 수 있으며, 모든 리뷰는 별도의 테이블에서 관리됩니다.

## Backend Implementation

### 1. Entity Layer
**`ReviewEntity.java`** - 리뷰 엔티티
- 위치: `/src/main/java/com/example/Spot/review/domain/entity/ReviewEntity.java`
- 필드:
  - `id`: UUID (Primary Key)
  - `store`: 가게 (ManyToOne)
  - `user`: 작성자 (ManyToOne)
  - `rating`: 별점 (1-5)
  - `content`: 리뷰 내용
- `UpdateBaseEntity`를 상속하여 soft delete 지원

### 2. Repository Layer
**`ReviewRepository.java`** - 리뷰 저장소
- 위치: `/src/main/java/com/example/Spot/review/domain/repository/ReviewRepository.java`
- 주요 메서드:
  - `findByStoreIdAndIsDeletedFalse()`: 특정 가게의 활성 리뷰 조회
  - `findByUserIdAndIsDeletedFalse()`: 특정 사용자의 리뷰 조회
  - `getAverageRatingByStoreId()`: 평균 별점 계산
  - `countByStoreIdAndIsDeletedFalse()`: 리뷰 개수 조회

### 3. Service Layer
**`ReviewService.java`** - 리뷰 비즈니스 로직
- 위치: `/src/main/java/com/example/Spot/review/application/service/ReviewService.java`
- 주요 기능:
  - `createReview()`: 리뷰 작성
  - `getStoreReviews()`: 가게 리뷰 목록 조회 (페이징)
  - `getStoreReviewStats()`: 평균 별점 및 리뷰 개수 통계
  - `updateReview()`: 리뷰 수정 (작성자만 가능)
  - `deleteReview()`: 리뷰 삭제 (작성자 또는 관리자만 가능)

### 4. Controller Layer
**`ReviewController.java`** - REST API 엔드포인트
- 위치: `/src/main/java/com/example/Spot/review/presentation/controller/ReviewController.java`
- API 엔드포인트:
  - `POST /api/reviews`: 리뷰 작성 (인증 필요)
  - `GET /api/reviews/stores/{storeId}`: 가게 리뷰 목록 조회 (공개)
  - `GET /api/reviews/stores/{storeId}/stats`: 리뷰 통계 조회 (공개)
  - `PATCH /api/reviews/{reviewId}`: 리뷰 수정 (인증 필요)
  - `DELETE /api/reviews/{reviewId}`: 리뷰 삭제 (인증 필요)

### 5. DTOs
**Request DTOs:**
- `ReviewCreateRequest`: 리뷰 작성 요청 (storeId, rating, content)
- `ReviewUpdateRequest`: 리뷰 수정 요청 (rating, content)

**Response DTOs:**
- `ReviewResponse`: 리뷰 응답 데이터
- `ReviewStatsResponse`: 리뷰 통계 (averageRating, totalReviews)

### 6. Database Relationship
**`StoreEntity.java`** 수정
- `reviews` 필드 추가: `List<ReviewEntity>` (OneToMany)
- Cascade ALL, orphanRemoval true
- FetchType LAZY

## Frontend Implementation

### 1. API Client
**`lib/review.ts`** - 리뷰 API 클라이언트
- 위치: `/FE/spot/lib/review.ts`
- 타입 정의:
  - `Review`: 리뷰 인터페이스
  - `ReviewStats`: 리뷰 통계 인터페이스
  - `ReviewCreateRequest`, `ReviewUpdateRequest`
- API 함수:
  - `createReview()`, `getStoreReviews()`, `getStoreReviewStats()`
  - `updateReview()`, `deleteReview()`

### 2. Components

**`ReviewWriteForm.tsx`** - 리뷰 작성 폼
- 위치: `/FE/spot/components/review/ReviewWriteForm.tsx`
- 기능:
  - 별점 선택 (1-5점, 별 아이콘 클릭)
  - 리뷰 내용 입력 (최대 500자, 선택사항)
  - 로그인 여부 확인
  - 작성 완료 시 콜백 호출

**`ReviewList.tsx`** - 리뷰 목록 및 통계
- 위치: `/FE/spot/components/review/ReviewList.tsx`
- 기능:
  - 평균 별점 및 총 리뷰 개수 표시
  - 리뷰 목록 페이지네이션
  - 본인 리뷰 수정/삭제 기능
  - 인라인 편집 모드
  - 별점 시각화 (★ 아이콘)

### 3. Store Detail Page Integration
**`app/stores/[storeId]/page.tsx`** 수정
- 탭 네비게이션 추가 (메뉴/리뷰)
- 리뷰 탭에서 `ReviewWriteForm`과 `ReviewList` 표시
- 리뷰 작성 완료 시 목록 자동 갱신

## Features

### User Features
1. **리뷰 작성**
   - 로그인한 사용자만 작성 가능
   - 별점 필수, 내용 선택
   - 별 아이콘 클릭으로 직관적인 별점 선택

2. **리뷰 조회**
   - 로그인 없이 누구나 조회 가능
   - 페이지네이션 지원 (10개씩)
   - 평균 별점 및 총 리뷰 개수 표시

3. **리뷰 수정**
   - 본인이 작성한 리뷰만 수정 가능
   - 인라인 편집 모드
   - 별점 및 내용 수정 가능

4. **리뷰 삭제**
   - 본인 또는 관리자만 삭제 가능
   - Soft delete 방식으로 데이터 보존
   - 삭제 확인 다이얼로그

### Admin Features
- 모든 리뷰 삭제 권한
- 부적절한 리뷰 관리

## Database Schema

```sql
CREATE TABLE p_review (
    id UUID PRIMARY KEY,
    store_id UUID NOT NULL REFERENCES p_store(id),
    user_id INTEGER NOT NULL REFERENCES p_user(id),
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    content TEXT,
    created_at TIMESTAMP NOT NULL,
    created_by INTEGER NOT NULL,
    updated_at TIMESTAMP,
    updated_by INTEGER,
    is_deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_at TIMESTAMP,
    deleted_by INTEGER
);

CREATE INDEX idx_review_store_id ON p_review(store_id);
CREATE INDEX idx_review_user_id ON p_review(user_id);
CREATE INDEX idx_review_is_deleted ON p_review(is_deleted);
```

## Security & Validation

### Backend Validation
- 별점: 1-5 사이 필수 값
- 가게 존재 여부 확인
- 사용자 인증 확인
- 작성자 본인 확인 (수정/삭제 시)

### Frontend Validation
- 로그인 여부 확인
- 별점 선택 필수
- 리뷰 내용 최대 500자 제한

## API Examples

### 리뷰 작성
```bash
POST /api/reviews
Authorization: Bearer {token}
Content-Type: application/json

{
  "storeId": "uuid-here",
  "rating": 5,
  "content": "정말 맛있어요!"
}
```

### 리뷰 목록 조회
```bash
GET /api/reviews/stores/{storeId}?page=0&size=10
```

### 리뷰 통계 조회
```bash
GET /api/reviews/stores/{storeId}/stats

Response:
{
  "result": {
    "averageRating": 4.5,
    "totalReviews": 128
  }
}
```

## Next Steps (Optional Enhancements)

1. **이미지 업로드**: 리뷰에 사진 첨부 기능
2. **신고 기능**: 부적절한 리뷰 신고
3. **좋아요/싫어요**: 리뷰 유용성 평가
4. **답글 기능**: 가게 사장님 답변
5. **필터링**: 별점별, 최신순/인기순 정렬
6. **주문 검증**: 실제 주문한 사용자만 리뷰 작성 가능

## Testing

Backend 서버 재시작 후 다음 사항 테스트:
1. 가게 상세 페이지에서 리뷰 탭 확인
2. 리뷰 작성 (별점 선택 및 내용 입력)
3. 작성한 리뷰 표시 확인
4. 본인 리뷰 수정/삭제
5. 평균 별점 및 리뷰 개수 통계 확인
6. 페이지네이션 동작 확인
