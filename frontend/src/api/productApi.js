import client from './client';

/**
 * Get paginated and filtered products
 * @param {Object} filters - { page, size, categoryId, minPrice, maxPrice, sortBy, sortDir }
 * @returns {Promise<Object>} - Paginated product response
 */
export const getProducts = async (filters = {}) => {
  const response = await client.get('/product', { params: filters });
  return response.data.result;
};

/**
 * Get single product by ID
 * @param {number} id - Product ID
 * @returns {Promise<Object>} - Product details
 */
export const getProductById = async (id) => {
  const response = await client.get(`/product/${id}`);
  return response.data.result;
};

/**
 * Get single product by slug
 * @param {string} slug - Product slug
 * @returns {Promise<Object>} - Product details
 */
export const getProductBySlug = async (slug) => {
  const response = await client.get(`/product/slug/${slug}`);
  return response.data.result;
};

/**
 * Get all categories
 * @returns {Promise<Array>} - List of categories
 */
export const getCategories = async () => {
  const response = await client.get('/category');
  return response.data.result;
};
