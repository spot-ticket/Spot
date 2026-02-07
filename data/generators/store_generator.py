"""Store Generator - 가게 데이터 생성"""
import random
from faker import Faker
from .base_generator import BaseGenerator, random_timestamp

fake = Faker('ko_KR')

# 종로구 도로명 주소 템플릿
JONGNO_ROADS = [
    '종로', '세종대로', '율곡로', '창경궁로', '삼일대로', '대학로', '혜화로',
    '자하문로', '북촌로', '삼청로', '윤보선길', '계동길', '가회로', '인사동길',
    '청계천로', '돈화문로', '종로1가', '종로2가', '종로3가', '종로4가', '종로5가'
]

STORE_STATUSES = ['PENDING', 'APPROVED', 'REJECTED']


def generate_jongno_address():
    """종로구 도로명 주소 생성"""
    road = random.choice(JONGNO_ROADS)
    building_num = random.randint(1, 300)
    return f"서울특별시 종로구 {road} {building_num}"


class StoreGenerator(BaseGenerator):
    """가게 데이터 생성"""

    def __init__(self, conn=None, cursor=None, direct_insert=False, users=None,
                 owner_users=None, categories=None):
        super().__init__(conn, cursor, direct_insert)
        self.users = users or []
        self.owner_users = owner_users or []
        self.categories = categories or []
        self.stores = []

    def generate(self, num_stores=1000):
        """가게 생성"""
        print("-- Stores")
        store_columns = ['id', 'name', 'road_address', 'address_detail', 'phone_number',
                        'open_time', 'close_time', 'status', 'is_deleted', 'deleted_at',
                        'deleted_by', 'created_at', 'created_by', 'updated_at', 'updated_by']
        sc_columns = ['id', 'store_id', 'category_id', 'created_at', 'created_by',
                     'updated_at', 'updated_by', 'is_deleted', 'deleted_at', 'deleted_by']
        su_columns = ['id', 'store_id', 'user_id', 'created_at', 'created_by',
                     'updated_at', 'updated_by', 'is_deleted', 'deleted_at', 'deleted_by']

        for i in range(num_stores):
            store_id = self.generate_uuid()
            store_name = fake.company() + " " + random.choice(['식당', '레스토랑', '카페', '치킨', '피자'])
            created_at = random_timestamp(365, 30)
            updated_at = self.random_updated_at(created_at)
            created_by = random.choice(self.users) if self.users else 1
            updated_by = random.choice(self.users) if self.users else 1

            # 85% APPROVED, 10% PENDING, 5% REJECTED
            store_status = random.choices(STORE_STATUSES, weights=[0.1, 0.85, 0.05])[0]
            is_deleted = random.random() < 0.05
            deleted_at = self.random_updated_at(created_at) if is_deleted else None
            deleted_by = random.choice(self.users) if is_deleted and self.users else None

            self.stores.append({
                'id': store_id,
                'name': store_name,
                'created_at': created_at,
                'status': store_status
            })

            # Store
            self.batch_insert('p_store', store_columns, (
                store_id, store_name, generate_jongno_address(),
                fake.building_name() or f'{random.randint(1, 10)}층',
                fake.phone_number(), '09:00:00', '22:00:00', store_status,
                is_deleted, deleted_at, deleted_by, created_at, created_by, updated_at, updated_by
            ))

            # Store-Category 연결 (1-3개 카테고리)
            if self.categories:
                for cat in random.sample(self.categories, random.randint(1, min(3, len(self.categories)))):
                    self.batch_insert('p_store_category', sc_columns, (
                        self.generate_uuid(), store_id, cat['id'], created_at, created_by,
                        self.random_updated_at(created_at), random.choice(self.users) if self.users else 1,
                        False, None, None
                    ))

            # Store-User 연결 (점주)
            owner_id = random.choice(self.owner_users) if self.owner_users else random.choice(self.users) if self.users else 1
            self.batch_insert('p_store_user', su_columns, (
                self.generate_uuid(), store_id, owner_id, created_at, created_by,
                self.random_updated_at(created_at), random.choice(self.users) if self.users else 1,
                False, None, None
            ))

        self.flush_batch('p_store')
        self.flush_batch('p_store_category')
        self.flush_batch('p_store_user')
        print(f"  Created {len(self.stores)} stores")
        print()

    def get_stores(self):
        """생성된 가게 목록 반환"""
        return self.stores
