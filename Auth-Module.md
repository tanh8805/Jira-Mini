# Auth-Module.md — Jira Mini

---

## Tổng quan

Module **Auth** xử lý toàn bộ vòng đời xác thực người dùng: đăng ký, đăng nhập, làm mới token và đăng xuất.
Bảo mật dựa trên **JWT** với 2 loại token riêng biệt (Access Token và Refresh Token), kết hợp **Redis** để blacklist token khi logout.

---

## API Endpoints

| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| POST | `/api/auth/register` | Đăng ký tài khoản | ❌ |
| POST | `/api/auth/login` | Đăng nhập | ❌ |
| POST | `/api/auth/logout` | Đăng xuất | ✅ Access Token |
| POST | `/api/auth/refresh` | Làm mới Access Token | ✅ Refresh Token |

---

## Chi tiết từng endpoint

### POST `/api/auth/register`

**Request body:**
```json
{
  "email": "string (required, đúng format email)",
  "password": "string (required, tối thiểu 6 ký tự)",
  "fullName": "string (required)"
}
```

**Luồng xử lý:**
```
Validate input (@Valid)
    ↓
Kiểm tra email đã tồn tại chưa
    ↓
Hash password bằng BCrypt
    ↓
Lưu User vào DB
```

**Response `200 OK`:**
```json
{
  "message": "Register successfully!"
}
```

**Response `400 Bad Request`** — Validation lỗi:
```json
{
  "status": 400,
  "message": "..."
}
```

**Response `409 Conflict`** — Email đã tồn tại:
```json
{
  "status": 409,
  "message": "Email ... in use. Please use another one.!"
}
```

---

### POST `/api/auth/login`

**Request body:**
```json
{
  "email": "string (required, đúng format email)",
  "password": "string (required, tối thiểu 6 ký tự)"
}
```

**Luồng xử lý:**
```
Validate input (@Valid)
    ↓
AuthenticationManager xác thực email + password
    ↓
Sinh Access Token (JWT, expire theo config)
    ↓
Sinh Refresh Token (JWT, expire theo config, subject = userId)
    ↓
Trả về cả 2 token
```

**Response `200 OK`:**
```json
{
  "message": "Login Successful",
  "accessToken": "string",
  "refreshToken": "string"
}
```

**Response `401 Unauthorized`** — Sai email hoặc password:
```json
{
  "status": 401,
  "message": "..."
}
```

---

### POST `/api/auth/logout`

**Header:** `Authorization: Bearer <accessToken>`

**Luồng xử lý:**
```
Lấy token từ Authorization header
    ↓
Kiểm tra header hợp lệ (có "Bearer " prefix)
    ↓
Tính TTL còn lại của token
    ↓
Nếu TTL > 0 → blacklist token trong Redis với TTL tương ứng
```

**Response `200 OK`:**
```json
{
  "message": "Logout success!"
}
```

**Response `400 Bad Request`** — Không có token trong header:
```json
{
  "message": "No token!"
}
```

---

### POST `/api/auth/refresh`

**Header:** `Authorization: Bearer <refreshToken>`

**Luồng xử lý:**
```
Lấy refresh token từ Authorization header
    ↓
Kiểm tra refresh token còn hạn không
    ↓
Extract userId từ subject của refresh token
    ↓
Tìm User theo userId
    ↓
Sinh Access Token mới từ email + role của user
    ↓
Trả về Access Token mới
```

**Response `200 OK`:**
```json
{
  "accessToken": "string"
}
```

**Response `401 Unauthorized`** — Refresh token hết hạn hoặc thiếu:
```json
{
  "status": 401,
  "message": "Refresh token expired"
}
```

**Response `404 Not Found`** — User không tồn tại:
```json
{
  "status": 404,
  "message": "User Not Found!"
}
```

---

## JWT Token

### Access Token
| Thuộc tính | Giá trị |
|---|---|
| Algorithm | HS256 |
| Subject | `email` của user |
| Claim `role` | Role của user (vd: `ROLE_USER`) |
| Expire | Cấu hình qua `jwt.expiration` |
| Secret | Cấu hình qua `jwt.secret` |

### Refresh Token
| Thuộc tính | Giá trị |
|---|---|
| Algorithm | HS256 |
| Subject | `userId` (UUID) của user |
| Expire | Cấu hình qua `jwt.expiration-refresh-token` |
| Secret | Cấu hình qua `jwt.secret_refresh_token` |

> Access Token và Refresh Token dùng **2 secret key khác nhau**.
> Refresh Token **không chứa role** — chỉ chứa userId để tìm user và sinh lại Access Token.

---

## Token Blacklist (Redis)

Khi user logout, Access Token được lưu vào Redis với key `bl_<token>` và TTL = thời gian còn lại của token.

```
Key:   bl_<accessToken>
Value: "1"
TTL:   thời gian còn lại (giây)
```

Mọi request sau đó qua `JwtFilterChain` sẽ bị từ chối nếu token nằm trong blacklist:
```json
{ "message": "Token is blacklisted" }
```

---

## JwtFilterChain

Filter chạy trên mọi request **ngoại trừ** `/api/auth/**`.

**Luồng xử lý:**
```
Lấy token từ Authorization header
    ↓
Kiểm tra token hết hạn
    ↓
Kiểm tra token bị blacklist (Redis)
    ↓
Extract email + role → set vào SecurityContext
```

**Các response lỗi từ filter:**

| Trường hợp | HTTP | Response |
|---|---|---|
| Token hết hạn | 401 | `{"message": "Token expired"}` |
| Token bị blacklist | 401 | `{"message": "Token is blacklisted"}` |
| Token không hợp lệ | 401 | `{"error": "Unauthorized", "message": "Token invalid!"}` |

---

## Custom Exceptions

| Exception | HTTP | Thông điệp mẫu |
|---|---|---|
| `EmailAlreadyExistsException` | 409 | `Email ... in use. Please use another one.!` |
| `TokenExpiredException` | 401 | `Refresh token expired` / `Refresh token missing` |
| `UserNotFoundException` | 404 | `User Not Found!` |