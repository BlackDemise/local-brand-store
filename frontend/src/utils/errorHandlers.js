/**
 * Extract error message from API error response
 * @param {Error} error - Error object from API call
 * @returns {string} - User-friendly error message
 */
export const handleApiError = (error) => {
  if (error.response) {
    // Server responded with error
    const message = error.response.data?.message || 'An error occurred';
    return message;
  } else if (error.request) {
    // Request made but no response
    return 'Unable to connect to server. Please check your connection.';
  } else {
    // Error setting up request
    return error.message || 'An unexpected error occurred';
  }
};

/**
 * Map error codes to user-friendly messages
 * @param {string} errorCode - Error code from backend
 * @returns {string} - User-friendly error message
 */
export const getErrorMessage = (errorCode) => {
  const errorMessages = {
    // Cart errors
    'CART_NOT_FOUND': 'Giỏ hàng không tìm thấy',
    'CART_ITEM_NOT_FOUND': 'Sản phẩm không có trong giỏ hàng',
    'INSUFFICIENT_STOCK': 'Không đủ hàng trong kho',
    
    // Product errors
    'PRODUCT_NOT_FOUND': 'Sản phẩm không tìm thấy',
    'SKU_NOT_FOUND': 'Phiên bản sản phẩm không tìm thấy',
    'PRODUCT_OUT_OF_STOCK': 'Sản phẩm đã hết hàng',
    
    // Order errors
    'ORDER_NOT_FOUND': 'Đơn hàng không tìm thấy',
    'RESERVATION_EXPIRED': 'Thời gian giữ hàng đã hết. Vui lòng thêm lại vào giỏ hàng.',
    'RESERVATION_NOT_FOUND': 'Không tìm thấy đơn hàng tạm giữ',
    'INVALID_ORDER_STATUS': 'Trạng thái đơn hàng không hợp lệ',
    
    // Auth errors
    'INVALID_CREDENTIALS': 'Email hoặc mật khẩu không đúng',
    'EMAIL_ALREADY_EXISTS': 'Email đã được sử dụng',
    'USER_NOT_FOUND': 'Người dùng không tìm thấy',
    'INVALID_TOKEN': 'Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại.',
    'TOKEN_EXPIRED': 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.',
    'INVALID_OTP': 'Mã OTP không đúng',
    'OTP_EXPIRED': 'Mã OTP đã hết hạn',
    'UNAUTHORIZED': 'Bạn không có quyền truy cập',
    
    // Validation errors
    'INVALID_INPUT': 'Dữ liệu nhập không hợp lệ',
    'VALIDATION_ERROR': 'Vui lòng kiểm tra lại thông tin',
    
    // Generic errors
    'INTERNAL_SERVER_ERROR': 'Lỗi hệ thống. Vui lòng thử lại sau.',
    'BAD_REQUEST': 'Yêu cầu không hợp lệ',
    'NOT_FOUND': 'Không tìm thấy tài nguyên',
  };
  
  return errorMessages[errorCode] || 'Đã xảy ra lỗi. Vui lòng thử lại.';
};

/**
 * Show error toast notification
 * @param {Error|string} error - Error object or message
 * @param {Function} toast - Toast notification function
 */
export const showErrorToast = (error, toast) => {
  const message = typeof error === 'string' ? error : handleApiError(error);
  if (toast) {
    toast.error(message);
  } else {
    console.error(message);
  }
};

/**
 * Show success toast notification
 * @param {string} message - Success message
 * @param {Function} toast - Toast notification function
 */
export const showSuccessToast = (message, toast) => {
  if (toast) {
    toast.success(message);
  } else {
    console.log(message);
  }
};
