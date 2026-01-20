/**
 * Application-wide constants
 */

// API Base URL
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

// Order Status Enum
export const ORDER_STATUS = {
  PENDING: 'PENDING',
  PROCESSING: 'PROCESSING',
  SHIPPED: 'SHIPPED',
  DELIVERED: 'DELIVERED',
  CANCELLED: 'CANCELLED',
};

// Order Status Labels
export const ORDER_STATUS_LABELS = {
  [ORDER_STATUS.PENDING]: 'Chờ xử lý',
  [ORDER_STATUS.PROCESSING]: 'Đang xử lý',
  [ORDER_STATUS.SHIPPED]: 'Đã gửi hàng',
  [ORDER_STATUS.DELIVERED]: 'Đã giao hàng',
  [ORDER_STATUS.CANCELLED]: 'Đã hủy',
};

// Order Status Colors
export const ORDER_STATUS_COLORS = {
  [ORDER_STATUS.PENDING]: 'warning',
  [ORDER_STATUS.PROCESSING]: 'info',
  [ORDER_STATUS.SHIPPED]: 'primary',
  [ORDER_STATUS.DELIVERED]: 'success',
  [ORDER_STATUS.CANCELLED]: 'danger',
};

// Payment Method Enum
export const PAYMENT_METHOD = {
  COD: 'COD',
  BANK_TRANSFER: 'BANK_TRANSFER',
};

// Payment Method Labels
export const PAYMENT_METHOD_LABELS = {
  [PAYMENT_METHOD.COD]: 'Thanh toán khi nhận hàng (COD)',
  [PAYMENT_METHOD.BANK_TRANSFER]: 'Chuyển khoản ngân hàng',
};

// Pagination
export const ITEMS_PER_PAGE = 12;
export const ADMIN_ITEMS_PER_PAGE = 20;

// Reservation Timeout (in seconds)
export const RESERVATION_TIMEOUT = 15 * 60; // 15 minutes

// LocalStorage Keys
export const STORAGE_KEYS = {
  CART_TOKEN: 'cartToken',
  ACCESS_TOKEN: 'accessToken',
  USER_INFO: 'userInfo',
};

// Sort Options
export const SORT_OPTIONS = [
  { value: 'createdAt,desc', label: 'Mới nhất' },
  { value: 'createdAt,asc', label: 'Cũ nhất' },
  { value: 'basePrice,asc', label: 'Giá thấp đến cao' },
  { value: 'basePrice,desc', label: 'Giá cao đến thấp' },
  { value: 'name,asc', label: 'Tên A-Z' },
  { value: 'name,desc', label: 'Tên Z-A' },
];

// Toast Duration
export const TOAST_DURATION = 3000; // 3 seconds

// Debounce Delay
export const DEBOUNCE_DELAY = 500; // 500ms
