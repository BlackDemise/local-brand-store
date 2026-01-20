import React, { createContext, useContext, useState, useCallback } from 'react';

const ToastContext = createContext();

export const useToast = () => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within ToastProvider');
  }
  return context;
};

/**
 * Toast notification provider
 */
export const ToastProvider = ({ children }) => {
  const [toasts, setToasts] = useState([]);
  
  /**
   * Show toast notification
   * @param {string} message - Toast message
   * @param {'success'|'error'|'info'|'warning'} type - Toast type
   * @param {number} duration - Duration in ms (default 3000)
   */
  const showToast = useCallback((message, type = 'info', duration = 3000) => {
    const id = Date.now();
    const toast = { id, message, type };
    
    setToasts((prev) => [...prev, toast]);
    
    // Auto remove after duration
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, duration);
  }, []);
  
  /**
   * Success toast
   */
  const success = useCallback((message, duration) => {
    showToast(message, 'success', duration);
  }, [showToast]);
  
  /**
   * Error toast
   */
  const error = useCallback((message, duration) => {
    showToast(message, 'error', duration);
  }, [showToast]);
  
  /**
   * Info toast
   */
  const info = useCallback((message, duration) => {
    showToast(message, 'info', duration);
  }, [showToast]);
  
  /**
   * Warning toast
   */
  const warning = useCallback((message, duration) => {
    showToast(message, 'warning', duration);
  }, [showToast]);
  
  /**
   * Remove toast by id
   */
  const removeToast = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);
  
  const value = {
    toasts,
    showToast,
    success,
    error,
    info,
    warning,
    removeToast,
  };
  
  return (
    <ToastContext.Provider value={value}>
      {children}
      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </ToastContext.Provider>
  );
};

/**
 * Toast container component
 */
const ToastContainer = ({ toasts, onRemove }) => {
  if (toasts.length === 0) return null;
  
  const typeStyles = {
    success: 'bg-green-500',
    error: 'bg-red-500',
    info: 'bg-blue-500',
    warning: 'bg-yellow-500',
  };
  
  return (
    <div className="fixed top-4 right-4 z-50 space-y-2">
      {toasts.map((toast) => (
        <div
          key={toast.id}
          className={`${typeStyles[toast.type]} text-white px-6 py-3 rounded-lg shadow-lg flex items-center gap-2 min-w-[300px] animate-slide-in`}
        >
          <span className="flex-1">{toast.message}</span>
          <button
            onClick={() => onRemove(toast.id)}
            className="text-white hover:text-gray-200 focus:outline-none"
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      ))}
    </div>
  );
};
