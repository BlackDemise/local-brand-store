# Project Deliverable Summary

According to `docs\initial\assignment-instruct.txt` (or `docs\initial\assignment-instruct.pdf`):

## Assignment Requirements

### 1. Báo cáo kỹ thuật (Technical Report)

**Định dạng:** PDF hoặc Markdown, bao gồm:

#### (1) Đánh giá sơ bộ & Phân tích yêu cầu

- **Xác định Scope:** Dựa trên email khách hàng, liệt kê các tính năng Phải làm (Must-have) và Nên làm (Nice-to-have)
  trong 2 tuần.
- **Gap Analysis:** So sánh giữa yêu cầu thô (Email) và yêu cầu kỹ thuật thực tế.
- **Đánh giá khả năng hoàn thiện:** Cam kết bao nhiêu % so với yêu cầu? Nếu không kịp, đề xuất cắt giảm tính năng nào? (
  Ví dụ: Thay vì tích hợp SePay thật, có thể giả lập Webhook).

#### (2) Thiết kế hệ thống (System Design)

**DB Design (3):**

- Sơ đồ ERD (Entity Relationship Diagram) chi tiết các bảng, kiểu dữ liệu và quan hệ.
- Giải thích lý do thiết kế (Tại sao tách bảng X? Tại sao cần bảng Y?).

**LLD (Low-Level Design) (4):**

- Danh sách API Endpoints (chỉ cần Method, URL, Description).
- Sequence Diagram (Biểu đồ tuần tự) cho các luồng được đánh giá là quan trọng hoặc phức tạp nhất.

### 2. Source Code & Hướng dẫn

- **Source Code:** Link GitHub Repository (Cấu trúc thư mục rõ ràng, tuân thủ Clean Architecture/Modular).
- **Setup Guide:** File README.md hướng dẫn chi tiết:
    - Cách cài đặt môi trường (Node version, DB setup).
    - Cách chạy Migration/Seed dữ liệu mẫu.
    - Cách chạy server.
- **API Collection:** File export từ Postman/Insomnia hoặc Link Swagger để giảng viên test.

---

*(Accepting Markdown is such a huge convenience, thank you :v)*

## Deliverables Mapping

### (1) Đánh giá sơ bộ & Phân tích yêu cầu

- **Location:** `docs\deliverables\requirement-analysis\requirement-analysis.md`

### (2) Thiết kế hệ thống (System Design)

#### DB Design (3): Sơ đồ ERD

- **Location:** `docs\deliverables\database-design\erd.puml`

#### Giải thích lý do thiết kế

- **Location:** `docs\deliverables\database-design\design-explanation.md`

#### LLD (4) - Danh sách API Endpoints

- **Location:** All Markdown files in `docs\deliverables\api-documentation\**`

#### Sequence Diagram

- **Location:** All PlantUML files in `docs\deliverables\sequence-diagram`

### Source Code & Hướng dẫn

#### Source Code

- **Location:** `backend`

#### Setup Guide

- **Location:** `backend\README.md`

#### API Collection

- **Location:** All HTTP request files in `docs\deliverables\api-documentation\**\`

---

## Additional Notes

- There is a `frontend` as well, just my OCD, so please don't mind it.

With that in mind, once again, sincerely thank you :v.
