import React from 'react';

/**
 * Page container with responsive padding and max-width
 * @param {Object} props - Component props
 * @param {React.ReactNode} props.children - Container content
 * @param {string} props.maxWidth - Max width class (default: 'max-w-7xl')
 * @param {string} props.className - Additional CSS classes
 */
const Container = ({ children, maxWidth = 'max-w-7xl', className = '' }) => {
  return (
    <div className={`container mx-auto px-4 py-6 ${maxWidth} ${className}`}>
      {children}
    </div>
  );
};

export default Container;
