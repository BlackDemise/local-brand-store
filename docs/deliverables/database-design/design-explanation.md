# Database Design Explanation

---

## Table of Contents

1. [Table Structure & Rationale](#1-table-structure--rationale)
2. [Key Design Decisions](#2-key-design-decisions)
3. [Relationships & Constraints](#3-relationships--constraints)
4. [Indexes & Performance](#4-indexes--performance)
5. [Data Integrity](#5-data-integrity)

---

## 1. Table Structure & Rationale

### 1.1 Product Catalog Tables

#### `categories`

**Purpose**: Organize products into browsable categories (Áo, Quần, Giày dép, Phụ kiện)

**Why separate table?**

- Reusability: Multiple products share same category
- Maintainability: Change category name once, affects all products
- Filtering: Efficient category-based queries with foreign key index
- Extensibility: Can add category properties (image, description) later

**Key Fields**:

```sql
id
BIGINT PRIMARY KEY
name        VARCHAR(100)        -- "Áo", "Quần"
slug        VARCHAR(100) UNIQUE -- "ao", "quan" (URL-friendly, help SEO here)
created_at  TIMESTAMP
```

**Why `slug` field?**

- SEO-friendly URLs: `/products?category=ao` instead of `/products?category=1`
- Human-readable API parameters
- Unique constraint ensures no duplicates

---

#### `products`

**Purpose**: Store product master data (name, description, base price)

**Why separate from SKUs?**

- DRY (Don't repeat yourself): Product info (name, description) shared across all variants
- Logical Grouping: "IPhone 17" is one product with multiple SKUs (Pro, Pro Max,...)
- Presentation: Product detail page shows one product with variant options

**Key Fields**:

```sql
id
BIGINT PRIMARY KEY
category_id  BIGINT FOREIGN KEY -> categories(id)
name         VARCHAR(255)        -- "Áo Thun Basic"
slug         VARCHAR(255) UNIQUE -- "ao-thun-basic"
description  TEXT                -- Product details
base_price   DECIMAL(10,2)       -- Starting price (for display)
created_at   TIMESTAMP
```

**Why `base_price` when SKUs have prices?**

- **Display Purpose**: Show "From 150,000₫" on product list
- **Not transactional**: Actual price comes from SKU during checkout

---

#### `skus` (Stock Keeping Units)

**Purpose**: Represent product variants (size/color combinations) with individual stock tracking

**Why separate SKU table? Why not store in products?**

**Bad Design (Single Table)**:

```sql
products
(
  id, name, 
  size_m_white_stock, size_m_white_price,
  size_l_white_stock, size_l_white_price,
  size_m_black_stock, size_m_black_price,
  ... -- Looks terrible
)
```

**Problems with single table**:

- Fixed number of variants (what if add new size?)
- Sparse data (most combinations may not exist, but we still need to define all)
- Hard to query "show all available sizes"

**Good Design (SKU Table)**:

```sql
skus
(
  id, product_id, size, color, price, stock_qty, sku_code
)
```

**Benefits**:

- Flexible: Add unlimited variants dynamically
- Atomic stock updates per variant
- Easy to query available sizes/colors
- Independent pricing per variant
- Each SKU has unique barcode (`sku_code`)

**Key Fields**:

```sql
id
BIGINT PRIMARY KEY
product_id  BIGINT FOREIGN KEY -> products(id)
size        VARCHAR(10)         -- "M", "L", "XL", "30", "40"
color       VARCHAR(50)         -- "Trắng", "Đen", "Xanh"
price       DECIMAL(10,2)       -- Actual selling price
stock_qty   INT NOT NULL        -- Overselling prevention
sku_code    VARCHAR(100) UNIQUE -- "AO-THUN-BASIC-TRANG-M"
created_at  TIMESTAMP
```

**Why does `stock_qty` prevent overselling?**

- **Atomic Updates**: `UPDATE skus SET stock_qty = stock_qty - ? WHERE stock_qty >= ?`
- **Race Condition Prevention**: Database guarantees only ONE transaction succeeds
- **NOT NULL Constraint**: Must always have a value (0 = out of stock)

**Real-World Example**:

```
Product: "Áo Thun Basic" (id=1)
  ├─ SKU 1: Size M, White, Price 150k, Stock 100
  ├─ SKU 2: Size L, White, Price 150k, Stock 80
  ├─ SKU 3: Size M, Black, Price 150k, Stock 0 (out of stock)
  └─ SKU 4: Size L, Black, Price 150k, Stock 50
```

---

#### `sku_codes`

**Purpose**: Store multiple barcode/identifier codes for each SKU

**Why separate table? Why not store in SKUs?**

**Problem with single code in SKU**:

- SKU can only have ONE barcode/identifier
- Cannot support multiple barcode formats (EAN-13, UPC, internal codes)
- Cannot support legacy codes during system migrations
- Cannot support international/regional code variations

**Solution - Separate sku_codes table**:

```sql
sku_codes
(
  id, sku_id, code, is_primary
)
```

**Benefits**:

- **Multiple Barcodes**: Same SKU can have different barcodes (warehouse, POS, international)
- **Legacy Support**: Keep old codes during system migration while adding new ones
- **Format Flexibility**: Support EAN-13, UPC, internal codes, QR codes simultaneously
- **Primary Designation**: Mark one code as primary for display/default use
- **No SKU Duplication**: Stock remains at SKU level, codes just reference it

**Key Fields**:

```sql
id
BIGINT PRIMARY KEY
sku_id      BIGINT FOREIGN KEY -> skus(id) ON DELETE
CASCADE
code        VARCHAR(255) UNIQUE NOT NULL  -- "AT-BASIC-TRANG-M", "SKU-000001", "8934567890101"
is_primary  BOOLEAN NOT NULL              -- TRUE for main/display code
created_at  TIMESTAMP
```

**Why `is_primary` field?**

- **Display**: Show primary code on product pages and invoices
- **Search**: Prioritize primary code in search results
- **Integration**: Use primary code for main warehouse system
- **Constraint**: Application ensures at least one primary code per SKU

**Real-World Example**:

```
SKU: Áo Thun Basic - Size M - White (sku_id=1)
  ├─ Code 1: "AT-BASIC-TRANG-M" (PRIMARY) - Internal naming convention
  ├─ Code 2: "SKU-000001" - Sequential numbering system
  ├─ Code 3: "8934567890101" - EAN-13 barcode for POS scanning
  └─ Code 4: "HHB-TS-WHT-M" - International distribution code
```

**Use Cases**:

1. **Multi-warehouse Operations**: Each warehouse uses different barcode system
2. **System Migration**: Keep old codes active while transitioning to new format
3. **International Expansion**: Add country-specific codes without affecting existing operations
4. **Barcode Standards**: Support both EAN-13 (Vietnam) and UPC (USA) simultaneously
5. **Rebranding**: Add new codes for rebranded products while preserving legacy codes

---

#### `product_images`

**Purpose**: Store multiple images per product

**Why separate table?**

- Multiple images per product (1:N relationship)
- Ordering: `position` field defines display order
- Primary image designation: `is_primary` flag
- Extensibility: Can add alt text, captions later

**Key Fields**:

```sql
id
BIGINT PRIMARY KEY
product_id  BIGINT FOREIGN KEY -> products(id)
image_url   VARCHAR(500)        -- CDN or storage path
position    INT                 -- Display order (1, 2, 3...)
is_primary  BOOLEAN             -- Main thumbnail image
```

---

### 1.2 Shopping Cart Tables

#### `carts`

**Purpose**: Store anonymous shopping carts

**Why separate cart table?**

- Tracks cart creation time
- Can add cart-level properties (expiry, session data) later
- Clean separation: Cart -> Cart Items

**Key Fields**:

```sql
id
BIGINT PRIMARY KEY
token       UUID UNIQUE NOT NULL -- **Session identifier**
created_at  TIMESTAMP
```

**Why `token` as UUID?**

- **Anonymous Shopping**: No user login required
- **Security**: UUIDv4 is unguessable (128-bit random)
- **Stateless**: Frontend stores token in localStorage/cookie
- **Uniqueness**: UUID collision probability negligible (total combinations of 2^128)

**Workflow**:

1. Customer adds first item -> Generate new UUID -> Create cart
2. Frontend stores UUID in browser
3. Subsequent requests include UUID: `POST /cart/items?cartToken={uuid}`
4. Backend looks up cart: `SELECT * FROM carts WHERE token = ?`

---

#### `cart_items`

**Purpose**: Store items in cart with quantities

**Why separate table? Why not JSON in carts?**

**Bad Design (JSON Array)**:

```sql
carts
(
  id, token, items JSONB -- [{sku_id: 1, qty: 2}, ...]
)
```

**Problems**:

- Can't enforce foreign key to SKUs
- Hard to update single item quantity
- No database constraints on quantity

**Good Design (Separate Table)**:

```sql
cart_items
(
  id, cart_id, sku_id, quantity
)
```

**Benefits**:

- Foreign key ensures SKU exists
- Can query across carts
- Easy partial updates
- Can add item-level properties later

**Key Fields**:

```sql
id
BIGINT PRIMARY KEY
cart_id     BIGINT FOREIGN KEY -> carts(id)
sku_id      BIGINT FOREIGN KEY -> skus(id)
quantity    INT NOT NULL CHECK (quantity > 0)
created_at  TIMESTAMP
```

---

### 1.3 Stock Reservation Tables

#### `reservations`

**Purpose**: Prevents overselling by reserving stock during checkout and support the `10-15 minutes reservation`
mechanism.

**Why this table exists?**

If we rely on the quantity of `skus` table, it is hard to tell who reserves those quantities.
It doesn't make sense if we create a new `Order` record since we haven't completed the payment (in case of bank
transfer).
Also, it might be confusing if after some time, those quantities suddenly come back without any clear logs.
This design also helps resume the checkout session if intentionally or accidentally, customers click `Checkout` multiple
times.

**Key Fields**:

```sql
id
BIGINT PRIMARY KEY
sku_id      BIGINT FOREIGN KEY -> skus(id)
cart_id     BIGINT FOREIGN KEY -> carts(id)
quantity    INT NOT NULL CHECK (quantity > 0)
status      VARCHAR(20)         -- ACTIVE, CONSUMED, EXPIRED, CANCELLED
expires_at  TIMESTAMP NOT NULL  -- Auto-calculated: NOW() + 15 minutes
created_at  TIMESTAMP
```

**Why `expires_at` field?**

- **Problem**: Customer starts checkout but abandons (close browser, change mind)
- **Impact**: Stock locked indefinitely, other customers can't buy
- **Solution**: Auto-expire after 15 minutes
- **Implementation**: Scheduled job finds `status=ACTIVE AND expires_at < NOW()`

**Status Flow**:

```
ACTIVE -------------------> CONSUMED (order placed)
                |---------> EXPIRED (15 min timeout)
                |---------> CANCELLED (new checkout started)
```

**Atomic Reservation Process**:

```sql
-- Step 1: Deduct stock (CRITICAL - must be atomic)
UPDATE skus
SET stock_qty = stock_qty - ?
WHERE id = ?
  AND stock_qty >= ? RETURNING *;
-- If affected_rows = 0: Insufficient stock, rollback

-- Step 2: Create reservation
INSERT INTO reservations
    (sku_id, cart_id, quantity, status, expires_at)
VALUES (?, ?, ?, 'ACTIVE', NOW() + INTERVAL '15 minutes');
```

**Why this prevents race conditions?**

- `UPDATE ... WHERE stock_qty >= ?` is **atomic operation**
- Database locks row during UPDATE
- Only ONE transaction can succeed if stock=1
- Loser gets 0 affected rows immediately

---

### 1.4 Order Tables

#### `orders`

**Purpose**: Store customer orders

**Why separate from cart?**

- Cart is temporary, Order is permanent
- Cart can be modified, Order is immutable (except status)
- Different fields needed (shipping address, payment method)
- Clean separation of concerns

**Key Fields**:

```sql
id
BIGINT PRIMARY KEY
tracking_token  UUID UNIQUE NOT NULL -- **Anonymous tracking**
status          VARCHAR(20)          -- Order lifecycle
payment_method  VARCHAR(20)          -- COD, BANK_TRANSFER
total_amount    DECIMAL(10,2)
customer_name   VARCHAR(100)
customer_phone  VARCHAR(20)
customer_email  VARCHAR(100)
shipping_address TEXT
note            TEXT                 -- Customer instructions
created_at      TIMESTAMP
```

**Why `tracking_token` as UUID?**

- **Requirement**: "Đừng bắt họ đăng nhập... cứ bấm link là xem được"
- **Solution**: UUID in email link: `{frontend}/track/{uuid}`
- **Security**: Unguessable token = pseudo-authentication
- **Simplicity**: No login/password required

**Why `total_amount` when can calculate from order_items?**

- **Performance**: Avoid JOIN for common operation
- **Historical Accuracy**: Preserve total even if prices change later
- **Denormalization Justified**: Read-heavy operation, rarely updated

**Status Enum Values**:

- `PENDING_PAYMENT`: Order created, awaiting payment confirmation
- `CONFIRMED`: Payment confirmed, ready for processing
- `SHIPPING`: Order shipped to customer
- `DELIVERED`: Successfully delivered
- `CANCELLED`: Order cancelled (stock restored)

**Status Transitions**:

```
PENDING_PAYMENT -> CONFIRMED -> SHIPPING -> DELIVERED
       |
   CANCELLED (can cancel from any status)
```

---

#### `order_items`

**Purpose**: Store items in order (line items)

**Why separate table?**

- One order -> Many items (1:N relationship)
- Normalize order structure
- Can query "all orders containing product X"

**Key Fields**:

```sql
id
BIGINT PRIMARY KEY
order_id    BIGINT FOREIGN KEY -> orders(id)
sku_id      BIGINT FOREIGN KEY -> skus(id)
quantity    INT NOT NULL CHECK (quantity > 0)
unit_price  DECIMAL(10,2)       -- **Price snapshot**
created_at  TIMESTAMP
```

**Why store `unit_price` when SKU has price?**

One day, customers order an iPhone 17 Pro Max with price of 50.000.000 VND.
Later on, that iPhone is on sale with price of only 45.000.000 VND.
That will be when customers complain `Why is it price 45.000.000 VND now? I bought it with the price of 50.000.000 VND`.

**Solution - Price Snapshot**:

```
order_items stores unit_price = 50.000.000 VND at time of order
Even if SKU price changes later, order history shows correct price
```

**Benefits**:

- Historical accuracy (what customer actually paid)
- Audit compliance (financial records must not change)
- Customer trust (order shows agreed price)

**Calculated Field**:

- `item_total = unit_price × quantity` (calculated in application, not stored)

---

#### `order_histories`

**Purpose**: Audit trail of order status changes

**Why separate table? Why not update order with timestamp?**

**Bad Design (Single Table)**:

```sql
orders
(
  id, status, last_updated_at, last_updated_by
)
```

Track `What is the latest modification?`, not the entire history.

**Good Design (History Table)**:

```sql
order_histories
(
  id, order_id, old_status, new_status, note, created_at
)
```

**Key Fields**:

```sql
id
BIGINT PRIMARY KEY
order_id    BIGINT FOREIGN KEY -> orders(id)
old_status  VARCHAR(20)         -- Previous status (NULL for creation)
new_status  VARCHAR(20)         -- New status
note        TEXT                -- "Shipped via GHN", "Customer requested"
created_at  TIMESTAMP           -- When change occurred
```

**Benefits**:

- **Non-destructive**: Never lose history
- **Auditability**: Know who changed what and when
- **Compliance**: Required for financial auditing
- **Debugging**: Trace order lifecycle issues

**Example History**:

```
Order #1001:
  2026-01-18 10:00 | NULL            -> PENDING_PAYMENT | "Order created"
  2026-01-18 10:30 | PENDING_PAYMENT -> CONFIRMED       | "Payment confirmed by admin@example.com"
  2026-01-18 14:00 | CONFIRMED       -> SHIPPING        | "Shipped via GHN, tracking: GHN123"
  2026-01-19 09:00 | SHIPPING        -> DELIVERED       | "Delivered successfully"
```

---

### 1.5 Authentication Tables

#### `users`

**Purpose**: Admin accounts, ready for scaling up to customer acccounts.

**Why not customer accounts for now?**

- **Requirement**: "Đừng bắt họ đăng nhập" - No customer login required
- **Scope**: Only admin needs authentication

**Key Fields**:

```sql
id
BIGINT PRIMARY KEY
full_name   VARCHAR(100)
email       VARCHAR(100) UNIQUE -- Login identifier
password    VARCHAR(255)        -- BCrypt hashed (NOT plain text)
role        VARCHAR(20)         -- ADMINISTRATOR, CUSTOMER
is_active   BOOLEAN             -- Account status
created_at  TIMESTAMP
```

**Why `password` is VARCHAR(255)?**

- BCrypt hash output is 60 characters (even if max hash rounds of 20)
- VARCHAR(255) provides buffer for algorithm changes

---

#### `blacklisted_token`

**Purpose**: Invalidate JWT tokens on logout

**Why needed? JWT can't be "revoked" normally**

**Problem**:

- JWT tokens are stateless (no server-side session)
- Token valid until expiration
- Logout button doesn't invalidate token
- Attacker with stolen token can still use it

**Solution**:

```sql
blacklisted_token
(
  id, token, token_type, expiry_date, created_at
)
```

**Workflow**:

1. User clicks "Logout"
2. Backend adds token to blacklist
3. Subsequent requests check: `WHERE token IN (SELECT token FROM blacklisted_token)`
4. If blacklisted -> 401 Unauthorized

**Why `expiry_date` field?**

- **Cleanup**: Automatically delete expired tokens
- **Performance**: No need to check tokens that expired anyway
- **Storage**: Prevent infinite growth of blacklist

**Scheduled Cleanup**:

```sql
DELETE
FROM blacklisted_token
WHERE expiry_date < NOW();
-- Run daily to clean expired tokens
```

---

## 2. Key Design Decisions

### 2.1 Normalization vs. Denormalization

**Normalized Tables** (Strict 3NF):

- `categories` - No redundant data
- `products` ↔ `skus` - Variants separated
- `skus` ↔ `sku_codes` - Multiple codes per SKU separated
- `order_histories` - Full audit trail

**Strategic Denormalization**:

- `products.base_price` - Could compute from MIN(skus.price), but denormalized for performance
- `orders.total_amount` - Could sum order_items, but cached for fast queries
- `order_items.unit_price` - Price snapshot for historical accuracy

**Rationale**:

- Read-heavy operations (viewing orders) benefit from denormalization
- Writes are less frequent (creating order once)
- Tradeoff: Slightly more storage for much faster queries

---

### 2.2 UUID vs. Auto-Increment ID

**Auto-Increment IDs** (Chosen for most tables):

```sql
id
BIGINT GENERATED BY DEFAULT AS IDENTITY
```

**Pros**:

- Compact (8 bytes vs 16 bytes)
- Sequential = better index performance
- Human-readable (Order #1001)

**UUIDs** (Used for tokens only):

```sql
token
UUID DEFAULT gen_random_uuid()
```

**Pros**:

- Globally unique (no coordination needed)
- Unguessable (security for tracking links)
- Can generate client-side

**Design Decision**:

- Use auto-increment for internal IDs (primary keys)
- Use UUID for external identifiers (cart token, tracking token)

---

### 2.3 Soft Delete vs. Hard Delete

**Soft Delete** (Chosen for users):

```sql
is_active
BOOLEAN DEFAULT TRUE
-- "Delete" = UPDATE users SET is_active = FALSE
```

**Pros**:

- Recoverable (can reactivate)
- Foreign key references intact

**Hard Delete** (Chosen for carts, reservations):

```sql
DELETE
FROM carts
WHERE created_at < NOW() - INTERVAL '30 days';
```

**Pros**:

- Automatic cleanup
- GDPR (General Data Protection Regulation) compliance (data actually removed)

+ These kinds of data can be cleaned up without storing some encrypted trails.
+ For example, in some systems that provide free trial (AI agents, IDEs,...), they usually provide pro version with free
  trial of some days, and after that they fee the user.
+ There's one trick: creating the account, register the free trial, and delete the account when the trial ends, and
  repeat that.
+ Usually, when having to adhere to GDPR, system has to remove the entire user's information, but that leaves the window
  for that trick.
+ Therefore, they usually store encrypted data (i.e, email), and check the registered email with the hash one to see if
  it matches or not.
+ For this specific system, we don't need to store encrypted data, just fully remove it.

- Database size management

**Design Decision**:

- Soft delete: Important data (users, orders)
- Hard delete: Temporary data (carts, expired reservations)

---

### 2.4 Timestamp Handling

**All tables have**:

```sql
created_at
TIMESTAMP DEFAULT CURRENT_TIMESTAMP
```

**Some tables have**:

```sql
updated_at
TIMESTAMP DEFAULT CURRENT_TIMESTAMP
-- Trigger: ON UPDATE SET updated_at = CURRENT_TIMESTAMP
```

**Why not `updated_at` on orders?**

- Orders are mostly immutable (only status changes)
- Status changes tracked in `order_histories` with timestamps
- Avoid redundancy

---

## 3. Relationships & Constraints

### 3.1 Foreign Key Relationships

**One-to-Many (1:N)**:

```
categories (1) --> (*) products
products   (1) --> (*) skus
skus       (1) --> (*) sku_codes
products   (1) --> (*) product_images
carts      (1) --> (*) cart_items
carts      (1) --> (*) reservations
orders     (1) --> (*) order_items
orders     (1) --> (*) order_histories
```

**Many-to-One (N:1)**:

```
cart_items (*) --> (1) skus
order_items (*) --> (1) skus
reservations (*) --> (1) skus
sku_codes (*) --> (1) skus
```

### 3.2 Cascade Behavior

**CASCADE DELETE** (When parent deleted, children deleted):

```sql
-- If product deleted, all SKUs deleted
ALTER TABLE skus
    ADD CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE;

-- If SKU deleted, all its codes deleted
ALTER TABLE sku_codes
    ADD CONSTRAINT fk_sku_codes_sku FOREIGN KEY (sku_id) REFERENCES skus (id) ON DELETE CASCADE;
```

**Design Decision**: Use cascade carefully

- CASCADE: product -> skus (makes sense, SKU can't exist without product)
- CASCADE: sku -> sku_codes (codes are meaningless without SKU)
- NO CASCADE: sku -> order_items (preserve order history even if SKU deleted)

---

## 4. Indexes & Performance

### 4.1 Primary Key Indexes (Automatic)

Every table has index on primary key:

```sql
CREATE INDEX products_pkey ON products (id);
```

### 4.2 Foreign Key Indexes (Essential)

**Why index foreign keys?**

- JOIN performance (e.g., products JOIN skus)
- Cascade delete performance
- Referential integrity checks

**Created Indexes**:

```sql
CREATE INDEX idx_products_category ON products (category_id);
CREATE INDEX idx_skus_product ON skus (product_id);
CREATE INDEX idx_sku_codes_sku ON sku_codes (sku_id);
CREATE INDEX idx_sku_codes_code ON sku_codes (code);
CREATE INDEX idx_cart_items_cart ON cart_items (cart_id);
CREATE INDEX idx_cart_items_sku ON cart_items (sku_id);
CREATE INDEX idx_order_items_order ON order_items (order_id);
CREATE INDEX idx_order_items_sku ON order_items (sku_id);
CREATE INDEX idx_reservations_cart ON reservations (cart_id);
CREATE INDEX idx_reservations_sku ON reservations (sku_id);
```

### 4.3 Unique Constraint Indexes (Business Logic)

**Why these unique indexes?**

```sql
CREATE UNIQUE INDEX idx_carts_token ON carts (token);
-- Ensures no duplicate cart tokens (UUID collision protection)

CREATE UNIQUE INDEX idx_orders_tracking_token ON orders (tracking_token);
-- Ensures no duplicate tracking links

CREATE UNIQUE INDEX idx_users_email ON users (email);
-- Ensures one account per email

CREATE UNIQUE INDEX idx_sku_codes_code ON sku_codes (code);
-- Ensures no duplicate codes across all SKUs (global uniqueness)
```

### 4.4 Query Optimization Indexes

**Composite Index for Reservation Cleanup**:

```sql
CREATE INDEX idx_reservations_cleanup ON reservations (status, expires_at);
-- Optimizes: WHERE status='ACTIVE' AND expires_at < NOW()
```

**Partial Index for Active Reservations**:

```sql
CREATE INDEX idx_reservations_active ON reservations (cart_id, sku_id) WHERE status='ACTIVE';
-- Faster lookup for active reservations only
```

---

## 5. Data Integrity

### 5.1 NOT NULL Constraints

**Critical Fields Must Not Be NULL**:

```sql
skus.stock_qty NOT NULL           -- Must always know stock level
sku_codes.code NOT NULL           -- Every code must have a value
sku_codes.is_primary NOT NULL     -- Must specify if code is primary
orders.tracking_token NOT NULL    -- Every order needs tracking
carts.token NOT NULL              -- Cart must have identifier
reservations.expires_at NOT NULL  -- Must know when to cleanup
```

### 5.2 CHECK Constraints

**Enforce Business Rules at Database Level**:

```sql
CHECK (quantity > 0)              -- No zero-quantity items
CHECK (stock_qty >= 0)            -- Stock can't be negative
CHECK (price > 0)                 -- Prices must be positive
CHECK (total_amount >= 0)         -- Orders can't have negative total
```

### 5.3 UNIQUE Constraints

**Prevent Duplicates**:

```sql
UNIQUE (token)                    -- Unique cart identifiers
UNIQUE (tracking_token)           -- Unique order tracking
UNIQUE (email)                    -- One account per email
UNIQUE (code)                     -- Unique SKU codes globally
UNIQUE (slug)                     -- Unique URLs
```

---