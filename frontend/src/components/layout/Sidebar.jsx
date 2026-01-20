import React from 'react';
import PropTypes from 'prop-types';
import { Link, useLocation } from 'react-router-dom';
import {
  FaShoppingBag,
  FaTimes,
  FaChartLine,
  FaHome,
} from 'react-icons/fa';

/**
 * Sidebar Component
 * Admin navigation sidebar with collapsible mobile menu
 */
const Sidebar = ({ isOpen, onToggle }) => {
  const location = useLocation();

  const menuItems = [
    {
      path: '/admin',
      label: 'Dashboard',
      icon: FaChartLine,
    },
    {
      path: '/admin/orders',
      label: 'Quản lý đơn hàng',
      icon: FaShoppingBag,
    },
  ];

  const isActive = (path) => {
    if (path === '/admin') {
      return location.pathname === '/admin';
    }
    return location.pathname.startsWith(path);
  };

  return (
    <>
      {/* Mobile Overlay */}
      {isOpen && (
        <div
          className="fixed inset-0 bg-black bg-opacity-50 z-40 lg:hidden"
          onClick={onToggle}
        />
      )}

      {/* Sidebar */}
      <aside
        className={`
          fixed lg:sticky top-0 left-0 z-50 h-screen w-64 bg-gray-900 text-white
          transform transition-transform duration-300 ease-in-out
          ${isOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}
        `}
      >
        <div className="flex flex-col h-full">
          {/* Header */}
          <div className="flex items-center justify-between p-6 border-b border-gray-800">
            <Link to="/admin" className="flex items-center gap-2">
              <FaShoppingBag className="text-2xl text-blue-400" />
              <span className="text-xl font-bold">Admin Panel</span>
            </Link>
            <button
              onClick={onToggle}
              className="lg:hidden text-gray-400 hover:text-white transition-colors"
              aria-label="Close sidebar"
            >
              <FaTimes className="text-xl" />
            </button>
          </div>

          {/* Navigation */}
          <nav className="flex-1 overflow-y-auto py-6">
            <ul className="space-y-2 px-4">
              {menuItems.map((item) => {
                const Icon = item.icon;
                const active = isActive(item.path);

                return (
                  <li key={item.path}>
                    <Link
                      to={item.path}
                      className={`
                        flex items-center gap-3 px-4 py-3 rounded-lg
                        transition-colors duration-200
                        ${
                          active
                            ? 'bg-blue-600 text-white'
                            : 'text-gray-300 hover:bg-gray-800 hover:text-white'
                        }
                      `}
                      onClick={() => {
                        // Close mobile menu when clicking a link
                        if (window.innerWidth < 1024) {
                          onToggle();
                        }
                      }}
                    >
                      <Icon className="text-xl" />
                      <span className="font-medium">{item.label}</span>
                    </Link>
                  </li>
                );
              })}
            </ul>
          </nav>

          {/* Footer - Back to Store */}
          <div className="p-4 border-t border-gray-800">
            <Link
              to="/"
              className="flex items-center gap-3 px-4 py-3 text-gray-300 hover:bg-gray-800 hover:text-white rounded-lg transition-colors"
            >
              <FaHome className="text-xl" />
              <span className="font-medium">Về trang chủ</span>
            </Link>
          </div>
        </div>
      </aside>
    </>
  );
};

Sidebar.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onToggle: PropTypes.func.isRequired,
};

export default Sidebar;
