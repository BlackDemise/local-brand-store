import React, { useState, useEffect } from 'react';
import Button from '../common/Button';
import Input from '../common/Input';
import Select from '../common/Select';

/**
 * ProductFilters component for filtering and sorting products
 * @param {Object} props
 * @param {Array} props.categories - Array of category objects
 * @param {Object} props.filters - Current filter values
 * @param {Function} props.onFilterChange - Callback when filters change
 * @param {Function} props.onClearFilters - Callback to clear all filters
 * @param {boolean} props.isMobile - Whether to show mobile version
 */
const ProductFilters = ({ 
  categories = [], 
  filters = {}, 
  onFilterChange, 
  onClearFilters,
  isMobile = false 
}) => {
  const [localFilters, setLocalFilters] = useState({
    categoryId: filters.categoryId || '',
    minPrice: filters.minPrice || '',
    maxPrice: filters.maxPrice || '',
    sortBy: filters.sortBy || 'createdAt',
    sortDir: filters.sortDir || 'desc',
  });

  // Sort options
  const sortOptions = [
    { value: 'createdAt:desc', label: 'Newest First' },
    { value: 'createdAt:asc', label: 'Oldest First' },
    { value: 'basePrice:asc', label: 'Price: Low to High' },
    { value: 'basePrice:desc', label: 'Price: High to Low' },
    { value: 'name:asc', label: 'Name: A to Z' },
    { value: 'name:desc', label: 'Name: Z to A' },
  ];

  const handleLocalChange = (field, value) => {
    setLocalFilters(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleSortChange = (value) => {
    const [sortBy, sortDir] = value.split(':');
    const newFilters = {
      ...localFilters,
      sortBy,
      sortDir
    };
    setLocalFilters(newFilters);
    // Apply sort immediately
    onFilterChange(newFilters);
  };

  const handleApplyFilters = () => {
    onFilterChange(localFilters);
  };

  const handleClear = () => {
    const clearedFilters = {
      categoryId: '',
      minPrice: '',
      maxPrice: '',
      sortBy: 'createdAt',
      sortDir: 'desc',
    };
    setLocalFilters(clearedFilters);
    onClearFilters();
  };

  // Sync with parent filters
  useEffect(() => {
    setLocalFilters({
      categoryId: filters.categoryId || '',
      minPrice: filters.minPrice || '',
      maxPrice: filters.maxPrice || '',
      sortBy: filters.sortBy || 'createdAt',
      sortDir: filters.sortDir || 'desc',
    });
  }, [filters]);

  const currentSortValue = `${localFilters.sortBy}:${localFilters.sortDir}`;

  return (
    <div className={`bg-white rounded-lg shadow-md p-6 ${isMobile ? 'mb-6' : ''}`}>
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-semibold text-gray-900">Filters</h3>
        <button
          onClick={handleClear}
          className="text-sm text-blue-600 hover:text-blue-800"
        >
          Clear All
        </button>
      </div>

      <div className="space-y-6">
        {/* Category Filter */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Category
          </label>
          <div className="space-y-2">
            <label className="flex items-center">
              <input
                type="radio"
                name="category"
                value=""
                checked={localFilters.categoryId === ''}
                onChange={(e) => handleLocalChange('categoryId', e.target.value)}
                className="mr-2"
              />
              <span className="text-sm text-gray-700">All Categories</span>
            </label>
            {categories.map((category) => (
              <label key={category.id} className="flex items-center">
                <input
                  type="radio"
                  name="category"
                  value={category.id}
                  checked={localFilters.categoryId === String(category.id)}
                  onChange={(e) => handleLocalChange('categoryId', e.target.value)}
                  className="mr-2"
                />
                <span className="text-sm text-gray-700">{category.name}</span>
              </label>
            ))}
          </div>
        </div>

        {/* Price Range Filter */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Price Range (VND)
          </label>
          <div className="grid grid-cols-2 gap-3">
            <Input
              type="number"
              placeholder="Min"
              value={localFilters.minPrice}
              onChange={(e) => handleLocalChange('minPrice', e.target.value)}
              min="0"
            />
            <Input
              type="number"
              placeholder="Max"
              value={localFilters.maxPrice}
              onChange={(e) => handleLocalChange('maxPrice', e.target.value)}
              min="0"
            />
          </div>
        </div>

        {/* Sort Options */}
        <div>
          <Select
            label="Sort By"
            value={currentSortValue}
            onChange={(e) => handleSortChange(e.target.value)}
            options={sortOptions}
          />
        </div>

        {/* Apply Button */}
        <Button
          onClick={handleApplyFilters}
          variant="primary"
          className="w-full"
        >
          Apply Filters
        </Button>
      </div>
    </div>
  );
};

export default ProductFilters;
