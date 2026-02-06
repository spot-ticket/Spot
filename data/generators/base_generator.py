"""Base Generator - 공통 기능 제공"""
import uuid
from datetime import datetime, timedelta
import random

try:
    import psycopg2
    from psycopg2.extras import execute_values
    PSYCOPG2_AVAILABLE = True
except ImportError:
    PSYCOPG2_AVAILABLE = False


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


def random_timestamp(start_days_ago=90, end_days_ago=0):
    """랜덤 과거 타임스탬프"""
    days = random.randint(end_days_ago, start_days_ago)
    hours = random.randint(0, 23)
    minutes = random.randint(0, 59)
    return datetime.now() - timedelta(days=days, hours=hours, minutes=minutes)


class BaseGenerator:
    """Base Generator - 모든 Generator의 공통 기능"""

    def __init__(self, conn=None, cursor=None, direct_insert=False):
        self.conn = conn
        self.cursor = cursor
        self.direct_insert = direct_insert
        self.batch_data = {}  # 테이블별 배치 데이터

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

    def generate_uuid(self):
        """UUID 생성"""
        return str(uuid.uuid4())
