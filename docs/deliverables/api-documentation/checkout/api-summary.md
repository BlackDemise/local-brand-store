# Checkout API Documentation

Base URL: `/api/v1/checkout`

## Overview

The Checkout API manages the stock reservation process before order placement. When a customer initiates checkout, the
system reserves the requested stock quantities for 15 minutes, preventing other customers from purchasing the same
items. No authentication is required.

## Endpoints

### 1. Start Checkout

**Method Signature:** `POST /api/v1/checkout/start`

**Description:** Initiates the checkout process by reserving stock for selected cart items. Stock is reserved for 15
minutes (900 seconds). Any previous reservations for the same cart are automatically cancelled and replaced with the new
ones.

**Request Payload:**

```json
{
  "cartToken": "46aa1b08-4a96-452c-80f5-2828db7d1d48",
  "items": [
    {
      "cartItemId": 1,
      "quantity": 2
    },
    {
      "cartItemId": 3,
      "quantity": 1
    }
  ]
}
```

**Field Validations:**

- `cartToken`: Required, must be a valid cart token
- `items`: Required, must contain at least one item
    - `cartItemId`: Required, must be a valid cart item ID
    - `quantity`: Required, minimum value of 1

**Response:**

```json
{
  "timestamp": 1705593600000,
  "statusCode": 200,
  "message": "Checkout started successfully. Stock reserved for 15 minutes.",
  "result": {
    "cartId": 1,
    "reservations": [
      {
        "reservationId": 101,
        "skuId": 1,
        "skuCode": "AT-BASIC-TRANG-M",
        "quantity": 2,
        "status": "RESERVED",
        "expiresAt": "2026-01-18T10:15:00Z"
      },
      {
        "reservationId": 102,
        "skuId": 24,
        "skuCode": "QJ-SKINNY-XANH-30",
        "quantity": 1,
        "status": "RESERVED",
        "expiresAt": "2026-01-18T10:15:00Z"
      }
    ],
    "expiresAt": "2026-01-18T10:15:00Z",
    "expirationSeconds": 900,
    "totalAmount": 550000
  }
}
```

---

## Response Fields

### CheckoutSessionResponse

- `cartId`: The cart ID associated with this checkout session
- `reservations`: List of stock reservations created
- `expiresAt`: ISO 8601 timestamp when all reservations will expire
- `expirationSeconds`: Number of seconds until expiration (always 900 = 15 minutes)
- `totalAmount`: Total amount for all reserved items

### ReservationResponse

- `reservationId`: Unique identifier for this stock reservation
- `skuId`: Product SKU identifier
- `skuCode`: Human-readable SKU code
- `quantity`: Number of units reserved
- `status`: Reservation status (typically "RESERVED")
- `expiresAt`: ISO 8601 timestamp when this reservation expires

## Reservation Status Values

- `RESERVED`: Stock is successfully reserved
- `EXPIRED`: Reservation time has elapsed (handled by background job)
- `CANCELLED`: Reservation was cancelled (when starting new checkout)
- `COMPLETED`: Stock was converted to an order

## Checkout Scenarios

### Full Cart Checkout

Checkout all items currently in the cart:

```json
{
  "cartToken": "46aa1b08-4a96-452c-80f5-2828db7d1d48",
  "items": [
    {
      "cartItemId": 1,
      "quantity": 2
    },
    {
      "cartItemId": 2,
      "quantity": 1
    },
    {
      "cartItemId": 3,
      "quantity": 1
    }
  ]
}
```

### Partial Cart Checkout

Checkout only selected items from cart (others remain in cart):

```json
{
  "cartToken": "46aa1b08-4a96-452c-80f5-2828db7d1d48",
  "items": [
    {
      "cartItemId": 1,
      "quantity": 1
    }
  ]
}
```

### Checkout Less Than Cart Quantity

If cart has 5 units but customer only wants to buy 2:

```json
{
  "cartToken": "46aa1b08-4a96-452c-80f5-2828db7d1d48",
  "items": [
    {
      "cartItemId": 1,
      "quantity": 2
    }
  ]
}
```

Remaining 3 units stay in cart.

## Reservation Lifecycle

1. **Start Checkout**: `POST /checkout/start` creates reservations
2. **15-Minute Window**: Stock is held and unavailable to others
3. **Options**:
    - **Complete Order**: Call `POST /order` with reservation details (converts to order)
    - **Start New Checkout**: Call `POST /checkout/start` again (cancels old, creates new)
    - **Expire**: Wait 15 minutes (background job releases stock automatically)

## Integration with Order API

After successfully starting checkout, use the cart token to place an order:

```http
POST /api/v1/order
{
  "cartToken": "46aa1b08-4a96-452c-80f5-2828db7d1d48",
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "customerPhone": "0901234567",
  "shippingAddress": "123 Main St, District 1, Ho Chi Minh City"
}
```