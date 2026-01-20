# Product API Documentation

Base URL: `/api/v1/product`

## Overview

The Product API provides read-only access to the product catalog with filtering, sorting, and pagination capabilities.
No authentication is required.

## Endpoints

### 1. Get Products with Filters

**Method Signature:** `GET /api/v1/product`

**Description:** Retrieves a paginated list of products with optional filtering by category, price range, and sorting
options.

**Query Parameters:**

- `categoryId`: Optional, filter by category ID
- `minPrice`: Optional, minimum price filter (inclusive)
- `maxPrice`: Optional, maximum price filter (inclusive)
- `page`: Optional, page number (0-indexed), default 0
- `size`: Optional, page size, default 20
- `sortBy`: Optional, field to sort by, default `createdAt`
    - Valid values: `createdAt`, `name`, `basePrice`
- `sortDirection`: Optional, sort direction, default `DESC`
    - Valid values: `ASC`, `DESC`

**Request Payload:** None

**Response:**

```json
{
  "timestamp": 1705593600000,
  "statusCode": 200,
  "message": "Products retrieved successfully",
  "result": {
    "content": [
      {
        "id": 1,
        "name": "Áo Thun Basic Trắng",
        "slug": "ao-thun-basic-trang",
        "basePrice": 150000,
        "primaryImageUrl": "https://picsum.photos/800/800?random=1",
        "category": {
          "id": 1,
          "name": "Áo Thun",
          "slug": "ao-thun"
        },
        "minStock": 30
      },
      {
        "id": 2,
        "name": "Áo Thun Polo Nam",
        "slug": "ao-thun-polo-nam",
        "basePrice": 250000,
        "primaryImageUrl": "https://picsum.photos/800/800?random=5",
        "category": {
          "id": 1,
          "name": "Áo Thun",
          "slug": "ao-thun"
        },
        "minStock": 25
      }
    ],
    "currentPage": 0,
    "totalPages": 5,
    "totalElements": 100,
    "pageSize": 20,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

**Example Requests:**

1. **Get all products (default pagination):**
   ```
   GET /api/v1/product
   ```

2. **Filter by category (Áo Thun):**
   ```
   GET /api/v1/product?categoryId=1
   ```

3. **Filter by category (Giày Dép):**
   ```
   GET /api/v1/product?categoryId=3
   ```

4. **Filter by price range (Under 200k):**
   ```
   GET /api/v1/product?maxPrice=200000
   ```

5. **Filter by price range (400k-600k):**
   ```
   GET /api/v1/product?minPrice=400000&maxPrice=600000
   ```

6. **Multiple filters with sorting (Quần Jean 400k-500k, sorted by price):**
   ```
   GET /api/v1/product?categoryId=2&minPrice=400000&maxPrice=500000&sortBy=basePrice&sortDirection=ASC
   ```

7. **Custom pagination:**
   ```
   GET /api/v1/product?page=2&size=10
   ```

---

### 2. Get Product by ID

**Method Signature:** `GET /api/v1/product/{id}`

**Description:** Retrieves detailed information about a specific product, including all available SKUs (size/color
variants) with stock information.

**Path Parameters:**

- `id`: Required, the product ID

**Request Payload:** None

**Response:**

```json
{
  "timestamp": 1705593600000,
  "statusCode": 200,
  "message": "Product detail retrieved successfully",
  "result": {
    "id": 1,
    "name": "Áo Thun Basic Trắng",
    "slug": "ao-thun-basic-trang",
    "description": "Áo thun cotton 100% thoáng mát, form regular phù hợp mọi lứa tuổi",
    "basePrice": 150000,
    "imageUrls": [
      "https://picsum.photos/800/800?random=1",
      "https://picsum.photos/800/800?random=2",
      "https://picsum.photos/800/800?random=3",
      "https://picsum.photos/800/800?random=4"
    ],
    "category": {
      "id": 1,
      "name": "Áo Thun",
      "slug": "ao-thun"
    },
    "skus": [
      {
        "id": 1,
        "skuCode": "AT-BASIC-TRANG-M",
        "size": "M",
        "color": "Trắng",
        "price": 150000,
        "stockQty": 50,
        "available": true
      },
      {
        "id": 2,
        "skuCode": "AT-BASIC-TRANG-L",
        "size": "L",
        "color": "Trắng",
        "price": 150000,
        "stockQty": 45,
        "available": true
      },
      {
        "id": 3,
        "skuCode": "AT-BASIC-TRANG-XL",
        "size": "XL",
        "color": "Trắng",
        "price": 150000,
        "stockQty": 40,
        "available": true
      },
      {
        "id": 4,
        "skuCode": "AT-BASIC-TRANG-XXL",
        "size": "XXL",
        "color": "Trắng",
        "price": 150000,
        "stockQty": 30,
        "available": true
      }
    ]
  }
}
```

---

### 3. Get Product by Slug

**Method Signature:** `GET /api/v1/product/slug/{slug}`

**Description:** Retrieves detailed information about a specific product using its URL-friendly slug identifier. Returns
the same detailed information as "Get Product by ID".

**Path Parameters:**

- `slug`: Required, the product slug (URL-friendly identifier)

**Request Payload:** None

**Response:** Same structure as "Get Product by ID" response

**Example:**

```
GET /api/v1/product/slug/ao-thun-basic-trang
GET /api/v1/product/slug/giay-sneaker-trang
GET /api/v1/product/slug/quan-jean-skinny-xanh
```

---

## Response Fields

### ProductListResponse (for list endpoint)

- `id`: Unique product identifier
- `name`: Product name
- `slug`: URL-friendly identifier
- `basePrice`: Starting price for this product
- `primaryImageUrl`: Main product image URL
- `category`: Category information (id, name, slug)
- `minStock`: Minimum stock quantity across all SKUs

### ProductDetailResponse (for detail endpoints)

- `id`: Unique product identifier
- `name`: Product name
- `slug`: URL-friendly identifier
- `description`: Detailed product description
- `basePrice`: Starting price for this product
- `imageUrls`: Array of product image URLs
- `category`: Category information (id, name, slug)
- `skus`: Array of available SKU variants

### SkuResponse (product variants)

- `id`: Unique SKU identifier (used for cart operations)
- `skuCode`: Human-readable SKU code
- `size`: Size variant (e.g., "M", "L", "XL", "30", "40")
- `color`: Color variant (e.g., "Trắng", "Đen", "Xanh")
- `price`: Price for this specific variant
- `stockQty`: Available stock quantity
- `available`: Boolean indicating if this SKU is in stock

### PageResponse (pagination wrapper)

- `content`: Array of products for current page
- `currentPage`: Current page number (0-indexed)
- `totalPages`: Total number of pages
- `totalElements`: Total number of products matching filters
- `pageSize`: Number of items per page
- `hasNext`: Boolean indicating if there's a next page
- `hasPrevious`: Boolean indicating if there's a previous page

---

## Usage Patterns

### Browse All Products

```http
GET /api/v1/product
```

### Browse by Category

```http
GET /api/v1/product?categoryId=1
```

### Filter by Price Range

```http
GET /api/v1/product?minPrice=100000&maxPrice=500000
```

### Sort by Price (Low to High)

```http
GET /api/v1/product?sortBy=basePrice&sortDirection=ASC
```

### Sort by Name (A to Z)

```http
GET /api/v1/product?sortBy=name&sortDirection=ASC
```

### Complex Filter Example

```http
GET /api/v1/product?categoryId=2&minPrice=200000&maxPrice=800000&page=0&size=10&sortBy=basePrice&sortDirection=ASC
```

### View Product Details

```http
GET /api/v1/product/1
```

### View Product by Slug (SEO-friendly)

```http
GET /api/v1/product/slug/ao-thun-basic-trang
GET /api/v1/product/slug/giay-sneaker-trang
```

---