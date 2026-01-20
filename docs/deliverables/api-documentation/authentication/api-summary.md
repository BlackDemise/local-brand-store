# Authentication API Documentation

Base URL: `/api/v1/auth`

## Endpoints

### 1. Register User

**Method Signature:** `POST /api/v1/auth/register`

**Description:** Initiates user registration by creating a new account and sending an OTP code to the user's email for
verification.

**Request Payload:**

```json
{
   "email": "bruhitispie@gmail.com",
   "fullName": "PiePie",
   "password": "Whydoyouask?"
}
```

**Field Validations:**

- `email`: Required, must be a valid email format
- `fullName`: Required, 2-100 characters
- `password`: Required, 6-100 characters

**Response:**

```json
{
   "timestamp": 1705593600000,
   "statusCode": 200,
   "message": "Registration initiated. Please verify OTP.",
   "result": {
      "email": "bruhitispie@gmail.com",
      "message": "OTP sent to email",
      "otpExpirySeconds": 300
   }
}
```

---

### 2. Verify OTP

**Method Signature:** `POST /api/v1/auth/verify-otp`

**Description:** Verifies the OTP code sent during registration to complete the account activation. Returns access token
and sets refresh token as HTTP-only cookie.

**Request Payload:**

```json
{
   "email": "bruhitispie@gmail.com",
   "otpCode": "123456"
}
```

**Field Validations:**

- `email`: Required, must be a valid email format
- `otpCode`: Required, must be exactly 6 digits

**Response:**

```json
{
   "timestamp": 1705593600000,
   "statusCode": 200,
   "message": "Registration completed successfully",
   "result": {
      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
   }
}
```

**Response Cookies:**

- `refreshToken`: HTTP-only cookie containing refresh token

---

### 3. Login

**Method Signature:** `POST /api/v1/auth/login`

**Description:** Authenticates user with email and password. Returns access token and sets refresh token as HTTP-only
cookie.

**Request Payload:**

```json
{
   "email": "nguyenvanan@gmail.com",
   "password": "12345Ab!"
}
```

**Example - Admin Login:**

```json
{
   "email": "auquangkhanh@gmail.com",
   "password": "12345Ab!"
}
```

**Field Validations:**

- `email`: Required, must be a valid email format
- `password`: Required, 6-100 characters

**Response:**

```json
{
   "timestamp": 1705593600000,
   "statusCode": 200,
   "message": "OK",
   "result": {
      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
   }
}
```

**Response Cookies:**

- `refreshToken`: HTTP-only cookie containing refresh token

---

### 4. Logout

**Method Signature:** `POST /api/v1/auth/logout`

**Description:** Invalidates the user's current authentication tokens and clears the refresh token cookie.

**Request Headers:**

- `Authorization`: Optional, Bearer token (e.g., `Bearer eyJhbGc...`)

**Request Cookies:**

- `refreshToken`: Optional, refresh token from cookie

**Request Payload:** None

**Response:**

```json
{
   "timestamp": 1705593600000,
   "statusCode": 200,
   "message": "OK"
}
```

**Response Cookies:**

- `refreshToken`: Cleared (Max-Age=0)

---

### 5. Introspect Token

**Method Signature:** `POST /api/v1/auth/introspect`

**Description:** Validates whether the provided access token is valid and not expired.

**Request Headers:**

- `Authorization`: Optional, Bearer token (e.g., `Bearer eyJhbGc...`)

**Request Payload:** None

**Response:**

```json
{
   "timestamp": 1705593600000,
   "statusCode": 200,
   "message": "OK",
   "result": true
}
```

**Result Values:**

- `true`: Token is valid
- `false`: Token is invalid or expired

---

### 6. Refresh Token

**Method Signature:** `POST /api/v1/auth/refresh`

**Description:** Generates a new access token using the refresh token from the HTTP-only cookie.

**Request Cookies:**

- `refreshToken`: Required, refresh token from cookie

**Request Payload:** None

**Response:**

```json
{
   "timestamp": 1705593600000,
   "statusCode": 200,
   "message": "OK",
   "result": {
      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
   }
}
```

**Response Cookies:**

- `refreshToken`: Updated HTTP-only cookie containing new refresh token

---

## Authentication Flow

1. **Registration:**
   - User calls `POST /register` with email, fullName, and password
   - System sends OTP to email
   - User calls `POST /verify-otp` with email and OTP code
   - System returns access token and sets refresh token cookie

2. **Login:**
   - User calls `POST /login` with email and password
   - System returns access token and sets refresh token cookie

3. **Token Usage:**
   - Include access token in `Authorization: Bearer {token}` header for protected endpoints
   - Refresh token is automatically sent via HTTP-only cookie

4. **Token Refresh:**
   - When access token expires, call `POST /refresh`
   - System returns new access token using refresh token from cookie

5. **Logout:**
   - Call `POST /logout` to invalidate tokens and clear cookies

## Error Responses

All endpoints may return error responses with the following structure:

```json
{
   "timestamp": 1705593600000,
   "statusCode": 400,
   "message": "Error description",
   "errors": {
      "fieldName": "Field-specific error message"
   }
}
```