import React from 'react';
import Button from './Button';

/**
 * Pagination component
 * @param {Object} props - Component props
 * @param {number} props.currentPage - Current page number (0-indexed)
 * @param {number} props.totalPages - Total number of pages
 * @param {Function} props.onPageChange - Page change handler
 * @param {number} props.itemsPerPage - Items per page (for display)
 * @param {number} props.totalItems - Total number of items (for display)
 * @param {string} props.className - Additional CSS classes
 */
const Pagination = ({
  currentPage = 0,
  totalPages = 1,
  onPageChange,
  itemsPerPage,
  totalItems,
  className = '',
}) => {
  if (totalPages <= 1) return null;
  
  const pages = [];
  const maxVisiblePages = 5;
  
  let startPage = Math.max(0, currentPage - Math.floor(maxVisiblePages / 2));
  let endPage = Math.min(totalPages - 1, startPage + maxVisiblePages - 1);
  
  if (endPage - startPage < maxVisiblePages - 1) {
    startPage = Math.max(0, endPage - maxVisiblePages + 1);
  }
  
  for (let i = startPage; i <= endPage; i++) {
    pages.push(i);
  }
  
  const handlePageClick = (page) => {
    if (page >= 0 && page < totalPages && page !== currentPage) {
      onPageChange(page);
    }
  };
  
  return (
    <div className={`flex items-center justify-between ${className}`}>
      {/* Info */}
      {itemsPerPage && totalItems !== undefined && (
        <div className="text-sm text-gray-700">
          Hiển thị{' '}
          <span className="font-medium">{Math.min(currentPage * itemsPerPage + 1, totalItems)}</span>
          {' - '}
          <span className="font-medium">{Math.min((currentPage + 1) * itemsPerPage, totalItems)}</span>
          {' trong '}
          <span className="font-medium">{totalItems}</span>
          {' kết quả'}
        </div>
      )}
      
      {/* Pagination */}
      <div className="flex items-center gap-2">
        <Button
          variant="outline"
          size="sm"
          onClick={() => handlePageClick(currentPage - 1)}
          disabled={currentPage === 0}
        >
          Trước
        </Button>
        
        {startPage > 0 && (
          <>
            <Button
              variant="outline"
              size="sm"
              onClick={() => handlePageClick(0)}
            >
              1
            </Button>
            {startPage > 1 && <span className="text-gray-500">...</span>}
          </>
        )}
        
        {pages.map((page) => (
          <Button
            key={page}
            variant={page === currentPage ? 'primary' : 'outline'}
            size="sm"
            onClick={() => handlePageClick(page)}
          >
            {page + 1}
          </Button>
        ))}
        
        {endPage < totalPages - 1 && (
          <>
            {endPage < totalPages - 2 && <span className="text-gray-500">...</span>}
            <Button
              variant="outline"
              size="sm"
              onClick={() => handlePageClick(totalPages - 1)}
            >
              {totalPages}
            </Button>
          </>
        )}
        
        <Button
          variant="outline"
          size="sm"
          onClick={() => handlePageClick(currentPage + 1)}
          disabled={currentPage === totalPages - 1}
        >
          Sau
        </Button>
      </div>
    </div>
  );
};

export default Pagination;
