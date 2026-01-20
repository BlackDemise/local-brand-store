import React from 'react';
import { Link, useLocation } from 'react-router-dom';

/**
 * Main navigation bar
 */
const Navbar = () => {
  const location = useLocation();
  
  const isActive = (path) => {
    return location.pathname === path;
  };
  
  const linkClasses = (path) => {
    const baseClasses = 'px-4 py-2 rounded-md font-medium transition-colors';
    const activeClasses = 'bg-blue-600 text-white';
    const inactiveClasses = 'text-gray-700 hover:bg-gray-100';
    
    return `${baseClasses} ${isActive(path) ? activeClasses : inactiveClasses}`;
  };
  
  return (
    <nav className="bg-white border-b">
      <div className="container mx-auto px-4">
        <div className="flex items-center space-x-2 py-2">
          <Link to="/" className={linkClasses('/')}>
            Trang chủ
          </Link>
          <Link to="/products" className={linkClasses('/products')}>
            Sản phẩm
          </Link>
          <Link to="/track-order" className={linkClasses('/track-order')}>
            Tra cứu đơn hàng
          </Link>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
