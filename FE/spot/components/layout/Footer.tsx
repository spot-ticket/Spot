'use client';

import React from 'react';
import Link from 'next/link';

export function Footer() {
  return (
    <footer className="bg-gray-900 text-gray-300">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          {/* 로고 및 설명 */}
          <div className="col-span-1 md:col-span-2">
            <Link href="/" className="text-2xl font-bold text-orange-500">
              SPOT
            </Link>
            <p className="mt-4 text-sm">
              빠르고 편리한 음식 픽업 서비스
              <br />
              주문하고, 픽업하고, 맛있게 드세요!
            </p>
          </div>

          {/* 링크 */}
          <div>
            <h3 className="text-white font-semibold mb-4">서비스</h3>
            <ul className="space-y-2 text-sm">
              <li>
                <Link href="/" className="hover:text-orange-500">
                  가게 찾기
                </Link>
              </li>
              <li>
                <Link href="/orders" className="hover:text-orange-500">
                  주문내역
                </Link>
              </li>
              <li>
                <Link href="/mypage" className="hover:text-orange-500">
                  마이페이지
                </Link>
              </li>
            </ul>
          </div>

          {/* 고객센터 */}
          <div>
            <h3 className="text-white font-semibold mb-4">고객센터</h3>
            <ul className="space-y-2 text-sm">
              <li>전화: 1588-0000</li>
              <li>이메일: help@here.com</li>
              <li>운영시간: 09:00 - 18:00</li>
            </ul>
          </div>
        </div>

        <div className="mt-8 pt-8 border-t border-gray-700 text-sm text-center">
          <p>&copy; 2024 HERE. All rights reserved.</p>
        </div>
      </div>
    </footer>
  );
}

export default Footer;
