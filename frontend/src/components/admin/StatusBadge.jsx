import React from 'react';
import PropTypes from 'prop-types';
import OrderStatus from '../order/OrderStatus';

/**
 * StatusBadge Component
 * Wrapper around OrderStatus for admin interface
 * Uses the same color-coded badges as customer view
 */
const StatusBadge = ({ status, size = 'md', showIcon = true, className = '' }) => {
  return (
    <OrderStatus
      status={status}
      size={size}
      showIcon={showIcon}
      className={className}
    />
  );
};

StatusBadge.propTypes = {
  status: PropTypes.string.isRequired,
  size: PropTypes.oneOf(['sm', 'md', 'lg']),
  showIcon: PropTypes.bool,
  className: PropTypes.string,
};

export default StatusBadge;
