# Cart API Documentation

Base URL: `/api/v1/cart`

## Overview

The Cart API provides anonymous shopping cart functionality using a cart token for session management. No authentication
is required to use cart operations.

## Endpoints

### 1. Add Item to Cart

**Method Signature:** `POST /api/v1/cart/items`

**Description:** Adds a product SKU to the shopping cart. If the cart token is not provided or is empty, a new cart will
be created and a cart token will be returned. If the same SKU already exists in the cart, the quantities will be summed.

**Request Payload:**

```json
{
  "cartToken": "",
  "skuId": 1,
  "quantity": 2
}
```

**Field Validations:**

- `cartToken`: Optional (empty string or omitted creates new cart)
- `skuId`: Required, must be a valid SKU ID
- `quantity`: Required, minimum value of 1

**Response:**

```json
{
  "timestamp": 1705593600000,
  "statusCode": 200,
  "message": "Item added to cart successfully",
  "result": {
    "cartId": 1,
    "cartToken": "46aa1b08-4a96-452c-80f5-2828db7d1d48",
    "items": [
      {
        "id": 1,
        "skuId": 1,
        "skuCode": "AT-BASIC-TRANG-M",
        "productName": "Áo Thun Basic Trắng",
        "size": "M",
        "color": "Trắng",
        "unitPrice": 150000,
        "quantity": 2,
        "availableStock": 50,
        "sufficient": true,
        "itemTotal": 300000
      }
    ],
    "subtotal": 300000,
    "warnings": []
  }
}
```

---

### 2. Get Cart

**Method Signature:** `GET /api/v1/cart?cartToken={cartToken}`

**Description:** Retrieves the current cart with all items, validates stock availability, and includes warnings for
items with insufficient stock.

**Query Parameters:**

- `cartToken`: Required, the cart session token

**Request Payload:** None

**Response:**

```json
{
  "timestamp": 1705593600000,
  "statusCode": 200,
  "message": "Cart retrieved successfully",
  "result": {
    "cartId": 1,
    "cartToken": "46aa1b08-4a96-452c-80f5-2828db7d1d48",
    "items": [
      {
        "id": 1,
        "skuId": 1,
        "skuCode": "AT-BASIC-TRANG-M",
        "productName": "Áo Thun Basic Trắng",
        "size": "M",
        "color": "Trắng",
        "unitPrice": 150000,
        "quantity": 2,
        "availableStock": 50,
        "sufficient": true,
        "itemTotal": 300000
      },
      {
        "id": 2,
        "skuId": 7,
        "skuCode": "AT-POLO-XANH-L",
        "productName": "Áo Polo",
        "size": "L",
        "color": "Xanh",
        "unitPrice": 250000,
        "quantity": 5,
        "availableStock": 3,
        "sufficient": false,
        "itemTotal": 1250000
      }
    ],
    "subtotal": 1550000,
    "warnings": [
      {
        "skuId": 6,
        "message": "Insufficient stock for AO-POLO-XANH-L",
        "requestedQty": 5,
        "availableQty": 3
      }
    ]
  }
}
```

---

### 3. Update Cart Item

**Method Signature:** `PUT /api/v1/cart/items/{cartItemId}?cartToken={cartToken}`

**Description:** Updates the quantity of a specific cart item. Setting quantity to 0 will remove the item from the cart.

**Path Parameters:**

- `cartItemId`: Required, the cart item ID to update

**Query Parameters:**

- `cartToken`: Required, the cart session token

**Request Payload:**

```json
{
  "quantity": 3
}
```

**Field Validations:**

- `quantity`: Required, minimum value of 0 (0 = remove item)

**Response:**

```json
{
  "timestamp": 1705593600000,
  "statusCode": 200,
  "message": "Cart item updated successfully",
  "result": {
    "cartId": 1,
    "cartToken": "46aa1b08-4a96-452c-80f5-2828db7d1d48",
    "items": [
      {
        "id": 1,
        "skuId": 1,
        "skuCode": "AT-BASIC-TRANG-M",
        "productName": "Áo Thun Basic Trắng",
        "size": "M",
        "color": "Trắng",
        "unitPrice": 150000,
        "quantity": 3,
        "availableStock": 50,
        "sufficient": true,
        "itemTotal": 450000
      }
    ],
    "subtotal": 450000,
    "warnings": []
  }
}
```

---

### 4. Remove Cart Item

**Method Signature:** `DELETE /api/v1/cart/items/{cartItemId}?cartToken={cartToken}`

**Description:** Removes a specific item from the cart.

**Path Parameters:**

- `cartItemId`: Required, the cart item ID to remove

**Query Parameters:**

- `cartToken`: Required, the cart session token

**Request Payload:** None

**Response:**

```json
{
  "timestamp": 1705593600000,
  "statusCode": 200,
  "message": "Cart item removed successfully",
  "result": {
    "cartId": 1,
    "cartToken": "46aa1b08-4a96-452c-80f5-2828db7d1d48",
    "items": [],
    "subtotal": 0,
    "warnings": []
  }
}
```

---

### 5. Clear Cart

**Method Signature:** `DELETE /api/v1/cart?cartToken={cartToken}`

**Description:** Removes all items from the cart, effectively clearing it.

**Query Parameters:**

- `cartToken`: Required, the cart session token

**Request Payload:** None

**Response:**

```json
{
  "timestamp": 1705593600000,
  "statusCode": 200,
  "message": "Cart cleared successfully"
}
```

---

## Cart Item Response Fields

- `id`: Cart item ID (different from SKU ID, used for updates/deletes)
- `skuId`: Product SKU identifier
- `skuCode`: Human-readable SKU code
- `productName`: Name of the product
- `size`: Product size variant
- `color`: Product color variant
- `unitPrice`: Price per unit
- `quantity`: Quantity in cart
- `availableStock`: Current stock available
- `sufficient`: Boolean indicating if requested quantity is available
- `itemTotal`: Total price for this item (unitPrice × quantity)

## Stock Warnings

The API automatically validates stock availability and includes warnings in the response when:

- Requested quantity exceeds available stock
- Stock levels have changed since items were added to cart

**Warning Structure:**

```json
{
  "skuId": 6,
  "message": "Insufficient stock for AO-POLO-XANH-L",
  "requestedQty": 5,
  "availableQty": 3
}
```

## Usage Flow

1. **Create Cart**: Call `POST /cart/items` with empty `cartToken` to create a new cart
2. **Save Cart Token**: Store the returned `cartToken` for subsequent requests
3. **Add Items**: Use the same `cartToken` to add more items
4. **View Cart**: Call `GET /cart` to see all items and check for stock warnings
5. **Update Quantities**: Use `PUT /cart/items/{id}` to change quantities
6. **Remove Items**: Use `DELETE /cart/items/{id}` to remove specific items
7. **Clear Cart**: Use `DELETE /cart` to remove all items