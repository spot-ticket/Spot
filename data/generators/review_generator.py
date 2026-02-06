"""Review Generator - 리뷰 데이터 생성"""
import random
from .base_generator import BaseGenerator, random_timestamp


class ReviewGenerator(BaseGenerator):
    """리뷰 데이터 생성"""

    def __init__(self, conn=None, cursor=None, direct_insert=False, stores=None, orders=None):
        super().__init__(conn, cursor, direct_insert)
        self.stores = stores or []
        self.orders = orders or []
        self.reviews = []

    def generate(self, reviews_per_store=(0, 20)):
        """리뷰 생성"""
        print("-- Reviews")
        review_columns = ['id', 'store_id', 'user_id', 'rating', 'content', 'is_deleted',
                         'deleted_at', 'deleted_by', 'created_at', 'created_by',
                         'updated_at', 'updated_by']

        review_contents = {
            5: ['정말 맛있어요!', '최고입니다', '또 시켜먹을게요', '강추합니다', '맛있고 친절해요'],
            4: ['맛있어요', '괜찮습니다', '좋아요', '다음에도 주문할게요'],
            3: ['보통이에요', '나쁘지 않아요', '그럭저럭 먹을만해요'],
            2: ['별로에요', '기대 이하였어요', '다시는 안시킬듯'],
            1: ['최악이에요', '너무 실망했어요', '돈아까워요']
        }

        approved_stores = [s for s in self.stores if s['status'] == 'APPROVED']

        for store in approved_stores:
            completed_orders = [o for o in self.orders
                              if o['store_id'] == store['id'] and o['status'] == 'COMPLETED']
            if not completed_orders:
                continue

            reviewed_users = set()
            num_reviews = random.randint(*reviews_per_store)

            for _ in range(num_reviews):
                # 아직 리뷰하지 않은 사용자의 주문 선택
                available_orders = [o for o in completed_orders
                                  if o['user_id'] not in reviewed_users]
                if not available_orders:
                    available_orders = completed_orders

                order = random.choice(available_orders)
                user_id = order['user_id']
                reviewed_users.add(user_id)

                # 평점 (높은 평점이 더 많이 나오도록)
                rating = random.choices([1, 2, 3, 4, 5],
                                       weights=[0.02, 0.03, 0.10, 0.35, 0.50])[0]
                content = random.choice(review_contents[rating]) if random.random() > 0.2 else None
                created_at = random_timestamp(80, 0)

                review_id = self.generate_uuid()
                self.reviews.append({
                    'id': review_id,
                    'store_id': store['id'],
                    'user_id': user_id,
                    'rating': rating
                })

                self.batch_insert('p_review', review_columns, (
                    review_id, store['id'], user_id, rating, content,
                    False, None, None, created_at, user_id,
                    self.random_updated_at(created_at), user_id
                ))

        self.flush_batch('p_review')
        print(f"  Created {len(self.reviews)} reviews")
        print()

    def get_reviews(self):
        """생성된 리뷰 목록 반환"""
        return self.reviews
