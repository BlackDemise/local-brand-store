import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import Input from '../../components/common/Input';
import Button from '../../components/common/Button';
import Card from '../../components/common/Card';
import Modal from '../../components/common/Modal';

/**
 * Register Page Component with OTP Verification
 */
const RegisterPage = () => {
  const navigate = useNavigate();
  const { register, verifyOtp } = useAuth();
  
  const [formData, setFormData] = useState({
    email: '',
    fullName: '',
    password: '',
    confirmPassword: '',
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  
  // OTP Modal State
  const [showOtpModal, setShowOtpModal] = useState(false);
  const [otpCode, setOtpCode] = useState('');
  const [otpError, setOtpError] = useState('');
  const [otpLoading, setOtpLoading] = useState(false);
  const [registeredEmail, setRegisteredEmail] = useState('');
  const [otpExpirySeconds, setOtpExpirySeconds] = useState(0);
  
  /**
   * Handle input change
   */
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    // Clear error when user starts typing
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };
  
  /**
   * Validate form
   */
  const validate = () => {
    const newErrors = {};
    
    if (!formData.email) {
      newErrors.email = 'Email là bắt buộc';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Email không hợp lệ';
    }
    
    if (!formData.fullName) {
      newErrors.fullName = 'Họ tên là bắt buộc';
    } else if (formData.fullName.length < 2) {
      newErrors.fullName = 'Họ tên phải có ít nhất 2 ký tự';
    }
    
    if (!formData.password) {
      newErrors.password = 'Mật khẩu là bắt buộc';
    } else if (formData.password.length < 6) {
      newErrors.password = 'Mật khẩu phải có ít nhất 6 ký tự';
    }
    
    if (!formData.confirmPassword) {
      newErrors.confirmPassword = 'Xác nhận mật khẩu là bắt buộc';
    } else if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Mật khẩu không khớp';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };
  
  /**
   * Handle form submission
   */
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validate()) {
      return;
    }
    
    setLoading(true);
    
    try {
      const response = await register({
        email: formData.email,
        fullName: formData.fullName,
        password: formData.password,
      });
      
      // Show OTP modal
      setRegisteredEmail(response.email);
      setOtpExpirySeconds(response.otpExpirySeconds || 300); // Default 5 minutes
      setShowOtpModal(true);
      setLoading(false);
    } catch (error) {
      // Error is handled by AuthContext
      setLoading(false);
    }
  };
  
  /**
   * Handle OTP verification
   */
  const handleVerifyOtp = async () => {
    // Validate OTP
    if (!otpCode) {
      setOtpError('Vui lòng nhập mã OTP');
      return;
    }
    
    if (!/^\d{6}$/.test(otpCode)) {
      setOtpError('Mã OTP phải là 6 chữ số');
      return;
    }
    
    setOtpLoading(true);
    setOtpError('');
    
    try {
      await verifyOtp(registeredEmail, otpCode);
      setShowOtpModal(false);
      navigate('/');
    } catch (error) {
      setOtpError(error.message || 'Mã OTP không hợp lệ');
      setOtpLoading(false);
    }
  };
  
  /**
   * Handle resend OTP
   */
  const handleResendOtp = async () => {
    setOtpLoading(true);
    setOtpError('');
    
    try {
      const response = await register({
        email: formData.email,
        fullName: formData.fullName,
        password: formData.password,
      });
      
      setOtpExpirySeconds(response.otpExpirySeconds || 300);
      setOtpCode('');
      setOtpLoading(false);
    } catch (error) {
      setOtpError(error.message || 'Không thể gửi lại mã OTP');
      setOtpLoading(false);
    }
  };
  
  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full">
        <div className="text-center mb-8">
          <h2 className="text-3xl font-bold text-gray-900">Đăng ký tài khoản</h2>
          <p className="mt-2 text-sm text-gray-600">
            Đã có tài khoản?{' '}
            <Link to="/login" className="font-medium text-blue-600 hover:text-blue-500">
              Đăng nhập
            </Link>
          </p>
        </div>
        
        <Card className="p-8">
          <form onSubmit={handleSubmit} className="space-y-6">
            <Input
              label="Email"
              name="email"
              type="email"
              value={formData.email}
              onChange={handleChange}
              error={errors.email}
              placeholder="example@gmail.com"
              required
              disabled={loading}
            />
            
            <Input
              label="Họ tên"
              name="fullName"
              type="text"
              value={formData.fullName}
              onChange={handleChange}
              error={errors.fullName}
              placeholder="Nguyễn Văn A"
              required
              disabled={loading}
            />
            
            <Input
              label="Mật khẩu"
              name="password"
              type="password"
              value={formData.password}
              onChange={handleChange}
              error={errors.password}
              placeholder="Nhập mật khẩu"
              required
              disabled={loading}
            />
            
            <Input
              label="Xác nhận mật khẩu"
              name="confirmPassword"
              type="password"
              value={formData.confirmPassword}
              onChange={handleChange}
              error={errors.confirmPassword}
              placeholder="Nhập lại mật khẩu"
              required
              disabled={loading}
            />
            
            <Button
              type="submit"
              variant="primary"
              className="w-full"
              loading={loading}
              disabled={loading}
            >
              Đăng ký
            </Button>
          </form>
          
          <div className="mt-6">
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-300"></div>
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-2 bg-white text-gray-500">
                  Hoặc
                </span>
              </div>
            </div>
            
            <div className="mt-6">
              <Link to="/">
                <Button
                  type="button"
                  variant="outline"
                  className="w-full"
                >
                  Quay lại trang chủ
                </Button>
              </Link>
            </div>
          </div>
        </Card>
      </div>
      
      {/* OTP Verification Modal */}
      <Modal
        isOpen={showOtpModal}
        onClose={() => setShowOtpModal(false)}
        title="Xác thực OTP"
      >
        <div className="space-y-4">
          <p className="text-sm text-gray-600">
            Chúng tôi đã gửi mã OTP đến email <strong>{registeredEmail}</strong>.
            Vui lòng kiểm tra hộp thư và nhập mã để hoàn tất đăng ký.
          </p>
          
          <Input
            label="Mã OTP"
            name="otpCode"
            type="text"
            value={otpCode}
            onChange={(e) => {
              setOtpCode(e.target.value);
              setOtpError('');
            }}
            error={otpError}
            placeholder="Nhập 6 chữ số"
            maxLength={6}
            disabled={otpLoading}
          />
          
          <div className="text-sm text-gray-500">
            Mã OTP có hiệu lực trong {Math.floor(otpExpirySeconds / 60)} phút
          </div>
          
          <div className="flex gap-3">
            <Button
              type="button"
              variant="primary"
              className="flex-1"
              onClick={handleVerifyOtp}
              loading={otpLoading}
              disabled={otpLoading}
            >
              Xác nhận
            </Button>
            
            <Button
              type="button"
              variant="outline"
              onClick={handleResendOtp}
              disabled={otpLoading}
            >
              Gửi lại
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default RegisterPage;
