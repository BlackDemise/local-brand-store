import { useState, useEffect } from 'react';
import { getProducts, getCategories } from '../api/productApi';

/**
 * Custom hook for fetching products with filters
 * @param {Object} initialFilters - Initial filter values
 * @returns {Object} - Products data, loading state, error, and refetch function
 */
const useProducts = (initialFilters = {}) => {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [pagination, setPagination] = useState({
    currentPage: 1,
    totalPages: 1,
    totalElements: 0,
    pageSize: 12,
  });

  const [filters, setFilters] = useState({
    page: 0, // API uses 0-based indexing
    size: 12,
    categoryId: '',
    minPrice: '',
    maxPrice: '',
    sortBy: 'createdAt',
    sortDir: 'desc',
    ...initialFilters,
  });

  // Fetch categories on mount
  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const data = await getCategories();
        setCategories(data);
      } catch (err) {
        console.error('Failed to fetch categories:', err);
      }
    };

    fetchCategories();
  }, []);

  // Fetch products when filters change
  useEffect(() => {
    const fetchProducts = async () => {
      setLoading(true);
      setError(null);

      try {
        // Build query params, excluding empty values
        const params = {};
        Object.keys(filters).forEach(key => {
          const value = filters[key];
          if (value !== '' && value !== null && value !== undefined) {
            params[key] = value;
          }
        });

        const response = await getProducts(params);

        setProducts(response.content || []);
        setPagination({
          currentPage: response.number + 1, // Convert to 1-based for display
          totalPages: response.totalPages || 1,
          totalElements: response.totalElements || 0,
          pageSize: response.size || 12,
        });
      } catch (err) {
        console.error('Failed to fetch products:', err);
        setError(err.message || 'Failed to load products');
        setProducts([]);
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, [filters]);

  // Update filters
  const updateFilters = (newFilters) => {
    setFilters(prev => ({
      ...prev,
      ...newFilters,
      page: 0, // Reset to first page when filters change
    }));
  };

  // Change page
  const changePage = (page) => {
    setFilters(prev => ({
      ...prev,
      page: page - 1, // Convert to 0-based for API
    }));
    // Scroll to top
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  // Clear all filters
  const clearFilters = () => {
    setFilters({
      page: 0,
      size: 12,
      categoryId: '',
      minPrice: '',
      maxPrice: '',
      sortBy: 'createdAt',
      sortDir: 'desc',
    });
  };

  // Refetch with current filters
  const refetch = () => {
    setFilters(prev => ({ ...prev }));
  };

  return {
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
  };
};

export default useProducts;
