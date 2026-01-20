# Order API Documentation

Base URL: `/api/v1/order`

## Overview

The Order API manages the complete order lifecycle from placement to tracking and administration. It includes both
public endpoints (for customers) and admin-only endpoints (for order management).

## Authentication

- **Public Endpoints**: No authentication required
    - `POST /order` (Place order)
    - `GET /order/track/{trackingToken}` (Track order)

- **Admin Endpoints**: Require authentication with ADMIN role
    - `GET /order/{orderId}` (Get order by ID)
    - `GET /order` (List orders with filters)
    - `PUT /order/{orderId}/status` (Update order status)
    - `POST /order/{orderId}/cancel` (Cancel order)
    - `GET /order/{orderId}/history` (Get order history)

## Endpoints

### 1. Place Order (Public)

**Method Signature:** `POST /api/v1/order`

**Description:** Creates a new order from reserved cart items. This endpoint validates that stock reservations exist and
haven't expired, then converts them into an order. The reserved items are removed from the cart.

**Request Payload:**

```json
{
  "cartId": 1,
  "customerName": "Nguyễn Văn An",
  "customerPhone": "0901234567",
  "customerEmail": "nguyenvanan@gmail.com",
  "shippingAddress": "123 Lê Lợi, Quận 1, TP. Hồ Chí Minh",
  "note": "Giao hàng giờ hành chính",
  "paymentMethod": "COD"
}
```

**Example - Bank Transfer Payment:**

```json
{
  "cartId": 1,
  "customerName": "Trần Thị Bình",
  "customerPhone": "0987654321",
  "customerEmail": "tranthibinh@gmail.com",
  "shippingAddress": "456 Nguyễn Huệ, Quận 3, TP. Hồ Chí Minh",
  "note": "Gọi trước khi giao 30 phút",
  "paymentMethod": "BANK_TRANSFER"
}
```

**Field Validations:**

- `cartId`: Required, must be a valid cart ID with active reservations
- `customerName`: Required
- `customerPhone`: Required
- `customerEmail`: Required, valid email format
- `shippingAddress`: Required
- `note`: Optional
- `paymentMethod`: Required, valid values: `COD`, `BANK_TRANSFER`

**Response:**

```json
{
  "timestamp": 1705593600000,
  "statusCode": 201,
  "message": "Order placed successfully",
  "result": {
    "orderId": 1001,
    "trackingToken": "TRK-1705593600-ABC123",
    "status": "PENDING_PAYMENT",
    "paymentMethod": "COD",
    "totalAmount": 1250000,
    "customerName": "Nguyễn Văn An",
    "customerPhone": "0901234567",
    "customerEmail": "nguyenvanan@gmail.com",
    "shippingAddress": "123 Lê Lợi, Quận 1, TP. Hồ Chí Minh",
    "note": "Giao hàng giờ hành chính",
    "items": [
      {
        "id": 1,
        "skuId": 1,
        "skuCode": "AT-BASIC-TRANG-M",
        "productName": "Áo Thun Basic Trắng",
        "size": "M",
        "color": "Trắng",
        "quantity": 2,
        "unitPrice": 150000,
        "itemTotal": 300000
      },
      {
        "id": 2,
        "skuId": 24,
        "skuCode": "QJ-SKINNY-XANH-30",
        "productName": "Quần Jean Skinny Xanh",
        "size": "30",
        "color": "Xanh Đậm",
        "quantity": 1,
        "unitPrice": 450000,
        "itemTotal": 450000
      }
    ],
    "createdAt": "2026-01-18T10:00:00Z"
  }
}
```

---

### 2. Track Order (Public)

**Method Signature:** `GET /api/v1/order/track/{trackingToken}`

**Description:** Allows customers to track their order status using the tracking token received when placing the order.

**Path Parameters:**

- `trackingToken`: Required, the unique tracking token

**Request Payload:** None

**Response:**

```json
{
  "timestamp": 1705593600000,
  "statusCode": 200,
  "message": "Order retrieved successfully",
  "result": {
    "orderId": 1001,
    "trackingToken": "TRK-1705593600-ABC123",
    "status": "SHIPPING",
    "paymentMethod": "COD",
    "totalAmount": 1250000,
    "customerName": "Nguyễn Văn An",
    "customerPhone": "0901234567",
    "customerEmail": "nguyenvanan@gmail.com",
    "shippingAddress": "123 Lê Lợi, Quận 1, TP. Hồ Chí Minh",
    "note": "Giao hàng giờ hành chính",
    "items": [
      {
        "id": 1,
        "skuId": 1,
        "skuCode": "AT-BASIC-TRANG-M",
        "productName": "Áo Thun Basic Trắng",
        "size": "M",
        "color": "Trắng",
        "quantity": 2,
        "unitPrice": 150000,
        "itemTotal": 300000
      }
    ],
    "createdAt": "2026-01-18T10:00:00Z"
  }
}
```

---

### 3. Get Order by ID (Admin)

**Method Signature:** `GET /api/v1/order/{orderId}`

**Description:** Retrieves complete order details by order ID. Admin only.

**Path Parameters:**

- `orderId`: Required, the order ID

**Request Headers:**

- `Authorization`: Required, Bearer token with ADMIN role

**Request Payload:** None

**Response:** Same structure as Track Order response

---

### 4. Get Orders with Filters (Admin)

**Method Signature:** `GET /api/v1/order`

**Description:** Retrieves a paginated list of orders with optional filters. Admin only.

**Query Parameters:**

- `status`: Optional, filter by order status (`PENDING_PAYMENT`, `CONFIRMED`, `SHIPPING`, `DELIVERED`, `CANCELLED`)
- `startDate`: Optional, ISO 8601 datetime, filter orders from this date
- `endDate`: Optional, ISO 8601 datetime, filter orders until this date
- `page`: Optional, default 0
- `size`: Optional, default 20

**Request Headers:**

- `Authorization`: Required, Bearer token with ADMIN role

**Request Payload:** None

**Response:**

```json
{
  "timestamp": 1705593600000,
  "statusCode": 200,
  "message": "Orders retrieved successfully",
  "result": {
    "content": [
      {
        "orderId": 1001,
        "trackingToken": "TRK-1705593600-ABC123",
        "status": "SHIPPING",
        "totalAmount": 1250000,
        "customerName": "Nguyễn Văn A",
        "createdAt": "2026-01-18T10:00:00Z"
      },
      {
        "orderId": 1002,
        "trackingToken": "TRK-1705594200-XYZ789",
        "status": "DELIVERED",
        "totalAmount": 850000,
        "customerName": "Trần Thị B",
        "createdAt": "2026-01-18T09:30:00Z"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "totalPages": 5,
    "totalElements": 100,
    "last": false,
    "first": true,
    "size": 20,
    "number": 0,
    "numberOfElements": 20,
    "empty": false
  }
}
```

---

### 5. Update Order Status (Admin)

**Method Signature:** `PUT /api/v1/order/{orderId}/status`

**Description:** Updates the status of an order. Creates an audit log entry tracking the change. Admin only.

**Path Parameters:**

- `orderId`: Required, the order ID

**Request Headers:**

- `Authorization`: Required, Bearer token with ADMIN role

**Request Payload:**

```json
{
  "newStatus": "SHIPPING",
  "note": "Đơn hàng đã được giao cho đơn vị vận chuyển"
}
```

**Field Validations:**

- `newStatus`: Required, valid values: `PENDING_PAYMENT`, `CONFIRMED`, `SHIPPING`, `DELIVERED`, `CANCELLED`
- `note`: Optional, additional notes about the status change

**Response:**

```json
{
  "timestamp": 1705593600000,
  "statusCode": 200,
  "message": "Order status updated successfully",
  "result": {
    "orderId": 1001,
    "trackingToken": "TRK-1705593600-ABC123",
    "status": "SHIPPING",
    "paymentMethod": "COD",
    "totalAmount": 1250000,
    "customerName": "Nguyễn Văn An",
    "customerPhone": "0901234567",
    "customerEmail": "nguyenvanan@gmail.com",
    "shippingAddress": "123 Lê Lợi, Quận 1, TP. Hồ Chí Minh",
    "note": "Giao hàng giờ hành chính",
    "items": [
      ...
    ],
    "createdAt": "2026-01-18T10:00:00Z"
  }
}
```

---

### 6. Cancel Order (Admin)

**Method Signature:** `POST /api/v1/order/{orderId}/cancel`

**Description:** Cancels an order and restores the stock quantities. Creates an audit log entry. Admin only.

**Path Parameters:**

- `orderId`: Required, the order ID

**Request Headers:**

- `Authorization`: Required, Bearer token with ADMIN role

**Request Payload:**

```json
{
  "reason": "Khách hàng yêu cầu hủy đơn"
}
```

**Field Validations:**

- `reason`: Required, reason for cancellation

**Response:**

```json
{
  "timestamp": 1705593600000,
  "statusCode": 200,
  "message": "Order cancelled successfully",
  "result": {
    "orderId": 1001,
    "trackingToken": "TRK-1705593600-ABC123",
    "status": "CANCELLED",
    "paymentMethod": "COD",
    "totalAmount": 1250000,
    "customerName": "Nguyễn Văn An",
    "customerPhone": "0901234567",
    "customerEmail": "nguyenvanan@gmail.com",
    "shippingAddress": "123 Lê Lợi, Quận 1, TP. Hồ Chí Minh",
    "note": "Giao hàng giờ hành chính",
    "items": [
      ...
    ],
    "createdAt": "2026-01-18T10:00:00Z"
  }
}
```

---

### 7. Get Order History (Admin)

**Method Signature:** `GET /api/v1/order/{orderId}/history`

**Description:** Retrieves the complete history of status changes for an order. Admin only.

**Path Parameters:**

- `orderId`: Required, the order ID

**Request Headers:**

- `Authorization`: Required, Bearer token with ADMIN role

**Request Payload:** None

**Response:**

```json
{
  "timestamp": 1705593600000,
  "statusCode": 200,
  "message": "Order history retrieved successfully",
  "result": [
    {
      "id": 1,
      "oldStatus": null,
      "newStatus": "PENDING_PAYMENT",
      "note": "Order created",
      "changedAt": "2026-01-18T10:00:00Z"
    },
    {
      "id": 2,
      "oldStatus": "PENDING_PAYMENT",
      "newStatus": "CONFIRMED",
      "note": "Payment confirmed by admin@example.com",
      "changedAt": "2026-01-18T10:30:00Z"
    },
    {
      "id": 3,
      "oldStatus": "CONFIRMED",
      "newStatus": "SHIPPING",
      "note": "Đơn hàng đã được giao cho đơn vị vận chuyển",
      "changedAt": "2026-01-18T11:00:00Z"
    }
  ]
}
```

---

## Complete Order Flow

### Customer Flow:

1. **Browse Products**: `GET /api/v1/product`
2. **Add to Cart**: `POST /api/v1/cart/items`
3. **View Cart**: `GET /api/v1/cart`
4. **Start Checkout**: `POST /api/v1/checkout/start` (reserves stock)
5. **Place Order**: `POST /api/v1/order` (creates order from reservations)
6. **Track Order**: `GET /api/v1/order/track/{trackingToken}`

### Admin Flow:

1. **View Orders**: `GET /api/v1/order?status=PENDING_PAYMENT`
2. **View Order Details**: `GET /api/v1/order/{orderId}`
3. **Confirm Payment**: `PUT /api/v1/order/{orderId}/status` → `CONFIRMED`
4. **Mark as Shipping**: `PUT /api/v1/order/{orderId}/status` → `SHIPPING`
5. **Mark as Delivered**: `PUT /api/v1/order/{orderId}/status` → `DELIVERED`
6. **Or Cancel**: `POST /api/v1/order/{orderId}/cancel` (if needed)
7. **View History**: `GET /api/v1/order/{orderId}/history`

---