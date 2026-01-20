import axios from 'axios';

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // Enable sending cookies with requests
});

// Request interceptor - Add auth token if present
client.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - Handle errors globally
client.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    const originalRequest = error.config;
    
    if (error.response) {
      // Handle 401 - Unauthorized (try to refresh token)
      // Skip refresh logic for the refresh endpoint itself to prevent infinite loops
      if (
        error.response.status === 401 && 
        !originalRequest._retry &&
        !originalRequest.url?.includes('/auth/refresh') &&
        !originalRequest.url?.includes('/auth/login')
      ) {
        originalRequest._retry = true;
        
        try {
          // Try to refresh the token using axios directly to bypass interceptors
          const refreshResponse = await axios.post(
            `${client.defaults.baseURL}/auth/refresh`,
            {},
            {
              timeout: 15000,
              headers: {
                'Content-Type': 'application/json',
              },
              withCredentials: true, // Important: include cookies for refresh token
            }
          );
          
          const newAccessToken = refreshResponse.data.result.accessToken;
          
          if (newAccessToken) {
            // Update stored token
            localStorage.setItem('accessToken', newAccessToken);
            
            // Update authorization header for the original request
            originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
            
            // Retry the original request with new token
            return client(originalRequest);
          }
        } catch (refreshError) {
          // Refresh failed, clear token and redirect to login
          localStorage.removeItem('accessToken');
          
          // Only redirect if not already on login page or register page
          if (!window.location.pathname.includes('/login') && 
              !window.location.pathname.includes('/register')) {
            window.location.href = '/login';
          }
          
          return Promise.reject(refreshError);
        }
      }
      
      // Extract error message from backend response
      const message = error.response.data?.message || 'An error occurred';
      return Promise.reject(new Error(message));
    } else if (error.request) {
      return Promise.reject(new Error('No response from server'));
    } else {
      return Promise.reject(error);
    }
  }
);

export default client;
