import React from 'react';
import Button from './Button';

/**
 * Error message component with retry option
 * @param {Object} props - Component props
 * @param {string} props.message - Error message
 * @param {Function} props.onRetry - Retry handler
 * @param {string} props.className - Additional CSS classes
 */
const ErrorMessage = ({ message, onRetry, className = '' }) => {
  return (
    <div className={`flex flex-col items-center justify-center p-8 ${className}`}>
      <svg
        className="w-16 h-16 text-red-500 mb-4"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
        />
      </svg>
      <h3 className="text-lg font-semibold text-gray-900 mb-2">Đã xảy ra lỗi</h3>
      <p className="text-gray-600 text-center mb-4">{message}</p>
      {onRetry && (
        <Button variant="primary" onClick={onRetry}>
          Thử lại
        </Button>
      )}
    </div>
  );
};

export default ErrorMessage;
