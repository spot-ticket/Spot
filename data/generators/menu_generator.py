"""Menu Generator - 메뉴 및 옵션 데이터 생성"""
import random
from datetime import timedelta
from faker import Faker
from .base_generator import BaseGenerator

fake = Faker('ko_KR')

# 메뉴 이름 템플릿
MENU_TEMPLATES = {
    '한식': ['김치찌개', '된장찌개', '불고기', '비빔밥', '제육볶음', '갈비탕', '삼계탕'],
    '중식': ['짜장면', '짬뽕', '탕수육', '깐풍기', '마파두부', '울면', '양장피'],
    '일식': ['초밥세트', '우동', '소바', '돈까스', '규동', '라멘', '카레라이스'],
    '양식': ['스테이크', '파스타', '리조또', '그라탕', '오믈렛', '필라프'],
    '치킨': ['후라이드', '양념치킨', '간장치킨', '반반치킨', '파닭', '순살치킨'],
    '피자': ['페퍼로니', '콤비네이션', '불고기', '포테이토', '치즈크러스트', '슈퍼슈프림'],
    '분식': ['떡볶이', '김밥', '튀김', '순대', '라면', '우동', '쫄면'],
    '카페/디저트': ['아메리카노', '카페라떼', '케이크', '마카롱', '크로플', '와플'],
}

# 메뉴 옵션 템플릿
OPTION_TEMPLATES = [
    ('맵기 선택', ['안맵게', '보통', '맵게', '아주맵게']),
    ('사이즈', ['Small', 'Medium', 'Large']),
    ('추가 토핑', ['치즈 추가', '야채 추가', '고기 추가', '계란 추가']),
    ('음료', ['콜라', '사이다', '제로콜라', '환타']),
]

# 원산지 정보
ORIGIN_TEMPLATES = [
    ('쇠고기', ['한우', '미국산', '호주산']),
    ('돼지고기', ['국내산', '미국산', '스페인산']),
    ('닭고기', ['국내산', '브라질산']),
    ('쌀', ['국내산', '캘리포니아산']),
    ('김치', ['국내산 배추', '국내산 고춧가루']),
    ('해산물', ['국내산', '노르웨이산', '칠레산']),
]


class MenuGenerator(BaseGenerator):
    """메뉴 데이터 생성"""

    def __init__(self, conn=None, cursor=None, direct_insert=False, users=None, stores=None):
        super().__init__(conn, cursor, direct_insert)
        self.users = users or []
        self.stores = stores or []
        self.menus = []
        self.menu_options = []

    def generate(self, menus_per_store=(5, 15), options_per_menu=(0, 5), origins_per_menu=(0, 3)):
        """메뉴 생성"""
        print("-- Menus")
        menu_columns = ['menu_id', 'store_id', 'name', 'category', 'price', 'description',
                       'image_url', 'is_available', 'is_hidden', 'is_deleted', 'deleted_at',
                       'deleted_by', 'created_at', 'created_by', 'updated_at', 'updated_by']
        option_columns = ['option_id', 'menu_id', 'name', 'detail', 'price', 'is_available',
                         'is_hidden', 'is_deleted', 'deleted_at', 'deleted_by', 'created_at',
                         'created_by', 'updated_at', 'updated_by']
        origin_columns = ['id', 'menu_id', 'origin_name', 'ingredient_name', 'is_deleted',
                         'deleted_at', 'deleted_by', 'created_at', 'created_by',
                         'updated_at', 'updated_by']

        approved_stores = [s for s in self.stores if s['status'] == 'APPROVED']

        for store in approved_stores:
            store_category = random.choice(list(MENU_TEMPLATES.keys()))
            menu_names = MENU_TEMPLATES.get(store_category, ['메뉴'])

            for j in range(random.randint(*menus_per_store)):
                menu_id = self.generate_uuid()
                menu_name = random.choice(menu_names)
                if random.random() > 0.5:
                    menu_name += f" {random.choice(['세트', '특선', '프리미엄', '스페셜'])}"

                price = random.randint(5, 50) * 1000
                created_at = store['created_at'] + timedelta(days=random.randint(0, 30))
                created_by = random.choice(self.users) if self.users else 1

                self.menus.append({
                    'id': menu_id,
                    'store_id': store['id'],
                    'name': menu_name,
                    'price': price
                })

                # Menu
                self.batch_insert('p_menu', menu_columns, (
                    menu_id, store['id'], menu_name, store_category, price, fake.sentence(), None,
                    random.random() > 0.1, random.random() < 0.05, False, None, None,
                    created_at, created_by, self.random_updated_at(created_at),
                    random.choice(self.users) if self.users else 1
                ))

                # Menu Options - 같은 메뉴에 동일한 옵션 이름이 중복되지 않도록
                num_options = random.randint(*options_per_menu)
                if num_options > 0:
                    # 사용 가능한 옵션 템플릿을 랜덤하게 섞고, 필요한 만큼만 선택
                    available_templates = random.sample(OPTION_TEMPLATES, min(num_options, len(OPTION_TEMPLATES)))

                    for option_template in available_templates:
                        option_id = self.generate_uuid()
                        option_detail = random.choice(option_template[1])
                        option_price = random.choice([0, 500, 1000, 2000, 3000])

                        self.menu_options.append({
                            'id': option_id,
                            'menu_id': menu_id,
                            'name': option_template[0],
                            'detail': option_detail,
                            'price': option_price
                        })

                        self.batch_insert('p_menu_option', option_columns, (
                            option_id, menu_id, option_template[0], option_detail,
                            option_price, random.random() > 0.05, random.random() < 0.02,
                            False, None, None, created_at, created_by,
                            self.random_updated_at(created_at), random.choice(self.users) if self.users else 1
                        ))

                # Menu Origins
                for l in range(random.randint(*origins_per_menu)):
                    origin_template = random.choice(ORIGIN_TEMPLATES)
                    origin_id = self.generate_uuid()

                    self.batch_insert('p_origin', origin_columns, (
                        origin_id, menu_id, random.choice(origin_template[1]), origin_template[0],
                        False, None, None, created_at, created_by,
                        self.random_updated_at(created_at), random.choice(self.users) if self.users else 1
                    ))

        self.flush_batch('p_menu')
        self.flush_batch('p_menu_option')
        self.flush_batch('p_origin')
        print(f"  Created {len(self.menus)} menus with {len(self.menu_options)} options")
        print()

    def get_menus(self):
        """생성된 메뉴 목록 반환"""
        return self.menus

    def get_menu_options(self):
        """생성된 메뉴 옵션 목록 반환"""
        return self.menu_options
