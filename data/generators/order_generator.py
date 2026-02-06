"""Order Generator - 주문 및 결제 데이터 생성"""
import random
from datetime import timedelta
from faker import Faker
from .base_generator import BaseGenerator, random_timestamp

fake = Faker('ko_KR')

# 주문 및 결제 상태
ORDER_STATUSES = ['PAYMENT_PENDING', 'PENDING', 'ACCEPTED', 'COOKING', 'READY', 'COMPLETED', 'CANCELLED', 'REJECTED']
PAYMENT_STATUSES = ['READY', 'IN_PROGRESS', 'WAITING_FOR_DEPOSIT', 'DONE', 'CANCELLED', 'PARTIAL_CANCELLED', 'ABORTED', 'EXPIRED']
PAYMENT_METHODS = ['CREDIT_CARD', 'BANK_TRANSFER']
CANCELLED_BY = ['CUSTOMER', 'STORE', 'SYSTEM']


class OrderGenerator(BaseGenerator):
    """주문 및 결제 데이터 생성"""

    def __init__(self, conn=None, cursor=None, direct_insert=False, users=None,
                 owner_users=None, stores=None, menus=None, menu_options=None):
        super().__init__(conn, cursor, direct_insert)
        self.users = users or []
        self.owner_users = owner_users or []
        self.stores = stores or []
        self.menus = menus or []
        self.menu_options = menu_options or []
        self.orders = []

    def generate(self, items_per_order=(1, 5)):
        """주문 생성 - Customer별 주문 분포를 고려"""
        print("-- Orders & Payments")

        # Customer별 주문 횟수 분포 (파레토 분포)
        customer_users = [u for u in self.users if u not in self.owner_users]
        customer_order_counts = self._generate_customer_distribution(customer_users)

        print(f"  Customer distribution: "
              f"{sum(1 for c in customer_order_counts.values() if c >= 10)} active, "
              f"{sum(1 for c in customer_order_counts.values() if 2 <= c < 10)} regular, "
              f"{sum(1 for c in customer_order_counts.values() if c < 2)} inactive")

        order_number = 1
        approved_stores = [s for s in self.stores if s['status'] == 'APPROVED']

        for user_id, order_count in customer_order_counts.items():
            for _ in range(order_count):
                store = random.choice(approved_stores) if approved_stores else None
                if not store:
                    continue

                store_menus = [m for m in self.menus if m['store_id'] == store['id']]
                if not store_menus:
                    continue

                # 주문 생성
                order_id = self._create_order(user_id, store, store_menus, order_number, items_per_order)
                if order_id:
                    order_number += 1

        self._flush_all_batches()
        print(f"  Created {len(self.orders)} orders")
        print()

    def _generate_customer_distribution(self, customer_users):
        """Customer별 주문 횟수 분포 생성 (파레토 분포)"""
        customer_order_counts = {}

        # 활성 고객 (자주 주문) - 20%
        active_count = int(len(customer_users) * 0.2)
        active_customers = random.sample(customer_users, active_count)
        for customer in active_customers:
            customer_order_counts[customer] = random.randint(30, 50)

        # 일반 고객 (가끔 주문) - 50%
        remaining = [c for c in customer_users if c not in active_customers]
        regular_count = int(len(customer_users) * 0.5)
        regular_customers = random.sample(remaining, min(regular_count, len(remaining)))
        for customer in regular_customers:
            customer_order_counts[customer] = random.randint(10, 30)

        # 비활성 고객 (거의 주문 안함) - 30%
        inactive_customers = [c for c in customer_users
                             if c not in active_customers and c not in regular_customers]
        for customer in inactive_customers:
            customer_order_counts[customer] = random.randint(0, 5)

        return customer_order_counts

    def _create_order(self, user_id, store, store_menus, order_number, items_per_order):
        """개별 주문 생성"""
        order_id = self.generate_uuid()

        # 주문 상태 및 타임스탬프 생성
        order_status = random.choices(ORDER_STATUSES,
                                     weights=[0.05, 0.10, 0.10, 0.10, 0.10, 0.50, 0.03, 0.02])[0]
        created_at = random_timestamp(90, 0)
        timestamps = self._generate_order_timestamps(order_status, created_at)

        # Order 데이터
        order_columns = ['id', 'user_id', 'store_id', 'order_number', 'request',
                        'need_disposables', 'pickup_time', 'order_status',
                        'payment_completed_at', 'payment_failed_at', 'accepted_at',
                        'rejected_at', 'cooking_started_at', 'cooking_completed_at',
                        'picked_up_at', 'cancelled_at', 'cancelled_by', 'estimated_time',
                        'reason', 'created_at', 'created_by']

        pickup_time = created_at + timedelta(minutes=random.randint(30, 90))

        cancelled_by = random.choice(CANCELLED_BY) if order_status == 'CANCELLED' else None

        self.batch_insert('p_order', order_columns, (
            order_id, user_id, store['id'], f'ORD{order_number:08d}',
            random.choice(['빨리요', '문앞에 놔주세요', '벨 누르지 마세요', None]),
            random.choice([True, False]), pickup_time, order_status,
            timestamps['payment_completed_at'], None, timestamps['accepted_at'],
            timestamps['rejected_at'], timestamps['cooking_started_at'],
            timestamps['cooking_completed_at'], timestamps['picked_up_at'],
            timestamps['cancelled_at'], cancelled_by,
            random.randint(20, 60) if order_status in ['ACCEPTED', 'COOKING', 'READY', 'COMPLETED'] else None,
            fake.sentence() if order_status in ['CANCELLED', 'REJECTED'] else None,
            created_at, user_id
        ))

        # Order Items
        total_amount = self._create_order_items(order_id, user_id, store_menus,
                                                created_at, items_per_order)

        # Payment & Payment History
        self._create_payment(order_id, user_id, store, order_number, order_status,
                            total_amount, created_at, timestamps)

        self.orders.append({
            'id': order_id,
            'user_id': user_id,
            'store_id': store['id'],
            'status': order_status
        })

        return order_id

    def _generate_order_timestamps(self, order_status, created_at):
        """주문 상태에 따른 타임스탬프 생성"""
        timestamps = {
            'payment_completed_at': None,
            'accepted_at': None,
            'rejected_at': None,
            'cooking_started_at': None,
            'cooking_completed_at': None,
            'picked_up_at': None,
            'cancelled_at': None,
        }

        if order_status not in ['PAYMENT_PENDING', 'CANCELLED', 'REJECTED']:
            timestamps['payment_completed_at'] = created_at

        if order_status in ['ACCEPTED', 'COOKING', 'READY', 'COMPLETED']:
            timestamps['accepted_at'] = created_at + timedelta(minutes=random.randint(5, 15))

        if order_status in ['COOKING', 'READY', 'COMPLETED'] and timestamps['accepted_at']:
            timestamps['cooking_started_at'] = timestamps['accepted_at'] + timedelta(minutes=random.randint(5, 10))

        if order_status in ['READY', 'COMPLETED'] and timestamps['cooking_started_at']:
            timestamps['cooking_completed_at'] = timestamps['cooking_started_at'] + timedelta(minutes=random.randint(15, 30))

        if order_status == 'COMPLETED' and timestamps['cooking_completed_at']:
            timestamps['picked_up_at'] = timestamps['cooking_completed_at'] + timedelta(minutes=random.randint(5, 20))

        if order_status == 'CANCELLED':
            timestamps['cancelled_at'] = created_at + timedelta(minutes=random.randint(5, 30))

        if order_status == 'REJECTED':
            timestamps['rejected_at'] = created_at + timedelta(minutes=random.randint(5, 15))

        return timestamps

    def _create_order_items(self, order_id, user_id, store_menus, created_at, items_per_order):
        """주문 아이템 생성"""
        item_columns = ['id', 'order_id', 'menu_id', 'menu_name', 'menu_price',
                       'quantity', 'created_at', 'created_by']
        item_opt_columns = ['id', 'order_item_id', 'menu_option_id', 'option_name',
                           'option_detail', 'option_price', 'created_at', 'created_by']

        selected_menus = random.sample(store_menus, min(random.randint(*items_per_order), len(store_menus)))
        total_amount = 0

        for menu in selected_menus:
            item_id = self.generate_uuid()
            quantity = random.randint(1, 3)
            item_total = menu['price'] * quantity

            self.batch_insert('p_order_item', item_columns, (
                item_id, order_id, menu['id'], menu['name'], float(menu['price']),
                quantity, created_at, user_id
            ))

            # Order Item Options
            menu_opts = [o for o in self.menu_options if o['menu_id'] == menu['id']]
            if menu_opts and random.random() > 0.5:
                for opt in random.sample(menu_opts, min(random.randint(1, 2), len(menu_opts))):
                    item_total += opt['price'] * quantity
                    self.batch_insert('p_order_item_option', item_opt_columns, (
                        self.generate_uuid(), item_id, opt['id'], opt['name'],
                        opt['detail'], float(opt['price']), created_at, user_id
                    ))

            total_amount += item_total

        return total_amount

    def _create_payment(self, order_id, user_id, store, order_number, order_status,
                       total_amount, created_at, timestamps):
        """결제 및 결제 이력 생성"""
        payment_columns = ['id', 'user_id', 'order_id', 'payment_title', 'payment_content',
                          'payment_method', 'payment_amount', 'created_at', 'created_by']
        history_columns = ['id', 'payment_id', 'payment_status', 'created_at', 'created_by']
        key_columns = ['id', 'payment_id', 'payment_key', 'confirmed_at',
                      'created_at', 'created_by']

        payment_id = self.generate_uuid()

        # Payment 상태 결정
        if order_status == 'CANCELLED':
            final_status = 'CANCELLED'
        elif order_status == 'PAYMENT_PENDING':
            final_status = 'READY'
        else:
            final_status = 'DONE'

        # Payment
        self.batch_insert('p_payment', payment_columns, (
            payment_id, user_id, order_id, f'{store["name"]} 주문',
            f'주문번호: ORD{order_number:08d}',
            random.choice(PAYMENT_METHODS), total_amount, created_at, user_id
        ))

        # Payment History - 상태 변화 이력 생성
        self._create_payment_history(payment_id, user_id, final_status, created_at, timestamps)

        # Payment Key (결제 완료 시)
        if final_status == 'DONE':
            self.batch_insert('p_payment_key', key_columns, (
                self.generate_uuid(), payment_id,
                f'paymentkey_{self.generate_uuid()[:20]}',
                created_at, created_at, user_id
            ))

    def _create_payment_history(self, payment_id, user_id, final_status, created_at, timestamps):
        """결제 상태 변화 이력 생성"""
        history_columns = ['id', 'payment_id', 'payment_status', 'created_at', 'created_by']

        if final_status == 'DONE':
            # READY → IN_PROGRESS → DONE 순서로 이력 생성
            self.batch_insert('p_payment_history', history_columns, (
                self.generate_uuid(), payment_id, 'READY', created_at, user_id
            ))
            self.batch_insert('p_payment_history', history_columns, (
                self.generate_uuid(), payment_id, 'IN_PROGRESS',
                created_at + timedelta(seconds=random.randint(1, 3)), user_id
            ))
            self.batch_insert('p_payment_history', history_columns, (
                self.generate_uuid(), payment_id, 'DONE',
                timestamps['payment_completed_at'] or created_at, user_id
            ))
        elif final_status == 'CANCELLED':
            # READY → CANCELLED
            self.batch_insert('p_payment_history', history_columns, (
                self.generate_uuid(), payment_id, 'READY', created_at, user_id
            ))
            self.batch_insert('p_payment_history', history_columns, (
                self.generate_uuid(), payment_id, 'CANCELLED',
                timestamps['cancelled_at'] or created_at, user_id
            ))
        else:
            # READY 상태만
            self.batch_insert('p_payment_history', history_columns, (
                self.generate_uuid(), payment_id, final_status, created_at, user_id
            ))

    def _flush_all_batches(self):
        """모든 배치 데이터 플러시"""
        self.flush_batch('p_order')
        self.flush_batch('p_order_item')
        self.flush_batch('p_order_item_option')
        self.flush_batch('p_payment')
        self.flush_batch('p_payment_history')
        self.flush_batch('p_payment_key')

    def get_orders(self):
        """생성된 주문 목록 반환"""
        return self.orders
