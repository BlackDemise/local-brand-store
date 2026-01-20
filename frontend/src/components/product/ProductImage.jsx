import React, { useState } from 'react';
import Modal from '../common/Modal';

/**
 * ProductImage component with gallery and zoom functionality
 * @param {Object} props
 * @param {Array} props.images - Array of image objects with url and alt
 * @param {string} props.productName - Product name for alt text
 */
const ProductImage = ({ images = [], productName = '' }) => {
  const [selectedImageIndex, setSelectedImageIndex] = useState(0);
  const [isModalOpen, setIsModalOpen] = useState(false);

  // Use placeholder if no images
  const displayImages = images.length > 0 
    ? images 
    : [{ url: '/placeholder-product.jpg', alt: productName }];

  const currentImage = displayImages[selectedImageIndex];

  const handleThumbnailClick = (index) => {
    setSelectedImageIndex(index);
  };

  const handleImageClick = () => {
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
  };

  return (
    <div className="space-y-4">
      {/* Main Image */}
      <div 
        className="relative w-full pt-[100%] bg-gray-200 rounded-lg overflow-hidden cursor-zoom-in"
        onClick={handleImageClick}
      >
        <img
          src={currentImage}
          alt={currentImage.alt || productName}
          className="absolute top-0 left-0 w-full h-full object-cover"
          loading="lazy"
        />
        {/* Zoom Hint */}
        <div className="absolute bottom-4 right-4 bg-black bg-opacity-50 text-white text-xs px-2 py-1 rounded">
          Click to enlarge
        </div>
      </div>

      {/* Thumbnail Gallery */}
      {displayImages.length > 1 && (
        <div className="flex gap-2 overflow-x-auto pb-2">
          {displayImages.map((image, index) => (
            <button
              key={index}
              onClick={() => handleThumbnailClick(index)}
              className={`
                flex-shrink-0 w-20 h-20 rounded-md overflow-hidden border-2 transition-all
                ${selectedImageIndex === index 
                  ? 'border-blue-600 ring-2 ring-blue-200' 
                  : 'border-gray-300 hover:border-gray-400'
                }
              `}
            >
              <img
                src={image}
                alt={image.alt || `${productName} - view ${index + 1}`}
                className="w-full h-full object-cover"
                loading="lazy"
              />
            </button>
          ))}
        </div>
      )}

      {/* Zoom Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={handleCloseModal}
        title="Product Image"
      >
        <div className="max-w-4xl max-h-[80vh] overflow-auto">
          <img
            src={currentImage}
            alt={currentImage.alt || productName}
            className="w-full h-auto"
          />
        </div>
      </Modal>
    </div>
  );
};

export default ProductImage;
