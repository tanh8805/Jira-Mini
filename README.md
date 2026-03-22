# 🗂️ Jira Mini

Một ứng dụng quản lý công việc theo phong cách Jira cho các đội nhóm vừa và nhỏ, xây dựng bằng **Java Spring Boot + PostgreSQL + Docker + Redis + JWT**.

### 🌐 Live Demo

**👉 Trải nghiệm trực tiếp tại đây: [Jira Mini Live Demo](https://jira-mini-eight.vercel.app/)**

---

## 🚀 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| Security | Spring Security + JWT (JJWT) |
| ORM | Spring Data JPA (Hibernate) |
| Database | PostgreSQL |
| Cache / Blacklist | Redis |
| Build Tool | Maven |
| Containerization | Docker + Docker Compose |

---

## 📋 Chức năng

### 🔐 Xác thực (Auth)
- **Đăng ký** tài khoản với email, password, fullName
- **Đăng nhập** nhận về Access Token (15 phút) + Refresh Token (7 ngày)
- **Làm mới token** bằng Refresh Token
- **Đăng xuất** — blacklist Access Token vào Redis

### 📁 Project
- **Tạo project** — người tạo tự động trở thành OWNER
- **Cập nhật** tên và mô tả project — chỉ OWNER
- **Thêm thành viên** với role chỉ định — OWNER và MANAGER
- Phân quyền 3 cấp: **OWNER / MANAGER / MEMBER**

### ✅ Task
- **Tạo task** trong project — mọi member
- **Cập nhật** title, description, status, priority, assignee
- **Xoá task** — mọi member
- **Lọc task** theo status / priority / assignee, có phân trang
- Assignee phải là thành viên của project

### 📜 Audit Log
- Tự động ghi lại **toàn bộ lịch sử** tạo / cập nhật / xoá task
- Lưu snapshot **trước và sau** mỗi thay đổi

---

## 📁 Project Structure

```
src/main/java/com/example/jira_mini/
├── config/
│   ├── jwt/
│   ├── redis/
│   └── security/
├── controller/
│   ├── Auth/
│   ├── Project/
│   ├── Task/
│   └── Audit/
├── service/
│   ├── Auth/
│   ├── Project/
│   ├── Task/
│   └── Audit/
├── repository/
├── entity/
│   └── enums/
├── dto/
│   ├── Auth/
│   ├── Project/
│   ├── Task/
│   └── Audit/
└── exception/
```