import React from 'react';
import { useNavigate } from 'react-router-dom';
import Container from '../../components/layout/Container';
import Button from '../../components/common/Button';

/**
 * NotFoundPage - 404 error page
 */
const NotFoundPage = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gray-50 flex items-center">
      <Container>
        <div className="text-center py-20">
          <div className="mb-8">
            <svg
              className="mx-auto h-48 w-48 text-gray-400"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={1}
                d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
          </div>

          <h1 className="text-6xl font-bold text-gray-900 mb-4">404</h1>
          <h2 className="text-2xl font-semibold text-gray-800 mb-4">
            Page Not Found
          </h2>
          <p className="text-lg text-gray-600 mb-8">
            Sorry, we couldn't find the page you're looking for.
          </p>

          <div className="flex justify-center gap-4">
            <Button
              onClick={() => navigate('/')}
              variant="primary"
              className="px-6 py-3"
            >
              Go to Home
            </Button>
            <Button
              onClick={() => navigate(-1)}
              variant="outline"
              className="px-6 py-3"
            >
              Go Back
            </Button>
          </div>
        </div>
      </Container>
    </div>
  );
};

export default NotFoundPage;
