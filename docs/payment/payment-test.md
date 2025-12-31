### 엔티티별 테스트 체크리스트
- PaymentEntity (결제 도메인)  
    - 상태 전이(State Transition): READY → SUCCESS 등의 정상 흐름 확인.

    - 불변성: SUCCESS, CANCELLED 등 터미널 상태에서 다른 상태로 변경 시도 시 IllegalStateException 발생 확인.

    - 기본값: 빌더 생성 시 상태 미지정 시 READY로 초기화되는지 확인.

- PaymentItemEntity (결제-주문 매핑)   
    - 필수값 검증: 생성 시 Payment나 Order가 null이면 IllegalArgumentException 발생 확인.

    - 연관관계: 결제 정보와 주문 정보가 정확히 매핑되는지 확인.

- PaymentCancelEntity (결제 취소)   
    - 취소 사유: 사유가 공백이거나 null일 때 생성 차단 여부.

    - 멱등성 키: cancelIdempotency가 중복 없이 저장되는지 확인.