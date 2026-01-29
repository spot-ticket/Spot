import random
import uuid
from datetime import datetime, timedelta
from faker import Faker

try:
    import bcrypt
    BCRYPT_AVAILABLE = True
except ImportError:
    BCRYPT_AVAILABLE = False
    print("WARNING: bcrypt not installed. Install it with: pip install bcrypt")
    print("Passwords will use placeholder hashes instead of real BCrypt hashes.")
    print()

fake = Faker('ko_KR')

# 설정
NUM_USERS = 500
NUM_STORES = 1000
NUM_CATEGORIES = 20
NUM_MENUS_PER_STORE = (5, 15)  # 가게당 메뉴 수 범위
NUM_OPTIONS_PER_MENU = (0, 5)  # 메뉴당 옵션 수 범위
NUM_ORIGINS_PER_MENU = (0, 3)  # 메뉴당 원산지 정보 수
NUM_ORDERS = 10000
ITEMS_PER_ORDER = (1, 5)  # 주문당 아이템 수
NUM_REVIEWS_PER_STORE = (0, 20)  # 가게당 리뷰 수 범위
OWNER_RATIO = 0.1  # 전체 사용자 중 OWNER 비율

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
ORDER_STATUSES = ['PAYMENT_PENDING', 'PENDING', 'ACCEPTED', 'COOKING', 'READY', 'COMPLETED', 'CANCELLED', 'REJECTED']
PAYMENT_STATUSES = ['READY', 'IN_PROGRESS', 'WAITING_FOR_DEPOSIT', 'DONE', 'CANCELLED', 'PARTIAL_CANCELLED', 'ABORTED', 'EXPIRED']
PAYMENT_METHODS = ['CREDIT_CARD', 'BANK_TRANSFER']
STORE_STATUSES = ['PENDING', 'APPROVED', 'REJECTED']
CANCELLED_BY = ['USER', 'OWNER']

# 원산지 정보
ORIGIN_TEMPLATES = [
    ('쇠고기', ['한우', '미국산', '호주산']),
    ('돼지고기', ['국내산', '미국산', '스페인산']),
    ('닭고기', ['국내산', '브라질산']),
    ('쌀', ['국내산', '캘리포니아산']),
    ('김치', ['국내산 배추', '국내산 고춧가루']),
    ('해산물', ['국내산', '노르웨이산', '칠레산']),
]

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
        self.owner_users = []  # OWNER 역할 사용자들
        self.categories = []
        self.stores = []
        self.menus = []
        self.menu_options = []
        self.menu_origins = []
        self.orders = []
        self.reviews = []

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

        # 고정 테스트 계정 먼저 생성 (master, owner, chef, customer)
        test_accounts = [
            {'id': 1, 'name': 'master', 'nickname': 'master', 'email': 'master@example.com', 'role': 'MASTER'},
            {'id': 2, 'name': 'owner', 'nickname': 'owner', 'email': 'owner@example.com', 'role': 'OWNER'},
            {'id': 3, 'name': 'chef', 'nickname': 'chef', 'email': 'chef@example.com', 'role': 'CHEF'},
            {'id': 4, 'name': 'customer', 'nickname': 'customer', 'email': 'customer@example.com', 'role': 'CUSTOMER'},
        ]

        for account in test_accounts:
            user_id = account['id']
            name = account['name']
            nickname = account['nickname']
            email = account['email']
            role = account['role']

            created_at = random_timestamp(365, 1)
            updated_at = self.random_updated_at(created_at)
            updated_by = user_id

            if role == 'OWNER':
                self.owner_users.append(user_id)

            self.users.append(user_id)

            print(f"INSERT INTO p_user (id, name, nickname, email, male, age, road_address, address_detail, role, created_at, created_by, updated_at, updated_by, is_deleted, deleted_at, deleted_by) VALUES")
            print(f"({user_id}, {sql_format(name)}, {sql_format(nickname)}, {sql_format(email)}, {random.choice([True, False])}, {random.randint(25, 45)}, {sql_format(fake.address())}, {sql_format(fake.building_name() or f'{random.randint(101, 999)}호')}, {sql_format(role)}, {sql_format(created_at)}, {user_id}, {sql_format(updated_at)}, {updated_by}, false, NULL, NULL);")

            # User Auth - 비밀번호는 nickname과 동일
            auth_id = str(uuid.uuid4())
            auth_updated_at = self.random_updated_at(created_at)
            auth_updated_by = user_id

            # BCrypt로 비밀번호 해시 생성 (비밀번호 = nickname)
            if BCRYPT_AVAILABLE:
                password_bytes = nickname.encode('utf-8')
                salt = bcrypt.gensalt(rounds=10)
                hashed_password = bcrypt.hashpw(password_bytes, salt).decode('utf-8')
            else:
                hashed_password = f'$2a$10$hashed_{nickname}_placeholder'

            print(f"INSERT INTO p_user_auth (id, user_id, hashed_password, created_at, created_by, updated_at, updated_by, is_deleted, deleted_at, deleted_by) VALUES")
            print(f"({sql_format(auth_id)}, {user_id}, {sql_format(hashed_password)}, {sql_format(created_at)}, {user_id}, {sql_format(auth_updated_at)}, {auth_updated_by}, false, NULL, NULL);")

        # 나머지 일반 사용자 생성 (user5부터 시작)
        num_owners = int((NUM_USERS - 4) * OWNER_RATIO)  # 테스트 계정 4개 제외

        for i in range(4, NUM_USERS):  # 5번째부터 시작 (user5, user6, ...)
            user_id = i + 1
            name = fake.name()
            nickname = f"user{user_id}"
            email = f"user{user_id}@example.com"
            created_at = random_timestamp(365, 1)
            updated_at = self.random_updated_at(created_at)
            # user 생성 시에는 이전에 생성된 user 중에서 선택
            updated_by = random.randint(1, user_id)

            # 일부 사용자를 OWNER로 설정
            role = 'OWNER' if (i - 4) < num_owners else 'CUSTOMER'
            if role == 'OWNER':
                self.owner_users.append(user_id)

            self.users.append(user_id)

            print(f"INSERT INTO p_user (id, name, nickname, email, male, age, road_address, address_detail, role, created_at, created_by, updated_at, updated_by, is_deleted, deleted_at, deleted_by) VALUES")
            print(f"({user_id}, {sql_format(name)}, {sql_format(nickname)}, {sql_format(email)}, {random.choice([True, False])}, {random.randint(18, 65)}, {sql_format(fake.address())}, {sql_format(fake.building_name() or f'{random.randint(101, 999)}호')}, {sql_format(role)}, {sql_format(created_at)}, {user_id}, {sql_format(updated_at)}, {updated_by}, false, NULL, NULL);")

            # User Auth - 비밀번호는 nickname과 동일 (user1, user2, ...)
            # 로그인 시: username/email = user1@example.com, password = user1
            auth_id = str(uuid.uuid4())
            auth_updated_at = self.random_updated_at(created_at)
            auth_updated_by = random.randint(1, user_id)

            # BCrypt로 비밀번호 해시 생성 (비밀번호 = nickname)
            if BCRYPT_AVAILABLE:
                # BCrypt를 사용하여 실제 해시 생성 (rounds=10)
                password_bytes = nickname.encode('utf-8')
                salt = bcrypt.gensalt(rounds=10)
                hashed_password = bcrypt.hashpw(password_bytes, salt).decode('utf-8')
            else:
                # bcrypt가 없으면 placeholder 사용
                hashed_password = f'$2a$10$hashed_{nickname}_placeholder'

            print(f"INSERT INTO p_user_auth (id, user_id, hashed_password, created_at, created_by, updated_at, updated_by, is_deleted, deleted_at, deleted_by) VALUES")
            print(f"({sql_format(auth_id)}, {user_id}, {sql_format(hashed_password)}, {sql_format(created_at)}, {user_id}, {sql_format(auth_updated_at)}, {auth_updated_by}, false, NULL, NULL);")
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

            # 가게 상태 (대부분 APPROVED, 일부 PENDING/REJECTED)
            store_status = random.choices(
                STORE_STATUSES,
                weights=[0.1, 0.85, 0.05]  # PENDING 10%, APPROVED 85%, REJECTED 5%
            )[0]

            # 일부 가게를 삭제 상태로 설정 (5%)
            is_deleted = random.random() < 0.05
            deleted_at = self.random_updated_at(created_at) if is_deleted else None
            deleted_by = self.random_updated_by() if is_deleted else None

            self.stores.append({
                'id': store_id,
                'name': store_name,
                'created_at': created_at,
                'status': store_status
            })

            print(f"INSERT INTO p_store (id, name, road_address, address_detail, phone_number, open_time, close_time, status, is_deleted, deleted_at, deleted_by, created_at, created_by, updated_at, updated_by) VALUES")
            print(f"({sql_format(store_id)}, {sql_format(store_name)}, {sql_format(generate_jongno_address())}, {sql_format(fake.building_name() or f'{random.randint(1, 10)}층')}, {sql_format(fake.phone_number())}, '09:00:00', '22:00:00', {sql_format(store_status)}, {is_deleted}, {sql_format(deleted_at)}, {sql_format(deleted_by)}, {sql_format(created_at)}, {created_by}, {sql_format(updated_at)}, {updated_by});")

            # Store-Category 연결 (1-3개 카테고리)
            assigned_categories = random.sample(self.categories, random.randint(1, min(3, len(self.categories))))
            for cat in assigned_categories:
                sc_id = str(uuid.uuid4())
                sc_updated_at = self.random_updated_at(created_at)
                sc_updated_by = self.random_updated_by()
                print(f"INSERT INTO p_store_category (id, store_id, category_id, created_at, created_by, updated_at, updated_by, is_deleted, deleted_at, deleted_by) VALUES")
                print(f"({sql_format(sc_id)}, {sql_format(store_id)}, {sql_format(cat['id'])}, {sql_format(created_at)}, {created_by}, {sql_format(sc_updated_at)}, {sc_updated_by}, false, NULL, NULL);")

            # Store-User 연결 (점주) - OWNER 사용자 중에서 선택
            if self.owner_users:
                owner_id = random.choice(self.owner_users)
            else:
                owner_id = random.choice(self.users)

            su_id = str(uuid.uuid4())
            su_updated_at = self.random_updated_at(created_at)
            su_updated_by = self.random_updated_by()
            print(f"INSERT INTO p_store_user (id, store_id, user_id, created_at, created_by, updated_at, updated_by, is_deleted, deleted_at, deleted_by) VALUES")
            print(f"({sql_format(su_id)}, {sql_format(store_id)}, {owner_id}, {sql_format(created_at)}, {created_by}, {sql_format(su_updated_at)}, {su_updated_by}, false, NULL, NULL);")
        print()
    
    def generate_menus(self):
        """메뉴 생성"""
        print("-- Menus")
        for store in self.stores:
            # APPROVED된 가게만 메뉴 생성
            if store['status'] != 'APPROVED':
                continue

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

                # 일부 메뉴를 품절/숨김 상태로 설정
                is_available = random.random() > 0.1  # 90% 판매 가능
                is_hidden = random.random() < 0.05  # 5% 숨김

                self.menus.append({
                    'id': menu_id,
                    'store_id': store['id'],
                    'name': menu_name,
                    'price': price
                })

                print(f"INSERT INTO p_menu (menu_id, store_id, name, category, price, description, image_url, is_available, is_hidden, is_deleted, deleted_at, deleted_by, created_at, created_by, updated_at, updated_by) VALUES")
                print(f"({sql_format(menu_id)}, {sql_format(store['id'])}, {sql_format(menu_name)}, {sql_format(store_category)}, {price}, {sql_format(fake.sentence())}, NULL, {is_available}, {is_hidden}, false, NULL, NULL, {sql_format(created_at)}, {created_by}, {sql_format(updated_at)}, {updated_by});")

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

                    # is_available과 is_hidden 필드 생성 (nullable=false)
                    opt_is_available = random.random() > 0.05  # 95% 판매 가능
                    opt_is_hidden = random.random() < 0.02  # 2% 숨김

                    self.menu_options.append({
                        'id': option_id,
                        'menu_id': menu_id,
                        'name': option_name,
                        'detail': option_detail,
                        'price': option_price
                    })

                    print(f"INSERT INTO p_menu_option (option_id, menu_id, name, detail, price, is_available, is_hidden, is_deleted, deleted_at, deleted_by, created_at, created_by, updated_at, updated_by) VALUES")
                    print(f"({sql_format(option_id)}, {sql_format(menu_id)}, {sql_format(option_name)}, {sql_format(option_detail)}, {option_price}, {opt_is_available}, {opt_is_hidden}, false, NULL, NULL, {sql_format(created_at)}, {created_by}, {sql_format(opt_updated_at)}, {opt_updated_by});")

                # Menu Origins (원산지 정보)
                num_origins = random.randint(*NUM_ORIGINS_PER_MENU)
                for l in range(num_origins):
                    origin_id = str(uuid.uuid4())
                    origin_template = random.choice(ORIGIN_TEMPLATES)
                    ingredient_name = origin_template[0]
                    origin_name = random.choice(origin_template[1])
                    origin_updated_at = self.random_updated_at(created_at)
                    origin_updated_by = self.random_updated_by()

                    self.menu_origins.append({
                        'id': origin_id,
                        'menu_id': menu_id,
                        'ingredient_name': ingredient_name,
                        'origin_name': origin_name
                    })

                    print(f"INSERT INTO p_origin (id, menu_id, origin_name, ingredient_name, is_deleted, deleted_at, deleted_by, created_at, created_by, updated_at, updated_by) VALUES")
                    print(f"({sql_format(origin_id)}, {sql_format(menu_id)}, {sql_format(origin_name)}, {sql_format(ingredient_name)}, false, NULL, NULL, {sql_format(created_at)}, {created_by}, {sql_format(origin_updated_at)}, {origin_updated_by});")
        print()
    
    def generate_orders(self):
        """주문 생성"""
        print("-- Orders")
        for i in range(NUM_ORDERS):
            order_id = str(uuid.uuid4())
            user_id = random.choice(self.users)
            store = random.choice([s for s in self.stores if s['status'] == 'APPROVED'])

            if not store:
                continue

            store_menus = [m for m in self.menus if m['store_id'] == store['id']]

            if not store_menus:
                continue

            # 주문 상태 가중치 설정
            order_status = random.choices(
                ORDER_STATUSES,
                weights=[0.05, 0.10, 0.10, 0.10, 0.10, 0.50, 0.03, 0.02]  # COMPLETED 50%
            )[0]

            created_at = random_timestamp(90, 0)
            pickup_time = created_at + timedelta(minutes=random.randint(30, 90))

            # 주문 상태별 타임스탬프 설정
            payment_completed_at = created_at if order_status not in ['PAYMENT_PENDING', 'CANCELLED', 'REJECTED'] else None
            accepted_at = created_at + timedelta(minutes=random.randint(5, 15)) if order_status in ['ACCEPTED', 'COOKING', 'READY', 'COMPLETED'] else None
            cooking_started_at = accepted_at + timedelta(minutes=random.randint(5, 10)) if order_status in ['COOKING', 'READY', 'COMPLETED'] else None
            cooking_completed_at = cooking_started_at + timedelta(minutes=random.randint(15, 30)) if order_status in ['READY', 'COMPLETED'] else None
            picked_up_at = cooking_completed_at + timedelta(minutes=random.randint(5, 20)) if order_status == 'COMPLETED' else None

            cancelled_at = created_at + timedelta(minutes=random.randint(5, 30)) if order_status == 'CANCELLED' else None
            rejected_at = created_at + timedelta(minutes=random.randint(5, 15)) if order_status == 'REJECTED' else None

            cancelled_by = random.choice(CANCELLED_BY) if order_status == 'CANCELLED' else None
            reason = fake.sentence() if order_status in ['CANCELLED', 'REJECTED'] else None
            estimated_time = random.randint(20, 60) if order_status in ['ACCEPTED', 'COOKING', 'READY', 'COMPLETED'] else None

            self.orders.append({
                'id': order_id,
                'user_id': user_id,
                'store_id': store['id'],
                'menus': store_menus,
                'status': order_status
            })

            print(f"INSERT INTO p_order (id, user_id, store_id, order_number, request, need_disposables, pickup_time, order_status, payment_completed_at, payment_failed_at, accepted_at, rejected_at, cooking_started_at, cooking_completed_at, picked_up_at, cancelled_at, cancelled_by, estimated_time, reason, created_at, created_by) VALUES")
            print(f"({sql_format(order_id)}, {user_id}, {sql_format(store['id'])}, {sql_format(f'ORD{i+1:08d}')}, {sql_format(random.choice(['빨리요', '문앞에 놔주세요', '벨 누르지 마세요', None]))}, {random.choice([True, False])}, {sql_format(pickup_time)}, {sql_format(order_status)}, {sql_format(payment_completed_at)}, NULL, {sql_format(accepted_at)}, {sql_format(rejected_at)}, {sql_format(cooking_started_at)}, {sql_format(cooking_completed_at)}, {sql_format(picked_up_at)}, {sql_format(cancelled_at)}, {sql_format(cancelled_by)}, {sql_format(estimated_time)}, {sql_format(reason)}, {sql_format(created_at)}, {user_id});")

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
            payment_method = random.choice(PAYMENT_METHODS)
            print(f"INSERT INTO p_payment (id, user_id, order_id, payment_title, payment_content, payment_method, payment_amount, created_at, created_by) VALUES")
            print(f"({sql_format(payment_id)}, {user_id}, {sql_format(order_id)}, {sql_format(f'{store_name} 주문')}, {sql_format(f'주문번호: ORD{i+1:08d}')}, {sql_format(payment_method)}, {total_amount}, {sql_format(created_at)}, {user_id});")

            # Payment History
            history_id = str(uuid.uuid4())
            if order_status in ['CANCELLED', 'REJECTED', 'PAYMENT_PENDING']:
                payment_status = 'CANCELLED' if order_status == 'CANCELLED' else 'READY'
            else:
                payment_status = 'DONE'

            print(f"INSERT INTO p_payment_history (id, payment_id, payment_status, created_at, created_by) VALUES")
            print(f"({sql_format(history_id)}, {sql_format(payment_id)}, {sql_format(payment_status)}, {sql_format(created_at)}, {user_id});")

            # Payment Key (for successful payments)
            if payment_status == 'DONE':
                key_id = str(uuid.uuid4())
                print(f"INSERT INTO p_payment_key (id, payment_id, payment_key, confirmed_at, created_at, created_by) VALUES")
                print(f"({sql_format(key_id)}, {sql_format(payment_id)}, {sql_format(f'paymentkey_{uuid.uuid4().hex[:20]}')}, {sql_format(created_at)}, {sql_format(created_at)}, {user_id});")
        print()

    def generate_reviews(self):
        """리뷰 생성"""
        print("-- Reviews")
        approved_stores = [s for s in self.stores if s['status'] == 'APPROVED']

        for store in approved_stores:
            # 완료된 주문이 있는 가게만 리뷰 생성 가능
            completed_orders = [o for o in self.orders if o['store_id'] == store['id'] and o['status'] == 'COMPLETED']

            if not completed_orders:
                continue

            num_reviews = random.randint(*NUM_REVIEWS_PER_STORE)

            # 리뷰는 완료된 주문을 한 고객만 작성 가능
            reviewed_users = set()

            for _ in range(num_reviews):
                if not completed_orders:
                    break

                # 중복되지 않은 사용자 선택
                available_orders = [o for o in completed_orders if o['user_id'] not in reviewed_users]
                if not available_orders:
                    available_orders = completed_orders

                order = random.choice(available_orders)
                user_id = order['user_id']
                reviewed_users.add(user_id)

                review_id = str(uuid.uuid4())
                rating = random.choices([1, 2, 3, 4, 5], weights=[0.02, 0.03, 0.10, 0.35, 0.50])[0]  # 4-5점이 85%

                # 평점에 따른 리뷰 내용
                review_contents = {
                    5: ['정말 맛있어요!', '최고입니다', '또 시켜먹을게요', '강추합니다', '맛있고 친절해요'],
                    4: ['맛있어요', '괜찮습니다', '좋아요', '다음에도 주문할게요'],
                    3: ['보통이에요', '나쁘지 않아요', '그럭저럭 먹을만해요'],
                    2: ['별로에요', '기대 이하였어요', '다시는 안시킬듯'],
                    1: ['최악이에요', '너무 실망했어요', '돈아까워요']
                }
                content = random.choice(review_contents[rating]) if random.random() > 0.2 else None  # 80%만 내용 작성

                # 리뷰는 주문 완료 후 작성 (주문 생성 시간 기준으로 설정되어야 하지만 간단히 처리)
                created_at = random_timestamp(80, 0)
                updated_at = self.random_updated_at(created_at)
                created_by = user_id
                updated_by = user_id

                self.reviews.append({
                    'id': review_id,
                    'store_id': store['id'],
                    'user_id': user_id,
                    'rating': rating
                })

                print(f"INSERT INTO p_review (id, store_id, user_id, rating, content, is_deleted, deleted_at, deleted_by, created_at, created_by, updated_at, updated_by) VALUES")
                print(f"({sql_format(review_id)}, {sql_format(store['id'])}, {user_id}, {rating}, {sql_format(content)}, false, NULL, NULL, {sql_format(created_at)}, {created_by}, {sql_format(updated_at)}, {updated_by});")
        print()

def main():
    generator = DataGenerator()

    print("-- Generated Dummy Data for Food Delivery Platform")
    print("-- Generated at:", datetime.now())
    print("-- Configuration:")
    print(f"--   Users: {NUM_USERS} (OWNER: {int(NUM_USERS * OWNER_RATIO)})")
    print(f"--   Stores: {NUM_STORES}")
    print(f"--   Categories: {NUM_CATEGORIES}")
    print(f"--   Menus per store: {NUM_MENUS_PER_STORE[0]}-{NUM_MENUS_PER_STORE[1]}")
    print(f"--   Options per menu: {NUM_OPTIONS_PER_MENU[0]}-{NUM_OPTIONS_PER_MENU[1]}")
    print(f"--   Origins per menu: {NUM_ORIGINS_PER_MENU[0]}-{NUM_ORIGINS_PER_MENU[1]}")
    print(f"--   Orders: {NUM_ORDERS}")
    print(f"--   Reviews per store: {NUM_REVIEWS_PER_STORE[0]}-{NUM_REVIEWS_PER_STORE[1]}")
    print()

    generator.generate_categories()
    generator.generate_users()
    generator.generate_stores()
    generator.generate_menus()
    generator.generate_orders()
    generator.generate_reviews()

    print("-- Data generation completed!")
    print(f"-- Generated records:")
    print(f"--   Users: {len(generator.users)} (OWNER: {len(generator.owner_users)})")
    print(f"--   Categories: {len(generator.categories)}")
    print(f"--   Stores: {len(generator.stores)}")
    print(f"--   Menus: {len(generator.menus)}")
    print(f"--   Menu Options: {len(generator.menu_options)}")
    print(f"--   Menu Origins: {len(generator.menu_origins)}")
    print(f"--   Orders: {len(generator.orders)}")
    print(f"--   Reviews: {len(generator.reviews)}")

if __name__ == "__main__":
    main()
