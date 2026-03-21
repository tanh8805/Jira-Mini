# Jira Mini — Project Overview

## Tech Stack

| Layer | Công nghệ |
|---|---|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.x |
| **Security** | Spring Security + JWT (JJWT) |
| **ORM** | Spring Data JPA (Hibernate) |
| **Database** | PostgreSQL |
| **Cache / Session** | Redis (lưu Refresh Token) |
| **Build tool** | Maven |
| **Containerization** | Docker + Docker Compose |

---

## Kiến trúc

Dự án theo mô hình **Layered Architecture** (phân lớp thuần túy):

```
Request
   ↓
[Controller]      — Nhận request, validate input, trả response
   ↓
[Service]         — Business logic, kiểm tra phân quyền
   ↓
[Repository]      — Truy vấn database qua JPA
   ↓
[Entity]          — Ánh xạ bảng database
   ↓
[PostgreSQL]
```

### Xử lý ngoại lệ tập trung

Toàn bộ lỗi được bắt bởi `GlobalExceptionHandler` (`@RestControllerAdvice`), trả về `ErrorMessage` chuẩn hóa với HTTP status phù hợp.

---

## Cấu trúc thư mục

```
src/main/java/com/example/jira_mini/
├── config/
│   ├── jwt/                    # JwtService, JwtFilter
│   ├── redis/                  # RedisConfig
│   └── security                # CustomerDetails, CustomerDetailsService, SecurityConfig
├── controller/
│   ├── Auth/                   # LoginController, RegisterController, RefreshTokenController, LogoutController
│   └── Project/                # GetProject, CreateProject, UpdateProject, AddMember
├── service/
│   └── Auth/                   #LoginService, RegisterService, TokenBlacklistService
│   └── Project/                # ProjectService
├── repository/                 # JPA Repositories
├── entity/                     # JPA Entities
│   └── enums/                  # ProjectRole, SystemRole, TaskPriority, TaskStatus
├── dto/
│   ├── Auth/                   # LoginRequest, RegisterRequest
│   └── Project/                # CreateProjectRequest, UpdateProjectRequest, AddMemberRequest
│   └──                         # ErrorMessage, ResponseMessage, TokenMessage
├── repository/                 # AuditLogRepository, ProjectMemberRepository, ProjectRepository, TaskRepository, UserRepository
└── exception/                  # Custom exceptions + GlobalExceptionHandler
```

---

## Bảo mật

- **Access Token**: JWT ngắn hạn (15 phút), gửi kèm mọi request qua `Authorization: Bearer <token>`
- **Refresh Token**: Lưu trong Redis với TTL dài hơn (7 ngày), dùng để cấp lại Access Token
- **Phân quyền trong Project**: Kiểm tra role ngay trong Service layer, tránh query thừa

### Flow xác thực

```
Client → [POST /api/auth/login]
            ↓
      AuthenticationManager xác thực
            ↓
      Cấp Access Token + Refresh Token
            ↓
Client gửi Access Token trong header mọi request
            ↓
      JwtFilter xác thực → SecurityContext
```

---

## Database Schema (tóm tắt)

```
users
├── id (UUID, PK)
├── email (unique)
├── password (bcrypt)
└── full_name

projects
├── id (UUID, PK)
├── name
├── description
├── owner_id (FK → users)
└── created_at

project_members
├── id (UUID, PK)
├── project_id (FK → projects)
├── user_id (FK → users)
└── role (OWNER / MANAGER / MEMBER)

issues
├── id (UUID, PK)
├── project_id (FK → projects)
├── assignee_id (FK → users)
├── title
├── description
├── status (TODO / IN_PROGRESS / DONE)
└── created_at
```

---

## API Endpoints (tóm tắt)

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| POST | `/api/auth/register` | Đăng ký | ❌ |
| POST | `/api/auth/login` | Đăng nhập | ❌ |
| POST | `/api/auth/refresh` | Làm mới token | ❌ |
| GET | `/api/projects` | Lấy danh sách project của tôi | ✅ |
| POST | `/api/projects` | Tạo project mới | ✅ |
| PUT | `/api/projects/{id}` | Cập nhật project | ✅ OWNER |
| POST | `/api/projects/{id}/members` | Thêm thành viên | ✅ OWNER/MANAGER |

---

## Error Response Format

Mọi lỗi đều trả về cùng một cấu trúc:

```json
{
  "status": 403,
  "message": "Only OWNER can update the project"
}
```

| HTTP Status | Trường hợp |
|---|---|
| 400 | Validation thất bại |
| 401 | Token hết hạn / chưa đăng nhập |
| 403 | Không đủ quyền trong project |
| 404 | Không tìm thấy resource |
| 409 | Trùng dữ liệu (email, member) |
| 500 | Lỗi server chưa được xử lý |