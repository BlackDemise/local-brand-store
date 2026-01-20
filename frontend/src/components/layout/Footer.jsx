import React from 'react';
import { Link } from 'react-router-dom';

/**
 * Site footer
 */
const Footer = () => {
  const currentYear = new Date().getFullYear();
  
  return (
    <footer className="bg-gray-800 text-white mt-auto">
      <div className="container mx-auto px-4 py-8">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {/* About */}
          <div>
            <h3 className="text-lg font-semibold mb-4">Hung Hypebeast</h3>
            <p className="text-gray-300 text-sm">
              Cửa hàng thời trang streetwear và hypebeast chính hãng tại Việt Nam.
            </p>
          </div>
          
          {/* Links */}
          <div>
            <h3 className="text-lg font-semibold mb-4">Liên kết</h3>
            <ul className="space-y-2 text-sm">
              <li>
                <Link to="/" className="text-gray-300 hover:text-white transition-colors">
                  Trang chủ
                </Link>
              </li>
              <li>
                <Link to="/products" className="text-gray-300 hover:text-white transition-colors">
                  Sản phẩm
                </Link>
              </li>
              <li>
                <Link to="/track-order" className="text-gray-300 hover:text-white transition-colors">
                  Tra cứu đơn hàng
                </Link>
              </li>
              <li>
                <Link to="/cart" className="text-gray-300 hover:text-white transition-colors">
                  Giỏ hàng
                </Link>
              </li>
            </ul>
          </div>
          
          {/* Contact */}
          <div>
            <h3 className="text-lg font-semibold mb-4">Liên hệ</h3>
            <ul className="space-y-2 text-sm text-gray-300">
              <li>Email: contact@hunghypebeast.com</li>
              <li>Hotline: 1900 xxxx</li>
              <li>Địa chỉ: TP. Hồ Chí Minh, Việt Nam</li>
            </ul>
          </div>
        </div>
        
        <div className="border-t border-gray-700 mt-8 pt-8 text-center text-sm text-gray-400">
          <p>&copy; {currentYear} Hung Hypebeast. All rights reserved.</p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
