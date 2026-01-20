import type {Metadata} from 'next';
import './globals.css';
import {Header} from '@/components/layout/Header';
import {Footer} from '@/components/layout/Footer';
import {AuthProvider} from '@/components/providers/AuthProvider';

export const metadata: Metadata = {
  title: 'HERE - 음식 픽업 서비스',
  description: '빠르고 편리한 음식 픽업 주문 서비스',
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body className="min-h-screen flex flex-col">
        <AuthProvider>
          <Header />
          <main className="flex-1">{children}</main>
          <Footer />
        </AuthProvider>
      </body>
    </html>
  );
}
