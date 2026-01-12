#!/usr/bin/env python3
"""
더미 데이터의 비밀번호 해시를 테스트하는 스크립트

사용법:
    python3 test_password.py user1
    python3 test_password.py user10
"""

import sys
import re

def extract_password_hash(user_nickname):
    """SQL 파일에서 특정 사용자의 비밀번호 해시를 추출"""
    try:
        with open('dummy_data.sql', 'r') as f:
            lines = f.readlines()

        user_id = None
        # user_id 찾기 - nickname이 정확히 매칭되는 줄 찾기
        for i, line in enumerate(lines):
            if f"'{user_nickname}'" in line and 'INSERT INTO p_user ' in lines[i-1]:
                # 줄에서 user_id 추출 (첫 번째 숫자)
                match = re.search(r'\((\d+),', line)
                if match:
                    user_id = match.group(1)
                    break

        if not user_id:
            print(f"Error: User '{user_nickname}' not found in dummy_data.sql")
            return None

        # 해당 user_id의 비밀번호 해시 찾기
        for i, line in enumerate(lines):
            if 'INSERT INTO p_user_auth ' in line:
                # 다음 줄에서 user_id와 해시 추출
                if i + 1 < len(lines):
                    next_line = lines[i + 1]
                    # user_id와 해시 패턴: ('uuid', user_id, 'hash', ...)
                    pattern = rf"\('[^']+',\s*{user_id},\s*'([^']+)'"
                    match = re.search(pattern, next_line)
                    if match:
                        return match.group(1)

        print(f"Error: Password hash not found for user_id={user_id}")
        return None

    except FileNotFoundError:
        print("Error: dummy_data.sql not found. Run generate_dummy_data.py first.")
        return None

def test_password(user_nickname, password_hash):
    """BCrypt를 사용하여 비밀번호 검증"""
    try:
        import bcrypt
    except ImportError:
        print("Error: bcrypt not installed. Install it with: pip install bcrypt")
        return

    password = user_nickname  # 비밀번호는 nickname과 동일
    password_bytes = password.encode('utf-8')
    hash_bytes = password_hash.encode('utf-8')

    if bcrypt.checkpw(password_bytes, hash_bytes):
        print(f"✓ Password verification SUCCESSFUL for '{user_nickname}'")
        print(f"  Username: {user_nickname}")
        print(f"  Email: {user_nickname}@example.com")
        print(f"  Password: {password}")
        print(f"  Hash: {password_hash}")
        return True
    else:
        print(f"✗ Password verification FAILED for '{user_nickname}'")
        return False

def main():
    if len(sys.argv) < 2:
        print("Usage: python3 test_password.py <username>")
        print("Example: python3 test_password.py user1")
        sys.exit(1)

    user_nickname = sys.argv[1]

    print(f"Testing password for user: {user_nickname}")
    print("-" * 60)

    password_hash = extract_password_hash(user_nickname)
    if password_hash:
        test_password(user_nickname, password_hash)

if __name__ == "__main__":
    main()
