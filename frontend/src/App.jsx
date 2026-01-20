import React from 'react'
import { Routes, Route } from 'react-router-dom'
import './App.css'
import Header from './components/layout/Header'
import Footer from './components/layout/Footer'
import ProtectedRoute from './components/auth/ProtectedRoute'

// Import customer pages
import HomePage from './pages/customer/HomePage'
import ProductListPage from './pages/customer/ProductListPage'
import ProductDetailPage from './pages/customer/ProductDetailPage'
import CartPage from './pages/customer/CartPage'
import CheckoutPage from './pages/customer/CheckoutPage'
import OrderConfirmationPage from './pages/customer/OrderConfirmationPage'
import OrderTrackingPage from './pages/customer/OrderTrackingPage'
import NotFoundPage from './pages/customer/NotFoundPage'
import LoginPage from './pages/customer/LoginPage'
import RegisterPage from './pages/customer/RegisterPage'

// Import admin pages and layout
import AdminLayout from './pages/admin/AdminLayout'
import AdminDashboard from './pages/admin/AdminDashboard'
import OrderListPage from './pages/admin/OrderListPage'
import OrderDetailPage from './pages/admin/OrderDetailPage'

/**
 * Main App component with routing
 */
function App() {
  return (
    <Routes>
      {/* Customer routes with Header and Footer */}
      <Route path="/" element={
        <div className="flex flex-col min-h-screen">
          <Header />
          <main className="flex-1">
            <HomePage />
          </main>
          <Footer />
        </div>
      } />
      
      <Route path="/products" element={
        <div className="flex flex-col min-h-screen">
          <Header />
          <main className="flex-1">
            <ProductListPage />
          </main>
          <Footer />
        </div>
      } />
      
      <Route path="/products/:slug" element={
        <div className="flex flex-col min-h-screen">
          <Header />
          <main className="flex-1">
            <ProductDetailPage />
          </main>
          <Footer />
        </div>
      } />
      
      <Route path="/cart" element={
        <div className="flex flex-col min-h-screen">
          <Header />
          <main className="flex-1">
            <CartPage />
          </main>
          <Footer />
        </div>
      } />
      
      <Route path="/checkout" element={
        <div className="flex flex-col min-h-screen">
          <Header />
          <main className="flex-1">
            <CheckoutPage />
          </main>
          <Footer />
        </div>
      } />
      
      <Route path="/order-confirmation" element={
        <div className="flex flex-col min-h-screen">
          <Header />
          <main className="flex-1">
            <OrderConfirmationPage />
          </main>
          <Footer />
        </div>
      } />
      
      <Route path="/track/:trackingToken" element={
        <div className="flex flex-col min-h-screen">
          <Header />
          <main className="flex-1">
            <OrderTrackingPage />
          </main>
          <Footer />
        </div>
      } />
      
      <Route path="/track" element={
        <div className="flex flex-col min-h-screen">
          <Header />
          <main className="flex-1">
            <OrderTrackingPage />
          </main>
          <Footer />
        </div>
      } />
      
      {/* Auth routes */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      
      {/* Admin routes with AdminLayout */}
      <Route path="/admin" element={
        <ProtectedRoute>
          <AdminLayout />
        </ProtectedRoute>
      }>
        <Route index element={<AdminDashboard />} />
        <Route path="orders" element={<OrderListPage />} />
        <Route path="orders/:orderId" element={<OrderDetailPage />} />
      </Route>
      
      {/* 404 */}
      <Route path="*" element={
        <div className="flex flex-col min-h-screen">
          <Header />
          <main className="flex-1">
            <NotFoundPage />
          </main>
          <Footer />
        </div>
      } />
    </Routes>
  )
}

export default App
