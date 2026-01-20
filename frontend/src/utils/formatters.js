/**
 * Format number to Vietnamese Dong (VND) currency
 * @param {number} amount - Amount to format
 * @returns {string} - Formatted currency string (e.g., "1.000.000 ₫")
 */
export const formatCurrency = (amount) => {
  if (amount === null || amount === undefined) return '0 ₫';
  
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(amount);
};

/**
 * Format date to Vietnamese format
 * @param {string|Date} date - Date to format
 * @returns {string} - Formatted date (e.g., "09/01/2026")
 */
export const formatDate = (date) => {
  if (!date) return '';
  
  const d = new Date(date);
  return new Intl.DateTimeFormat('vi-VN').format(d);
};

/**
 * Format date and time to Vietnamese format
 * @param {string|Date} date - Date to format
 * @returns {string} - Formatted datetime (e.g., "09/01/2026 14:30")
 */
export const formatDateTime = (date) => {
  if (!date) return '';
  
  const d = new Date(date);
  return new Intl.DateTimeFormat('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(d);
};

/**
 * Format Vietnamese phone number
 * @param {string} phone - Phone number to format
 * @returns {string} - Formatted phone (e.g., "0123 456 789")
 */
export const formatPhoneNumber = (phone) => {
  if (!phone) return '';
  
  // Remove all non-digits
  const cleaned = phone.replace(/\D/g, '');
  
  // Format based on length
  if (cleaned.length === 10) {
    return cleaned.replace(/(\d{4})(\d{3})(\d{3})/, '$1 $2 $3');
  } else if (cleaned.length === 11) {
    return cleaned.replace(/(\d{4})(\d{3})(\d{4})/, '$1 $2 $3');
  }
  
  return phone;
};

/**
 * Format product price range
 * @param {number} minPrice - Minimum price
 * @param {number} maxPrice - Maximum price
 * @returns {string} - Formatted price range
 */
export const formatPriceRange = (minPrice, maxPrice) => {
  if (!minPrice && !maxPrice) return '';
  if (minPrice === maxPrice) return formatCurrency(minPrice);
  if (!maxPrice) return `Từ ${formatCurrency(minPrice)}`;
  if (!minPrice) return `Đến ${formatCurrency(maxPrice)}`;
  return `${formatCurrency(minPrice)} - ${formatCurrency(maxPrice)}`;
};

/**
 * Format time remaining (e.g., for reservation countdown)
 * @param {number} seconds - Seconds remaining
 * @returns {string} - Formatted time (e.g., "14:35")
 */
export const formatTimeRemaining = (seconds) => {
  if (seconds <= 0) return '00:00';
  
  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};
