import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Container from '../../components/layout/Container';
import ProductList from '../../components/product/ProductList';
import Button from '../../components/common/Button';
import Card from '../../components/common/Card';
import { getProducts, getCategories } from '../../api/productApi';

/**
 * HomePage - Landing page with hero section, featured products, and categories
 */
const HomePage = () => {
  const navigate = useNavigate();
  const [featuredProducts, setFeaturedProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        
        // Fetch featured products (latest 8 products)
        const productsResponse = await getProducts({ 
          page: 0, 
          size: 8, 
          sortBy: 'createdAt', 
          sortDir: 'desc' 
        });
        setFeaturedProducts(productsResponse.content || []);

        // Fetch categories
        const categoriesData = await getCategories();
        setCategories(categoriesData || []);
      } catch (err) {
        console.error('Failed to fetch home page data:', err);
        setError(err.message || 'Failed to load data');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const handleShopNowClick = () => {
    navigate('/products');
  };

  const handleCategoryClick = (categoryId) => {
    navigate(`/products?categoryId=${categoryId}`);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Hero Section */}
      <section className="bg-gradient-to-r from-blue-600 to-blue-800 text-white">
        <Container>
          <div className="py-20 lg:py-32 text-center">
            <h1 className="text-4xl lg:text-6xl font-bold mb-6">
              Welcome to Local Brand Store
            </h1>
            <p className="text-xl lg:text-2xl mb-8 text-blue-100">
              Discover authentic Vietnamese streetwear and fashion
            </p>
            <Button
              onClick={handleShopNowClick}
              variant="secondary"
              className="bg-white text-blue-600 hover:bg-gray-100 px-8 py-4 text-lg"
            >
              Shop Now
            </Button>
          </div>
        </Container>
      </section>

      {/* Categories Showcase */}
      <section className="py-16 bg-white">
        <Container>
          <div className="text-center mb-12">
            <h2 className="text-3xl lg:text-4xl font-bold text-gray-900 mb-4">
              Shop by Category
            </h2>
            <p className="text-lg text-gray-600">
              Browse our collection by category
            </p>
          </div>

          {categories.length > 0 ? (
            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-6">
              {categories.map((category) => (
                <Card
                  key={category.id}
                  onClick={() => handleCategoryClick(category.id)}
                  className="text-center p-6 hover:scale-105 transition-transform"
                >
                  <div className="w-16 h-16 mx-auto mb-4 bg-blue-100 rounded-full flex items-center justify-center">
                    <svg
                      className="w-8 h-8 text-blue-600"
                      fill="none"
                      viewBox="0 0 24 24"
                      stroke="currentColor"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"
                      />
                    </svg>
                  </div>
                  <h3 className="text-lg font-semibold text-gray-900 mb-1">
                    {category.name}
                  </h3>
                  {category.description && (
                    <p className="text-sm text-gray-600 line-clamp-2">
                      {category.description}
                    </p>
                  )}
                </Card>
              ))}
            </div>
          ) : (
            <div className="text-center py-8 text-gray-500">
              No categories available
            </div>
          )}
        </Container>
      </section>

      {/* Featured Products */}
      <section className="py-16 bg-gray-50">
        <Container>
          <div className="text-center mb-12">
            <h2 className="text-3xl lg:text-4xl font-bold text-gray-900 mb-4">
              Featured Products
            </h2>
            <p className="text-lg text-gray-600">
              Check out our latest arrivals
            </p>
          </div>

          <ProductList
            products={featuredProducts}
            loading={loading}
            error={error}
            onRetry={() => window.location.reload()}
          />

          {/* View All Button */}
          {featuredProducts.length > 0 && (
            <div className="text-center mt-12">
              <Button
                onClick={handleShopNowClick}
                variant="outline"
                className="px-8 py-3"
              >
                View All Products
              </Button>
            </div>
          )}
        </Container>
      </section>

      {/* Features Section */}
      <section className="py-16 bg-white">
        <Container>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="text-center">
              <div className="w-16 h-16 mx-auto mb-4 bg-green-100 rounded-full flex items-center justify-center">
                <svg
                  className="w-8 h-8 text-green-600"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M5 13l4 4L19 7"
                  />
                </svg>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-2">
                Free Shipping
              </h3>
              <p className="text-gray-600">
                On orders over 500,000 ₫
              </p>
            </div>

            <div className="text-center">
              <div className="w-16 h-16 mx-auto mb-4 bg-blue-100 rounded-full flex items-center justify-center">
                <svg
                  className="w-8 h-8 text-blue-600"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                  />
                </svg>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-2">
                7-Day Returns
              </h3>
              <p className="text-gray-600">
                Easy return policy
              </p>
            </div>

            <div className="text-center">
              <div className="w-16 h-16 mx-auto mb-4 bg-purple-100 rounded-full flex items-center justify-center">
                <svg
                  className="w-8 h-8 text-purple-600"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
                  />
                </svg>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-2">
                Secure Payment
              </h3>
              <p className="text-gray-600">
                100% secure transactions
              </p>
            </div>
          </div>
        </Container>
      </section>
    </div>
  );
};

export default HomePage;
