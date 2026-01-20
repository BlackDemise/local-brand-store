import React from 'react';
import { Link } from 'react-router-dom';
import { FaShoppingBag, FaChartLine, FaUsers } from 'react-icons/fa';

/**
 * AdminDashboard Component
 * Admin dashboard home page with overview
 */
const AdminDashboard = () => {
  // Placeholder data - will be replaced with real data from API
  const stats = [
    {
      title: 'Đơn hàng mới',
      value: '12',
      icon: FaShoppingBag,
      color: 'bg-blue-500',
      link: '/admin/orders?status=PENDING_PAYMENT',
    },
    {
      title: 'Đang xử lý',
      value: '8',
      icon: FaChartLine,
      color: 'bg-yellow-500',
      link: '/admin/orders?status=CONFIRMED',
    },
    {
      title: 'Đang giao hàng',
      value: '15',
      icon: FaUsers,
      color: 'bg-purple-500',
      link: '/admin/orders?status=SHIPPING',
    },
  ];

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
        <p className="mt-2 text-gray-600">
          Chào mừng đến trang quản trị
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        {stats.map((stat, index) => {
          const Icon = stat.icon;
          return (
            <Link
              key={index}
              to={stat.link}
              className="bg-white rounded-lg shadow p-6 hover:shadow-lg transition-shadow"
            >
              <div className="flex items-center">
                <div className={`${stat.color} p-3 rounded-lg text-white`}>
                  <Icon className="text-2xl" />
                </div>
                <div className="ml-4">
                  <p className="text-sm text-gray-600">{stat.title}</p>
                  <p className="text-2xl font-bold text-gray-900">{stat.value}</p>
                </div>
              </div>
            </Link>
          );
        })}
      </div>

      {/* Quick Actions */}
      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-xl font-semibold mb-4">Thao tác nhanh</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <Link
            to="/admin/orders"
            className="flex items-center gap-3 p-4 border-2 border-gray-200 rounded-lg hover:border-blue-500 hover:bg-blue-50 transition-colors"
          >
            <FaShoppingBag className="text-2xl text-blue-600" />
            <div>
              <h3 className="font-semibold text-gray-900">Quản lý đơn hàng</h3>
              <p className="text-sm text-gray-600">Xem và quản lý tất cả đơn hàng</p>
            </div>
          </Link>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
