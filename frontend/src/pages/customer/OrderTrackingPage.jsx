import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Container from '../../components/layout/Container';
import Button from '../../components/common/Button';
import Spinner from '../../components/common/Spinner';
import TrackingInfo from '../../components/order/TrackingInfo';
import OrderDetails from '../../components/order/OrderDetails';
import * as orderApi from '../../api/orderApi';

/**
 * Order Tracking Page - Track order by tracking token
 * Phase 4: Complete implementation with timeline and detailed tracking
 */
const OrderTrackingPage = () => {
  const { trackingToken } = useParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [order, setOrder] = useState(null);
  const [error, setError] = useState(null);
  const [searchToken, setSearchToken] = useState(trackingToken || '');
  
  const handleSearch = async (e) => {
    e?.preventDefault();
    
    if (!searchToken.trim()) {
      setError('Vui lòng nhập mã tra cứu đơn hàng');
      return;
    }
    
    setLoading(true);
    setError(null);
    
    try {
      const orderData = await orderApi.trackOrder(searchToken.trim());
      setOrder(orderData);
    } catch (err) {
      console.error('Failed to track order:', err);
      setError('Không tìm thấy đơn hàng. Vui lòng kiểm tra lại mã tra cứu.');
      setOrder(null);
    } finally {
      setLoading(false);
    }
  };
  
  // Auto-search if tracking token is in URL
  React.useEffect(() => {
    if (trackingToken) {
      handleSearch();
    }
  }, [trackingToken]);
  
  return (
    <Container className="py-12">
      <div className="max-w-3xl mx-auto">
        {/* Page Title */}
        <h1 className="text-3xl font-bold text-gray-900 mb-8 text-center">
          Tra cứu đơn hàng
        </h1>
        
        {/* Search Form */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-8">
          <form onSubmit={handleSearch}>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Nhập mã tra cứu đơn hàng
            </label>
            <div className="flex gap-3">
              <input
                type="text"
                value={searchToken}
                onChange={(e) => setSearchToken(e.target.value)}
                placeholder="Ví dụ: ABC123XYZ"
                className="flex-1 px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              <Button
                type="submit"
                variant="primary"
                disabled={loading}
              >
                {loading ? 'Đang tìm...' : 'Tra cứu'}
              </Button>
            </div>
            {error && (
              <p className="mt-2 text-sm text-red-500">{error}</p>
            )}
          </form>
        </div>
        
        {/* Loading State */}
        {loading && (
          <div className="flex justify-center items-center py-12">
            <Spinner size="large" />
          </div>
        )}
        
        {/* Order Details */}
        {!loading && order && (
          <div className="space-y-6">
            {/* Tracking Info with Timeline */}
            <TrackingInfo order={order} />
            
            {/* Order Details */}
            <OrderDetails order={order} />
            
            {/* Back Button */}
            <div className="flex gap-4">
              <Button
                onClick={() => navigate('/')}
                variant="outline"
                className="flex-1"
              >
                Quay lại trang chủ
              </Button>
              <Button
                onClick={() => {
                  setSearchToken('');
                  setOrder(null);
                  navigate('/track');
                }}
                variant="secondary"
                className="flex-1"
              >
                Tra cứu đơn khác
              </Button>
            </div>
          </div>
        )}
      </div>
    </Container>
  );
};

export default OrderTrackingPage;
