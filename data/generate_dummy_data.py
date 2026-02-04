import random
import uuid
import os
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

try:
    import psycopg2
    from psycopg2.extras import execute_values
    PSYCOPG2_AVAILABLE = True
except ImportError:
    PSYCOPG2_AVAILABLE = False
    print("WARNING: psycopg2 not installed. Install it with: pip install psycopg2-binary")
    print("Direct database insertion will not be available.")
    print()

# Database Configuration
DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'port': int(os.getenv('DB_PORT', '5432')),
    'database': os.getenv('DB_NAME', 'myapp_db'),
    'user': os.getenv('DB_USER', 'admin'),
    'password': os.getenv('DB_PASSWORD', 'secret'),
}

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
    def __init__(self, direct_insert=False):
        self.users = []
        self.owner_users = []  # OWNER 역할 사용자들
        self.categories = []
        self.stores = []
        self.menus = []
        self.menu_options = []
        self.menu_origins = []
        self.orders = []
        self.reviews = []
        self.direct_insert = direct_insert
        self.conn = None
        self.cursor = None
        self.batch_data = {}  # 테이블별 배치 데이터

    def connect_db(self):
        """데이터베이스 연결"""
        if not PSYCOPG2_AVAILABLE:
            raise RuntimeError("psycopg2 is not installed")
        self.conn = psycopg2.connect(**DB_CONFIG)
        self.cursor = self.conn.cursor()
        print(f"Connected to PostgreSQL: {DB_CONFIG['host']}:{DB_CONFIG['port']}/{DB_CONFIG['database']}")

    def close_db(self):
        """데이터베이스 연결 종료"""
        if self.cursor:
            self.cursor.close()
        if self.conn:
            self.conn.commit()
            self.conn.close()
            print("Database connection closed.")

    def execute_insert(self, table, columns, values):
        """INSERT 실행 (직접 삽입 또는 SQL 출력)"""
        if self.direct_insert:
            placeholders = ', '.join(['%s'] * len(columns))
            sql = f"INSERT INTO {table} ({', '.join(columns)}) VALUES ({placeholders})"
            self.cursor.execute(sql, values)
        else:
            formatted_values = ', '.join([sql_format(v) for v in values])
            print(f"INSERT INTO {table} ({', '.join(columns)}) VALUES ({formatted_values});")

    def batch_insert(self, table, columns, values):
        """배치 INSERT를 위한 데이터 수집"""
        if table not in self.batch_data:
            self.batch_data[table] = {'columns': columns, 'values': []}
        self.batch_data[table]['values'].append(values)

    def flush_batch(self, table):
        """배치 데이터 플러시 (DB에 삽입)"""
        if table not in self.batch_data or not self.batch_data[table]['values']:
            return

        data = self.batch_data[table]
        if self.direct_insert:
            sql = f"INSERT INTO {table} ({', '.join(data['columns'])}) VALUES %s"
            execute_values(self.cursor, sql, data['values'], page_size=1000)
            self.conn.commit()
            print(f"  Inserted {len(data['values'])} rows into {table}")
        else:
            for values in data['values']:
                formatted_values = ', '.join([sql_format(v) for v in values])
                print(f"INSERT INTO {table} ({', '.join(data['columns'])}) VALUES ({formatted_values});")

        self.batch_data[table] = {'columns': data['columns'], 'values': []}

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
        columns = ['id', 'name', 'is_deleted', 'deleted_at', 'deleted_by', 'created_at', 'created_by', 'updated_at', 'updated_by']

        for i, cat_name in enumerate(CATEGORIES[:NUM_CATEGORIES]):
            cat_id = str(uuid.uuid4())
            created_at = random_timestamp(180, 150)
            updated_at = self.random_updated_at(created_at)
            self.categories.append({'id': cat_id, 'name': cat_name})

            self.batch_insert('p_category', columns, (cat_id, cat_name, False, None, None, created_at, 0, updated_at, 0))

        self.flush_batch('p_category')
        print()
    
    def generate_users(self):
        """사용자 생성"""
        print("-- Users")
        user_columns = ['id', 'username', 'nickname', 'email', 'male', 'age', 'road_address', 'address_detail', 'role', 'created_at', 'created_by', 'updated_at', 'updated_by', 'is_deleted', 'deleted_at', 'deleted_by']
        auth_columns = ['id', 'user_id', 'hashed_password', 'created_at', 'created_by', 'updated_at', 'updated_by', 'is_deleted', 'deleted_at', 'deleted_by']

        # 고정 테스트 계정 먼저 생성 (master, owner, chef, customer)
        test_accounts = [
            {'id': 1, 'username': 'master', 'nickname': 'master', 'email': 'master@example.com', 'role': 'MASTER'},
            {'id': 2, 'username': 'owner', 'nickname': 'owner', 'email': 'owner@example.com', 'role': 'OWNER'},
            {'id': 3, 'username': 'chef', 'nickname': 'chef', 'email': 'chef@example.com', 'role': 'CHEF'},
            {'id': 4, 'username': 'customer', 'nickname': 'customer', 'email': 'customer@example.com', 'role': 'CUSTOMER'},
        ]

        for account in test_accounts:
            user_id = account['id']
            nickname = account['nickname']
            role = account['role']
            created_at = random_timestamp(365, 1)
            updated_at = self.random_updated_at(created_at)

            if role == 'OWNER':
                self.owner_users.append(user_id)
            self.users.append(user_id)

            self.batch_insert('p_user', user_columns, (
                user_id, account['username'], nickname, account['email'],
                random.choice([True, False]), random.randint(25, 45),
                fake.address(), fake.building_name() or f'{random.randint(101, 999)}호',
                role, created_at, user_id, updated_at, user_id, False, None, None
            ))

            # User Auth
            hashed_password = bcrypt.hashpw(nickname.encode('utf-8'), bcrypt.gensalt(rounds=10)).decode('utf-8') if BCRYPT_AVAILABLE else f'$2a$10$hashed_{nickname}_placeholder'
            self.batch_insert('p_user_auth', auth_columns, (
                str(uuid.uuid4()), user_id, hashed_password, created_at, user_id,
                self.random_updated_at(created_at), user_id, False, None, None
            ))

        # 나머지 일반 사용자 생성
        num_owners = int((NUM_USERS - 4) * OWNER_RATIO)

        for i in range(4, NUM_USERS):
            user_id = i + 1
            nickname = f"user{user_id}"
            created_at = random_timestamp(365, 1)
            updated_at = self.random_updated_at(created_at)
            role = 'OWNER' if (i - 4) < num_owners else 'CUSTOMER'

            if role == 'OWNER':
                self.owner_users.append(user_id)
            self.users.append(user_id)

            self.batch_insert('p_user', user_columns, (
                user_id, nickname, nickname, f"user{user_id}@example.com",  # username = nickname (unique)
                random.choice([True, False]), random.randint(18, 65),
                fake.address(), fake.building_name() or f'{random.randint(101, 999)}호',
                role, created_at, user_id, updated_at, random.randint(1, user_id), False, None, None
            ))

            hashed_password = bcrypt.hashpw(nickname.encode('utf-8'), bcrypt.gensalt(rounds=10)).decode('utf-8') if BCRYPT_AVAILABLE else f'$2a$10$hashed_{nickname}_placeholder'
            self.batch_insert('p_user_auth', auth_columns, (
                str(uuid.uuid4()), user_id, hashed_password, created_at, user_id,
                self.random_updated_at(created_at), random.randint(1, user_id), False, None, None
            ))

        self.flush_batch('p_user')
        self.flush_batch('p_user_auth')
        print()
    
    def generate_stores(self):
        """가게 생성"""
        print("-- Stores")
        store_columns = ['id', 'name', 'road_address', 'address_detail', 'phone_number', 'open_time', 'close_time', 'status', 'is_deleted', 'deleted_at', 'deleted_by', 'created_at', 'created_by', 'updated_at', 'updated_by']
        sc_columns = ['id', 'store_id', 'category_id', 'created_at', 'created_by', 'updated_at', 'updated_by', 'is_deleted', 'deleted_at', 'deleted_by']
        su_columns = ['id', 'store_id', 'user_id', 'created_at', 'created_by', 'updated_at', 'updated_by', 'is_deleted', 'deleted_at', 'deleted_by']

        for i in range(NUM_STORES):
            store_id = str(uuid.uuid4())
            store_name = fake.company() + " " + random.choice(['식당', '레스토랑', '카페', '치킨', '피자'])
            created_at = random_timestamp(365, 30)
            updated_at = self.random_updated_at(created_at)
            created_by = self.random_updated_by()
            updated_by = self.random_updated_by()

            store_status = random.choices(STORE_STATUSES, weights=[0.1, 0.85, 0.05])[0]
            is_deleted = random.random() < 0.05
            deleted_at = self.random_updated_at(created_at) if is_deleted else None
            deleted_by = self.random_updated_by() if is_deleted else None

            self.stores.append({'id': store_id, 'name': store_name, 'created_at': created_at, 'status': store_status})

            self.batch_insert('p_store', store_columns, (
                store_id, store_name, generate_jongno_address(),
                fake.building_name() or f'{random.randint(1, 10)}층',
                fake.phone_number(), '09:00:00', '22:00:00', store_status,
                is_deleted, deleted_at, deleted_by, created_at, created_by, updated_at, updated_by
            ))

            # Store-Category 연결
            for cat in random.sample(self.categories, random.randint(1, min(3, len(self.categories)))):
                self.batch_insert('p_store_category', sc_columns, (
                    str(uuid.uuid4()), store_id, cat['id'], created_at, created_by,
                    self.random_updated_at(created_at), self.random_updated_by(), False, None, None
                ))

            # Store-User 연결 (점주)
            owner_id = random.choice(self.owner_users) if self.owner_users else random.choice(self.users)
            self.batch_insert('p_store_user', su_columns, (
                str(uuid.uuid4()), store_id, owner_id, created_at, created_by,
                self.random_updated_at(created_at), self.random_updated_by(), False, None, None
            ))

        self.flush_batch('p_store')
        self.flush_batch('p_store_category')
        self.flush_batch('p_store_user')
        print()
    
    def generate_menus(self):
        """메뉴 생성"""
        print("-- Menus")
        menu_columns = ['menu_id', 'store_id', 'name', 'category', 'price', 'description', 'image_url', 'is_available', 'is_hidden', 'is_deleted', 'deleted_at', 'deleted_by', 'created_at', 'created_by', 'updated_at', 'updated_by']
        option_columns = ['option_id', 'menu_id', 'name', 'detail', 'price', 'is_available', 'is_hidden', 'is_deleted', 'deleted_at', 'deleted_by', 'created_at', 'created_by', 'updated_at', 'updated_by']
        origin_columns = ['id', 'menu_id', 'origin_name', 'ingredient_name', 'is_deleted', 'deleted_at', 'deleted_by', 'created_at', 'created_by', 'updated_at', 'updated_by']

        for store in self.stores:
            if store['status'] != 'APPROVED':
                continue

            store_category = random.choice(list(MENU_TEMPLATES.keys()))
            menu_names = MENU_TEMPLATES.get(store_category, ['메뉴'])

            for j in range(random.randint(*NUM_MENUS_PER_STORE)):
                menu_id = str(uuid.uuid4())
                menu_name = random.choice(menu_names)
                if random.random() > 0.5:
                    menu_name += f" {random.choice(['세트', '특선', '프리미엄', '스페셜'])}"

                price = random.randint(5, 50) * 1000
                created_at = store['created_at'] + timedelta(days=random.randint(0, 30))
                created_by = self.random_updated_by()

                self.menus.append({'id': menu_id, 'store_id': store['id'], 'name': menu_name, 'price': price})

                self.batch_insert('p_menu', menu_columns, (
                    menu_id, store['id'], menu_name, store_category, price, fake.sentence(), None,
                    random.random() > 0.1, random.random() < 0.05, False, None, None,
                    created_at, created_by, self.random_updated_at(created_at), self.random_updated_by()
                ))

                # Menu Options
                for k in range(random.randint(*NUM_OPTIONS_PER_MENU)):
                    option_template = random.choice(OPTION_TEMPLATES)
                    option_id = str(uuid.uuid4())
                    self.menu_options.append({'id': option_id, 'menu_id': menu_id, 'name': option_template[0], 'detail': random.choice(option_template[1]), 'price': random.choice([0, 500, 1000, 2000, 3000])})

                    self.batch_insert('p_menu_option', option_columns, (
                        option_id, menu_id, option_template[0], random.choice(option_template[1]),
                        random.choice([0, 500, 1000, 2000, 3000]), random.random() > 0.05, random.random() < 0.02,
                        False, None, None, created_at, created_by, self.random_updated_at(created_at), self.random_updated_by()
                    ))

                # Menu Origins
                for l in range(random.randint(*NUM_ORIGINS_PER_MENU)):
                    origin_template = random.choice(ORIGIN_TEMPLATES)
                    origin_id = str(uuid.uuid4())
                    self.menu_origins.append({'id': origin_id, 'menu_id': menu_id, 'ingredient_name': origin_template[0], 'origin_name': random.choice(origin_template[1])})

                    self.batch_insert('p_origin', origin_columns, (
                        origin_id, menu_id, random.choice(origin_template[1]), origin_template[0],
                        False, None, None, created_at, created_by, self.random_updated_at(created_at), self.random_updated_by()
                    ))

        self.flush_batch('p_menu')
        self.flush_batch('p_menu_option')
        self.flush_batch('p_origin')
        print()
    
    def generate_orders(self):
        """주문 생성"""
        print("-- Orders")
        order_columns = ['id', 'user_id', 'store_id', 'order_number', 'request', 'need_disposables', 'pickup_time', 'order_status', 'payment_completed_at', 'payment_failed_at', 'accepted_at', 'rejected_at', 'cooking_started_at', 'cooking_completed_at', 'picked_up_at', 'cancelled_at', 'cancelled_by', 'estimated_time', 'reason', 'created_at', 'created_by']
        item_columns = ['id', 'order_id', 'menu_id', 'menu_name', 'menu_price', 'quantity', 'created_at', 'created_by']
        item_opt_columns = ['id', 'order_item_id', 'menu_option_id', 'option_name', 'option_detail', 'option_price', 'created_at', 'created_by']
        payment_columns = ['id', 'user_id', 'order_id', 'payment_title', 'payment_content', 'payment_method', 'payment_amount', 'created_at', 'created_by']
        history_columns = ['id', 'payment_id', 'payment_status', 'created_at', 'created_by']
        key_columns = ['id', 'payment_id', 'payment_key', 'confirmed_at', 'created_at', 'created_by']

        approved_stores = [s for s in self.stores if s['status'] == 'APPROVED']

        for i in range(NUM_ORDERS):
            user_id = random.choice(self.users)
            store = random.choice(approved_stores) if approved_stores else None
            if not store:
                continue

            store_menus = [m for m in self.menus if m['store_id'] == store['id']]
            if not store_menus:
                continue

            order_id = str(uuid.uuid4())
            order_status = random.choices(ORDER_STATUSES, weights=[0.05, 0.10, 0.10, 0.10, 0.10, 0.50, 0.03, 0.02])[0]
            created_at = random_timestamp(90, 0)
            pickup_time = created_at + timedelta(minutes=random.randint(30, 90))

            payment_completed_at = created_at if order_status not in ['PAYMENT_PENDING', 'CANCELLED', 'REJECTED'] else None
            accepted_at = created_at + timedelta(minutes=random.randint(5, 15)) if order_status in ['ACCEPTED', 'COOKING', 'READY', 'COMPLETED'] else None
            cooking_started_at = accepted_at + timedelta(minutes=random.randint(5, 10)) if order_status in ['COOKING', 'READY', 'COMPLETED'] and accepted_at else None
            cooking_completed_at = cooking_started_at + timedelta(minutes=random.randint(15, 30)) if order_status in ['READY', 'COMPLETED'] and cooking_started_at else None
            picked_up_at = cooking_completed_at + timedelta(minutes=random.randint(5, 20)) if order_status == 'COMPLETED' and cooking_completed_at else None
            cancelled_at = created_at + timedelta(minutes=random.randint(5, 30)) if order_status == 'CANCELLED' else None
            rejected_at = created_at + timedelta(minutes=random.randint(5, 15)) if order_status == 'REJECTED' else None

            self.orders.append({'id': order_id, 'user_id': user_id, 'store_id': store['id'], 'menus': store_menus, 'status': order_status})

            self.batch_insert('p_order', order_columns, (
                order_id, user_id, store['id'], f'ORD{i+1:08d}',
                random.choice(['빨리요', '문앞에 놔주세요', '벨 누르지 마세요', None]),
                random.choice([True, False]), pickup_time, order_status, payment_completed_at, None,
                accepted_at, rejected_at, cooking_started_at, cooking_completed_at, picked_up_at,
                cancelled_at, random.choice(CANCELLED_BY) if order_status == 'CANCELLED' else None,
                random.randint(20, 60) if order_status in ['ACCEPTED', 'COOKING', 'READY', 'COMPLETED'] else None,
                fake.sentence() if order_status in ['CANCELLED', 'REJECTED'] else None, created_at, user_id
            ))

            # Order Items
            selected_menus = random.sample(store_menus, min(random.randint(*ITEMS_PER_ORDER), len(store_menus)))
            total_amount = 0

            for menu in selected_menus:
                item_id = str(uuid.uuid4())
                quantity = random.randint(1, 3)
                item_total = menu['price'] * quantity

                self.batch_insert('p_order_item', item_columns, (
                    item_id, order_id, menu['id'], menu['name'], float(menu['price']), quantity, created_at, user_id
                ))

                menu_opts = [o for o in self.menu_options if o['menu_id'] == menu['id']]
                if menu_opts and random.random() > 0.5:
                    for opt in random.sample(menu_opts, min(random.randint(1, 2), len(menu_opts))):
                        item_total += opt['price'] * quantity
                        self.batch_insert('p_order_item_option', item_opt_columns, (
                            str(uuid.uuid4()), item_id, opt['id'], opt['name'], opt['detail'], float(opt['price']), created_at, user_id
                        ))

                total_amount += item_total

            # Payment
            payment_id = str(uuid.uuid4())
            payment_status = 'CANCELLED' if order_status == 'CANCELLED' else ('READY' if order_status == 'PAYMENT_PENDING' else 'DONE')

            self.batch_insert('p_payment', payment_columns, (
                payment_id, user_id, order_id, f'{store["name"]} 주문', f'주문번호: ORD{i+1:08d}',
                random.choice(PAYMENT_METHODS), total_amount, created_at, user_id
            ))

            self.batch_insert('p_payment_history', history_columns, (
                str(uuid.uuid4()), payment_id, payment_status, created_at, user_id
            ))

            if payment_status == 'DONE':
                self.batch_insert('p_payment_key', key_columns, (
                    str(uuid.uuid4()), payment_id, f'paymentkey_{uuid.uuid4().hex[:20]}', created_at, created_at, user_id
                ))

        self.flush_batch('p_order')
        self.flush_batch('p_order_item')
        self.flush_batch('p_order_item_option')
        self.flush_batch('p_payment')
        self.flush_batch('p_payment_history')
        self.flush_batch('p_payment_key')
        print()

    def generate_reviews(self):
        """리뷰 생성"""
        print("-- Reviews")
        review_columns = ['id', 'store_id', 'user_id', 'rating', 'content', 'is_deleted', 'deleted_at', 'deleted_by', 'created_at', 'created_by', 'updated_at', 'updated_by']
        review_contents = {
            5: ['정말 맛있어요!', '최고입니다', '또 시켜먹을게요', '강추합니다', '맛있고 친절해요'],
            4: ['맛있어요', '괜찮습니다', '좋아요', '다음에도 주문할게요'],
            3: ['보통이에요', '나쁘지 않아요', '그럭저럭 먹을만해요'],
            2: ['별로에요', '기대 이하였어요', '다시는 안시킬듯'],
            1: ['최악이에요', '너무 실망했어요', '돈아까워요']
        }

        for store in [s for s in self.stores if s['status'] == 'APPROVED']:
            completed_orders = [o for o in self.orders if o['store_id'] == store['id'] and o['status'] == 'COMPLETED']
            if not completed_orders:
                continue

            reviewed_users = set()
            for _ in range(random.randint(*NUM_REVIEWS_PER_STORE)):
                available_orders = [o for o in completed_orders if o['user_id'] not in reviewed_users] or completed_orders
                order = random.choice(available_orders)
                user_id = order['user_id']
                reviewed_users.add(user_id)

                rating = random.choices([1, 2, 3, 4, 5], weights=[0.02, 0.03, 0.10, 0.35, 0.50])[0]
                content = random.choice(review_contents[rating]) if random.random() > 0.2 else None
                created_at = random_timestamp(80, 0)

                self.reviews.append({'id': str(uuid.uuid4()), 'store_id': store['id'], 'user_id': user_id, 'rating': rating})

                self.batch_insert('p_review', review_columns, (
                    str(uuid.uuid4()), store['id'], user_id, rating, content,
                    False, None, None, created_at, user_id, self.random_updated_at(created_at), user_id
                ))

        self.flush_batch('p_review')
        print()

def main():
    import argparse

    parser = argparse.ArgumentParser(description='Generate dummy data for Spot platform')
    parser.add_argument('--direct', '-d', action='store_true', help='Insert directly into PostgreSQL database')
    parser.add_argument('--host', default=os.getenv('DB_HOST', 'localhost'), help='Database host')
    parser.add_argument('--port', type=int, default=int(os.getenv('DB_PORT', '5432')), help='Database port')
    parser.add_argument('--database', default=os.getenv('DB_NAME', 'myapp_db'), help='Database name')
    parser.add_argument('--user', default=os.getenv('DB_USER', 'admin'), help='Database user')
    parser.add_argument('--password', default=os.getenv('DB_PASSWORD', 'secret'), help='Database password')
    args = parser.parse_args()

    # Update DB config from args
    if args.direct:
        DB_CONFIG['host'] = args.host
        DB_CONFIG['port'] = args.port
        DB_CONFIG['database'] = args.database
        DB_CONFIG['user'] = args.user
        DB_CONFIG['password'] = args.password

    generator = DataGenerator(direct_insert=args.direct)

    print("=" * 60)
    print("  Spot Platform - Dummy Data Generator")
    print("=" * 60)
    print(f"Mode: {'Direct DB Insert' if args.direct else 'SQL Output'}")
    if args.direct:
        print(f"Database: {DB_CONFIG['host']}:{DB_CONFIG['port']}/{DB_CONFIG['database']}")
    print(f"Generated at: {datetime.now()}")
    print()
    print("Configuration:")
    print(f"  Users: {NUM_USERS} (OWNER: {int(NUM_USERS * OWNER_RATIO)})")
    print(f"  Stores: {NUM_STORES}")
    print(f"  Categories: {NUM_CATEGORIES}")
    print(f"  Menus per store: {NUM_MENUS_PER_STORE[0]}-{NUM_MENUS_PER_STORE[1]}")
    print(f"  Options per menu: {NUM_OPTIONS_PER_MENU[0]}-{NUM_OPTIONS_PER_MENU[1]}")
    print(f"  Origins per menu: {NUM_ORIGINS_PER_MENU[0]}-{NUM_ORIGINS_PER_MENU[1]}")
    print(f"  Orders: {NUM_ORDERS}")
    print(f"  Reviews per store: {NUM_REVIEWS_PER_STORE[0]}-{NUM_REVIEWS_PER_STORE[1]}")
    print("=" * 60)
    print()

    try:
        if args.direct:
            if not PSYCOPG2_AVAILABLE:
                print("ERROR: psycopg2 is required for direct insertion.")
                print("Install it with: pip install psycopg2-binary")
                return
            generator.connect_db()

        generator.generate_categories()
        generator.generate_users()
        generator.generate_stores()
        generator.generate_menus()
        generator.generate_orders()
        generator.generate_reviews()

        print()
        print("=" * 60)
        print("  Data generation completed!")
        print("=" * 60)
        print(f"Generated records:")
        print(f"  Users: {len(generator.users)} (OWNER: {len(generator.owner_users)})")
        print(f"  Categories: {len(generator.categories)}")
        print(f"  Stores: {len(generator.stores)}")
        print(f"  Menus: {len(generator.menus)}")
        print(f"  Menu Options: {len(generator.menu_options)}")
        print(f"  Menu Origins: {len(generator.menu_origins)}")
        print(f"  Orders: {len(generator.orders)}")
        print(f"  Reviews: {len(generator.reviews)}")

    finally:
        if args.direct:
            generator.close_db()

if __name__ == "__main__":
    main()
