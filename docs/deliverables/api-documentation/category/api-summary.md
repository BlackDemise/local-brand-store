# Category API Documentation

Base URL: `/api/v1/category`

## Overview

The Category API provides read-only access to product categories. No authentication is required.

## Endpoints

### 1. Get All Categories

**Method Signature:** `GET /api/v1/category`

**Description:** Retrieves a list of all available product categories.

**Request Payload:** None

**Response:**

```json
{
  "timestamp": 1705593600000,
  "statusCode": 200,
  "message": "Categories retrieved successfully",
  "result": [
    {
      "id": 1,
      "name": "Áo Thun",
      "slug": "ao-thun"
    },
    {
      "id": 2,
      "name": "Quần Jean",
      "slug": "quan-jean"
    },
    {
      "id": 3,
      "name": "Giày Dép",
      "slug": "giay-dep"
    },
    {
      "id": 4,
      "name": "Phụ Kiện",
      "slug": "phu-kien"
    },
    {
      "id": 5,
      "name": "Túi Xách",
      "slug": "tui-xach"
    }
  ]
}
```

---

## Category Response Fields

- `id`: Unique category identifier
- `name`: Display name of the category
- `slug`: URL-friendly category identifier

## Usage

Categories are typically used for:

- Filtering products by category
- Navigation menus
- Product categorization display

You can use the `id` field to filter products via the Product API:

```
GET /api/v1/product?categoryId=1
```