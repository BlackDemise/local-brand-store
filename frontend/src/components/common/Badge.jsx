import React from 'react';

/**
 * Badge component for status indicators
 * @param {Object} props - Component props
 * @param {React.ReactNode} props.children - Badge content
 * @param {'success'|'warning'|'danger'|'info'|'default'} props.variant - Badge color variant
 * @param {string} props.className - Additional CSS classes
 * @param {Function} props.onClick - Click handler (makes badge clickable)
 */
const Badge = ({ children, variant = 'default', className = '', onClick }) => {
  const baseClasses = 'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium';
  const clickableClasses = onClick ? 'cursor-pointer hover:opacity-80 transition-opacity' : '';
  
  const variantClasses = {
    success: 'bg-green-100 text-green-800',
    warning: 'bg-yellow-100 text-yellow-800',
    danger: 'bg-red-100 text-red-800',
    info: 'bg-blue-100 text-blue-800',
    default: 'bg-gray-100 text-gray-800',
  };
  
  const Component = onClick ? 'button' : 'span';
  
  return (
    <Component 
      className={`${baseClasses} ${variantClasses[variant]} ${clickableClasses} ${className}`}
      onClick={onClick}
    >
      {children}
    </Component>
  );
};

export default Badge;
