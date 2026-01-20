/**
 * Validate email format
 * @param {string} email - Email to validate
 * @returns {boolean} - True if valid
 */
export const validateEmail = (email) => {
  if (!email) return false;
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

/**
 * Validate Vietnamese phone number
 * @param {string} phone - Phone to validate
 * @returns {boolean} - True if valid
 */
export const validatePhone = (phone) => {
  if (!phone) return false;
  
  // Remove all non-digits
  const cleaned = phone.replace(/\D/g, '');
  
  // Vietnamese phone: 10 or 11 digits, starts with 0
  return /^0\d{9,10}$/.test(cleaned);
};

/**
 * Validate required field
 * @param {any} value - Value to validate
 * @returns {boolean} - True if not empty
 */
export const validateRequired = (value) => {
  if (value === null || value === undefined) return false;
  if (typeof value === 'string') return value.trim().length > 0;
  return true;
};

/**
 * Validate minimum length
 * @param {string} value - Value to validate
 * @param {number} minLength - Minimum length required
 * @returns {boolean} - True if meets minimum length
 */
export const validateMinLength = (value, minLength) => {
  if (!value) return false;
  return value.length >= minLength;
};

/**
 * Validate maximum length
 * @param {string} value - Value to validate
 * @param {number} maxLength - Maximum length allowed
 * @returns {boolean} - True if within maximum length
 */
export const validateMaxLength = (value, maxLength) => {
  if (!value) return true;
  return value.length <= maxLength;
};

/**
 * Validate password strength
 * @param {string} password - Password to validate
 * @returns {Object} - { isValid: boolean, errors: string[] }
 */
export const validatePassword = (password) => {
  const errors = [];
  
  if (!password) {
    return { isValid: false, errors: ['Password is required'] };
  }
  
  if (password.length < 8) {
    errors.push('Password must be at least 8 characters');
  }
  
  if (!/[A-Z]/.test(password)) {
    errors.push('Password must contain at least one uppercase letter');
  }
  
  if (!/[a-z]/.test(password)) {
    errors.push('Password must contain at least one lowercase letter');
  }
  
  if (!/[0-9]/.test(password)) {
    errors.push('Password must contain at least one number');
  }
  
  return {
    isValid: errors.length === 0,
    errors,
  };
};

/**
 * Validate OTP code (6 digits)
 * @param {string} otp - OTP code to validate
 * @returns {boolean} - True if valid
 */
export const validateOtp = (otp) => {
  if (!otp) return false;
  return /^\d{6}$/.test(otp);
};
