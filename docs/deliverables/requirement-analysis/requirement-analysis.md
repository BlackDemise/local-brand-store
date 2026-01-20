# Requirement Analysis

---

## Table of Contents

1. [Preliminary Assessment](#1-preliminary-assessment)
2. [Requirements Analysis](#2-requirements-analysis)
3. [Scope Definition](#3-scope-definition)
4. [Gap Analysis](#4-gap-analysis)
5. [Feasibility Assessment](#5-feasibility-assessment)

---

## 1. Preliminary Assessment

### 1.1 Business Context

**Client**: Anh Hùng operates "Hung Hypebeast", a local fashion brand in Vietnam selling streetwear products (áo thun,
hoodie, quần jean, giày dép, phụ kiện).

**Current Pain Points**:

- Manual order management (`Hiện tại shop anh đang vỡ trận vì quản lý thủ công quá`)
- Critical overselling issues during sales events (multiple customers purchasing the last item simultaneously)

`Đợt trước sale, cái áo còn đúng 1 cái size L. Hai ông khách cùng bấm mua một lúc. Hệ thống cũ vẫn báo thành công cho cả 2, cuối cùng anh phải gọi điện xin lỗi 1 ông, bị chửi te tua.`

**Inferred Pain Points**:

- No customer order and payment status tracking (customers must contact the shop manually to check status)
- No shopping cart functionality (customers must remember what they want to buy)

**Business Impact**:

- Customer complaints and loss of trust during overselling incidents
- Operational inefficiency (staff spending hours on manual processes)
- Missed sales opportunities during peak periods
- Poor customer satisfaction

### 1.2 Project Goals

**Primary Objective**: Develop a production-ready headless e-commerce backend API within 2 weeks to support the upcoming
end-of-month sales event.

**Success Criteria**:

1. **Eliminate overselling** - No customer can purchase out-of-stock items
2. **Automation** - Orders processed automatically without manual intervention
3. **Customer self-service** - Order tracking without requiring phone calls
4. **Scalability** - Handle concurrent customers during sales events

---

## 2. Requirements Analysis

### 2.1 Raw Requirements from Client Email

The client email identified the following core needs:

#### Critical Requirements (Must-Have)

**1. Product Catalog Management**

- Client Need: `Quản lý được từng cái SKU này` (Manage each SKU)
- Business Context: One product (e.g., `Áo Thun Rồng`) has multiple variants by size (M, L, XL) and color (Đen, Trắng)
- Technical Interpretation:
    - Separate SKU (Stock Keeping Unit) management for each size/color combination
    - Display product list with fast loading and pagination
    - Filter by price range and category

**2. Shopping Cart (Anonymous)**

- Client Need: `Giỏ hàng để khách gom nhiều món mua một lần cho tiện ship`
- Business Context: Customers want to add multiple items before checkout
- Technical Interpretation:
    - Add/update/remove items from cart
    - Real-time stock validation ("đừng để khách thêm 10 cái vào giỏ trong khi kho còn có 2 cái")
    - No login required (reduce friction)
    - Cart persistence across sessions

**3. Inventory Control - "Last Item Problem"**

- Client Need: `Hai ông khách cùng bấm mua một lúc... cuối cùng anh phải gọi điện xin lỗi 1 ông`
- Business Context: During sales, multiple customers attempting to buy the last item simultaneously
- Technical Interpretation:
    - Stock reservation during checkout (10-15 minutes hold)
    - Atomic stock deduction (only one customer succeeds)
    - Auto-release if checkout not completed
    - Prevent negative stock at database level

**4. Checkout & Order Placement**

- Client Need: `Khách điền thông tin ship -> Chọn thanh toán -> Bấm 'Đặt hàng'`
- Business Context: Two-phase process separating reservation from order creation
- Technical Interpretation:
    - Collect shipping information
    - Support COD and Bank Transfer payment methods
    - Create order only after customer confirms
    - Convert reservations to orders

**5. Order Tracking (No Login)**

- Client Need: `Đừng bắt họ đăng nhập hay tạo tài khoản gì cả, phiền phức lắm`
- Business Context: Customers want simple tracking without account creation hassle
- Technical Interpretation:
    - Email confirmation with tracking link
    - Anonymous tracking using unique token
    - Display order status (Đã xác nhận, Đã thanh toán, Đang giao...)
    - Email notifications for status updates

**6. Admin Order Management**

- Client Need: `API để xem danh sách đơn hàng... đổi trạng thái đơn hàng`
- Business Context: Admin needs to process orders without accessing database directly
- Technical Interpretation:
    - View order list with filters (status, date range)
    - Update order status workflow
    - Cancel orders with stock restoration
    - Audit trail for status changes

#### Secondary Requirements (Nice-to-Have)

**7. Email Notifications**

- Client Need: Mentioned but not critical for Phase 1
- Decision: **Included** - Low effort, high value; later on beautify the email using a template (let client decide this)

**8. SePay Payment Integration**

- Client Need: "Tuy nhiên nếu không kịp thì để phase sau"
- Decision: **Included** - Needs regression tests and audits to make sure this function doesn't only function properly
  but is also secured.

**9. Admin Product Management**

- Client Need: "Chưa cần làm phần nhập liệu sản phẩm... để phase sau"
- Decision: **Deferred** - Client will insert products directly to database

### 2.2 Requirements Clarification

#### Q1: What happens if server crashes during stock reservation?

**Analysis**: Server crash after reserving stock but before order completion could leave stock permanently locked.

**Resolution**:

- On UI, we notify the customer that if they don't pay within 15 minutes (if payment method is bank transfer), his/her
  reservation will be aborted.
- Scheduled cleanup job runs every 1 minutes (for Phase 1, real-time cleanup is deferred)
- Database transaction ensures no partial state
- On server restart, expired reservations auto-cleaned

#### Q2: How to handle concurrent requests for the last item?

**Analysis**: Two customers clicking "buy" simultaneously for the last item - both see "1 available" but only one should
succeed. From technical point of view, we can't know who will succeed, but if only one succeeds, then we manage to
prevent this issue.

**Resolution**:

- Use atomic database UPDATE with WHERE clause: `UPDATE skus SET stock_qty = stock_qty - ? WHERE stock_qty >= ?`
- Database guarantees only ONE transaction succeeds (returns 1 affected row)
- Loser immediately receives "Insufficient Stock" error
- No application-level locking required

#### Q3: What if customer changes mind during reservation?

**Analysis**: Stock held for 15 minutes prevents other customers from buying. (refer to Q1)

**Resolution**:

- For Phase 2: add Cancel button on UI to instantly abort the reservation.

#### Q4: Should cart require authentication?

**Client Requirement**: `Đừng bắt họ đăng nhập hay tạo tài khoản gì cả`

**Resolution**:

- Anonymous cart using UUID token stored in browser (duplicate risk is negligable)
- No authentication required for shopping
- Only admin operations require authentication

#### Q5: How to ensure email tracking links work?

**Analysis**: Customer needs to track order without login, but how to secure it?

**Resolution**:

- Generate unique UUID tracking token per order
- Token is unguessable (128-bit UUID)
- Include in email: `{frontend_url}/track/{tracking_token}` (this will work once client integrates the actual frontend
  implementation)
- No authentication required to view order with valid token

### 2.3 Derived Technical Requirements

Based on business needs, the following technical requirements were identified:

**Database Design**:

- Normalize schema: `products` → `skus` for variant management
- `reservations` table for stock reservation system
- `order_histories` table for audit trail
- Indexes on frequently queried fields

**Concurrency Control**:

- Atomic updates for stock deduction
- Transaction management across multiple operations

**Performance**:

- Pagination for product listing (default 20 items per page)
- Database indexing on `category_id`, `cart.token`, `order.tracking_token`
- Query optimization for common operations
- Redis caching for frequently accessed data

**Security**:

- JWT authentication for admin operations
- Password hashing with BCrypt
- CORS configuration for frontend integration
- Input validation and sanitization
- Token blacklist on logout

**Email System**:

- SMTP integration for transactional emails
- Async sending to avoid blocking requests
- Retry logic for failed sends

---

## 3. Scope Definition

### 3.1 Must-Have Features (Committed for Phase 1)

| # | Feature           | Description                                                  | Priority | Status   |
|---|-------------------|--------------------------------------------------------------|----------|----------|
| 1 | Product Catalog   | List/filter products with pagination, category/price filters | P0       | Complete |
| 2 | Anonymous Cart    | Add/update/remove items without login, UUID-based session    | P0       | Complete |
| 3 | Stock Reservation | Reserve stock during checkout, 15-min expiry, auto-cleanup   | P0       | Complete |
| 4 | Order Placement   | Create orders with COD/Bank Transfer, two-phase checkout     | P0       | Complete |
| 5 | Order Tracking    | Anonymous tracking via email link, no login required         | P0       | Complete |
| 6 | Admin Order Mgmt  | View/filter/update/cancel orders with stock restoration      | P0       | Complete |

**Commitment**: **100%** of must-have features delivered

### 3.2 Nice-to-Have Features (Stretch Goals)

| #  | Feature             | Description                               | Priority | Status   |
|----|---------------------|-------------------------------------------|----------|----------|
| 7  | Email Notifications | Order confirmation & status update emails | P1       | Complete |
| 8  | JWT Authentication  | Secure admin login with token-based auth  | P1       | Complete |
| 9  | Order History       | Full audit trail of status changes        | P1       | Complete |
| 10 | SePay Webhook       | Ready for regression tests and audits     | P1       | Complete |

**Achievement**: **100%** of nice-to-have features delivered

### 3.3 Out of Scope

| # | Feature               | Reason for Deferral                                   | Estimated Effort |
|---|-----------------------|-------------------------------------------------------|------------------|
| 1 | Admin Product CRUD    | Client: "Chưa cần làm phần nhập liệu... để phase sau" | 2-3 days         |
| 3 | Customer Registration | Not required by client (anonymous shopping only)      | 2-3 days         |
| 4 | Advanced Analytics    | Not requested (basic order list sufficient)           | 3-4 days         |
| 5 | Multi-language        | Not requested (Vietnamese only)                       | 2-3 days         |

### 3.4 Scope Adjustment Rationale

**Why defer Admin Product CRUD?**

- Client explicitly stated: "Phần này anh nhờ team data nhập thẳng vào Database cũng được"
- Allows focus on critical features (overselling prevention)
- Database seeding scripts can handle initial product import
- Can be added in Phase 2 when system is stable

---

## 4. Gap Analysis

### 4.1 Technical Gaps

| Gap                        | Impact                    | Solution                                     |
|----------------------------|---------------------------|----------------------------------------------|
| **No Database**            | Cannot persist data       | PostgreSQL with Flyway migrations            |
| **No API**                 | Frontend cannot integrate | 24 RESTful API endpoints                     |
| **No Concurrency Control** | Overselling crisis        | Atomic operations and database locks         |
| **No Email System**        | Manual customer contact   | SMTP integration with simple email template  |
| **No Security**            | Unauthorized access       | JWT authentication for admin                 |
| **No Testing**             | Bugs in production        | 52 tests in total                            |
| **No Documentation**       | Hard to maintain          | Comprehensive documents in docs/deliverables |

### 4.2 Capability Gaps Addressed

**Before Project** → **After Project**:

1. **Order Creation**:
    - Before: Customer calls, admin manually enters in Excel
    - After: Customer places order via API, automatically stored in database

2. **Stock Management**:
    - Before: Manual counting, Excel tracking
    - After: Real-time atomic updates, automatic validation

3. **Overselling Prevention**:
    - Before: Frequent issues during sales (crisis-level problem)
    - After: **Completely eliminated** via reservation system

4. **Order Tracking**:
    - Before: Customer calls admin, admin checks Excel, calls back
    - After: Customer clicks email link, sees status immediately

5. **Status Updates**:
    - Before: Admin manually calls customers
    - After: Automatic email notifications on status change

---

## 5. Feasibility Assessment

### 5.1 Complexity Evaluation

#### Low Complexity Features (1-2 days each)

- Product catalog API with pagination
- Category management
- Basic CRUD operations
- Simple email sending

#### Medium Complexity Features (2-3 days each)

- Anonymous cart system (UUID-based sessions)
- Order placement workflow
- JWT authentication implementation
- Admin order management APIs

#### High Complexity Features (3-4 days each)

- **Stock reservation system** (atomic operations, race conditions)
- **Two-phase checkout** (reserve -> confirm)
- **Scheduled cleanup job** (expired reservations)
- **Concurrent request handling** (last item problem)

### 5.2 Resource Assessment

**Available Resources**:

- 1 Backend Developer
- PostgreSQL
- Spring Boot ecosystem
- Email server (use client's email)
- Local environment for rapid development and deploy to VPS for testing payment feature

**Missing Resource(s)**:

- [ ] UI/UX designer (not needed - headless API)

### 5.3 Risk Assessment

| Risk                         | Probability | Impact   | Mitigation Strategy                      |
|------------------------------|-------------|----------|------------------------------------------|
| Stock reservation complexity | Medium      | Critical | Research database locking patterns early |
| Concurrency bugs             | Medium      | Critical | Load testing with concurrent requests    |
| Database performance         | Low         | High     | Proper indexing, query optimization      |

---