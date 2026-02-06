"""User Generator - 사용자 및 인증 정보 생성"""
import random
from faker import Faker
from .base_generator import BaseGenerator, random_timestamp

try:
    import bcrypt
    BCRYPT_AVAILABLE = True
except ImportError:
    BCRYPT_AVAILABLE = False

fake = Faker('ko_KR')


class UserGenerator(BaseGenerator):
    """사용자 데이터 생성"""

    def __init__(self, conn=None, cursor=None, direct_insert=False):
        super().__init__(conn, cursor, direct_insert)
        self.users = []
        self.owner_users = []

    def generate(self, num_users=500, owner_ratio=0.1):
        """사용자 생성"""
        print("-- Users")
        user_columns = ['id', 'username', 'nickname', 'email', 'male', 'age', 'road_address',
                       'address_detail', 'role', 'created_at', 'created_by', 'updated_at',
                       'updated_by', 'is_deleted', 'deleted_at', 'deleted_by']
        auth_columns = ['id', 'user_id', 'hashed_password', 'created_at', 'created_by',
                       'updated_at', 'updated_by', 'is_deleted', 'deleted_at', 'deleted_by']

        # 고정 테스트 계정 생성
        test_accounts = [
            {'id': 1, 'username': 'master', 'nickname': 'master', 'email': 'master@example.com', 'role': 'MASTER'},
            {'id': 2, 'username': 'owner', 'nickname': 'owner', 'email': 'owner@example.com', 'role': 'OWNER'},
            {'id': 3, 'username': 'chef', 'nickname': 'chef', 'email': 'chef@example.com', 'role': 'CHEF'},
            {'id': 4, 'username': 'customer', 'nickname': 'customer', 'email': 'customer@example.com', 'role': 'CUSTOMER'},
        ]

        for account in test_accounts:
            self._create_user(account['id'], account['username'], account['nickname'],
                            account['email'], account['role'], user_columns, auth_columns)

        # 일반 사용자 생성
        num_owners = int((num_users - 4) * owner_ratio)
        for i in range(4, num_users):
            user_id = i + 1
            nickname = f"user{user_id}"
            role = 'OWNER' if (i - 4) < num_owners else 'CUSTOMER'

            self._create_user(user_id, nickname, nickname, f"user{user_id}@example.com",
                            role, user_columns, auth_columns)

        self.flush_batch('p_user')
        self.flush_batch('p_user_auth')
        print(f"  Created {len(self.users)} users ({len(self.owner_users)} owners)")
        print()

    def _create_user(self, user_id, username, nickname, email, role, user_columns, auth_columns):
        """개별 사용자 생성"""
        created_at = random_timestamp(365, 1)
        updated_at = self.random_updated_at(created_at)

        if role == 'OWNER':
            self.owner_users.append(user_id)
        self.users.append(user_id)

        # User
        self.batch_insert('p_user', user_columns, (
            user_id, username, nickname, email,
            random.choice([True, False]), random.randint(18, 65),
            fake.address(), fake.building_name() or f'{random.randint(101, 999)}호',
            role, created_at, user_id, updated_at, user_id, False, None, None
        ))

        # User Auth
        hashed_password = (bcrypt.hashpw(nickname.encode('utf-8'), bcrypt.gensalt(rounds=10)).decode('utf-8')
                          if BCRYPT_AVAILABLE else f'$2a$10$hashed_{nickname}_placeholder')

        self.batch_insert('p_user_auth', auth_columns, (
            self.generate_uuid(), user_id, hashed_password, created_at, user_id,
            self.random_updated_at(created_at), user_id, False, None, None
        ))

    def get_users(self):
        """생성된 사용자 ID 목록 반환"""
        return self.users

    def get_owner_users(self):
        """Owner 사용자 ID 목록 반환"""
        return self.owner_users
