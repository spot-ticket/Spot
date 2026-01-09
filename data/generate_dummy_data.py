import random
import uuid
from datetime import datetime, timedelta
from faker import Faker

fake = Faker('ko_KR')

# 설정
NUM_USERS = 1000
NUM_STORES = 1000
NUM_CATEGORIES = 20
NUM_MENUS_PER_STORE = (5, 15)  # 가게당 메뉴 수 범위
NUM_OPTIONS_PER_MENU = (0, 5)  # 메뉴당 옵션 수 범위
NUM_ORDERS = 10000
ITEMS_PER_ORDER = (1, 5)  # 주문당 아이템 수

# 한국 음식 카테고리
CATEGORIES = [
    '한식', '중식', '일식', '양식', '치킨', '피자', '분식', '카페/디저트',
    '패스트푸드', '아시안', '족발/보쌈', '찜/탕', '회/초밥', '고기/구이',
    '도시락', '야식', '샐러드', '버거', '샌드위치', '베이커리'
]

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

# 종로구 도로명 주소 템플릿
JONGNO_ROADS = [
    '종로', '세종대로', '율곡로', '창경궁로', '삼일대로', '대학로', '혜화로',
    '자하문로', '북촌로', '삼청로', '윤보선길', '계동길', '가회로', '인사동길',
    '청계천로', '돈화문로', '종로1가', '종로2가', '종로3가', '종로4가', '종로5가'
]

def generate_jongno_address():
    """종로구 도로명 주소 생성"""
    road = random.choice(JONGNO_ROADS)
    building_num = random.randint(1, 300)
    return f"서울특별시 종로구 {road} {building_num}"

# 주문 상태
ORDER_STATUSES = ['PENDING', 'ACCEPTED', 'COOKING', 'READY', 'COMPLETED', 'CANCELLED']
PAYMENT_STATUSES = ['READY', 'IN_PROGRESS', 'DONE', 'CANCELLED', 'ABORTED']

def generate_timestamp(days_ago=0, hours_ago=0):
    """과거 타임스탬프 생성"""
    return datetime.now() - timedelta(days=days_ago, hours=hours_ago)

def random_timestamp(start_days_ago=90, end_days_ago=0):
    """랜덤 과거 타임스탬프"""
    days = random.randint(end_days_ago, start_days_ago)
    hours = random.randint(0, 23)
    minutes = random.randint(0, 59)
    return generate_timestamp(days_ago=days) + timedelta(hours=hours, minutes=minutes)

def sql_format(value):
    """SQL 값 포맷팅"""
    if value is None:
        return 'NULL'
    elif isinstance(value, bool):
        return str(value).lower()
    elif isinstance(value, (int, float)):
        return str(value)
    elif isinstance(value, datetime):
        return f"'{value.strftime('%Y-%m-%d %H:%M:%S')}'"
    else:
        return f"'{str(value).replace(chr(39), chr(39)+chr(39))}'"  # SQL escape

class DataGenerator:
    def __init__(self):
        self.users = []
        self.categories = []
        self.stores = []
        self.menus = []
        self.menu_options = []
        self.orders = []

    def random_updated_at(self, created_at):
        """created_at 이후의 랜덤한 updated_at 생성"""
        days_diff = random.randint(0, 30)
        hours_diff = random.randint(0, 23)
        return created_at + timedelta(days=days_diff, hours=hours_diff)

    def random_updated_by(self, exclude_id=None):
        """실제 생성된 user_id 중에서 랜덤 선택"""
        if not self.users:
            return 1
        available = [u for u in self.users if u != exclude_id] if exclude_id else self.users
        return random.choice(available) if available else self.users[0]
        
    def generate_categories(self):
        """카테고리 생성"""
        print("-- Categories")
        for i, cat_name in enumerate(CATEGORIES[:NUM_CATEGORIES]):
            cat_id = str(uuid.uuid4())
            created_at = random_timestamp(180, 150)
            updated_at = self.random_updated_at(created_at)
            # 카테고리는 users보다 먼저 생성되므로 created_by, updated_by는 0(시스템)
            self.categories.append({
                'id': cat_id,
                'name': cat_name
            })

            print(f"INSERT INTO p_category (id, name, is_deleted, deleted_at, deleted_by, created_at, created_by, updated_at, updated_by) VALUES")
            print(f"({sql_format(cat_id)}, {sql_format(cat_name)}, false, NULL, NULL, {sql_format(created_at)}, 0, {sql_format(updated_at)}, 0);")
        print()
    
    def generate_users(self):
        """사용자 생성"""
        print("-- Users")
        for i in range(NUM_USERS):
            user_id = i + 1
            name = fake.name()
            email = f"user{user_id}@example.com"
            created_at = random_timestamp(365, 1)
            updated_at = self.random_updated_at(created_at)
            # user 생성 시에는 이전에 생성된 user 중에서 선택 (첫 번째는 자기 자신)
            updated_by = random.randint(1, user_id)

            self.users.append(user_id)

            print(f"INSERT INTO p_user (id, name, nickname, email, male, age, road_address, address_detail, role, created_at, created_by, updated_at, updated_by, is_deleted, deleted_at, deleted_by) VALUES")
            print(f"({user_id}, {sql_format(name)}, {sql_format(f'user{user_id}')}, {sql_format(email)}, {random.choice([True, False])}, {random.randint(18, 65)}, {sql_format(fake.address())}, {sql_format(fake.building_name() or f'{random.randint(101, 999)}호')}, 'CUSTOMER', {sql_format(created_at)}, {user_id}, {sql_format(updated_at)}, {updated_by}, false, NULL, NULL);")

            # User Auth
            auth_id = str(uuid.uuid4())
            auth_updated_at = self.random_updated_at(created_at)
            auth_updated_by = random.randint(1, user_id)
            print(f"INSERT INTO p_user_auth (id, user_id, hashed_password, created_at, created_by, updated_at, updated_by, is_deleted, deleted_at, deleted_by) VALUES")
            print(f"({sql_format(auth_id)}, {user_id}, {sql_format('$2a$10$hashed_password_placeholder')}, {sql_format(created_at)}, {user_id}, {sql_format(auth_updated_at)}, {auth_updated_by}, false, NULL, NULL);")
        print()
    
    def generate_stores(self):
        """가게 생성"""
        print("-- Stores")
        for i in range(NUM_STORES):
            store_id = str(uuid.uuid4())
            store_name = fake.company() + " " + random.choice(['식당', '레스토랑', '카페', '치킨', '피자'])
            created_at = random_timestamp(365, 30)
            updated_at = self.random_updated_at(created_at)
            created_by = self.random_updated_by()
            updated_by = self.random_updated_by()

            self.stores.append({
                'id': store_id,
                'name': store_name,
                'created_at': created_at
            })

            print(f"INSERT INTO p_store (id, name, road_address, address_detail, phone_number, open_time, close_time, is_deleted, deleted_at, deleted_by, created_at, created_by, updated_at, updated_by) VALUES")
            print(f"({sql_format(store_id)}, {sql_format(store_name)}, {sql_format(generate_jongno_address())}, {sql_format(fake.building_name() or f'{random.randint(1, 10)}층')}, {sql_format(fake.phone_number())}, '09:00:00', '22:00:00', false, NULL, NULL, {sql_format(created_at)}, {created_by}, {sql_format(updated_at)}, {updated_by});")

            # Store-Category 연결 (1-3개 카테고리)
            assigned_categories = random.sample(self.categories, random.randint(1, min(3, len(self.categories))))
            for cat in assigned_categories:
                sc_id = str(uuid.uuid4())
                sc_updated_at = self.random_updated_at(created_at)
                sc_updated_by = self.random_updated_by()
                print(f"INSERT INTO p_store_category (id, store_id, category_id, created_at, created_by, updated_at, updated_by, is_deleted, deleted_at, deleted_by) VALUES")
                print(f"({sql_format(sc_id)}, {sql_format(store_id)}, {sql_format(cat['id'])}, {sql_format(created_at)}, {created_by}, {sql_format(sc_updated_at)}, {sc_updated_by}, false, NULL, NULL);")

            # Store-User 연결 (점주)
            ownerID = random.choice(self.users)
            su_id = str(uuid.uuid4())
            su_updated_at = self.random_updated_at(created_at)
            su_updated_by = self.random_updated_by()
            print(f"INSERT INTO p_store_user (id, store_id, user_id, created_at, created_by, updated_at, updated_by, is_deleted, deleted_at, deleted_by) VALUES")
            print(f"({sql_format(su_id)}, {sql_format(store_id)}, {ownerID}, {sql_format(created_at)}, {created_by}, {sql_format(su_updated_at)}, {su_updated_by}, false, NULL, NULL);")
        print()
    
    def generate_menus(self):
        """메뉴 생성"""
        print("-- Menus")
        for store in self.stores:
            num_menus = random.randint(*NUM_MENUS_PER_STORE)
            store_category = random.choice(list(MENU_TEMPLATES.keys()))
            menu_names = MENU_TEMPLATES.get(store_category, ['메뉴'])

            for j in range(num_menus):
                menu_id = str(uuid.uuid4())
                menu_name = random.choice(menu_names)
                if random.random() > 0.5:
                    menu_name += f" {random.choice(['세트', '특선', '프리미엄', '스페셜'])}"

                price = random.randint(5, 50) * 1000  # 5,000 ~ 50,000
                created_at = store['created_at'] + timedelta(days=random.randint(0, 30))
                updated_at = self.random_updated_at(created_at)
                created_by = self.random_updated_by()
                updated_by = self.random_updated_by()

                self.menus.append({
                    'id': menu_id,
                    'store_id': store['id'],
                    'name': menu_name,
                    'price': price
                })

                print(f"INSERT INTO p_menu (menu_id, store_id, name, category, price, description, image_url, is_available, is_hidden, is_deleted, deleted_at, deleted_by, created_at, created_by, updated_at, updated_by) VALUES")
                print(f"({sql_format(menu_id)}, {sql_format(store['id'])}, {sql_format(menu_name)}, {sql_format(store_category)}, {price}, {sql_format(fake.sentence())}, NULL, true, false, false, NULL, NULL, {sql_format(created_at)}, {created_by}, {sql_format(updated_at)}, {updated_by});")

                # Menu Options
                num_options = random.randint(*NUM_OPTIONS_PER_MENU)
                for k in range(num_options):
                    option_id = str(uuid.uuid4())
                    option_template = random.choice(OPTION_TEMPLATES)
                    option_name = option_template[0]
                    option_detail = random.choice(option_template[1])
                    option_price = random.choice([0, 500, 1000, 2000, 3000])
                    opt_updated_at = self.random_updated_at(created_at)
                    opt_updated_by = self.random_updated_by()

                    self.menu_options.append({
                        'id': option_id,
                        'menu_id': menu_id,
                        'name': option_name,
                        'detail': option_detail,
                        'price': option_price
                    })

                    print(f"INSERT INTO p_menu_option (option_id, menu_id, name, detail, price, is_available, is_deleted, deleted_at, deleted_by, created_at, created_by, updated_at, updated_by) VALUES")
                    print(f"({sql_format(option_id)}, {sql_format(menu_id)}, {sql_format(option_name)}, {sql_format(option_detail)}, {option_price}, true, false, NULL, NULL, {sql_format(created_at)}, {created_by}, {sql_format(opt_updated_at)}, {opt_updated_by});")
        print()
    
    def generate_orders(self):
        """주문 생성"""
        print("-- Orders")
        for i in range(NUM_ORDERS):
            order_id = str(uuid.uuid4())
            user_id = random.choice(self.users)
            store = random.choice(self.stores)
            store_menus = [m for m in self.menus if m['store_id'] == store['id']]
            
            if not store_menus:
                continue
            
            order_status = random.choice(ORDER_STATUSES)
            created_at = random_timestamp(90, 0)
            pickup_time = created_at + timedelta(minutes=random.randint(30, 90))
            
            self.orders.append({
                'id': order_id,
                'user_id': user_id,
                'store_id': store['id'],
                'menus': store_menus
            })
            
            print(f"INSERT INTO p_order (id, user_id, store_id, order_number, request, need_disposables, pickup_time, order_status, created_at, created_by) VALUES")
            print(f"({sql_format(order_id)}, {user_id}, {sql_format(store['id'])}, {sql_format(f'ORD{i+1:08d}')}, {sql_format(random.choice(['빨리요', '문앞에 놔주세요', '벨 누르지 마세요', None]))}, {random.choice([True, False])}, {sql_format(pickup_time)}, {sql_format(order_status)}, {sql_format(created_at)}, {sql_format(str(user_id))});")
            
            # Order Items
            num_items = random.randint(*ITEMS_PER_ORDER)
            selected_menus = random.sample(store_menus, min(num_items, len(store_menus)))
            total_amount = 0
            
            for menu in selected_menus:
                item_id = str(uuid.uuid4())
                quantity = random.randint(1, 3)
                item_total = menu['price'] * quantity
                
                print(f"INSERT INTO p_order_item (id, order_id, menu_id, menu_name, menu_price, quantity, created_at, created_by) VALUES")
                print(f"({sql_format(item_id)}, {sql_format(order_id)}, {sql_format(menu['id'])}, {sql_format(menu['name'])}, {menu['price']:.2f}, {quantity}, {sql_format(created_at)}, {user_id});")
                
                # Order Item Options
                menu_opts = [o for o in self.menu_options if o['menu_id'] == menu['id']]
                if menu_opts and random.random() > 0.5:
                    selected_opts = random.sample(menu_opts, min(random.randint(1, 2), len(menu_opts)))
                    for opt in selected_opts:
                        opt_id = str(uuid.uuid4())
                        item_total += opt['price'] * quantity
                        
                        print(f"INSERT INTO p_order_item_option (id, order_item_id, menu_option_id, option_name, option_detail, option_price, created_at, created_by) VALUES")
                        print(f"({sql_format(opt_id)}, {sql_format(item_id)}, {sql_format(opt['id'])}, {sql_format(opt['name'])}, {sql_format(opt['detail'])}, {opt['price']:.2f}, {sql_format(created_at)}, {user_id});")
                
                total_amount += item_total
            
            # Payment
            payment_id = str(uuid.uuid4())
            store_name = store['name']
            print(f"INSERT INTO p_payment (id, user_id, order_id, payment_title, payment_content, payment_method, payment_amount, created_at, created_by) VALUES")
            print(f"({sql_format(payment_id)}, {user_id}, {sql_format(order_id)}, {sql_format(f'{store_name} 주문')}, {sql_format(f'주문번호: ORD{i+1:08d}')}, 'CREDIT_CARD', {total_amount}, {sql_format(created_at)}, {user_id});")
            
            # Payment History
            history_id = str(uuid.uuid4())
            payment_status = 'DONE' if order_status not in ['CANCELLED'] else 'CANCELLED'
            print(f"INSERT INTO p_payment_history (id, payment_id, payment_status, created_at, created_by) VALUES")
            print(f"({sql_format(history_id)}, {sql_format(payment_id)}, {sql_format(payment_status)}, {sql_format(created_at)}, {user_id});")
            
            # Payment Key (for successful payments)
            if payment_status == 'DONE':
                key_id = str(uuid.uuid4())
                print(f"INSERT INTO p_payment_key (id, payment_id, payment_key, confirmed_at, created_at, created_by) VALUES")
                print(f"({sql_format(key_id)}, {sql_format(payment_id)}, {sql_format(f'paymentkey_{uuid.uuid4().hex[:20]}')}, {sql_format(created_at)}, {sql_format(created_at)}, {user_id});")
        print()

def main():
    generator = DataGenerator()
    
    print("-- Generated Dummy Data for Food Delivery Platform")
    print("-- Generated at:", datetime.now())
    print()
    
    generator.generate_categories()
    generator.generate_users()
    generator.generate_stores()
    generator.generate_menus()
    generator.generate_orders()
    
    print("-- Data generation completed!")
    print(f"-- Total records: Users={NUM_USERS}, Stores={NUM_STORES}, Orders={NUM_ORDERS}")

if __name__ == "__main__":
    main()
