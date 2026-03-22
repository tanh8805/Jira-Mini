    # Module: Project

## Tổng quan

Module Project quản lý vòng đời của một project trong hệ thống, bao gồm: tạo project, cập nhật thông tin, và quản lý thành viên theo role. Mọi thao tác nhạy cảm đều được kiểm tra phân quyền ngay trong Service layer.

---

## Entities

### `Project`

| Field | Type | Ràng buộc |
|---|---|---|
| `id` | UUID | PK, auto-generate |
| `name` | String | NOT NULL |
| `description` | String | nullable |
| `owner` | User | FK, LAZY, NOT NULL |
| `createdAt` | LocalDateTime | auto set khi persist |

### `ProjectMember`

| Field | Type | Ràng buộc |
|---|---|---|
| `id` | UUID | PK, auto-generate |
| `project` | Project | FK, LAZY, NOT NULL |
| `user` | User | FK, LAZY, NOT NULL |
| `role` | ProjectRole | ENUM, NOT NULL |

### `ProjectRole` (enum)

```java
OWNER, MANAGER, MEMBER
```

---

## Repository

### `ProjectRepository`

| Method | Mô tả |
|---|---|
| `findById(UUID)` | Kèm `@EntityGraph(owner)` — tránh N+1 khi truy cập `owner` |

### `ProjectMemberRepository`

| Method | Mô tả |
|---|---|
| `findByProjectId(UUID)` | Kèm `@EntityGraph(user)` — lấy danh sách member kèm user |
| `findByProjectIdAndUserId(UUID, UUID)` | Kiểm tra membership và role |
| `findByUserId(UUID)` | Lấy tất cả project mà user tham gia |

---

## Service — `ProjectService`

Toàn bộ business logic và kiểm tra phân quyền nằm tại đây.

### Helper nội bộ

```java
private ProjectMember getMemberOrThrow(UUID projectId, String email)
```

Fetch `ProjectMember` một lần duy nhất — vừa xác nhận user là thành viên, vừa dùng để check role ngay sau đó, tránh query thừa.

---

### 1. `getProjectsByUserEmail(String email)`

**Mô tả:** Lấy danh sách project mà user đang tham gia (với bất kỳ role nào).

**Luồng xử lý:**
```
Tìm User theo email
    ↓
findByUserId → List<ProjectMember>
    ↓
map → List<Project>
```

**Lỗi có thể xảy ra:**

| Exception | HTTP | Khi nào |
|---|---|---|
| `UserNotFoundException` | 404 | Email không tồn tại |

---

### 2. `createProject(String ownerEmail, String name, String description)`

**Mô tả:** Tạo project mới. Người tạo tự động được thêm vào `project_members` với role `OWNER`.

**Luồng xử lý:**
```
Tìm User theo email
    ↓
Tạo và lưu Project
    ↓
Tạo ProjectMember (role = OWNER) và lưu
    ↓
Trả về Project đã tạo
```

**Lỗi có thể xảy ra:**

| Exception | HTTP | Khi nào |
|---|---|---|
| `UserNotFoundException` | 404 | Email không tồn tại |

---

### 3. `updateProject(UUID projectId, String requesterEmail, String name, String description)`

**Mô tả:** Cập nhật `name` và/hoặc `description` của project. Chỉ **OWNER** mới được phép.

**Luồng xử lý:**
```
Tìm Project theo projectId
    ↓
getMemberOrThrow → lấy ProjectMember của requester
    ↓
Kiểm tra role == OWNER
    ↓
Cập nhật name / description (bỏ qua nếu null/blank)
    ↓
Lưu và trả về Project
```

**Lỗi có thể xảy ra:**

| Exception | HTTP | Khi nào |
|---|---|---|
| `ProjectNotFoundException` | 404 | projectId không tồn tại |
| `UserNotFoundException` | 404 | Email không tồn tại |
| `UnauthorizedProjectAccessException` | 403 | Không phải thành viên hoặc không phải OWNER |

---

### 4. `addMember(UUID projectId, String requesterEmail, String memberEmail, ProjectRole role)`

**Mô tả:** Thêm user mới vào project với role chỉ định. **OWNER** và **MANAGER** đều được phép.

**Luồng xử lý:**
```
Tìm Project theo projectId
    ↓
getMemberOrThrow → lấy ProjectMember của requester
    ↓
Kiểm tra role == OWNER hoặc MANAGER
    ↓
Tìm User cần thêm theo memberEmail
    ↓
Kiểm tra đã là member chưa
    ↓
Tạo ProjectMember mới và lưu
```

**Lỗi có thể xảy ra:**

| Exception | HTTP | Khi nào |
|---|---|---|
| `ProjectNotFoundException` | 404 | projectId không tồn tại |
| `UserNotFoundException` | 404 | requesterEmail hoặc memberEmail không tồn tại |
| `UnauthorizedProjectAccessException` | 403 | Không đủ quyền |
| `MemberAlreadyExistsException` | 409 | User đã là thành viên |

---

## Controllers

Mỗi endpoint tách thành 1 controller riêng, đặt trong package `controller/Project/`.

| Controller | Method | Endpoint | Quyền |
|---|---|---|---|
| `GetProjectController` | GET | `/api/projects` | Đã đăng nhập |
| `CreateProjectController` | POST | `/api/projects` | Đã đăng nhập |
| `UpdateProjectController` | PUT | `/api/projects/{projectId}` | OWNER |
| `AddMemberController` | POST | `/api/projects/{projectId}/members` | OWNER / MANAGER |

> Kiểm tra quyền được thực hiện trong **Service**, không dùng `@PreAuthorize` ở Controller.
> Email của người thực hiện request được lấy từ `authentication.getName()` (JWT token).

---

## DTOs

| DTO | Dùng cho | Validation |
|---|---|---|
| `CreateProjectRequest` | Tạo project | `name` — `@NotBlank` |
| `UpdateProjectRequest` | Cập nhật project | Không bắt buộc (partial update) |
| `AddMemberRequest` | Thêm thành viên | `email` — `@Email @NotBlank`, `role` — `@NotNull` |

---

## Custom Exceptions

| Exception | HTTP Status | Thông điệp mẫu |
|---|---|---|
| `ProjectNotFoundException` | 404 | `Project not found with id: <uuid>` |
| `MemberAlreadyExistsException` | 409 | `User with email '...' is already a member` |
| `UnauthorizedProjectAccessException` | 403 | `Only OWNER can update the project` |

---

## Tối ưu Query

| Vấn đề | Giải pháp |
|---|---|
| N+1 khi load `owner` của Project | `@EntityGraph(attributePaths = {"owner"})` trong `ProjectRepository` |
| N+1 khi load `user` của ProjectMember | `@EntityGraph(attributePaths = {"user"})` trong `ProjectMemberRepository` |
| Query role 2 lần (check quyền + lấy data) | `getMemberOrThrow()` fetch 1 lần, tái sử dụng kết quả |

---

## Ví dụ Request / Response

### Tạo project

```http
POST /api/projects
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "name": "Jira Mini",
  "description": "Quản lý công việc nội bộ"
}
```

```json
// 201 Created
{
  "id": "b3f1c2d4-...",
  "name": "Jira Mini",
  "description": "Quản lý công việc nội bộ",
  "createdAt": "2024-01-15T10:30:00"
}
```

### Thêm thành viên — lỗi không đủ quyền

```http
POST /api/projects/b3f1c2d4-.../members
Authorization: Bearer <access_token_of_member>
```

```json
// 403 Forbidden
{
  "status": 403,
  "message": "Only OWNER or MANAGER can add members"
}
```