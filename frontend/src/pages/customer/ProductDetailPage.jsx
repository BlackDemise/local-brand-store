import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Container from '../../components/layout/Container';
import ProductDetail from '../../components/product/ProductDetail';
import Spinner from '../../components/common/Spinner';
import ErrorMessage from '../../components/common/ErrorMessage';
import { getProductBySlug } from '../../api/productApi';

/**
 * ProductDetailPage - View single product details with SKU selection and add to cart
 */
const ProductDetailPage = () => {
  const { slug } = useParams();
  const navigate = useNavigate();

  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchProduct = async () => {
      try {
        setLoading(true);
        setError(null);
        
        const data = await getProductBySlug(slug);
        setProduct(data);
      } catch (err) {
        console.error('Failed to fetch product:', err);
        setError(err.message || 'Failed to load product');
      } finally {
        setLoading(false);
      }
    };

    if (slug) {
      fetchProduct();
    }
  }, [slug]);

  const handleRetry = () => {
    window.location.reload();
  };

  // Loading State
  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <Container>
          <div className="flex justify-center items-center py-20">
            <Spinner size="large" />
          </div>
        </Container>
      </div>
    );
  }

  // Error State
  if (error || !product) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <Container>
          <div className="py-20">
            <ErrorMessage 
              message={error || 'Product not found'} 
              onRetry={handleRetry} 
            />
            <div className="text-center mt-6">
              <button
                onClick={() => navigate('/products')}
                className="text-blue-600 hover:text-blue-800 font-medium"
              >
                ← Back to Products
              </button>
            </div>
          </div>
        </Container>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <Container>
        {/* Breadcrumb */}
        <nav className="text-sm text-gray-600 mb-6">
          <a href="/" className="hover:text-blue-600">Home</a>
          <span className="mx-2">/</span>
          <a href="/products" className="hover:text-blue-600">Shop</a>
          {product.category && (
            <>
              <span className="mx-2">/</span>
              <a 
                href={`/products?categoryId=${product.category.id}`}
                className="hover:text-blue-600"
              >
                {product.category.name}
              </a>
            </>
          )}
          <span className="mx-2">/</span>
          <span className="text-gray-900">{product.name}</span>
        </nav>

        {/* Product Detail */}
        <div className="bg-white rounded-lg shadow-md p-6 lg:p-8">
          <ProductDetail product={product} />
        </div>

        {/* Back to Products Link */}
        <div className="mt-8 text-center">
          <button
            onClick={() => navigate('/products')}
            className="text-blue-600 hover:text-blue-800 font-medium inline-flex items-center"
          >
            <svg
              className="w-5 h-5 mr-2"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M15 19l-7-7 7-7"
              />
            </svg>
            Continue Shopping
          </button>
        </div>
      </Container>
    </div>
  );
};

export default ProductDetailPage;
