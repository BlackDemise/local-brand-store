import React, { useState } from 'react';
import { Outlet, Navigate } from 'react-router-dom';
import { FaBars } from 'react-icons/fa';
import Sidebar from '../../components/layout/Sidebar';
import Button from '../../components/common/Button';

/**
 * AdminLayout Component
 * Layout wrapper for admin pages with sidebar navigation
 * Future: Add authentication check here
 */
const AdminLayout = () => {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  // TODO: Add authentication check
  // const { isAuthenticated, isAdmin } = useAuth();
  // if (!isAuthenticated) return <Navigate to="/login" />;
  // if (!isAdmin) return <Navigate to="/" />;

  const toggleSidebar = () => {
    setSidebarOpen(!sidebarOpen);
  };

  return (
    <div className="flex h-screen bg-gray-100">
      {/* Sidebar */}
      <Sidebar isOpen={sidebarOpen} onToggle={toggleSidebar} />

      {/* Main Content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Admin Header */}
        <header className="bg-white shadow-sm z-30">
          <div className="flex items-center justify-between px-6 py-4">
            <div className="flex items-center gap-4">
              <Button
                variant="outline"
                size="sm"
                onClick={toggleSidebar}
                className="lg:hidden"
                aria-label="Toggle sidebar"
              >
                <FaBars />
              </Button>
              <h1 className="text-2xl font-bold text-gray-900">
                Quản trị viên
              </h1>
            </div>

            {/* Future: Add user menu here */}
            <div className="flex items-center gap-4">
              {/* Placeholder for user dropdown */}
            </div>
          </div>
        </header>

        {/* Page Content */}
        <main className="flex-1 overflow-x-hidden overflow-y-auto bg-gray-100">
          <div className="container mx-auto px-6 py-8">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
};

export default AdminLayout;
