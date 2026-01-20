import React, { useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import Container from '../../components/layout/Container';
import ProductList from '../../components/product/ProductList';
import ProductFilters from '../../components/product/ProductFilters';
import Pagination from '../../components/common/Pagination';
import useProducts from '../../hooks/useProducts';

/**
 * ProductListPage - Browse products with filters and pagination
 */
const ProductListPage = () => {
  const [searchParams, setSearchParams] = useSearchParams();

  // Get initial filters from URL
  const initialFilters = {
    categoryId: searchParams.get('categoryId') || '',
    minPrice: searchParams.get('minPrice') || '',
    maxPrice: searchParams.get('maxPrice') || '',
    sortBy: searchParams.get('sortBy') || 'createdAt',
    sortDir: searchParams.get('sortDir') || 'desc',
    page: parseInt(searchParams.get('page') || '0'),
  };

  const {
    products,
    categories,
    loading,
    error,
    pagination,
    filters,
    updateFilters,
    changePage,
    clearFilters,
    refetch,
  } = useProducts(initialFilters);

  // Update URL when filters change
  useEffect(() => {
    const params = {};
    
    if (filters.categoryId) params.categoryId = filters.categoryId;
    if (filters.minPrice) params.minPrice = filters.minPrice;
    if (filters.maxPrice) params.maxPrice = filters.maxPrice;
    if (filters.sortBy !== 'createdAt') params.sortBy = filters.sortBy;
    if (filters.sortDir !== 'desc') params.sortDir = filters.sortDir;
    if (filters.page > 0) params.page = filters.page;

    setSearchParams(params, { replace: true });
  }, [filters, setSearchParams]);

  const handleFilterChange = (newFilters) => {
    updateFilters(newFilters);
  };

  const handleClearFilters = () => {
    clearFilters();
  };

  const handlePageChange = (page) => {
    changePage(page);
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <Container>
        {/* Breadcrumb */}
        <nav className="text-sm text-gray-600 mb-6">
          <a href="/" className="hover:text-blue-600">Home</a>
          <span className="mx-2">/</span>
          <span className="text-gray-900">Shop</span>
        </nav>

        {/* Page Header */}
        <div className="mb-8">
          <h1 className="text-3xl lg:text-4xl font-bold text-gray-900 mb-2">
            All Products
          </h1>
          <p className="text-gray-600">
            {pagination.totalElements > 0 
              ? `Showing ${pagination.totalElements} product${pagination.totalElements !== 1 ? 's' : ''}`
              : 'No products found'
            }
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
          {/* Filters Sidebar - Desktop */}
          <aside className="hidden lg:block">
            <ProductFilters
              categories={categories}
              filters={filters}
              onFilterChange={handleFilterChange}
              onClearFilters={handleClearFilters}
            />
          </aside>

          {/* Filters - Mobile (Collapsible) */}
          <div className="lg:hidden">
            <details className="mb-6">
              <summary className="cursor-pointer bg-white rounded-lg shadow-md p-4 font-semibold text-gray-900 flex items-center justify-between">
                <span>Filters & Sort</span>
                <svg
                  className="w-5 h-5 text-gray-500"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M19 9l-7 7-7-7"
                  />
                </svg>
              </summary>
              <div className="mt-4">
                <ProductFilters
                  categories={categories}
                  filters={filters}
                  onFilterChange={handleFilterChange}
                  onClearFilters={handleClearFilters}
                  isMobile={true}
                />
              </div>
            </details>
          </div>

          {/* Main Content */}
          <main className="lg:col-span-3">
            {/* Product List */}
            <ProductList
              products={products}
              loading={loading}
              error={error}
              onRetry={refetch}
            />

            {/* Pagination */}
            {!loading && !error && products.length > 0 && pagination.totalPages > 1 && (
              <div className="mt-8">
                <Pagination
                  currentPage={pagination.currentPage}
                  totalPages={pagination.totalPages}
                  onPageChange={handlePageChange}
                />
              </div>
            )}
          </main>
        </div>
      </Container>
    </div>
  );
};

export default ProductListPage;
