// API Response 공통 타입
export interface ApiResponse<T> {
  isSuccess: boolean;
  code: string;
  message: string;
  result: T;
}

// 사용자 관련 타입
export type Role = 'CUSTOMER' | 'OWNER' | 'CHEF' | 'MANAGER' | 'MASTER';

export interface User {
  id: number;
  username: string;
  role: Role;
  nickname: string;
  email: string;
  roadAddress: string;
  addressDetail: string;
  age: number;
  male: boolean;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
}

export interface JoinRequest {
  username: string;
  password: string;
  nickname: string;
  email: string;
  male: boolean;
  age: number;
  roadAddress: string;
  addressDetail: string;
  role: Role;
}

// 가게 관련 타입
export interface Store {
  id: string;
  name: string;
  roadAddress: string;
  addressDetail: string;
  phoneNumber: string;
  openTime: string;
  closeTime: string;
  categories?: Category[];
}

export interface StoreListResponse {
  content: Store[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

// 카테고리 관련 타입
export interface Category {
  id: string;
  name: string;
}

// 메뉴 관련 타입
export interface Menu {
  id: string;
  storeId: string;
  name: string;
  category: string;
  price: number;
  description: string;
  imageUrl: string;
  isAvailable?: boolean;
  isHidden?: boolean;
  options?: MenuOption[];
}

export interface MenuOption {
  id: string;
  optionName: string;
  optionPrice: number;
}

// 주문 관련 타입
export type OrderStatus =
  | 'PAYMENT_PENDING'
  | 'PENDING'
  | 'ACCEPTED'
  | 'REJECTED'
  | 'COOKING'
  | 'READY'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'PAYMENT_FAILED';

export interface OrderItem {
  menuId: string;
  quantity: number;
  orderItemOptions?: OrderItemOption[];
}

export interface OrderItemOption {
  optionId: string;
  value: string;
}

export interface OrderCreateRequest {
  storeId: string;
  orderItems: OrderItem[];
  pickupTime: string;
  needDisposables: boolean;
  request?: string;
}

export interface OrderResponse {
  id: string;
  userId: number;
  storeId: string;
  storeName: string;
  orderNumber: string;
  needDisposables: boolean;
  request: string;
  pickupTime: string;
  orderStatus: OrderStatus;
  estimatedTime?: number;
  reason?: string;
  cancelledBy?: string;
  createdAt: string;
  orderItems: OrderItemResponse[];
  totalAmount: number;
}

export interface OrderItemResponse {
  itemId: string;
  menuId: string;
  menuName: string;
  quantity: number;
  price: number;
  subtotal: number;
  orderItemOptions?: OrderItemOptionResponse[];
}

export interface OrderItemOptionResponse {
  optionId: string;
  optionName: string;
  optionValue: string;
}

// 결제 관련 타입
export type PaymentMethod = 'CREDIT_CARD' | 'DEBIT_CARD' | 'BANK_TRANSFER';

export interface PaymentConfirmRequest {
  title: string;
  content: string;
  userId: number;
  orderId: string;
  paymentMethod: PaymentMethod;
  paymentAmount: number;
}

export interface PaymentConfirmResponse {
  paymentId: string;
  status: string;
  amount: number;
  approvedAt: string;
}

// 장바구니 타입
export interface CartItem {
  menu: Menu;
  quantity: number;
  selectedOptions: MenuOption[];
}

export interface Cart {
  storeId: string;
  storeName: string;
  items: CartItem[];
}
