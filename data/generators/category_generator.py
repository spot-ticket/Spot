"""Category Generator - 카테고리 데이터 생성"""
from .base_generator import BaseGenerator, random_timestamp

# 한국 음식 카테고리
CATEGORIES = [
    '한식', '중식', '일식', '양식', '치킨', '피자', '분식', '카페/디저트',
    '패스트푸드', '아시안', '족발/보쌈', '찜/탕', '회/초밥', '고기/구이',
    '도시락', '야식', '샐러드', '버거', '샌드위치', '베이커리'
]


class CategoryGenerator(BaseGenerator):
    """카테고리 데이터 생성"""

    def __init__(self, conn=None, cursor=None, direct_insert=False):
        super().__init__(conn, cursor, direct_insert)
        self.categories = []

    def generate(self, num_categories=20):
        """카테고리 생성"""
        print("-- Categories")
        columns = ['id', 'name', 'is_deleted', 'deleted_at', 'deleted_by',
                  'created_at', 'created_by', 'updated_at', 'updated_by']

        for i, cat_name in enumerate(CATEGORIES[:num_categories]):
            cat_id = self.generate_uuid()
            created_at = random_timestamp(180, 150)
            updated_at = self.random_updated_at(created_at)
            self.categories.append({'id': cat_id, 'name': cat_name})

            self.batch_insert('p_category', columns, (
                cat_id, cat_name, False, None, None, created_at, 0, updated_at, 0
            ))

        self.flush_batch('p_category')
        print(f"  Created {len(self.categories)} categories")
        print()

    def get_categories(self):
        """생성된 카테고리 목록 반환"""
        return self.categories
